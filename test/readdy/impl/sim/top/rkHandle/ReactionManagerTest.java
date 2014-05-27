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

import readdy.api.assembly.IReactionManagerFactory;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.assembly.ReactionManagerFactory;
import readdy.impl.sim.top.rkHandle.rkExecutors.StandardGroupBasedRkExecutor;
import readdy.impl.sim.top.rkHandle.rkExecutors.StandardParticleBasedRkExecutor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.impl.assembly.GroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IGroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IStandardGroupBasedRkExecutorFactory;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.impl.assembly.StandardGroupBasedRkExecutorFactory;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.api.assembly.IParticleCoordinateCreatorFactory;
import readdy.api.assembly.IStandardParticleBasedRkExecutorFactory;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.impl.assembly.ParticleCoordinateCreatorFactory;
import readdy.impl.assembly.StandardParticleBasedRkExecutorFactory;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core_mc.MetropolisDecider;
import readdy.impl.sim.core_mc.PotentialEnergyComputer;

/**
 *
 * @author schoeneberg
 */
public class ReactionManagerTest {

    private static IReactionManager reactionManager;

    public ReactionManagerTest() {
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




        //##############################################################################
        // potentialInventory
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

        System.out.println("parse tplgy_potentials");
        String tplgyPotentials = "./test/testInputFiles/test_default/tplgy_potentials.xml";
        tplgyPotentialsFileParser.parse(tplgyPotentials);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();
        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();

        potentialManagerFactory.set_potentialInventory(potentialInventory);

        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);

        potentialManagerFactory.set_particleParameters(particleParameters);
        IPotentialManager potentialManager = potentialManagerFactory.createPotentialManager();



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
        IReactionExecutor standardGroupBasedRkExecutor = standardGroupBasedRkExecutorFactory.createStandardGroupBasedRkExecutor();

        //##############################################################################
        // create ParticleCoordinateCreator
        //##############################################################################



        IParticleCoordinateCreatorFactory particleCoordinateCreatorFactory = new ParticleCoordinateCreatorFactory();
        particleCoordinateCreatorFactory.set_particleParameters(particleParameters);
        particleCoordinateCreatorFactory.set_globalParameters(globalParameters);
        IParticleCoordinateCreator particleCoordinateCreator = particleCoordinateCreatorFactory.createParticleCoordinateCreator();

        //##############################################################################
        // create standardParticleBasedRkExecutor
        //##############################################################################
        
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
        reactionManager = reactionManagerFactory.createReactionManager();
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
     * Test of get_reactionTypeId method, of class ReactionManager.
     */
    @Test
    public void testGet_reactionTypeId() {
        System.out.println("get_reactionTypeId");
        String[] reactionTypeNames = new String[]{
            "creation",
            "decay",
            "doubleCreation",
            "annihilation",
            "typeConversion",
            "birth",
            "death",
            "fission",
            "fusion",
            "enzymatic",
            "doubleTypeConversion",
            "group",
            "ungroup",
            "gTypeConversion",
            "gFission",
            "gFusion",
            "gEnzymatic",
            "gDoubleTypeConversion"
        };
        int[] expResultTypeIds = new int[]{
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            100,
            101,
            102,
            103,
            104,
            105,
            106};

        assertEquals(reactionTypeNames.length, expResultTypeIds.length);
        for (int i = 0; i < reactionTypeNames.length; i++) {

            String inputTypeName = reactionTypeNames[i];
            int expectedRkTypeId = expResultTypeIds[i];
            int resultRkTypeId = reactionManager.get_reactionTypeId(inputTypeName);
            System.out.println("inputTypeName " + inputTypeName + " expTypeId " + expectedRkTypeId + " resultTypeId " + resultRkTypeId);
            assertEquals(expectedRkTypeId, resultRkTypeId);
        }

    }

    /**
     * Test of get_reactionTypeName method, of class ReactionManager.
     */
    @Test
    public void testGet_reactionTypeName() {
        System.out.println("get_reactionTypeName");
        String[] expReactionTypeNames = new String[]{
            "creation",
            "decay",
            "doubleCreation",
            "annihilation",
            "typeConversion",
            "birth",
            "death",
            "fission",
            "fusion",
            "enzymatic",
            "doubleTypeConversion",
            "group",
            "ungroup",
            "gTypeConversion",
            "gFission",
            "gFusion",
            "gEnzymatic",
            "gDoubleTypeConversion"
        };
        int[] inputTypeIds = new int[]{
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            100,
            101,
            102,
            103,
            104,
            105,
            106,};

        assertEquals(expReactionTypeNames.length, inputTypeIds.length);
        for (int i = 0; i < expReactionTypeNames.length; i++) {

            String expTypeName = expReactionTypeNames[i];
            int inputTypeId = inputTypeIds[i];
            String resultTypeName = reactionManager.get_reactionTypeName(inputTypeId);
            System.out.println("inputTypeId " + inputTypeId + " expTypeName " + expTypeName + " resultTypeName " + resultTypeName);
            assertEquals(expTypeName, resultTypeName);
        }


    }

    /**
     * Test of get_inverseReactionTypeId method, of class ReactionManager.
     */
    @Test
    public void testGet_inverseReactionTypeId() {
        System.out.println("get_inverseReactionTypeId");
        int[] inputRkTypeIds = new int[]{
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            100,
            101,
            102,
            103,
            104,
            105,
            106
        };
        int[] expInverseRkTypeIds = new int[]{
            1,
            0,
            3,
            2,
            4,
            6,
            5,
            8,
            7,
            9,
            10,
            101,
            100,
            102,
            104,
            103,
            105,
            106
        };

        assertEquals(inputRkTypeIds.length, expInverseRkTypeIds.length);
        for (int i = 0; i < inputRkTypeIds.length; i++) {
            int inputTypeId = inputRkTypeIds[i];
            int expInvRkTypeId = expInverseRkTypeIds[i];
            int resultInvRkTypeId = reactionManager.get_inverseReactionTypeId(inputTypeId);
            System.out.println("inputTypeId " + inputTypeId + " expInvRkTypeId " + expInvRkTypeId + " resultInvRkTypeId " + resultInvRkTypeId);
            assertEquals(expInvRkTypeId, resultInvRkTypeId);
        }

    }

    /**
     * Test of getReactionExecutor method, of class ReactionManager.
     */
    @Test
    public void testGetReactionExecutor() {
        System.out.println("getReactionExecutor");
        for (int rkTypeId_particle = 0; rkTypeId_particle < 11; rkTypeId_particle++) {
            System.out.println("expResult " + StandardParticleBasedRkExecutor.class + " |result " + reactionManager.getReactionExecutor(rkTypeId_particle).getClass());
            assertEquals(StandardParticleBasedRkExecutor.class, reactionManager.getReactionExecutor(rkTypeId_particle).getClass());

        }
        for (int rkTypeId_group = 100; rkTypeId_group < 107; rkTypeId_group++) {
            System.out.println("expResult " + StandardGroupBasedRkExecutor.class + " |result " + reactionManager.getReactionExecutor(rkTypeId_group).getClass());
            assertEquals(StandardGroupBasedRkExecutor.class, reactionManager.getReactionExecutor(rkTypeId_group).getClass());

        }
    }
}
