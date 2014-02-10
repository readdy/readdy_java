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
package readdy.impl.sim.top.group;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupParameters;
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

/**
 *
 * @author schoeneberg
 */
public class GroupConfigurationTest {

    private static ITplgyGroupsFileData tplgyGroupsFileData;
    private static IGroupDisassembler groupDisassembler;
    private static IGroupFactory groupFactory;
    private static IGroupParameters groupParameters;
    private static IGroupConfigurationFactory groupConfigurationFactory;
    private static IGroupConfiguration groupConfig;
    private static IParticleConfiguration particleConfig;

    public GroupConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


        //##############################################################################
        // group Topology
        //##############################################################################

        String filename = "./test/testInputFiles/test_default/tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
        tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();


        //##############################################################################
        // potentialManager
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

        IPotentialManager potentialManager = potentialManagerFactory.createPotentialManager();

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
        particleConfig = configFactory.createParticleConfiguration();


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
        groupParameters = groupParametersFactory.createGroupParameters();


        //##############################################################################
        // put it all together
        //##############################################################################

        groupFactory = new GroupFactory();
        groupFactory.set_potentialManager(potentialManager);
        groupFactory.set_groupParameters(groupParameters);

        groupDisassembler = new GroupDisassembler();
        groupDisassembler.set_potentialManager(potentialManager);
        groupDisassembler.set_groupParameters(groupParameters);

        groupConfigurationFactory = new GroupConfigurationFactory();
        groupConfigurationFactory.set_tplgyGroupsFileData(tplgyGroupsFileData);
        groupConfigurationFactory.set_groupFactory(groupFactory);
        groupConfigurationFactory.set_groupDisassembler(groupDisassembler);
        groupConfigurationFactory.set_groupParameters(groupParameters);
        groupConfigurationFactory.set_particleConfiguration(particleConfig);

        groupConfig = groupConfigurationFactory.createGroupConfiguration();

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
     * Test of getGroup method, of class GroupConfiguration.
     */
    @Test
    public void testGetGroup() {
        System.out.println("getGroup");

        //########################################################################
        // test the A-Dimer, consisting of particle ID 4 and 5
        int expGroupId = 1;
        int expGroupType = 0;
        ArrayList<IParticle> expPositionedMemberParticles = new ArrayList();
        expPositionedMemberParticles.add(new Particle(4, 0, new double[]{0, 0, 0}));
        expPositionedMemberParticles.add(new Particle(5, 0, new double[]{0, 0, 0}));
        System.out.println("testing group id " + expGroupId + " ...");

        IGroup result = groupConfig.getGroup(expGroupId);
        assertEquals(expGroupId, result.get_id());
        assertEquals(expGroupType, result.get_typeId());
        ArrayList<IParticle> resPositionedMemberParticles = result.get_positionedMemberParticles();
        assertEquals(expPositionedMemberParticles.size(), resPositionedMemberParticles.size());
        for (int internalPosition = 0; internalPosition < expPositionedMemberParticles.size(); internalPosition++) {

            assertEquals(expPositionedMemberParticles.get(internalPosition).get_id(), resPositionedMemberParticles.get(internalPosition).get_id());
        }


        //########################################################################
        // testing the tetramer group, consisting of particle IDs 0,1,2,3

        expGroupId = 0;
        expGroupType = 1;
        expPositionedMemberParticles = new ArrayList();
        expPositionedMemberParticles.add(new Particle(0, 0, new double[]{0, 0, 0}));
        expPositionedMemberParticles.add(new Particle(1, 0, new double[]{0, 0, 0}));
        expPositionedMemberParticles.add(new Particle(2, 0, new double[]{0, 0, 0}));
        expPositionedMemberParticles.add(new Particle(3, 0, new double[]{0, 0, 0}));
        System.out.println("testing group id " + expGroupId + " ...");

        result = groupConfig.getGroup(expGroupId);
        assertEquals(expGroupId, result.get_id());
        assertEquals(expGroupType, result.get_typeId());
        resPositionedMemberParticles = result.get_positionedMemberParticles();
        assertEquals(expPositionedMemberParticles.size(), resPositionedMemberParticles.size());
        for (int internalPosition = 0; internalPosition < expPositionedMemberParticles.size(); internalPosition++) {

            assertEquals(expPositionedMemberParticles.get(internalPosition).get_id(), resPositionedMemberParticles.get(internalPosition).get_id());

        }
    }

    /**
     * Test of createGroup method, of class GroupConfiguration.
     */
    @Test
    public void testCreateGroup() {
        int expGroupId = 2;
        int expGroupType = 0;
        ArrayList<IParticle> expPositionedMemberParticles = new ArrayList();
        expPositionedMemberParticles.add(new Particle(0, 5, new double[]{0, 0, 0}));
        expPositionedMemberParticles.add(new Particle(1, 6, new double[]{0, 0, 0}));
        System.out.println("testing creation of group with id " + expGroupId + " ...");

        int expNGroupsBefore = 2;
        int expNGroupAfter = 3;
        assertEquals(expNGroupsBefore, groupConfig.getNGroups());

        groupConfig.createGroup(expGroupType, expPositionedMemberParticles);
        assertEquals(expNGroupAfter, groupConfig.getNGroups());

        IGroup result = groupConfig.getGroup(expGroupId);
        assertEquals(expGroupId, result.get_id());
        assertEquals(expGroupType, result.get_typeId());
        ArrayList<IParticle> resPositionedMemberParticles = result.get_positionedMemberParticles();
        assertEquals(expPositionedMemberParticles.size(), resPositionedMemberParticles.size());
        for (int internalPosition = 0; internalPosition < expPositionedMemberParticles.size(); internalPosition++) {

            assertEquals(expPositionedMemberParticles.get(internalPosition).get_id(), resPositionedMemberParticles.get(internalPosition).get_id());

        }

    }

    /**
     * Test of removeGroup method, of class GroupConfiguration.
     */
    @Test
    public void testRemoveGroup() {
        System.out.println("removeGroup");
        int expGroupId = 2;
        int expNGroupsBefore = 3;
        int expNGroupAfter = 2;
        assertEquals(expNGroupsBefore, groupConfig.getNGroups());
        System.out.println("testing removal of group with id " + expGroupId + " ...");
        groupConfig.removeGroup(expGroupId);
        assertEquals(expNGroupAfter, groupConfig.getNGroups());
    }

    /**
     * Test of removeGroup method, of class GroupConfiguration.
     */
    @Test(expected = java.lang.RuntimeException.class)
    public void testRemoveGroup_Exception() {
        System.out.println("removeGroup exceptionExptected");
        int expGroupId = 2;
        int expNGroupsBefore = 2;
        int expNGroupAfter = 2;
        assertEquals(expNGroupsBefore, groupConfig.getNGroups());
        System.out.println("testing removal of group with id " + expGroupId + " ...");
        groupConfig.removeGroup(expGroupId);
        assertEquals(expNGroupAfter, groupConfig.getNGroups());
    }

    /**
     * Test of changeGroupType method, of class GroupConfiguration.
     */
    @Test
    public void testChangeGroupType() {
        System.out.println("changeGroupType");
        int groupIdToChange = 1;
        int groupIdFrom = 0;
        int groupIdTo = 0;
        IGroup groupBefore = groupConfig.getGroup(groupIdToChange);
        assertEquals(groupIdFrom, groupBefore.get_typeId());
        groupConfig.changeGroupType(groupIdToChange, groupIdFrom, groupIdTo);
        IGroup groupAfter = groupConfig.getGroup(groupIdToChange);
        assertEquals(groupIdTo, groupAfter.get_typeId());

    }

    /**
     * Test of changeGroupType method, of class GroupConfiguration.
     */
    @Test(expected = java.lang.RuntimeException.class)
    public void testChangeGroupType_expectException() {
        System.out.println("testChangeGroupType_expectException");
        int groupIdToChange = 1;
        int groupIdFrom = 0;
        int groupIdTo = 1;
        IGroup groupBefore = groupConfig.getGroup(groupIdToChange);
        assertEquals(groupIdFrom, groupBefore.get_typeId());
        groupConfig.changeGroupType(groupIdToChange, groupIdFrom, groupIdTo);
        IGroup groupAfter = groupConfig.getGroup(groupIdToChange);
        assertEquals(groupIdTo, groupAfter.get_typeId());

    }

    /**
     * Test of getCurrNAssignmentsOfGroupType method, of class GroupConfiguration.
     */
    @Test
    public void testGetCurrNAssignmentsOfGroupType() {
        System.out.println("getCurrNAssignmentsOfGroupType");

        int groupTypeIdAssignedTo = 0; // dimer
        int particleIdToCheck = 4;

        int nAssignments = groupConfig.getCurrNAssignmentsOfGroupType(particleIdToCheck, groupTypeIdAssignedTo);
        int expNAssignments = 1;
        assertEquals(expNAssignments, nAssignments);

        groupTypeIdAssignedTo = 0;
        particleIdToCheck = 999;

        nAssignments = groupConfig.getCurrNAssignmentsOfGroupType(particleIdToCheck, groupTypeIdAssignedTo);
        expNAssignments = 0;
        assertEquals(expNAssignments, nAssignments);


    }

    /**
     * Test of getGroupsWhereParticleIsInvolved method, of class GroupConfiguration.
     */
    @Test
    public void testGetGroupsWhereParticleIsInvolved() {
        System.out.println("getGroupsWhereParticleIsInvolved");

        int groupTypeIdAssignedTo = 0; // dimer
        int particleIdToCheck = 5;
        ArrayList<IExtendedIdAndType> expectedAssignedGroups = new ArrayList();
        expectedAssignedGroups.add(new ExtendedIdAndType(true, 1, 0));

        ArrayList<IExtendedIdAndType> assignedGroups = groupConfig.getGroupsWhereParticleIsInvolved(particleIdToCheck, groupTypeIdAssignedTo);

        assertEquals(expectedAssignedGroups.size(), assignedGroups.size());
        ArrayList<IExtendedIdAndType> stillToFind = expectedAssignedGroups;
        while (!stillToFind.isEmpty()) {
            boolean found = false;
            IExtendedIdAndType toFind = stillToFind.remove(0);
            for (IExtendedIdAndType possibleMatch : assignedGroups) {
                found = possibleMatch.get_isGroup() == toFind.get_isGroup()
                        && possibleMatch.get_id() == toFind.get_id()
                        && possibleMatch.get_type() == toFind.get_type();
                if (found == true) {
                    break;
                }
            }
            assertEquals(true, found);
        }
    }

    /**
     * Test of getAllGroupsWhereParticleIsInvolvedIn method, of class GroupConfiguration.
     */
    @Test
    public void testGetAllGroupsWhereParticleIsInvolvedIn() {
        System.out.println("getAllGroupsWhereParticleIsInvolvedIn");

        int particleIdToCheck = 2;
        // one reads this as follows:
        // extendedIdAndType: true, 0, 1
        // true -> its a group, false -> its a particle
        // 0 -> its the respective group or particle Id
        // 1 -> its the respective group or particle type
        // true, 0, 1 means for that reason:
        // group, group ID 0, group type 1 (tetramer in this particular test case)
        ArrayList<IExtendedIdAndType> expectedAssignedGroups = new ArrayList();
        expectedAssignedGroups.add(new ExtendedIdAndType(true, 0, 1));

        ArrayList<IExtendedIdAndType> assignedGroups = groupConfig.getAllGroupsWhereParticleIsInvolvedIn(particleIdToCheck);

        for (IExtendedIdAndType x : assignedGroups) {
            System.out.print("assignedGroups: ");
            x.print();
        }

        assertEquals(expectedAssignedGroups.size(), assignedGroups.size());
        ArrayList<IExtendedIdAndType> stillToFind = expectedAssignedGroups;
        while (!stillToFind.isEmpty()) {
            boolean found = false;
            IExtendedIdAndType toFind = stillToFind.remove(0);
            for (IExtendedIdAndType possibleMatch : assignedGroups) {

                found = possibleMatch.get_isGroup() == toFind.get_isGroup()
                        && possibleMatch.get_id() == toFind.get_id()
                        && possibleMatch.get_type() == toFind.get_type();
                if (found == true) {
                    break;
                }
            }
            assertEquals(true, found);
        }
    }
}
