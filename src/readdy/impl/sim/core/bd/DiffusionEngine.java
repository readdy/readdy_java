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
package readdy.impl.sim.core.bd;

import java.util.Iterator;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.bd.INoiseDisplacementComputer;
import readdy.api.sim.core.bd.IPotentialDisplacementComputer;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.impl.tools.ProcessorStopWatch;

/**
 *
 * @author schoeneberg
 */
public class DiffusionEngine implements IDiffusionEngine {

    private IPotentialManager potentialManager;
    private IParticleParameters particleParameters;
    private INoiseDisplacementComputer noiseDisplacementComputer;
    private IPotentialDisplacementComputer potentialDisplacementComputer;
    // for optimization purposes
    double[] defaultCollRadiusMap_dArr;
    double[][] collisionRadiusByTypeMatrix_dMatrix;
    // for the FAQ: What is dFactorNoise:
    // double dFactorNoise = Math.sqrt(2 * D * dt);
    // defined in ParticleParametersFactory.java
    double[] dFactorNoiseMap_dArr;
    // for the FAQ: What is DFactorPot:
    // double dFactorPot = -1 * D * dt / (Kb * T);
    // defined in ParticleParametersFactory.java
    double[] dFactorPotMap_dArr;
    private boolean measureTime = false;
    private ProcessorStopWatch stopWatch = new ProcessorStopWatch();

    public void set_potentialManager(IPotentialManager pScheme) {
        potentialManager = pScheme;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
        this.defaultCollRadiusMap_dArr = particleParameters.getDefaultCollRadiusMap_dArr();
        this.collisionRadiusByTypeMatrix_dMatrix = particleParameters.getCollisionRadiusByTypeMatrix_dMatrix();
        this.dFactorNoiseMap_dArr = particleParameters.getdFactorNoiseMap_dArr();
        this.dFactorPotMap_dArr = particleParameters.getdFactorPotMap_dArr();

    }

    public void set_potentialDisplacementComputer(IPotentialDisplacementComputer pdc) {
        this.potentialDisplacementComputer = pdc;
    }

    public void set_noiseDisplacementComputer(INoiseDisplacementComputer ndc) {
        this.noiseDisplacementComputer = ndc;
    }
    //static int nPot1, nPot2;
    double[] cumulatedDisplacementSingle = new double[3];

    public double[] propagateSingle(IParticle p) {
        int pTypeId = p.get_type();
        //   if(measureTime){stopWatch.measureTime(10, System.nanoTime());}
        double[] initCoords = p.get_coords();
        //   if(measureTime){stopWatch.measureTime(11, System.nanoTime());}
        //NOISE
        //double DFactorNoise = particleParameters.get_DFactorNoise(p.get_type());
        double DFactorNoise = dFactorNoiseMap_dArr[pTypeId];

        //   if(measureTime){stopWatch.measureTime(12, System.nanoTime());}
        double[] noiseDisplacement = noiseDisplacementComputer.computeDisplacement(initCoords, DFactorNoise);
        //   if(measureTime){stopWatch.measureTime(13, System.nanoTime());}
        cumulatedDisplacementSingle = noiseDisplacement;
        //   if(measureTime){stopWatch.measureTime(14, System.nanoTime());}
        // POTENTIALS
        //double DFactorPot = particleParameters.get_DFactorPot(p.get_type());
        double DFactorPot = dFactorPotMap_dArr[pTypeId];

        //double pRadius = particleParameters.get_pCollisionRadius(p.get_type());
        double pRadius = defaultCollRadiusMap_dArr[pTypeId];
        //   if(measureTime){stopWatch.measureTime(15, System.nanoTime());}
        Iterator<IPotential1> potential1Iterator = potentialManager.getPotentials(p);
        //   if(measureTime){stopWatch.measureTime(16, System.nanoTime());}
        while (potential1Iterator.hasNext()) {
            IPotential1 pot1 = potential1Iterator.next();
            double[] potentialDisplacement = potentialDisplacementComputer.computeSingleDisplacement(
                    initCoords,
                    DFactorPot,
                    pRadius,
                    pot1);

            cumulatedDisplacementSingle[0] = cumulatedDisplacementSingle[0] + potentialDisplacement[0];
            cumulatedDisplacementSingle[1] = cumulatedDisplacementSingle[1] + potentialDisplacement[1];
            cumulatedDisplacementSingle[2] = cumulatedDisplacementSingle[2] + potentialDisplacement[2];
            //nPot1++;
        }
        //  if(measureTime){stopWatch.measureTime(17, System.nanoTime());}
/*
        if(measureTime){stopWatch.accumulateTime(10, 10, 11);
        stopWatch.accumulateTime(11, 11, 12);
        stopWatch.accumulateTime(12, 12, 13);
        stopWatch.accumulateTime(13, 13, 14);
        stopWatch.accumulateTime(14, 14, 15);
        stopWatch.accumulateTime(15, 15, 16);
        stopWatch.accumulateTime(16, 16, 17);

        stopWatch.setAccumulatorName(10,"PROPAGATE SINGLE setupArrays                         ");
        stopWatch.setAccumulatorName(11,"PROPAGATE SINGLE get noise parameters                ");
        stopWatch.setAccumulatorName(12,"PROPAGATE SINGLE call noiseDisplacementComputer      ");
        stopWatch.setAccumulatorName(13,"PROPAGATE SINGLE cumulate the noise displacement     ");
        stopWatch.setAccumulatorName(14,"PROPAGATE SINGLE get potential parameters            ");
        stopWatch.setAccumulatorName(15,"PROPAGATE SINGLE get potentials                      ");
        stopWatch.setAccumulatorName(16,"PROPAGATE SINGLE compute potential displacement loop ");}
         */


        return cumulatedDisplacementSingle;
    }
    double[][] cumulatedDisplacementPair = new double[][]{new double[3], new double[3]};

    public double[][] propagatePair(IParticle p1, IParticle p2, double dist) {
        int pTypeId1 = p1.get_type();
        int pTypeId2 = p2.get_type();
        cumulatedDisplacementPair[0][0] = 0;
        cumulatedDisplacementPair[0][1] = 0;
        cumulatedDisplacementPair[0][2] = 0;

        cumulatedDisplacementPair[1][0] = 0;
        cumulatedDisplacementPair[1][1] = 0;
        cumulatedDisplacementPair[1][2] = 0;

        //   if(measureTime){stopWatch.measureTime(0, System.nanoTime());}
        Iterator<IPotential2> pairPotentialIterator = potentialManager.getPotentials(p1, p2);
        //   if(measureTime){stopWatch.measureTime(1, System.nanoTime());}
        double[] initCoords1 = p1.get_coords();
        double[] initCoords2 = p2.get_coords();

        //double[] cumulatedDisplacement0 = new double[p1.get_coords().length];
        //double[] cumulatedDisplacement1 = new double[p2.get_coords().length];
        //   if(measureTime){stopWatch.measureTime(2, System.nanoTime());}
        //double collRadius = particleParameters.get_pCollisionRadius(p1.get_type(), p2.get_type());
        //double DFactorPot1 = particleParameters.get_DFactorPot(p1.get_type());
        //double DFactorPot2 = particleParameters.get_DFactorPot(p2.get_type());
        double collRadius = collisionRadiusByTypeMatrix_dMatrix[pTypeId1][pTypeId2];
        double DFactorPot1 = dFactorPotMap_dArr[pTypeId1];
        double DFactorPot2 = dFactorPotMap_dArr[pTypeId2];
        //   if(measureTime){stopWatch.measureTime(3, System.nanoTime());}

        while (pairPotentialIterator.hasNext()) {
            double[][] displacements = potentialDisplacementComputer.computePairDisplacement(
                    initCoords1,
                    DFactorPot1,
                    initCoords2,
                    DFactorPot2,
                    collRadius,
                    dist,
                    pairPotentialIterator.next());


            cumulatedDisplacementPair[0][0] = cumulatedDisplacementPair[0][0] + displacements[0][0];
            cumulatedDisplacementPair[0][1] = cumulatedDisplacementPair[0][1] + displacements[0][1];
            cumulatedDisplacementPair[0][2] = cumulatedDisplacementPair[0][2] + displacements[0][2];

            cumulatedDisplacementPair[1][0] = cumulatedDisplacementPair[1][0] + displacements[1][0];
            cumulatedDisplacementPair[1][1] = cumulatedDisplacementPair[1][1] + displacements[1][1];
            cumulatedDisplacementPair[1][2] = cumulatedDisplacementPair[1][2] + displacements[1][2];
        }
        /*
        if(measureTime){stopWatch.measureTime(4, System.nanoTime());
        stopWatch.accumulateTime(0, 0, 1);
        stopWatch.accumulateTime(1, 1, 2);
        stopWatch.accumulateTime(2, 2, 3);
        stopWatch.accumulateTime(3, 3, 4);
        stopWatch.setAccumulatorName(0,"PROPAGATE PAIR DiffusionEngine.getPotentials          ");
        stopWatch.setAccumulatorName(1,"PROPAGATE PAIR setup arrays                           ");
        stopWatch.setAccumulatorName(2,"PROPAGATE PAIR get parameters                         ");
        stopWatch.setAccumulatorName(3,"PROPAGATE PAIR compute displacement loop              ");}

         * 
         */
        return cumulatedDisplacementPair;
    }

    public IPotentialManager get_potentialManager() {
        return potentialManager;
    }

    public ProcessorStopWatch get_stopWatch() {
        if (measureTime) {
            System.out.println("measureTime true in DiffusionEngine");

            return stopWatch;

        } else {
            return null;
        }

    }
}
