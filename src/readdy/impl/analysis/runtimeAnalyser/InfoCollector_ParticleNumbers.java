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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.particle.IParticleParameters;

/**
 *
 * @author schoeneberg
 */
public class InfoCollector_ParticleNumbers implements IReaddyRuntimeAnalyser {

    IParticleParameters particleParameters;
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

    public InfoCollector_ParticleNumbers(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;

        HashSet<Integer> particleTypes = particleParameters.getAllParticleTypes();
        Iterator<Integer> iterator = particleTypes.iterator();
        while (iterator.hasNext()) {
            int particleTypeId = iterator.next();
            particleTypeIds.add(particleTypeId);

            particleTypeId2ParticleCounts_map.put(particleTypeId, 0);
            particleTypeId2ParticleName_map.put(particleTypeId, particleParameters.getParticleTypeNameFromTypeId(particleTypeId));
        }
        // ids come from the hash set in an unsorted fashion.
        Collections.sort(particleTypeIds);


        /*
         * the output of the following lines will look like this
         * in a system of 3 particle types: A,B and C
         *
        #ReaDDY by j schoeneberg. Run on Tue Oct 30 23:09:57 CET 2012
        #, typeIds, 0, 1, 2
        timeStep, A, B, C
        0, 100, 100, 0
        1000, 99, 99, 1
         */

        doc.clear();
        ArrayList<String> headersLine = new ArrayList();
        headersLine.add("HEADER"); // this is only to indicate that this is a header line, a flag so to say
        headersLine.add("timeStep");

        ArrayList<String> commentIdLine = new ArrayList();
        commentIdLine.add("#");
        commentIdLine.add("typeIds");
        for (int i = 0; i < particleTypeIds.size(); i++) {
            headersLine.add(particleTypeId2ParticleName_map.get(particleTypeIds.get(i)));
            commentIdLine.add(particleTypeIds.get(i) + "");
        }

        doc.add(commentIdLine);
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
        // clear the counts list
        for (int i = 0; i < particleTypeIds.size(); i++) {
            particleTypeId2ParticleCounts_map.put(i, 0);
        }

        if (settedUp) {
            // counting
            Iterator<IParticle> particleIterator = particleConfiguration.particleIterator();
            while (particleIterator.hasNext()) {
                int currentParticleType = particleIterator.next().get_type();
                particleTypeId2ParticleCounts_map.put(currentParticleType, particleTypeId2ParticleCounts_map.get(currentParticleType) + 1);
            }

            //output

            ArrayList<String> line = new ArrayList();
            for (int typeId : particleTypeIds) {
                line.add(particleTypeId2ParticleCounts_map.get(typeId) + "");
            }
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
        return particleParameters != null;
    }
}
