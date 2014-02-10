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
package readdy.impl.sim.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.api.sim.core.space.INeighborListEntry;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.impl.sim.core.rk.ReactionsOccurredExeption;
import readdy.impl.tools.ProcessorStopWatch;

/**
 * This is the ReaDDy core implementation.
 *
 * It operates on a particle basis and computes the position displacements
 * for particles that arise from diffusion or from potential forces.
 *
 * It is given a list of possible reactions that are parsed to operate on
 * particle level. That means if both reactants are groups of particles
 * actually, the Core is only given information that some specific particle
 * pairs within these groups may react.
 * The core evaluates if a particle pair is able to perform a reaction and
 * also calculates, if this reaction will take place within the current
 * time step. If the reaction would fire, the core sends that information to
 * the Top level where it is handled and the changes in the particle
 * configuration are performed.
 *
 * As a explicit space and time reaction diffusion simulator, ReaDDy's
 * main task is to compute the displacements of particles in every
 * time step. ReaDDy optimization would start at this task, e.g. a parallel
 * version for the displacement computation.
 * For this reason, this task is capsuled entirely within this core module
 * which is designed such, that it can be replaced by different, e.g. parallel
 * implementations.
 * 
 * @author schoeneberg
 */
public class Core implements ICore {

    IParticleConfiguration particleConfig = null;
    IDiffusionEngine diffusionEngine = null;
    IReactionObserver reactionObserver = null;
    IParticleParameters particleParameters = null;
    ArrayList<IOccurredElementalReaction> occurredElementalReactions = new ArrayList();
    //HashMap<IParticle,double[]> cumulatedDisplacementsOfParticles = new HashMap();
    HashMap<Integer, double[]> cumulatedDisplacementsOfParticles = new HashMap();
    private ProcessorStopWatch stopWatch = new ProcessorStopWatch();
    private ProcessorStopWatch stopWatch_diffusionEngine;
    private boolean measureTime = false;
    private static int singles, pairs;

    /**
     * This method takes single and pair particle iterators from the particle
     * configuration and performs for each of their entries a step by the diffusion
     * engine and a step by the reaction observer.
     * This is the very core method of ReaDDy.
     *
     * the assumption is, that reactions occur infrequently
     *
     * therefor the core operates stand-alone and only reports occurring reactions
     * during a step as an exception. This exception is caught on the level above
     * by the Top class where the reaction is handled and performed.
     *
     * @throws ReactionsOccurredExeption
     */
    public void step(int stepId) throws ReactionsOccurredExeption {
        
        
        

        occurredElementalReactions.clear();

        // SPONTANEOUS
        ArrayList<IOccurredElementalReaction> reactionsSpontaneous = reactionObserver.checkSpontaneous(stepId);
        occurredElementalReactions.addAll(reactionsSpontaneous);


        // SINGLES
        //if(measureTime){stopWatch.measureTime(0, System.nanoTime());};
        Iterator<IParticle> singleParticleIterator = particleConfig.particleIterator();
        //if(measureTime){stopWatch.measureTime(1, System.nanoTime());}
        while (singleParticleIterator.hasNext()) {
            //if(measureTime){stopWatch.measureTime(10, System.nanoTime());}
            IParticle p = singleParticleIterator.next();

            // initialize the cumulated displacement with the initial coordinate
            cumulatedDisplacementsOfParticles.put(p.get_id(), p.get_coords());
            //if(measureTime){stopWatch.measureTime(11, System.nanoTime());}

            // potentials
            cumulateDisplacement(p.get_id(), diffusionEngine.propagateSingle(p));
            //if(measureTime){stopWatch.measureTime(12, System.nanoTime());}

            // reactions
            ArrayList<IOccurredElementalReaction> reactionsSingle = reactionObserver.checkSingle(stepId, p);
            //if(measureTime){stopWatch.measureTime(13, System.nanoTime());}
            occurredElementalReactions.addAll(reactionsSingle);
            //if(measureTime){stopWatch.measureTime(14, System.nanoTime());}
            singles++;
            
        }

        if (measureTime) {
            stopWatch.measureTime(2, System.nanoTime());
        }


        // PAIRS

        Iterator<INeighborListEntry> pairParticleIterator = particleConfig.particlePairIterator();
        //if(measureTime){stopWatch.measureTime(3, System.nanoTime());}
        while (pairParticleIterator.hasNext()) {
            //counter++;
            //t3+=System.currentTimeMillis();
            //if(measureTime){stopWatch.measureTime(20, System.nanoTime());}
            INeighborListEntry neighborListEntry = pairParticleIterator.next();
            int pId0 = neighborListEntry.getId1();
            int pId1 = neighborListEntry.getId2();
            //if(measureTime){stopWatch.measureTime(21, System.nanoTime());}
            IParticle p1 = particleConfig.getParticle(pId0);
            IParticle p2 = particleConfig.getParticle(pId1);


            //if(measureTime){stopWatch.measureTime(22, System.nanoTime());}
            double dist = neighborListEntry.getDist();
            //if(measureTime){stopWatch.measureTime(23, System.nanoTime());}
            // potentials

            double[][] cumulatedDisplacements = diffusionEngine.propagatePair(p1, p2, dist);
            //if(measureTime){stopWatch.measureTime(24, System.nanoTime());}
            cumulateDisplacement(pId0, cumulatedDisplacements[0]);
            cumulateDisplacement(pId1, cumulatedDisplacements[1]);
            //if(measureTime){stopWatch.measureTime(25, System.nanoTime());}


            // reactions
            ArrayList<IOccurredElementalReaction> reactionsPair = reactionObserver.checkPair(stepId, p1, p2, dist);
            //if(measureTime){stopWatch.measureTime(26, System.nanoTime());}
            occurredElementalReactions.addAll(reactionsPair);
            //if(measureTime){stopWatch.measureTime(27, System.nanoTime());}
            pairs++;



        }
        if (measureTime) {
            stopWatch.measureTime(4, System.nanoTime());
        }
        // apply the cumulated displacements to all particles
        for (int pId : cumulatedDisplacementsOfParticles.keySet()) {
            //double[] initCoords = p.get_coords();
            //double[] totalCumulatedDisplacement = cumulatedDisplacementsOfParticles.get(p);
            //AdvancedSystemOut.print("totalCumulatedDisplacement", totalCumulatedDisplacement);
            //double[] newCoords = DoubleArrays.add(initCoords, totalCumulatedDisplacement);
            particleConfig.setCoordinates(pId, cumulatedDisplacementsOfParticles.get(pId));
        }
        cumulatedDisplacementsOfParticles.clear();
        
        // update the neighbor List distances for all neighbor List entries
        // this could be done during the particle displacements. This way, however, 
        // it is much cheaper since all particles have their new positions. All 
        // all neighbors are known so only the distances between the neighbors have
        // to be calculated.
        particleConfig.updateNeighborListDistances();
        

        if (measureTime) {
            stopWatch.measureTime(5, System.nanoTime());
        }
        
        
        // randomly shuffle the occurred elemental reactions.
        // this ensures, that it is not ordered like 
        // 0th order first,
        // 1th order scnd,
        // 2nd order third...
        // this introduces an ordering in the reactions which is not wat we want!
        Collections.shuffle(occurredElementalReactions);
        
        
        // give an alert when reactions happened,
        // this is catched by the top level who handles reactions
        if (!occurredElementalReactions.isEmpty()) {
            throw new ReactionsOccurredExeption();
        }
        
        
        
        

        /*
        if(measureTime){stopWatch.measureTime(6, System.nanoTime());}
        if(measureTime){stopWatch.accumulateTime(0, 0, 1);}
        if(measureTime){stopWatch.accumulateTime(1, 1, 2);}
        if(measureTime){stopWatch.accumulateTime(2, 2, 3);}
        if(measureTime){stopWatch.accumulateTime(3, 3, 4);}
        if(measureTime){stopWatch.accumulateTime(4, 4, 5);}
        if(measureTime){stopWatch.accumulateTime(5, 5, 6);}
        if(measureTime){stopWatch.accumulateTime(6, 0, 6);}

        // do single particle stuff
        if(measureTime){stopWatch.accumulateTime(10, 10, 11);}
        if(measureTime){stopWatch.accumulateTime(11, 11, 12);}
        if(measureTime){stopWatch.accumulateTime(12, 12, 13);}
        if(measureTime){stopWatch.accumulateTime(13, 13, 14);}

        stopWatch.setAccumulatorName(10, "SINGLE getParticle                                 ");
        stopWatch.setAccumulatorName(11, "SINGLE call diffusionEngine.propagateSingle()      ");
        stopWatch.setAccumulatorName(12, "SINGLE call reactionEngine.check single reactions  ");
        stopWatch.setAccumulatorName(13, "SINGLE add all reactions                           ");


        // do pair particle stuff
        if(measureTime){stopWatch.accumulateTime(20, 20, 21);}
        if(measureTime){stopWatch.accumulateTime(21, 21, 22);}
        if(measureTime){stopWatch.accumulateTime(22, 22, 23);}
        if(measureTime){stopWatch.accumulateTime(23, 23, 24);}
        if(measureTime){stopWatch.accumulateTime(24, 24, 25);}
        if(measureTime){stopWatch.accumulateTime(25, 25, 26);}
        if(measureTime){stopWatch.accumulateTime(26, 26, 27);}


        stopWatch.setAccumulatorName(20, "PAIR getNeighborListEntry                 ");
        stopWatch.setAccumulatorName(21, "PAIR getParticles from ParticleConfig     ");
        stopWatch.setAccumulatorName(22, "PAIR get distance                         ");
        stopWatch.setAccumulatorName(23, "PAIR call diffusionEngine.propagatePair() ");
        stopWatch.setAccumulatorName(24, "PAIR cumulate displacements               ");
        stopWatch.setAccumulatorName(25, "PAIR check reactions                      ");
        stopWatch.setAccumulatorName(26, "PAIR add occ reactions to list            ");


         *
         */
    }

    public ProcessorStopWatch getStopWatch() {
        if (measureTime) {
            return stopWatch;
        } else {
            return null;
        }
    }

    public ProcessorStopWatch getDiffusionEngineStopWatch() {
        return stopWatch_diffusionEngine;
    }

    private void cumulateDisplacement(int pId, double[] displacementToBeAdded) {
        if (cumulatedDisplacementsOfParticles.containsKey(pId)) {
            double[] currentDisplacement = cumulatedDisplacementsOfParticles.get(pId);
            currentDisplacement[0] = currentDisplacement[0] + displacementToBeAdded[0];
            currentDisplacement[1] = currentDisplacement[1] + displacementToBeAdded[1];
            currentDisplacement[2] = currentDisplacement[2] + displacementToBeAdded[2];
        } else {
            cumulatedDisplacementsOfParticles.put(pId, displacementToBeAdded);
        }
    }

    // SETTER ******************************************************************
    public void set_ParticleConfiguration(IParticleConfiguration particleConfig) {
        this.particleConfig = particleConfig;
    }

    public void set_DiffusionEngine(IDiffusionEngine diffusionEngine) {
        this.diffusionEngine = diffusionEngine;
        stopWatch_diffusionEngine = diffusionEngine.get_stopWatch();
    }

    public void set_ReactionObserver(IReactionObserver reactionObserver) {
        this.reactionObserver = reactionObserver;
    }

    // GETTER ******************************************************************
    public IParticleConfiguration get_ParticleConfiguration() {
        return particleConfig;
    }

    public ArrayList<IOccurredElementalReaction> get_OccurredElementalReactions() {
        return occurredElementalReactions;
    }

    public IPotentialManager get_PotentialManager() {
        return diffusionEngine.get_potentialManager();
    }

}
