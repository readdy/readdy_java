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
package readdy.impl.io.in.par_group;

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.io.in.par_group.IGroupData;
import readdy.impl.tools.AdvancedSystemOut;

/**
 *
 * @author schoeneberg
 */
public class GroupData implements IGroupData {

    int id;
    String typeName;
    ArrayList<String[]> buildingBlocks = new ArrayList();
    ArrayList<HashMap<String, String>> involvedPotentials = new ArrayList();
    int maxNumberOfGroupAssignmentsPerParticle;
    double[] templateOrigin,templateNormal;

    public double[] getTemplateNormal() {
        return templateNormal;
    }

    public double[] getTemplateOrigin() {
        return templateOrigin;
    }


    public void setBuildingBlocks(ArrayList<String[]> buildingBlocks) {
        this.buildingBlocks = buildingBlocks;
    }

    public void setInvolvedPotentials(ArrayList<HashMap<String, String>> involvedPotentials) {
        this.involvedPotentials = involvedPotentials;
    }

    public void setId(int typeId) {
        this.id = typeId;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }



    void setMaxNumberOfGroupAssignmentsPerParticle(int maxNumberOfGroupAssignmentsPerParticle) {
        this.maxNumberOfGroupAssignmentsPerParticle = maxNumberOfGroupAssignmentsPerParticle;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public int getTypeId() {
        return this.id;
    }

    public ArrayList<String[]> getBuildingBlocks() {
        return this.buildingBlocks;
    }

    public ArrayList<HashMap<String, String>> getInvolvedPotentials() {
        return this.involvedPotentials;
    }

    public int getMaxNumberOfGroupAssignmentsPerParticle() {
        return this.maxNumberOfGroupAssignmentsPerParticle;
    }

    public void print() {
        System.out.println("<groupData>");
        System.out.println("\t ID: " + id + " Type: " + typeName);
        AdvancedSystemOut.println("\t templateOrigin: ", templateOrigin, "");
        AdvancedSystemOut.println("\t templateNormal: ", templateNormal, "");
        System.out.println("\t buildingBlocks:");
        for (String[] block : buildingBlocks) {
            System.out.print("\t block: ");
            for (int i = 0; i < block.length; i++) {
                System.out.print(block[i] + ", ");
            }
            System.out.println();
        }
        System.out.println("\t involvedPotentials: ");

        for (HashMap<String, String> potParamSet : involvedPotentials) {
            for (String parameterName : potParamSet.keySet()) {
                String paramValue = potParamSet.get(parameterName);
                System.out.println("\t" + parameterName + " -> " + paramValue);
            }
        }

        System.out.println("\t maxNumberOfGroupAssignmentsPerParticle: " + maxNumberOfGroupAssignmentsPerParticle);

        System.out.println("</groupData>");
    }

    void setTemplateOrigin(double[] templateOrigin) {
        this.templateOrigin = templateOrigin;
    }

    void setTemplateNormal(double[] templateNormal) {
        this.templateNormal = templateNormal;
    }

   
}
