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
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.rkHandle.IExecutableReaction;

/**
 *
 * @author schoeneberg
 */
public class ExecutableReaction implements IExecutableReaction {

    int rkId, rkTypeId, derivedFromElmtlRkId;
    HashMap<IParticle, IExtendedIdAndType> educts;
    ArrayList<IExtendedIdAndType> products;

    /**
     * 
     * @param rkId
     * @param rkTypeId
     * @param derivedFromElmtlRkId
     * @param educts
     * @param products
     */
    public ExecutableReaction(int rkId,
            int rkTypeId,
            int derivedFromElmtlRkId,
            HashMap<IParticle, IExtendedIdAndType> educts,
            ArrayList<IExtendedIdAndType> products) {
        this.rkId = rkId;
        this.rkTypeId = rkTypeId;
        this.derivedFromElmtlRkId = derivedFromElmtlRkId;
        this.educts = educts;
        this.products = products;
    }

    public int get_rkId() {
        return this.rkId;
    }

    public int get_rkTypeId() {
        return this.rkTypeId;
    }

    public int get_derivedFromElmtlRkId() {
        return this.derivedFromElmtlRkId;
    }

    public HashMap<IParticle, IExtendedIdAndType> get_educts() {
        return this.educts;
    }

    public ArrayList<IExtendedIdAndType> get_products() {
        return this.products;
    }

    public void print() {
        System.out.println("<executableReaction "
                + "rkId=" + rkId
                + " rkTypeId=" + rkTypeId
                + " derivedFromElmtlRkId=" + derivedFromElmtlRkId + ">");
        System.out.println("\t educts:");
        for (IParticle p : educts.keySet()) {
            p.print();
            educts.get(p).print();
        }
        System.out.println("\tproducts:");
        for (IExtendedIdAndType product : products) {
            product.print();
        }
        System.out.println("</executableReaction>");
    }
}
