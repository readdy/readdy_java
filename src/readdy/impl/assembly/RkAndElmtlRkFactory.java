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
import readdy.api.assembly.IRkAndElmtlRkFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IReactionData;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.sim.core.rk.ElementalReaction;
import readdy.impl.sim.top.group.ExtendedIdAndType;
import readdy.impl.sim.top.rkHandle.Reaction;
import readdy.impl.tools.StringTools;

/**
 *
 * @author schoeneberg
 */
public class RkAndElmtlRkFactory implements IRkAndElmtlRkFactory {

    HashMap<Integer, IElementalReaction> elmtlRkId_to_elmtlRk_map = new HashMap();
    HashMap<Integer, IReaction> rkId_to_rk_map = new HashMap();
    HashMap<Integer, Integer> elmtlRkId_to_rkId_map = new HashMap();
    HashMap<Integer, ArrayList<Integer>> rkId_to_elmtlRkId_map = new HashMap();
    IParamReactionsFileData rawInputData;
    IReactionManager reactionTypeInventory;
    IGroupParameters groupParameters;
    IParticleParameters particleParameters;
    IGlobalParameters globalParameters;
    int currentRkId = -1;
    int currentElmtlRkId = -1;
    boolean reactionsHaveBeenCreated = false;

    public void createReactionsAndElmtlReactions(IParamReactionsFileData rawInputData) {
        boolean verbose = true;
        System.out.println("RkAndElmtlRkFactory: Create Reactions and elemental Reactions...");
        this.rawInputData = rawInputData;

        if (allInputPresent()) {

            ArrayList<IReactionData> rawReactionDataList = rawInputData.get_reactionDataList();

            for (IReactionData rawReaction : rawReactionDataList) {
                String name = rawReaction.get_rkName();
                double kForward = rawReaction.get_kForward();
                double kBackward = rawReaction.get_kBackward();

                ArrayList<String[]> educts = rawReaction.get_educts();
                ArrayList<String[]> products = rawReaction.get_products();
                String reactionTypeName = rawReaction.get_typeName();
                int rkTypeIdForward = reactionTypeInventory.get_reactionTypeId(reactionTypeName);
                int rkTypeIdBackward = reactionTypeInventory.get_inverseReactionTypeId(rkTypeIdForward);
                if (kForward != 0) {

                    createReactionAndItsElementalReactions(name + "_forward", rkTypeIdForward, kForward, educts, products);
                }

                if (kBackward != 0) {

                    createReactionAndItsElementalReactions(name + "_backward", rkTypeIdBackward, kBackward, products, educts);
                }

            }
            reactionsHaveBeenCreated = true;
            if (verbose) {
                System.out.println("--------------------------------------");
                System.out.println("RkAndElmtlRkFactory: parsedReactions...");
                for (int rkId : rkId_to_rk_map.keySet()) {
                    rkId_to_rk_map.get(rkId).print();
                    System.out.println();
                }
                System.out.println("--------------------------------------");
                System.out.println("RkAndElmtlRkFactory: parsedElementalReactions...");
                for (int elmtlRkId : elmtlRkId_to_elmtlRk_map.keySet()) {
                    elmtlRkId_to_elmtlRk_map.get(elmtlRkId).print();
                    System.out.println();
                }
                System.out.println("--------------------------------------");
            }
            System.out.println("RkAndElmtlRkFactory: done.");
        } else {
            throw new RuntimeException("not all input present!");
        }
    }

    private boolean allInputPresent() {
        return rawInputData != null
                && reactionTypeInventory != null
                && groupParameters != null
                && particleParameters != null
                && globalParameters != null;
    }

    public HashMap<Integer, Integer> get_elmtlRkToRkMapping() {
        if (reactionsHaveBeenCreated) {
            return this.elmtlRkId_to_rkId_map;
        } else {
            throw new RuntimeException("reactions have not been created yet. Call createReactionsAndElmtlReactions() function.");
        }
    }

    public HashMap<Integer, ArrayList<Integer>> get_rkToElmtlRkMapping() {
        if (reactionsHaveBeenCreated) {
            return this.rkId_to_elmtlRkId_map;
        } else {
            throw new RuntimeException("reactions have not been created yet. Call createReactionsAndElmtlReactions() function.");
        }
    }

    public HashMap<Integer, IElementalReaction> get_elementalReactions() {
        if (reactionsHaveBeenCreated) {
            return this.elmtlRkId_to_elmtlRk_map;
        } else {
            throw new RuntimeException("reactions have not been created yet. Call createReactionsAndElmtlReactions() function.");
        }
    }

    public HashMap<Integer, IReaction> get_reactions() {
        if (reactionsHaveBeenCreated) {
            return this.rkId_to_rk_map;
        } else {
            throw new RuntimeException("reactions have not been created yet. Call createReactionsAndElmtlReactions() function.");
        }
    }

    private void createReactionAndItsElementalReactions(String rkName, int rkTypeId, double k, ArrayList<String[]> rawEducts, ArrayList<String[]> rawProducts) {


        Reaction rk = new Reaction();
        int rkId = getNextRkId();
        rk.setId(rkId);
        rk.setRkName(rkName);
        rk.setK(k);
        double p = computeMicroscopicReactionProbability(k);
        rk.setP(p);
        rk.setRkTypeId(rkTypeId);
        rk.setRkTypeName(reactionTypeInventory.get_reactionTypeName(rkTypeId));

        IExtendedIdAndType[] educts = (parseExtendedIdAndType(rawEducts));
        ArrayList<IExtendedIdAndType[]> elmentalReactionEductCombinations = (parseElementalEductCombinations(rawEducts));
        IExtendedIdAndType[] products = (parseExtendedIdAndType(rawProducts));
        rk.setEducts(educts);
        rk.setProducts(products);
        rkId_to_rk_map.put(rkId, rk);

        if (thereIsNoGroupReactionPresent(educts)) {
            ElementalReaction elmtlRk = new ElementalReaction();
            int elmtlRkId = getNextElmtlRkId();
            elmtlRk.setId(elmtlRkId);
            elmtlRk.setName(rkName);
            elmtlRk.setEducts(educts);
            elmtlRk.setP(p);

            elmtlRkId_to_elmtlRk_map.put(elmtlRkId, elmtlRk);
            elmtlRkId_to_rkId_map.put(elmtlRkId, rkId);
            if (!rkId_to_elmtlRkId_map.containsKey(rkId)) {
                ArrayList<Integer> list = new ArrayList();
                list.add(elmtlRkId);
                rkId_to_elmtlRkId_map.put(rkId, list);
            } else {
                rkId_to_elmtlRkId_map.get(rkId).add(elmtlRkId);
            }


        } else {
            //System.out.println( rkName + " l: " + elmentalReactionEductCombinations.size());
            for (IExtendedIdAndType[] elmtlRkEductCombi : elmentalReactionEductCombinations) {
                ElementalReaction elmtlRk = new ElementalReaction();
                int elmtlRkId = getNextElmtlRkId();
                elmtlRk.setId(elmtlRkId);
                elmtlRk.setName(rkName);
                elmtlRk.setEducts(elmtlRkEductCombi);
                elmtlRk.setP(p);

                elmtlRkId_to_elmtlRk_map.put(elmtlRkId, elmtlRk);
                elmtlRkId_to_rkId_map.put(elmtlRkId, rkId);
                if (!rkId_to_elmtlRkId_map.containsKey(rkId)) {
                    ArrayList<Integer> list = new ArrayList();
                    list.add(elmtlRkId);
                    rkId_to_elmtlRkId_map.put(rkId, list);
                } else {
                    rkId_to_elmtlRkId_map.get(rkId).add(elmtlRkId);
                }
            }
        }


    }

    private boolean thereIsNoGroupReactionPresent(IExtendedIdAndType[] educts) {
        for (IExtendedIdAndType e : educts) {
            if (e.get_isGroup() == true) {
                return false;
            }
        }
        return true;
    }

    private int getNextRkId() {
        currentRkId++;
        return currentRkId;
    }

    private double computeMicroscopicReactionProbability(double k) {
        return inversePoissonProbability(globalParameters.get_dt(), k);
    }

    private double inversePoissonProbability(double dt, double k) {
        return 1 - Math.exp(-dt * k);
    }

    private IExtendedIdAndType[] parseExtendedIdAndType(ArrayList<String[]> rawData) {

        IExtendedIdAndType[] data = new IExtendedIdAndType[rawData.size()];
        for (int i = 0; i < rawData.size(); i++) {
            String[] rawDataLine = rawData.get(i);


            if (rawDataLine.length == 3) {
                String groupOrParticle = rawDataLine[0];
                // String currentReactiveInternalIds = rawDataLine[1];
                String typeName = rawDataLine[2];

                boolean group = false;
                int entityId = -1;
                int typeId = -1;


                if (groupOrParticle.contentEquals("group")) {
                    group = true;
                    typeId = groupParameters.getGroupTypeIdFromTypeName(typeName);
                } else {
                    if (groupOrParticle.contentEquals("particle")) {
                        group = false;
                        typeId = particleParameters.getParticleTypeIdFromTypeName(typeName);
                    } else {
                        throw new RuntimeException("the content should be group or particle but was '" + groupOrParticle + "'");
                    }
                }

                IExtendedIdAndType e = new ExtendedIdAndType(group, entityId, typeId);
                data[i] = e;

            } else {
                throw new RuntimeException("there should be no more than 3 arguments.");
            }

        }
        return data;
    }

    private int getNextElmtlRkId() {
        currentElmtlRkId++;
        return currentElmtlRkId;
    }

    public void set_reactionManager(IReactionManager reactionTypeInventory) {
        this.reactionTypeInventory = reactionTypeInventory;
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public void set_groupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    private ArrayList<IExtendedIdAndType[]> parseElementalEductCombinations(ArrayList<String[]> rawEducts) {

        ArrayList<IExtendedIdAndType[]> finalElmtlEductSinglesOrEductPairs = new ArrayList();
        ArrayList<IExtendedIdAndType[]> elmtlEductsToCombine = new ArrayList();

        for (int i = 0; i < rawEducts.size(); i++) {
            String[] rawDataLine = rawEducts.get(i);


            if (rawDataLine.length == 3) {
                String groupOrParticle = rawDataLine[0];
                String rawCurrentReactiveInternalIds = rawDataLine[1];
                String typeName = rawDataLine[2];
                boolean group = false;
                int entityId = -1;
                int typeId = -1;


                if (groupOrParticle.contentEquals("group")) {
                    group = true;
                    typeId = groupParameters.getGroupTypeIdFromTypeName(typeName);
                } else {
                    if (groupOrParticle.contentEquals("particle")) {
                        group = false;
                        typeId = particleParameters.getParticleTypeIdFromTypeName(typeName);
                    } else {
                        throw new RuntimeException("the content should be group or particle but was '" + groupOrParticle + "'");
                    }
                }

                IExtendedIdAndType e = new ExtendedIdAndType(group, entityId, typeId);

                // if it is no group, the extendedIdAndType is a single particle
                // the particle is by itself an elementalEduct
                if (group == false) {
                    elmtlEductsToCombine.add(new IExtendedIdAndType[]{e});

                } else {

                    // if it is a group, we have to look for specific reactive internal ids
                    if (rawCurrentReactiveInternalIds.contentEquals("all")) {

                        // if the reactive entries are not specified but all are reactive,
                        // collect them and store it to the combine array for further processing
                        IExtendedIdAndType[] elmtlEductsToCombineEntry = new IExtendedIdAndType[groupParameters.getGroupInternalIds(typeId).size()];
                        int pos = 0;
                        for (int groupInternalId : groupParameters.getGroupInternalIds(typeId)) {
                            IExtendedIdAndType elmtlEduct = groupParameters.getElmementalBuildingBlock(groupInternalId);
                            elmtlEductsToCombineEntry[pos] = elmtlEduct;
                            pos++;
                        }
                        elmtlEductsToCombine.add(elmtlEductsToCombineEntry);

                    } else {
                        // if not all are reactive we have to parse the given string to internal ids or pairs of them
                        // and threat them accordingly
                        int[][] currentReactiveInternalIds = StringTools.splitMatrixString_convertToInt(rawCurrentReactiveInternalIds);
                        for (int[] singleOrPair : currentReactiveInternalIds) {
                            // if there are already pairs defined, these are reaction pairs and
                            // can stay as they are. They do not have to be combined with other
                            // elmentalEducts.
                            if (singleOrPair.length == 2) {
                                finalElmtlEductSinglesOrEductPairs.add(new IExtendedIdAndType[]{
                                            groupParameters.getElmementalBuildingBlock(singleOrPair[0]),
                                            groupParameters.getElmementalBuildingBlock(singleOrPair[1])});
                            } else {

                                // if there are single entries, they have to be combined with other reactive educts.
                                if (singleOrPair.length == 1) {
                                    elmtlEductsToCombine.add(new IExtendedIdAndType[]{
                                                groupParameters.getElmementalBuildingBlock(singleOrPair[0])});
                                } else {
                                    throw new RuntimeException("the entry as reactive internal Id has to consist of pairs [a,b] or of singles [a]. length " + singleOrPair.length + "is not supported.");
                                }
                            }
                        }
                    }
                }
            } else {
                throw new RuntimeException("there should be no more than 3 arguments.");
            }
        }
        // finally, after having collected all elementalEducts from the particles
        // and groups, combine them in the correct way

        // there are only two possibilities for the length of the elmtlEductsToComine Array:
        // since only reactions up to order 2 are supported, and each educt can contribute only one entry in the
        // array, we have at most 2 entries. Those two entries have to be combined with each other, every entry of the
        // first with every entry of the second.
        // If we have only one entry here, we are done.
        switch (elmtlEductsToCombine.size()) {
            case 0:
                // we are done.
                break;
            case 1:
                // we have to insert all given entries to the final array.
                IExtendedIdAndType[] extIdTypeArr = elmtlEductsToCombine.get(0);
                // combine everyone with everyone else but ommit dublicates
                if (extIdTypeArr.length == 1) {


                    finalElmtlEductSinglesOrEductPairs.add(new IExtendedIdAndType[]{extIdTypeArr[0]});

                } else {
                    for (int i = 0; i < extIdTypeArr.length; i++) {
                        for (int j = i + 1; j < extIdTypeArr.length; j++) {
                            finalElmtlEductSinglesOrEductPairs.add(new IExtendedIdAndType[]{extIdTypeArr[i], extIdTypeArr[j]});
                        }


                    }
                }

                break;


            case 2:
                // combine all with themselves
                IExtendedIdAndType[] arr0 = elmtlEductsToCombine.get(0);
                IExtendedIdAndType[] arr1 = elmtlEductsToCombine.get(1);
                for (IExtendedIdAndType from0 : arr0) {
                    for (IExtendedIdAndType from1 : arr1) {
                        finalElmtlEductSinglesOrEductPairs.add(new IExtendedIdAndType[]{from0, from1});
                    }
                }
                break;

            default:
                throw new RuntimeException("there should be no more than 2 entries in this array. But there are " + elmtlEductsToCombine.size() + ".");
        }

        return finalElmtlEductSinglesOrEductPairs;
    }
}
