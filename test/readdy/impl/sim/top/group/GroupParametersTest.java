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

import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.api.assembly.IGroupParametersFactory;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author schoeneberg
 */
public class GroupParametersTest {

    private static IPotentialInventory potentialInventory;
    private static IParamGroupsFileData paramGroupsFileData;
    private static IParticleParameters particleParameters;
    private static IGroupParameters groupParameters;

    public GroupParametersTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        //##############################################################################
        String filename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(filename);
        paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();

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
        particleParameters = particleParamFactory.createParticleParameters();

        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        potentialInventory = potInvFactory.createPotentialInventory();

        //##############################################################################

        IGroupParametersFactory groupParametersFactory = new GroupParametersFactory();
        groupParametersFactory.set_paramGroupsFileData(paramGroupsFileData);
        groupParametersFactory.set_particleParameters(particleParameters);
        groupParametersFactory.set_potentialInventory(potentialInventory);
        groupParameters = groupParametersFactory.createGroupParameters();
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
     * Test of getGroupInternalIds method, of class GroupParameters.
     */
    @Test
    public void testGetGroupInternalIds() {
        System.out.println("getGroupInternalIds");
        int[] groupTypeIds = new int[]{0, 1};

        int[][] expResults = new int[][]{new int[]{0, 1}, new int[]{0, 1, 2, 3}};
        for (int type : groupTypeIds) {
            ArrayList<Integer> result = groupParameters.getGroupInternalIds(groupTypeIds[type]);
            int[] expResult = expResults[type];
            for (int i : result) {
                assertEquals(true, result.contains(expResult[i]));
            }
        }
    }

    /**
     * Test of getNumberOfGroupMembers method, of class GroupParameters.
     */
    @Test
    public void testGetNumberOfGroupMembers() {
        int[] groupTypeIds = new int[]{0, 1};
        int[] expResults = new int[]{2, 4};
        for (int type : groupTypeIds) {
            int result = groupParameters.getNumberOfGroupMembers(groupTypeIds[type]);
            int expResult = expResults[type];
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getMaxNumberOfAssignmentsPerParticle method, of class GroupParameters.
     */
    @Test
    public void testGetMaxNumberOfAssignmentsPerParticle() {
        int[] groupTypeIds = new int[]{0, 1}; // dimer
        int[] expResults = new int[]{1, 2}; // tetramer, a particle can be part of two tetramer formations
        for (int type : groupTypeIds) {
            int result = groupParameters.getMaxNumberOfAssignmentsPerParticle(type);
            int expResult = expResults[type];
            System.out.println("typeId: " + type + " expResult: " + expResult + " result: " + result);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getGroupInternalPotentials method, of class GroupParameters.
     */
    @Test
    public void testGetGroupInternalPotentials() {
        System.out.println("getGroupInternalPotentials");

        int groupTypeId = 0;
        System.out.println("testing type Id " + groupTypeId + " ...");
        HashMap<Integer, int[][]> expResult = new HashMap();
        expResult.put(0, new int[][]{new int[]{0, 1}});
        HashMap<Integer, int[][]> result = groupParameters.getGroupInternalPotentials(groupTypeId);

        for (int potIdKey : expResult.keySet()) {
            assertEquals(true, result.containsKey(potIdKey));
            verifyIntMatrix(expResult.get(potIdKey), result.get(potIdKey));
        }


        groupTypeId = 1;
        System.out.println("testing type Id " + groupTypeId + " ...");
        expResult = new HashMap();
        expResult.put(1, new int[][]{new int[]{0, 1},
                    new int[]{0, 2},
                    new int[]{1, 3},
                    new int[]{2, 3}
                });
        result = groupParameters.getGroupInternalPotentials(groupTypeId);

        for (int potIdKey : expResult.keySet()) {
            assertEquals(true, result.containsKey(potIdKey));
            verifyIntMatrix(expResult.get(potIdKey), result.get(potIdKey));
        }
    }

    /**
     * Test of doesTypeIdExist method, of class GroupParameters.
     */
    @Test
    public void testDoesTypeIdExist() {
        System.out.println("doesTypeIdExist");
        int[] groupTypeIds = new int[]{0, 1, 5};
        boolean[] groupExistence = new boolean[]{true, true, false};

        for (int i = 0; i < groupTypeIds.length; i++) {
            int groupTypeId = groupTypeIds[i];
            System.out.println("testing type Id " + groupTypeId + " ...");
            boolean expResult = groupExistence[i];
            boolean result = groupParameters.doesTypeIdExist(groupTypeId);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getBuildingBlocks method, of class GroupParameters.
     */
    @Test
    public void testGetBuildingBlocks() {
        System.out.println("test GetBuildingBlocks()");
        int groupTypeId = 0;
        System.out.println("testing type Id " + groupTypeId + " ...");
        ArrayList<IExtendedIdAndType> expResult = new ArrayList();
        expResult.add(new ExtendedIdAndType(false, 0, 0));
        expResult.add(new ExtendedIdAndType(false, 1, 0));
        ArrayList<IExtendedIdAndType> result = groupParameters.getBuildingBlocks(groupTypeId);
        for (IExtendedIdAndType eiat : result) {
            eiat.print();
        }
        assertEquals(expResult.size(), result.size());
        ArrayList<IExtendedIdAndType> toFind = new ArrayList();
        toFind.addAll(expResult);
        while (!toFind.isEmpty()) {

            IExtendedIdAndType e = toFind.remove(0);
            boolean found = false;
            for (IExtendedIdAndType e2 : result) {
                if (e.get_isGroup() == e2.get_isGroup()
                        && e.get_id() == e2.get_id()
                        && e.get_type() == e2.get_type()) {
                    found = true;
                    break;
                }
            }
            assertEquals(true, found);
        }


        groupTypeId = 1;
        System.out.println("testing type Id " + groupTypeId + " ...");
        expResult = new ArrayList();
        expResult.add(new ExtendedIdAndType(false, 0, 0));
        expResult.add(new ExtendedIdAndType(false, 1, 0));
        expResult.add(new ExtendedIdAndType(false, 2, 0));
        expResult.add(new ExtendedIdAndType(false, 3, 0));
        result = groupParameters.getBuildingBlocks(groupTypeId);
        assertEquals(expResult.size(), result.size());
        toFind = new ArrayList();
        toFind.addAll(expResult);
        while (!toFind.isEmpty()) {

            IExtendedIdAndType e = toFind.remove(0);
            boolean found = false;
            for (IExtendedIdAndType e2 : result) {
                if (e.get_isGroup() == e2.get_isGroup()
                        && e.get_id() == e2.get_id()
                        && e.get_type() == e2.get_type()) {
                    found = true;
                    break;
                }
            }
            assertEquals(true, found);
        }

    }

    private void verifyIntMatrix(int[][] expMatrix, int[][] resMatrix) {
        assertEquals(expMatrix.length, resMatrix.length);
        for (int i = 0; i < expMatrix.length; i++) {
            int[] expRow = expMatrix[i];
            int[] resRow = resMatrix[i];
            assertEquals(expRow.length, resRow.length);
            for (int j = 0; j < expRow.length; j++) {
                assertEquals(expRow[j], resRow[j]);
            }
        }
    }
}
