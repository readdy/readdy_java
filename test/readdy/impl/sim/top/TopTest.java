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

import readdy.api.analysis.IAnalysisAndOutputManager;
import readdy.api.sim.top.ITop;
import readdy.impl.assembly.Core_Default_Factory;
import readdy.api.assembly.IDiffusionEngineFactory;
import readdy.impl.assembly.DiffusionEngineFactory;
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.assembly.ITopFactory;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.impl.assembly.ReactionObserverFactory;
import readdy.impl.assembly.Top_Default_Factory;
import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.api.assembly.IElmtlRkToRkMatcherFactory;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.impl.assembly.ElmtlRkToRkMatcherFactory;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.impl.assembly.GroupConfigurationFactory;
import readdy.impl.assembly.GroupFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.disassembly.GroupDisassembler;
import readdy.impl.io.in.tpl_group.TplgyGroupsFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.api.assembly.IReactionParametersFactory;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import readdy.impl.assembly.ReactionParametersFactory;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import readdy.impl.assembly.AnalysisAndOutputManagerFactory;
import readdy.impl.assembly.ElementalReactionManagerFactory_internal;
import readdy.impl.assembly.GroupInteriorParticlePositionerFactory;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.api.assembly.IReactionConflictResolverFactory;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleCoordinateCreatorFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.ReactionConflictResolverFactory;
import readdy.impl.assembly.ReactionHandlerFactory;
import readdy.impl.assembly.ReactionManagerFactory;
import readdy.impl.assembly.ReactionValidatorFactory;
import readdy.impl.assembly.RkAndElmtlRkFactory;
import readdy.impl.assembly.StandardGroupBasedRkExecutorFactory;
import readdy.impl.assembly.StandardParticleBasedRkExecutorFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.core_mc.MetropolisDecider;
import readdy.impl.sim.core_mc.PotentialEnergyComputer;
import readdy.impl.sim.top.rkHandle.rkExecutors.custom.ParticleIdConservingDimerRkExecutor;

/**
 *
 * @author schoeneberg
 */
public class TopTest {

    private static IAnalysisAndOutputManager analysisAndOutputManager;
    private static ICore core;
    private static IGlobalParameters globalParameters;
    private static IReactionHandler reactionHandler;
    private static ITop top;
    private static IParticleConfiguration particleConfiguration;

    public TopTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //##############################################################################
        // get the particle parameters as input
        //##############################################################################


        System.out.println();
        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_top/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();
        globalParameters.setOutputPath("./test/testInputFiles/test_top/output/");

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_top/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();



        //##############################################################################
        // geht the potential parameters as input
        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        String tplgyPotentialFilename = "./test/testInputFiles/test_top/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
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
        // particleConfiguration
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_top/tplgy_coordinates.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        particleConfiguration = configFactory.createParticleConfiguration();


        //##############################################################################
        // groupParameters
        //##############################################################################

        String paramGroupsfilename = "./test/testInputFiles/test_top/param_groups.xml";
        //String paramGroupsfilename = "./test/testInputFiles/test_top/x_racks_param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(paramGroupsfilename);
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

        String filename = "./test/testInputFiles/test_top/tplgy_groups.xml";
        //String filename = "./test/testInputFiles/test_top/x_racks_tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
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

        
        MetropolisDecider metropolisDecider = new MetropolisDecider();
        metropolisDecider.set_GlobalParameters(globalParameters);

        PotentialEnergyComputer potentialEnergyComputer = new PotentialEnergyComputer();
        potentialEnergyComputer.set_particleParameters(particleParameters);
        potentialEnergyComputer.set_potentialManager(potentialManager);

        IStandardParticleBasedRkExecutorFactory standardParticleBasedRkExecutorFactory = new StandardParticleBasedRkExecutorFactory();

        standardParticleBasedRkExecutorFactory.set_particleCoordinateCreator(particleCoordinateCreator);
        standardParticleBasedRkExecutorFactory.set_PotentialEnergyComputer(potentialEnergyComputer);
        standardParticleBasedRkExecutorFactory.set_MetropolisDecider(metropolisDecider);

        standardParticleBasedRkExecutorFactory.set_particleParameters(particleParameters);
        IReactionExecutor standardParticleBasedRkExecutor = standardParticleBasedRkExecutorFactory.createStandardParticleBasedRkExecutor();


        IReactionManagerFactory reactionManagerFactory = new ReactionManagerFactory();
        reactionManagerFactory.setStandardGroupBasedRkExecutor(standardGroupBasedRkExecutor);
        reactionManagerFactory.setStandardParticleBasedRkExecutor(standardParticleBasedRkExecutor);
        IReactionManager reactionManager = reactionManagerFactory.createReactionManager();


        //----------------------------------------------------------------------------------------
        // create additional Reaction Executors for specialized Reactions
        //----------------------------------------------------------------------------------------

        ParticleIdConservingDimerRkExecutor particleIdConservingRhodopsinRkExecutor_ = new ParticleIdConservingDimerRkExecutor();
        particleIdConservingRhodopsinRkExecutor_.setParticleCoordinateCreator(particleCoordinateCreator);
        IReactionExecutor particleIdConservingRhodopsinRkExecutor = particleIdConservingRhodopsinRkExecutor_;

        reactionManager.registerAdditionalReaction(11, "idConservingActRAndGCplxFormation", 12, particleIdConservingRhodopsinRkExecutor);
        reactionManager.registerAdditionalReaction(12, "idConservingActRAndActGCplxFission", 11, particleIdConservingRhodopsinRkExecutor);

        //##############################################################################
        // Reactions
        //##############################################################################


        //----------------------------------------------------------------------------------------
        // param Reactions ...
        //----------------------------------------------------------------------------------------

        String filenameParamReactions = "./test/testInputFiles/test_top/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filenameParamReactions);
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


        Core_Default_Factory coreFactory = new Core_Default_Factory();

        coreFactory.set_ParticleConfiguration(particleConfiguration);
        coreFactory.set_DiffusionEngine(diffusionEngine);
        coreFactory.set_ReactionObserver(reactionObserver);

        core = coreFactory.createCore();

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
        analysisAndOutputManagerFactory.set_particleParameters(particleParameters);
        analysisAndOutputManagerFactory.set_reactionParameters(reactionParameters);
        analysisAndOutputManager = analysisAndOutputManagerFactory.createAnalysisAndOutputManager();

        //##############################################################################
        // assemble everything
        //##############################################################################


        ITopFactory topFactory = new Top_Default_Factory();
        topFactory.setAnalysisManager(analysisAndOutputManager);
        topFactory.setCore(core);
        topFactory.setGlobalParameters(globalParameters);
        topFactory.setReactionHandler(reactionHandler);
        top = topFactory.createTop();


    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of runSimulation method, of class Top.
     */
    @Test
    public void testRunSimulation() {
        long t0 = System.nanoTime();
        top.runSimulation();
        long t1 = System.nanoTime();
        System.out.println("raw runtime = " + (t1 - t0) + " ns");
        System.out.println("avg time per step = " + (t1 - t0) / globalParameters.get_nSimulationSteps() + " ns");

    }
}
