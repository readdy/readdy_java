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
package readdy.impl.analysis;

import java.util.ArrayList;
import readdy.api.analysis.IAnalysisAndOutputManager;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.io.out.IOutputWriter;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;

/**
 *
 * @author schoeneberg
 */
public class AnalysisAndOutputManager implements IAnalysisAndOutputManager {

    IGlobalParameters globalParameters;
    static int analysisUnitId = 0;
    ArrayList<String[]> specialFlagsList = new ArrayList();
    ArrayList<Boolean> resetReactionReportsFlagList = new ArrayList();
    ArrayList<String> analysisAndOutputUnitNameList = new ArrayList();
    ArrayList<Integer> everyXStepList = new ArrayList();
    ArrayList<IReaddyRuntimeAnalyser> frameAnalyserList = new ArrayList();
    ArrayList<IOutputWriter> outputWriterList = new ArrayList();
    ArrayList<Integer> posInListOfLastAnalysisRequestCall = new ArrayList();
    private boolean resetReactionReportsList = false;

    public boolean analysisRequested(int stepId) {
        boolean result = false;
        posInListOfLastAnalysisRequestCall.clear();

        for (int posInList = 0; posInList < everyXStepList.size(); posInList++) {
            int currentEveryXStep = everyXStepList.get(posInList);
            // step ids start with id 0. Therefore this construction.
            if (stepId % currentEveryXStep == (currentEveryXStep-1)) {
                posInListOfLastAnalysisRequestCall.add(posInList);
                result = true;
            }
        }
        return result;

    }

    public void analyseAndOutput(int stepId, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports) {
        resetReactionReportsList = false;
        
        

        for (int posInList : posInListOfLastAnalysisRequestCall) {
            String name = analysisAndOutputUnitNameList.get(posInList);
            System.out.println(stepId + " -> analysis by " + name + " ...");

            // is the reset flag set for this particular analysis method?
            boolean resetFlag = resetReactionReportsFlagList.get(posInList);
            //System.out.println("name: "+name+" resetFlag: "+resetFlag);
            if (resetFlag) {
                resetReactionReportsList = true;
                //System.out.println("reset flag enabled.");
            }

            IReaddyRuntimeAnalyser frameAnalyser = frameAnalyserList.get(posInList);
            String[] specialFlags = specialFlagsList.get(posInList);
            if ((!specialFlags[0].equals("singleAnalysisOnlyAtThatStep")) || (specialFlags[0].equals("singleAnalysisOnlyAtThatStep") && stepId == everyXStepList.get(posInList))) {
                IOutputWriter outputWriter = outputWriterList.get(posInList);
                frameAnalyser.setup(stepId, particleConfiguration, rkReports, specialFlags);
                frameAnalyser.analyse();
                IDataReadyForOutput outputData = frameAnalyser.getOutputData();
                outputWriter.write(stepId, outputData);
            }
        }
    }

    public boolean get_resetReactionReportsList() {
        return resetReactionReportsList;
    }

    public void registerAnalysisAndOutputUnit(String name, int everyXStep, IReaddyRuntimeAnalyser analyser, IOutputWriter outputWriter, String[] specialFlags) {
        analysisAndOutputUnitNameList.add(analysisUnitId, name);
        everyXStepList.add(analysisUnitId, everyXStep);
        frameAnalyserList.add(analysisUnitId, analyser);
        outputWriterList.add(analysisUnitId, outputWriter);
        specialFlagsList.add(analysisUnitId, specialFlags);
        for (String flag : specialFlags) {
            if (flag.contentEquals("RESET_REACTION_REPORTS")) {
                resetReactionReportsFlagList.add(analysisUnitId, true);
                System.out.println("reset reaction reports flag registered TRUE");
            } else {
                // this is enabled for an entire run
                resetReactionReportsFlagList.add(analysisUnitId, false);
            }
        }
        analysisUnitId++;
    }

    public void finishRuntimeAnalysis() {
        for (IOutputWriter writer : outputWriterList) {
            writer.close();
        }
    }
}
