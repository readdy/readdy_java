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
package readdy.impl.assembly;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import readdy.api.analysis.IAnalysisInstruction;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.assembly.IAnalysisAndOutputManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.out.IOutputWriter;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import readdy.impl.analysis.AnalysisAndOutputManager;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_Particle;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_ParticleNumbers;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_PotentialEnergy;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_Reaction;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_Reaction_tabulated;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_VMDParticle;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_VMDParticle_ReaDDyLogo;
import readdy.impl.analysis.runtimeAnalyser.InfoCollector_VMDParticle_Tutorial;
import readdy.impl.analysis.runtimeAnalyser.MsdComputer;
import readdy.impl.analysis.runtimeAnalyser.MsdComputer_GNUplot;
import readdy.impl.analysis.runtimeAnalyser.MsdComputer_GNUplot_Sphere;
import readdy.impl.analysis.runtimeAnalyser.ParticleDensityAlongAxisComputer;
import readdy.impl.analysis.runtimeAnalyser.RadialParticleDensityComputer;
import readdy.impl.analysis.runtimeAnalyser.RdfComputer_GNUplot;
import readdy.impl.analysis.runtimeAnalyser.RdfComputer_GNUplot_Sphere;
import readdy.impl.analysis.runtimeAnalyser.RdfComputer_GNUplot_old;
import readdy.impl.analysis.runtimeAnalyser.RdfComputer_old;
import readdy.impl.io.out.CSV_Writer;
import readdy.impl.io.out.PACSV_Writer;
import readdy.impl.io.out.TplgyCoordsFile_Writer;
import readdy.impl.io.out.XYZ_Writer;

/**
 *
 * @author schoeneberg
 */
public class AnalysisAndOutputManagerFactory implements IAnalysisAndOutputManagerFactory {

    IGlobalParameters globalParameters;
    IReactionParameters reactionParameters;
    IParticleParameters particleParameters;
    HashMap<String, IReaddyRuntimeAnalyser> availableRuntimeAnalyser = new HashMap();
    HashMap<String, IOutputWriter> availableOutputWriter = new HashMap();

    public AnalysisAndOutputManagerFactory() {
        availableRuntimeAnalyser.clear();
    }

    private void setupAllAvailableRuntimeAnalyser() {
        availableRuntimeAnalyser.put("standard_traj", new InfoCollector_Particle());
        availableRuntimeAnalyser.put("vmdReadable_traj", new InfoCollector_VMDParticle());
        // this vmd output reader is designed for the ReaDDy tutorial and works for the vesicle - membrane model in there
        availableRuntimeAnalyser.put("vmdReadable_traj_tutorial", new InfoCollector_VMDParticle_Tutorial());
        availableRuntimeAnalyser.put("vmdReadable_traj_ReaDDyLogo", new InfoCollector_VMDParticle_ReaDDyLogo());
        availableRuntimeAnalyser.put("standardReactionReporting", new InfoCollector_Reaction_tabulated(reactionParameters));
        // prints only the timestep, the reaction id and the reaction type id
        availableRuntimeAnalyser.put("reactionReporting_short", new InfoCollector_Reaction("short",reactionParameters));
        availableRuntimeAnalyser.put("reactionReporting_cumulative", new InfoCollector_Reaction("cumulative",reactionParameters));
        availableRuntimeAnalyser.put("reactionEventFile", new InfoCollector_Reaction("standard",reactionParameters));
        availableRuntimeAnalyser.put("spatialUnimolecularReactionEval", new InfoCollector_Reaction("spatialUnimolecularReactionEval",reactionParameters));
        availableRuntimeAnalyser.put("msd", new MsdComputer());
        availableRuntimeAnalyser.put("msd_gnuplot", new MsdComputer_GNUplot(particleParameters));
        availableRuntimeAnalyser.put("msd_gnuplot_sphere", new MsdComputer_GNUplot_Sphere(particleParameters));
        availableRuntimeAnalyser.put("particleNumbers", new InfoCollector_ParticleNumbers(particleParameters));
        availableRuntimeAnalyser.put("rdf", new RdfComputer_old());
        availableRuntimeAnalyser.put("rdf_gnuplot", new RdfComputer_GNUplot_old());
        availableRuntimeAnalyser.put("rdf_gnuplot_3d", new RdfComputer_GNUplot());
        availableRuntimeAnalyser.put("rdf_gnuplot_sphere", new RdfComputer_GNUplot_Sphere());
        availableRuntimeAnalyser.put("particleDensityAlongAxis", new ParticleDensityAlongAxisComputer());
        availableRuntimeAnalyser.put("radialParticleDensity", new RadialParticleDensityComputer());

        availableRuntimeAnalyser.put("potentialEnergy", new InfoCollector_PotentialEnergy());

        availableOutputWriter.put("xyz", new XYZ_Writer());
        availableOutputWriter.put("csv", new CSV_Writer());
        availableOutputWriter.put("pacsv", new PACSV_Writer());
        availableOutputWriter.put("tplgyCoords", new TplgyCoordsFile_Writer());
    }

    public AnalysisAndOutputManager createAnalysisAndOutputManager() {
        if (allInputAvailable()) {

            setupAllAvailableRuntimeAnalyser();


            AnalysisAndOutputManager analysisAndOutputManager = new AnalysisAndOutputManager();

            ArrayList<IAnalysisInstruction> analysisInstructions = globalParameters.get_analysisInstructions();
            System.out.println("parse analysis instructions...");
            for (IAnalysisInstruction instruction : analysisInstructions) {
                //instruction.print();
                String analysisAndOutputUnitName = getNextName(instruction);
                int everyXStep = instruction.get_everyXStep();
                String[] specialFlags = instruction.get_specialFlags();
                IReaddyRuntimeAnalyser analyser = parseFrameAnalyser(instruction);
                analyser.set_globalParameters(globalParameters);
                IOutputWriter outputWriter = parseOutputWriter(instruction);
                analysisAndOutputManager.registerAnalysisAndOutputUnit(analysisAndOutputUnitName, everyXStep, analyser, outputWriter, specialFlags);
            }
            System.out.println("... done.");

            return analysisAndOutputManager;
        } else {
            throw new RuntimeException("not all input available");
        }
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public void set_reactionParameters(IReactionParameters reactionParameters) {
        this.reactionParameters = reactionParameters;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    private boolean allInputAvailable() {
        return globalParameters != null && reactionParameters != null && particleParameters != null;
    }

    private IReaddyRuntimeAnalyser parseFrameAnalyser(IAnalysisInstruction instruction) {
        String method = instruction.get_method();
        String outputFormat = instruction.get_outputFormat();
        IReaddyRuntimeAnalyser frameAnalyser = null;
        System.out.println("build output for method: \"" + method + "\" ...");

        // <catch problems>
        if (!availableRuntimeAnalyser.containsKey(method)) {
            String availableMethods = "";
            for (String avMethod : availableRuntimeAnalyser.keySet()) {
                availableMethods += avMethod + "\n";
            }
            throw new RuntimeException("\n-----------------------------------------------\n"
                    + "the requested runtime Analyser with the method: \"" + method + "\" has not been found. \n"
                    + "Available methods:\n\n"
                    + availableMethods
                    + "\nAbort!\n"
                    + "-----------------------------------------------\n");
        }
        if (!availableOutputWriter.containsKey(outputFormat)) {
            String availableOutputFormats = "";
            for (String avOut : availableOutputWriter.keySet()) {
                availableOutputFormats += avOut + "\n";
            }
            throw new RuntimeException("\n-----------------------------------------------\n"
                    + "the requested output format: \"" + outputFormat + "\" has not been found. \n"
                    + "Available formats:\n\n"
                    + availableOutputFormats
                    + "\nAbort!\n"
                    + "-----------------------------------------------\n");
        }
        // </catch problems>

        if (availableRuntimeAnalyser.containsKey(method) && availableOutputWriter.containsKey(outputFormat)) {
            frameAnalyser = availableRuntimeAnalyser.get(method);
        }
        if (frameAnalyser == null) {
            throw new RuntimeException("the requested frame analyser with the method: \"" + method + "\" has not been found. Abort!");
        }
        return frameAnalyser;
    }

    private IOutputWriter parseOutputWriter(IAnalysisInstruction instruction) {
        String method = instruction.get_method();
        String outputFormat = instruction.get_outputFormat();
        String outputFile = globalParameters.get_outputPath() + instruction.get_outputFile();
        IOutputWriter result = null;
        if (availableRuntimeAnalyser.containsKey(method) && availableOutputWriter.containsKey(outputFormat)) {
            try {
                try {
                    result = availableOutputWriter.get(outputFormat).getClass().getConstructor().newInstance();
                    result.open(outputFile);
                } catch (InstantiationException ex) {
                    Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);

                }
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(AnalysisAndOutputManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    private static int analysisAndOutputUnitId = -1;

    private String getNextName(IAnalysisInstruction instruction) {
        analysisAndOutputUnitId++;
        int nextId = analysisAndOutputUnitId;
        String[] s = instruction.get_outputFile().split("/");

        String filename = "";
        for (int i = 0; i < s.length; i++) {
            filename = s[i];
        }
        return nextId + "_" + instruction.get_method() + "|" + instruction.get_outputFormat() + " -> " + filename;
    }
}
