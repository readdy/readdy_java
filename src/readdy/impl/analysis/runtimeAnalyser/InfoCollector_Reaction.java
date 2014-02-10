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
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionParameters;

/**
 *
 * @author schoeneberg
 */
public class InfoCollector_Reaction implements IReaddyRuntimeAnalyser {

    static long[] cumulatedRkEventCounter;
    static HashMap<Integer, String> reactionId2ReactionNameMap = new HashMap();
    IReactionParameters reactionParameters;
    ArrayList<IReactionExecutionReport> rkReports;
    int currentStep;
    boolean settedUp = false;
    boolean analysed = false;
    IDataReadyForOutput dataReadyForOutput;
    String[] availableModes = new String[]{"standard", "reactionEvents", "short", "cumulative", "spatialUnimolecularReactionEval"};
    int currentMode;
    ArrayList<ArrayList<String>> doc = new ArrayList();

    private int getModeId(String mode) {
        for (int i = 0; i < availableModes.length; i++) {
            String availableMode = availableModes[i];
            if (mode.contentEquals(availableMode)) {
                return i;
            }
        }
        return -1;
    }

    public InfoCollector_Reaction(String mode, IReactionParameters reactionParameters) {
        this.reactionParameters = reactionParameters;



        // mode selection
        int modeId;
        if ((modeId = getModeId(mode)) != -1) {
            currentMode = modeId;
        } else {
            throw new RuntimeException("the analysis mode " + mode + " doesnt exist");
        }


        // for cumulative reaction handling
        if (currentMode == 3) {

            Iterator<IReaction> iterator = reactionParameters.reactionIterator();
            int counter = 0;

            // collect the names
            while (iterator.hasNext()) {
                IReaction reaction = iterator.next();
                int rkId = reaction.get_id();
                String rkName = reaction.get_name();
                reactionId2ReactionNameMap.put(rkId, rkName);


                counter++;
            }

            // set the cumulative reaction counter
            cumulatedRkEventCounter = new long[counter];

            doc.clear();
            ArrayList<String> headersLine = new ArrayList();
            headersLine.add("HEADER");
            headersLine.add("timeStep");

            ArrayList<String> commentIdLine = new ArrayList();
            commentIdLine.add("#");
            commentIdLine.add("rkIds");
            for (int i = 0; i < cumulatedRkEventCounter.length; i++) {
                headersLine.add(reactionId2ReactionNameMap.get(i));
                commentIdLine.add(i + "");
            }

            doc.add(commentIdLine);
            doc.add(headersLine);

        }
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        this.currentStep = currentStep;
        this.rkReports = rkReports;
        settedUp = true;

    }

    public void analyse() {
        switch (currentMode) {
            case 0:
                analyse_standard();
                break;
            case 1:
                analyse_reactionEvents();
                break;
            case 2:
                analyse_short();
                break;
            case 3:
                analyse_cumulative();
                break;
            case 4:
                analyse_spatialUnimolecular();
                break;
            default:
                throw new RuntimeException("this should never happen");
        }

    }

    public void analyse_standard() {
        doc.clear();
        if (settedUp) {
            ArrayList<String> line = new ArrayList();
            line.add("##------------------------------------------------");
            doc.add(line);
            for (IReactionExecutionReport report : rkReports) {
                line = new ArrayList();
                line.add("#0--- --- reaction --- ---");
                doc.add(line);
                line = new ArrayList();
                line.add("#stepIdOfExecution,rkId,rkTypeId,derivdFromElmtlRkId");
                doc.add(line);
                line = new ArrayList();
                line.add(report.get_stepIdOfExecution() + "");
                line.add(report.getExecutableReaction().get_rkId() + "");
                line.add(report.getExecutableReaction().get_rkTypeId() + "");
                line.add(report.getExecutableReaction().get_derivedFromElmtlRkId() + "");
                doc.add(line);
                line = new ArrayList();
                line.add("#1--- particle based ---");
                doc.add(line);
                line = new ArrayList();
                line.add("#2removedParticles:");
                doc.add(line);
                for (IParticle p : report.getRemovedParticles()) {
                    doc.add(getParticleLine(p));
                }
                line = new ArrayList();
                line.add("#3createdParticles:");
                doc.add(line);
                for (IParticle p : report.getCreatedParticles()) {
                    doc.add(getParticleLine(p));
                }

                line = new ArrayList();
                line.add("#4typeChangedParticles:");
                doc.add(line);
                for (int i = 0; i < report.getTypeChangedParticles().size(); i++) {
                    IParticle p = report.getTypeChangedParticles().get(i);
                    int from = report.getTypeChangeParticles_from().get(i);
                    int to = report.getTypeChangeParticles_to().get(i);
                    line = getParticleLine(p);
                    line.add(from + "");
                    line.add(to + "");
                }

                // ------------------------------------------------------------
                line = new ArrayList();
                line.add("#5--- group based ---");
                doc.add(line);
                line = new ArrayList();
                line.add("#6removedParticles:");
                doc.add(line);
                for (IGroup g : report.getRemovedGroups()) {
                    doc.add(getGroupLine(g));
                }
                line = new ArrayList();
                line.add("#7createdParticles:");
                doc.add(line);
                for (IGroup g : report.getCreatedGroups()) {
                    doc.add(getGroupLine(g));
                }

                line = new ArrayList();
                line.add("#8typeChangedParticles:");
                doc.add(line);
                for (int i = 0; i < report.getTypeChangedGroups().size(); i++) {
                    IGroup g = report.getTypeChangedGroups().get(i);
                    int from = report.getTypeChangeGroups_from().get(i);
                    int to = report.getTypeChangeGroups_to().get(i);
                    line = getGroupLine(g);
                    line.add(from + "");
                    line.add(to + "");
                }
            }

            dataReadyForOutput = new DataReadyForOutput(doc);
            analysed = true;

        } else {
            throw new RuntimeException("not setted up yet");
        }
    }

    public void analyse_reactionEvents() {
        doc.clear();
        if (settedUp) {
            for (IReactionExecutionReport report : rkReports) {
                ArrayList<String> line = new ArrayList();
                line.add(report.get_stepIdOfExecution() + "");
                line.add(report.getExecutableReaction().get_rkId() + "");
                doc.add(line);
            }

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

    private ArrayList<String> getParticleLine(IParticle p) {
        ArrayList<String> line = new ArrayList();
        line.add(p.get_id() + "");
        line.add(p.get_type() + "");
        double[] c = p.get_coords();
        for (double d : c) {
            line.add(d + "");
        }
        return line;
    }

    private ArrayList<String> getGroupLine(IGroup g) {
        ArrayList<String> line = new ArrayList();
        line.add(g.get_id() + "");
        line.add(g.get_typeId() + "");
        line.add("memberParticleIds");
        for (IParticle p : g.get_positionedMemberParticles()) {
            line.add(p.get_id() + "");
        }
        return line;
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    private void analyse_short() {
        doc.clear();
        if (settedUp) {
            ArrayList<String> line;
            for (IReactionExecutionReport report : rkReports) {
                if(report.getExecutionWasSuccessfull()){
                line = new ArrayList();
                line.add(report.get_stepIdOfExecution() + "");
                line.add(report.getExecutableReaction().get_rkId() + "");
                line.add(report.getExecutableReaction().get_rkTypeId() + "");

                doc.add(line);
                }
            }

            dataReadyForOutput = new DataReadyForOutput(doc);
            analysed = true;

        } else {
            throw new RuntimeException("not setted up yet");
        }
    }

    private void analyse_cumulative() {
        if (currentStep != 0) {
            doc.clear();
        };
        if (settedUp) {
            ArrayList<String> line = new ArrayList();
            for (IReactionExecutionReport report : rkReports) {
                if(report.getExecutionWasSuccessfull()){
                // count the number of reactions that occurred of that particular id
                int rkId = report.getExecutableReaction().get_rkId();
                cumulatedRkEventCounter[rkId]++;
                }

            }

            for (int rkID = 0; rkID < cumulatedRkEventCounter.length; rkID++) {
                long counts = cumulatedRkEventCounter[rkID];
                line.add(counts + "");
            }

            doc.add(line);

            dataReadyForOutput = new DataReadyForOutput(doc);
            analysed = true;

        } else {
            throw new RuntimeException("not setted up yet");
        }

    }

    /**
     * these spatial unimolecular reactions have the following problem: They may
     * be triggered all the time but, dependent on the current coordinate of the
     * particle, the reacitons may be successfull or not. For this reason, the
     * reports have to be evaluated in a way, that only the successfull,
     * spatially triggered reactions find their way into the reports.
     */
    private void analyse_spatialUnimolecular() {
        ArrayList<String> line;
        for (IReactionExecutionReport report : rkReports) {

            line = new ArrayList();
            line.add(report.get_stepIdOfExecution() + "");
            line.add(report.getExecutableReaction().get_rkId() + "");
            line.add(report.getExecutableReaction().get_rkTypeId() + "");

            doc.add(line);

        }


        doc.clear();
        if (settedUp) {
            for (IReactionExecutionReport report : rkReports) {
                if(report.getExecutionWasSuccessfull()){
                if (report.getTypeChangedParticles().size() != 0) {
                    line = new ArrayList();
                    line.add(report.get_stepIdOfExecution() + "");
                    line.add(report.getExecutableReaction().get_rkId() + "");
                    line.add(report.getExecutableReaction().get_rkTypeId() + "");
                    int from = report.getTypeChangeParticles_from().get(0);
                    int to = report.getTypeChangeParticles_to().get(0);
                    line.add(from + "");
                    line.add(to + "");

                    doc.add(line);
                }
                }

            }

            dataReadyForOutput = new DataReadyForOutput(doc);
            analysed = true;

        } else {
            throw new RuntimeException("not setted up yet");
        }
    }
}
