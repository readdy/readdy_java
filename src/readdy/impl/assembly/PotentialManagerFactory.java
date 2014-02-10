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
import java.util.HashSet;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.dtypes.IIntPair;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileDataEntry;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.impl.dtypes.IntPair;
import readdy.impl.sim.core.pot.PotentialManager;
import readdy.impl.tools.StringTools;

/**
 *
 * @author schoeneberg
 */
public class PotentialManagerFactory implements IPotentialManagerFactory {

    ITplgyPotentialsFileData potentialsFileData;
    IPotentialInventory potentialInventory;
    IParticleParameters particleParameters;
    private HashMap<Integer, HashSet<Integer>> pTypeId_to_pot1Id_map = new HashMap();
    private HashMap<Integer, HashSet<Integer>> pId_to_pot1Id_map = new HashMap();
    private HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_pot2Id_map = new HashMap();
    private HashMap<IIntPair, HashSet<Integer>> pIdPair_to_pot2Id_map = new HashMap();

    public void set_tplgyPotentialsFileData(ITplgyPotentialsFileData potentialsFileData) {
        this.potentialsFileData = potentialsFileData;
    }

    public void set_potentialInventory(IPotentialInventory potentialInventory) {
        this.potentialInventory = potentialInventory;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public IPotentialManager createPotentialManager() {
        if (potentialsFileData != null
                && potentialInventory != null
                && particleParameters != null) {

            PotentialManager potentialManager = new PotentialManager();
            kondenseInputDataToUsefullComponents();

            potentialManager.set_potentialInventory(potentialInventory);

            potentialManager.set_pTypeId_to_pot1Id_map(pTypeId_to_pot1Id_map);
            potentialManager.set_pId_to_pot1Id_map(pId_to_pot1Id_map);

            potentialManager.set_pTypeIdPair_to_pot2Id_map(pTypeIdPair_to_pot2Id_map);
            potentialManager.set_pIdPair_to_pot2Id_map(pIdPair_to_pot2Id_map);


            return potentialManager;
        } else {
            throw new RuntimeException("not all input files available. Abort!");
        }

    }

    private void kondenseInputDataToUsefullComponents() {


        for (ITplgyPotentialsFileDataEntry entry : potentialsFileData.get_potentialsDataList()) {
            // match the requested potential to a potential template
            HashMap<String, String> parameters = entry.get_paramNameToValueMap();
            int potId = potentialInventory.createPotential(parameters);
            int potOrder = potentialInventory.getPotentialOrder(potId);
            switch (potOrder) {
                case 1:
                    // build the potentials and assign the affected particles
                    int[] affectedParticleTypeIds = getAffectedParticleTypeIds(parameters);
                    int[] affectedParticleIds = getAffectedParticleIds(parameters);
                    assignAffectedParticlesToPotentials(affectedParticleTypeIds, affectedParticleIds, potId);
                    break;

                case 2:
                    // build the potentials and assign the affected particles
                    IIntPair[] affectedParticleTypeIdPairs = getAffectedParticleTypeIdPairs(parameters);
                    IIntPair[] affectedParticleIdPairs = getAffectedParticleIdPairs(parameters);
                    assignAffectedParticlesToPotentials(affectedParticleTypeIdPairs, affectedParticleIdPairs, potId);
                    break;
                default:
                    throw new RuntimeException("the potential order is "
                            + ""
                            + "different from 1 or 2 in potentialOrder '" + potId + "'. Abort!");
            }

        }
    }

    private int[] getAffectedParticleTypeIds(HashMap<String, String> parameters) {
        String keyString = "affectedParticleTypeIds";

        // if all is required, we can take all possible ids and do not have to check if they exist
        if (parameters.containsKey(keyString)) {
            if (parameters.get(keyString).contentEquals("all")) {
                return getAllPossibleTypeIds();
            }
        }


        int[] typeIdArray = get_parseIntArrayFromParameters(keyString, parameters);
        for (int i = 0; i < typeIdArray.length; i++) {
            int typeId = typeIdArray[i];
            if (!particleParameters.doesTypeIdExist(typeId)) {
                throw new RuntimeException("the requested particleType " + typeId + " in the"
                        + "potential definition does not exist. Abort!");
            }

        }
        return typeIdArray;
    }

    private int[] getAffectedParticleIds(HashMap<String, String> parameters) {
        String keyString = "affectedParticleIds";
        return get_parseIntArrayFromParameters(keyString, parameters);
    }

    private void assignAffectedParticlesToPotentials(int[] affectedParticleTypeIds,
            int[] affectedParticleIds,
            int pot1_id) {
        for (int i = 0; i < affectedParticleTypeIds.length; i++) {
            int flag = 0; // type
            addPotentialToRespectiveHashMap(flag, affectedParticleTypeIds[i], pot1_id);
        }
        for (int i = 0; i < affectedParticleIds.length; i++) {
            int flag = 1; // id
            addPotentialToRespectiveHashMap(flag, affectedParticleIds[i], pot1_id);
        }

    }

    private IIntPair[] getAffectedParticleTypeIdPairs(HashMap<String, String> parameters) {

        String keyString = "affectedParticleTypeIdPairs";
        IIntPair[] typeIdPairArray = get_parseIntPairsFromParameters(keyString, parameters);
        for (int i = 0; i < typeIdPairArray.length; i++) {
            IIntPair typeIdPair = typeIdPairArray[i];
            int[] typeIds = new int[]{typeIdPair.get_i(), typeIdPair.get_j()};
            for (int typeId : typeIds) {
                if (!particleParameters.doesTypeIdExist(typeId)) {
                    throw new RuntimeException("the requested particleType " + typeId + " in the"
                            + "potential definition does not exist. Abort!");
                }
            }


        }
        return typeIdPairArray;
    }

    private IIntPair[] getAffectedParticleIdPairs(HashMap<String, String> parameters) {

        String keyString = "affectedParticleIdPairs";
        return get_parseIntPairsFromParameters(keyString, parameters);

    }

    private void assignAffectedParticlesToPotentials(IIntPair[] affectedParticleTypeIdPairs,
            IIntPair[] affectedParticleIdPairs,
            int pot2_id) {
        for (int i = 0; i < affectedParticleTypeIdPairs.length; i++) {
            int flag = 0; // type
            addPotentialToRespectiveHashMap(flag, affectedParticleTypeIdPairs[i], pot2_id);

        }
        for (int i = 0; i < affectedParticleIdPairs.length; i++) {
            int flag = 1; // id
            addPotentialToRespectiveHashMap(flag, affectedParticleIdPairs[i], pot2_id);


        }
    }

    private IIntPair[] get_parseIntPairsFromParameters(String keyString, HashMap<String, String> parameters) {
        int[][] preResult = new int[0][0];

        if (parameters.containsKey(keyString)) {
            String value = parameters.get(keyString);
            if (value.contentEquals("all") && keyString.contentEquals("affectedParticleTypeIdPairs")) {
                preResult = createAllPossibleTypeIdPairCombinations();
            } else {
                preResult = StringTools.splitMatrixString_convertToInt(value);
            }
        }

        IIntPair[] result = new IIntPair[preResult.length];
        IntPair pair;
        for (int i = 0; i < preResult.length; i++) {
            if (preResult[i].length != 2) {
                throw new RuntimeException("this is not supposed to happen. "
                        + "The given String should only contain pairs. "
                        + "Maybe separator problem in input file: remind [[a,b];[c,d]] format.");
            } else {
                pair = new IntPair(preResult[i][0], preResult[i][1]);
            }
            result[i] = pair;
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

    public void addPotentialToRespectiveHashMap(int flag, int particleId, int potId) {
        HashMap<Integer, HashSet<Integer>> objectToOperateOn;
        switch (flag) {
            // type
            case 0:
                objectToOperateOn = pTypeId_to_pot1Id_map;
                break;
            // id
            case 1:
                objectToOperateOn = pId_to_pot1Id_map;
                break;
            default:
                throw new RuntimeException("does not happen normally!");
        }

        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (objectToOperateOn.containsKey(particleId)) {
                // already a potential present
                HashSet<Integer> setAlready = objectToOperateOn.get(particleId);
                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }
            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                objectToOperateOn.put(particleId, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }

    }

    public void addPotentialToRespectiveHashMap(int flag, IIntPair pair, int potId) {
        HashMap<IIntPair, HashSet<Integer>> objectToOperateOn;
        switch (flag) {
            case 0:
                // type
                objectToOperateOn = pTypeIdPair_to_pot2Id_map;
                break;
            case 1:
                // id
                objectToOperateOn = pIdPair_to_pot2Id_map;
                break;
            default:
                throw new RuntimeException("does not happen normally!");
        }

        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (objectToOperateOn.containsKey(pair)) {
                // already a potential present
                HashSet<Integer> setAlready = objectToOperateOn.get(pair);
                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }
            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                objectToOperateOn.put(pair, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }
    }

    private int[][] createAllPossibleTypeIdPairCombinations() {
        ArrayList<int[]> intPairs = new ArrayList();
        HashSet<Integer> allTypeIds_set = particleParameters.getAllParticleTypes();
        ArrayList<Integer> allTypeIds = new ArrayList();
        for (int typeId : allTypeIds_set) {
            allTypeIds.add(typeId);
        }
        for (int i = 0; i < allTypeIds.size(); i++) {
            int typeId0 = allTypeIds.get(i);
            for (int j = i; j < allTypeIds.size(); j++) {
                int typeId1 = allTypeIds.get(j);
                intPairs.add(new int[]{typeId0, typeId1});
            }

        }
        int[][] result = new int[intPairs.size()][];
        for (int i = 0; i < intPairs.size(); i++) {
            result[i] = intPairs.get(i);
        }
        return result;
    }

    private int[] getAllPossibleTypeIds() {

        HashSet<Integer> allTypeIds_set = particleParameters.getAllParticleTypes();


        int[] result = new int[allTypeIds_set.size()];
        int i = 0;
        for (int typeId : allTypeIds_set) {
            result[i] = typeId;
            i++;
        }

        return result;
    }
}
