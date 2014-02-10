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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import readdy.api.assembly.IReaDDySimulatorFactory;
import readdy.api.sim.IReaDDySimulator;
import readdy.api.analysis.IAnalysisAndOutputManager;
import readdy.api.sim.top.ITop;
import readdy.api.assembly.IDiffusionEngineFactory;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.assembly.ITopFactory;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.api.assembly.IElmtlRkToRkMatcherFactory;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import java.util.ArrayList;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.impl.disassembly.GroupDisassembler;
import readdy.impl.io.in.tpl_group.TplgyGroupsFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.api.assembly.IReactionParametersFactory;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import java.util.HashMap;
import readdy.api.assembly.IAnalysisAndOutputManagerFactory;
import readdy.api.assembly.IGroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleCoordinateCreatorFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IReactionHandlerFactory;
import readdy.api.assembly.IReactionManagerFactory;
import readdy.api.assembly.IReactionValidatorFactory;
import readdy.api.assembly.IRkAndElmtlRkFactory;
import readdy.api.assembly.IStandardGroupBasedRkExecutorFactory;
import readdy.api.assembly.IStandardParticleBasedRkExecutorFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.api.assembly.IReactionConflictResolverFactory;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.rkHandle.rkExecutors.ICustomReactionExecutor;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.sim.ReaDDySimulator;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.top.rkHandle.rkExecutors.custom.ParticleIdConservingRhodopsinRkExecutor;
import readdy.impl.sim.top.rkHandle.rkExecutors.custom.PositionConservingBimolecularRkExecutor;
import readdy.impl.sim.top.rkHandle.rkExecutors.custom.PositionDependentUnimolecularRkExecutor;

/**
 * This is the essential main factory that creates the entire ReaDDy simulator!s
 *
 * @author schoeneberg
 */
public class ReaDDySimulatorFactory implements IReaDDySimulatorFactory {
    /*
     * these are the simulator Versions, that are currenly supported by ReaDDy
     *    we have default:         The default, single core ReaDDy implementation (schoneberg et al. 2013)
     *   we have monteCarlo:     The monte carlo core version of the single Core ReaDDy (schoneberg et al. 2013)
     *    we have readdyMM:       ReaDDy with its core replaced by OpenMM (to be published)
     *  * @author johannesschoeneberg
     */
    
    private static String reaDDyImplementation = "default";
    private String[] possibleValuesForImplementationKey = new String[]{"default", "monteCarlo", "readdyMM"};

    private static IAnalysisAndOutputManager analysisAndOutputManager;
    private ICore core;
    private IGlobalParameters globalParameters;
    private IReactionHandler reactionHandler;
    private ITop top;
    HashMap<String, String> inputValues;
    private String[] essentialInputFileKeys = new String[]{
        "param_global", //0
        "param_groups", //1
        "param_particles", //2
        "param_reactions", //3
        "tplgy_coordinates", //4
        "tplgy_groups", //5
        "tplgy_potentials" //6
    };
    private String[] optionalInputFileKeys = new String[]{
        "react_elmtlRk",
        "output_path",
        "implementation", // default, monteCarlo, readdyMM
    };
    private String[] softwareInputKeys = new String[]{
        "help",
        "version",
        "license"
    };
    

    public String[] getEssentialInputFileKeys() {
        return essentialInputFileKeys;
    }

    public String[] getOptionalInputFileKeys() {
        return optionalInputFileKeys;
    }

    private void setup() {


        //##############################################################################
        // get the desired Version of the ReaDDy implementation
        //##############################################################################
         /*
         * these are the simulator Versions, that are currenly supported by ReaDDy
         *    we have DEFAULT:         The default, single core ReaDDy implementation (schoneberg et al. 2013)
         *    we have MONTE_CARLO:     The monte carlo core version of the single Core ReaDDy (schoneberg et al. 2013)
         *    we have READDY_MM:       ReaDDy with its core replaced by OpenMM (to be published)
         *  * @author johannesschoeneberg
         *  * 
         */
        if (inputValues.containsKey("implementation")) {
            String impl = inputValues.get("implementation");
            if (impl.contentEquals(possibleValuesForImplementationKey[0])) {
                // Default
                System.out.println("Use ReaDDy Implementation: " + possibleValuesForImplementationKey[0]);
                reaDDyImplementation = possibleValuesForImplementationKey[0];
            } else {
                if (impl.contentEquals(possibleValuesForImplementationKey[1])) {
                    // Monte Carlo
                    System.out.println("Use ReaDDy Implementation: " + possibleValuesForImplementationKey[1]);
                    reaDDyImplementation = possibleValuesForImplementationKey[1];
                } else {
                    if (impl.contentEquals(possibleValuesForImplementationKey[2])) {
                        // ReaDDy MM
                        System.out.println("Use ReaDDy Implementation: " + possibleValuesForImplementationKey[2]);
                        reaDDyImplementation = possibleValuesForImplementationKey[2];
                    } else {

                        System.out.println("ATTENTION: implementation key '" + impl + "' not recognized. Supported are "
                                + possibleValuesForImplementationKey[0] + ","
                                + possibleValuesForImplementationKey[1] + "and"
                                + possibleValuesForImplementationKey[2] + ". Resolved by setting to DEFAULT.");
                        reaDDyImplementation = possibleValuesForImplementationKey[0];

                    }
                }
            }
        }



        //##############################################################################
        // get the particle parameters as input
        //##############################################################################
        System.out.println();

        System.out.println(
                "parse globalParameters...");
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();

        paramGlobalFileParser.parse(inputValues.get("param_global"));
        globalParameters = paramGlobalFileParser.get_globalParameters();

        if (inputValues.containsKey(
                "output_path")) {
            System.out.println(globalParameters.get_outputPath());
            globalParameters.setOutputPath(inputValues.get("output_path"));
        } else {
            String defaultPath = ".";
            java.io.File f = new java.io.File(".");
            System.out.println("$$$ ReaDDySimulatorFactory: no output path specified.");
            try {
                defaultPath = f.getCanonicalPath() + "/output/";


            } catch (IOException ex) {
                Logger.getLogger(ReaDDySimulatorFactory.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("$$$ ReaDDySimulatorFactory: output is written to: ");
            System.out.println(defaultPath);
            globalParameters.setOutputPath(defaultPath);



        }

        System.out.println(globalParameters.get_outputPath());
        System.out.println(
                "parse ParamParticles");
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();

        paramParticlesFileParser.parse(inputValues.get("param_particles"));
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();
        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();

        particleParamFactory.set_globalParameters(globalParameters);

        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();
        //##############################################################################
        // get the potential parameters as input
        //##############################################################################
        //IParamPotTplFileParser parser = new ParamPotTplFileParser();
        //parser.parse(inputValues.get("param_potentialTemplates"));
        //IParamPotTplFileData parPotTplFileData = parser.get_paramPotTplFileData();
        IPotentialFactory potentialFactory = new PotentialFactory();
        //potentialFactory.set_paramPotTplFileData(parPotTplFileData);
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();

        potInvFactory.set_potentialFactory(potentialFactory);
        //potInvFactory.set_paramPotTplFileData(parPotTplFileData);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();

        tplgyPotentialsFileParser.parse(inputValues.get("tplgy_potentials"));
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();
        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();

        potentialManagerFactory.set_potentialInventory(potentialInventory);

        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);

        potentialManagerFactory.set_particleParameters(particleParameters);
        IPotentialManager potentialManager = potentialManagerFactory.createPotentialManager();
        //##############################################################################
        // create the diffusion Engine
        //##############################################################################
        IDiffusionEngineFactory diffEngineFactory = new DiffusionEngineFactory();

        diffEngineFactory.set_particleParameters(particleParameters);

        diffEngineFactory.set_potentialManager(potentialManager);
        IDiffusionEngine diffusionEngine = diffEngineFactory.createDiffusionEngine();
        //##############################################################################
        // determine lattice box size
        // it is important, that this happens, before the particleConfiguration assembly
        //##############################################################################
        ILatticeBoxSizeComputer latticeBoxSizeComputer = new LatticeBoxSizeComputer(
                particleParameters,
                potentialInventory,
                globalParameters);
        double latticeBoxSize = latticeBoxSizeComputer.getLatticeBoxSize();

        globalParameters.set_latticeBoxSize(latticeBoxSize);

        //##############################################################################
        // particle Configuration 
        //##############################################################################
        System.out.println(
                "parse tplgyCoordinatesFile");
        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();

        tplgyCoordsParser.parse(inputValues.get("tplgy_coordinates"));
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();
        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();

        configFactory.set_particleParameters(particleParameters);

        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);

        configFactory.set_globalParameters(globalParameters);
        IParticleConfiguration particleConfiguration = configFactory.createParticleConfiguration();
        //##############################################################################
        // groupParameters
        //##############################################################################
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();

        paramGroupsFileParser.parse(inputValues.get("param_groups"));
        IParamGroupsFileData paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();
        IGroupParametersFactory groupParametersFactory = new GroupParametersFactory();

        groupParametersFactory.set_paramGroupsFileData(paramGroupsFileData);

        groupParametersFactory.set_particleParameters(particleParameters);

        groupParametersFactory.set_potentialInventory(potentialInventory);
        IGroupParameters groupParameters = groupParametersFactory.createGroupParameters();
        //##############################################################################
        // Group Configuration
        //##############################################################################
        //----------------------------------------------------------------------------------------
        // group Topology
        //----------------------------------------------------------------------------------------
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();

        tplgyGroupsFileParser.parse(inputValues.get("tplgy_groups"));
        ITplgyGroupsFileData tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();
        //----------------------------------------------------------------------------------------
        IGroupFactory groupFactory = new GroupFactory();

        groupFactory.set_potentialManager(potentialManager);

        groupFactory.set_groupParameters(groupParameters);
        IGroupDisassembler groupDisassembler = new GroupDisassembler();

        groupDisassembler.set_potentialManager(potentialManager);

        groupDisassembler.set_groupParameters(groupParameters);
        IGroupConfigurationFactory groupConfigurationFactory = new GroupConfigurationFactory();

        groupConfigurationFactory.set_tplgyGroupsFileData(tplgyGroupsFileData);

        groupConfigurationFactory.set_groupFactory(groupFactory);

        groupConfigurationFactory.set_groupDisassembler(groupDisassembler);

        groupConfigurationFactory.set_groupParameters(groupParameters);

        groupConfigurationFactory.set_particleConfiguration(particleConfiguration);
        IGroupConfiguration groupConfiguration = groupConfigurationFactory.createGroupConfiguration();
        //##############################################################################
        // Reaction Manager
        //##############################################################################
        //----------------------------------------------------------------------------------------
        // create GroupInteriorParticlePositioner
        //----------------------------------------------------------------------------------------
        IGroupInteriorParticlePositionerFactory groupInteriorParticlePositionerFactory = new GroupInteriorParticlePositionerFactory();

        groupInteriorParticlePositionerFactory.set_groupParameters(groupParameters);
        IGroupInteriorParticlePositioner particlePositioner = groupInteriorParticlePositionerFactory.createGroupInteriorParticlePositioner();
        //----------------------------------------------------------------------------------------
        // create standardGroupBasedRkExecutor
        //----------------------------------------------------------------------------------------
        IStandardGroupBasedRkExecutorFactory standardGroupBasedRkExecutorFactory = new StandardGroupBasedRkExecutorFactory();

        standardGroupBasedRkExecutorFactory.set_groupInteriorParticlePositioner(particlePositioner);
        IReactionExecutor standardGroupBasedRkExecutor = standardGroupBasedRkExecutorFactory.createStandardGroupBasedRkExecutor();
        //----------------------------------------------------------------------------------------
        // create ParticleCoordinateCreator
        //----------------------------------------------------------------------------------------
        IParticleCoordinateCreatorFactory particleCoordinateCreatorFactory = new ParticleCoordinateCreatorFactory();

        particleCoordinateCreatorFactory.set_particleParameters(particleParameters);

        particleCoordinateCreatorFactory.set_globalParameters(globalParameters);
        IParticleCoordinateCreator particleCoordinateCreator = particleCoordinateCreatorFactory.createParticleCoordinateCreator();
        //----------------------------------------------------------------------------------------
        // create standardParticleBasedRkExecutor
        //----------------------------------------------------------------------------------------
        IStandardParticleBasedRkExecutorFactory standardParticleBasedRkExecutorFactory = new StandardParticleBasedRkExecutorFactory();

        standardParticleBasedRkExecutorFactory.set_particleCoordinateCreator(particleCoordinateCreator);

        standardParticleBasedRkExecutorFactory.set_particleParameters(particleParameters);
        IReactionExecutor standardParticleBasedRkExecutor = standardParticleBasedRkExecutorFactory.createStandardParticleBasedRkExecutor();
        //----------------------------------------------------------------------------------------
        IReactionManagerFactory reactionManagerFactory = new ReactionManagerFactory();

        reactionManagerFactory.setStandardGroupBasedRkExecutor(standardGroupBasedRkExecutor);

        reactionManagerFactory.setStandardParticleBasedRkExecutor(standardParticleBasedRkExecutor);
        IReactionManager reactionManager = reactionManagerFactory.createReactionManager();
        //##############################################################################
        // Reactions
        //##############################################################################
        //----------------------------------------------------------------------------------------
        // create additional Reaction Executors for specialized Reactions
        //----------------------------------------------------------------------------------------
        ParticleIdConservingRhodopsinRkExecutor particleIdConservingRhodopsinRkExecutor_ = new ParticleIdConservingRhodopsinRkExecutor();

        particleIdConservingRhodopsinRkExecutor_.setParticleCoordinateCreator(particleCoordinateCreator);
        ICustomReactionExecutor particleIdConservingRhodopsinRkExecutor = particleIdConservingRhodopsinRkExecutor_;
        int newRkIdForward = reactionManager.getNextFreeReactionId();
        int newRkIdBackward = reactionManager.getNextFreeReactionId();

        particleIdConservingRhodopsinRkExecutor.setForwardAndBackwardRkId(newRkIdForward, newRkIdBackward);

        reactionManager.registerAdditionalReaction(newRkIdForward,
                "idConservingActRAndGCplxFormation", newRkIdBackward, particleIdConservingRhodopsinRkExecutor);
        reactionManager.registerAdditionalReaction(newRkIdBackward,
                "idConservingActRAndActGCplxFission", newRkIdForward, particleIdConservingRhodopsinRkExecutor);


        // ----------------------------

        PositionConservingBimolecularRkExecutor positionConservingBimolecularRkExecutor_ = new PositionConservingBimolecularRkExecutor();

        positionConservingBimolecularRkExecutor_.setParticleParticleParameters(particleParameters);

        positionConservingBimolecularRkExecutor_.setParticleCoordinateCreator(particleCoordinateCreator);
        ICustomReactionExecutor positionConservingBimolecularRkExecutor = positionConservingBimolecularRkExecutor_;
        newRkIdForward = reactionManager.getNextFreeReactionId();
        newRkIdBackward = reactionManager.getNextFreeReactionId();

        positionConservingBimolecularRkExecutor.setForwardAndBackwardRkId(newRkIdForward, newRkIdBackward);

        reactionManager.registerAdditionalReaction(newRkIdForward,
                "positionConserving_fusion", newRkIdBackward, positionConservingBimolecularRkExecutor);
        reactionManager.registerAdditionalReaction(newRkIdBackward,
                "positionConserving_fission", newRkIdForward, positionConservingBimolecularRkExecutor);


        // ----------------------------

        PositionDependentUnimolecularRkExecutor positionDependentUnimolecularRkExecutor_ = new PositionDependentUnimolecularRkExecutor();
        ICustomReactionExecutor positionDependentUnimolecularRkExecutor = positionDependentUnimolecularRkExecutor_;
        newRkIdForward = reactionManager.getNextFreeReactionId();
        newRkIdBackward = reactionManager.getNextFreeReactionId();

        positionDependentUnimolecularRkExecutor.setForwardAndBackwardRkId(newRkIdForward, newRkIdBackward);

        reactionManager.registerAdditionalReaction(newRkIdForward,
                "positionDependentUnimolecular_outOfBox", newRkIdBackward, positionDependentUnimolecularRkExecutor);
        reactionManager.registerAdditionalReaction(newRkIdBackward,
                "positionDependentUnimolecular_insideBox", newRkIdForward, positionDependentUnimolecularRkExecutor);


        // ----------------------------
    /*
         ParticleIdConservingDimerRkExecutor particleIdConservingDimerRkExecutor_ = new ParticleIdConservingDimerRkExecutor();
         particleIdConservingDimerRkExecutor_.setParticleCoordinateCreator(particleCoordinateCreator);
         ICustomReactionExecutor particleIdConservingDimerRkExecutor = particleIdConservingDimerRkExecutor_;

         int newRkIdForward_dimer = reactionManager.getNextFreeReactionId();
         int newRkIdBackward_dimer = reactionManager.getNextFreeReactionId();
         particleIdConservingDimerRkExecutor.setForwardAndBackwardRkId(newRkIdForward_dimer,newRkIdBackward_dimer);
         reactionManager.registerAdditionalReaction(newRkIdForward_dimer,"idConservingDimerFormation", newRkIdBackward_dimer, particleIdConservingDimerRkExecutor);
         reactionManager.registerAdditionalReaction(newRkIdBackward_dimer,"idConservingDimerFission", newRkIdForward_dimer, particleIdConservingDimerRkExecutor);

         // ----------------------------

         ParticleIdConservingTetramerRkExecutor particleIdConservingTetramerRkExecutor_ = new ParticleIdConservingTetramerRkExecutor();
         particleIdConservingTetramerRkExecutor_.setParticleCoordinateCreator(particleCoordinateCreator);
         ICustomReactionExecutor particleIdConservingTetramerRkExecutor = particleIdConservingTetramerRkExecutor_;

         int newRkIdForward_tetramer = reactionManager.getNextFreeReactionId();
         int newRkIdBackwardtetramer = reactionManager.getNextFreeReactionId();
         particleIdConservingTetramerRkExecutor.setForwardAndBackwardRkId(newRkIdForward_tetramer,newRkIdBackwardtetramer);
         reactionManager.registerAdditionalReaction(newRkIdForward_tetramer,"idConservingTetramerFormation", newRkIdBackwardtetramer, particleIdConservingTetramerRkExecutor);
         reactionManager.registerAdditionalReaction(newRkIdBackwardtetramer,"idConservingTetramerFission", newRkIdForward_tetramer, particleIdConservingTetramerRkExecutor);
         *
         */
        //----------------------------------------------------------------------------------------
        // param Reactions ...
        //----------------------------------------------------------------------------------------

        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();

        paramReactionsFileParser.parse(inputValues.get("param_reactions"));
        IParamReactionsFileData paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();
        //----------------------------------------------------------------------------------------
        IRkAndElmtlRkFactory rkAndElmtlRkFactory = new RkAndElmtlRkFactory();

        rkAndElmtlRkFactory.set_globalParameters(globalParameters);

        rkAndElmtlRkFactory.set_reactionManager(reactionManager);

        rkAndElmtlRkFactory.set_groupParameters(groupParameters);

        rkAndElmtlRkFactory.set_particleParameters(particleParameters);

        rkAndElmtlRkFactory.createReactionsAndElmtlReactions(paramReactionsFileData);
        HashMap<Integer, IReaction> reactions = rkAndElmtlRkFactory.get_reactions();
        HashMap<Integer, Integer> elmtlRkId_to_rkId_map = rkAndElmtlRkFactory.get_elmtlRkToRkMapping();
        //##############################################################################
        // Elemental Reactionmanager
        //##############################################################################
        HashMap<Integer, IElementalReaction> elementalReactions = rkAndElmtlRkFactory.get_elementalReactions();
        for (int rkId : elementalReactions.keySet()) {
            elementalReactions.get(rkId).print();
        }
        ElementalReactionManagerFactory_internal elmtlRkManagerFactory = new ElementalReactionManagerFactory_internal();

        elmtlRkManagerFactory.set_particleParameters(particleParameters);

        elmtlRkManagerFactory.set_elmtlReactions(elementalReactions);
        IElementalReactionManager elementalReactionManager = elmtlRkManagerFactory.createElementalRactionManager();
        //##############################################################################
        // reactionObserver
        //##############################################################################
        IReactionObserverFactory reactionObserverFactory = new ReactionObserverFactory();

        reactionObserverFactory.set_elementalReactionManager(elementalReactionManager);

        reactionObserverFactory.set_particleParameters(particleParameters);
        IReactionObserver reactionObserver = reactionObserverFactory.createReactionObserver();

        //##############################################################################
        // CORE
        //##############################################################################

        if (reaDDyImplementation.contentEquals(possibleValuesForImplementationKey[1])) {
            //MONTE_CARLO

            Core_MC_Factory coreFactory = new Core_MC_Factory();

            coreFactory.set_GlobalParameters(globalParameters);
            coreFactory.set_PotentialManager(potentialManager);
            coreFactory.set_ParticleConfiguration(particleConfiguration);
            coreFactory.set_ParticleParameters(particleParameters);
            coreFactory.set_ReactionObserver(reactionObserver);

            core = coreFactory.createCore();
        } else {

            Core_Default_Factory coreFactory = new Core_Default_Factory();

            coreFactory.set_ParticleConfiguration(particleConfiguration);
            coreFactory.set_DiffusionEngine(diffusionEngine);
            coreFactory.set_ReactionObserver(reactionObserver);

            core = coreFactory.createCore();
        }
        //##############################################################################
        // reactionParameters
        //##############################################################################
        IReactionParametersFactory reactionParametersFactory = new ReactionParametersFactory();

        reactionParametersFactory.set_reactions(reactions);

        reactionParametersFactory.set_elmtlRkId_to_rkId_map(elmtlRkId_to_rkId_map);
        IReactionParameters reactionParameters = reactionParametersFactory.createReactionParameters();
        //##############################################################################
        // ElmtlRkToRkMatcher
        //##############################################################################
        IElmtlRkToRkMatcherFactory elmtlRkToRkMatcherFactory = new ElmtlRkToRkMatcherFactory();

        elmtlRkToRkMatcherFactory.set_groupConfiguration(groupConfiguration);

        elmtlRkToRkMatcherFactory.set_reactionParameters(reactionParameters);
        IElmtlRkToRkMatcher elmtlRkToRkMatcher = elmtlRkToRkMatcherFactory.createElmtlRkToRkMatcher();
        //##############################################################################
        // ReactionValidator
        //##############################################################################
        IReactionValidatorFactory reactionValidatorFactory = new ReactionValidatorFactory();

        reactionValidatorFactory.set_groupConfiguration(groupConfiguration);

        reactionValidatorFactory.set_groupParameters(groupParameters);
        IReactionValidator reactionValidator = reactionValidatorFactory.createReactionValidator();
        //##############################################################################
        // ReactionConflictResolver
        //##############################################################################
        IReactionConflictResolverFactory reactionConflictResolverFactory = new ReactionConflictResolverFactory();

        reactionConflictResolverFactory.set_groupConfiguration(groupConfiguration);
        IReactionConflictResolver reactionConflictResolver = reactionConflictResolverFactory.createReactionConflictResolver();
        //##############################################################################
        //##############################################################################
        // Reaction Handler
        //##############################################################################
        //##############################################################################
        IReactionHandlerFactory reactionHandlerFactory = new ReactionHandlerFactory();

        reactionHandlerFactory.set_elmtlRkToRkMatcher(elmtlRkToRkMatcher);

        reactionHandlerFactory.set_reactionValidator(reactionValidator);

        reactionHandlerFactory.set_reactionConflictResolver(reactionConflictResolver);

        reactionHandlerFactory.set_reactionManager(reactionManager);
        reactionHandler = reactionHandlerFactory.createReactionHandler();
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        //##############################################################################
        // create the analysis and output manager
        //##############################################################################
        IAnalysisAndOutputManagerFactory analysisAndOutputManagerFactory = new AnalysisAndOutputManagerFactory();

        analysisAndOutputManagerFactory.set_globalParameters(globalParameters);

        analysisAndOutputManagerFactory.set_reactionParameters(reactionParameters);

        analysisAndOutputManagerFactory.set_particleParameters(particleParameters);
        analysisAndOutputManager = analysisAndOutputManagerFactory.createAnalysisAndOutputManager();
        //##############################################################################
        // assemble everything
        //##############################################################################
        ITopFactory topFactory = new TopFactory();

        topFactory.setAnalysisManager(analysisAndOutputManager);

        topFactory.setCore(core);

        topFactory.setGlobalParameters(globalParameters);

        topFactory.setReactionHandler(reactionHandler);
        top = topFactory.createTop();
    }

    public IReaDDySimulator createReaDDySimulator() {
        if (allInputPresent()) {
            setup();
            ReaDDySimulator readdySimulator = new ReaDDySimulator(top);
            return readdySimulator;
        } else {
            throw new RuntimeException("not all input present");
        }
    }

    public void set_inputValues(HashMap<String, String> inputFilenames) {
        for (String essentialKey : essentialInputFileKeys) {
            if (!inputFilenames.containsKey(essentialKey)) {
                throw new RuntimeException("the essential input file '" + essentialKey + "' is not present in the input file list");
            }
        }
        this.inputValues = inputFilenames;
    }

    private boolean allInputPresent() {
        return inputValues != null;
    }

    public String[] getSoftwareInputKeys() {
        return softwareInputKeys;
    }
}
