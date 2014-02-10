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
package readdy.impl.sim.top.group;

import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;

/**
 *
 * @author schoeneberg
 */
public class ExtendedIdAndType implements IExtendedIdAndType {

    int id, type;
    boolean isGroup;

    public ExtendedIdAndType(IParticle p) {
        this.id = p.get_id();
        this.type = p.get_type();
        this.isGroup = false;
    }

    public ExtendedIdAndType(IGroup g) {
        this.id = g.get_id();
        this.type = g.get_typeId();
        this.isGroup = true;
    }

    public ExtendedIdAndType(boolean isGroup, int id, int particleTypeId) {
        this.isGroup = isGroup;
        this.id = id;
        this.type = particleTypeId;
    }

    public boolean get_isGroup() {
        return this.isGroup;
    }

    public int get_id() {
        return this.id;
    }

    public int get_type() {
        return this.type;
    }

    public void print() {
        System.out.println("<ExtendedIdAndType group=" + isGroup + " id=" + id + " type=" + type + "/>");
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtendedIdAndType that = (ExtendedIdAndType) o;


        if (this.get_isGroup() == that.get_isGroup() && this.get_id() == that.get_id() && this.get_type() == that.get_type()) {
            return true;
        }

        return false;

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this != null ? this.hashCode() : 0);
        return hash;
    }
}
