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

import java.util.ArrayList;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.bd.IPotentialDisplacementComputer;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
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
 * @author schoeneberg
 */
public class PotentialDisplacementComputerTest {

    private static IPotentialManager potentialManager;
    private static IParticleParameters particleParameters;
    private static IPotentialDisplacementComputer pdc;

    public PotentialDisplacementComputerTest() {
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
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();

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

        String tplgyPotentialFilename = "./test/testInputFiles/test_potentials/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();
        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        potentialManager = potentialManagerFactory.createPotentialManager();

        //##############################################################################
        // create the actual potential displacement computer class
        //##############################################################################

        pdc = new PotentialDisplacementComputer();
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
     * Test of computeSingleDisplacement method, of class PotentialDisplacementComputer.
     */
    @Test
    public void testComputeSingleDisplacement() {
        System.out.println("computeSingleDisplacement");
        IParticle pAbove = new Particle(0, 0, new double[]{-0.5, 1.5, 0.5}); // above the plane
        IParticle pBelow = new Particle(1, 0, new double[]{1.167, -0.167, -1.167}); // mirror point below the plane

        double pRadiusS = 1; // not touching the disk
        double pRadiusL = 2; // rouching and crossing the disk

        // attractive disc, pAbove, pRadiusS
        System.out.println("-----------------TEST CASE 1....");
        double[] expectedGradient = new double[]{0, 0, -50};
        double[] expectedDisplacement = new double[]{0, 0, -0.200454};
        testComputeSingleDisplacementOnDisk(pAbove, pRadiusS, expectedGradient, expectedDisplacement);

        // attractive disc, pAbove, pRadiusL
        System.out.println("-----------------TEST CASE 2....");
        expectedGradient = new double[]{0, 0, -50};
        expectedDisplacement = new double[]{0, 0, -0.200454};
        testComputeSingleDisplacementOnDisk(pAbove, pRadiusL, expectedGradient, expectedDisplacement);

        // repulsive disc, pBelow, pRadiusS
        System.out.println("-----------------TEST CASE 3....");
        expectedGradient = new double[]{0.0, 0.0, 116.7};
        expectedDisplacement = new double[]{0, 0, 0.467859};
        testComputeSingleDisplacementOnDisk(pBelow, pRadiusS, expectedGradient, expectedDisplacement);

        // repulsive disc, pBelow, pRadiusL
        System.out.println("-----------------TEST CASE 4....");
        // exprectedGradient = new double[]{-0.642068, 0.642068, 0.642068};
        expectedGradient = new double[]{0.0, 0.0, 116.7};
        expectedDisplacement = new double[]{0, 0, 0.467859};
        testComputeSingleDisplacementOnDisk(pBelow, pRadiusL, expectedGradient, expectedDisplacement);

    }

    private void testComputeSingleDisplacementOnDisk(IParticle p, double pRadius, double[] expectedGradient, double[] expDisplacement) {
        AdvancedSystemOut.println("pCoords", p.get_coords(), "");
        double DFactorPot = particleParameters.get_DFactorPot(p.get_type());
        System.out.println("DFactorPot: " + DFactorPot);
        System.out.println("pRadius: " + pRadius);
        Iterator<IPotential1> potIter = potentialManager.getPotentials(p);


        while (potIter.hasNext()) {
            IPotential1 pot = potIter.next();
            System.out.println("potName: " + pot.get_name() + " potId: " + pot.get_id());

            
            
            System.out.println("essental Parameter keys->values: ");
            for (String parameterKey : pot.get_defaultParameterMap().keySet()) {
                System.out.print(parameterKey + "->"+pot.get_parameterMap().get(parameterKey)+", ");
            }
            System.out.println("");

            // test the gradient
            System.out.println("test gradient...");
            pot.set_coordinates(p.get_coords(), pRadius);
            double[] grad = pot.getGradient();
            AdvancedSystemOut.println("expected gradient", expectedGradient, "");
            AdvancedSystemOut.println("potential gradient", grad, "");
            assertEquals(expectedGradient.length, grad.length);
            for (int i = 0; i < grad.length; i++) {
                System.out.println("expResult: " + expectedGradient[i] + " result: " + (double) grad[i]);
                assertEquals(expectedGradient[i], grad[i], 0.0001);
            }

            // test the displacement
            System.out.println("test displacement...");
            double[] result = pdc.computeSingleDisplacement(p.get_coords(), DFactorPot, pRadius, pot);
            assertEquals(expDisplacement.length, result.length);
            AdvancedSystemOut.println("expectedDisplacement: ", expDisplacement, "");
            AdvancedSystemOut.println("result: ", result, "");

            for (int i = 0; i < expDisplacement.length; i++) {
                System.out.println("expResult: " + expDisplacement[i] + " result: " + (double) result[i]);
                assertEquals(expDisplacement[i], result[i], 0.0001);
            }

        }


    }

    /**
     * Test of computePairDisplacement method, of class PotentialDisplacementComputer.
     */
    @Test
    public void testComputePairDisplacement() {
        System.out.println("========================================");
        System.out.println("TEST computePairDisplacement");


        double radiusS = 5.5;
        double radiusL = 6.5;


        // SPRING
        System.out.println("-----------------spring");

        IParticle p1 = new Particle(10, 2, new double[]{1, 2, 3});
        IParticle p2 = new Particle(11, 2, new double[]{5, 4, 3});

        double[][] expDisplacement = new double[][]{
            new double[]{-2.76430856092163E-4, -1.382154280460815E-4, 0},
            new double[]{2.76430856092163E-4, 1.382154280460815E-4, 0}
        };
        testComputePairDisplacementViaHarmonics(p1, p2, radiusS, expDisplacement);
        expDisplacement = new double[][]{
                    new double[]{-5.453680345417214E-4, -2.726840172708607E-4, 0},
                    new double[]{5.453680345417214E-4, 2.726840172708607E-4, 0}
                };

        testComputePairDisplacementViaHarmonics(p1, p2, radiusL, expDisplacement);


        // REPULSIVE
        System.out.println("-----------------repulsive");

        p1 = new Particle(0, 0, new double[]{1, 2, 3});
        p2 = new Particle(1, 0, new double[]{5, 4, 3});

        expDisplacement = new double[][]{
                    new double[]{-0.003685744747895506, -0.001842872373947753, 0},
                    new double[]{0.003685744747895506, 0.001842872373947753, 0}
                };
        testComputePairDisplacementViaHarmonics(p1, p2, radiusS, expDisplacement);
        expDisplacement = new double[][]{
                    new double[]{0, 0, 0},
                    new double[]{0, 0, 0},};

        testComputePairDisplacementViaHarmonics(p1, p2, 2, expDisplacement);

        // ATTRACTIVE
        System.out.println("-----------------attractive");

        p1 = new Particle(100, 1, new double[]{1, 2, 3});
        p2 = new Particle(111, 1, new double[]{6, 5, 4});

        expDisplacement = new double[][]{
                    new double[]{0.002114, 0.001268, 4.229396860118695E-4},
                    new double[]{-0.002114, -0.001268819, -4.229396860118695E-4}
                };
        testComputePairDisplacementViaHarmonics(p1, p2, radiusS, expDisplacement);
        expDisplacement = new double[][]{
                    new double[]{0, 0, 0},
                    new double[]{0, 0, 0},};
        testComputePairDisplacementViaHarmonics(p1, p2, radiusL, expDisplacement);

    }

    private void testComputePairDisplacementViaHarmonics(IParticle p1, IParticle p2, double radius, double[][] expDisplacement) {

        double DFactorPot1 = particleParameters.get_DFactorPot(p1.get_type());
        double DFactorPot2 = particleParameters.get_DFactorPot(p2.get_type());

        double dist = DoubleArrays.distance(p1.get_coords(), p2.get_coords());
        System.out.println("dist " + dist);
        System.out.println("DFactorPot1: " + DFactorPot1);
        System.out.println("DFactorPot2: " + DFactorPot2);

        //double pRadius = particleParameters.get_pCollisionRadius(p.get_type());
        System.out.println("pRadius both: " + radius);
        Iterator<IPotential2> potIter = potentialManager.getPotentials(p1, p2);


        while (potIter.hasNext()) {
            IPotential2 pot = potIter.next();
            System.out.println("potName: " + pot.get_name() + " potId: " + pot.get_id());


            double[][] result = pdc.computePairDisplacement(p1.get_coords(),
                    DFactorPot1,
                    p2.get_coords(),
                    DFactorPot2,
                    radius,
                    dist,
                    pot);

            assertEquals(2, result.length);
            assertEquals(expDisplacement.length, result.length);

            for (int i = 0; i < expDisplacement.length; i++) {
                for (int j = 0; j < expDisplacement[i].length; j++) {
                    System.out.println("expResult[" + i + "," + j + "]: " + expDisplacement[i][j]
                            + " result[" + i + "," + j + "]: " + result[i][j]);
                    assertEquals(expDisplacement[i][j], result[i][j], 0.0001);

                }

            }

        }
    }
}
