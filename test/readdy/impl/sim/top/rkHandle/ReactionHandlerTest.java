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

import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.api.assembly.IElmtlRkToRkMatcherFactory;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.impl.assembly.ElmtlRkToRkMatcherFactory;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core.rk.OccurredElementalReaction;
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
import static org.junit.Assert.*;
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
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
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

/**
 *
 * @author schoeneberg
 */
public class ReactionHandlerTest {

    private static IElmtlRkToRkMatcher elmtlRkToRkMatcher;
    private static IReactionValidator reactionValidator;
    private static IReactionConflictResolver reactionConflictResolver;
    private static IReactionManager reactionManager;
    private static IReactionHandler reactionHandler;
    private static IParticleConfiguration setting_particleConfiguration;
    private static IGroupConfiguration setting_groupConfiguration;
    private static IPotentialManager setting_potentialManager;

    public ReactionHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


        //##############################################################################
        // particleParameters
        //##############################################################################

        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_default/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

        


        //##############################################################################
        // potentialManager
        //##############################################################################
        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        String tplgyPotentialFilename = "./test/testInputFiles/test_default/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();
        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        setting_potentialManager = potentialManagerFactory.createPotentialManager();


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
        // ParticleConfiguration
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_default/tplgy_coordinates.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        setting_particleConfiguration = configFactory.createParticleConfiguration();
        

        //##############################################################################
        // groupParameters
        //##############################################################################

        String paramGroupsfilename = "./test/testInputFiles/test_default/param_groups.xml";
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

        String filename = "./test/testInputFiles/test_default/tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
        ITplgyGroupsFileData tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();

        //----------------------------------------------------------------------------------------
        IGroupFactory groupFactory = new GroupFactory();
        groupFactory.set_potentialManager(setting_potentialManager);
        groupFactory.set_groupParameters(groupParameters);

        IGroupDisassembler groupDisassembler = new GroupDisassembler();
        groupDisassembler.set_potentialManager(setting_potentialManager);
        groupDisassembler.set_groupParameters(groupParameters);

        IGroupConfigurationFactory groupConfigurationFactory = new GroupConfigurationFactory();
        groupConfigurationFactory.set_tplgyGroupsFileData(tplgyGroupsFileData);
        groupConfigurationFactory.set_groupFactory(groupFactory);
        groupConfigurationFactory.set_groupDisassembler(groupDisassembler);
        groupConfigurationFactory.set_groupParameters(groupParameters);
        groupConfigurationFactory.set_particleConfiguration(setting_particleConfiguration);

        setting_groupConfiguration = groupConfigurationFactory.createGroupConfiguration();
        ArrayList<IParticle> positionedParticles = new ArrayList();
        positionedParticles.add(new Particle(0, 0, new double[]{0, 0, 0}));
        positionedParticles.add(new Particle(1, 0, new double[]{0, 0, 0}));
        setting_groupConfiguration.createGroup(0, positionedParticles);



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
        potentialEnergyComputer.set_potentialManager(setting_potentialManager);

        IStandardParticleBasedRkExecutorFactory standardParticleBasedRkExecutorFactory = new StandardParticleBasedRkExecutorFactory();

        standardParticleBasedRkExecutorFactory.set_particleCoordinateCreator(particleCoordinateCreator);
        standardParticleBasedRkExecutorFactory.set_PotentialEnergyComputer(potentialEnergyComputer);
        standardParticleBasedRkExecutorFactory.set_MetropolisDecider(metropolisDecider);

        standardParticleBasedRkExecutorFactory.set_particleParameters(particleParameters);
        IReactionExecutor standardParticleBasedRkExecutor = standardParticleBasedRkExecutorFactory.createStandardParticleBasedRkExecutor();

        //----------------------------------------------------------------------------------------

        IReactionManagerFactory reactionManagerFactory = new ReactionManagerFactory();
        reactionManagerFactory.setStandardGroupBasedRkExecutor(standardGroupBasedRkExecutor);
        reactionManagerFactory.setStandardParticleBasedRkExecutor(standardParticleBasedRkExecutor);
        reactionManager = reactionManagerFactory.createReactionManager();

        //##############################################################################
        // ReactionParameters
        //##############################################################################


        //----------------------------------------------------------------------------------------
        // param Reactions ...
        //----------------------------------------------------------------------------------------

        String filenameParamReactions = "./test/testInputFiles/test_default/param_reactions.xml";
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

        IReactionParametersFactory reactionParametersFactory = new ReactionParametersFactory();
        reactionParametersFactory.set_reactions(reactions);
        reactionParametersFactory.set_elmtlRkId_to_rkId_map(elmtlRkId_to_rkId_map);
        IReactionParameters reactionParameters = reactionParametersFactory.createReactionParameters();

        //##############################################################################
        // ElmtlRkToRkMatcher
        //##############################################################################


        IElmtlRkToRkMatcherFactory elmtlRkToRkMatcherFactory = new ElmtlRkToRkMatcherFactory();
        elmtlRkToRkMatcherFactory.set_groupConfiguration(setting_groupConfiguration);
        elmtlRkToRkMatcherFactory.set_reactionParameters(reactionParameters);
        elmtlRkToRkMatcher = elmtlRkToRkMatcherFactory.createElmtlRkToRkMatcher();

        //##############################################################################
        // ReactionValidator
        //##############################################################################

        IReactionValidatorFactory reactionValidatorFactory = new ReactionValidatorFactory();
        reactionValidatorFactory.set_groupConfiguration(setting_groupConfiguration);
        reactionValidatorFactory.set_groupParameters(groupParameters);
        reactionValidator = reactionValidatorFactory.createReactionValidator();

        //##############################################################################
        // ReactionConflictResolver
        //##############################################################################

        IReactionConflictResolverFactory reactionConflictResolverFactory = new ReactionConflictResolverFactory();
        reactionConflictResolverFactory.set_groupConfiguration(setting_groupConfiguration);
        reactionConflictResolver = reactionConflictResolverFactory.createReactionConflictResolver();

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
     * Test of handleOccurredReactions method, of class ReactionHandler.
     */
    @Test
    public void testHandleOccurredReactions() {
        System.out.println("handleOccurredReactions");


        ArrayList<IOccurredElementalReaction> occElmtlRkList = new ArrayList();

        int nParticlesBefore = setting_particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);

        int nGroupsBefore = setting_groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        // ---------------------------------------------------------------------------
        // 2 x CREATION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("CREATION");
        System.out.println("---------------------------------------------------------------------------");


        int elmtlRkId = 14;
        System.out.println("testing elmtlRkId " + elmtlRkId + " ...");
        IParticle[] involvedParticles = new IParticle[]{};

        IOccurredElementalReaction occElmtlRk0 = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        IOccurredElementalReaction occElmtlRk1 = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk0);
        occElmtlRkList.add(occElmtlRk1);


        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GROUP");
        System.out.println("---------------------------------------------------------------------------");


        elmtlRkId = 0;
        System.out.println("testing elmtlRkId " + elmtlRkId + " ...");
        // this reaction should not work because the max number of dimers (1) is already reached for both particles
        // they are already in a dimer
        involvedParticles = new IParticle[]{setting_particleConfiguration.getParticle(4), setting_particleConfiguration.getParticle(5)};
        IOccurredElementalReaction occElmtlRk2 = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk2);

        // this reaction should pass
        involvedParticles = new IParticle[]{setting_particleConfiguration.getParticle(6), setting_particleConfiguration.getParticle(7)};
        IOccurredElementalReaction occElmtlRk3 = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk3);


        //------------------------------------------------------------------------------------------------
        ArrayList<IReactionExecutionReport> reportList = reactionHandler.handleOccurredReactions(0, occElmtlRkList,
                setting_particleConfiguration,
                setting_groupConfiguration,
                setting_potentialManager);
        //------------------------------------------------------------------------------------------------
        System.out.println("execution reports...");
        System.out.println("reportList size:"+reportList.size());
        for (IReactionExecutionReport report : reportList) {
            report.print();
        }

        int nParticlesAfter = setting_particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore + 2, nParticlesAfter);


        int nGroupsAfter = setting_groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);
        assertEquals(nGroupsBefore + 1, nGroupsAfter);

        

    }
}
