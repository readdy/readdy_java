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
package readdy.impl.io.in.tpl_group;

import java.util.HashMap;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileDataEntry;

/**
 *
 * @author schoeneberg
 */
class TplgyGroupsFileDataEntry implements ITplgyGroupsFileDataEntry {

    int id, typeId;
    HashMap<Integer, Integer> internalId2ParticleIdMap;

    public void set_id(int id) {
        this.id = id;
    }

    public void set_internalId2ParticleIdMap(HashMap<Integer, Integer> internalId2ParticleIdMap) {
        this.internalId2ParticleIdMap = internalId2ParticleIdMap;
    }

    public void set_typeId(int typeId) {
        this.typeId = typeId;
    }

    public int get_id() {
        return this.id;
    }

    public int get_typeId() {
        return this.typeId;
    }

    public HashMap<Integer, Integer> get_internalAndParticleId() {
        return internalId2ParticleIdMap;
    }

    public void print() {
        System.out.print("<g id: " + id + " type: " + typeId + " : internalAndParticleId:");
        int size = internalId2ParticleIdMap.keySet().size();
        int i = 0;
        System.out.print("[");
        for (int key : internalId2ParticleIdMap.keySet()) {
            if (i == size - 1) {
                System.out.print("[" + key + "," + internalId2ParticleIdMap.get(key) + "]");
            } else {
                System.out.print("[" + key + "," + internalId2ParticleIdMap.get(key) + "],");
            }
            i++;
        }
        System.out.println("]");
    }
}
