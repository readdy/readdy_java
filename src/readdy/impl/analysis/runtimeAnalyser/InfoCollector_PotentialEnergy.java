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

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 */
public class InfoCollector_PotentialEnergy implements IReaddyRuntimeAnalyser {

    IParticleConfiguration particleConfiguration;
    int currentTimestep;
    boolean settedUp = false;
    boolean analysed = false;
    IDataReadyForOutput dataReadyForOutput;
    HashMap<Integer, Integer> particleTypeId2ParticleCounts_map = new HashMap();
    HashMap<Integer, String> particleTypeId2ParticleName_map = new HashMap();
    ArrayList<Integer> particleTypeIds = new ArrayList();
    static boolean firstWritingToFile = true;
    ArrayList<ArrayList<String>> doc = new ArrayList();

    public InfoCollector_PotentialEnergy() {

        doc.clear();
        ArrayList<String> headersLine = new ArrayList();
        headersLine.add("HEADER");
        headersLine.add("timeStep");
        headersLine.add("E_pot");
        doc.add(headersLine);

    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        if (allInputAvailable()) {
            this.currentTimestep = currentStep;
            this.particleConfiguration = particleConfiguration;
            settedUp = true;
        } else {
            throw new RuntimeException("not all input available");
        }

    }

    public void analyse() {
        if (firstWritingToFile) {
            firstWritingToFile = false;
        } else {
            doc.clear();
        }


        if (settedUp) {

            // remark: the timestep is written automatically.
            ArrayList<String> line = new ArrayList();
            line.add(particleConfiguration.getSystemPotentialEnergy() + "");
            doc.add(line);
            dataReadyForOutput = new DataReadyForOutput(doc);


            analysed = true;
        } else {
            throw new RuntimeException("not setted up yet or not all input available");
        }
    }

    public IDataReadyForOutput getOutputData() {
        if (analysed) {
            return dataReadyForOutput;
        } else {
            throw new RuntimeException("not analysed yet");
        }

    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean allInputAvailable() {
        return true;
    }
}
