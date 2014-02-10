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
import java.util.HashSet;
import readdy.api.assembly.IElementalReactionManagerFactory;
import readdy.api.dtypes.IIntPair;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IElementalReactionToCheck;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.impl.dtypes.IntPair;
import readdy.impl.sim.core.rk.ElementalReactionManager;
import readdy.impl.sim.core.rk.ElementalReactionToCheck;

/**
 *
 * @author schoeneberg
 */
public class ElementalReactionManagerFactory_internal implements IElementalReactionManagerFactory {

    IParticleParameters particleParameters;
    HashMap<Integer, IElementalReaction> elmtlReactions;
    private HashMap<Integer, IElementalReactionToCheck> elmtlRkId_to_elmtlRkToCheckMap;
    ArrayList<IElementalReactionToCheck> spontaneousReactionsList;
    private HashMap<Integer, HashSet<Integer>> pTypeId_to_elmtlRk_map;
    private HashMap<Integer, HashSet<Integer>> pId_to_elmtlRk_map;
    private HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_elmtlRk_map;
    private HashMap<IIntPair, HashSet<Integer>> pIdPair_to_elmtlRk_map;
    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> pTypeId_to_pId_to_elmtlRk_map;

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void set_elmtlReactions(HashMap<Integer, IElementalReaction> elmtlReactions) {
        this.elmtlReactions = elmtlReactions;
    }

    public IElementalReactionManager createElementalRactionManager() {

        if (particleParameters != null && elmtlReactions != null) {

            elmtlRkId_to_elmtlRkToCheckMap = new HashMap();
            spontaneousReactionsList = new ArrayList();
            pTypeId_to_elmtlRk_map = new HashMap();
            pId_to_elmtlRk_map = new HashMap();
            pTypeIdPair_to_elmtlRk_map = new HashMap();
            pIdPair_to_elmtlRk_map = new HashMap();
            pTypeId_to_pId_to_elmtlRk_map = new HashMap();

            condenseInputDataToUsefullComponents();
            ElementalReactionManager elemtalReactionManager = new ElementalReactionManager();


            elemtalReactionManager.set_elmtlRkId_to_elmtlRkToCheckMap(elmtlRkId_to_elmtlRkToCheckMap);

            // order 0
            elemtalReactionManager.set_spontaneousReactionsList(spontaneousReactionsList);

            // order 1
            elemtalReactionManager.set_pTypeId_to_elmtlRk_map(pTypeId_to_elmtlRk_map);
            elemtalReactionManager.set_pId_to_elmtlRk_map(pId_to_elmtlRk_map);

            // order 2
            elemtalReactionManager.set_pTypeIdPair_to_elmtlRk_map(pTypeIdPair_to_elmtlRk_map);
            elemtalReactionManager.set_pIdPair_to_elmtlRk_map(pIdPair_to_elmtlRk_map);
            elemtalReactionManager.set_pTypeId_to_pId_to_elmtlRk_map(pTypeId_to_pId_to_elmtlRk_map);

            return elemtalReactionManager;
        } else {
            throw new RuntimeException("insuficint information available to create elemtl reaction manager!");
        }
    }

    private void condenseInputDataToUsefullComponents() {
        for (int elmtlRkId : elmtlReactions.keySet()) {
            IElementalReaction elmtlRk = elmtlReactions.get(elmtlRkId);


            createElementalReactionToCheck(elmtlRk);

            int order = elmtlRk.get_educts().length;
            switch (order) {
                case 0:
                    addZeroOrderReaction(elmtlRk);
                    break;
                case 1:
                    addFirstOrderReaction(elmtlRk);
                    break;
                case 2:
                    addSecondOrderReaction(elmtlRk);
                    break;
                default:
                    throw new RuntimeException("the reaction Order " + order
                            + " is not compatible. Only orders 0,1 and 2 are allowed! ");
            }
        }
    }

    private void createElementalReactionToCheck(IElementalReaction elmtlRk) {
        int elmtlRkId = elmtlRk.get_id();
        ElementalReactionToCheck rkToCheck = new ElementalReactionToCheck();
        rkToCheck.setElmtlRkId(elmtlRkId);
        rkToCheck.setP(elmtlRk.get_p());
        elmtlRkId_to_elmtlRkToCheckMap.put(elmtlRkId, rkToCheck);
    }

    private void addZeroOrderReaction(IElementalReaction elmtlRk) {
        ElementalReactionToCheck rkToCheck = new ElementalReactionToCheck();
        rkToCheck.setElmtlRkId(elmtlRk.get_id());
        rkToCheck.setP(elmtlRk.get_p());
        spontaneousReactionsList.add(rkToCheck);
    }

    private void addFirstOrderReaction(IElementalReaction elmtlRk) {

        int elmtlRkId = elmtlRk.get_id();

        IExtendedIdAndType[] educts = elmtlRk.get_educts();
        if (educts.length == 1) {
            if (educts[0].get_type() != -1) {
                // reaction of order 1 in particle type ids
                int eductTypeId = educts[0].get_type();
                addReactionToRespectiveHashMap(0, eductTypeId, elmtlRkId);

            } else {
                // reaction of order 1 in particle ids
                int eductParticleId = educts[0].get_id();
                addReactionToRespectiveHashMap(1, eductParticleId, elmtlRkId);
            }
        } else {
            throw new RuntimeException("a first order reaction has to have 1 educt and not '" + educts.length + "'.");
        }


    }

    public void addReactionToRespectiveHashMap(int flag, int eductId_typeOrId, int elmtlRkId) {
        HashMap<Integer, HashSet<Integer>> objectToOperateOn;
        switch (flag) {
            // type
            case 0:
                objectToOperateOn = pTypeId_to_elmtlRk_map;
                break;
            // id
            case 1:
                objectToOperateOn = pId_to_elmtlRk_map;
                break;
            default:
                throw new RuntimeException("does not happen normally!");
        }

        // has the particleType already a potential assigned?
        if (objectToOperateOn.containsKey(eductId_typeOrId)) {
            // already a potential present
            HashSet<Integer> setAlready = objectToOperateOn.get(eductId_typeOrId);
            setAlready.add(elmtlRkId);
        } else {
            // not yet present
            HashSet<Integer> set = new HashSet();
            set.add(elmtlRkId);
            objectToOperateOn.put(eductId_typeOrId, set);
        }
    }

    private void addSecondOrderReaction(IElementalReaction elmtlRk) {
        int elmtlRkId = elmtlRk.get_id();
        IExtendedIdAndType[] educts = elmtlRk.get_educts();
        if (educts.length == 2) {
            int educt1_typeId = educts[0].get_type();
            int educt1_pId = educts[0].get_id();
            int educt2_typeId = educts[1].get_type();
            int educt2_pId = educts[1].get_id();

            if ((educt1_typeId == -1 && educt2_typeId != -1)
                    || (educt1_typeId != -1 && educt2_typeId == -1)) {
                // reaction of order 2 in both particle types and ids each 1
                if (educt1_typeId == -1 && educt2_typeId != -1) {
                    // case 1

                    addReactionToSpecialHashMap(educt2_typeId, educt1_pId, elmtlRkId);
                } else {
                    // case2
                    addReactionToSpecialHashMap(educt1_typeId, educt2_pId, elmtlRkId);
                }

            } else {
                if (educt1_typeId != -1 && educt2_typeId != -1) {
                    // reaction of order 2 in particleTypes
                    IntPair pair = new IntPair(educt1_typeId, educt2_typeId);
                    addReactionToRespectiveHashMap(0, pair, elmtlRkId);
                    //elmtlRk.print();
                } else {

                    // reaction of order 2 in particleIds
                    IntPair pair = new IntPair(educt1_pId, educt2_pId);
                    addReactionToRespectiveHashMap(1, pair, elmtlRkId);
                }

            }
        } else {
            throw new RuntimeException("a scnd order reaction has to have 2 educt and not '" + educts.length + "'.");
        }

    }

    public void addReactionToRespectiveHashMap(int flag, IIntPair pair, int elmtlRkId) {
        HashMap<IIntPair, HashSet<Integer>> objectToOperateOn;
        switch (flag) {
            case 0:
                // type
                objectToOperateOn = pTypeIdPair_to_elmtlRk_map;
                break;
            case 1:
                // id
                objectToOperateOn = pIdPair_to_elmtlRk_map;
                break;
            default:
                throw new RuntimeException("does not happen normally!");
        }

        // has the particleType already a potential assigned?
        if (objectToOperateOn.containsKey(pair)) {
            // already a potential present
            HashSet<Integer> setAlready = objectToOperateOn.get(pair);
            setAlready.add(elmtlRkId);

        } else {
            // not yet present
            HashSet<Integer> set = new HashSet();
            set.add(elmtlRkId);
            objectToOperateOn.put(pair, set);
        }
    }

    private void addReactionToSpecialHashMap(int eductTypeId, int eductParticleId, int elmtlRkId) {
        if (pTypeId_to_pId_to_elmtlRk_map.containsKey(eductTypeId)) {

            HashMap<Integer, HashSet<Integer>> eductParticleIdMap = pTypeId_to_pId_to_elmtlRk_map.get(eductTypeId);
            if (eductParticleIdMap.containsKey(eductParticleId)) {
                HashSet<Integer> setAlready = eductParticleIdMap.get(eductParticleId);
                setAlready.add(elmtlRkId);
            } else {
                // build new hash set
                HashSet<Integer> set = new HashSet();
                set.add(elmtlRkId);
                eductParticleIdMap.put(eductParticleId, set);

            }
        } else {
            // build new hash map containing a new hash set
            HashMap<Integer, HashSet<Integer>> map = new HashMap();
            HashSet<Integer> set = new HashSet();
            set.add(elmtlRkId);
            map.put(eductParticleId, set);
            pTypeId_to_pId_to_elmtlRk_map.put(eductTypeId, map);
        }
    }
}
