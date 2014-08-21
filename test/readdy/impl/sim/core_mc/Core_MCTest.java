/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdy.impl.sim.core_mc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IReactionObserver;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import readdy.api.assembly.IGroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleCoordinateCreatorFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IReactionManagerFactory;
import readdy.api.assembly.IRkAndElmtlRkFactory;
import readdy.api.assembly.IStandardGroupBasedRkExecutorFactory;
import readdy.api.assembly.IStandardParticleBasedRkExecutorFactory;
import readdy.api.dtypes.IIntPair;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.api.sim.top.rkHandle.rkExecutors.ICustomReactionExecutor;
import readdy.impl.assembly.Core_MC_Factory;
import readdy.impl.assembly.ElementalReactionManagerFactory_internal;
import readdy.impl.assembly.GroupConfigurationFactory;
import readdy.impl.assembly.GroupFactory;
import readdy.impl.assembly.GroupInteriorParticlePositionerFactory;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleCoordinateCreatorFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.assembly.ReactionManagerFactory;
import readdy.impl.assembly.ReactionObserverFactory;
import readdy.impl.assembly.RkAndElmtlRkFactory;
import readdy.impl.assembly.StandardGroupBasedRkExecutorFactory;
import readdy.impl.assembly.StandardParticleBasedRkExecutorFactory;
import readdy.impl.dtypes.IntPair;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.top.rkHandle.rkExecutors.custom.ParticleIdConservingRhodopsinRkExecutor;

import statlab.base.datatypes.IIntIterator;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author johannesschoeneberg
 */
public class Core_MCTest {

    private static IParticleConfiguration particleConfiguration;
    private static IGlobalParameters globalParameters;
    private static IParticleParameters particleParameters;
    private static IPotentialManager potentialManager;
    private static ICore core;
    
    private static String folder = "./test/testInputFiles/test_MonteCarlo_potentialEnergyComputer";

    public Core_MCTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("PotentialEnergyComputerTest...:");
        System.out.println();
        System.out.println("parse globalParameters...");



        System.out.println();
        System.out.println("parse globalParameters...");
        String paramGlobalFilename = folder+"/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();


        System.out.println("parse ParamParticles...");
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        String paramParticlesFilename = folder+"/param_particles.xml";
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();


        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        particleParameters = particleParamFactory.createParticleParameters();
        
        
        //##############################################################################
        // geht the potential parameters as input
        //##############################################################################


        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        String filename_tplgyPotentials = folder+"/tplgy_potentials.xml";
        tplgyPotentialsFileParser.parse(filename_tplgyPotentials);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();

        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        potentialManager = potentialManagerFactory.createPotentialManager();



        //##############################################################################
        // determine lattice box size
        // it is important, that this happens, before the particleConfiguration assembly
        //##############################################################################
        ILatticeBoxSizeComputer latticeBoxSizeComputer = new LatticeBoxSizeComputer(
                particleParameters, 
                potentialInventory, 
                globalParameters);
        double latticeBoxSize = latticeBoxSizeComputer.getLatticeBoxSize();
        System.out.println("latticeBoxSize "+latticeBoxSize);
        globalParameters.set_latticeBoxSize(latticeBoxSize);

        //##############################################################################
        // get the topology coordinates File data as input
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile...");
        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        String tplgyCoordinatesFilename = folder+"/tplgy_coordinates.xml";
        tplgyCoordsParser.parse(tplgyCoordinatesFilename);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();

        //##############################################################################
        // build up the actual class
        //##############################################################################


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        particleConfiguration = configFactory.createParticleConfiguration();


        



        //##############################################################################
        // groupParameters
        //##############################################################################

        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        String filename_paramGroups = folder+"/param_groups.xml";
        paramGroupsFileParser.parse(filename_paramGroups);
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
        String filename_tplgyGroups = folder+"/tplgy_groups.xml";
        tplgyGroupsFileParser.parse(filename_tplgyGroups);
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
        reactionManager.registerAdditionalReaction(newRkIdForward, "idConservingActRAndGCplxFormation", newRkIdBackward, particleIdConservingRhodopsinRkExecutor);
        reactionManager.registerAdditionalReaction(newRkIdBackward, "idConservingActRAndActGCplxFission", newRkIdForward, particleIdConservingRhodopsinRkExecutor);


        //----------------------------------------------------------------------------------------
        // param Reactions ...
        //----------------------------------------------------------------------------------------

        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        String filename_paramReactions = folder+"/param_reactions.xml";
        paramReactionsFileParser.parse(filename_paramReactions);
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

        Core_MC_Factory coreFactory = new Core_MC_Factory();

        coreFactory.set_GlobalParameters(globalParameters);
        coreFactory.set_PotentialManager(potentialManager);
        coreFactory.set_ParticleConfiguration(particleConfiguration);
        coreFactory.set_ParticleParameters(particleParameters);
        coreFactory.set_ReactionObserver(reactionObserver);

        core = coreFactory.createCore();

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
     * Test of get_ParticleConfiguration method, of class Core_MC.
     */
    @Test
    public void testGet_ParticleConfiguration() {
        System.out.println("get_ParticleConfiguration");

        IParticleConfiguration expResult = particleConfiguration;
        IParticleConfiguration result = core.get_ParticleConfiguration();
        assertEquals(expResult, result);


    }

    /**
     * Test of get_PotentialManager method, of class Core_MC.
     */
    @Test
    public void testGet_PotentialManager() {
        System.out.println("get_PotentialManager");

        IPotentialManager expResult = potentialManager;
        IPotentialManager result = core.get_PotentialManager();
        assertEquals(expResult, result);
    }

    /**
     * Test of step method, of class Core_MC.
     */
    @Test
    public void testStep() throws Exception {
        System.out.println();
        System.out.println("test step...");

        System.out.println("test the setup befor the step()");
        System.out.println("testing particle 0 ...");
        IParticle p0 = particleConfiguration.getParticle(0);
        assertEquals(p0.get_id(), 0);
        assertEquals(p0.get_type(), 0);
        assertEquals(p0.get_coords()[0], -3, 0.00);
        assertEquals(p0.get_coords()[1], -4, 0.00);
        assertEquals(p0.get_coords()[2], -5, 0.00);

        System.out.println("testing particle 1 ...");
        IParticle p1 = particleConfiguration.getParticle(1);
        assertEquals(p1.get_id(), 1);
        assertEquals(p1.get_type(), 0);
        assertEquals(p1.get_coords()[0], -4.5, 0.00);
        assertEquals(p1.get_coords()[1], -3.2, 0.00);
        assertEquals(p1.get_coords()[2], -5.3, 0.00);

        double initialDistance = DoubleArrays.distance(p0.get_coords(), p1.get_coords());
        double p0_initDistanceToBoxOrigin = DoubleArrays.distance(p0.get_coords(), new double[]{0, 0, 0});
        double p1_initDistanceToBoxOrigin = DoubleArrays.distance(p1.get_coords(), new double[]{0, 0, 0});
        double[] p0_initCoords = new double[]{};
        p0_initCoords = p0.get_coords();
        double[] p1_initCoords = new double[]{};
        p1_initCoords = p1.get_coords();

        System.out.println("testing current potential energy of the system...");
        double initialPotentialEnergy = 258.493 + 5500 + 6129; // pair contribution, contribution p0, contribution p1
        double expResult = initialPotentialEnergy;

        double result = particleConfiguration.getSystemPotentialEnergy();
        System.out.println("expResult: " + expResult + " result: " + result);
        assertEquals(expResult, result, 0.1);

        System.out.println();
        System.out.println("step...");
        int stepId = 0;
        core.step(stepId);

        System.out.println("after step evaluation");
        System.out.println("new particle positions:");
        IParticle p0_new = particleConfiguration.getParticle(0);
        IParticle p1_new = particleConfiguration.getParticle(1);
        p0_new.print();
        p1_new.print();

        System.out.println("check the diffusion...");
        double dt = globalParameters.get_dt();

        double p0_D = particleParameters.getParticleParametersEntry(p0.get_type()).get_D();
        double p0_diffusionDistance = DoubleArrays.distance(p0_initCoords, p0_new.get_coords());
        // the values are normally distributed with sigma sqrt(2*D*dt)
        // we take 5sigma as the maximal value
        // five-sigma corresponds to a p-value, or probability, of 3×10-7
        double p0_maxDiffusionDistance = 5 * Math.sqrt(2 * p0_D * dt);
        System.out.println("p0 diffusionDistance: " + p0_diffusionDistance + " max 5*Sqrt(2*D*dt): " + p0_maxDiffusionDistance);
        // the absolute value of the noise can be at most
        // Math.sqrt(2 * D *dt);
        assertTrue(p0_diffusionDistance <= p0_maxDiffusionDistance);

        double p1_D = particleParameters.getParticleParametersEntry(p1.get_type()).get_D();
        double p1_diffusionDistance = DoubleArrays.distance(p1_initCoords, p1_new.get_coords()) + 0.0000000000001;
        double p1_maxDiffusionDistance = 5 * Math.sqrt(2 * p1_D * dt);
        System.out.println("p1 diffusionDistance: " + p1_diffusionDistance + " max 5*Sqrt(2*D*dt): " + p1_maxDiffusionDistance);
        assertTrue(p1_diffusionDistance <= p1_maxDiffusionDistance);

        System.out.println("done.");


        System.out.println("check the potential energy difference...");
        double newPotentialEnergy = particleConfiguration.getSystemPotentialEnergy();
        if (newPotentialEnergy > initialPotentialEnergy) {
            System.out.println("LARGER new potential energy: " + newPotentialEnergy);
            System.out.println("init E_pot: " + initialPotentialEnergy + " new E_pot: " + newPotentialEnergy);


            System.out.println("make sure, the DISTANCE BETWEEN the has DECREASED, "
                    + "or their DISTANCE to the BOX potential has INCREASED.");

            double newDistance = DoubleArrays.distance(p0_new.get_coords(), p1_new.get_coords());
            System.out.println("init dist(p0,p1): " + initialDistance + " new dist(p0,p1): " + newDistance);

            double p0_newDistanceToBoxOrigin = DoubleArrays.distance(p0_new.get_coords(), new double[]{0, 0, 0});
            System.out.println("init dist(p0,{0,0,0}): " + p0_initDistanceToBoxOrigin
                    + " new dist(p0,{0,0,0}): " + p0_newDistanceToBoxOrigin);
            double p1_newDistanceToBoxOrigin = DoubleArrays.distance(p1_new.get_coords(), new double[]{0, 0, 0});
            System.out.println("init dist(p1,{0,0,0}): " + p1_initDistanceToBoxOrigin
                    + " new dist(p1,{0,0,0}): " + p1_newDistanceToBoxOrigin);
            assertTrue(newDistance > initialDistance
                    || p0_newDistanceToBoxOrigin > p0_initDistanceToBoxOrigin
                    || p1_newDistanceToBoxOrigin > p1_initDistanceToBoxOrigin);
        } else {
            System.out.println("SMALLER OR EQUAL new potential energy: " + newPotentialEnergy);
            System.out.println("init E_pot: " + initialPotentialEnergy + " new E_pot: " + newPotentialEnergy);


            System.out.println("make sure, the DISTANCE between the has INCREASED, "
                    + "or their DISTANCE to the box potential has DECREASED.");

            double newDistance = DoubleArrays.distance(p0_new.get_coords(), p1_new.get_coords());
            System.out.println("init dist(p0,p1): " + initialDistance + " new dist(p0,p1): " + newDistance);

            double p0_newDistanceToBoxOrigin = DoubleArrays.distance(p0_new.get_coords(), new double[]{0, 0, 0});
            System.out.println("init dist(p0,{0,0,0}): " + p0_initDistanceToBoxOrigin
                    + " new dist(p0,{0,0,0}): " + p0_newDistanceToBoxOrigin);
            double p1_newDistanceToBoxOrigin = DoubleArrays.distance(p1_new.get_coords(), new double[]{0, 0, 0});
            System.out.println("init dist(p1,{0,0,0}): " + p1_initDistanceToBoxOrigin
                    + " new dist(p1,{0,0,0}): " + p1_newDistanceToBoxOrigin);
            assertTrue(newDistance <= initialDistance
                    || p0_newDistanceToBoxOrigin <= p0_initDistanceToBoxOrigin
                    || p1_newDistanceToBoxOrigin <= p1_initDistanceToBoxOrigin);
        }
        System.out.println("done");



    }

    /**
     * check if the neighborlist returns the correct neighbors after multiple steps
     * @throws Exception
     */
    @Test
    public void testStep_neighBorTesting() throws Exception {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("computeEnergy single particle");
        
        System.out.println("putting in particles");
        int nParticles = 100;
        double[] boxExtension = new double[]{10, 10, 10};
        double[] boxOrigin = new double[]{0, 0, 0};
        int typeIdOfNewParticles = 0;
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            double[] newCoords = new double[]{boxOrigin[0] + rand.nextDouble() * boxExtension[0],
                boxOrigin[1] + rand.nextDouble() * boxExtension[1],
                boxOrigin[2] + rand.nextDouble() * boxExtension[2]
            };
            particleConfiguration.createParticle(typeIdOfNewParticles, newCoords);

        }

        int nSteps = 100;
        for (int i = 0; i < nSteps; i++) {
            System.out.println("step: "+i);

            core.step(i);
            // monitor particle 10
            int particleId_specific = 10;
            IParticle pSpecific = particleConfiguration.getParticle(particleId_specific);
            pSpecific.print();
            // compute the neighbor list in a brute force way
            HashSet<IIntPair> pairList = new HashSet();
            IIntPair pair1 = new IntPair(1, 2);
            IIntPair pair2 = new IntPair(2, 1);
            //System.out.println("equal pairs?");
            //System.out.println(pair1.equals(pair2));
            assertTrue(pair1.equals(pair2));

            System.out.println("computing the neighbors brute forcely");
            double criticalDistance = globalParameters.get_latticeBoxSize();
            System.out.println("latticeBoxSize " + criticalDistance);
            Iterator<IParticle> pIter = particleConfiguration.particleIterator();

            while (pIter.hasNext()) {
                IParticle p1 = pIter.next();
                Iterator<IParticle> pIter2 = particleConfiguration.particleIterator();
                while (pIter2.hasNext()) {
                    IParticle p2 = pIter2.next();
                    //p1.print();
                    //p2.print();
                    double dist = DoubleArrays.distance(p1.get_coords(), p2.get_coords());
                    //System.out.println("dist: "+dist);
                    if (p1.get_id() != p2.get_id() && dist <= criticalDistance) {
                        IIntPair possibleNewPair = new IntPair(p1.get_id(), p2.get_id());
                        if (!pairList.contains(possibleNewPair)) {
                            pairList.add(possibleNewPair);
                        }
                    }
                }
            }

            int pairCounter = 0;
            for (IIntPair pair : pairList) {
                pairCounter++;
            }
            System.out.println("we created " + pairCounter + " pairs");


            // now take all pairs that go with a particular particle:
            ArrayList<IIntPair> specificNeighborList = new ArrayList();
            HashSet<Integer> specificNeighborParticleIds_bruteForce = new HashSet();
            System.out.println("get all neighbors of particleId 10");
            for (IIntPair pair : pairList) {
                if (pair.get_i() == particleId_specific || pair.get_j() == particleId_specific) {
                    System.out.println("pair: " + pair.get_i() + "," + pair.get_j());
                    specificNeighborList.add(pair);
                    if (pair.get_i() == particleId_specific) {
                        specificNeighborParticleIds_bruteForce.add(pair.get_j());
                    }
                    if (pair.get_j() == particleId_specific) {
                        specificNeighborParticleIds_bruteForce.add(pair.get_i());
                    }
                }
            }
            System.out.print("Brute Force neighboringParticleIds: ");
            for (int particleId : specificNeighborParticleIds_bruteForce) {
                double dist = DoubleArrays.distance(pSpecific.get_coords(),particleConfiguration.getParticle(particleId).get_coords());
                System.out.print(particleId + "(d="+roundTwoDecimals(dist)+"), ");

            }
            System.out.println();

            // compute the neighbor Pair list in with the neighborLattice
            IIntIterator iter = particleConfiguration.getNeighboringParticleIds(pSpecific.get_coords());
            HashSet<Integer> specificNeighborParticleIds_neighborList = new HashSet();
            System.out.print("NeighborList neighboringParticleIds: ");
            while (iter.hasNext()) {
                int pId_neighbor_neighborList = iter.next();
                System.out.print(pId_neighbor_neighborList + ", ");
                specificNeighborParticleIds_neighborList.add(pId_neighbor_neighborList);
            }
            System.out.println();

            // check if the brute force computed and the neighborList computed neighbors are the same
            for(int bruteForceNeighborId : specificNeighborParticleIds_bruteForce){
                assertTrue(specificNeighborParticleIds_neighborList.contains(bruteForceNeighborId));
            }
            assertTrue(specificNeighborParticleIds_bruteForce.size() <= specificNeighborParticleIds_neighborList.size());
            System.out.println("all  brute force found neighbors included in the neighbor list list! - good!");

            // compute the potential energy that goes with that particular particle

            
            //double expResult = 6129;
            // compare the result with the result from the potential Energy computer



            //IParticle pSpecific = particleConfiguration.getParticle(particleId_specific);
            //double result = potentialEnergyComputer.computeEnergy(pSpecific);
            //System.out.println("expResult: "+expResult+ " result: "+result);
            //assertEquals(expResult, result, 0.00);
        }
    }

    double roundTwoDecimals(double d) {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
}
}
