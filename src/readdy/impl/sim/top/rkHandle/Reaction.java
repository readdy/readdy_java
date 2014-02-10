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

import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.rkHandle.IReaction;

/**
 *
 * @author schoeneberg
 */
public class Reaction implements IReaction {

    int id, rkTypeId;
    String rkTypeName, rkName;
    double k, p;
    IExtendedIdAndType[] educts, products;

    public int get_id() {
        return this.id;
    }

    public int get_reactionTypeId() {
        return this.rkTypeId;
    }

    public String get_reactionTypeName() {
        return this.rkTypeName;
    }

    public String get_name() {
        return this.rkName;
    }

    public double get_k() {
        return this.k;
    }

    public double get_p() {
        return this.p;
    }

    public IExtendedIdAndType[] getEducts() {
        return this.educts;
    }

    public IExtendedIdAndType[] getProducts() {
        return this.products;
    }

    public void print() {
        System.out.println("<reaction>");
        System.out.println("\t<id>" + id + "</id>");
        System.out.println("\t<name>" + rkName + "</name>");
        System.out.println("\t<typeId>" + rkTypeId + "</typeId>");
        System.out.println("\t<rkTypeName>" + rkTypeName + "</rkTypeName>");
        System.out.println("\t<k>" + k + "</k>");
        System.out.println("\t<p>" + p + "</p>");
        System.out.println("\t<educts>");
        for (IExtendedIdAndType educt : educts) {
            educt.print();
        }
        System.out.println("\t</educts>");
        System.out.println("\t<products>");
        for (IExtendedIdAndType product : products) {
            product.print();
        }
        System.out.println("\t</products>");
        System.out.println("</reaction>");
    }

    public void setEducts(IExtendedIdAndType[] educts) {
        this.educts = educts;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setP(double p) {
        this.p = p;
    }

    public void setProducts(IExtendedIdAndType[] products) {
        this.products = products;
    }

    public void setRkName(String rkName) {
        this.rkName = rkName;
    }

    public void setRkTypeId(int rkTypeId) {
        this.rkTypeId = rkTypeId;
    }

    public void setRkTypeName(String rkTypeName) {
        this.rkTypeName = rkTypeName;
    }
}
