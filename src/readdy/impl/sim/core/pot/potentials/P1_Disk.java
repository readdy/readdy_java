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
import readdy.impl.tools.AdvancedSystemOut;
import readdy.impl.tools.StringTools;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class P1_Disk implements IPotential1 {

    private final int order = 1;
    
    // potential parameters
    public static String[] essentialParameterKeys = new String[]{"id", "name", "type", "subtype","forceConst",
            "center", "normal", "radius", "considerParticleRadius","affectedParticleTypeIds","affectedParticleIds"};
    public static String[] defaultParameterValues = new String[]{"-1", "DISK_0", "DISK", "attractive", "1",
        "[0.0,0.0,0.0]", "[0.0,0.0,1.0]", "1","true","null","null"};
    HashMap<String, String> defaultParameterMap = new HashMap(); // is generated in the constructor from the above info

    
    
    
    private int id;
    private String type;
    private String name;

    // u and v are vectors within the plane, perpendicular to the normal
    private double[] center, normal, centerToPointVec, pointOnDiskPlane, coords1;
    private double[] gradient = new double[3];
    private double diskRadius, distCenterToTheOrigin, normalScalingFactor, distToDiskPlane, distToCenterWithinDiskPlane, pRadius, k;
    private double energy;
    private boolean parametersSet, coordsSet, gradientComputed, nrgComputed = false;
    private PotentialType potentialType;

    
    private enum PotentialType {

        ATTRACTIVE, REPULSIVE
    }
    

    HashMap<String, String> parameters;
    
    public P1_Disk(){
      defaultParameterMap = new HashMap();
        if (essentialParameterKeys.length == defaultParameterValues.length) {
            for (int i = 0; i < essentialParameterKeys.length; i++) {
                defaultParameterMap.put(essentialParameterKeys[i], defaultParameterValues[i]);
            }
        }else{
            throw new RuntimeException("potential construction aborted. implementation error");
        }
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

    public int get_order() {
        return this.order;
    }

    public HashMap<String, String> get_defaultParameterMap() {
        return defaultParameterMap;
    }


    public void set_coordinates(double[] coords1, double radius) {
        nrgComputed = false;
        gradientComputed = false;
        if (parametersSet) {

            this.coords1 = coords1;

            this.pRadius = radius;

            this.normalScalingFactor = computeDistToDiskPlane(this.coords1, normal)-distCenterToTheOrigin;
            this.pointOnDiskPlane = projectPointOnDiscPlane(this.coords1, normal, normalScalingFactor);
            this.distToDiskPlane = Math.abs(normalScalingFactor);
            this.centerToPointVec = DoubleArrays.subtract(pointOnDiskPlane, center);
            this.distToCenterWithinDiskPlane = DoubleArrays.norm(centerToPointVec);

            coordsSet = true;
            computeGradient();
        } else {
            throw new RuntimeException("parameter of the potential not set yet.");
        }


    }

    public void set_parameterMap(HashMap<String, String> parameters) {
        this.parameters = parameters;
        
        for (int i = 0; i < essentialParameterKeys.length; i++) {
            if (!parameters.containsKey(essentialParameterKeys[i])) {
                throw new RuntimeException("parameter "+essentialParameterKeys[i]+" missing. Abort potential construction!");
            }
        }

        this.id = Integer.parseInt(parameters.get(essentialParameterKeys[0]));
        this.name = parameters.get(essentialParameterKeys[1]);
        this.type=parameters.get(essentialParameterKeys[2]);
        String[] types = new String[]{"attractive", "repulsive"};
        String givenType = parameters.get(essentialParameterKeys[3]);

        if (givenType.contentEquals(types[0])) {
            this.potentialType = PotentialType.ATTRACTIVE;
        } else {
            if (givenType.contentEquals(types[1])) {
                this.potentialType = PotentialType.REPULSIVE;
            } else {
                throw new RuntimeException(givenType+ " is an unknown potential type!");
            }
        }

        this.k = Double.parseDouble(parameters.get(essentialParameterKeys[4]));

        this.center = StringTools.splitArrayString_convertToDouble(parameters.get(essentialParameterKeys[5]));
        if (center.length != 3) {
            throw new RuntimeException("center vector dimension != 3");
        }

        double[] rawNormal = StringTools.splitArrayString_convertToDouble(parameters.get(essentialParameterKeys[6]));
        if (rawNormal.length != 3) {
            throw new RuntimeException("normal vector dimension != 3");
        }
        this.normal = DoubleArrays.normalize(rawNormal);

        this.distCenterToTheOrigin = DoubleArrays.dot(center, normal)/DoubleArrays.norm(normal);
        this.diskRadius = Double.parseDouble(parameters.get(essentialParameterKeys[7]));

        parametersSet = true;


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
        if (nrgComputed) {
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

    private void computeGradient() {
        if (coordsSet) {

            
                gradient[0]=0;
                gradient[1]=0;
                gradient[2]=0;


            double precompute = 0;
            double r, r0;
            switch (potentialType) {
                case ATTRACTIVE:
                    // force along normal vector

                    r = distToDiskPlane;// actual
                    r0 = 0;// desired
                    if (r > r0) {
                        precompute = (k * (-r0 + r) / r);
                        
                            gradient[0] = gradient[0]+precompute * (pointOnDiskPlane[0]-coords1[0]);
                            gradient[1] = gradient[1]+precompute * (pointOnDiskPlane[1]-coords1[1]);
                            gradient[2] = gradient[2]+precompute * (pointOnDiskPlane[2]-coords1[2]);
                        
                    }
                    // force within disc plane
                    r = distToCenterWithinDiskPlane + pRadius;// actual
                    r0 = diskRadius;// desired
                    if (r > r0) {
                        precompute = (k * (-r0 + r) / r);
                        
                            gradient[0] = gradient[0]+precompute * (center[0]-pointOnDiskPlane[0]);
                            gradient[1] = gradient[1]+precompute * (center[1]-pointOnDiskPlane[1]);
                            gradient[2] = gradient[2]+precompute * (center[2]-pointOnDiskPlane[2]);
                        
                    }
                    break;
                case REPULSIVE:
                    // force along normal vector
                    r = distToDiskPlane;// actual
                    r0 = pRadius;// desired
                    double r_1 = distToCenterWithinDiskPlane - pRadius;
                    double r0_1 = diskRadius;
                    if (r < r0 && r_1 < r0_1) {

                        precompute = (k * (-r0 + r) / r);
                        
                            gradient[0] = gradient[0]+ precompute * ( pointOnDiskPlane[0]-coords1[0]);
                            gradient[1] = gradient[1]+ precompute * ( pointOnDiskPlane[1]-coords1[1]);
                            gradient[2] = gradient[2]+ precompute * ( pointOnDiskPlane[2]-coords1[2]);
                        
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
            energy = 0;

            double r, r0;
            double nrg = 0;
            switch (potentialType) {
                case ATTRACTIVE:
                    // force along normal vector

                    r = distToDiskPlane;// actual
                    r0 = 0;// desired

                    if (r > r0) {
                        nrg = nrg+ (0.5 * k * (-r0 + r) * (-r0 + r));
                    }
                    // force within disc plane
                    r = distToCenterWithinDiskPlane + pRadius;// actual
                    r0 = diskRadius;// desired
                    if (r > r0) {
                        nrg = nrg+ (0.5 * k * (-r0 + r) * (-r0 + r));
                    }
                    break;
                case REPULSIVE:
                    // force along normal vector
                    r = distToDiskPlane;// actual
                    r0 = pRadius;// desired
                    double r_1 = distToCenterWithinDiskPlane - pRadius;
                    double r0_1 = diskRadius;
                    
                    if (r < r0 && r_1 < r0_1) {
                        nrg = nrg+ (0.5 * k * (-r0 + r) * (-r0 + r));
                    }

                    break;
                default:
                    throw new RuntimeException("potential type not properly set!");
            }
            energy = nrg;
            nrgComputed = true;
        } else {
            throw new RuntimeException("coordinates not set!");
        }
    }



    private double[] projectPointOnDiscPlane(double[] coords1, double[] normal, double factor) {
        return new double[]{
                    coords1[0] - normal[0] * factor,
                    coords1[1] - normal[1] * factor,
                    coords1[2] - normal[2] * factor};

    }

    private double computeDistToDiskPlane(double[] coords, double[] normal) {
        return DoubleArrays.dot(coords, normal)/DoubleArrays.norm(normal);
    }
}
