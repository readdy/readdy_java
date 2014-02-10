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
package readdy.impl.analysis.runtimeAnalyser;

import readdy.api.analysis.IReaddyRuntimeAnalyser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import statlab.base.util.DoubleArrays;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 */
public class PairwiseDistanceAnalyser implements IReaddyRuntimeAnalyser {

    private IParticleConfiguration particleConfig;
    private static BufferedWriter out;

    public PairwiseDistanceAnalyser() {
        try {
            this.out = new BufferedWriter(new FileWriter("./data/pairwiseDist.pacsv"));
        } catch (IOException ex) {
            Logger.getLogger(PairwiseDistanceAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setParticleConfiguration(IParticleConfiguration particleConfiguration) {
        this.particleConfig = particleConfiguration;
    }

    public void analyse() {
        int typeIdToAnalyse = 0;

        Iterator<IParticle> iter = particleConfig.particleIterator();
        // collect relevant particles
        ArrayList<IParticle> relevantParticles = new ArrayList();
        while (iter.hasNext()) {
            IParticle p = iter.next();
            if (p.get_type() == typeIdToAnalyse) {
                relevantParticles.add(p);
            }
        }

        // compute radius of gyration

        double rootMeanSquaredPairwiseDistances = computeRMSPD(relevantParticles);
        System.out.println("RMSPD: " + rootMeanSquaredPairwiseDistances);
        try {
            out.write(rootMeanSquaredPairwiseDistances + ",");
            out.newLine();
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(PairwiseDistanceAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns the quadratic radius of gyration of the ensemble of specified particles
     * @param relevantParticles
     * @return
     */
    private double computeRMSPD(ArrayList<IParticle> relevantParticles) {
        // compute center of mass
        ArrayList<double[]> alreadyProcessedCoords = new ArrayList();

        double rmspd = 0;
        int counter = 0;
        for (IParticle p : relevantParticles) {
            for (double[] alreadyProcessedCoord : alreadyProcessedCoords) {
                double d = DoubleArrays.distance(p.get_coords(), alreadyProcessedCoord);
                rmspd += d * d;
                counter++;
            }
            alreadyProcessedCoords.add(p.get_coords());

        }
        return Math.sqrt(rmspd / counter);
    }

    public void setCurrentTimestep(int step) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSimulationTime(double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setup(int currentStep, IParticleConfiguration pc, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IDataReadyForOutput getOutputData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        //  throw new UnsupportedOperationException("Not supported yet.");
    }
}
