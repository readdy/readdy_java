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
package readdy.impl.io.in.par_rk;

import java.util.ArrayList;
import readdy.api.io.in.par_rk.IReactionData;

/**
 *
 * @author schoeneberg
 */
public class ReactionData implements IReactionData {

    private int rkId;
    private String rkName;
    private String rkTypeName;
    private ArrayList<String[]> educts, products;
    private double kForward, kBackward;

    public void setEducts(ArrayList<String[]> educts) {
        this.educts = educts;
    }

    public void setkBackward(double kBackward) {
        this.kBackward = kBackward;
    }

    public void setkForward(double kForward) {
        this.kForward = kForward;
    }

    public void setProducts(ArrayList<String[]> products) {
        this.products = products;
    }

    public void setRkTypeName(String rkTypeName) {
        this.rkTypeName = rkTypeName;
    }

    public ArrayList<String[]> get_educts() {
        return educts;
    }

    public ArrayList<String[]> get_products() {
        return this.products;
    }

    public double get_kForward() {
        return kForward;
    }

    public double get_kBackward() {
        return kBackward;
    }

    public void print() {
        System.out.println("<ReactionData>");
        System.out.println("\t rkTypeName: " + rkTypeName);
        System.out.println();
        System.out.println("\t educts:");
        for (String[] educt : educts) {
            System.out.println(
                    "\t  groupOrParticle: " + educt[0]
                    + ", currentReactiveInternalIds: " + educt[1]
                    + ", reagentTypeName: " + educt[2]);
        }
        System.out.println();

        for (String[] product : products) {
            System.out.println(
                    "\t  groupOrParticle: " + product[0]
                    + ", currentReactiveInternalIds: " + product[1]
                    + ", reagentTypeName: " + product[2]);
        }
        System.out.println();
        System.out.println();


        System.out.println("\t kForward: " + kForward);
        System.out.println("\t kBackward: " + kBackward);
        System.out.println("</ReactionData>");

    }

    public String get_rkName() {
        return rkName;
    }

    public String get_typeName() {
        return rkTypeName;
    }

    void setId(int rkId) {
        this.rkId = rkId;
    }

    public int get_rkId() {
        return this.rkId;
    }

    void setRkName(String name) {
        this.rkName = name;
    }
}
