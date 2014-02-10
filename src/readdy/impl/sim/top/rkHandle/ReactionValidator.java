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
package readdy.impl.sim.top.rkHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionValidator;

/**
 *
 * @author schoeneberg
 */
public class ReactionValidator implements IReactionValidator {

    IGroupParameters groupParameters;
    IGroupConfiguration groupConfiguration;
    private static boolean verbose = false;

    public void setGroupConfiguration(IGroupConfiguration groupConfiguration) {
        this.groupConfiguration = groupConfiguration;
    }

    public void setGroupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }


    public boolean validateReaction(IExecutableReaction rk) {
        boolean isValid = true;



        // check if the particles exist
        HashMap<IParticle, IExtendedIdAndType> educts = rk.get_educts();
        ArrayList<IExtendedIdAndType> products = rk.get_products();

        //##############################################################################
        // check if educt particles are not identical
        HashSet<Integer> foundParticles = new HashSet();
        for (IParticle eductParticle : educts.keySet()) {
            int pId = eductParticle.get_id();
            if (foundParticles.contains(pId)) {
                isValid = false;
                return isValid;
            } else {
                foundParticles.add(pId);
            }
        }



        //##############################################################################
        // check if the max number of assignments per particle is reached for
        // the product type.
        
        for (IExtendedIdAndType product : products) {
            
            for (IParticle eductParticle : educts.keySet()) {
                if (product.get_isGroup() == true) {
                    // get the max number of assignments per particle from the given group type
                    int groupTypeId = product.get_type();
                    int maxNAssignments = groupParameters.getMaxNumberOfAssignmentsPerParticle(groupTypeId);
                    int currNAssignments = groupConfiguration.getCurrNAssignmentsOfGroupType(eductParticle.get_id(), groupTypeId);
                    if (currNAssignments +1 > maxNAssignments) {
                        if (verbose) {
                            System.out.println("maxN Assignments reached - reaction is not allowed.");
                        }
                        isValid = false;
                        return isValid;
                    }
                }
            }
        }

        return isValid;
    }

    private boolean settedUpCorrectly() {
        return groupParameters != null && groupConfiguration != null;
    }
}
