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
package readdy.impl.assembly;

import java.util.HashMap;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.sim.core.pot.potentials.IPotential;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.impl.sim.core.pot.potentials.P1_Cube;
import readdy.impl.sim.core.pot.potentials.P1_Cylinder;
import readdy.impl.sim.core.pot.potentials.P1_Disk;
import readdy.impl.sim.core.pot.potentials.P1_Lollipop;
import readdy.impl.sim.core.pot.potentials.P1_Sphere;
import readdy.impl.sim.core.pot.potentials.P2_Harmonic;
import readdy.impl.sim.core.pot.potentials.P2_WeakInteraction;
import readdy.impl.sim.core.pot.potentials.P2_WeakInteractionPiecewiseHarmonic;
import readdy.impl.sim.core.pot.potentials.P2_WeakInteractionPiecewiseHarmonic_onlyXY;
import readdy.impl.sim.core.pot.potentials.P2_WeakInteractionPiecewiseHarmonic_onlyZ;
import readdy.impl.sim.core.pot.potentials.P2_WeakInteractionPiecewiseHarmonic_zWindow;

/**
 *
 * @author schoeneberg
 */
public class PotentialFactory implements IPotentialFactory {
    boolean verbose = false;
    private int currentId = -1;
    
    private HashMap<String,Integer> availablePotentials = new HashMap();

    public IPotential createPotential(int newPotentialId, HashMap<String, String> userGivenParameters) {
        if(userGivenParameters.containsKey("type")){
              String potentialType = userGivenParameters.get("type");

                //potentials of order 1 (particle-geometry interactions)

                if (potentialType.contentEquals("DISK")) {
                    IPotential1 potential = new P1_Disk();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P1_Disk.essentialParameterKeys, 
                            P1_Disk.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }

                if (potentialType.contentEquals("SPHERE")) {
                    IPotential1 potential = new P1_Sphere();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P1_Sphere.essentialParameterKeys, 
                            P1_Sphere.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }

                if (potentialType.contentEquals("CUBE")) {
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P1_Cube.essentialParameterKeys, 
                            P1_Cube.defaultParameterValues, 
                            userGivenParameters);
                    IPotential1 potential = new P1_Cube();
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                 if (potentialType.contentEquals("CYLINDER")) {
                    IPotential1 potential = new P1_Cylinder();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P1_Cylinder.essentialParameterKeys, 
                            P1_Cylinder.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                 
                  if (potentialType.contentEquals("LOLLIPOP")) {
                    IPotential1 potential = new P1_Lollipop();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P1_Lollipop.essentialParameterKeys, 
                            P1_Lollipop.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    
                    return potential;
                }
                 
                  // potentials of order 2 (particle-particle interactions)
                if (potentialType.contentEquals("HARMONIC")) {
                    IPotential2 potential = new P2_Harmonic();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_Harmonic.essentialParameterKeys, 
                            P2_Harmonic.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }

                if (potentialType.contentEquals("WEAK_INTERACTION")) {
                    IPotential2 potential = new P2_WeakInteraction();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_WeakInteraction.essentialParameterKeys, 
                            P2_WeakInteraction.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                if (potentialType.contentEquals("WEAK_INTERACTION_PIECEWISE_HARMONIC")) {
                    IPotential2 potential = new P2_WeakInteractionPiecewiseHarmonic();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_WeakInteractionPiecewiseHarmonic.essentialParameterKeys, 
                            P2_WeakInteractionPiecewiseHarmonic.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                if (potentialType.contentEquals("WEAK_INTERACTION_PIECEWISE_HARMONIC_ONLY_XY")) {
                    IPotential2 potential = new P2_WeakInteractionPiecewiseHarmonic_onlyXY();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_WeakInteractionPiecewiseHarmonic_onlyXY.essentialParameterKeys, 
                            P2_WeakInteractionPiecewiseHarmonic_onlyXY.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                if (potentialType.contentEquals("WEAK_INTERACTION_PIECEWISE_HARMONIC_ONLY_Z")) {
                    IPotential2 potential = new P2_WeakInteractionPiecewiseHarmonic_onlyZ();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_WeakInteractionPiecewiseHarmonic_onlyZ.essentialParameterKeys, 
                            P2_WeakInteractionPiecewiseHarmonic_onlyZ.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                 if (potentialType.contentEquals("WEAK_INTERACTION_PIECEWISE_HARMONIC_WINDOW_IN_Z")) {
                    IPotential2 potential = new P2_WeakInteractionPiecewiseHarmonic_zWindow();
                    HashMap<String, String> potentialParameters = 
                            matchDefaultAndUserParameters(newPotentialId, 
                            P2_WeakInteractionPiecewiseHarmonic_zWindow.essentialParameterKeys, 
                            P2_WeakInteractionPiecewiseHarmonic_zWindow.defaultParameterValues, 
                            userGivenParameters);
                    potential.set_parameterMap(potentialParameters);
                    return potential;
                }
                
                throw new RuntimeException("no availalbe potential type matched \'"+potentialType+"'. Abort.");
        }else{
            throw new RuntimeException("no type given to the specified potential. potential construction not possible. abort.");
        }
    }
    
        /*
    public IPotential createPotential(int templateId, HashMap<String, String> userGivenParameters) {
        IPotentialTemplate template = potentialTemplateData.get_potentialTemplate(templateId);
        boolean matched = false;
        if (template.get_typeId() == templateId) {
            if (template.get_order() != 1) {
                throw new RuntimeException("unable to create potential of order 1 from template of different order: " + template.get_order());
            } else {
                HashMap<String, String> potentialParameters = matchTemplateAndUserParameters(template, userGivenParameters);

                if (!matched && template.get_typeName().contentEquals("DISK")) {
                    matched = true;
                    IPotential1 potential = new P1_Disk();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }

                if (!matched && template.get_typeName().contentEquals("SPHERE")) {
                    matched = true;
                    IPotential1 potential = new P1_Sphere();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }

                if (!matched && template.get_typeName().contentEquals("CUBE")) {
                    matched = true;
                    IPotential1 potential = new P1_Cube();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }

                if (!matched && template.get_typeName().contentEquals("CYLINDER")) {
                    matched = true;
                    IPotential1 potential = new P1_Cylinder();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }
                
               

            }
            // template id and order in the list of templates should be consistent!
            throw new RuntimeException("problem in the potential factory! potential template type name not known");
        }
        throw new RuntimeException("potential type has not been recognized by the potential factory!");

    }

    public IPotential2 createPotential2(int templateId, HashMap<String, String> userGivenParameters) {
        IPotentialTemplate template = potentialTemplateData.get_potentialTemplateDataList().get(templateId);
        boolean matched = false;
        if (template.get_typeId() == templateId) {
            if (template.get_order() != 2) {
                throw new RuntimeException("unable to create potential of order 1 from template of different order: " + template.get_order());
            } else {
                HashMap<String, String> potentialParameters = matchTemplateAndUserParameters(template, userGivenParameters);

                if (!matched && template.get_typeName().contentEquals("HARMONIC")) {
                    matched = true;
                    IPotential2 potential = new P2_Harmonic();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }

                if (!matched && template.get_typeName().contentEquals("WEAK_INTERACTION")) {
                    matched = true;
                    IPotential2 potential = new P2_WeakInteraction();
                    potential.set_parameterMap(potentialParameters);
                    return potential;

                }

            }
            // template id and order in the list of templates should be consistent!
            throw new RuntimeException("problem in the potential factory! potential template type name not known");
        }
        throw new RuntimeException("potential type has not been recognized by the potential factory!");
    }
    * /

    /**
     * this method matches the default parameters with the given parameters and
     * returns the final parameter set for the potentials.
     * @param template
     * @param userGivenParameters
     * @return
     */
     private HashMap<String, String> matchDefaultAndUserParameters(int newPotentialId, String[] essentialParameterKeys, String[] defaultParameterValues, HashMap<String, String> userGivenParameters) {
        HashMap<String, String> finalParameters = new HashMap();
        for (int i = 0; i < essentialParameterKeys.length; i++) {
            String parameterKey = essentialParameterKeys[i];
            String parameterValue;
            if(userGivenParameters.containsKey(parameterKey)){
                parameterValue = userGivenParameters.get(parameterKey);
            }else{
                parameterValue = defaultParameterValues[i];
            }
            finalParameters.put(parameterKey, parameterValue);   
        }
        finalParameters.put("id",Integer.toString(newPotentialId));
        return finalParameters;
    }

}
