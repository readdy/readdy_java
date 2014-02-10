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
package readdy.impl.sim.core.rk;

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
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.rk.IElementalReaction;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IElementalReactionToCheck;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.assembly.ElementalReactionManagerFactory_internal;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.RkAndElmtlRkFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.top.rkHandle.ReactionManager;

/**
 *
 * @author schoeneberg
 */
public class ElementalReactionManagerInternalTest {

    private static IElementalReactionManager elementalReactionManager;

    public ElementalReactionManagerInternalTest() {
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

        //##

        String filename = "./test/testInputFiles/test_reactions/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filename);
        IParamReactionsFileData paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();



        //##############################################################################
        String paramGroupsFilename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(paramGroupsFilename);
        IParamGroupsFileData paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();


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
        HashMap<Integer, IElementalReaction> elementalReactions = rkAndElmtlRkFactory.get_elementalReactions();
        for (int rkId : elementalReactions.keySet()) {
            elementalReactions.get(rkId).print();
        }

        ElementalReactionManagerFactory_internal elmtlRkManagerFactory = new ElementalReactionManagerFactory_internal();
        elmtlRkManagerFactory.set_particleParameters(particleParameters);
        elmtlRkManagerFactory.set_elmtlReactions(elementalReactions);

        elementalReactionManager = elmtlRkManagerFactory.createElementalRactionManager();
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
     * Test of getElmtlReactions method, of class ElementalReactionManager.
     */
    @Test
    public void testGetElmtlReactions_0args() {
        System.out.println("getElmtlReactions of order 0");
        int[] expElmtlRkToCheck_id = new int[]{14};
        double[] expElmtlRkToCheck_p = new double[]{1.6999998553313134E-7};
        int expNCounts = 1;

        Iterator<IElementalReactionToCheck> resultIter = elementalReactionManager.getElmtlReactions();
        int counter = 0;
        while (resultIter.hasNext()) {

            IElementalReactionToCheck elmtlRkToCheck = resultIter.next();
            System.out.println("elmtlRkToCheck_id: " + elmtlRkToCheck.get_elmtlRkId() + " p: " + elmtlRkToCheck.get_p());
            assertEquals(expElmtlRkToCheck_id[counter], elmtlRkToCheck.get_elmtlRkId());
            assertEquals(expElmtlRkToCheck_p[counter], elmtlRkToCheck.get_p(), 0.0001);
            counter++;
        }
        assertEquals(expNCounts, counter);

    }

    /**
     * Test of getElmtlReactions method, of class ElementalReactionManager.
     */
    @Test
    public void testGetElmtlReactions_IParticle() {
        System.out.println("getElmtlReactions of order 1");
        IParticle p = new Particle(0, 3, new double[]{0, 0, 0});
        int[] expElmtlRkToCheck_id = new int[]{8};
        double[] expElmtlRkToCheck_p = new double[]{1.4999998876330523E-7};
        int expNCounts = 1;

        Iterator<IElementalReactionToCheck> resultIter = elementalReactionManager.getElmtlReactions(p);
        int counter = 0;
        while (resultIter.hasNext()) {

            IElementalReactionToCheck elmtlRkToCheck = resultIter.next();
            System.out.println("elmtlRkToCheck_id: " + elmtlRkToCheck.get_elmtlRkId() + " p: " + elmtlRkToCheck.get_p());
            assertEquals(expElmtlRkToCheck_id[counter], elmtlRkToCheck.get_elmtlRkId());
            assertEquals(expElmtlRkToCheck_p[counter], elmtlRkToCheck.get_p(), 0.0001);
            counter++;
        }
        assertEquals(expNCounts, counter);

        p = new Particle(0, 5, new double[]{0, 0, 0});
        expElmtlRkToCheck_id = new int[]{10};
        expElmtlRkToCheck_p = new double[]{1.5999998714821828E-7};
        expNCounts = 1;

        resultIter = elementalReactionManager.getElmtlReactions(p);
        counter = 0;
        while (resultIter.hasNext()) {

            IElementalReactionToCheck elmtlRkToCheck = resultIter.next();
            System.out.println("elmtlRkToCheck_id: " + elmtlRkToCheck.get_elmtlRkId() + " p: " + elmtlRkToCheck.get_p());
            assertEquals(expElmtlRkToCheck_id[counter], elmtlRkToCheck.get_elmtlRkId());
            assertEquals(expElmtlRkToCheck_p[counter], elmtlRkToCheck.get_p(), 0.0001);
            counter++;
        }
        assertEquals(expNCounts, counter);

        p = new Particle(0, 10, new double[]{0, 0, 0});
        expElmtlRkToCheck_id = new int[]{15};
        expElmtlRkToCheck_p = new double[]{6.999999757617559E-8};
        expNCounts = 1;

        resultIter = elementalReactionManager.getElmtlReactions(p);
        counter = 0;
        while (resultIter.hasNext()) {

            IElementalReactionToCheck elmtlRkToCheck = resultIter.next();
            System.out.println("elmtlRkToCheck_id: " + elmtlRkToCheck.get_elmtlRkId() + " p: " + elmtlRkToCheck.get_p());
            assertEquals(expElmtlRkToCheck_id[counter], elmtlRkToCheck.get_elmtlRkId());
            assertEquals(expElmtlRkToCheck_p[counter], elmtlRkToCheck.get_p(), 0.0001);
            counter++;
        }
        assertEquals(expNCounts, counter);


    }

    /**
     * Test of getElmtlReactions method, of class ElementalReactionManager.
     */
    @Test
    public void testGetElmtlReactions_IParticle_IParticle() {
        System.out.println("getElmtlReactions of order 2");
        IParticle p1 = new Particle(0, 0, new double[]{0, 0, 0});
        IParticle p2 = new Particle(1, 0, new double[]{0, 0, 0});
        int[] expElmtlRkToCheck_id = new int[]{0};
        double[] expElmtlRkToCheck_p = new double[]{0.9999999999993086};
        int expNCounts = 8;

        Iterator<IElementalReactionToCheck> resultIter = elementalReactionManager.getElmtlReactions(p1, p2);
        int counter = 0;
        while (resultIter.hasNext()) {
            resultIter.next();
            counter++;
        }
        assertEquals(expNCounts, counter);


    }
}
