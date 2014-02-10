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
package readdy.impl.sim.core_mc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.bd.INoiseDisplacementComputer;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.api.sim.core.space.INeighborListEntry;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core_mc.IMetropolisDecider;
import readdy.api.sim.core_mc.IPotentialEnergyComputer;
import readdy.impl.io.out.CSV_Writer;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core.rk.ReactionsOccurredExeption;
import readdy.impl.tools.ProcessorStopWatch;
import statlab.base.datatypes.IIntIterator;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class Core_MC implements ICore {

    IParticleConfiguration particleConfiguration = null;
    IReactionObserver reactionObserver = null;
    IParticleParameters particleParameters = null;
    INoiseDisplacementComputer noiseDisplacementComputer = null;
    IPotentialEnergyComputer potentialEnergyComputer = null;
    IMetropolisDecider metropolisDecider = null;
    ArrayList<IOccurredElementalReaction> occurredElementalReactions = new ArrayList();
    // current potential Energy of the system
    double E0 = 0.;
    private ProcessorStopWatch stopWatch = new ProcessorStopWatch();
    private ProcessorStopWatch stopWatch_diffusionEngine;
    private boolean measureTime = false;
    double[] dFactorNoiseMap_dArr;
    double[] dFactorPotMap_dArr;
    private static boolean alreadyInitialized = false;
    // we will compute the average from the last n acceptance probabilities
    private static double sumOfTheLastNAcceptanceProbabilities = 0;
    private static LinkedList lastNAcceptanceProbabilities = new LinkedList();
    private static int maxNumberOfStoredAcceptanceProbabilities = 100;
    private static int numberOfStoredAcceptanceProbabilities = 0;
    // just for some output stuff
    private static boolean outputProbabilities = false;
    CSV_Writer csvWriter = new CSV_Writer();

    public Core_MC() {
        alreadyInitialized = false;
        if (outputProbabilities) {
            csvWriter.open("./../ReaDDy_MC_output_0/out_moveAcceptanceProbability.csv");
        }
    }

    // SETTER ******************************************************************
    public void set_ParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
        this.dFactorNoiseMap_dArr = particleParameters.getdFactorNoiseMap_dArr();
    }

    public void set_ParticleConfiguration(IParticleConfiguration particleConfig) {
        this.particleConfiguration = particleConfig;
    }

    public void set_ReactionObserver(IReactionObserver reactionObserver) {
        this.reactionObserver = reactionObserver;
    }

    public void set_NoiseDisplacementComputer(INoiseDisplacementComputer noiseDisplacementComputer) {
        this.noiseDisplacementComputer = noiseDisplacementComputer;
    }

    public void set_PotentialEnergyComputer(IPotentialEnergyComputer potentialEnergyComputer) {
        this.potentialEnergyComputer = potentialEnergyComputer;
    }

    public void set_MetropolisDecider(IMetropolisDecider metropolisDecider) {
        this.metropolisDecider = metropolisDecider;
    }

    // GETTER ******************************************************************
    public IParticleConfiguration get_ParticleConfiguration() {
        return particleConfiguration;
    }

    public ArrayList<IOccurredElementalReaction> get_OccurredElementalReactions() {
        return occurredElementalReactions;
    }

    public IPotentialManager get_PotentialManager() {
        return potentialEnergyComputer.get_potentialManager();
    }

    /**
     * this method is a derivative of the Core.java class of ReaDDy
     * for every particle it does the following:
     *
     *  0. Have the current potential energy of the particle E0 at position c0
     *  1. Compute a new position for that particular partilcle, c1
     *  2. Compute the potential energy change of ths system for placing particle E1 to position c1
     *  3. compute the potential energy difference dE = E1-E0
     *  4. Decide via a metropolis based on the boltzmann distribution if we accept the move
     *  5. If we accept the move, move the particle. If not - do nothing.
     *
     * So far this class does not feature reactions but is limited on displacements of particles
     * by schoeneberg 2012_10_31
     * @param stepId
     * @throws ReactionsOccurredExeption
     */
    public void step(int stepId) throws ReactionsOccurredExeption {
        int trialCounter = 0;
        int acceptedMoveCounter = 0;


        if (!alreadyInitialized) {
            // compute the current potential energy of the system.
            initialize();
        }

        //System.out.println("MONTE CARLO STEP");

        occurredElementalReactions.clear();

        // SPONTANEOUS
        //ArrayList<IOccurredElementalReaction> reactionsSpontaneous = reactionObserver.checkSpontaneous(stepId);
        //occurredElementalReactions.addAll(reactionsSpontaneous);


        // SINGLES
        //if(measureTime){stopWatch.measureTime(0, System.nanoTime());};
        Iterator<IParticle> singleParticleIterator = particleConfiguration.particleIterator();
        //if(measureTime){stopWatch.measureTime(1, System.nanoTime());}
        while (singleParticleIterator.hasNext()) {
            trialCounter++;
            //if(measureTime){stopWatch.measureTime(10, System.nanoTime());}
            IParticle p = singleParticleIterator.next();

            //  0. What is the current energy and current position of the particle
            double[] c0 = p.get_coords();
            int pId = p.get_id();

            //  1. Compute the new position of the particle

            int pTypeId = p.get_type();
            double DFactorNoise = dFactorNoiseMap_dArr[pTypeId];
            double[] noiseDisplacement = noiseDisplacementComputer.computeDisplacement(c0, DFactorNoise);
            double[] c1 = DoubleArrays.add(c0, noiseDisplacement);

            //  2. Compute the potential energy change of ths system for placing particle E1 to position c1

            // 2.1 what is the energy difference if we remove the particle from its current position?
            double dE_removeFrom_c0 = -getCurrentPotentialEnergyContribution_singleParticle(pId, pTypeId, c0);

            // 2.2 what is the energy difference if we put the particle to its new position?
            double dE_putTo_c1 = getCurrentPotentialEnergyContribution_singleParticle(pId, pTypeId, c1);

            // for this scheme it is obviously not necessary to compute E0
            // it is however nice to see the total potential energy of a system decreasing
            // while doing the monte carlo scheme. Thats the reason for having it here.
            double E1 = E0 + dE_removeFrom_c0 + dE_putTo_c1;

            //  3. compute the potential energy difference dE = E1-E0
            double dE = E1 - E0;

            //  4. Decide via a metropolis based on the boltzmann distribution if we accept the move
            boolean moveAccepted = metropolisDecider.doWeAcceptGivenEnergyDifference(dE);

            //  5. If we accept the move, move the particle. If not - do nothing.
            if (moveAccepted) {
                acceptedMoveCounter++;
                // do the move
                particleConfiguration.setCoordinates(pId, c1);
                // change current energy of the system
                E0 += dE;
            }

            particleConfiguration.setSystemPotentialEnergy(E0);

            //if(measureTime){stopWatch.measureTime(11, System.nanoTime());}

        }

        //if(measureTime){stopWatch.measureTime(2, System.nanoTime());}


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


        double acceptanceProbability = acceptedMoveCounter / (double) trialCounter;
        double avgAcceptanceProbability = getAvgAcceptanceProbability(acceptanceProbability);
        if (outputProbabilities) {
            if (stepId % 1000 == 0) {
                ArrayList<ArrayList<String>> data = new ArrayList();
                ArrayList<String> line = new ArrayList();
                line.add(acceptanceProbability + "");
                line.add(avgAcceptanceProbability + "");
                data.add(line);
                DataReadyForOutput drfo = new DataReadyForOutput();
                drfo.set_data(data);
                if (csvWriter != null) {
                    csvWriter.write(stepId, drfo);
                } else {
                    System.out.println("csvWriter == null. There is maybe a problem here.");
                }
            }
        }
        //System.out.println("step,p_acc,<p_acc>_100: , "+stepId+", "+acceptanceProbability+", "+avgAcceptanceProbability);
        //System.out.println("singles,pairs "+singles+","+pairs);
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

    public void initialize() {
        //System.out.println("initialize the Core...");
        if (!alreadyInitialized) {
            // compute the current potential energy of the system.



            if (allNecessaryInputAvailable()) {
                E0 = 0;

                // potentials of order 1
                double E_potOrder1 = 0;
                Iterator<IParticle> singleParticleIterator = particleConfiguration.particleIterator();
                while (singleParticleIterator.hasNext()) {
                    IParticle p = singleParticleIterator.next();
                    E_potOrder1 += potentialEnergyComputer.computeEnergy(p);
                }

                double E_potOrder2 = 0;
                Iterator<INeighborListEntry> pairParticleIterator = particleConfiguration.particlePairIterator();
                while (pairParticleIterator.hasNext()) {
                    INeighborListEntry nle = pairParticleIterator.next();

                    int id0 = nle.getId1();
                    IParticle p0 = particleConfiguration.getParticle(id0);
                    int id1 = nle.getId2();
                    IParticle p1 = particleConfiguration.getParticle(id1);
                    E_potOrder2 += potentialEnergyComputer.computeEnergy(p0, p1);
                }

                E0 = E_potOrder1 + E_potOrder2;
                //System.out.println("current potential Energy: " + E0);
                particleConfiguration.setSystemPotentialEnergy(E0);

                alreadyInitialized = true;
            } else {
                throw new RuntimeException("not all input available. Abort!");
            }
        }
    }

    private boolean allNecessaryInputAvailable() {
        return particleConfiguration != null
                && reactionObserver != null
                && particleParameters != null
                && noiseDisplacementComputer != null
                && potentialEnergyComputer != null
                && metropolisDecider != null;
    }

    /**
     * warning - do not use this function for a global calculation
     * because it will double count pairs when you use it in that way
     * @param pId
     * @param pTypeId
     * @param coords
     * @return
     */
    private double getCurrentPotentialEnergyContribution_singleParticle(int pId, int pTypeId, double[] coords) {
        IParticle p1 = new Particle(pId, pTypeId, coords);
        // potentials of order 1
        double E_potOrder1 = potentialEnergyComputer.computeEnergy(p1);
        // potentials of order 2
        double E_potOrder2 = 0;
        IIntIterator iterator_particleNeighborIds_c0 = particleConfiguration.getNeighboringParticleIds(coords);
        while (iterator_particleNeighborIds_c0.hasNext()) {
            int neighbor_pId = iterator_particleNeighborIds_c0.next();

            if (pId != neighbor_pId) { // prevent identical particles from being computed a potential between them
                IParticle p2 = particleConfiguration.getParticle(neighbor_pId);
                E_potOrder2 += potentialEnergyComputer.computeEnergy(p1, p2);
            }
        }
        return E_potOrder1 + E_potOrder2;
    }

    private double getAvgAcceptanceProbability(double acceptanceProbability) {

        if (numberOfStoredAcceptanceProbabilities < maxNumberOfStoredAcceptanceProbabilities) {
            lastNAcceptanceProbabilities.addFirst(acceptanceProbability);
            sumOfTheLastNAcceptanceProbabilities += acceptanceProbability;
            numberOfStoredAcceptanceProbabilities++;

        } else {

            sumOfTheLastNAcceptanceProbabilities += acceptanceProbability;
            lastNAcceptanceProbabilities.addFirst(acceptanceProbability);
            sumOfTheLastNAcceptanceProbabilities -= (Double) lastNAcceptanceProbabilities.getLast();
            lastNAcceptanceProbabilities.removeLast();
        }

        return sumOfTheLastNAcceptanceProbabilities / (double) numberOfStoredAcceptanceProbabilities;



    }
}
