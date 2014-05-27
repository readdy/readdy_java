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
package readdy.impl.sim.top.rkHandle.rkExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.api.assembly.IParticleCoordinateCreatorFactory;
import readdy.api.assembly.IStandardParticleBasedRkExecutorFactory;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.impl.assembly.ParticleCoordinateCreatorFactory;
import readdy.impl.assembly.StandardParticleBasedRkExecutorFactory;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.assembly.GroupConfigurationFactory;
import readdy.impl.assembly.GroupFactory;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.disassembly.GroupDisassembler;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.io.in.tpl_group.TplgyGroupsFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.core_mc.MetropolisDecider;
import readdy.impl.sim.core_mc.PotentialEnergyComputer;
import readdy.impl.sim.top.group.ExtendedIdAndType;
import readdy.impl.sim.top.rkHandle.ExecutableReaction;

/**
 *
 * @author schoeneberg
 */
public class StandardParticleBasedRkExecutorTest {

    private static IReactionExecutor standardParticleBasedRkExecutor;
    // test suite
    private static IParticleConfiguration particleConfiguration;
    private static IGroupConfiguration groupConfiguration;
    private static IPotentialManager potentialManager;

    public StandardParticleBasedRkExecutorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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


        IParticleCoordinateCreatorFactory particleCoordinateCreatorFactory = new ParticleCoordinateCreatorFactory();
        particleCoordinateCreatorFactory.set_particleParameters(particleParameters);
        particleCoordinateCreatorFactory.set_globalParameters(globalParameters);
        IParticleCoordinateCreator particleCoordinateCreator = particleCoordinateCreatorFactory.createParticleCoordinateCreator();

        
        
       

        //########################################################################################
        // generate testing suite
        //########################################################################################


        //##############################################################################
        // group Topology
        //##############################################################################

        String filename = "./test/testInputFiles/test_default/tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
        ITplgyGroupsFileData tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();


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
        particleConfiguration = configFactory.createParticleConfiguration();


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
        // GroupConfiguration
        //##############################################################################

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
        groupConfiguration = groupConfigurationFactory.createGroupConfiguration();

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
        
        standardParticleBasedRkExecutor = standardParticleBasedRkExecutorFactory.createStandardParticleBasedRkExecutor();
        standardParticleBasedRkExecutor.setup(particleConfiguration, groupConfiguration, potentialManager);
        
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
     * Test of setup method, of class StandardParticleBasedRkExecutor.
     */
    @Test
    public void testSetup() {
        System.out.println("setup");
        standardParticleBasedRkExecutor.setup(particleConfiguration, groupConfiguration, potentialManager);
        // this method has actually no output but if it fails intrinsically one would see it here.
    }

    /**
     * Test of executeReaction method, of class StandardParticleBasedRkExecutor.
     */
    @Test
    public void testExecuteReaction() {
        System.out.println("executeReaction");

        // ---------------------------------------------------------------------------
        // CREATION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("CREATION");
        System.out.println("---------------------------------------------------------------------------");


        int nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);

        int rkId = 0;
        int rkTypeId = 0; // creation
        int derivedFromElmtlRkId = 0;

        HashMap<IParticle, IExtendedIdAndType> educts = new HashMap();

        IExtendedIdAndType p_extIdAndType1 = new ExtendedIdAndType(false, 0, 1);
        ArrayList<IExtendedIdAndType> products = new ArrayList();
        products.add(p_extIdAndType1);

        IExecutableReaction executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        IReactionExecutionReport report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        ArrayList<IParticle> removedParticles = report.getRemovedParticles();
        ArrayList<IParticle> createdParticles = report.getCreatedParticles();
        ArrayList<IParticle> typeChangedParticles = report.getTypeChangedParticles();
        ArrayList<Integer> typeChangeParticles_from = report.getTypeChangeParticles_from();
        ArrayList<Integer> typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(1, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("createdParticle:");
        IParticle createdParticleInCreation = createdParticles.get(0);
        createdParticleInCreation.print();

        int nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore + 1, nParticlesAfter);



        // ---------------------------------------------------------------------------
        // DECAY
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("DECAY");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 1; // decay
        derivedFromElmtlRkId = 0;

        educts = new HashMap();
        educts.put(createdParticleInCreation, new ExtendedIdAndType(createdParticleInCreation));

        products = new ArrayList();


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(1, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("removedParticle:");
        IParticle removedParticleInDecay = removedParticles.get(0);
        removedParticleInDecay.print();

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore - 1, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // DOUBLE CREATION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("DOUBLE CREATION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 2; // double creation
        derivedFromElmtlRkId = 0;

        educts = new HashMap();

        products = new ArrayList();
        p_extIdAndType1 = new ExtendedIdAndType(false, 0, 1);
        IExtendedIdAndType p_extIdAndType2 = new ExtendedIdAndType(false, 0, 1);
        products.add(p_extIdAndType1);
        products.add(p_extIdAndType2);

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(2, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("createdParticles:");
        IParticle createdParticleDuringDoubleCreation1 = createdParticles.get(0);
        IParticle createdParticleDuringDoubleCreation2 = createdParticles.get(1);
        createdParticleDuringDoubleCreation1.print();
        createdParticleDuringDoubleCreation2.print();

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore + 2, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // ANNIHILATION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("ANNIHILATION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 3; // annihilation
        derivedFromElmtlRkId = 0;

        educts = new HashMap();
        educts.put(createdParticleDuringDoubleCreation1, new ExtendedIdAndType(createdParticleDuringDoubleCreation1));
        educts.put(createdParticleDuringDoubleCreation2, new ExtendedIdAndType(createdParticleDuringDoubleCreation2));

        products = new ArrayList();

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(2, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("createdParticles:");
        IParticle removedParticleDuringDoubleAnnihilation1 = removedParticles.get(0);
        IParticle removedParticleDuringDoubleAnnihilation2 = removedParticles.get(1);
        removedParticleDuringDoubleAnnihilation1.print();
        removedParticleDuringDoubleAnnihilation2.print();

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore - 2, nParticlesAfter);



        // ---------------------------------------------------------------------------
        // TYPE CONVERSION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("TYPE CONVERSION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 4; // typeConversion
        derivedFromElmtlRkId = 0;

        IParticle particleToConvert = particleConfiguration.getParticle(11);
        int typeIdToConvertInto = 2;
        System.out.println("particle to convert to " + typeIdToConvertInto);
        particleToConvert.print();

        educts = new HashMap();
        educts.put(particleToConvert, new ExtendedIdAndType(particleToConvert));


        products = new ArrayList();
        products.add(new ExtendedIdAndType(new Particle(particleToConvert.get_id(), typeIdToConvertInto, particleToConvert.get_coords())));

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(1, typeChangedParticles.size());
        assertEquals(1, typeChangeParticles_from.size());
        assertEquals(1, typeChangeParticles_to.size());

        System.out.println("typeConvertedParticle after conversion:");
        IParticle particleAfterTypeConversion = typeChangedParticles.get(0);
        int particleTypeBeforConversion = typeChangeParticles_from.get(0);
        int particleTypeAfterConversion = typeChangeParticles_to.get(0);
        particleAfterTypeConversion.print();
        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // BIRTH
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("BIRTH");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 5; // BIRTH
        derivedFromElmtlRkId = 0;



        educts = new HashMap();
        educts.put(particleAfterTypeConversion, new ExtendedIdAndType(particleAfterTypeConversion));


        products = new ArrayList();
        products.add(new ExtendedIdAndType(particleAfterTypeConversion));
        products.add(new ExtendedIdAndType(false, -1, 1));

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(1, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("bornPaticle:");
        IParticle bornParticle = createdParticles.get(0);

        bornParticle.print();
        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore + 1, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // DEATH
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("DEATH");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 6; // death
        derivedFromElmtlRkId = 0;

        System.out.println("deathEducts:");
        particleAfterTypeConversion.print();
        bornParticle.print();
        System.out.println("deathProducts:");
        ExtendedIdAndType deathProduct = new ExtendedIdAndType(particleAfterTypeConversion);
        deathProduct.print();

        educts = new HashMap();
        educts.put(particleAfterTypeConversion, new ExtendedIdAndType(particleAfterTypeConversion));
        educts.put(bornParticle, new ExtendedIdAndType(bornParticle));


        products = new ArrayList();
        products.add(deathProduct);


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(1, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());

        System.out.println("diedParticle:");
        IParticle diedParticle = removedParticles.get(0);

        diedParticle.print();
        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore - 1, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // FISSION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("FISSION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);

        /*
        Iterator<IParticle> particleIterator = particleConfiguration.particleIterator();
        while(particleIterator.hasNext()){
            particleIterator.next().print();
        }
        */
        

        rkId = 0;
        rkTypeId = 7; // fission
        derivedFromElmtlRkId = 0;



        educts = new HashMap();
        educts.put(particleAfterTypeConversion, new ExtendedIdAndType(particleAfterTypeConversion));



        products = new ArrayList();
        products.add(new ExtendedIdAndType(false, -1, 0));
        products.add(new ExtendedIdAndType(false, -1, 0));


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(1, removedParticles.size());
        assertEquals(2, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());


        IParticle cleavedParticle = removedParticles.get(0);
        IParticle fissionProduct1 = createdParticles.get(0);
        IParticle fissionProduct2 = createdParticles.get(1);

        System.out.println("cleavedParticle:");
        cleavedParticle.print();
        System.out.println("both fission products:");
        fissionProduct1.print();
        fissionProduct2.print();
        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore + 1, nParticlesAfter);
        
        
        
        // ---------------------------------------------------------------------------
        // FISSION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("FISSION, in crowded teritory");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);

        
        

        rkId = 0;
        rkTypeId = 7; // fission
        derivedFromElmtlRkId = 0;



        educts = new HashMap();
        educts.put(particleConfiguration.getParticle(0), new ExtendedIdAndType(particleAfterTypeConversion));



        products = new ArrayList();
        products.add(new ExtendedIdAndType(false, -1, 0));
        products.add(new ExtendedIdAndType(false, -1, 0));


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());


        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // FUSION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("FUSION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 8; // fusion
        derivedFromElmtlRkId = 0;



        educts = new HashMap();
        educts.put(fissionProduct1, new ExtendedIdAndType(fissionProduct1));
        educts.put(fissionProduct2, new ExtendedIdAndType(fissionProduct2));



        products = new ArrayList();
        products.add(new ExtendedIdAndType(cleavedParticle));


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(2, removedParticles.size());
        assertEquals(1, createdParticles.size());
        assertEquals(0, typeChangedParticles.size());
        assertEquals(0, typeChangeParticles_from.size());
        assertEquals(0, typeChangeParticles_to.size());


        IParticle fusionEduct1 = removedParticles.get(0);
        IParticle fusionEduct2 = removedParticles.get(1);
        IParticle fusionProduct = createdParticles.get(0);

        System.out.println("removed fusion educts:");
        fusionEduct1.print();
        fusionEduct2.print();
        System.out.println("fusion product:");
        fusionProduct.print();

        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore - 1, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // ENZYMATIC
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("ENZYMATIC");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 9; // ENZYMATIC
        derivedFromElmtlRkId = 0;

        IParticle enzyme = particleConfiguration.getParticle(1);
        System.out.println("enzyme:");
        enzyme.print();
        System.out.println("ligand:");
        fusionProduct.print();

        educts = new HashMap();
        educts.put(fusionProduct, new ExtendedIdAndType(fissionProduct1));
        educts.put(enzyme, new ExtendedIdAndType(fissionProduct2));



        products = new ArrayList();
        products.add(new ExtendedIdAndType(enzyme));
        products.add(new ExtendedIdAndType(false, -1, 1));


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(1, typeChangedParticles.size());
        assertEquals(1, typeChangeParticles_from.size());
        assertEquals(1, typeChangeParticles_to.size());


        IParticle enzymaticTypeChanged = typeChangedParticles.get(0);
        int typeChangeFrom = typeChangeParticles_from.get(0);
        int typeChangeTo = typeChangeParticles_to.get(0);

        System.out.println("typeChanged enzymeLigand:");
        enzymaticTypeChanged.print();

        System.out.println("typeChange from: " + typeChangeFrom + " to: " + typeChangeTo);


        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore, nParticlesAfter);


        // ---------------------------------------------------------------------------
        // DOUBLE TYPE CONVERSION
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("DOUBLE TYPE CONVERSION");
        System.out.println("---------------------------------------------------------------------------");


        nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before: " + nParticlesBefore);


        rkId = 0;
        rkTypeId = 10; // DOUBLE TYPE CONVERSION
        derivedFromElmtlRkId = 0;


        System.out.println("educts:");
        enzymaticTypeChanged.print();
        enzyme.print();


        educts = new HashMap();
        educts.put(enzymaticTypeChanged, new ExtendedIdAndType(fissionProduct1));
        educts.put(enzyme, new ExtendedIdAndType(fissionProduct2));


        ExtendedIdAndType product1 = new ExtendedIdAndType(false, -1, 2);
        ExtendedIdAndType product2 = new ExtendedIdAndType(false, -1, 1);
        System.out.println("products:");
        product1.print();
        product2.print();


        products = new ArrayList();
        products.add(product1);
        products.add(product2);


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardParticleBasedRkExecutor.executeReaction(0, executableReaction);
        removedParticles = report.getRemovedParticles();
        createdParticles = report.getCreatedParticles();
        typeChangedParticles = report.getTypeChangedParticles();
        typeChangeParticles_from = report.getTypeChangeParticles_from();
        typeChangeParticles_to = report.getTypeChangeParticles_to();

        assertEquals(0, removedParticles.size());
        assertEquals(0, createdParticles.size());
        assertEquals(2, typeChangedParticles.size());
        assertEquals(2, typeChangeParticles_from.size());
        assertEquals(2, typeChangeParticles_to.size());


        IParticle enzymaticTypeChanged0 = typeChangedParticles.get(0);
        IParticle enzymaticTypeChanged1 = typeChangedParticles.get(1);
        int typeChangeFrom0 = typeChangeParticles_from.get(0);
        int typeChangeFrom1 = typeChangeParticles_from.get(1);
        int typeChangeTo0 = typeChangeParticles_to.get(0);
        int typeChangeTo1 = typeChangeParticles_to.get(1);

        System.out.println("typeChanged enzymeLigands:");
        enzymaticTypeChanged0.print();
        enzymaticTypeChanged1.print();

        System.out.println("typeChange from: " + typeChangeFrom0 + " to: " + typeChangeTo0);
        System.out.println("typeChange from: " + typeChangeFrom1 + " to: " + typeChangeTo1);


        assertEquals(typeIdToConvertInto, particleTypeAfterConversion);

        nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after: " + nParticlesAfter);
        assertEquals(nParticlesBefore, nParticlesAfter);

    }
}
