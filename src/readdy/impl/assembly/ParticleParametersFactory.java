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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.particle.IParticleParametersEntry;
import readdy.impl.sim.core.particle.ParticleParameters;
import readdy.impl.sim.core.particle.ParticleParametersEntry;

/**
 *
 * @author schoeneberg
 */
public class ParticleParametersFactory implements IParticleParametersFactory {
    boolean verbose = false;
    
    IParamParticlesFileData paramParticlesFileData = null;
    IGlobalParameters globalParameters = null;

    HashMap<Integer, Double> defaultCollRadiusMap = new HashMap();
    ArrayList<ArrayList<Double>> collisionRadiusByTypeMatrix = new ArrayList();

    HashMap<Integer, Double> defaultRctRadiusMap = new HashMap();
    ArrayList<ArrayList<Double>> reactionRadiusByTypeMatrix = new ArrayList();

    Map<Integer, Double> maxInteractionRadiusMapByType = new HashMap();
    ArrayList<ArrayList<Double>> maxInteractionRadiusByTypeMatrix = new ArrayList();


    HashMap<Integer, Double> dFactorNoiseMap = new HashMap();
    HashMap<Integer, Double> dFactorPotMap = new HashMap();
    HashSet<Integer> existingParticleTypeIds = new HashSet();

        HashMap<Integer,String> typeIdToTypeNameMap = new HashMap();
    HashMap<String,Integer> typeNameToTypeIdMap = new HashMap();

    HashMap<Integer,IParticleParametersEntry> pTyeId_to_particleParametersEntry;
    

    public void set_paramParticlesFileData(IParamParticlesFileData paramParticlesFileData) {
        this.paramParticlesFileData = paramParticlesFileData;
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public IParticleParameters createParticleParameters() {
        if (paramParticlesFileData != null && globalParameters != null) {

            ParticleParameters particleParameters = new ParticleParameters();
            kondenseInputDataToUsefullComponents();
            
            
            particleParameters.set_dFactorNoiseMap(dFactorNoiseMap);
            particleParameters.set_dFactorPotMap(dFactorPotMap);

            particleParameters.set_existingParticleTypeIds(existingParticleTypeIds);
            
            particleParameters.set_collisionRadiusByTypeMatrix(collisionRadiusByTypeMatrix);
            particleParameters.set_defaultCollRadiusMap(defaultCollRadiusMap);

            particleParameters.set_reactionRadiusByTypeMatrix(reactionRadiusByTypeMatrix);
            particleParameters.set_defaultRctRadiusMap(defaultRctRadiusMap);

            particleParameters.set_maxInteractionRadiusMapByType(maxInteractionRadiusMapByType);
            particleParameters.set_maxInteractionRadiusByTypeMatrix(maxInteractionRadiusByTypeMatrix);
            
            particleParameters.set_typeIdToTypeNameMap(typeIdToTypeNameMap);
            particleParameters.set_typeNameToTypeIdMap(typeNameToTypeIdMap);

            particleParameters.setpTyeId_to_particleParametersEntry(pTyeId_to_particleParametersEntry);

            return particleParameters;
        } else {
            throw new RuntimeException("ParticleParameters Factory called without giving"
                    + "it sufficient input. Either paramParticlesFileData is not present"
                    + "or globalParameters is not present.");
        }
    }

    private void kondenseInputDataToUsefullComponents() {

        defaultCollRadiusMap= new HashMap();
        collisionRadiusByTypeMatrix = new ArrayList();

        defaultRctRadiusMap = new HashMap();
        reactionRadiusByTypeMatrix = new ArrayList();
        dFactorNoiseMap = new HashMap();
        dFactorPotMap = new HashMap();

        maxInteractionRadiusMapByType = new HashMap();
        maxInteractionRadiusByTypeMatrix = new ArrayList();

        double Kb = globalParameters.get_Kb();
        double T = globalParameters.get_T();
        double dt = globalParameters.get_dt();

        ArrayList<IParticleData> particleDataList = paramParticlesFileData.get_particleDataList();


        pTyeId_to_particleParametersEntry=new HashMap();
        for (IParticleData pd : particleDataList) {
            int typeId1 = pd.getTypeId();

            
            pTyeId_to_particleParametersEntry.put(typeId1,new ParticleParametersEntry(pd));

            existingParticleTypeIds.add(typeId1);
            typeIdToTypeNameMap.put(typeId1,pd.getType());
            typeNameToTypeIdMap.put(pd.getType(),typeId1);

            // compute diffusion speed factor related precomputations
            double D = pd.getD();
            double dFactorNoise = Math.sqrt(2 * D * dt);
            double dFactorPot = -1 * D * dt / (Kb * T);
            dFactorNoiseMap.put(typeId1, dFactorNoise);
            dFactorPotMap.put(typeId1, dFactorPot);

            defaultCollRadiusMap.put(typeId1, pd.get_defaultCollR());
            defaultRctRadiusMap.put(typeId1, pd.get_defaultRctR());

            // compute the reaction and collision radii
            ArrayList<Double> collision_list = new ArrayList(pd.getCollisionRadiusMap().keySet().size());
            ArrayList<Double> reaction_list = new ArrayList(pd.getCollisionRadiusMap().keySet().size());
            ArrayList<Double> interaction_list = new ArrayList(pd.getCollisionRadiusMap().keySet().size());

            
            ArrayList<Integer> list = new ArrayList();
            for (int typeId: pd.getCollisionRadiusMap().keySet()) {
                list.add(typeId);
            }
            Collections.sort(list);
            for (int typeId2 : list) {
                // what type1 radius when in contact with type2 + type2 radius when in contact with type1
                double collRadiiSum = pd.getCollisionRadiusMap().get(typeId2)
                        + particleDataList.get(typeId2).getCollisionRadiusMap().get(typeId1);

                double rctRadiiSum = pd.getReactionRadiusMap().get(typeId2)
                        + particleDataList.get(typeId2).getReactionRadiusMap().get(typeId1);

                collision_list.add(typeId2,collRadiiSum);
                reaction_list.add(typeId2,rctRadiiSum);
                interaction_list.add(typeId2,Math.max(collRadiiSum,rctRadiiSum));
            }
            collisionRadiusByTypeMatrix.add(pd.getTypeId(), collision_list);
            reactionRadiusByTypeMatrix.add(pd.getTypeId(), reaction_list);
            maxInteractionRadiusByTypeMatrix.add(pd.getTypeId(),interaction_list);

        }

        // compute for each particle the maximal interaction radius that he has.
        for (IParticleData pd : particleDataList) {
            int typeId = pd.getTypeId();
            double radius = 0;
            // what is the largest radius, the particle has, independent of the reaction partner
            for (double r : pd.getCollisionRadiusMap().values()) {
                if (r > radius) {
                    radius = r;
                }
            }
            for (double r : pd.getReactionRadiusMap().values()) {
                if (r > radius) {
                    radius = r;
                }
            }

            maxInteractionRadiusMapByType.put(typeId, radius);

        }

        if(verbose){
            //print collision radius matrix
            System.out.println("collision radii matrix by type Id");
            printMatrix(collisionRadiusByTypeMatrix);
            checkMatrixSymmetry(collisionRadiusByTypeMatrix);
            System.out.println("reaction radii matrix by type Id");
            printMatrix(reactionRadiusByTypeMatrix);
            checkMatrixSymmetry(reactionRadiusByTypeMatrix);
            System.out.println("max interaction radii matrix by type Id");
            printMatrix(maxInteractionRadiusByTypeMatrix);
            checkMatrixSymmetry(maxInteractionRadiusByTypeMatrix);
        }
    }

    private void printMatrix(ArrayList<ArrayList<Double>> m) {
        int i = 0;
        for (ArrayList<Double> list : m) {
            System.out.println(i);
            for (double collRadius : list) {
                System.out.print(collRadius + ", ");
            }
            System.out.println();
            i++;
        }
    }

    private void checkMatrixSymmetry(ArrayList<ArrayList<Double>> m) {
        for (int i = 0; i < m.size(); i++) {
            for (int j = 0; j < m.get(0).size(); j++) {
                double d1 = m.get(i).get(j);
                double d2 = m.get(j).get(i);
                if (d1 != d2) {
                    System.out.println(i + " " + j + " " + m.get(i).get(j) + " " + m.get(j).get(i));
                    throw new RuntimeException("checked matrix not symmetric!");
                }

            }

        }
    }
}
