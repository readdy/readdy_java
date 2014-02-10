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
import java.util.Iterator;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionParameters;

/**
 *
 * @author schoeneberg
 */
public class InfoCollector_Reaction_tabulated implements IReaddyRuntimeAnalyser {

    IReactionParameters reactionParameters;
    ArrayList<IReactionExecutionReport> rkReports;
    int currentStep;
    boolean settedUp = false;
    boolean analysed = false;
    IDataReadyForOutput dataReadyForOutput;
    String[] availableModes = new String[]{"standard", "reactionEvents"};
    int currentMode;
    static HashMap<Integer, Integer> reactionId2EventCounts = new HashMap();
    static HashMap<Integer, String> reactionId2ReactionNameMap = new HashMap();
    static ArrayList<Integer> reactionIdList = new ArrayList();
    static boolean firstWritingToFile = true;
    ArrayList<ArrayList<String>> doc = new ArrayList();


    public InfoCollector_Reaction_tabulated(IReactionParameters reactionParameters) {
        // initialize the reactionEvent
        this.reactionParameters = reactionParameters;
        Iterator<IReaction> iterator = reactionParameters.reactionIterator();
        while (iterator.hasNext()) {
            IReaction reaction = iterator.next();
            int rkId = reaction.get_id();
            String rkName = reaction.get_name();
            reactionId2EventCounts.put(rkId, 0);
            reactionId2ReactionNameMap.put(rkId, rkName);
            reactionIdList.add(rkId);
        }

        doc.clear();
        ArrayList<String> headersLine = new ArrayList();
        headersLine.add("HEADER");
        headersLine.add("timeStep");
        doc.clear();
        ArrayList<String> commentIdLine = new ArrayList();
        commentIdLine.add("#");
        commentIdLine.add("rkIds");
        for (int i = 0; i < reactionIdList.size(); i++) {
            headersLine.add(reactionId2ReactionNameMap.get(reactionIdList.get(i)));
            commentIdLine.add(reactionIdList.get(i) + "");
        }

        doc.add(commentIdLine);
        doc.add(headersLine);
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        this.currentStep = currentStep;
        this.rkReports = rkReports;
        settedUp = true;

    }

    public void analyse() {
        if (firstWritingToFile) {
            firstWritingToFile = false;
        } else {
            doc.clear();
        }
        if (settedUp) {
            for (IReactionExecutionReport report : rkReports) {
                int reportedRkId = report.getExecutableReaction().get_rkId();
                int currentCounts = reactionId2EventCounts.get(reportedRkId);
                reactionId2EventCounts.put(reportedRkId, currentCounts + 1);
            }
            ArrayList<String> line = new ArrayList();
            for (int i = 0; i < reactionIdList.size(); i++) {
                line.add(reactionId2EventCounts.get(reactionIdList.get(i)) + "");
            }
            doc.add(line);
            dataReadyForOutput = new DataReadyForOutput(doc);
            analysed = true;
        } else {
            throw new RuntimeException("not setted up yet");
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
        //  throw new UnsupportedOperationException("Not supported yet.");
    }
}
