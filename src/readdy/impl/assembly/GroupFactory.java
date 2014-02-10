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

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.assembly.IGroupFactory;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroupAllAccess;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.impl.sim.top.group.Group;
import readdy.impl.tools.AdvancedSystemOut;

/**
 *
 * @author schoeneberg
 */
public class GroupFactory implements IGroupFactory {

    IPotentialManager potentialManager = null;
    IGroupParameters groupParameters = null;
    int highestUsedID = -1;

    public IGroupAllAccess createGroup(int groupTypeToCreate, ArrayList<IParticle> positionedParticles) {

        if (newGroupOK(peekNextId(), groupTypeToCreate)) {
            IGroupAllAccess g = new Group(getNextId(), groupTypeToCreate, positionedParticles);
            HashMap<Integer, int[][]> groupPotentials = groupParameters.getGroupInternalPotentials(groupTypeToCreate);
            for (int potId : groupPotentials.keySet()) {
                int[][] internalIdOrInternalIdPairList = groupPotentials.get(potId);
                if (internalIdOrInternalIdPairList[0].length == 1) {
                    // order 1 potential
                    for (int[] singles : internalIdOrInternalIdPairList) {
                        if (singles.length != 1) {
                            throw new RuntimeException("only order 1 potentials allowed in this list");
                        } else {
                            int groupInternalParticleId = singles[0];
                            int correspondingParticleId = positionedParticles.get(groupInternalParticleId).get_id();
                            potentialManager.addPotentialByID(correspondingParticleId, potId);
                        }
                    }
                } else {
                    if (internalIdOrInternalIdPairList[0].length == 2) {
                        // order 2 potential
                        for (int[] pairs : internalIdOrInternalIdPairList) {
                            if (pairs.length != 2) {
                                throw new RuntimeException("only order 2 potentials allowed in this list");
                            } else {
                                int groupInternalParticleId1 = pairs[0];
                                int groupInternalParticleId2 = pairs[1];
                                int correspondingParticleId1 = positionedParticles.get(groupInternalParticleId1).get_id();
                                int correspondingParticleId2 = positionedParticles.get(groupInternalParticleId2).get_id();
                                //System.out.println("create group in groupFactory: pId1,pId2,potId " + correspondingParticleId1 + "," + correspondingParticleId2 + "," + potId);
                                potentialManager.addPotentialByID(correspondingParticleId1, correspondingParticleId2, potId);
                            }
                        }
                    } else {
                        AdvancedSystemOut.println("<internalIdOrInternalIdPairList>", internalIdOrInternalIdPairList, "</internalIdOrInternalIdPairList>");
                        throw new RuntimeException("the expected internalIdOrInternalIdPairList has not the expected form:");
                    }
                }
            }

            return g;
        } else {
            String message = "the group that is supposed to be created is corrupted:"
                    + "id: " + peekNextId()
                    + "type:" + groupTypeToCreate
                    + "positionedparticles: ";
            for (int i = 0; i < positionedParticles.size(); i++) {
                message += "[" + i + "-> id:"
                        + positionedParticles.get(i);
            }

            throw new RuntimeException(message);
        }
    }

    private int getNextId() {
        int id = highestUsedID + 1;
        highestUsedID++;
        return id;
    }

    private int peekNextId() {

        return highestUsedID + 1;
    }

    private boolean newGroupOK(int id, int groupTypeId) {
        return id > highestUsedID
                && groupParameters.doesTypeIdExist(groupTypeId);
    }

    public void set_potentialManager(IPotentialManager potentialManager) {
        this.potentialManager = potentialManager;
    }

    public void set_groupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }
}
