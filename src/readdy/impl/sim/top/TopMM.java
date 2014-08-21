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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import java.util.Map;
import java.util.Set;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.particle.IParticleParametersEntry;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.pot.potentials.IPotential;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupParameters;

/**
 * edited by Johann Biedermann. Particle dynamic is calculated in OpenMM. 
 * Analysis and Reactions are still done in ReaDDy
 * 
 * 1) calling a new OpenMM simulation, transfer all necessary parameter
 *
 * 2) after each defined frame OpenMM interface calls the "callback" method. 
 * it calles the part of the Core, which handles reactions, and the analysis part
 * to catch the reaction exceptions that are thrown by the Core during a
 * timestep. If it catches such exceptions, the top hands these exceptions,
 * together with the information contained within them, to the reaction handler
 * who handles the reactions and changes the particle configuration accordingly.
 *
 *
 * @author Schoeneberg, Biedermann
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
    IParticleParameters particleParameters;
    IPotentialInventory potentialInventory;
    IPotentialManager potentialManager;
    IGroupParameters groupParameters;
    String tplg_grp;
    String tplg_crd;

    /* methods to obtain simulation parameter, which are necessary for the 
     * dynamic simulation. Later handed over to OpenMM
     */
    public void setParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void setPotentialManager(IPotentialManager potentialManager) {
        this.potentialManager = potentialManager;
    }

    public void setPotentialInventory(IPotentialInventory potentialInventory) {
        this.potentialInventory = potentialInventory;
    }

    public void setGroupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }

    public void setGroupConfiguration(IGroupConfiguration groupConfiguration) {
        this.groupConfiguration = groupConfiguration;
    }

    public void setPathTplgyGrp(String tpgl_grp) {
        this.tplg_grp = tpgl_grp;
    }

    public void setPathTplgyCrd(String tplg_crd) {
        this.tplg_crd = tplg_crd;
    }

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

    /* this native function is defined in the C++-library, which contains the 
     * OpenMM-interface. This function creates a new OpenMM simulation, runs it,
     * and calls back the Java-method for reactions and analysis.
     */
    public native void cCreateSimulation(boolean testmode, String tplgyDir, String grpDir, int cuda, double nSteps, double stepSize, double stepsPerFrame, double kB, double T, double[] periodicBoundaries, int nTypes, double[] diffusion, double[] collisionRadii, double[] paramPot1, double[] paramPot2, float[] cReactions, int groupforce, int[] numberOfDummyParticles);

    /*
     * this method is called from native C++-library. It handles the reactions
     * and the analysis part. Its input is an array with the new (in OpenMM 
     * calculated) positions. Output is an array with all occured reactions.
     */
    public boolean frameCallback(int step, float[] JPos) {

        System.out.println("JAVA, step: "+ step);
        //if(computeTime){stopWatch.measureTime(9, System.nanoTime());}

        //if(i%1000 ==0){System.out.println("\ntop: 'step " + i + "'");}
        //System.out.println("top: 'step " + step + "'");

        //if(computeTime){stopWatch.measureTime(10, System.nanoTime());}

        reactions.clear();
        reactions.add(0.0f);

        try {
            //---------------------------------------------------------------
            // Advance particle dynamics
            // old: call core method
            // new: dynamics are handled in OpenMM
            // core now just handles reactions
            // later check whether analysis is requested
            //---------------------------------------------------------------

            //if(computeTime){stopWatch.measureTime(0, System.nanoTime());}

            ///core.step(i);
            IParticleConfiguration particleConfig = this.core.get_ParticleConfiguration();

            // the amount of particles should not change whithin the OpenMM-step
            if (particleConfig.getNParticles() != JPos[0]) {
                return (false);
            }
            // apply new positions for all particles
            for (int i = 0; i < JPos[0]; i++) {
                double[] newPos = new double[3];
                // magic numbers:
                // the first entry in the whole array is its length (therefore 1+ i...
                // there are 5 entries per particle               
                /// ReaDDy-ID
                IParticle p = particleConfig.getParticle((int) JPos[1 + i * 5 + 4]);
                //System.out.println("id: "+((int)JPos[1+i*5+4])+" Pos: "+);
                /// OpenMM-ID (index)
                p.setIndex((int) JPos[1 + i * 5 + 3]);
                /// xyz coordinates Position
                newPos[0] = JPos[1 + i * 5];
                //System.out.println(JPos[1+i*5]);
                newPos[1] = JPos[1 + i * 5 + 1];
                //System.out.println(JPos[1+i*5+1]);
                newPos[2] = JPos[1 + i * 5 + 2];
                //System.out.println(JPos[1+i*5+2]);
                //p.set_coords(newPos);   
                ///TODO: updates the neighborlist!
                particleConfig.setCoordinates((int) JPos[1 + i * 5 + 4], newPos);
                /// one could avoid some computational efford by update(neigborlist) and set Positions just for the reactive particles...
                newPos = null;
            }

            // ReaDDyMM-core just handles reactions
            this.core.step(step);
            //if(computeTime){stopWatch.measureTime(1, System.nanoTime());}

            //this.core.get_ParticleConfiguration().updateNeighborListDistances();

            //if(computeTime){stopWatch.measureTime(2, System.nanoTime());}

            //if(computeTime){stopWatch.accumulateTime(0, 0, 1);}
            //if(computeTime){stopWatch.accumulateTime(1, 1, 2);}

            //} catch (ReactionsOccurredExeption | NullPointerException ex) {
        } catch (Exception ex) {
            //---------------------------------------------------------------
            // handle reactions
            //---------------------------------------------------------------
            if (ex instanceof NullPointerException) {
                System.out.println("NullPointerException!!!");
                return false;
            } else if (ex instanceof ReactionsOccurredExeption) {
                System.out.println("Reaction!");
                ArrayList<IReactionExecutionReport> newRkReports;

                newRkReports = reactionHandler.handleOccurredReactions(step,
                        core.get_OccurredElementalReactions(),
                        core.get_ParticleConfiguration(),
                        groupConfiguration,
                        core.get_PotentialManager());

                if (verbose) {
                    for (IReactionExecutionReport report : newRkReports) {
                        report.print();
                    }
                }

                rkReports.addAll(newRkReports);


                // document reactions for C/OpenMM
                for (IReactionExecutionReport report : newRkReports) {
                    ArrayList<IParticle> removedParticle = report.getRemovedParticles();
                    for (IParticle particle : removedParticle) {
                        reactions.set(0, reactions.get(0) + 1.0f);
                        reactions.add((float) particle.get_id()); /// particle number
                        reactions.add((float) -1); /// particle Type
                        reactions.add((float) 0); /// particle Pos x
                        reactions.add((float) 0); /// particle Pos y
                        reactions.add((float) 0); /// particle Pos z
                        reactions.add((float) particle.getIndex()); /// Particle index in c
                    }
                    ArrayList<IParticle> createdParticle = report.getCreatedParticles();
                    for (IParticle particle : createdParticle) {
                        reactions.set(0, reactions.get(0) + 1.0f);
                        reactions.add((float) particle.get_id()); /// particle number
                        reactions.add((float) particle.get_type()); /// particle Type
                        reactions.add((float) particle.get_coords()[0]); /// particle Pos x
                        reactions.add((float) particle.get_coords()[1]); /// particle Pos y
                        reactions.add((float) particle.get_coords()[2]); /// particle Pos z
                        reactions.add((float) -1); /// Particle index in c
                    }
                    ArrayList<IParticle> changedParticle = report.getTypeChangedParticles();
                    for (IParticle particle : changedParticle) {
                        reactions.set(0, reactions.get(0) + 1.0f);
                        reactions.add((float) particle.get_id()); /// particle number
                        reactions.add((float) particle.get_type()); /// particle Type
                        reactions.add((float) particle.get_coords()[0]); /// particle Pos x
                        reactions.add((float) particle.get_coords()[1]); /// particle Pos y
                        reactions.add((float) particle.get_coords()[2]); /// particle Pos z
                        reactions.add((float) particle.getIndex()); /// Particle index in c
                    }
                }
            }
        }


        //System.err.println("analysis?");

        //---------------------------------------------------------------
        // Analysis by runtime analysers
        //---------------------------------------------------------------

        //if(computeTime){stopWatch.measureTime(20, System.nanoTime());}
        
        if (analysisAndOutputManager.analysisRequested(-1)) {
            if (analysisAndOutputManager.analysisRequested(step)) {
                System.out.println("analysis... step" + step);
                analysisAndOutputManager.analyseAndOutput(step, core.get_ParticleConfiguration(), rkReports);
                if (analysisAndOutputManager.get_resetReactionReportsList()) {
                    //System.out.println("clear reactionReport list");
                    rkReports.clear();
                }
            }
        }
        else{
            if (analysisAndOutputManager.analysisRequested(step - 1)) {
                System.out.println("analysis... step" + step);
                analysisAndOutputManager.analyseAndOutput(step-1, core.get_ParticleConfiguration(), rkReports);
                if (analysisAndOutputManager.get_resetReactionReportsList()) {
                    //System.out.println("clear reactionReport list");
                    rkReports.clear();
                }
            }
        }

        // since Java does not support primitives in its objects, we have
        // to convert our List<Float> to float[] manually
        cReactions = new float[reactions.size()];
        for (int j = 0; j < reactions.size(); j++) {
            //cReactions[j] = reactions.get(j);
            Float f = reactions.get(j);
            cReactions[j] = (f != null ? f : Float.NaN);
        }


        //if(computeTime){stopWatch.measureTime(30, System.nanoTime());}
        //if(computeTime){stopWatch.accumulateTime(3,10, 20);}
        //if(computeTime){stopWatch.accumulateTime(4,20, 30);}
        //if(computeTime){stopWatch.accumulateTime(5,9, 30);}
        return true;
    }

    /*
     * this method collects important parameter for the dynamic simulation
     * the are handed over the the OpenMM interface in the native C++-library
     * there a new OpenMM simulation is created and runned.
     * after each defined frame the java reaction and anaylsis method is called
     * back
     */
    public void runSimulation() {

        ProcessorStopWatch stopWatch = new ProcessorStopWatch();
        System.out.println("=================================================");
        System.out.println("=================================================");
        System.out.println("top: 'start simulation...'");
        long nSteps = globalParameters.get_nSimulationSteps();
        double dt = globalParameters.get_dt();
        System.out.println("analyse before first step...");
        /// analysisAndOutputManager.analyseAndOutput(0, core.get_ParticleConfiguration(), rkReports);
        //if(computeTime){stopWatch.measureTime(3,System.nanoTime());}

        /*for (int i = 0; i < nSteps; i++) 
         * previously this was the loop over the simulation steps
         */
    
        // initialize reactions-array
        reactions.add(0.0f);
        cReactions = new float[reactions.size()];
        for (int i = 0; i < reactions.size(); ++i) {
            Float f = reactions.get(i);
            cReactions[i] = (f != null ? f : Float.NaN);
        }

        System.out.println("run simulation...");

        // defines the amout of output in C++
        //boolean testmode = true;
        boolean testmode = false;
        // obtain various simulations parameter
        // timestep and framesize in OpenMM: 
        double OpenMMDT = globalParameters.get_dtOpenMM();
        double stepSize = OpenMMDT / 1E-12; /// in picoseconds
        double stepsPerFrame = dt / OpenMMDT;
        // number of cuda device to use
        int cudaDevNr = globalParameters.get_cudaDeviceIndex();
        // Boltzmann constant and temperature
        double kB = globalParameters.get_Kb();
        double T = globalParameters.get_T();
        // size of periodic boundary box
        double[][] periodicBoundariesReaDDy = globalParameters.get_latticeBounds();
        double[] periodicBoundaries = new double[3];
        periodicBoundaries[0] = periodicBoundariesReaDDy[0][0] - periodicBoundariesReaDDy[0][1];
        periodicBoundaries[1] = periodicBoundariesReaDDy[1][0] - periodicBoundariesReaDDy[1][1];
        periodicBoundaries[2] = periodicBoundariesReaDDy[2][0] - periodicBoundariesReaDDy[2][1];
        if (testmode) {
            System.out.println("steps in ReaDDy: " + nSteps);
            System.out.println("reaction dt: " + dt);
            System.out.println("OpenMMdt:" + OpenMMDT);
            System.out.println("steps per Frame: " + stepsPerFrame);
        }
        /// particle parameter ( diffusion constant and collision radii )
        HashSet<Integer> particleTypes = particleParameters.getAllParticleTypes();
        double[] diffusion = new double[particleTypes.size()];
        int[] numberOfDummyParticles = new int[particleTypes.size()];
        Iterator<Integer> allTypes = particleTypes.iterator();
        while (allTypes.hasNext()) {
            int type = allTypes.next();
            IParticleParametersEntry particleParametersEntry = particleParameters.getParticleParametersEntry(type);
            diffusion[type] = particleParametersEntry.get_D();
            numberOfDummyParticles[type] = particleParametersEntry.get_NumberOfDummyParticles();            
        }
        double[][] collisionRadiusByTypeMatrix_dMatrix = particleParameters.getCollisionRadiusByTypeMatrix_dMatrix();
        double[] collisionRadii = new double[collisionRadiusByTypeMatrix_dMatrix.length * collisionRadiusByTypeMatrix_dMatrix[0].length];
        for (int c = 0; c < collisionRadiusByTypeMatrix_dMatrix.length; c++) {
            for (int c2 = 0; c2 < collisionRadiusByTypeMatrix_dMatrix[c].length; c2++) {
                collisionRadii[c * collisionRadiusByTypeMatrix_dMatrix[0].length + c2] = collisionRadiusByTypeMatrix_dMatrix[c][c2];
            }
        }
        /// potential parameters
        // array for documenting parameters for potentials of order one (external forces)
        // structure: [numberOfForces, numberOfDoublesInArray, 
        //  potential0_id, potential0_param0, potential0_value0, potential0_param1,..., 
        //  potential1_id,...]
        ArrayList<Double> potParam1 = new ArrayList<>();
        potParam1.add(0.0); // amount of forces
        potParam1.add(0.0); // length of this array
        // array for documenting parameters for potentials of order two (pairwise forces)        
        ArrayList<Double> potParam2 = new ArrayList<>();
        potParam2.add(0.0); // amount of forces
        potParam2.add(0.0); // length of this array
        // encode all parameters in an array(double)
        // every parameter has it unique number, after which the respective value(s) follow
        Set<Integer> potentialIds = potentialInventory.getPotentialIds();
        Iterator<Integer> potentialIterator = potentialIds.iterator();
        while (potentialIterator.hasNext()) {
            IPotential potential = potentialInventory.getPotential(potentialIterator.next());
            int _order = potential.get_order();
            // #######################################################################################################################################
            // ################# Poterntials Order One ###############################################################################################
            // #######################################################################################################################################
            if (_order == 1) {
                potParam1.set(0, potParam1.get(0) + 1);
                String _type = potential.get_type();
                if (testmode) {
                    System.out.println(_type);
                }
                /// type
                switch (_type) {
                    case "DISK": {
                        potParam1.add(1000.0);
                        break;
                    }
                    case "CYLINDER": {
                        potParam1.add(1001.0);
                        break;
                    }
                    case "CUBE": {
                        potParam1.add(1002.0);
                        break;
                    }
                    case "LOLLIPOP": {
                        potParam1.add(1003.0);
                        break;
                    }
                    case "LOLLYPOP": {
                        potParam1.add(1003.0);
                        break;
                    }
                    case "SPHERE": {
                        potParam1.add(1004.0);
                        break;
                    }
                    /*
                     * add your own potential here
                    case "MY_POTENTIAL": {
                        potParam1.add(1234.0); // your own potentialID, >1000
                        break;
                    }*/
                    default: {
                        System.err.println("Force for CUDA not supportet! Please try a different core");
                        return;
                    }
                }
                /// parameters
                HashMap<String, String> _parameterMap = potential.get_parameterMap();
                Set<Map.Entry<String, String>> entrySet = _parameterMap.entrySet();
                Iterator<Map.Entry<String, String>> entryIterator = entrySet.iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, String> entry = entryIterator.next();
                    if (testmode) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                    switch (entry.getKey()) {
                        case "id": {
                            break;
                        }
                        case "name": {
                            break;
                        }
                        case "type": {
                            break;
                        }
                        case "center": {
                            potParam1.add(1.0);
                            String[] split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                            for (int i = 0; i < split.length; i++) {
                                potParam1.add(Double.parseDouble(split[i]));
                            }
                            break;
                        }
                        case "height": {
                            potParam1.add(2.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "normal": {
                            potParam1.add(3.0);
                            String[] split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                            for (int i = 0; i < split.length; i++) {
                                potParam1.add(Double.parseDouble(split[i]));
                            }
                            break;
                        }
                        case "subtype": {
                            potParam1.add(4.0);
                            if (0 == entry.getValue().compareTo("attractive")) {
                                potParam1.add(1.0);
                            } else if (0 == entry.getValue().compareTo("repulsive")) {
                                potParam1.add(2.0);
                            } else if (0 == entry.getValue().compareTo("membrane")) {
                                potParam1.add(3.0);
                            } else {
                                System.err.println("Force subtype for CUDA not supportet! Please try a different core");
                                return;
                            }
                            break;
                        }
                        case "affectedParticleIds": {
                            potParam1.add(5.0);
                            ArrayList<Double> temp = new ArrayList<Double>();
                            String[] split = null;
                            int length = 0;
                            if (!entry.getValue().isEmpty() && entry.getValue().compareTo("null") != 0 && entry.getValue().compareTo("all") != 0) {
                                split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                                for (int i = 0; i < split.length; i++) {
                                    temp.add(Double.parseDouble(split[i]));
                                }
                                length = split.length;
                            } else if (entry.getValue().compareTo("all") == 0) {
                                System.err.println("\"all\" affectedParticleIDs not supported jet on cuda ");
                                return;
                            }
                            potParam1.add((double) temp.size());
                            for (int i = 0; i < length; i++) {
                                potParam1.add(temp.get(i));
                            }
                            break;
                        }
                        case "radius": {
                            potParam1.add(6.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "forceConst": {
                            potParam1.add(7.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "affectedParticleTypeIds": {
                            potParam1.add(8.0);
                            ArrayList<Double> temp = new ArrayList<Double>();
                            String[] split = null;
                            int length = 0;
                            if (!entry.getValue().isEmpty() && entry.getValue().compareTo("null") != 0 && entry.getValue().compareTo("all") != 0) {
                                split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                                for (int i = 0; i < split.length; i++) {
                                    temp.add(Double.parseDouble(split[i]));
                                }
                                length = split.length;
                            } else if (entry.getValue().compareTo("all") == 0) {
                                allTypes = particleTypes.iterator();
                                while (allTypes.hasNext()) {
                                    temp.add((double) allTypes.next());
                                }
                                length = temp.size();
                            }
                            potParam1.add((double) temp.size());
                            for (int i = 0; i < length; i++) {
                                potParam1.add(temp.get(i));
                            }
                            break;
                        }
                        case "considerParticleRadius": {
                            potParam1.add(9.0);
                            if (entry.getValue().compareTo("true") == 0) {
                                potParam1.add(1.0);
                            } else if (entry.getValue().compareTo("false") == 0) {
                                potParam1.add(0.0);
                            } else {
                                System.err.println("don't understand \"considerParticleRadius value\"");
                            }
                            break;
                        }
                        case "sphereRadius": {
                            potParam1.add(10.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "cylinderRadius": {
                            potParam1.add(11.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "cylinderHeight": {
                            potParam1.add(12.0);
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "origin": {
                            potParam1.add(13.0);
                            String[] split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                            for (int i = 0; i < split.length; i++) {
                                potParam1.add(Double.parseDouble(split[i]));
                            }
                            break;
                        }
                        case "extension": {
                            potParam1.add(14.0);
                            String[] split = entry.getValue().substring(1, entry.getValue().length() - 1).split(",");
                            for (int i = 0; i < split.length; i++) {
                                potParam1.add(Double.parseDouble(split[i]));
                            }
                            break;
                        }
                        /*
                         * add your own parameter here
                        case "myParameter": {
                            potParam1.add(123.0);   // your unique parameter Key (<1000)
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            // if you use more than one parametervalue, than don't forget to note down the length of your values 
                            break;
                        }*/
                        default: {
                            System.out.println(entry.getKey() + ":" + entry.getValue());
                            System.err.println("Force parameter not supportet on CUDA. Please try a different core.");
                            return;
                        }
                    }
                }
            }// end order1
            
            // #######################################################################################################################################
            // ################# Poterntials Order Two ###############################################################################################
            // #######################################################################################################################################
            else if (_order == 2) {
                potParam2.set(0, potParam2.get(0) + 1);
                String _type = potential.get_type();
                if (testmode) {
                    System.out.println(_type);
                }
                switch (_type) {
                    case "HARMONIC": {
                        potParam2.add(2000.0);
                        break;
                    }
                    case "WEAK_INTERACTION_HARMONIC": {
                        System.err.println("WEAK_INTERACTION_HARMONIC: Depricated, use WEAK_INTERACTION instead");
                        return;
                    }
                    case "WEAK_INTERACTION": {
                        potParam2.add(2002.0);
                        break;
                    }
                    case "WEAK_INTERACTION_PIECEWISE_HARMONIC": {
                        potParam2.add(2003.0);
                        break;
                    }
                    /*
                     * add your own potential here
                    case "MY_POTENTIAL": {
                        potParam1.add(2345.0); // your own potentialID, >1000
                        break;
                    }*/
                    default: {
                        System.err.println("Force for CUDA not supportet! Please try a different core");
                        return;
                    }
                }

                /// parameters
                HashMap<String, String> _parameterMap = potential.get_parameterMap();
                Set<Map.Entry<String, String>> entrySet = _parameterMap.entrySet();
                Iterator<Map.Entry<String, String>> entryIterator = entrySet.iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, String> entry = entryIterator.next();
                    if (testmode) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                    switch (entry.getKey()) {
                        case "id": {
                            break;
                        }
                        case "name": {
                            break;
                        }
                        case "type": {
                            break;
                        }
                        case "interactionradius": {
                            potParam2.add(13.0);
                            potParam2.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "depth": {
                            potParam2.add(14.0);
                            potParam2.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "length": {
                            potParam2.add(15.0);
                            potParam2.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "forceConst": {
                            potParam2.add(7.0);
                            potParam2.add(Double.parseDouble(entry.getValue()));
                            break;
                        }
                        case "affectedParticleTypeIdPairs": {
                            potParam2.add(8.0);
                            // affectedParticleTypeIdPairs="[1,1];[0,1]"
                            if (entry.getValue().compareTo("null") == 0 || entry.getValue().isEmpty()) {
                                potParam2.add(-1.0);
                            } else {
                                ArrayList<Double> temp = new ArrayList<Double>();
                                int length = 0;
                                if (entry.getValue().compareTo("all") == 0) {
                                    // length=0 -> C++/OpenMM unses all Pairs
                                } else if (!entry.getValue().isEmpty()) {
                                    String[] split = entry.getValue().split(";");
                                    for (int j = 0; j < split.length; j++) {
                                        String[] split2 = split[j].substring(1, split[j].length() - 1).split(",");
                                        for (int i = 0; i < split2.length; i++) {
                                            temp.add(Double.parseDouble(split2[i]));
                                        }
                                        length += split2.length;
                                    }
                                }
                                potParam2.add((double) temp.size());
                                for (int i = 0; i < length; i++) {
                                    potParam2.add(temp.get(i));
                                }
                            }

                            break;
                        }
                        case "affectedParticleIdPairs": {
                            potParam2.add(5.0);
                            ArrayList<Double> temp = new ArrayList<Double>();
                            int length = 0;
                            if (!entry.getValue().isEmpty() && entry.getValue().compareTo("null") != 0) {
                                String[] split = entry.getValue().split(";");
                                for (int j = 0; j < split.length; j++) {
                                    String[] split2 = split[j].substring(1, split[j].length() - 1).split(",");
                                    for (int i = 0; i < split2.length; i++) {
                                        temp.add(Double.parseDouble(split2[i]));
                                    }
                                    length += split2.length;
                                }
                            } else if (entry.getValue().compareTo("all") == 0) {
                                System.err.println("\"all\" affectedParticleIDs not supported jet on cuda ");
                            }
                            potParam2.add((double) temp.size());
                            for (int i = 0; i < length; i++) {
                                potParam2.add(temp.get(i));
                            }
                            break;
                        }
                        case "subtype": {
                            potParam2.add(4.0);
                            if (0 == entry.getValue().compareTo("attractive")) {
                                potParam2.add(1.0);
                            } else if (0 == entry.getValue().compareTo("repulsive")) {
                                potParam2.add(2.0);
                            } else if (0 == entry.getValue().compareTo("spring")) {
                                potParam2.add(3.0);
                            } else {
                                System.err.println("Force subtype for CUDA not supportet! Please try a different core");
                                return;
                            }
                            break;
                        }
                        /*
                         * add your own parameter here
                        case "myParameter": {
                            potParam1.add(123.0);   // your unique parameter Key (<1000)
                            potParam1.add(Double.parseDouble(entry.getValue()));
                            // if you use more than one parametervalue, than don't forget to note down the length of your values 
                            break;
                        }*/
                        default: {
                            System.out.println(entry.getKey() + ":" + entry.getValue());
                            System.err.println("Force parameter not supportet on CUDA. Please try a different core.");
                            return;
                        }
                    }
                }

            }// end order2

            if (testmode) {
                System.out.println("############################################");
            }
        }
        potParam1.set(1, (double) potParam1.size());
        double[] paramPot1 = new double[potParam1.size()];
        for (int i = 0; i < potParam1.size(); i++) {
            paramPot1[i] = potParam1.get(i);
        }
        potParam2.set(1, (double) potParam2.size());
        double[] paramPot2 = new double[potParam2.size()];
        for (int i = 0; i < potParam2.size(); i++) {
            paramPot2[i] = potParam2.get(i);
        }

        // groupParameters
        // the group forces are also stored in the list with potentials of order two
        // obtain now how much different group potentials exist
        System.out.println("***********************************");
        System.out.println("groups:");
        int nGroups = groupConfiguration.getNGroups();
        System.out.println(nGroups + " groups");
        Iterator<IGroup> groupIterator = groupConfiguration.groupIterator();
        int groupforce = 0;
        while (groupIterator.hasNext()) {
            IGroup group = groupIterator.next();
            //group.print();
            int _typeId = group.get_typeId();
            HashMap<Integer, int[][]> groupInternalPotentials = groupParameters.getGroupInternalPotentials(_typeId);
            Set<Map.Entry<Integer, int[][]>> entrySet = groupInternalPotentials.entrySet();
            Iterator<Map.Entry<Integer, int[][]>> entryIterator = entrySet.iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<Integer, int[][]> entry = entryIterator.next();
                if ((int) groupforce < (int) entry.getKey()) {
                    groupforce = (int) entry.getKey();
                }
                //System.out.println(entry.getKey() + ": " + entry.getValue()[0][0] + " " + entry.getValue()[0][1] );// + " " + entry.getValue()[1][0] + " " + entry.getValue()[1][1]); 
            }
        }
        // document the number of group forces, if non exist, set the value to MAX_INT
        if (groupforce == 0) {
            groupforce = Integer.MAX_VALUE;
        }

        // first analysis before
        /*if (analysisAndOutputManager.analysisRequested(9)) {
            System.out.println("analysis...");
            analysisAndOutputManager.analyseAndOutput(0, core.get_ParticleConfiguration(), rkReports);
            if (analysisAndOutputManager.get_resetReactionReportsList()) {
                rkReports.clear();
            }
        }*/

        // call the native C++-library -> create and run new OpenMM simulation
        this.cCreateSimulation(testmode, tplg_crd, tplg_grp, cudaDevNr, nSteps, stepSize, stepsPerFrame, kB, T, periodicBoundaries, (int) particleTypes.size(), diffusion, collisionRadii, paramPot1, paramPot2, cReactions, groupforce, numberOfDummyParticles);
        //System.out.println("end");

        System.out.println("back in JAVA: simulation finished");



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
        System.loadLibrary("CReaDDyMM");
    }
}
