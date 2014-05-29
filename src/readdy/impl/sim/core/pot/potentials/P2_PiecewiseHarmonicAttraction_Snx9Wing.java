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
import readdy.api.sim.core.pot.potentials.IPotential2;

/**
 *
 * @author ullrich
 */
public class P2_PiecewiseHarmonicAttraction_Snx9Wing implements IPotential2 {
    
    private final int order = 2;
    
    // potential parameters
    public static String[] essentialParameterKeys = new String[]{"id", "name", "type","forceConst",
            "depth","length","affectedParticleTypeIdPairs","affectedParticleIdPairs"};
    public static String[] defaultParameterValues = new String[]{"-1", "WeakInteractionPiecewiseHarmonic", "WEAK_INTERACTION_PIECEWISE_HARMONIC", "1",
        "1","2","null","null"};
    HashMap<String, String> defaultParameterMap = new HashMap(); // is generated in the constructor from the above info



    private int id;
    private String type;
    private String name;
    private double[] coords1, coords2;
    private double[] gradient = new double[3];
    private double energy;
    private boolean parametersSet, coordsSet, gradientComputed, nrgComputed = false;
    private double r, r0, k, d, l; // actualParticleDistance, desiredParticleDistance, forceKonst, depth of the potential at r0, length between r0 and the point where the potential is 0 again
    HashMap<String, String> parameters;


    public String[] getEssentialParameterKeys() {
        return essentialParameterKeys;
    }

    public HashMap<String, String> getParameterValues() {
        return parameters;
    }

    public String get_type() {
        return this.type;
    }


    public void set_coordinates(double[] coords1, double[] coords2, double dist, double radius) {
        nrgComputed = false;
        gradientComputed = false;
        if (parametersSet) {

            this.coords1 = coords1;


            this.coords2 = coords2;

            if (coords1.length != coords2.length) {
                throw new RuntimeException("not equal coordinate dimension!");
            }

            this.r = dist;

            this.r0 = radius;
            coordsSet = true;
            computeGradient();
        } else {
            throw new RuntimeException("parameter of the potential not set yet.");
        }

    }

    public double[] getGradient() {
        if (gradientComputed) {
            return gradient;
        } else {
            computeGradient();
            return gradient;
        }
    }

    private void computeGradient() {
        if (coordsSet) {

            gradient[0] = 0;
            gradient[1] = 0;
            gradient[2] = 0;

            
            double dx = (coords2[0] - coords1[0]);
            double dy = (coords2[1] - coords1[1]);
            double dz = (coords2[2] - coords1[2]);
            
            
            double A = (dz * Math.PI)/(2 * r);
            double B = (r0 - (- Math.sqrt(2) * Math.sqrt(d) + Math.sqrt(k) *r0)/Math.sqrt(k));
            // only in z dimension
            double C = (-((dz*dz * Math.PI)/(2*r*Math.sqrt(r))) + Math.PI/(2 * r));

            
            if ((r - r0 - (0.707107 * Math.sqrt(d) * Math.cos(A))/ Math.sqrt(k)) < 0) {
                
                gradient[0] = -((d* dx * dz * Math.PI* Math.sin(A))/(2 * r * Math.sqrt(r))) +  k * (r - r0 - 1/2 * B * Math.cos(A)) * (dx/r - (B* dx *dz *Math.PI* Math.sin(A))/(4 *r*Math.sqrt(r)));
                gradient[1] = -((d* dy * dz * Math.PI* Math.sin(A))/(2 * r * Math.sqrt(r))) +  k * (r - r0 - 1/2 * B * Math.cos(A)) * (dy/r - (B* dy *dz *Math.PI* Math.sin(A))/(4 *r*Math.sqrt(r)));
                gradient[2] =    d * C*                 Math.sin(A)                           +  k * (r - r0 - 1/2 * B * Math.cos(A)) * (dz/r +     1/2 * B* C*       Math.sin(A));
                
                
            } else {
                if (r - r0 - (0.707107 * Math.sqrt(d) * Math.cos(A))/ Math.sqrt(k) >= 0 && -0.5 * l + r - r0 < 0) {
                    gradient[0] = (4 * d * dx * (r - r0) * Math.cos(A))/(l*l*r) + (dx * dz * Math.PI * (-d + (2 * d * (r - r0)*(r - r0))/(l*l)) * Math.sin(A))/(2 * r * Math.sqrt(r));
                    gradient[1] = (4 * d * dy * (r - r0) * Math.cos(A))/(l*l*r) + (dy * dz * Math.PI * (-d + (2 * d * (r - r0)*(r - r0))/(l*l)) * Math.sin(A))/(2 * r * Math.sqrt(r));
                    gradient[2] = (4 * d * dz * (r - r0) * Math.cos(A))/(l*l*r) - C *                   (-d + (2 * d * (r - r0)*(r - r0))/(l*l)) * Math.sin(A);

                } else {
                    if (r >= r0 + 0.5 * l && r < r0 + l) {
                    gradient[0] = -((4 * d * dx * (-l + r - r0) * Math.cos(A))/(l*l*r)) - (d * dx * dz * Math.PI* (-l + r - r0)*(-l + r - r0)* Math.sin(A))/(l*l* r*Math.sqrt(r));
                    gradient[1] = -((4 * d * dy * (-l + r - r0) * Math.cos(A))/(l*l*r)) - (d * dy * dz * Math.PI* (-l + r - r0)*(-l + r - r0)* Math.sin(A))/(l*l* r*Math.sqrt(r));
                    gradient[2] = -((4 * d * dz * (-l + r - r0) * Math.cos(A))/(l*l*r)) + ( 2 * d * C *          (-l + r - r0)*(-l + r - r0)* Math.sin(A))/(l*l);
                    } else {
                        if (r >= r0 + l) {
                            gradient[0] = 0;
                            gradient[1] = 0;
                            gradient[2] = 0;
                        } else {
                            throw new RuntimeException("this line should never be reached!");
                        }
                    }
                }
            }

            
            //System.out.println("precompute: " + precompute + " r " + r + " r0 " + r0 + " l " + l);
            //System.out.println("coords1: " + coords1[0] + ", " + coords1[1] + ", " + coords1[2]);
            //System.out.println("coords2: " + coords2[0] + ", " + coords2[1] + ", " + coords2[2]);
            //System.out.println("gradient: " + gradient[0] + ", " + gradient[1] + ", " + gradient[2]);
            gradientComputed = true;
        } else {
            throw new RuntimeException("coordinates not set!");
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

    private void computeEnergy() {
        if (coordsSet) {
            energy = 0;

            if (r < r0) {

                energy = k / 2 * (r - r0) * (r - r0) - d;
                //System.out.println("f");

                // from here on, k is 1 kJ/mol/nm^2 and therefore omitted
            } else {
                if (r >= r0 && r < r0 + 0.5 * l) {
                    //pot= k/2*    0.5*d   *(1/(0.5*l))*(1/(0.5*l))    *(r-r0)*(r-r0)     -d;
                    energy =  0.5 * d * (1 / (0.5 * l)) * (1 / (0.5 * l)) * (r - r0) * (r - r0) - d;
                    //System.out.println("g");
                } else {
                    if (r >= r0 + 0.5 * l && r < r0 + l) {
                        //pot= -k/2*   0.5*d   *(1/(0.5*l))*(1/(0.5*l))    *(r-r0-l)*(r-r0-l);
                        energy = - 0.5 * d * (1 / (0.5 * l)) * (1 / (0.5 * l)) * (r - r0 - l) * (r - r0 - l);
                        //System.out.println("h");
                    } else {
                        if (r >= r0 + l) {
                            energy = 0;
                            //System.out.println("null");
                        } else {
                            throw new RuntimeException("this line should never be reached!");
                        }
                    }
                }
            }

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

    public void evaluate() {
        computeGradient();
        computeEnergy();
    }


    public int get_order() {
        return this.order;
    }

    public HashMap<String, String> get_defaultParameterMap() {
        return defaultParameterMap;
    }

    public void set_parameterMap(HashMap<String, String> parameters) {
        this.parameters = parameters;

        for (int i = 0; i < essentialParameterKeys.length; i++) {
            if (!parameters.containsKey(essentialParameterKeys[i])) {
                throw new RuntimeException("parameters " + essentialParameterKeys[i] + " missing. Abort potential construction!");
            }

        }

        this.id = Integer.parseInt(parameters.get(essentialParameterKeys[0]));

        this.name = parameters.get(essentialParameterKeys[1]);
        this.type = parameters.get(essentialParameterKeys[2]);

        this.k = Double.parseDouble(parameters.get(essentialParameterKeys[3]));

        this.d = Double.parseDouble(parameters.get(essentialParameterKeys[4]));

        this.l = Double.parseDouble(parameters.get(essentialParameterKeys[5]));

        parametersSet = true;
        
    }

    public HashMap<String, String> get_parameterMap() {
        return parameters;
    }
}
