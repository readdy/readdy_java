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

import java.util.HashMap;
import java.util.HashSet;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;

/**
 * The reaction manager knows about all reactions that are possible
 * within the current simulator setup.
 * it provides the map between a reaction type id and the according
 * reaction executor.
 * Additionally, it provides a map between the reaction type id and a
 * human readable name of the reaction.
 * 
 * @author schoeneberg
 */
public class ReactionManager implements IReactionManager {

    HashMap<Integer, Integer> reactionTypeInversion_map = new HashMap();
    HashMap<String, Integer> reactionTypeName_to_reactionTypeId_map = new HashMap();
    HashMap<Integer, String> reactionTypeId_to_reactionTypeName_map = new HashMap();
    HashSet<Integer> standardParticleBasedReactionTypes = new HashSet();
    HashSet<Integer> standardGroupBasedReactionTypes = new HashSet();
    IReactionExecutor standardParticleBasedRkExecutor;
    IReactionExecutor standardGroupBasedRkExecutor;
    HashMap<Integer, IReactionExecutor> reactionId_to_reactionExecutor_map = new HashMap();

    /*
     * If the user defines own reactions they start with id 1000
     */
    int nextFreeCustomReactionId = 1000;

    public void setStandardGroupBasedRkExecutor(IReactionExecutor standardGroupBasedRkExecutor) {
        this.standardGroupBasedRkExecutor = standardGroupBasedRkExecutor;
    }

    public void setStandardParticleBasedRkExecutor(IReactionExecutor standardParticleBasedRkExecutor) {
        this.standardParticleBasedRkExecutor = standardParticleBasedRkExecutor;
    }

    public void registerAdditionalReaction(int rkId, String rkTypeName, int rkIdBackward, IReactionExecutor executor) {
        reactionId_to_reactionExecutor_map.put(rkId, executor);
        reactionTypeInversion_map.put(rkId, rkIdBackward);
        reactionTypeId_to_reactionTypeName_map.put(rkId, rkTypeName);
        reactionTypeName_to_reactionTypeId_map.put(rkTypeName, rkId);
    }

    public int getNextFreeReactionId() {
        nextFreeCustomReactionId++;
        return nextFreeCustomReactionId;

    }

    public ReactionManager() {
        // particle based
        reactionTypeInversion_map.put(0, 1);
        reactionTypeInversion_map.put(1, 0);
        reactionTypeInversion_map.put(2, 3);
        reactionTypeInversion_map.put(3, 2);
        reactionTypeInversion_map.put(4, 4);
        reactionTypeInversion_map.put(5, 6);
        reactionTypeInversion_map.put(6, 5);
        reactionTypeInversion_map.put(7, 8);
        reactionTypeInversion_map.put(8, 7);
        reactionTypeInversion_map.put(9, 9);
        reactionTypeInversion_map.put(10, 10);



        // group based
        reactionTypeInversion_map.put(100, 101);
        reactionTypeInversion_map.put(101, 100);
        reactionTypeInversion_map.put(102, 102);
        reactionTypeInversion_map.put(103, 104);
        reactionTypeInversion_map.put(104, 103);
        reactionTypeInversion_map.put(105, 105);
        reactionTypeInversion_map.put(106, 106);

        // particle based
        reactionTypeId_to_reactionTypeName_map.put(0, "creation");
        reactionTypeId_to_reactionTypeName_map.put(1, "decay");
        reactionTypeId_to_reactionTypeName_map.put(2, "doubleCreation");
        reactionTypeId_to_reactionTypeName_map.put(3, "annihilation");
        reactionTypeId_to_reactionTypeName_map.put(4, "typeConversion");
        reactionTypeId_to_reactionTypeName_map.put(5, "birth");
        reactionTypeId_to_reactionTypeName_map.put(6, "death");
        reactionTypeId_to_reactionTypeName_map.put(7, "fission");
        reactionTypeId_to_reactionTypeName_map.put(8, "fusion");
        reactionTypeId_to_reactionTypeName_map.put(9, "enzymatic");
        reactionTypeId_to_reactionTypeName_map.put(10, "doubleTypeConversion");





        // group based
        reactionTypeId_to_reactionTypeName_map.put(100, "group");
        reactionTypeId_to_reactionTypeName_map.put(101, "ungroup");
        reactionTypeId_to_reactionTypeName_map.put(102, "gTypeConversion");
        reactionTypeId_to_reactionTypeName_map.put(103, "gFission");
        reactionTypeId_to_reactionTypeName_map.put(104, "gFusion");
        reactionTypeId_to_reactionTypeName_map.put(105, "gEnzymatic");
        reactionTypeId_to_reactionTypeName_map.put(106, "gDoubleTypeConversion");

        for (int rkTypeId : reactionTypeId_to_reactionTypeName_map.keySet()) {
            String rkTypeName = reactionTypeId_to_reactionTypeName_map.get(rkTypeId);
            reactionTypeName_to_reactionTypeId_map.put(rkTypeName, rkTypeId);
        }

        standardParticleBasedReactionTypes.add(0);
        standardParticleBasedReactionTypes.add(1);
        standardParticleBasedReactionTypes.add(2);
        standardParticleBasedReactionTypes.add(3);
        standardParticleBasedReactionTypes.add(4);
        standardParticleBasedReactionTypes.add(5);
        standardParticleBasedReactionTypes.add(6);
        standardParticleBasedReactionTypes.add(7);
        standardParticleBasedReactionTypes.add(8);
        standardParticleBasedReactionTypes.add(9);
        standardParticleBasedReactionTypes.add(10);

        standardGroupBasedReactionTypes.add(100);
        standardGroupBasedReactionTypes.add(101);
        standardGroupBasedReactionTypes.add(102);
        standardGroupBasedReactionTypes.add(103);
        standardGroupBasedReactionTypes.add(104);
        standardGroupBasedReactionTypes.add(105);
        standardGroupBasedReactionTypes.add(106);


    }

    public int get_reactionTypeId(String reactionTypeName) {
        if (reactionTypeName_to_reactionTypeId_map.containsKey(reactionTypeName)) {
            return reactionTypeName_to_reactionTypeId_map.get(reactionTypeName);
        } else {
            throw new RuntimeException("the requested reaction type '" + reactionTypeName + "' is not known.");
        }
    }

    public String get_reactionTypeName(int rkTypeId) {
        if (reactionTypeId_to_reactionTypeName_map.containsKey(rkTypeId)) {
            return reactionTypeId_to_reactionTypeName_map.get(rkTypeId);
        } else {
            throw new RuntimeException("the requested reaction typeId '" + rkTypeId + "' is not known.");
        }
    }

    public int get_inverseReactionTypeId(int reactionTypeId) {
        if (reactionTypeInversion_map.containsKey(reactionTypeId)) {
            return reactionTypeInversion_map.get(reactionTypeId);
        } else {
            throw new RuntimeException("the requested reaction typeId '" + reactionTypeId + "' is not known.");
        }
    }

    public IReactionExecutor getReactionExecutor(int rkTypeId) {
        if (standardParticleBasedReactionTypes.contains(rkTypeId)) {
            return standardParticleBasedRkExecutor;
        } else {
            if (standardGroupBasedReactionTypes.contains(rkTypeId)) {
                return standardGroupBasedRkExecutor;
            } else {
                if (reactionId_to_reactionExecutor_map.containsKey(rkTypeId)) {
                    return reactionId_to_reactionExecutor_map.get(rkTypeId);
                } else {


                    switch (rkTypeId) {
                        // possible additional reactions to be added HERE
                        default:
                            throw new RuntimeException("the reaction '" + rkTypeId + "' is not yet supported.");

                    }
                }
            }
        }
    }
}
