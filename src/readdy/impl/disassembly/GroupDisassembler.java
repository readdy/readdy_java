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
package readdy.impl.disassembly;

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupParameters;

/**
 *
 * @author schoeneberg
 */
public class GroupDisassembler implements IGroupDisassembler {

    IPotentialManager potentialManager = null;
    IGroupParameters groupParameters = null;

    /**
     * Takes care of the removal of potentials when a group is disassembled
     * @param group
     */
    public void removeGroup(IGroup group) {
        if (settedUpProperly()) {
            ArrayList<IParticle> positionedMemberParticles = group.get_positionedMemberParticles();
            int groupTypeToRemove = group.get_typeId();

            HashMap<Integer, int[][]> groupPotentials = groupParameters.getGroupInternalPotentials(groupTypeToRemove);
            for (int potId : groupPotentials.keySet()) {
                int[][] internalIdOrInternalIdPairList = groupPotentials.get(potId);
                if (internalIdOrInternalIdPairList[0].length == 1) {
                    // order 1 potential
                    for (int[] singles : internalIdOrInternalIdPairList) {
                        if (singles.length != 1) {
                            throw new RuntimeException("only order 1 potentials allowed in this list");
                        } else {
                            int groupInternalParticleId = singles[0];
                            int correspondingParticleId = positionedMemberParticles.get(groupInternalParticleId).get_id();
                            potentialManager.removePotentialByID(correspondingParticleId, potId);
                        }
                    }
                }
                if (internalIdOrInternalIdPairList[0].length == 2) {
                    // order 2 potential
                    for (int[] pairs : internalIdOrInternalIdPairList) {
                        if (pairs.length != 2) {
                            throw new RuntimeException("only order 2 potentials allowed in this list");
                        } else {
                            int groupInternalParticleId1 = pairs[0];
                            int groupInternalParticleId2 = pairs[1];
                            int correspondingParticleId1 = positionedMemberParticles.get(groupInternalParticleId1).get_id();
                            int correspondingParticleId2 = positionedMemberParticles.get(groupInternalParticleId2).get_id();
                            potentialManager.removePotentialByID(correspondingParticleId1, correspondingParticleId2, potId);
                        }
                    }
                }
            }

        } else {
            throw new RuntimeException("groupDisassembler not settedUp properly");
        }

    }

    private boolean settedUpProperly() {
        return groupParameters != null && potentialManager != null;
    }

    public void set_potentialManager(IPotentialManager potentialManager) {
        this.potentialManager = potentialManager;
    }

    public void set_groupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }
}
