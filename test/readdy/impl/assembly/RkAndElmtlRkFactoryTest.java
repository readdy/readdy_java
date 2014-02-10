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

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IRkAndElmtlRkFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsFileParser;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.sim.top.rkHandle.ReactionManager;

/**
 *
 * @author schoeneberg
 */
public class RkAndElmtlRkFactoryTest {

    private static IParamReactionsFileData paramReactionsFileData;
    private static IRkAndElmtlRkFactory rkAndElmtlRkFactory;
    private static IReactionManager reactionTypeInventory;
    private static IGlobalParameters globalParameters;
    private static IGroupParameters groupParameters;
    private static IParticleParameters particleParameters;

    public RkAndElmtlRkFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String filename = "./test/testInputFiles/test_default/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filename);
        paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();

        //##############################################################################

        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();


        //##############################################################################
        String paramGroupsFilename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(paramGroupsFilename);
        IParamGroupsFileData paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();

        //##############################################################################

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
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        //##############################################################################

        IGroupParametersFactory groupParametersFactory = new GroupParametersFactory();
        groupParametersFactory.set_paramGroupsFileData(paramGroupsFileData);
        groupParametersFactory.set_particleParameters(particleParameters);
        groupParametersFactory.set_potentialInventory(potentialInventory);
        groupParameters = groupParametersFactory.createGroupParameters();

        reactionTypeInventory = new ReactionManager();

        rkAndElmtlRkFactory = new RkAndElmtlRkFactory();
        rkAndElmtlRkFactory.set_globalParameters(globalParameters);
        rkAndElmtlRkFactory.set_reactionManager(reactionTypeInventory);
        rkAndElmtlRkFactory.set_groupParameters(groupParameters);
        rkAndElmtlRkFactory.set_particleParameters(particleParameters);

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
     * Test of createReactionsAndElmtlReactions method, of class RkAndElmtlRkFactory.
     */
    @Test
    public void testCreateReactionsAndElmtlReactions() {
        System.out.println("createReactionsAndElmtlReactions");
        rkAndElmtlRkFactory.createReactionsAndElmtlReactions(paramReactionsFileData);
    }

    /**
     * Test of get_elmtlRkToRkMapping method, of class RkAndElmtlRkFactory.
     */
    @Test
    public void testGet_elmtlRkToRkMapping() {
        System.out.println("get_elmtlRkToRkMapping");

        int[] elmtlRks = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        int[] expResults = new int[]{0, 1, 2, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        assertEquals(elmtlRks.length, expResults.length);
        HashMap<Integer, Integer> result = rkAndElmtlRkFactory.get_elmtlRkToRkMapping();
        assertEquals(elmtlRks.length, result.keySet().size());
        for (int i = 0; i < elmtlRks.length; i++) {
            int expKey = elmtlRks[i];
            int expResult = expResults[i];

            assertEquals(true, result.containsKey(expKey));
            assertEquals(expResult, result.get(expKey), 0.0001);
        }

    }

    /**
     * Test of get_rkToElmtlRkMapping method, of class RkAndElmtlRkFactory.
     */
    @Test
    public void testGet_rkToElmtlRkMapping() {
        System.out.println("get_rkToElmtlRkMapping");
        int[] rkIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        int[][] expElmtlRkIds = new int[][]{new int[]{0},
            new int[]{1},
            new int[]{2, 3, 4, 5},
            new int[]{6, 7},
            new int[]{8},
            new int[]{9},
            new int[]{10},
            new int[]{11},
            new int[]{12},
            new int[]{13},
            new int[]{14},
            new int[]{15}
        };


        HashMap<Integer, ArrayList<Integer>> result = rkAndElmtlRkFactory.get_rkToElmtlRkMapping();
        assertEquals(rkIds.length, result.keySet().size());

        for (int i = 0; i < rkIds.length; i++) {
            int rkId = rkIds[i];
            int[] expElmtlRkIdsArr = expElmtlRkIds[i];


            assertEquals(true, result.containsKey(rkId));
            assertEquals(expElmtlRkIdsArr.length, result.get(rkId).size());
            for (int j = 0; j < expElmtlRkIdsArr.length; j++) {
                int expElmtlRkId = expElmtlRkIdsArr[j];
                int resultElmtlRkId = result.get(rkId).get(j);
                assertEquals(expElmtlRkId, resultElmtlRkId, 0.0001);
            }

        }
    }

    /**
     * Test of get_reactions method, of class RkAndElmtlRkFactory.
     */
    @Test
    public void testGet_reactions() {
        System.out.println("get_reactions");
        HashMap<Integer, IReaction> result = rkAndElmtlRkFactory.get_reactions();
        for (int rkId : result.keySet()) {
            IReaction rk = result.get(rkId);
            System.out.println();
            rk.print();
        }
    }

    /**
     * Test of get_elementalReactions method, of class RkAndElmtlRkFactory.
     */
    @Test
    public void testGet_elementalReactions() {
        System.out.println("get_elementalReactions");

        HashMap<Integer, IElementalReaction> result = rkAndElmtlRkFactory.get_elementalReactions();
        for (int elmtlRkId : result.keySet()) {
            IElementalReaction elmtlRk = result.get(elmtlRkId);
            System.out.println();
            elmtlRk.print();

        }
    }
}
