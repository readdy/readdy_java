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

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileParser;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.impl.assembly.ElementalReactionManagerFactory_externalFile;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.ReactionObserverFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.rct_elmtlRk.ReactElmtlRkFileParser;
import readdy.impl.sim.core.particle.Particle;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class ReactionObserverTest {

    private static IReactionObserver reactionObserver;

    public ReactionObserverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("parse elementalReactions...");
        String filenameElementalReactions = "./test/testInputFiles/test_elementalReactions/react_elmtlRk.xml";
        IReactElmtlRkFileParser reactElmtlRkFileParser = new ReactElmtlRkFileParser();
        reactElmtlRkFileParser.parse(filenameElementalReactions);
        IReactElmtlRkFileData reactElmtlRkFileData = reactElmtlRkFileParser.get_reactElmtlRkFileData();

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

        ElementalReactionManagerFactory_externalFile elmtlRkManagerFactory = new ElementalReactionManagerFactory_externalFile();
        elmtlRkManagerFactory.set_particleParameters(particleParameters);
        elmtlRkManagerFactory.set_reactElmtlRkFileData(reactElmtlRkFileData);

        IElementalReactionManager elementalReactionManager = elmtlRkManagerFactory.createElementalRactionManager();

        IReactionObserverFactory reactionObserverFactory = new ReactionObserverFactory();
        reactionObserverFactory.set_elementalReactionManager(elementalReactionManager);
        reactionObserverFactory.set_particleParameters(particleParameters);
        reactionObserver = reactionObserverFactory.createReactionObserver();
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
     * Test of checkSpontaneous method, of class ReactionObserver.
     */
    @Test
    public void testCheckSpontaneous() {
        System.out.println("checkSpontaneous");

        int[] expElmtlRkToCheck_id = new int[]{3};
        double[] expElmtlRkToCheck_p = new double[]{0.2};


        System.out.println("expResult: elmtlRkId: " + expElmtlRkToCheck_id[0] + " p: " + expElmtlRkToCheck_p[0]);

        ArrayList<IOccurredElementalReaction> occRkList = reactionObserver.checkSpontaneous(0);
        for (IOccurredElementalReaction occRk : occRkList) {
            occRk.print();
        }

    }

    /**
     * Test of checkSingle method, of class ReactionObserver.
     */
    @Test
    public void testCheckSingle() {
        System.out.println("checkSingle");


        IParticle p = new Particle(0, 2, new double[]{0, 0, 0});
        int[] expElmtlRkToCheck_id = new int[]{2};
        double[] expElmtlRkToCheck_p = new double[]{0.3};


        System.out.println("expResult: elmtlRkId: " + expElmtlRkToCheck_id[0] + " p: " + expElmtlRkToCheck_p[0]);
        ArrayList<IOccurredElementalReaction> occRkList = reactionObserver.checkSingle(0, p);
        for (IOccurredElementalReaction occRk : occRkList) {
            occRk.print();
        }
    }

    /**
     * Test of checkPair method, of class ReactionObserver.
     */
    @Test
    public void testCheckPair() {
        System.out.println("checkPair");
        double[] coords1 = new double[]{0, 0, 0};
        double[] coords2 = new double[]{1, 0, 0};
        IParticle p1 = new Particle(0, 0, coords1);
        IParticle p2 = new Particle(1, 1, coords2);
        double dist = DoubleArrays.distance(coords1, coords2);
        int[] expElmtlRkToCheck_id = new int[]{0};
        double[] expElmtlRkToCheck_p = new double[]{0.5};
        System.out.println("expResult: elmtlRkId: " + expElmtlRkToCheck_id[0] + " p: " + expElmtlRkToCheck_p[0]);

        ArrayList<IOccurredElementalReaction> occRkList = reactionObserver.checkPair(0, p1, p2, dist);
        for (IOccurredElementalReaction occRk : occRkList) {
            occRk.print();
        }

        coords1 = new double[]{0, 0, 0};
        coords2 = new double[]{1, 0, 0};
        p1 = new Particle(0, 2, coords1);
        p2 = new Particle(278, 1, coords2);

        expElmtlRkToCheck_id = new int[]{1};
        expElmtlRkToCheck_p = new double[]{0.1};
        System.out.println("expResult: elmtlRkId: " + expElmtlRkToCheck_id[0] + " p: " + expElmtlRkToCheck_p[0]);

        occRkList = reactionObserver.checkPair(0, p1, p2, dist);
        for (IOccurredElementalReaction occRk : occRkList) {
            occRk.print();
        }
    }
}
