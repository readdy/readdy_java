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

/**
 *
 * @author schoeneberg
 */
public class P1_Cube implements IPotential1 {

    private final int order = 1;
    // potential parameters
    public static String[] essentialParameterKeys = new String[]{"id", "name", "type", "subtype", "forceConst",
        "origin", "extension", "considerParticleRadius","affectedParticleTypeIds","affectedParticleIds"};
    public static String[] defaultParameterValues = new String[]{"-1", "CUBE_0", "CUBE", "attractive", "1",
        "[-5.0,-5.0,-5.0]", "[5.0,5.0,5.0]", "true","null","null"};
    HashMap<String, String> defaultParameterMap = new HashMap(); // is generated in the constructor from the above info

    
    private int id;
    private String name;
    private String type;


    private enum Subtype {

        ATTRACTIVE, REPULSIVE
    }
    private Subtype subtype;
    private double k; // potential force constant
    private double[] origin, extension;
    private boolean considerParticleRadius = true;
    
    
    //datastructures
    
    HashMap<String, String> parameters;
    // if the particle radius is considered, particles are forced to be e.g. inside the cube with their radius
    // if it is not considered, only their centers are forced to be inside the cube.
    double[] coords1;
    double pRadius;
    // internal variables
    private double[] gradient = new double[3];
    private double energy;
    private boolean parametersSet, coordsSet, gradientComputed, energyComputed = false;
    private double[] min = new double[3];
    private double[] max = new double[3];

    public P1_Cube() {
        defaultParameterMap = new HashMap();
        if (essentialParameterKeys.length == defaultParameterValues.length) {
            for (int i = 0; i < essentialParameterKeys.length; i++) {
                defaultParameterMap.put(essentialParameterKeys[i], defaultParameterValues[i]);
            }
        }else{
            throw new RuntimeException("potential construction aborted. implementation error");
        }
    }
    
    public HashMap<String, String> get_defaultParameterMap() {
        return defaultParameterMap;
    }
    
    public HashMap<String, String> get_parameterMap() {
        return parameters;

    }

    public void set_parameterMap(HashMap<String, String> parameters) {
        // parameters for the potentials are generated in the potential factory
        // from potential template parameters (stored in the potential class) and
        // the user input parameters
        this.parameters = parameters;

        for (int i = 0; i < essentialParameterKeys.length; i++) {
            if (!parameters.containsKey(essentialParameterKeys[i])) {
                throw new RuntimeException("parameter " + essentialParameterKeys[i] + " missing. Abort potential construction!");
            }

        }

        this.id = Integer.parseInt(parameters.get(essentialParameterKeys[0]));
        this.name = parameters.get(essentialParameterKeys[1]);
        this.type = parameters.get(essentialParameterKeys[2]);
        String[] types = new String[]{"attractive", "repulsive"};
        String givenType = parameters.get(essentialParameterKeys[3]);

        if (givenType.contentEquals(types[0])) {
            this.subtype = Subtype.ATTRACTIVE;
        } else {
            if (givenType.contentEquals(types[1])) {
                this.subtype = Subtype.REPULSIVE;
            } else {
                throw new RuntimeException(givenType + " is an unknown potential type!");
            }

        }

        this.k = Double.parseDouble(parameters.get(essentialParameterKeys[4]));

        this.origin = StringTools.splitArrayString_convertToDouble(parameters.get(essentialParameterKeys[5]));
        if (origin.length != 3) {
            throw new RuntimeException("center vector dimension != 3");
        }


        this.extension = StringTools.splitArrayString_convertToDouble(parameters.get(essentialParameterKeys[6]));
        if (extension.length != 3) {
            throw new RuntimeException("center vector dimension != 3");
        }

        this.considerParticleRadius = Boolean.parseBoolean(parameters.get(essentialParameterKeys[7]));



        for (int i = 0; i < 3; i++) {
            if (origin[i] < origin[i] + extension[i]) {
                min[i] = origin[i];
                max[i] = origin[i] + extension[i];
            } else {
                max[i] = origin[i];
                min[i] = origin[i] + extension[i];
            }

        }

        parametersSet = true;

    }

    /*
     * this function provides the potential with new coordinates of a particle and
     * the particle radius.
     * Based on these coordinates, the gradient ist computed.
     * In this function, multiple precomputation can speed up the gradient computation.
     */
    public void set_coordinates(double[] coords1, double radius) {
        energyComputed = false;
        gradientComputed = false;
        if (parametersSet) {
            this.coords1 = coords1;
            this.pRadius = radius;

            coordsSet = true;
            computeGradient();
        } else {
            throw new RuntimeException("parameter of the potential not set yet.");
        }


    }

    public void computeGradient() {
        if (coordsSet) {

            if (!considerParticleRadius) {
                pRadius = 0;
            }


            gradient[0] = 0;
            gradient[1] = 0;
            gradient[2] = 0;




            for (int i = 0; i < coords1.length; i++) {
                // are we in between of origin and extension?

                if (coords1[i] - pRadius >= min[i] && coords1[i] + pRadius <= max[i]) {
                    gradient[i] = 0;
                } else {
                    if (coords1[i] - pRadius < min[i]) {
                        double r = coords1[i] - pRadius;
                        double r0 = min[i];
                        gradient[i] = gradient[i] + (k * (r - r0));
                    } else {
                        double r = coords1[i] + pRadius;
                        double r0 = max[i];
                        gradient[i] = gradient[i] + (k * (r - r0));
                    }
                }

            }

            switch (subtype) {
                case ATTRACTIVE:
                    for (int i = 0; i < gradient.length; i++) {
                        gradient[i] = -1 * gradient[i];
                    }
                    break;
                case REPULSIVE:

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

            if (!considerParticleRadius) {
                pRadius = 0;
            }

            energy = 0;

            for (int i = 0; i < coords1.length; i++) {
                // are we in between of origin and extension?

                if (coords1[i] - pRadius >= min[i] && coords1[i] + pRadius <= max[i]) {
                    energy += 0;
                } else {
                    if (coords1[i] - pRadius < min[i]) {
                        double r = coords1[i] - pRadius;
                        double r0 = min[i];
                        energy += 0.5 * k * (r - r0) * (r - r0);

                    } else {
                        double r = coords1[i] + pRadius;
                        double r0 = max[i];
                        energy += 0.5 * k * (r - r0) * (r - r0);
                    }
                }
            }


            energyComputed = true;
        } else {
            throw new RuntimeException("coordinates not set!");
        }
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

    public String get_type() {
        return this.type;
    }

    public int get_order() {
        return order;
    }
}