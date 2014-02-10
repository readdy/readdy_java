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
package readdy.impl.sim.core.particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.particle.IParticleParametersEntry;

/**
 *
 * @author schoeneberg
 */
public class ParticleParameters implements IParticleParameters {

    HashMap<Integer, Double> defaultCollRadiusMap = new HashMap();
    double[] defaultCollRadiusMap_dArr;
    HashMap<Integer, Double> defaultRctRadiusMap = new HashMap();
    double[] defaultRctRadiusMap_dArr;
    ArrayList<ArrayList<Double>> collisionRadiusByTypeMatrix = new ArrayList();
    double[][] collisionRadiusByTypeMatrix_dMatrix;
    ArrayList<ArrayList<Double>> reactionRadiusByTypeMatrix = new ArrayList();
    double[][] reactionRadiusByTypeMatrix_dMatrix;
    ArrayList<ArrayList<Double>> interactionRadiusByTypeMatrix = new ArrayList();
    double[][] interactionRadiusByTypeMatrix_dMatrix;
    HashMap<Integer, Double> dFactorNoiseMap = new HashMap();
    double[] dFactorNoiseMap_dArr;
    HashMap<Integer, Double> dFactorPotMap = new HashMap();
    double[] dFactorPotMap_dArr;
    Map<Integer, Double> maxInteractionRadiusMapByType = new HashMap();
    double[] maxInteractionRadiusMapByType_dArr;
    HashSet<Integer> existingParticleTypeIds = new HashSet();
    HashMap<Integer, String> typeIdToTypeNameMap = new HashMap();
    HashMap<String, Integer> typeNameToTypeIdMap = new HashMap();
    HashMap<Integer, IParticleParametersEntry> pTyeId_to_particleParametersEntry;
    double globalMaxParticleParticleInteractionRadius = 0;

    public ParticleParameters() {
    }

    public void setpTyeId_to_particleParametersEntry(HashMap<Integer, IParticleParametersEntry> pTyeId_to_particleParametersEntry) {
        this.pTyeId_to_particleParametersEntry = pTyeId_to_particleParametersEntry;
    }

    public void set_collisionRadiusByTypeMatrix(ArrayList<ArrayList<Double>> collisionRadiusByTypeMatrix) {
        this.collisionRadiusByTypeMatrix = collisionRadiusByTypeMatrix;
        collisionRadiusByTypeMatrix_dMatrix = new double[collisionRadiusByTypeMatrix.size()][];
        for (int i = 0; i < collisionRadiusByTypeMatrix.size(); i++) {
            collisionRadiusByTypeMatrix_dMatrix[i] = new double[collisionRadiusByTypeMatrix.get(i).size()];
            for (int j = 0; j < collisionRadiusByTypeMatrix.get(i).size(); j++) {
                collisionRadiusByTypeMatrix_dMatrix[i][j] = collisionRadiusByTypeMatrix.get(i).get(j);
            }
        }
    }

    /**
     * The parameter DfactorNoise is the precomputed scaling factor for the noise.
     * In our case it is Math.sqrt(2 * D *setup.getTimestep());
     * @param dFactorNoiseMap
     */
    public void set_dFactorNoiseMap(HashMap<Integer, Double> dFactorNoiseMap) {
        this.dFactorNoiseMap = dFactorNoiseMap;
        dFactorNoiseMap_dArr = new double[dFactorNoiseMap.keySet().size()];
        for (int typeId : dFactorNoiseMap.keySet()) {
            dFactorNoiseMap_dArr[typeId] = dFactorNoiseMap.get(typeId);
        }
    }

    /**
     * The parameter DfactorPot is the precomputed scaling factor for the potential displacement.
     * In our case it is: -1 * D * dt / (Kb*T)
     * @param dFactorPotMap
     */
    public void set_dFactorPotMap(HashMap<Integer, Double> dFactorPotMap) {
        this.dFactorPotMap = dFactorPotMap;
        dFactorPotMap_dArr = new double[dFactorPotMap.keySet().size()];
        for (int typeId : dFactorPotMap.keySet()) {
            dFactorPotMap_dArr[typeId] = dFactorPotMap.get(typeId);
        }
    }

    public void set_maxInteractionRadiusMapByType(Map<Integer, Double> maxInteractionRadiusMapByType) {
        this.maxInteractionRadiusMapByType = maxInteractionRadiusMapByType;
        maxInteractionRadiusMapByType_dArr = new double[maxInteractionRadiusMapByType.keySet().size()];
        for (int typeId : maxInteractionRadiusMapByType.keySet()) {
            maxInteractionRadiusMapByType_dArr[typeId] = maxInteractionRadiusMapByType.get(typeId);
        }
    }

    public void set_maxInteractionRadiusByTypeMatrix(ArrayList<ArrayList<Double>> interactionRadiusByTypeMatrix) {

        this.interactionRadiusByTypeMatrix = interactionRadiusByTypeMatrix;
        interactionRadiusByTypeMatrix_dMatrix = new double[interactionRadiusByTypeMatrix.size()][];
        for (int i = 0; i < interactionRadiusByTypeMatrix.size(); i++) {
            interactionRadiusByTypeMatrix_dMatrix[i] = new double[interactionRadiusByTypeMatrix.get(i).size()];
            for (int j = 0; j < interactionRadiusByTypeMatrix.get(i).size(); j++) {
                interactionRadiusByTypeMatrix_dMatrix[i][j] = interactionRadiusByTypeMatrix.get(i).get(j);
                if (globalMaxParticleParticleInteractionRadius < interactionRadiusByTypeMatrix_dMatrix[i][j]) {
                    globalMaxParticleParticleInteractionRadius = interactionRadiusByTypeMatrix_dMatrix[i][j];
                }
            }
        }


    }

    public void set_reactionRadiusByTypeMatrix(ArrayList<ArrayList<Double>> reactionRadiusByTypeMatrix) {
        this.reactionRadiusByTypeMatrix = reactionRadiusByTypeMatrix;
        reactionRadiusByTypeMatrix_dMatrix = new double[reactionRadiusByTypeMatrix.size()][];
        for (int i = 0; i < reactionRadiusByTypeMatrix.size(); i++) {
            reactionRadiusByTypeMatrix_dMatrix[i] = new double[reactionRadiusByTypeMatrix.get(i).size()];
            for (int j = 0; j < reactionRadiusByTypeMatrix.get(i).size(); j++) {
                reactionRadiusByTypeMatrix_dMatrix[i][j] = reactionRadiusByTypeMatrix.get(i).get(j);
            }
        }
    }

    public void set_defaultCollRadiusMap(HashMap<Integer, Double> defaultCollRadiusMap) {
        this.defaultCollRadiusMap = defaultCollRadiusMap;
        defaultCollRadiusMap_dArr = new double[defaultCollRadiusMap.keySet().size()];
        for (int typeId : defaultCollRadiusMap.keySet()) {
            defaultCollRadiusMap_dArr[typeId] = defaultCollRadiusMap.get(typeId);
        }
    }

    public void set_defaultRctRadiusMap(HashMap<Integer, Double> defaultRctRadiusMap) {
        this.defaultRctRadiusMap = defaultRctRadiusMap;
        defaultRctRadiusMap_dArr = new double[defaultRctRadiusMap.keySet().size()];
        for (int typeId : defaultRctRadiusMap.keySet()) {
            defaultRctRadiusMap_dArr[typeId] = defaultRctRadiusMap.get(typeId);
        }
    }

    public void set_existingParticleTypeIds(HashSet<Integer> existingParticleTypeIds) {
        this.existingParticleTypeIds = existingParticleTypeIds;
    }

    public void set_typeIdToTypeNameMap(HashMap<Integer, String> typeIdToTypeNameMap) {
        this.typeIdToTypeNameMap = typeIdToTypeNameMap;
    }

    public void set_typeNameToTypeIdMap(HashMap<String, Integer> typeNameToTypeIdMap) {
        this.typeNameToTypeIdMap = typeNameToTypeIdMap;
    }

    public HashSet<Integer> getAllParticleTypes() {
        return existingParticleTypeIds;
    }


    public int getParticleTypeIdFromTypeName(String typeName) {
        if (typeNameToTypeIdMap.containsKey(typeName)) {
            return typeNameToTypeIdMap.get(typeName);
        } else {
            throw new RuntimeException("the requested typeName " + typeName + " is unknown. abort.");
        }
    }

    public String getParticleTypeNameFromTypeId(int typeId) {
        if (typeIdToTypeNameMap.containsKey(typeId)) {
            return typeIdToTypeNameMap.get(typeId);
        } else {
            throw new RuntimeException("the requested typeId " + typeId + " is unknown. abort.");
        }
    }

    
    // things above are crap
    public double get_maxPInteractionRadius(int type) {
        //return maxInteractionRadiusMapByType.get(type);
        return maxInteractionRadiusMapByType_dArr[type];
    }

    /**
     * this is the maximal distance, two particles can interact with each other
     * either by reaction or collision. So its max(max rCollision, max rInteraction).
     * @param typeId1
     * @param typeId2
     * @return
     */
    public double get_maxPInteractionRadius(int typeId1, int typeId2) {
        //return interactionRadiusByTypeMatrix.get(typeId1).get(typeId2);
        return interactionRadiusByTypeMatrix_dMatrix[typeId1][typeId2];
    }

    /**
     * this is the maximal distance in the system, at which
     * two particles can interact with each other
     * either by reaction or collision. So its max(max rCollision, max rInteraction).
     * @param typeId1
     * @param typeId2
     * @return
     */
    public double get_globalMaxParticleParticleInteractionRadius() {
        //return interactionRadiusByTypeMatrix.get(typeId1).get(typeId2);
        return globalMaxParticleParticleInteractionRadius;
    }

    /**
     * The parameter DfactorNoise is the precomputed scaling factor for the noise.
     * In our case it is Math.sqrt(2 * D *setup.getTimestep());
     * @param pTypeId
     * @return
     */
    public double get_DFactorNoise(int pTypeId) {
        //return dFactorNoiseMap.get(pTypeId);
        return dFactorNoiseMap_dArr[pTypeId];
    }

    /**
     * The parameter DfactorPot is the precomputed scaling factor for the potential displacement.
     * In our case it is: -1 * D * dt / (Kb*T)
     * @param pTypeId
     * @return
     */
    public double get_DFactorPot(int pTypeId) {
        //return dFactorPotMap.get(pTypeId);
        return dFactorPotMap_dArr[pTypeId];
    }

    public double get_pCollisionRadius(int pTypeId1, int pTypeId2) {
        //return collisionRadiusByTypeMatrix.get(pTypeId1).get(pTypeId2);
        return collisionRadiusByTypeMatrix_dMatrix[pTypeId1][pTypeId2];
    }

    public double get_pReactionRadius(int pTypeId1, int pTypeId2) {
        //return reactionRadiusByTypeMatrix.get(pTypeId1).get(pTypeId2);
        return reactionRadiusByTypeMatrix_dMatrix[pTypeId1][pTypeId2];

    }

    public double get_pCollisionRadius(int pTypeId) {
        return defaultCollRadiusMap_dArr[pTypeId];
    }

    public double get_pReactionRadius(int pTypeId) {
        return defaultRctRadiusMap_dArr[pTypeId];
    }

    public boolean doesTypeIdExist(int typeId) {
        return existingParticleTypeIds.contains(typeId);
    }

    public boolean doesTypeNameExist(String typeName) {
        return typeNameToTypeIdMap.containsKey(typeName);
    }

    public IParticleParametersEntry getParticleParametersEntry(int pTypeId) {
        if (pTyeId_to_particleParametersEntry.containsKey(pTypeId)) {
            return pTyeId_to_particleParametersEntry.get(pTypeId);
        } else {
            throw new RuntimeException("the pTypeId " + pTypeId + " is unknown.");
        }
    }

    //########################################################################
    // for optimization purposes -> it is much faster to get stuff from an existing
    // double array thatn everytime ask a class.
    //########################################################################
    public double[][] getCollisionRadiusByTypeMatrix_dMatrix() {
        return collisionRadiusByTypeMatrix_dMatrix;
    }

    public double[][] getInteractionRadiusByTypeMatrix_dMatrix() {
        return interactionRadiusByTypeMatrix_dMatrix;
    }

    public double[] getMaxInteractionRadiusMapByType_dArr() {
        return maxInteractionRadiusMapByType_dArr;
    }

    public double[][] getReactionRadiusByTypeMatrix_dMatrix() {
        return reactionRadiusByTypeMatrix_dMatrix;
    }

    public double[] getdFactorNoiseMap_dArr() {
        return dFactorNoiseMap_dArr;
    }

    public double[] getdFactorPotMap_dArr() {
        return dFactorPotMap_dArr;
    }

    public double[] getDefaultCollRadiusMap_dArr() {
        return defaultCollRadiusMap_dArr;
    }

    public double[] getDefaultRctRadiusMap_dArr() {
        return defaultRctRadiusMap_dArr;
    }
}
