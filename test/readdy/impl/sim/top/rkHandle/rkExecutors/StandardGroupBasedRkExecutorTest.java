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
import readdy.impl.assembly.GroupInteriorParticlePositionerFactory;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.assembly.IGroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.assembly.IStandardGroupBasedRkExecutorFactory;
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
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
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
import readdy.impl.assembly.StandardGroupBasedRkExecutorFactory;
import readdy.impl.disassembly.GroupDisassembler;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.io.in.tpl_group.TplgyGroupsFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.top.group.ExtendedIdAndType;
import readdy.impl.sim.top.rkHandle.ExecutableReaction;

/**
 *
 * @author schoeneberg
 */
public class StandardGroupBasedRkExecutorTest {

    private static IReactionExecutor standardGroupBasedRkExecutor;
    // test suite
    private static IParticleConfiguration particleConfiguration;
    private static IGroupConfiguration groupConfiguration;
    private static IPotentialManager potentialManager;

    public StandardGroupBasedRkExecutorTest() {
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

        String paramGroupsfilename = "./test/testInputFiles/test_group/param_groups.xml";
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


        //##############################################################################
        // create GroupInteriorParticlePositioner
        //##############################################################################

        IGroupInteriorParticlePositionerFactory groupInteriorParticlePositionerFactory = new GroupInteriorParticlePositionerFactory();
        groupInteriorParticlePositionerFactory.set_groupParameters(groupParameters);
        IGroupInteriorParticlePositioner particlePositioner = groupInteriorParticlePositionerFactory.createGroupInteriorParticlePositioner();

        //##############################################################################
        // create standardGroupBasedRkExecutor
        //##############################################################################

        IStandardGroupBasedRkExecutorFactory standardGroupBasedRkExecutorFactory = new StandardGroupBasedRkExecutorFactory();
        standardGroupBasedRkExecutorFactory.set_groupInteriorParticlePositioner(particlePositioner);
        standardGroupBasedRkExecutor = standardGroupBasedRkExecutorFactory.createStandardGroupBasedRkExecutor();


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
     * Test of setup method, of class StandardGroupBasedRkExecutor.
     */
    @Test
    public void testSetup() {
        System.out.println("setup");

        standardGroupBasedRkExecutor.setup(particleConfiguration, groupConfiguration, potentialManager);
        // this method has actually no output but if it fails intrinsically one would see it here.
    }

    /**
     * Test of executeReaction method, of class StandardGroupBasedRkExecutor.
     */
    @Test
    public void testExecuteReaction() {

        System.out.println("executeReaction");

        // ---------------------------------------------------------------------------
        // GROUP
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GROUP");
        System.out.println("---------------------------------------------------------------------------");


        int nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        int rkId = 0;
        int rkTypeId = 100; // GROUP
        int derivedFromElmtlRkId = 0;

        HashMap<IParticle, IExtendedIdAndType> educts = new HashMap();
        IParticle e_particle0 = new Particle(10, 0, new double[]{0, 0, 0});    // A
        IParticle e_particle1 = new Particle(11, 0, new double[]{0, 0, 0});    // A
        educts.put(e_particle0, new ExtendedIdAndType(e_particle0));
        educts.put(e_particle1, new ExtendedIdAndType(e_particle1));

        IExtendedIdAndType p_extIdAndType0 = new ExtendedIdAndType(true, 0, 0); // A dimer
        ArrayList<IExtendedIdAndType> products = new ArrayList();
        products.add(p_extIdAndType0);

        IExecutableReaction executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        IReactionExecutionReport report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);
        ArrayList<IGroup> removedGroups = report.getRemovedGroups();
        ArrayList<IGroup> createdGroups = report.getCreatedGroups();
        ArrayList<IGroup> typeChangedGroups = report.getTypeChangedGroups();
        ArrayList<Integer> typeChangeGroups_from = report.getTypeChangeGroups_from();
        ArrayList<Integer> typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(0, removedGroups.size());
        assertEquals(1, createdGroups.size());
        assertEquals(0, typeChangedGroups.size());
        assertEquals(0, typeChangeGroups_from.size());
        assertEquals(0, typeChangeGroups_to.size());

        System.out.println("created Group:");
        IGroup createdGroupInCreation = createdGroups.get(0);
        createdGroupInCreation.print();

        int nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore + 1, nGroupsAfter);



        // ---------------------------------------------------------------------------
        // UNGROUP
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("UNGROUP");
        System.out.println("---------------------------------------------------------------------------");


        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 101; // UNGROUP
        derivedFromElmtlRkId = 0;

        educts = new HashMap();
        e_particle0 = new Particle(10, 0, new double[]{0, 0, 0});    // rhodopsin -> same dime
        e_particle1 = new Particle(11, 0, new double[]{0, 0, 0});    // rhodopsin -> same dime
        educts.put(e_particle0, new ExtendedIdAndType(createdGroupInCreation));
        educts.put(e_particle1, new ExtendedIdAndType(createdGroupInCreation));

        p_extIdAndType0 = new ExtendedIdAndType(e_particle0); // rhodopsin
        IExtendedIdAndType p_extIdAndType1 = new ExtendedIdAndType(e_particle0); // rhodopsin
        products = new ArrayList();
        products.add(p_extIdAndType0);
        products.add(p_extIdAndType1);

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(1, removedGroups.size());
        assertEquals(0, createdGroups.size());
        assertEquals(0, typeChangedGroups.size());
        assertEquals(0, typeChangeGroups_from.size());
        assertEquals(0, typeChangeGroups_to.size());

        System.out.println("removed Group:");
        IGroup removedGroupDuringUngroup = removedGroups.get(0);
        removedGroupDuringUngroup.print();

        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore - 1, nGroupsAfter);



        // ---------------------------------------------------------------------------
        // GTypeConversion
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GTypeConversion");
        System.out.println("---------------------------------------------------------------------------");
        // we want to convert the A_DIMER to a A_DIMER_dummy

        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 102; // GTypeConversion
        derivedFromElmtlRkId = 0;

        IGroup groupToChangeType = groupConfiguration.getGroup(1); // group id 1 is the dimer

        educts = new HashMap();
        e_particle0 = new Particle(4, 0, new double[]{0, 0, 0});    // A -> in group 1
        e_particle1 = new Particle(5, 0, new double[]{0, 0, 0});    // A -> in group 1
        educts.put(e_particle0, new ExtendedIdAndType(groupToChangeType));
        educts.put(e_particle1, new ExtendedIdAndType(groupToChangeType));

        p_extIdAndType0 = new ExtendedIdAndType(true, -1, 2); // specialized group to convert to

        products = new ArrayList();
        products.add(p_extIdAndType0);


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(0, removedGroups.size());
        assertEquals(0, createdGroups.size());
        assertEquals(1, typeChangedGroups.size());
        assertEquals(1, typeChangeGroups_from.size());
        assertEquals(1, typeChangeGroups_to.size());

        System.out.println("changed Group:");
        IGroup changedGroupDuringTypeConversion = typeChangedGroups.get(0);
        changedGroupDuringTypeConversion.print();

        int expTypeChangeFrom = 0;
        int expTypeChangeTo = 2;
        int resTypeChangeFrom = typeChangeGroups_from.get(0);
        int resTypeChangeTo = typeChangeGroups_to.get(0);
        assertEquals(expTypeChangeFrom, resTypeChangeFrom, 0.0001);
        assertEquals(expTypeChangeTo, resTypeChangeTo, 0.0001);
        System.out.println("typeConversion from " + resTypeChangeFrom + " to " + resTypeChangeTo);

        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore, nGroupsAfter);



        // ---------------------------------------------------------------------------
        // GFission
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GFission");
        System.out.println("---------------------------------------------------------------------------");


        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 103; // GFission
        derivedFromElmtlRkId = 0;

        IGroup groupToBreak = groupConfiguration.getGroup(0); // tetramer

        educts = new HashMap();
        e_particle0 = new Particle(0, 0, new double[]{0, 0, 0});    // rhodopsin -> in group 0 (tetramer)
        e_particle1 = new Particle(1, 0, new double[]{0, 0, 0});    // rhodopsin -> in group 0 (tetramer)
        educts.put(e_particle0, new ExtendedIdAndType(groupToBreak));
        educts.put(e_particle1, new ExtendedIdAndType(groupToBreak));

        p_extIdAndType0 = new ExtendedIdAndType(true, -1, 0); // dimer
        p_extIdAndType1 = new ExtendedIdAndType(true, -1, 0); // dimer

        products = new ArrayList();
        products.add(p_extIdAndType0);
        products.add(p_extIdAndType1);


        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(1, removedGroups.size());
        assertEquals(0, createdGroups.size());
        assertEquals(0, typeChangedGroups.size());
        assertEquals(0, typeChangeGroups_from.size());
        assertEquals(0, typeChangeGroups_to.size());

        System.out.println("removedGroups");
        IGroup removedGroupDuringFission = removedGroups.get(0);
        removedGroupDuringFission.print();


        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore - 1, nGroupsAfter);


        // ---------------------------------------------------------------------------
        // GFusion
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GFusion");
        System.out.println("---------------------------------------------------------------------------");

        // first create a group to fuse an other one with
        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 100; // GROUP
        derivedFromElmtlRkId = 0;

        educts = new HashMap();
        e_particle0 = new Particle(1000, 0, new double[]{1.68069, -0.502364, 0.});    // A
        e_particle1 = new Particle(1001, 0, new double[]{-0.675995, 1.00961, 0.});    // A
        educts.put(e_particle0, new ExtendedIdAndType(e_particle0));
        educts.put(e_particle1, new ExtendedIdAndType(e_particle1));

        p_extIdAndType0 = new ExtendedIdAndType(true, 0, 0); // A dimer
        products = new ArrayList();
        products.add(p_extIdAndType0);

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);
        report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);

        IGroup newGroup = report.getCreatedGroups().get(0);
        int newGroupId = newGroup.get_id();
        System.out.println("generated new group with id " + newGroupId);
        newGroup.print();

        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        System.out.println("now start with the fusion");
        // now start with the fusion
        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 104; // GFusion
        derivedFromElmtlRkId = 0;

        IGroup groupToFuse0 = groupConfiguration.getGroup(1); // dimer
        IGroup groupToFuse1 = groupConfiguration.getGroup(3); // just now created dimer

        educts = new HashMap();
        e_particle0 = groupToFuse0.get_positionedMemberParticles().get(0);
        e_particle1 = groupToFuse1.get_positionedMemberParticles().get(0);
        educts.put(e_particle0, new ExtendedIdAndType(groupToFuse0));
        educts.put(e_particle1, new ExtendedIdAndType(groupToFuse1));

        p_extIdAndType0 = new ExtendedIdAndType(true, -1, 1); // Tetramer


        products = new ArrayList();
        products.add(p_extIdAndType0);



        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0, executableReaction);
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(0, removedGroups.size());
        assertEquals(1, createdGroups.size());
        assertEquals(0, typeChangedGroups.size());
        assertEquals(0, typeChangeGroups_from.size());
        assertEquals(0, typeChangeGroups_to.size());

        System.out.println("created groups");
        IGroup createdGroupDuringFusion = createdGroups.get(0);
        createdGroupDuringFusion.print();


        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore + 1, nGroupsAfter);


   
        // ---------------------------------------------------------------------------
        // GEnzymatic
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GEnzymatic");
        System.out.println("---------------------------------------------------------------------------");


        //------------
        // first create a group to fuse an other one with
        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 100; // GROUP
        derivedFromElmtlRkId = 0;

        //generate a dummy tetramer
        System.out.println("generate ligand tetramer");
        educts = new HashMap();
        e_particle0 = new Particle(2000, 0, new double[]{0, 0, 0.});    // A
        e_particle1 = new Particle(2001, 0, new double[]{0, 0, 0.});    // A
        IParticle e_particle2 = new Particle(2002, 0, new double[]{0, 0, 0.});    // A
        IParticle e_particle3 = new Particle(2003, 0, new double[]{0, 0, 0.});    // A
        educts.put(e_particle0, new ExtendedIdAndType(e_particle0));
        educts.put(e_particle1, new ExtendedIdAndType(e_particle1));
        educts.put(e_particle2, new ExtendedIdAndType(e_particle2));
        educts.put(e_particle3, new ExtendedIdAndType(e_particle3));

        p_extIdAndType0 = new ExtendedIdAndType(true, 0, 4); // A ligand tetramer
        products = new ArrayList();
        products.add(p_extIdAndType0);

        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);
        report = standardGroupBasedRkExecutor.executeReaction(0,executableReaction);

        newGroup = report.getCreatedGroups().get(0);
        newGroupId = newGroup.get_id();
        System.out.println("generated new group with id "+newGroupId);
        newGroup.print();

        //------------


        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);
        System.out.println("current Group Config Status");
        Iterator<IGroup> gIter = groupConfiguration.groupIterator();
        while(gIter.hasNext()){
        gIter.next().print();
        }

        rkId = 0;
        rkTypeId = 105; // GFusion
        derivedFromElmtlRkId = 0;

        IGroup groupEnzyme = groupConfiguration.getGroup(4); // tetramer
        IGroup groupLigand = newGroup; // tetramer ligand


        expTypeChangeFrom = groupLigand.get_typeId();
        expTypeChangeTo =  3; // tetramer_Dummy

        educts = new HashMap();
        e_particle0 = groupEnzyme.get_positionedMemberParticles().get(0);
        e_particle1 = groupLigand.get_positionedMemberParticles().get(0);
        educts.put(e_particle0, new ExtendedIdAndType(groupEnzyme));
        educts.put(e_particle1, new ExtendedIdAndType(groupLigand));

        p_extIdAndType0 = new ExtendedIdAndType(true,-1,expTypeChangeTo); // testTetramer
        p_extIdAndType1 = new ExtendedIdAndType(groupEnzyme); // enzymw


        products = new ArrayList();
        products.add(p_extIdAndType0);
        products.add(p_extIdAndType1);



        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0,executableReaction);
        report.print();
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(0, removedGroups.size());
        assertEquals(0, createdGroups.size());
        assertEquals(1, typeChangedGroups.size());
        assertEquals(1, typeChangeGroups_from.size());
        assertEquals(1, typeChangeGroups_to.size());

        System.out.println("ligand typeChange");
        IGroup groupTypeChangeDuringEnzymatic = typeChangedGroups.get(0);
        groupTypeChangeDuringEnzymatic.print();



        resTypeChangeFrom = typeChangeGroups_from.get(0);
        resTypeChangeTo =  typeChangeGroups_to.get(0);
        assertEquals(expTypeChangeFrom,resTypeChangeFrom,0.0001);
        assertEquals(expTypeChangeTo,resTypeChangeTo,0.0001);
        System.out.println("typeConversion from "+resTypeChangeFrom+" to "+resTypeChangeTo);

        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore , nGroupsAfter);

     
        // ---------------------------------------------------------------------------
        // GDoubleTypeConversion
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("GDoubleTypeConversion");
        System.out.println("---------------------------------------------------------------------------");


        nGroupsBefore = groupConfiguration.getNGroups();
        System.out.println("nGroups before: " + nGroupsBefore);

        rkId = 0;
        rkTypeId = 106; // GDoubleTypeConversion
        derivedFromElmtlRkId = 0;

        IGroup typeConversionEduct0 = groupConfiguration.getGroup(4); // tetramer
        IGroup typeConversionEduct1 = groupConfiguration.getGroup(5); // tetramer dimer


        int expTypeChangeFrom0 = typeConversionEduct0.get_typeId();
        int expTypeChangeTo0 =  3; // tetramer to teramer_Dummy

        int expTypeChangeFrom1 = typeConversionEduct1.get_typeId();
        int expTypeChangeTo1 =  1; // tetramer_dummy to tetramer

        educts = new HashMap();
        e_particle0 = typeConversionEduct0.get_positionedMemberParticles().get(0);
        e_particle1 = typeConversionEduct1.get_positionedMemberParticles().get(0);
        educts.put(e_particle0, new ExtendedIdAndType(typeConversionEduct0));
        educts.put(e_particle1, new ExtendedIdAndType(typeConversionEduct1));

        p_extIdAndType0 = new ExtendedIdAndType(true,typeConversionEduct0.get_id(),expTypeChangeTo0);
        p_extIdAndType1 = new ExtendedIdAndType(true,typeConversionEduct1.get_id(),expTypeChangeTo1);


        products = new ArrayList();
        products.add(p_extIdAndType0);
        products.add(p_extIdAndType1);



        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        report = standardGroupBasedRkExecutor.executeReaction(0,executableReaction);
        removedGroups = report.getRemovedGroups();
        createdGroups = report.getCreatedGroups();
        typeChangedGroups = report.getTypeChangedGroups();
        typeChangeGroups_from = report.getTypeChangeGroups_from();
        typeChangeGroups_to = report.getTypeChangeGroups_to();

        assertEquals(0, removedGroups.size());
        assertEquals(0, createdGroups.size());
        assertEquals(2, typeChangedGroups.size());
        assertEquals(2, typeChangeGroups_from.size());
        assertEquals(2, typeChangeGroups_to.size());

        System.out.println("double typeChange");
        IGroup typeChangeProduct0 = typeChangedGroups.get(0);
        IGroup typeChangeProduct1 = typeChangedGroups.get(1);
        typeChangeProduct0.print();
        typeChangeProduct1.print();

        report.print();


        System.out.println("typeConversion from "+expTypeChangeFrom0+" to "+groupConfiguration.getGroup(4).get_typeId());
        assertEquals(expTypeChangeTo0,groupConfiguration.getGroup(4).get_typeId(),0.0001); //3
        

        System.out.println("typeConversion from "+expTypeChangeFrom1+" to "+groupConfiguration.getGroup(5).get_typeId());
        assertEquals(expTypeChangeTo1,groupConfiguration.getGroup(5).get_typeId(),0.0001); //1
        

        nGroupsAfter = groupConfiguration.getNGroups();
        System.out.println("nGroups after: " + nGroupsAfter);

        assertEquals(nGroupsBefore , nGroupsAfter);


         
        // ---------------------------------------------------------------------------
        // GDoubleTypeConversion
        // ---------------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Final GroupConfiguration ...");
        System.out.println("---------------------------------------------------------------------------");
        Iterator<IGroup> groupIterator = groupConfiguration.groupIterator();
        while (groupIterator.hasNext()) {
            groupIterator.next().print();
        }
    }
}
