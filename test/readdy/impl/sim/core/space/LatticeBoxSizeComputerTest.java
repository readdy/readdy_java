/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdy.impl.sim.core.space;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IDiffusionEngineFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.impl.assembly.Core_Default_Factory;
import readdy.impl.assembly.DiffusionEngineFactory;
import readdy.impl.assembly.ElementalReactionManagerFactory_externalFile;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.assembly.ReactionObserverFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.rct_elmtlRk.ReactElmtlRkFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.io.out.XYZ_Writer;

/**
 *
 * @author johannesschoeneberg
 */
public class LatticeBoxSizeComputerTest {
    private IGlobalParameters globalParameters;
    private IParticleConfiguration particleConfiguration;
    private IPotentialInventory potentialInventory;
    private IParticleParameters particleParameters;
    private IGlobalParameters globalParameters2;
    
    public LatticeBoxSizeComputerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        //##############################################################################
        // global parameters
        //##############################################################################

        System.out.println("ParticleParametersTEST...:");
        System.out.println();
        System.out.println("parse param_global...");
        String paramGlobalFilename = "./test/testInputFiles/test_core/coreTest_param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();
        
        //##############################################################################
        // global parameters 2
        //##############################################################################

        System.out.println("ParticleParametersTEST...:");
        System.out.println();
        System.out.println("parse param_global...");
        paramGlobalFilename = "./test/testInputFiles/test_latticeBoxSizeComputer/boxSizeComputerTest_param_global.xml";
        paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters2 = paramGlobalFileParser.get_globalParameters();

        //##############################################################################
        // particle parameters
        //##############################################################################
        
        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_core/coreTest_param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        particleParameters = particleParamFactory.createParticleParameters();


        //##############################################################################
        // potential Inventory
        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        potentialInventory = potInvFactory.createPotentialInventory();

       
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getLatticeBoxSize method, of class LatticeBoxSizeComputer.
     */
    @Test
    public void testGetLatticeBoxSize() {
        System.out.println("getLatticeBoxSize test0, no user input");
        LatticeBoxSizeComputer instance = new LatticeBoxSizeComputer(particleParameters, potentialInventory, globalParameters);
        double expResult = 2.0;
        double result = instance.getLatticeBoxSize();
        assertEquals(expResult, result, 0.0);
        
        System.out.println("getLatticeBoxSize test1, user input");
        LatticeBoxSizeComputer instance2 = new LatticeBoxSizeComputer(particleParameters, potentialInventory, globalParameters2);
        expResult = 3.0;
        result = instance2.getLatticeBoxSize();
        assertEquals(expResult, result, 0.0);
        
    }
}
