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
package readdy.impl.sim.core.bd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import readdy.api.assembly.IDiffusionEngineFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.particle.IParticleAllAccess;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.impl.assembly.DiffusionEngineFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.util.DoubleArrays;

/**
 *
 * This test sets up two cases
 * @author schoeneberg
 */
public class DiffusionEngineTest {

    private static IPotentialManager potentialManager;
    private static IParticleParameters particleParameters;
    private static IDiffusionEngine diffusionEngine;
    private static int nIntegrationSteps = 1000;

    public DiffusionEngineTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //##############################################################################
        // geht the particle parameters as input
        //##############################################################################

        System.out.println("ParticleParametersTEST...:");
        System.out.println();
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
        // geht the potential parameters as input
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
        // geht the potential parameters as input
        //##############################################################################
        IDiffusionEngineFactory diffEngineFactory = new DiffusionEngineFactory();
        diffEngineFactory.set_particleParameters(particleParameters);
        diffEngineFactory.set_potentialManager(potentialManager);
        diffusionEngine = diffEngineFactory.createDiffusionEngine();


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
     * Test of propagateSingle method, of class DiffusionEngine.
     */
    @Test
    public void testPropagateSingle() {
        BufferedWriter out = null;
        try {
            String path = "./test/testInputFiles/test_diffusionEngine/";
            String singleOutFileName = path + "DiffusionEngineTest_single.txt";
            out = new BufferedWriter(new FileWriter(singleOutFileName));

            IParticleAllAccess p = new Particle(0, 0, new double[]{15, 5, 5});
            AdvancedSystemOut.println("p1 new coords a", p.get_coords(), "");
            double[] newCoords = new double[p.get_coords().length];
            newCoords = p.get_coords();
            int nSteps = nIntegrationSteps;
            out.write("{");
            for (int i = 0; i < nSteps; i++) {
                newCoords = DoubleArrays.add(newCoords, diffusionEngine.propagateSingle(p));
                AdvancedSystemOut.println("p1 new coords aa", newCoords, "");
                out.write("{");
                for (int j = 0; j < newCoords.length; j++) {
                    if (j == newCoords.length - 1) {
                        out.write(Double.toString(newCoords[j]));
                    } else {
                        out.write(Double.toString(newCoords[j]) + ",");
                    }
                }
                p.set_coords(newCoords);
                if (i == nSteps - 1) {
                    out.write("}\n");
                } else {
                    out.write("},\n");
                }
            }
            out.write("}\n");

        } catch (IOException ex) {
            Logger.getLogger(DiffusionEngineTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(DiffusionEngineTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Test of propagatePair method, of class DiffusionEngine.
     */
    @Test
    public void testPropagatePair() {
        BufferedWriter out = null;
        try {
            String path = "./test/testInputFiles/test_diffusionEngine/";
            String singleOutFileName = path + "DiffusionEngineTest_pair.txt";
            out = new BufferedWriter(new FileWriter(singleOutFileName));

            System.out.println("propagatePair");
            IParticleAllAccess p1 = new Particle(10, 0, new double[]{5, 5, 5});
            IParticleAllAccess p2 = new Particle(11, 0, new double[]{5.1, 5.1, 5.1});
            double dist;


            double[][] newCoords = new double[2][3];
            newCoords[0] = p1.get_coords();
            newCoords[1] = p2.get_coords();
            int nSteps = nIntegrationSteps;




            out.write("{\n");
            for (int i = 0; i < nSteps; i++) {
                dist = DoubleArrays.distance(p1.get_coords(), p2.get_coords());
                System.out.println("dist: " + dist);
                double[][] cumulatedDisplacement = diffusionEngine.propagatePair(p1, p2, dist);



                newCoords[0] = DoubleArrays.add(newCoords[0], cumulatedDisplacement[0]);
                newCoords[1] = DoubleArrays.add(newCoords[1], cumulatedDisplacement[1]);

                // add a little noise:

                out.write("{");
                for (int j = 0; j < newCoords.length; j++) {

                    out.write("{");
                    for (int k = 0; k < newCoords[j].length; k++) {
                        if (k == newCoords[j].length - 1) {
                            out.write(Double.toString(newCoords[j][k]));
                        } else {
                            out.write(Double.toString(newCoords[j][k]) + ",");
                        }
                    }
                    if (j == newCoords.length - 1) {
                        out.write("}");
                    } else {
                        out.write("},\n");
                    }
                }
                if (i == nSteps - 1) {
                    out.write("}\n");
                } else {
                    out.write("},\n");
                }

                // add a little noise
                newCoords[0] = DoubleArrays.add(newCoords[0], diffusionEngine.propagateSingle(p1));
                newCoords[1] = DoubleArrays.add(newCoords[1], diffusionEngine.propagateSingle(p2));

                p1.set_coords(newCoords[0]);
                p2.set_coords(newCoords[1]);

            }
            out.write("}");
        } catch (IOException ex) {
            Logger.getLogger(DiffusionEngineTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(DiffusionEngineTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
