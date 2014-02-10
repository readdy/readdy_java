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

import readdy.api.assembly.IReactionParametersFactory;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import readdy.impl.assembly.ReactionParametersFactory;
import java.util.HashMap;
import java.util.Iterator;
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
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.RkAndElmtlRkFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.sim.top.group.ExtendedIdAndType;

/**
 *
 * @author schoeneberg
 */
public class ReactionParametersTest {

    private static IReactionParameters reactionParameters;

    public ReactionParametersTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String filename = "./test/testInputFiles/test_default/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filename);
        IParamReactionsFileData paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();

        //##############################################################################

        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();


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
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

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
        IGroupParameters groupParameters = groupParametersFactory.createGroupParameters();

        IReactionManager reactionManager = new ReactionManager();

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
        reactionParameters = reactionParametersFactory.createReactionParameters();

        System.out.println("print all reactions before testing...");
        Iterator<IReaction> rkIterator = reactionParameters.reactionIterator();
        while (rkIterator.hasNext()) {
            IReaction rk = rkIterator.next();
            rk.print();
        }
        System.out.println("printing completed");
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
     * Test of getEducts method, of class ReactionParameters.
     */
    @Test
    public void testGetEducts() {
        System.out.println("getEducts");

        int rkId = 0;
        IExtendedIdAndType[] expResult = new IExtendedIdAndType[]{new ExtendedIdAndType(false, -1, 0), new ExtendedIdAndType(false, -1, 0)};
        IExtendedIdAndType[] result = reactionParameters.getEducts(rkId);
        compareExtendedIdAndTypeArrays(expResult, result);

    }

    /**
     * Test of getProducts method, of class ReactionParameters.
     */
    @Test
    public void testGetProducts() {
        System.out.println("getProducts");
        int rkId = 0;

        IExtendedIdAndType[] expResult = new IExtendedIdAndType[]{new ExtendedIdAndType(true, -1, 0)};
        IExtendedIdAndType[] result = reactionParameters.getProducts(rkId);
        compareExtendedIdAndTypeArrays(expResult, result);
    }

    /**
     * Test of getCorrespondingTopLevelReactionId method, of class ReactionParameters.
     */
    @Test
    public void testGetCorrespondingTopLevelReactionId() {
        System.out.println("getCorrespondingTopLevelReactionId");
        int[] elmtlRkIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        int[] expResults = new int[]{0, 1, 2, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        for (int i = 0; i < elmtlRkIds.length; i++) {
            int elmtlRkId = elmtlRkIds[i];
            int expResult = expResults[i];
            int result = reactionParameters.getCorrespondingTopLevelReactionId(elmtlRkId);
            assertEquals(expResult, result);

        }




    }

    /**
     * Test of getReaction method, of class ReactionParameters.
     */
    @Test
    public void testGetReaction() {
        System.out.println("getReaction");
        int rkId = 0;

        IReaction result = reactionParameters.getReaction(rkId);
        assertEquals(0, result.get_id());
        assertEquals(10000000, result.get_k(), 0.0001);
        assertEquals("A_dimerization_forward", result.get_name());
        assertEquals(100, result.get_reactionTypeId());
        assertEquals("group", result.get_reactionTypeName());

    }

    /**
     * Test of reactionIterator method, of class ReactionParameters.
     */
    @Test
    public void testReactionIterator() {
        System.out.println("reactionIterator");

        int counter = 0;
        int expNumber = 12;
        Iterator result = reactionParameters.reactionIterator();
        while (result.hasNext()) {
            counter++;
            result.next();
        }

        assertEquals(expNumber, counter);

    }

    /**
     * Test of getReactionTypeId method, of class ReactionParameters.
     */
    @Test
    public void testGetReactionTypeId() {
        System.out.println("getReactionTypeId");
        int rkId = 0;

        int expResult = 100;
        int result = reactionParameters.getReactionTypeId(rkId);
        assertEquals(expResult, result);

    }

    private void compareExtendedIdAndTypeArrays(IExtendedIdAndType[] expResult, IExtendedIdAndType[] result) {
        assertEquals(expResult.length, result.length);
        for (int i = 0; i < expResult.length; i++) {
            IExtendedIdAndType expResultEntry = expResult[i];
            IExtendedIdAndType resultEntry = result[i];
            assertEquals(expResultEntry.get_isGroup(), resultEntry.get_isGroup());
            assertEquals(expResultEntry.get_id(), resultEntry.get_id());
            assertEquals(expResultEntry.get_type(), resultEntry.get_type());
        }
    }
}
