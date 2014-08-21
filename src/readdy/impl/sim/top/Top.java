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
package readdy.impl.sim.top;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import readdy.api.analysis.IAnalysisAndOutputManager;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.ICore;
import readdy.api.sim.top.ITop;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.sim.core.rk.ReactionsOccurredExeption;
import readdy.impl.tools.ProcessorStopWatch;

/**
 * This is the top layer around the particle based simulation core.
 * It's three purposes are
 * 
 * 1) to contain the main iteration loop and perform the whole simulation
 * by triggering the Core.step() method which advances the dynamics of the
 * system by one timestep.
 *
 * 2) to catch the reaction exceptions that are thrown by the
 * Core during a timestep. If it catches such exceptions, the top hands these
 * exceptions, together with the information contained within them, to
 * the reaction handler who handles the reactions and changes the particle
 * configuration accordingly.
 *
 * 3) to trigger a runtime analysis when it is necessary.
 * 
 * @author Schoeneberg
 */
public class Top implements ITop {
    boolean verbose = false;
    IGlobalParameters globalParameters = null;
    ICore core = null;
    IGroupConfiguration groupConfiguration;
    IReactionHandler reactionHandler = null;
    IAnalysisAndOutputManager analysisAndOutputManager = null;
    ArrayList<IReactionExecutionReport> rkReports = new ArrayList();
    boolean computeTime = false;
    
    
    public void set_GlobalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public void set_Core(ICore core) {
        this.core = core;
    }

    public void set_ReactionHandler(IReactionHandler reactionHandler) {
        this.reactionHandler = reactionHandler;
    }

    public void set_AnalysisManager(IAnalysisAndOutputManager analysisManager) {
        this.analysisAndOutputManager = analysisManager;
    }

    public void runSimulation() {
        
        ProcessorStopWatch stopWatch = new ProcessorStopWatch();
        System.out.println("=================================================");
        System.out.println("=================================================");
        System.out.println("top: 'start simulation...'");
        long nSteps = globalParameters.get_nSimulationSteps();
        System.out.println("analyse before first step...");
        analysisAndOutputManager.analyseAndOutput(-1, core.get_ParticleConfiguration(),rkReports);
        //if(computeTime){stopWatch.measureTime(3,System.nanoTime());}
        
        for (int i = 0; i < nSteps; i++) {

            //if(computeTime){stopWatch.measureTime(9, System.nanoTime());}
            
            if(i%1000 ==0){System.out.println("\ntop: 'step " + i + "'");}
            
            //if(computeTime){stopWatch.measureTime(10, System.nanoTime());}
            
            try {
                //---------------------------------------------------------------
                // Advance particle dynamics
                //---------------------------------------------------------------

                //if(computeTime){stopWatch.measureTime(0, System.nanoTime());}

                core.step(i);
                //if(computeTime){stopWatch.measureTime(1, System.nanoTime());}

                //core.get_ParticleConfiguration().updateNeighborListDistances();
                //if(computeTime){stopWatch.measureTime(2, System.nanoTime());}

                //if(computeTime){stopWatch.accumulateTime(0, 0, 1);}
                //if(computeTime){stopWatch.accumulateTime(1, 1, 2);}

            } catch (ReactionsOccurredExeption ex) {
                //---------------------------------------------------------------
                // handle reactions
                //---------------------------------------------------------------
                
                ArrayList<IReactionExecutionReport> newRkReports = reactionHandler.handleOccurredReactions(i,
                        core.get_OccurredElementalReactions(),
                        core.get_ParticleConfiguration(),
                        groupConfiguration,
                        core.get_PotentialManager());

                if(verbose){
                for(IReactionExecutionReport report:newRkReports){
                    report.print();
                }
                }

                rkReports.addAll(newRkReports);
                
               
            }
            

            //---------------------------------------------------------------
            // Analysis by runtime analysers
            //---------------------------------------------------------------

            //if(computeTime){stopWatch.measureTime(20, System.nanoTime());}
            if (analysisAndOutputManager.analysisRequested(i)) {
                System.out.println("analysis...");
                analysisAndOutputManager.analyseAndOutput(i, core.get_ParticleConfiguration(),rkReports);
                if(analysisAndOutputManager.get_resetReactionReportsList()){
                    //System.out.println("clear reactionReport list");
                    rkReports.clear();
                }
            }
            //if(computeTime){stopWatch.measureTime(30, System.nanoTime());}
            //if(computeTime){stopWatch.accumulateTime(3,10, 20);}
            //if(computeTime){stopWatch.accumulateTime(4,20, 30);}
            //if(computeTime){stopWatch.accumulateTime(5,9, 30);}
        }
        analysisAndOutputManager.finishRuntimeAnalysis();
        //if(computeTime){stopWatch.measureTime(4,System.nanoTime());}
        //if(computeTime){stopWatch.accumulateTime(2,3, 4);}
        
        //if(computeTime){System.out.println("stepTime 0-1 "+stopWatch.getAverageTime(0));}
        //if(computeTime){System.out.println("latticeRecomputation 1-2 "+stopWatch.getAverageTime(1));}
        //if(computeTime){System.out.println("totalTime 3-4 "+stopWatch.getAverageTime(2));}

        //if(computeTime){System.out.println("first 10-20 "+stopWatch.getAverageTime(3));}
        //if(computeTime){System.out.println("second 20-30 "+stopWatch.getAverageTime(4));}
        //if(computeTime){System.out.println("action 9-30 "+stopWatch.getAverageTime(5));}



        
        // stop watch from the Core
        /*
        if(core.getStopWatch()!= null){
            ProcessorStopWatch coreStopWatch = core.getStopWatch();
            System.out.println("coreStopWatch getSINGLEParticleIterator    \t"+coreStopWatch.getAverageTime(0));
            System.out.println("coreStopWatch do SINGLE particle stuff     \t"+coreStopWatch.getAverageTime(1));
            System.out.println("coreStopWatch getPAIRParticleIterator      \t"+coreStopWatch.getAverageTime(2));
            System.out.println("coreStopWatch do PAIR particle stuff       \t"+coreStopWatch.getAverageTime(3));
            System.out.println("coreStopWatch apply cumulated displacement \t"+coreStopWatch.getAverageTime(4));
            System.out.println("coreStopWatch return an alert for reaction \t"+coreStopWatch.getAverageTime(5));
            System.out.println("coreStopWatch total time                   \t"+coreStopWatch.getAverageTime(6));

            int[]stopWatchIds = new int[]{10,11,12,13,20,21,22,23,24,25,26};
            for (int i = 0; i < stopWatchIds.length; i++) {
                int stopWatchId = stopWatchIds[i];
                System.out.println(coreStopWatch.getAccumulatorName(stopWatchId)+" "+coreStopWatch.getAverageTime(stopWatchId)+"\t"+coreStopWatch.getNAccumulations(stopWatchId));
            }


        }
         * 
         */
        /*
        if(core.getDiffusionEngineStopWatch()!=null){
            ProcessorStopWatch diffusionEngineStopWatch = core.getDiffusionEngineStopWatch();
            int[]stopWatchIds = new int[]{10,11,12,13,14,15,16,0,1,2,3};
            for (int i = 0; i < stopWatchIds.length; i++) {
                int stopWatchId = stopWatchIds[i];
                System.out.println(diffusionEngineStopWatch.getAccumulatorName(stopWatchId)+" "+diffusionEngineStopWatch.getAverageTime(stopWatchId)+"\t"+diffusionEngineStopWatch.getNAccumulations(stopWatchId));
            }
        }
        */
    }
}
