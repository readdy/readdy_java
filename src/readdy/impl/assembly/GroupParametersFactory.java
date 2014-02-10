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
package readdy.impl.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.io.in.par_group.IGroupData;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.group.IGroupParametersEntry;
import readdy.impl.sim.top.group.ExtendedIdAndType;
import readdy.impl.sim.top.group.GroupParameters;
import readdy.impl.sim.top.group.GroupParametersEntry;
import readdy.impl.tools.StringTools;

/**
 *
 * @author Schoeneberg
 */
public class GroupParametersFactory implements IGroupParametersFactory {

    IParamGroupsFileData groupsFileData;
    IParticleParameters particleParameters;
    IPotentialInventory potentialInventory;
    HashMap<Integer, IGroupParametersEntry> groupIdToGroupParametersMap = new HashMap();
    HashMap<Integer, String> groupTypeIdToGroupNameMap = new HashMap();
    HashMap<String, Integer> groupNameToGroupTypeIdMap = new HashMap();
    HashMap<Integer, IExtendedIdAndType> groupInternalId_to_elmtlBuildingBlock;

    public void set_paramGroupsFileData(IParamGroupsFileData groupsFileData) {
        this.groupsFileData = groupsFileData;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void set_potentialInventory(IPotentialInventory potentialInventory) {
        this.potentialInventory = potentialInventory;
    }

    public IGroupParameters createGroupParameters() {
        // first parse this info because it is essential in the next step
        // and has to be completed there.
        if (allInputAvailable()) {
            for (IGroupData groupData : groupsFileData.get_groupDataList()) {
                groupTypeIdToGroupNameMap.put(groupData.getTypeId(), groupData.getTypeName());
                groupNameToGroupTypeIdMap.put(groupData.getTypeName(), groupData.getTypeId());
            }

            for (IGroupData groupData : groupsFileData.get_groupDataList()) {
                IGroupParametersEntry groupIdToGroupParametersMapEntry = condenseInputDataToGroupParametersEntry(groupData);
                groupIdToGroupParametersMap.put(groupData.getTypeId(), groupIdToGroupParametersMapEntry);
            }

            // parse elemental buildingBlocks
            groupInternalId_to_elmtlBuildingBlock = create_groupInternalId_to_ElmtlBuildingBlockMap(groupIdToGroupParametersMap);

            GroupParameters groupParameters = new GroupParameters(groupIdToGroupParametersMap);
            groupParameters.setGroupTypeName_to_groupTypeId_Map(groupNameToGroupTypeIdMap);
            groupParameters.setGroupInternalId_to_elmtlBuildingBlock(groupInternalId_to_elmtlBuildingBlock);
            return groupParameters;
        } else {
            throw new RuntimeException("not all input given. Abort!");
        }
    }

    private IGroupParametersEntry condenseInputDataToGroupParametersEntry(IGroupData groupData) {
        GroupParametersEntry gpe = new GroupParametersEntry();
        gpe.set_typeId(groupData.getTypeId());
        gpe.set_typeName(groupData.getTypeName());
        gpe.set_getMaxNumberOfGroupAssignmentsPerParticle(groupData.getMaxNumberOfGroupAssignmentsPerParticle());
        gpe.setTemplateOrigin(groupData.getTemplateOrigin());
        gpe.setTemplateNormal(groupData.getTemplateNormal());

        //##############################################################################
        // buildingBlocks

        // ["particle","group"],internalIds,typeName
        ArrayList<String[]> rawBuildingBlocks = groupData.getBuildingBlocks();
        RawBuildingBlockConverter buildingBlockConverter = new RawBuildingBlockConverter(particleParameters, groupNameToGroupTypeIdMap);
        buildingBlockConverter.convert(rawBuildingBlocks);
        ArrayList<IExtendedIdAndType> buildingBlocks = buildingBlockConverter.get_buildingBlocks();
        gpe.set_buildingBlocks(buildingBlocks);
        ArrayList<double[]> buildingBlockTemplateCoordinates = buildingBlockConverter.getBuildingBlockTemplateCoordinates();
        gpe.setBuildingBlockTemplateCoordinates(buildingBlockTemplateCoordinates);
        ArrayList<Integer> internalIds = buildingBlockConverter.get_internalIds();
        gpe.set_internalIds(internalIds);
        gpe.set_unparsedInternalIds(buildingBlockConverter.get_unparsedInternalIds());

        //##############################################################################
        // potentials
        ArrayList<HashMap<String, String>> rawPotentials = groupData.getInvolvedPotentials();
        // potId -> internalId singles, internalId pairs
        HashMap<Integer, int[][]> groupInternalPotentials = new HashMap();
        for (HashMap<String, String> parameters : rawPotentials) {
            int potId = potentialInventory.createPotential(parameters);
            int potOrder = potentialInventory.getPotentialOrder(potId);
            switch (potOrder) {
                case 1:
                    // build the potentials and assign the affected particles
                    // {{1},{3},{4}}
                    int[][] affectedParticleIds = getAffectedInternalIds(parameters, internalIds);
                    if (affectedParticleIds[0].length != 1) {
                        throw new RuntimeException("there is something wrong");
                    }
                    groupInternalPotentials.put(potId, affectedParticleIds);
                    break;

                case 2:
                    // build the potentials and assign the affected particles
                    // {{1,2},{6,3},{8,9}}
                    int[][] affectedParticleIdPairs = getAffectedInternalIdPairs(parameters, internalIds);
                    if (affectedParticleIdPairs[0].length != 2) {
                        throw new RuntimeException("there is something wrong");
                    }

                    groupInternalPotentials.put(potId, affectedParticleIdPairs);
                    break;
                default:
                    throw new RuntimeException("the potential order is "
                            + ""
                            + "different from 1 or 2 in potentialOrder '" + potId + "'. Abort!");
            }

        }
        gpe.set_involvedPotentials(groupInternalPotentials);

        return gpe;
    }

    private int[][] getAffectedInternalIds(HashMap<String, String> parameters, ArrayList<Integer> internalIds) {
        String keyString = "affectedInternalIds";
        int[] preResult = get_parseIntArrayFromParameters(keyString, parameters);
        int[][] result = new int[preResult.length][1];
        for (int i = 0; i < preResult.length; i++) {
            int internalIdToTest = preResult[i];
            if (internalIds.contains(internalIdToTest)) {
                result[i] = new int[]{internalIdToTest};
            } else {
                throw new RuntimeException("the requested internal id " + internalIdToTest + " doesnt exist in the group.");
            }

        }
        return result;

    }

    private int[][] getAffectedInternalIdPairs(HashMap<String, String> parameters, ArrayList<Integer> internalIds) {

        String keyString = "affectedInternalIdPairs";
        int[][] result = get_parseIntPairsFromParameters(keyString, parameters);

        for (int i = 0; i < result.length; i++) {
            int[] pairList = result[i];

            for (int j = 0; j < pairList.length; j++) {
                int pairMemberIdToTest = pairList[j];

                if (!internalIds.contains(pairMemberIdToTest)) {
                    throw new RuntimeException("the requested internal id " + pairMemberIdToTest + " doesnt exist in the group.");
                }

            }
        }
        return result;

    }

    private int[] get_parseIntArrayFromParameters(String keyString, HashMap<String, String> parameters) {
        int[] result = new int[0];
        if (parameters.containsKey(keyString)) {
            String value = parameters.get(keyString);
            result = StringTools.splitArrayString_convertToInt(value);
        }
        return result;

    }

    private int[][] get_parseIntPairsFromParameters(String keyString, HashMap<String, String> parameters) {
        int[][] result = new int[0][0];

        if (parameters.containsKey(keyString)) {
            String value = parameters.get(keyString);
            result = StringTools.splitMatrixString_convertToInt(value);
        }
        return result;
    }

    private boolean allInputAvailable() {
        return groupsFileData != null
                && particleParameters != null
                && potentialInventory != null;
    }

    private HashMap<Integer, IExtendedIdAndType> create_groupInternalId_to_ElmtlBuildingBlockMap(HashMap<Integer, IGroupParametersEntry> groupIdToGroupParametersMap) {
        HashMap<Integer, IExtendedIdAndType> groupInternalId_to_elmtlBuildingBlock = new HashMap();
        // do this for each group
        for (int groupId : groupIdToGroupParametersMap.keySet()) {
            GroupParametersEntry entry = (GroupParametersEntry) groupIdToGroupParametersMap.get(groupId);
            ArrayList<IExtendedIdAndType> blocks = entry.getBuildingBlocks();
            ArrayList<Integer> internalIds = entry.get_internalIds();
            ArrayList<int[]> unparsedInternalIdsList = entry.get_unparsedInternalIds();


            // first find all elmtlExtendedIdAndTypes
            ArrayList<InternalIdAndBlockPair> resultElmtlBlockList = new ArrayList();
            if (blocks.size() != unparsedInternalIdsList.size()) {
                throw new RuntimeException("these two have to have the same size per construction.");
            } else {
                for (int i = 0; i < blocks.size(); i++) {
                    IExtendedIdAndType block = blocks.get(i);
                    int[] unparsedInternalIds = unparsedInternalIdsList.get(i);
                    InternalIdAndBlockPair pair = new InternalIdAndBlockPair(block, unparsedInternalIds);
                    resultElmtlBlockList.addAll(findElmtlExtendedIdAndTypeRecursively(pair));
                }
            }

            // store the found elmtlExtendedIdAndTypes in the appropriate hash map
            if (internalIds.size() == resultElmtlBlockList.size()) {
                for (int i = 0; i < internalIds.size(); i++) {
                    int internalId = internalIds.get(i);
                    IExtendedIdAndType elmtlBuildingBlock = resultElmtlBlockList.get(i).getExtendedIdAndType();
                    // store the internal ids in the elmtlBuilding blocks
                    IExtendedIdAndType extIdAndTypeToStore = new ExtendedIdAndType(elmtlBuildingBlock.get_isGroup(), internalId, elmtlBuildingBlock.get_type());
                    groupInternalId_to_elmtlBuildingBlock.put(internalId, extIdAndTypeToStore);
                }
            } else {
                throw new RuntimeException("the number of "
                        + "internalIds '" + internalIds.size() + "' should be equal "
                        + "to the number of found elmental building "
                        + "blocks '" + resultElmtlBlockList.size() + "'. This is not "
                        + "the case. Abort.");
            }
        }
        return groupInternalId_to_elmtlBuildingBlock;
    }

    private ArrayList<InternalIdAndBlockPair> findElmtlExtendedIdAndTypeRecursively(InternalIdAndBlockPair pair) {
        ArrayList<InternalIdAndBlockPair> list = new ArrayList();
        IExtendedIdAndType e = pair.getExtendedIdAndType();
        int[] intIds = pair.getAssignedInternalIds();
        // the id is preserved during recursion


        if (intIds.length == 1 && e.get_isGroup() == false) {

            list.add(pair);
            return list;
        } else {

            // update pair
            int eTypeId = e.get_type();
            GroupParametersEntry entry = (GroupParametersEntry) groupIdToGroupParametersMap.get(eTypeId);
            ArrayList<IExtendedIdAndType> entriesDeeper = entry.getBuildingBlocks();
            ArrayList<Integer> internalIdsDeeper = entry.get_internalIds();
            ArrayList<int[]> unparsedInternalIdsDeeper = entry.get_unparsedInternalIds();
            if (intIds.length == internalIdsDeeper.size()
                    && intIds.length == entriesDeeper.size()) {
                for (int i = 0; i < intIds.length; i++) {
                    IExtendedIdAndType eDeeper = entriesDeeper.get(i);
                    int[] intIdsDeeper = unparsedInternalIdsDeeper.get(i);
                    InternalIdAndBlockPair pairOneLevelDeeper = new InternalIdAndBlockPair(eDeeper, intIdsDeeper);
                    list.addAll(findElmtlExtendedIdAndTypeRecursively(pairOneLevelDeeper));
                }

            } else {
                throw new RuntimeException("this should never be the case");
            }



            return list;
        }
    }
}

class InternalIdAndBlockPair {

    private IExtendedIdAndType extendedIdAndType;
    private int[] assignedInternalIds;

    InternalIdAndBlockPair(IExtendedIdAndType extendedIdAndType, int[] assignedInternalIds) {
        this.assignedInternalIds = assignedInternalIds;
        this.extendedIdAndType = extendedIdAndType;
    }

    public int[] getAssignedInternalIds() {
        return assignedInternalIds;
    }

    public IExtendedIdAndType getExtendedIdAndType() {
        return extendedIdAndType;
    }
}

class RawBuildingBlockConverter {

    IParticleParameters particleParameters;
    ArrayList<IExtendedIdAndType> buildingBlocks;
    ArrayList<double[]> buildingBlockTemplateCoordinates;
    ArrayList<Integer> internalIds;
    ArrayList<int[]> unparsedInternalIds;
    HashMap<String, Integer> groupNameToGroupTypeIdMap;

    RawBuildingBlockConverter(IParticleParameters particleParameters, HashMap<String, Integer> groupNameToGroupTypeIdMap) {
        this.particleParameters = particleParameters;
        this.groupNameToGroupTypeIdMap = groupNameToGroupTypeIdMap;
    }
    // ["particle","group"],internalIds,typeName

    void convert(ArrayList<String[]> rawBuildingBlocks) {
        buildingBlocks = new ArrayList();
        buildingBlockTemplateCoordinates = new ArrayList();
        unparsedInternalIds = new ArrayList();
        internalIds = new ArrayList();
        int index = 0;
        for (String[] blockLine : rawBuildingBlocks) {
            if (blockLine.length == 4) {
                if (blockLine[0].equals("particle")) {
                    int parsedInternalId = Integer.parseInt(blockLine[1]);
                    internalIds.add(parsedInternalId);
                    unparsedInternalIds.add(new int[]{parsedInternalId});
                    int particleTypeId = particleParameters.getParticleTypeIdFromTypeName(blockLine[2]);
                    ExtendedIdAndType extIdAndType = new ExtendedIdAndType(false, parsedInternalId, particleTypeId);
                    buildingBlocks.add(index, extIdAndType);
                    double[] templateCoords = StringTools.splitArrayString_convertToDouble(blockLine[3]);
                    buildingBlockTemplateCoordinates.add(index, templateCoords);
                } else {
                    if (blockLine[0].equals("group")) {
                        int[] parsedInternalIds = StringTools.splitArrayString_convertToInt(blockLine[1]);
                        unparsedInternalIds.add(parsedInternalIds);
                        for (int intId : parsedInternalIds) {
                            internalIds.add(intId);
                        }
                        if (groupNameToGroupTypeIdMap.containsKey(blockLine[2])) {
                            int particleTypeId = groupNameToGroupTypeIdMap.get(blockLine[2]);
                            ExtendedIdAndType extIdAndType = new ExtendedIdAndType(true, -1, particleTypeId);
                            buildingBlocks.add(index, extIdAndType);
                        } else {
                            throw new RuntimeException("the groupName " + blockLine[2] + " is not known.");
                        }
                    } else {
                        throw new RuntimeException("the blockLine prefix is neither 'particle' nor 'group' ");
                    }
                }
            } else {
                throw new RuntimeException("the length of the blockLine To parse is not 3. Abort");
            }
            index++;
        }
    }

    ArrayList<IExtendedIdAndType> get_buildingBlocks() {
        return this.buildingBlocks;
    }

    ArrayList<int[]> get_unparsedInternalIds() {
        return this.unparsedInternalIds;
    }

    ArrayList<Integer> get_internalIds() {
        return this.internalIds;
    }

    public ArrayList<double[]> getBuildingBlockTemplateCoordinates() {
        return buildingBlockTemplateCoordinates;
    }
}
