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
import readdy.api.analysis.IAnalysisAndOutputManager;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.ICore;
import readdy.api.sim.top.ITop;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.sim.core.rk.ReactionsOccurredExeption;
import readdy.impl.tools.ProcessorStopWatch;
import java.util.List;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
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
public class TopMM implements ITop {
    boolean verbose = false;
    IGlobalParameters globalParameters = null;
    ICore core = null;
    IGroupConfiguration groupConfiguration;
    IReactionHandler reactionHandler = null;
    IAnalysisAndOutputManager analysisAndOutputManager = null;
    ArrayList<IReactionExecutionReport> rkReports = new ArrayList();
    boolean computeTime = false;
    List<Float> reactions = new ArrayList();
    float[] cReactions;
    
    
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
    
    //public native void cCreateSimulation(float[] Pos);
    public native void cCreateSimulation(float[] Pos, boolean testmode, String tplgyDir, double nSteps, double stepSize, double stepsPerFrame, float[] cReactions);
    /** possible parameter:
        timestep
        time(step) per frame(if one) = per callback
        input file, or partcle positions, masses and evtl. else parameter
        particle types,
        Ds,
        whitch plugin,
        forces (pariwise, external etc...) +  force parameter (global, per particle, cutoff, box size)
        integrator
        evtl. output (file, what...)
    **/
    /// maybe easier to write a file as interface... (runtime overhead is okay, since it is called just once)

    ///private native void cSimulate(int stepsPerFrame, float[] newPos);
    /**possible parameter:
        time(step) per frame(if one)
    **/

    public boolean frameCallback(int step, float[] JPos){
        
            System.out.println("JAVA");
            //if(computeTime){stopWatch.measureTime(9, System.nanoTime());}
            
            //if(i%1000 ==0){System.out.println("\ntop: 'step " + i + "'");}
            System.out.println("top: 'step " + step + "'");
            
            //if(computeTime){stopWatch.measureTime(10, System.nanoTime());}
            
            reactions.clear();
            reactions.add(0.0f);
            
            try {
                //---------------------------------------------------------------
                // Advance particle dynamics
                //---------------------------------------------------------------

                //if(computeTime){stopWatch.measureTime(0, System.nanoTime());}
        
                ///core.step(i);
                try {
                    IParticleConfiguration particleConfig = this.core.get_ParticleConfiguration();

                    for(int i=0; i<JPos[0]; i++){
                        double[] newPos = new double[3];
                        /// ID
                        IParticle p = particleConfig.getParticle((int)JPos[1+i*5+4]) ;
                        //System.out.println("id: "+((int)JPos[1+i*5+4])+" Pos: "+);
                        /// index
                        p.setIndex((int)JPos[1+i*5+3]);
                        /// xyz coordinates Position
                        newPos[0]=JPos[1+i*5];
                        //System.out.println(JPos[1+i*5]);
                        newPos[1]=JPos[1+i*5+1];
                        //System.out.println(JPos[1+i*5+1]);
                        newPos[2]=JPos[1+i*5+2];
                        //System.out.println(JPos[1+i*5+2]);
                        //p.set_coords(newPos);   
                        ///TODO: updates the neighborlist!
                        particleConfig.setCoordinates((int)JPos[1+i*5+4], newPos);
                        /// one could avoid some computational efford by update(neigborlist) and set Positions just for the reactive particles...
                        newPos=null;
                    }
        
                    this.core.step(step);
                }
                catch(NullPointerException ex1){
                    System.out.println("NullPointerException ex1");                   
                }
                //if(computeTime){stopWatch.measureTime(1, System.nanoTime());}

                /// ?? unnötig an dieser Stelle. wird auskommentiert
                //this.core.get_ParticleConfiguration().updateNeighborListDistances();
                
                //if(computeTime){stopWatch.measureTime(2, System.nanoTime());}

                //if(computeTime){stopWatch.accumulateTime(0, 0, 1);}
                //if(computeTime){stopWatch.accumulateTime(1, 1, 2);}

            } catch (ReactionsOccurredExeption|NullPointerException ex) {
                //---------------------------------------------------------------
                // handle reactions
                //---------------------------------------------------------------
                if(ex instanceof NullPointerException){
                    System.out.println("NullPointerException!!!");
                    return false;
                }
                else{
                    System.out.println("Reaction!");

                    ArrayList<IReactionExecutionReport> newRkReports = reactionHandler.handleOccurredReactions(step,
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

                    // reactions for C/OpenMM
                    for(IReactionExecutionReport report:newRkReports){
                    ArrayList<IParticle> removedParticle=report.getRemovedParticles();
                    for(IParticle particle:removedParticle){
                        reactions.set(0, reactions.get(0)+1.0f);
                        reactions.add((float)particle.get_id()); /// particle number
                        reactions.add((float)-1); /// particle Type
                        reactions.add((float)0); /// particle Pos x
                        reactions.add((float)0); /// particle Pos y
                        reactions.add((float)0); /// particle Pos z
                        reactions.add((float)particle.getIndex()); /// Particle index in c
                    }
                    ArrayList<IParticle> createdParticle=report.getCreatedParticles();
                    for(IParticle particle:createdParticle){
                        reactions.set(0, reactions.get(0)+1.0f);
                        reactions.add((float)particle.get_id()); /// particle number
                        reactions.add((float)particle.get_type()); /// particle Type
                        reactions.add((float)particle.get_coords()[0]); /// particle Pos x
                        reactions.add((float)particle.get_coords()[1]); /// particle Pos y
                        reactions.add((float)particle.get_coords()[2]); /// particle Pos z
                        reactions.add((float)-1); /// Particle index in c
                    }
                    ArrayList<IParticle> changedParticle=report.getTypeChangedParticles();
                    for(IParticle particle:changedParticle){
                        reactions.set(0, reactions.get(0)+1.0f);
                        reactions.add((float)particle.get_id()); /// particle number
                        reactions.add((float)particle.get_type()); /// particle Type
                        reactions.add((float)particle.get_coords()[0]); /// particle Pos x
                        reactions.add((float)particle.get_coords()[1]); /// particle Pos y
                        reactions.add((float)particle.get_coords()[2]); /// particle Pos z
                        reactions.add((float)particle.getIndex()); /// Particle index in c
                    }
                    }
                }
            }

            
            
            //---------------------------------------------------------------
            // Analysis by runtime analysers
            //---------------------------------------------------------------

            //if(computeTime){stopWatch.measureTime(20, System.nanoTime());}
            //if (analysisAndOutputManager.analysisRequested(i)) {
            if (analysisAndOutputManager.analysisRequested(step)) {
                System.out.println("analysis...");
                analysisAndOutputManager.analyseAndOutput(step, core.get_ParticleConfiguration(),rkReports);
                if(analysisAndOutputManager.get_resetReactionReportsList()){
                    //System.out.println("clear reactionReport list");
                    rkReports.clear();
                }
            }
            
            //List<Float> list = new ArrayList<Float>();
            //float[] floatArray = ArrayUtils.toPrimitive(list.toArray(new Float[0]), 0.0F);
            //cReactions = reactions.toArray(new Float[reactions.size()]);
            //cReactions = ArrayUtils.toPrimitive(reactions.toArray(new Float[reactions.size()]));
            cReactions = new float[reactions.size()];
            for( int j =0; j<reactions.size(); j++){
                //cReactions[j] = reactions.get(j);
                Float f = reactions.get(j);
                cReactions[j] = (f != null ? f : Float.NaN);
            }
            
            /*
             * 
            reactions.set(0, reactions.get(0)+1.0f);
            reactions.add((float)newParticle.get_id()); /// particle number
            reactions.add((float)newParticle.get_type()); /// particle Type
            reactions.add((float)newParticle.get_coords()[0]); /// particle Pos x
            reactions.add((float)newParticle.get_coords()[1]); /// particle Pos y
            reactions.add((float)newParticle.get_coords()[2]); /// particle Pos z
            reactions.add((float)-1); /// Particle index in c
            * 
             * cReactions = new float[6];
            cReactions[0] = 1.0f; // # Reactions
            cReactions[1] = 0.0f; // Particle#
            cReactions[2] = 2.0f; // new Type
            cReactions[3] = 0.0f; // pos
            cReactions[4] = 0.0f;
            cReactions[5] = 0.0f;*/
            
            //if(computeTime){stopWatch.measureTime(30, System.nanoTime());}
            //if(computeTime){stopWatch.accumulateTime(3,10, 20);}
            //if(computeTime){stopWatch.accumulateTime(4,20, 30);}
            //if(computeTime){stopWatch.accumulateTime(5,9, 30);}
            return true;
    }
    
    public void runSimulation() {
        
        ProcessorStopWatch stopWatch = new ProcessorStopWatch();
        System.out.println("=================================================");
        System.out.println("=================================================");
        System.out.println("top: 'start simulation...'");
        long nSteps = globalParameters.get_nSimulationSteps();
        double dt = globalParameters.get_dt();
        System.out.println("analyse before first step...");
        analysisAndOutputManager.analyseAndOutput(-1, core.get_ParticleConfiguration(),rkReports);
        //if(computeTime){stopWatch.measureTime(3,System.nanoTime());}
        
        /*for (int i = 0; i < nSteps; i++) {
        }*/
        /*
         * List<T> list = new ArrayList<T>();
         * T [] countries = list.toArray(new T[list.size()]);
         */
        reactions.add(0.0f);
        //cReactions = reactions.toArray(new Float[reactions.size()]);
        cReactions = new float[reactions.size()];
        for( int i =0; i<reactions.size(); ++i){
            //cReactions[i] = reactions.get(i);
            Float f = reactions.get(i);
            cReactions[i] = (f != null ? f : Float.NaN);
        }
        
        /*IParticleConfiguration particleConfig = null;
        Iterator<IParticle> singleParticleIterator1 = particleConfig.particleIterator();
        while (singleParticleIterator1.hasNext()) {
            IParticle p = singleParticleIterator1.next();
            int i = p.get_id();
            p.setPos(i);
        }*/
        
        float[] Pos = new float[4716*3];
        System.out.println("run simulation...");   
        //this.cCreateSimulation(Pos, testmode, tplgy-coords_dir, simulationtime, StepSize, forces!?, );
        boolean testmode = true;
        /*TODO:
         * take coordinates-topology form global parameters
         * hand over timestep (integrationtime)!
         */
        //String tplgyDir = "/home/mi/biederj/programs/NetBeansProjects/readdy2/test/fullTestSimulation_diskVesicle/ReaDDy_input";
        //String tplgyDir = "/home/mi/biederj/programs/NetBeansProjects/readdy2/test/ReaDDy_Tutorial/ReaDDy_input";
        String tplgyDir = "/home/mi/biederj/programs/NetBeansProjects/readdy2/test/SyxTest/ReaDDy_input";
        //double simulationtime = 1000;
        //double OpenMMDT=1E-10; /// in sceonds
        double OpenMMDT=globalParameters.get_dtO();
        //double OpenMMDT=1E-10; /// in sceonds  /// TODO: parameter
        //double OpenMMDT=5E-9; /// in sceonds  /// TODO: parameter
        double stepSize = OpenMMDT/1E-12; /// in picoseconds
        double stepsPerFrame = dt/OpenMMDT;
        System.out.println("steps in ReaDDy: "+nSteps);
        System.out.println("reaction dt: "+dt);
        System.out.println("OpenMMdt:"+OpenMMDT);
        System.out.println("steps per Frame: "+stepsPerFrame);
        //double simulationtime = dt;
        
        this.cCreateSimulation(Pos, testmode, tplgyDir, nSteps, stepSize, stepsPerFrame, cReactions );
        
        //this.cCreateSimulation(Pos);
        
        System.out.println("back in JAVA: simulation finished");
        //System.out.println("BEEPu0007!");
        //java.awt.Toolkit.getDefaultToolkit().beep();
        //System.out.println((char)7);
        
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
    
    static {
        //System.load("/home/mi/biederj/programs/NetBeansProjects/readdy2/lib/libCtest.so");
        //System.out.println("loading OpenMM-library ");
        //System.loadLibrary("OpenMM");
        System.out.println("loading c-library ");
        System.loadLibrary("Ctest");
    }
    
}
