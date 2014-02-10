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

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupParametersEntry;
import readdy.impl.tools.AdvancedSystemOut;

/**
 *
 * @author schoeneberg
 */
public class GroupParametersEntry implements IGroupParametersEntry {

    private int maxNumberOfAssignmentsPerParticle;
    private HashMap<Integer, int[][]> groupInternalPotentials;
    private ArrayList<IExtendedIdAndType> buildingBlocks;
    private int typeId;
    private String typeName;
    private ArrayList<Integer> internalIds;
    ArrayList<int[]> unparsedInternalIds;
    private ArrayList<double[]> buildingBlockTemplateCoordinates;
    private double[] templateNormal;
    private double[] templateOrigin;

    public int getNumberOfGroupMembers() {
        return this.internalIds.size();
    }

    public int getMaxNumberOfAssignmentsPerParticle() {
        return this.maxNumberOfAssignmentsPerParticle;
    }

    public HashMap<Integer, int[][]> getGroupInternalPotentials() {
        return this.groupInternalPotentials;
    }

    public ArrayList<IExtendedIdAndType> getBuildingBlocks() {
        return this.buildingBlocks;
    }

    public int get_typeId() {
        return this.typeId;
    }

    public String get_typeName() {
        return this.typeName;
    }

    public void set_typeId(int typeId) {
        this.typeId = typeId;
    }

    public void set_typeName(String typeName) {
        this.typeName = typeName;
    }

    public void set_getMaxNumberOfGroupAssignmentsPerParticle(int maxNumberOfGroupAssignmentsPerParticle) {
        this.maxNumberOfAssignmentsPerParticle = maxNumberOfGroupAssignmentsPerParticle;
    }

    public void set_involvedPotentials(HashMap<Integer, int[][]> groupInternalPotentials) {
        this.groupInternalPotentials = groupInternalPotentials;
    }

    public void set_internalIds(ArrayList<Integer> internalIds) {
        this.internalIds = internalIds;
    }

    public ArrayList<Integer> get_internalIds() {
        return this.internalIds;
    }

    public ArrayList<int[]> get_unparsedInternalIds() {
        return this.unparsedInternalIds;
    }

    public void set_buildingBlocks(ArrayList<IExtendedIdAndType> buildingBlocks) {
        this.buildingBlocks = buildingBlocks;
    }

    public void print() {

        System.out.println("<typeId>" + typeId + "</typeId>");

        System.out.println("<typeName>" + typeName + "</typeName>");

        System.out.print("<groupInternalIds>");
        for (int id : internalIds) {
            System.out.print(id + ",");
        }
        System.out.println("</groupInternalIds>");

        System.out.println("<maxNumberOfAssignmentsPerParticle>" + maxNumberOfAssignmentsPerParticle + "</maxNumberOfAssignmentsPerParticle>");

        for (int potId : groupInternalPotentials.keySet()) {
            System.out.println("<potential>");

            AdvancedSystemOut.println("\t potId: " + potId + " -> ", groupInternalPotentials.get(potId), "");
            System.out.println("</potential>");
        }


        for (int i = 0; i < buildingBlocks.size(); i++) {

            IExtendedIdAndType extIdAndType = buildingBlocks.get(i);
            System.out.println("<buildingBlock>");
            extIdAndType.print();
            if (buildingBlockTemplateCoordinates.size() < i) {
                double[] templateCoord = buildingBlockTemplateCoordinates.get(i);
                AdvancedSystemOut.println("<templateCoord ", templateCoord, "/>");
            }
            System.out.println("</buildingBlock>");
        }
        AdvancedSystemOut.println("<templateOrigin ", templateOrigin, "/>");
        AdvancedSystemOut.println("<templateNormal ", templateNormal, "/>");
    }

    public void set_unparsedInternalIds(ArrayList<int[]> unparsedInternalIds) {
        this.unparsedInternalIds = unparsedInternalIds;
    }

    public ArrayList<double[]> getBuildingBlockTemplateCoordinates() {
        return this.buildingBlockTemplateCoordinates;
    }

    public double[] getGroupTemplateNormal() {
        return this.templateNormal;
    }

    public double[] getGroupTemplateCenter() {
        return this.templateOrigin;
    }

    public void setBuildingBlockTemplateCoordinates(ArrayList<double[]> buildingBlockTemplateCoordinates) {
        this.buildingBlockTemplateCoordinates = buildingBlockTemplateCoordinates;
    }

    public void setTemplateNormal(double[] templateNormal) {
        this.templateNormal = templateNormal;
    }

    public void setTemplateOrigin(double[] templateOrigin) {
        this.templateOrigin = templateOrigin;
    }
}
