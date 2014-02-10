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
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.group.IGroupParametersEntry;

/**
 *
 * @author schoeneberg
 */
public class GroupParameters implements IGroupParameters {

    HashMap<Integer, IGroupParametersEntry> groupIdToGroupParametersMap;
    HashMap<String, Integer> groupTypeName_to_groupTypeId_Map;
    HashMap< Integer,String> groupTypeId_to_groupTypeName_Map;
    HashMap<Integer, IExtendedIdAndType> groupInternalId_to_elmtlBuildingBlock;

    public void setGroupInternalId_to_elmtlBuildingBlock(HashMap<Integer, IExtendedIdAndType> groupInternalId_to_elmtlBuildingBlock) {
        this.groupInternalId_to_elmtlBuildingBlock = groupInternalId_to_elmtlBuildingBlock;
    }

    public void setGroupTypeName_to_groupTypeId_Map(HashMap<String, Integer> groupTypeName_to_groupTypeId_Map) {
        this.groupTypeName_to_groupTypeId_Map = groupTypeName_to_groupTypeId_Map;
        groupTypeId_to_groupTypeName_Map = new HashMap();
        for(String typeName:groupTypeName_to_groupTypeId_Map.keySet()){
            int typeId= groupTypeName_to_groupTypeId_Map.get(typeName);
            groupTypeId_to_groupTypeName_Map.put(typeId, typeName);
        }
        
    }

    public GroupParameters(HashMap<Integer, IGroupParametersEntry> groupIdToGroupParametersMap) {
        this.groupIdToGroupParametersMap = groupIdToGroupParametersMap;
    }

    public ArrayList<Integer> getGroupInternalIds(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).get_internalIds();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public int getNumberOfGroupMembers(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getNumberOfGroupMembers();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public int getMaxNumberOfAssignmentsPerParticle(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getMaxNumberOfAssignmentsPerParticle();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    // potentialId -> single particle Internal Ids , particle pairs internal Ids
    public HashMap<Integer, int[][]> getGroupInternalPotentials(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getGroupInternalPotentials();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public boolean doesTypeIdExist(int groupTypeId) {
        return groupIdToGroupParametersMap.containsKey(groupTypeId);
    }

    public boolean doesTypeNameExist(String groupTypeName) {
        return groupTypeName_to_groupTypeId_Map.containsKey(groupTypeName);

    }

    public ArrayList<IExtendedIdAndType> getBuildingBlocks(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getBuildingBlocks();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public int getGroupTypeIdFromTypeName(String typeName) {

        if (groupTypeName_to_groupTypeId_Map.containsKey(typeName)) {
            return groupTypeName_to_groupTypeId_Map.get(typeName);
        } else {
            throw new RuntimeException("the requested groupType " + typeName + " is unknown.");
        }
    }

    public IExtendedIdAndType getElmementalBuildingBlock(int groupInternalId) {
        if (groupInternalId_to_elmtlBuildingBlock.containsKey(groupInternalId)) {
            return groupInternalId_to_elmtlBuildingBlock.get(groupInternalId);
        } else {
            throw new RuntimeException("the requested groupInternalId '" + groupInternalId + "' is unknown.");
        }
    }

    public double[] getGroupTemplateNormalVector(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            IGroupParametersEntry gpe = groupIdToGroupParametersMap.get(groupTypeId);
            return gpe.getGroupTemplateNormal();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public double[] getGroupTemplateOriginVector(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getGroupTemplateCenter();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public ArrayList<double[]> getBuildingBlockTemplateCoordinates(int groupTypeId) {
        if (groupIdToGroupParametersMap.containsKey(groupTypeId)) {
            return groupIdToGroupParametersMap.get(groupTypeId).getBuildingBlockTemplateCoordinates();
        } else {
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }

    public String getGroupTypeNameFromTypeId(int groupTypeId) {
        if(groupTypeId_to_groupTypeName_Map.containsKey(groupTypeId)){
            return groupTypeId_to_groupTypeName_Map.get(groupTypeId);
        }else{
            throw new RuntimeException("requested groupType Id" + groupTypeId + " doesnt exist");
        }
    }
}
