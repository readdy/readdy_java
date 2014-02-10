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
 * @author schöneberg, ullrich
 */
public class P2_WeakInteraction implements IPotential2 {
    
    private final int order = 2;
    
    // potential parameters
    public static String[] essentialParameterKeys = new String[]{"id", "name", "type","forceConst",
            "interactionradius","affectedParticleTypeIdPairs","affectedParticleIdPairs"};
    public static String[] defaultParameterValues = new String[]{"-1", "WeakInteraction", "WEAK_INTERACTION", "1",
        "2","null","null"};
    HashMap<String, String> defaultParameterMap = new HashMap(); // is generated in the constructor from the above info


    private int id;
    private String type;
    private String name;
    private double[] coords1, coords2;
    private double[] gradient = new double[3];
    private double r, r0, k, iradius; //iradius, radius in which the weak interactions should be applied
    private double energy;
    private boolean parametersSet, coordsSet, gradientComputed, nrgComputed = false;

    
    HashMap<String, String> parameters;
    
    public P2_WeakInteraction(){
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


    public HashMap<String, String> get_parameterMap() {
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

                gradient[0]=0;
                gradient[1]=0;
                gradient[2]=0;

			double precompute = 0;

			if (r < iradius && r > r0) {
                            precompute = (k* (-r0 + r) * (iradius -r));
                            
                            gradient[0] = precompute * (coords2[0] - coords1[0]);
                            gradient[1] = precompute * (coords2[1] - coords1[1]);
                            gradient[2] = precompute * (coords2[2] - coords1[2]);
                        }

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

            if (r < iradius && r > r0) {
                        energy = 1/12 * k * r*r *(3 * r*r + 6 * r0 * iradius - 4 * r * (r0 + iradius));
                        //energy = ( k * (r - r0) * (r - r0) * (iradius -r) * (iradius -r) );
                    }

            nrgComputed = true;
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

    public void set_parameterMap(HashMap<String, String> parameters) {
        this.parameters = parameters;

        for (int i = 0; i < essentialParameterKeys.length; i++) {
            if (!parameters.containsKey(essentialParameterKeys[i])) {
                throw new RuntimeException("parameters " + essentialParameterKeys[i] + " missing. Abort potential construction!");
            }

        }

        this.id = Integer.parseInt(parameters.get(essentialParameterKeys[0]));

        this.name = parameters.get(essentialParameterKeys[1]);
        this.type= parameters.get(essentialParameterKeys[2]);
        this.k = Double.parseDouble(parameters.get(essentialParameterKeys[3]));
		this.iradius = Double.parseDouble(parameters.get(essentialParameterKeys[4]));
        parametersSet = true;

    }

    
}
