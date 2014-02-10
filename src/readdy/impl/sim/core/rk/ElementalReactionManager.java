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
package readdy.impl.sim.core.rk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import readdy.api.dtypes.IIntPair;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IElementalReactionToCheck;
import readdy.impl.dtypes.IntPair;

/**
 *
 * @author schoeneberg
 */
public class ElementalReactionManager implements IElementalReactionManager {

    private HashMap<Integer, IElementalReactionToCheck> elmtlRkId_to_elmtlRkToCheckMap;
    ArrayList<IElementalReactionToCheck> spontaneousReactionsList;
    private HashMap<Integer, HashSet<Integer>> pTypeId_to_elmtlRk_map;
    private HashMap<Integer, HashSet<Integer>> pId_to_elmtlRk_map;
    private HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_elmtlRk_map;
    private HashMap<IIntPair, HashSet<Integer>> pIdPair_to_elmtlRk_map;
    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> pTypeId_to_pId_to_elmtlRk_map;

    public void set_elmtlRkId_to_elmtlRkToCheckMap(HashMap<Integer, IElementalReactionToCheck> elmtlRkId_to_elmtlRkToCheckMap) {
        this.elmtlRkId_to_elmtlRkToCheckMap = elmtlRkId_to_elmtlRkToCheckMap;
    }

    public void set_spontaneousReactionsList(ArrayList<IElementalReactionToCheck> spontaneousReactionsList) {
        this.spontaneousReactionsList = spontaneousReactionsList;
    }

    public void set_pTypeId_to_elmtlRk_map(HashMap<Integer, HashSet<Integer>> pTypeId_to_elmtlRk_map) {
        this.pTypeId_to_elmtlRk_map = pTypeId_to_elmtlRk_map;
    }

    public void set_pId_to_elmtlRk_map(HashMap<Integer, HashSet<Integer>> pId_to_elmtlRk_map) {
        this.pId_to_elmtlRk_map = pId_to_elmtlRk_map;
    }

    public void set_pTypeIdPair_to_elmtlRk_map(HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_elmtlRk_map) {
        this.pTypeIdPair_to_elmtlRk_map = pTypeIdPair_to_elmtlRk_map;
    }

    public void set_pIdPair_to_elmtlRk_map(HashMap<IIntPair, HashSet<Integer>> pIdPair_to_elmtlRk_map) {
        this.pIdPair_to_elmtlRk_map = pIdPair_to_elmtlRk_map;
    }

    public void set_pTypeId_to_pId_to_elmtlRk_map(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> pTypeId_to_pId_to_elmtlRk_map) {
        this.pTypeId_to_pId_to_elmtlRk_map = pTypeId_to_pId_to_elmtlRk_map;
    }

    public Iterator<IElementalReactionToCheck> getElmtlReactions() {

        return spontaneousReactionsList.iterator();
    }

    public Iterator<IElementalReactionToCheck> getElmtlReactions(IParticle p) {
        ArrayList<IElementalReactionToCheck> queryResult = new ArrayList();
        HashSet<Integer> requestedElmtlIds = new HashSet();
        if (pTypeId_to_elmtlRk_map.containsKey(p.get_type())) {
            requestedElmtlIds.addAll(pTypeId_to_elmtlRk_map.get(p.get_type()));
        }
        if (pId_to_elmtlRk_map.containsKey(p.get_id())) {
            requestedElmtlIds.addAll(pId_to_elmtlRk_map.get(p.get_id()));
        }
        for (int elmtlRkId : requestedElmtlIds) {
            queryResult.add(elmtlRkId_to_elmtlRkToCheckMap.get(elmtlRkId));
        }

        return queryResult.iterator();
    }

    public Iterator<IElementalReactionToCheck> getElmtlReactions(IParticle p1, IParticle p2) {
        if (p1.get_id() != p2.get_id()) {
            ArrayList<IElementalReactionToCheck> queryResult = new ArrayList();
            HashSet<Integer> requestedElmtlIds = new HashSet();
            // not shure if it is more efficient to go over the referenced particle functions
            // or to create those 4 helper elements.
            IIntPair idPair = new IntPair(p1.get_id(), p2.get_id());
            IIntPair typeIdPair = new IntPair(p1.get_type(), p2.get_type());


            if (pTypeIdPair_to_elmtlRk_map.containsKey(typeIdPair)) {
                requestedElmtlIds.addAll(pTypeIdPair_to_elmtlRk_map.get(typeIdPair));
            }
            if (pIdPair_to_elmtlRk_map.containsKey(idPair)) {
                requestedElmtlIds.addAll(pIdPair_to_elmtlRk_map.get(idPair));
            }

            if (pTypeId_to_pId_to_elmtlRk_map.containsKey(p1.get_type())) {

                if (pTypeId_to_pId_to_elmtlRk_map.get(p1.get_type()).containsKey(p2.get_id())) {
                    requestedElmtlIds.addAll(pTypeId_to_pId_to_elmtlRk_map.get(p1.get_type()).get(p2.get_id()));
                }
            }

            if (pTypeId_to_pId_to_elmtlRk_map.containsKey(p2.get_type())) {
                if (pTypeId_to_pId_to_elmtlRk_map.get(p2.get_type()).containsKey(p1.get_id())) {
                    requestedElmtlIds.addAll(pTypeId_to_pId_to_elmtlRk_map.get(p2.get_type()).get(p1.get_id()));
                }
            }

            for (int elmtlRkId : requestedElmtlIds) {
                queryResult.add(elmtlRkId_to_elmtlRkToCheckMap.get(elmtlRkId));
            }

            return queryResult.iterator();
        } else {
            throw new RuntimeException("treatment of identical particles forbidden. A pair only makes sense between two different particles.");
        }
    }
}
