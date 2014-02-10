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
package readdy.impl.sim.top.rkHandle;

import java.util.ArrayList;
import java.util.HashSet;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;

/**
 * The reaction handler has 4 tasks.
 *
 * 1. match the occurred particle based reactions with top level reactions 2.
 * validate these top level reactions 3. check for conflicts between possible
 * reactions 4. hand the final list to the reaction executor who finally
 * executes the changes in the topology due to the reaction
 *
 * @author schoeneberg
 */
public class ReactionHandler implements IReactionHandler {

    IElmtlRkToRkMatcher elmtlRkToRkMatcher;
    IReactionValidator reactionValidator;
    IReactionConflictResolver reactionConflictResolver;
    IReactionManager reactionManager;
    final static boolean verbose = false;

    public void setElmtlRkToRkMatcher(IElmtlRkToRkMatcher elmtlRkToRkMatcher) {
        this.elmtlRkToRkMatcher = elmtlRkToRkMatcher;
    }

    public void setReactionConflictResolver(IReactionConflictResolver reactionConflictResolver) {
        this.reactionConflictResolver = reactionConflictResolver;
    }

    public void setReactionManager(IReactionManager reactionManager) {
        this.reactionManager = reactionManager;
    }

    public void setReactionValidator(IReactionValidator reactionValidator) {
        this.reactionValidator = reactionValidator;
    }

    public ArrayList<IReactionExecutionReport> handleOccurredReactions(int stepId, ArrayList<IOccurredElementalReaction> occElmtlRkList,
            IParticleConfiguration particleConfiguration, IGroupConfiguration groupConfiguration,
            IPotentialManager potentialManager) {

        // create for every occurredElementalReaction that comes from the core an individual report
        ArrayList<IReactionExecutionReport> reportList = new ArrayList();
        ArrayList<IExecutableReaction> executableReactionList = new ArrayList(occElmtlRkList.size());
        for (int i = 0; i < occElmtlRkList.size(); i++) {
            reportList.add(i, null);
            executableReactionList.add(i, null);
        }
        if(verbose){System.out.println("HANDLE: reportListSize " + reportList.size());}
        // set of integers that contains all executable reaction indices that are checked
        // for conflicts and everything.
        HashSet<Integer> finalExecutableReactionIndicesInList = new HashSet();

        // can contain an executableReaction of null if the reaction that was supposed
        // to be generated from the occElmtlRk Element failed the matching, validation or conflict resolving

        for (int i = 0; i < occElmtlRkList.size(); i++) {
            if(verbose){System.out.println("HANDLER: for loop iteration..." + i);}
            IOccurredElementalReaction occElmtlRk = occElmtlRkList.get(i);
            //----------------------------------------------------------------------
            // match the reaction
            //----------------------------------------------------------------------
            IExecutableReaction executableReaction = elmtlRkToRkMatcher.matchReaction(occElmtlRk);
            if (executableReaction != null) {

                //----------------------------------------------------------------------
                // validate the reaction
                //----------------------------------------------------------------------
                if (reactionValidator.validateReaction(executableReaction)) {


                    //----------------------------------------------------------------------
                    // check for conflicts
                    //----------------------------------------------------------------------
                    // if there are no other reactions to compare with, store the new reaction
                    // in the to be tested list
                    if (finalExecutableReactionIndicesInList.isEmpty()) {
                        // add the new executable reaction to the list
                        finalExecutableReactionIndicesInList.add(i);
                        executableReactionList.set(i, executableReaction);
                    } else {
                        // if there are other reactions against which we can test for conflicts, test
                        // against them
                        boolean conflictsFound = true; // intitialisation on true for the subsequent while loop                        
                        boolean conflictFoundAndForLoopBroken = false;
                        int indexToAdd = 0;
                        IExecutableReaction reactionToAdd = null;
                        int reactionToRemoveIndex = 0;
                        IExecutableReaction reactionToRemove = null;
                        do {
                            conflictsFound = false;
                            conflictFoundAndForLoopBroken = false;
                            for (int other_executableReactionIndex : finalExecutableReactionIndicesInList) {
                                if (other_executableReactionIndex != i) { // prevent from a reaction to be checked for conflict against itself.
                                    if(verbose){System.out.print(other_executableReactionIndex+",");}
                                    IExecutableReaction otherExecutableReaction = executableReactionList.get(other_executableReactionIndex);
                                    if (reactionConflictResolver.isThereAConflict(executableReaction, otherExecutableReaction)) {
                                        conflictsFound = true;
                                        // there is A conflict
                                        if(verbose){System.out.println("REACTION_HANDLER: there is a conflict");}
                                        // determine which of the conflict parties has to die
                                        if (reactionConflictResolver.isTheFirstOneWinningTheConflict(executableReaction, otherExecutableReaction)) {
                                            if(verbose){System.out.println("REACTION_HANDLER: new one winning");}
                                            // the new reaction has won the conflict and the other, established one lost
                                            // change the lists accordingly
                                            // the new reaction is added and the old one is removed
                                            // we have to break the for loop here to avoid concurrent modification while iterating the list
                                            indexToAdd = i;
                                            reactionToAdd = executableReaction;
                                            reactionToRemoveIndex = other_executableReactionIndex;
                                            reactionToRemove = otherExecutableReaction;
                                            conflictFoundAndForLoopBroken = true;
                                            // put the report at the place of the loser reaction, in this case the other one.
                                            IReactionExecutionReport report_newOneIsWinner = getReactionConflictedReport_loser(stepId, reactionToAdd, reactionToRemove);
                                            if(verbose){System.out.println("REACTION_HANDLER: new reaction won, reaction index to remove from list: " + reactionToRemoveIndex);}
                                            reportList.set(reactionToRemoveIndex, report_newOneIsWinner);
                                            break;

                                        } else {
                                            if(verbose){System.out.println("REACTION_HANDLER: other one winning");}
                                            // the old reaction has won the conflict and the new one lost
                                            // change the lists accordingly
                                            // the new reaction is removed and the old one stays in place
                                            indexToAdd = other_executableReactionIndex;
                                            reactionToAdd = otherExecutableReaction;
                                            reactionToRemoveIndex = i;
                                            reactionToRemove = executableReaction;
                                            conflictFoundAndForLoopBroken = true;
                                            // put the report at the place of the loser reaction, in this case the new one
                                            IReactionExecutionReport report_otherIsWinner = getReactionConflictedReport_loser(stepId, otherExecutableReaction, executableReaction);
                                            if(verbose){System.out.println("REACTION_HANDLER: other reaction won, reaction index to remove from list: " + i);}
                                            reportList.set(i, report_otherIsWinner);
                                            break;


                                            

                                        }
                                    }
                                }
                            }

                            // now outside of the for loop, if we had to break the loop for modifying the list, do that here now
                            // handle the list modifications here now.
                            if (conflictFoundAndForLoopBroken) {

                                finalExecutableReactionIndicesInList.add(indexToAdd);
                                executableReactionList.set(indexToAdd, reactionToAdd);

                                finalExecutableReactionIndicesInList.remove(reactionToRemoveIndex);
                                executableReactionList.set(reactionToRemoveIndex, null);
                                
                            }

                        } while (conflictFoundAndForLoopBroken);

                        if (conflictsFound == false) {
                            // there is NO conflict
                            if(verbose){System.out.println("REACTION_HANDLER: there is no conflict.");}
                            // add the new executable reaction to the list
                            finalExecutableReactionIndicesInList.add(i);
                            executableReactionList.set(i, executableReaction);
                        }

                    }

                } else {
                    // reaction is not valid
                    // Do a report.

                    // insert null into the executable reaction list for this entry
                    executableReactionList.set(i, null);

                    IReactionExecutionReport report_notValid = getReactionNotValidReport(stepId, executableReaction);
                    reportList.set(i, report_notValid);
                }



            } else {
                // reaction failed to match
                // DO a report.

                // insert null into the executable reaction list for this entry
                executableReactionList.set(i, null);

                IReactionExecutionReport report_noMatch = getReactionNotMatchedReport(stepId, executableReaction);
                reportList.set(i, report_noMatch);
            }


        }
        //==========================================================================================    
        // finally, after having determined all conflicts, validated everything and
        // matched everything, we can handle the remaining pretty list of executable
        // reaction

        for (int executableReactionIndex : finalExecutableReactionIndicesInList) {
            if(verbose){System.out.println("final executable Reaction index: " + executableReactionIndex);}
            IExecutableReaction rk = executableReactionList.get(executableReactionIndex);
            IReactionExecutor executor = reactionManager.getReactionExecutor(rk.get_rkTypeId());
            executor.setup(particleConfiguration, groupConfiguration, potentialManager);
            IReactionExecutionReport report = executor.executeReaction(stepId, rk);
            reportList.set(executableReactionIndex, report);
            //report.print();
        }
        //==========================================================================================        

        return reportList;

    }

    private IReactionExecutionReport getReactionConflictedReport_loser(int stepId, IExecutableReaction winnerReaction, IExecutableReaction loserReaction) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, loserReaction);
        report.setExecutionWasSuccessfull(false);
        report.setExecutionComment("Reaction was in conflict with other Reaction.");
        report.setConflictReaction(winnerReaction);
        return report;
    }

    private IReactionExecutionReport getReactionNotValidReport(int stepId, IExecutableReaction executableReaction) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, executableReaction);
        report.setExecutionWasSuccessfull(false);
        report.setExecutionComment("Reaction was not valid.");
        return report;
    }

    private IReactionExecutionReport getReactionNotMatchedReport(int stepId, IExecutableReaction executableReaction) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, executableReaction);
        report.setExecutionWasSuccessfull(false);
        report.setExecutionComment("Reaction could not be matched.");
        return report;
    }
}
