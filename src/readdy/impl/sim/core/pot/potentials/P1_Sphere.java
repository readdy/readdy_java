/*===========================================================================*\
*           ReaDDy - The Library for Reaction Diffusion Dynamics              *
* =========================================================================== *
* Copyright (c) 2010-2013, Johannes Schöneberg, Frank Noé, FU Berlin          *
* All rights reserved.                                                        *
*                                                                             *
* Redistribution and use in source and binary forms, with or without          *
* modification, are permitted provided that the following conditions are met: *
*                                                                             *
*     * Redistributions of source code must retain the above copyright        *
*       notice, this list of conditions and the following disclaimer.         *
*     * Redistributions in binary form must reproduce the above copyright     *
*       notice, this list of conditions and the following disclaimer in the   *
*       documentation and/or other materials provided with the distribution.  *
*     * Neither the name of Johannes Schöneberg or Frank Noé or the FU Berlin *
*       nor the names of its contributors may be used to endorse or promote   *
*       products derived from this software without specific prior written    *
*       permission.                                                           *
*                                                                             *
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" *
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE   *
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE  *
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE   *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR         *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF        *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS    *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN     *
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)     *
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  *
* POSSIBILITY OF SUCH DAMAGE.                                                 *
*                                                                             *
\*===========================================================================*/
package readdy.impl.sim.core.pot.potentials;

import java.util.HashMap;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.impl.tools.StringTools;
import statlab.base.util.DoubleArrays;

/**
 *
 * 3D / 2D Potential Objects can have subtypes of the following kind if their surface
 * is closed.
 * 
 * attractive:
 * This means, that the potenial within the 3D object is 0
 * And particles outside of the object are attracted to it
 * 
 * repulsive
 * this means, that the potential outside of the 3D object is 0
 * and particles inside of the object are repelled from it to the outside
 * 
 * membrane
 * this means, that the potential is only 0 on the surface of the object
 * and particles from outside or inside the object are attracted to it.
 * 
 * there may exist cutoffs that constrain the potential. These are defined
 * individually for each potential.
 * 
 * @author schoeneberg
 */
public class P1_Sphere implements IPotential1 {
    
    private final int order = 1;
    
    // potential parameters
    public static String[] essentialParameterKeys = new String[]{"id", "name", "type", "subtype","forceConst",
            "center", "radius", "considerParticleRadius","affectedParticleTypeIds","affectedParticleIds"};
    public static String[] defaultParameterValues = new String[]{"-1", "SPHERE", "SPHERE", "attractive", "1",
        "[0.0,0.0,0.0]", "1","true","null","null"};
    HashMap<String, String> defaultParameterMap = new HashMap(); // is generated in the constructor from the above info


    private int id;
    private String type;
    private String name;
    // u and v are vectors within the plane, perpendicular to the normal
    private double[] center, coords1, centerToPointVec;
    private double[] gradient = new double[3];
    private double sphereRadius, k, pRadius, distToSphereCenter;
    private double energy;
    private boolean parametersSet, coordsSet, gradientComputed, energyComputed = false;
    private PotentialType potentialType;
    private boolean considerParticleRadius = true;
    HashMap<String, String> parameters;

    public P1_Sphere() {
        defaultParameterMap = new HashMap();
        if (essentialParameterKeys.length == defaultParameterValues.length) {
            for (int i = 0; i < essentialParameterKeys.length; i++) {
                defaultParameterMap.put(essentialParameterKeys[i], defaultParameterValues[i]);
            }
        }else{
            throw new RuntimeException("potential construction aborted. implementation error");
        }
    }

    public int get_order() {
        return order;
    }

    public HashMap<String, String> get_defaultParameterMap() {
        return defaultParameterMap;
    }
    
    public void set_parameterMap(HashMap<String, String> parameters) {
        this.parameters = parameters;

        for (int i = 0; i < essentialParameterKeys.length; i++) {
            if (!parameters.containsKey(essentialParameterKeys[i])) {
                throw new RuntimeException("parameter " + essentialParameterKeys[i] + " missing. Abort potential construction!");
            }
        }


        this.id = Integer.parseInt(parameters.get(essentialParameterKeys[0]));
        this.name = parameters.get(essentialParameterKeys[1]);
        this.type = parameters.get(essentialParameterKeys[2]);
        String[] types = new String[]{"attractive", "repulsive", "membrane"};
        String givenSubtype = parameters.get(essentialParameterKeys[3]);

        if (givenSubtype.contentEquals(types[0])) {
            this.potentialType = PotentialType.ATTRACTIVE;
        } else {
            if (givenSubtype.contentEquals(types[1])) {
                this.potentialType = PotentialType.REPULSIVE;
            } else {
                if (givenSubtype.contentEquals(types[2])) {
                    this.potentialType = PotentialType.MEMBRANE;
                } else {
                    throw new RuntimeException(givenSubtype + " is an unknown potential type!");
                }
            }
        }

        this.k = Double.parseDouble(parameters.get(essentialParameterKeys[4]));

        this.center = StringTools.splitArrayString_convertToDouble(parameters.get(essentialParameterKeys[5]));
        if (center.length != 3) {
            throw new RuntimeException("center vector dimension != 3");
        }

        this.sphereRadius = Double.parseDouble(parameters.get(essentialParameterKeys[6]));

        this.considerParticleRadius = Boolean.parseBoolean(parameters.get(essentialParameterKeys[7]));
        
        parametersSet = true;


    }

    public String[] getEssentialParameterKeys() {
        return essentialParameterKeys;
    }

    public HashMap<String, String> get_parameterMap() {
        return parameters;
    }

    public String get_type() {
        return this.type;
    }

    

    private enum PotentialType {

        ATTRACTIVE, REPULSIVE, MEMBRANE
    }

    public String get_name() {
        return name;
    }

    public int get_id() {
        return id;
    }

    public double[] getGradient() {
        if (gradientComputed) {
            return gradient;
        } else {
            computeGradient();
            return gradient;
        }
    }

    public double getEnergy() {
        if (energyComputed) {
            return energy;
        } else {
            computeEnergy();
            return energy;
        }
    }

    public void evaluate() {
        computeGradient();
        computeEnergy();
    }

    public void set_coordinates(double[] coords1, double radius) {
        energyComputed = false;
        gradientComputed = false;
        if (parametersSet) {

            this.coords1 = coords1;

            this.pRadius = radius;

            this.distToSphereCenter = DoubleArrays.norm(DoubleArrays.subtract(coords1, center));

            coordsSet = true;
            computeGradient();
        } else {
            throw new RuntimeException("parameter of the potential not set yet.");
        }


    }

    private void computeGradient() {
        if (coordsSet) {

            if(!considerParticleRadius){pRadius = 0;}
            
            gradient[0] = 0;
            gradient[1] = 0;
            gradient[2] = 0;


            double precompute = 0;
            double r, r0;
            switch (potentialType) {
                case ATTRACTIVE:

                    // if you are outside, you are forced in. Inside the potential is 0
                    r = distToSphereCenter + pRadius;// actual
                    r0 = sphereRadius;// desired
                    if (r > r0) {
                        precompute = (k * (-r0 + r) / r);

                        gradient[0] = gradient[0] + precompute * (center[0] - coords1[0]);
                        gradient[1] = gradient[1] + precompute * (center[1] - coords1[1]);
                        gradient[2] = gradient[2] + precompute * (center[2] - coords1[2]);

                    }
                    break;
                case REPULSIVE:

                    // if you are inside, you are forced out. Outside, the potential is 0 
                    r = distToSphereCenter;// actual
                    r0 = sphereRadius + pRadius;// desired

                    if (r < r0) {
                        precompute = (k * (-r0 + r) / r);

                        gradient[0] = gradient[0] + precompute * (center[0] - coords1[0]);
                        gradient[1] = gradient[1] + precompute * (center[1] - coords1[1]);
                        gradient[2] = gradient[2] + precompute * (center[2] - coords1[2]);
                    }

                    break;

                case MEMBRANE:

                    // if you are not on the surface, you are pushed to it. On the surface, the potential is 0.
                    r = distToSphereCenter;// actual
                    r0 = sphereRadius;// desired

                    if (r != r0) {

                        precompute = (k * (-r0 + r) / r);

                        gradient[0] = gradient[0] + precompute * (center[0] - coords1[0]);
                        gradient[1] = gradient[1] + precompute * (center[1] - coords1[1]);
                        gradient[2] = gradient[2] + precompute * (center[2] - coords1[2]);

                    }

                    break;

                default:
                    throw new RuntimeException("potential type not properly set!");
            }
            gradientComputed = true;
        } else {
            throw new RuntimeException("coordinates not set!");
        }
    }

    private void computeEnergy() {
         if (coordsSet) {
             energy=0;

            if(!considerParticleRadius){pRadius = 0;}

            double r, r0;
            switch (potentialType) {
                case ATTRACTIVE:

                    // if you are outside, you are forced in. Inside the potential is 0
                    r = distToSphereCenter + pRadius;// actual
                    r0 = sphereRadius;// desired
                    if (r > r0) {
                        energy += 0.5*k * (-r0 + r)* (-r0 + r);
                    }
                    break;
                case REPULSIVE:

                    // if you are inside, you are forced out. Outside, the potential is 0 
                    r = distToSphereCenter;// actual
                    r0 = sphereRadius + pRadius;// desired

                    if (r < r0) {
                        energy += 0.5*k * (-r0 + r)* (-r0 + r);
                    }

                    break;

                case MEMBRANE:

                    // if you are not on the surface, you are pushed to it. On the surface, the potential is 0.
                    r = distToSphereCenter;// actual
                    r0 = sphereRadius;// desired

                    if (r != r0) {
                        energy += 0.5*k * (-r0 + r)* (-r0 + r);
                    }

                    break;

                default:
                    throw new RuntimeException("potential type not properly set!");
            }
            energyComputed = true;
        } else {
            throw new RuntimeException("coordinates not set!");
        }
        
    }
}
