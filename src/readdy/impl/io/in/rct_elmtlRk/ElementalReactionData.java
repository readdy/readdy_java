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
package readdy.impl.io.in.rct_elmtlRk;

import java.util.ArrayList;
import readdy.api.io.in.rct_elmtlRk.IElementalReactionData;

/**
 *
 * @author schoeneberg
 */
public class ElementalReactionData implements IElementalReactionData {

    private String elmtlRkName;
    private ArrayList<String> eductTypeNames, productTypeNames;
    private int elmtlRkId;
    private ArrayList<Integer> eductParticleIds, productParticleIds;
    private double p;

    public void set_name(String elmtlRkName) {
        this.elmtlRkName = elmtlRkName;
    }

    public void set_elmtlRkId(int elmtlRkId) {
        this.elmtlRkId = elmtlRkId;
    }

    public void set_eductTypeNames(ArrayList<String> eductTypeNames) {
        this.eductTypeNames = eductTypeNames;
    }

    public void set_eductParticleIds(ArrayList<Integer> eductParticleIds) {
        this.eductParticleIds = eductParticleIds;
    }

    public void set_productTypeNames(ArrayList<String> productTypeNames) {
        this.productTypeNames = productTypeNames;
    }

    public void set_productParticleIds(ArrayList<Integer> productParticleIds) {
        this.productParticleIds = productParticleIds;
    }

    public void set_p(double p) {
        this.p = p;
    }

    public String get_name() {
        return elmtlRkName;
    }

    public int get_elmtlRkId() {
        return elmtlRkId;
    }

    public ArrayList<String> get_eductTypeNames() {
        return eductTypeNames;
    }

    public ArrayList<Integer> get_eductParticleIds() {
        return eductParticleIds;
    }

    public ArrayList<String> get_productTypeNames() {
        return productTypeNames;
    }

    public ArrayList<Integer> get_productParticleIds() {
        return productParticleIds;
    }

    public double get_p() {
        return p;
    }

    public void print() {
        System.out.println("<ElmtlReactionData>");
        System.out.println("\t elmtlRkId: " + elmtlRkId + " elmtlRkName: " + elmtlRkName);
        System.out.print("\t eductTypeNames:");
        for (int i = 0; i < eductTypeNames.size(); i++) {
            System.out.print("\t" + eductTypeNames.get(i));
        }
        System.out.println();

        System.out.print("\t eductParticleIds:");
        for (int i = 0; i < eductParticleIds.size(); i++) {
            System.out.print("\t" + eductParticleIds.get(i));
        }
        System.out.println();

        System.out.print("\t productTypeNames:");
        for (int i = 0; i < productTypeNames.size(); i++) {
            System.out.print("\t" + productTypeNames.get(i));
        }
        System.out.println();

        System.out.print("\t productParticleIds:");
        for (int i = 0; i < productParticleIds.size(); i++) {
            System.out.print("\t" + productParticleIds.get(i));
        }
        System.out.println();

        System.out.println("\t p: " + p);
        System.out.println("</ElmtlReactionData>");

    }
}
