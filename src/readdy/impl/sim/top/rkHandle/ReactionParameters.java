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
import java.util.Iterator;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionParameters;

/**
 *
 * @author schoeneberg
 */
public class ReactionParameters implements IReactionParameters {

    HashMap<Integer, Integer> elmtlRkId_to_rkId_map;
    HashMap<Integer, IReaction> rkId_to_rk_map;

    public IExtendedIdAndType[] getEducts(int rkId) {
        if (rkId_to_rk_map.containsKey(rkId)) {
            IReaction rk = rkId_to_rk_map.get(rkId);
            return rk.getEducts();

        } else {
            throw new RuntimeException("the reaction with id " + rkId + " is unknown.");
        }
    }

    public IExtendedIdAndType[] getProducts(int rkId) {
        if (rkId_to_rk_map.containsKey(rkId)) {
            IReaction rk = rkId_to_rk_map.get(rkId);
            return rk.getProducts();

        } else {
            throw new RuntimeException("the reaction with id " + rkId + " is unknown.");
        }
    }

    public int getCorrespondingTopLevelReactionId(int elmtlRkId) {
        if (elmtlRkId_to_rkId_map.containsKey(elmtlRkId)) {
            return elmtlRkId_to_rkId_map.get(elmtlRkId);

        } else {
            throw new RuntimeException("the elemental reaction with id " + elmtlRkId + " is unknown.");
        }
    }

    public IReaction getReaction(int rkId) {
        if (rkId_to_rk_map.containsKey(rkId)) {
            IReaction rk = rkId_to_rk_map.get(rkId);
            return rk;

        } else {
            throw new RuntimeException("the reaction with id " + rkId + " is unknown.");
        }
    }

    public Iterator<IReaction> reactionIterator() {
        Iterator<IReaction> iter = generateNewIterator();
        return iter;
    }

    private Iterator<IReaction> generateNewIterator() {
        Iterator<IReaction> action = new Iterator() {

            Iterator<IReaction> subIterator = rkId_to_rk_map.values().iterator();

            public boolean hasNext() {
                return subIterator.hasNext();
            }

            public Object next() {
                IReaction next = subIterator.next();
                return next;
            }

            public void remove() {
                subIterator.remove();
            }
        };
        return action;
    }

    public void set_elmtlRkId_to_rkId_map(HashMap<Integer, Integer> elmtlRkId_to_rkId_map) {
        this.elmtlRkId_to_rkId_map = elmtlRkId_to_rkId_map;
    }

    public void set_rkId_to_rk_map(HashMap<Integer, IReaction> rkId_to_rk_map) {
        this.rkId_to_rk_map = rkId_to_rk_map;
    }

    public int getReactionTypeId(int rkId) {
        if (rkId_to_rk_map.containsKey(rkId)) {
            return rkId_to_rk_map.get(rkId).get_reactionTypeId();
        } else {
            throw new RuntimeException("the requested reactionId '" + rkId + "' doesnt exist.");
        }
    }
}
