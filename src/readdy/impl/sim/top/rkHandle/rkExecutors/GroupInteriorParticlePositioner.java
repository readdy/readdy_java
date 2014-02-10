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
package readdy.impl.sim.top.rkHandle.rkExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.impl.sim.top.group.ExtendedIdAndType;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class GroupInteriorParticlePositioner implements IGroupInteriorParticlePositioner {

    Random rand = new Random();
    IGroupParameters groupParameters = null;

    public void setGroupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }

    public ArrayList<IParticle> positionParticles(int groupTypeId, Set<IParticle> particlesToPositionReference) {
        Set<IParticle> particlesToPosition = new HashSet();
        particlesToPosition.addAll(particlesToPositionReference);
        IExtendedIdAndType group = new ExtendedIdAndType(true, -1, groupTypeId);
        ArrayList<IParticle> positionedParticles = positionParticlesWithinBuildingBlock(group, particlesToPosition);
        // during the positioning, every call of the position method takes particles out of the set.
        // if there are remaining particles this is a problem
        if (!particlesToPosition.isEmpty()) {
            System.out.println("Problem during particle positioning: " + particlesToPosition.size() + " particles were not positioned:");
            for (IParticle p : particlesToPosition) {
                p.print();
            }
            throw new RuntimeException("the positioning failed.");
        }
        return positionedParticles;
    }

    private ArrayList<IParticle> positionParticlesWithinBuildingBlock(IExtendedIdAndType buildingBlock, Set<IParticle> particlesToPosition) {
        boolean verbose = false;
        if (verbose) {
            System.out.println("------");
        }
        if (verbose) {
            System.out.println("<new call particlesToPositionSize: " + particlesToPosition.size());
        }
        if (verbose) {
            buildingBlock.print();
        }
        if (verbose) {
            System.out.println(">");
        }
        ArrayList<IParticle> positionedParticles = new ArrayList();
        if (buildingBlock.get_isGroup() == false) {
            if (verbose) {
                System.out.println("buildingBlock is Particle:...");
            }
            if (verbose) {
                System.out.println("<ParticleBuildingBlock>");
            }

            buildingBlock.print();
            // we are done
            // find the matching particle within the eductParticles

            for (IParticle currentParticleToPosition : particlesToPosition) {
                if (buildingBlock.get_type() == currentParticleToPosition.get_type()) {

                    positionedParticles.add(currentParticleToPosition);
                    particlesToPosition.remove(currentParticleToPosition);
                    break;
                }
            }
            if (verbose) {
                System.out.println("</ParticleBuildingBlock>");
            }

        } else {

            if (verbose) {
                System.out.println("buildingBlock is Group - go deeper:...");
            }

            ArrayList<IExtendedIdAndType> buildingBlocksDeeper = groupParameters.getBuildingBlocks(buildingBlock.get_type());
            if (verbose) {
                System.out.println("<buildingBlocks deeper:>");
            }
            for (IExtendedIdAndType buildingBlockDeeper : buildingBlocksDeeper) {
                if (verbose) {
                    System.out.println("<buildingBlockDeeper>");
                }
                if (verbose) {
                    buildingBlockDeeper.print();
                }
                positionedParticles.addAll(positionParticlesWithinBuildingBlock(buildingBlockDeeper, particlesToPosition));
                if (verbose) {
                    System.out.println("</buildingBlockDeeper>");
                }

            }
            if (verbose) {
                System.out.println("</buildingBlocks deeper:>");
            }
        }



        return positionedParticles;
    }

    public ArrayList<IParticle> rearrangeParticlesWithinGroups_fusion(IGroup eductGroup1, IGroup eductGroup2, int productGroupType) {
        // we reject particle matching between template coords and actual coords
        // if their distances deviate more than the matchingDistanceTreshold
        double matchingDistanceTreshold = 0.8;
        int numberOfRandomOrderMatchingTrials = 10;
        int trialCounter = 0;
        boolean matchFound = false;


        ArrayList<IParticle> positionedParticles = new ArrayList();
        // check if we have enough building blocks (particles)

        ArrayList<IParticle> Educt1BuildingBlocksCoords = eductGroup1.get_positionedMemberParticles();
        ArrayList<IParticle> Educt2BuildingBlocksCoords = eductGroup2.get_positionedMemberParticles();

        ArrayList<IExtendedIdAndType> ProductBuildingBlocks = new ArrayList();
        // generate a new array because we want to draw some of these product building blocks if
        // educt particles were placed on its positions
        int nBuildingBlocksOfProduct = groupParameters.getBuildingBlocks(productGroupType).size();
        
        if (Educt1BuildingBlocksCoords.size() + Educt2BuildingBlocksCoords.size() == nBuildingBlocksOfProduct) {

            // we made sure, that we have enough building blocks,
            // now match the educt blocks with the product ones.

            // this is not the most sophisticated way of doing this
            // maybe introduce a 3d point pattern matching scheme here later

            ArrayList<double[]> productTemplateCoords = groupParameters.getBuildingBlockTemplateCoordinates(productGroupType);
            HashMap<Integer, IParticle> placedBuildingBlocks = new HashMap();
            while (!matchFound) {
                System.out.println("--------------------------------------");
                System.out.println("matching trial " + trialCounter + " ...");
                ArrayList<IParticle> toBePlacedBlocks = new ArrayList();
                toBePlacedBlocks.addAll(Educt1BuildingBlocksCoords);
                toBePlacedBlocks.addAll(Educt2BuildingBlocksCoords);

                placedBuildingBlocks.clear();

                ProductBuildingBlocks.addAll(groupParameters.getBuildingBlocks(productGroupType));
            
                while (!toBePlacedBlocks.isEmpty()) {

                    System.out.println(">>>toBePlacedBlocks length:" + toBePlacedBlocks.size());
                    // match a building block

                    int idToRemove = rand.nextInt(toBePlacedBlocks.size());
                    IParticle particle = toBePlacedBlocks.remove(idToRemove);
                    // find a product building block that matches

                    for (IExtendedIdAndType templateBlock : ProductBuildingBlocks) {

                        if (templateBlock.get_type() == particle.get_type()) {
                            // found a theoretical match.

                            // now check if the geometry constraints hold
                            boolean doDistanceConstraintsHold = true;
                            for (int alreadyPlacedTemplateId0 : placedBuildingBlocks.keySet()) {
                                for (int alreadyPlacedTemplateId1 : placedBuildingBlocks.keySet()) {
                                    // if the ids are the same, drop the request because its senseless
                                    if (alreadyPlacedTemplateId0 != alreadyPlacedTemplateId1) {
                                        // distance between the template coordinates
                                        System.out.println("check distance constraints...");
                                        double distTemplate = DoubleArrays.distance(
                                                productTemplateCoords.get(alreadyPlacedTemplateId0),
                                                productTemplateCoords.get(alreadyPlacedTemplateId1));

                                        // distance between the actual coordinates if we would place
                                        // the new point there
                                        double distNewConfig = DoubleArrays.distance(
                                                placedBuildingBlocks.get(alreadyPlacedTemplateId0).get_coords(),
                                                placedBuildingBlocks.get(alreadyPlacedTemplateId1).get_coords());
                                        System.out.println("distNewConfig " + distNewConfig);
                                        System.out.println("distTemplate " + distTemplate);
                                        double frac = distNewConfig / distTemplate;
                                        System.out.println("frac: " + frac);
                                        if (frac == Double.NaN) {
                                            doDistanceConstraintsHold = false;
                                            break;
                                        }
                                        if (frac > 1) {
                                            if (1 / frac < matchingDistanceTreshold) {
                                                doDistanceConstraintsHold = false;
                                                break;
                                            }
                                        } else {
                                            if (frac < matchingDistanceTreshold) {
                                                doDistanceConstraintsHold = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (doDistanceConstraintsHold) {

                                // add the block to the placed blocks at the position of the id of the
                                // template block
                                placedBuildingBlocks.put(templateBlock.get_id(), particle);
                                ProductBuildingBlocks.remove(templateBlock);
                                System.out.println("match found");
                                System.out.println("templateBlock.get_id()" + templateBlock.get_id() + " particle");
                                particle.print();
                                matchFound = true;
                                break;
                            } else {
                                matchFound = false;
                            }
                        }
                    }
                }
                trialCounter++;
                if (trialCounter > numberOfRandomOrderMatchingTrials) {
                    break;
                }
            }
            if (!matchFound) {
                throw new RuntimeException("particle matching for group reaction failed.");
            }
            System.out.println("===================================");
            System.out.println("match found!");
            // we matched all particles and are done.
            for (int key : placedBuildingBlocks.keySet()) {
                System.out.println("key:" + key + " value ");
                placedBuildingBlocks.get(key).print();
                positionedParticles.add(key, placedBuildingBlocks.get(key));
            }
            if (positionedParticles.size() != nBuildingBlocksOfProduct) {
                throw new RuntimeException("we didnt place all particles necessary ("+
                        positionedParticles.size()+")to build the group("+ProductBuildingBlocks.size()+").");
            }
            return positionedParticles;
        } else {
            throw new RuntimeException("the sum of educt building blocks is not enough to build the product. abort!");
        }
    }
}
