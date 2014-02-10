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
package readdy.impl.sim.core.particle;

import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.api.assembly.IParticleParametersFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  The particle parameters class has the duty, to provide all particle
 *  related parameters to the simulator.
 *  this includes also crucial precomputed factors for the particle
 *  displacement engine like DfactorNoise and DfactorPot.
 *  This test makes sure, that all these parameters are parsed correctly
 *  from the input files and that the right derivations are made whith this
 *  intput information.
 *
 * @author schoeneberg
 */
public class ParticleParametersTest {

    private static IParticleParameters particleParameters;

    public ParticleParametersTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of getMaxParticleInteractionRadiusByType method, of class ParticleParameters.
     */
    @Test
    public void testGet_maxPInteractionRadius_int() {
        System.out.println("testGet_maxPInteractionRadius_int");


        double[] expResult = new double[]{1.4, 10, 3};
        for (int i = 0; i < expResult.length; i++) {
            double result = particleParameters.get_maxPInteractionRadius(i);
            assertEquals(expResult[i], result, 0.0);
            System.out.println("exp: " + expResult[i] + " actual: " + result);
        }


        // TODO review the generated test code and remove the default call to fail.

    }

    /**
     * Test of get_pReactionRadius method, of class ParticleParameters.
     */
    @Test
    public void testGet_maxPInteractionRadius_int_int() {
        System.out.println("testGet_maxPInteractionRadius_int_int");

        double[] expResult_1 = new double[]{2.8, 6, 4.4};
        double[] expResult_2 = new double[]{6, 2.37, 13};
        double[] expResult_3 = new double[]{4.4, 13, 6};

        double[][] expResult = new double[][]{expResult_1, expResult_2, expResult_3};

        for (int i = 0; i < expResult.length; i++) {
            for (int j = 0; j < expResult.length; j++) {
                double result = particleParameters.get_maxPInteractionRadius(i, j);
                assertEquals(expResult[i][j], result, 0.00001);
                System.out.println("exp: " + expResult[i][j] + " actual: " + result);
            }
        }
    }

    /**
     * Test of getMaxParticleInteractionRadiusByType method, of class ParticleParameters.
     */
    @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
    public void testGetMaxParticleInteractionRadiusByType_typeNotExistent() {
        System.out.println("getMaxParticleInteractionRadiusByType - non existent type test -1");
        int nonExistentType = -1;
        double result = particleParameters.get_maxPInteractionRadius(nonExistentType);
    }

    @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
    public void testGetMaxParticleInteractionRadiusByType_typeNotExistent2() {
        System.out.println("getMaxParticleInteractionRadiusByType - non existent type test 5");
        int nonExistentType = 100;
        double result = particleParameters.get_maxPInteractionRadius(nonExistentType);
    }

    /**
     * double dFactorNoise = Math.sqrt(2 * D * dt);
     *
     * Test of get_DFactorNoise method, of class ParticleParameters.
     */
    @Test
    public void testGet_DFactorNoise() {
        System.out.println("get_DFactorNoise");
        double[] expResult = new double[]{0.141421, 0.1, 0.122474};
        for (int i = 0; i < expResult.length; i++) {
            double result = particleParameters.get_DFactorNoise(i);
            System.out.println("exp: " + expResult[i] + " actual: " + result);
            assertEquals(expResult[i], result, 0.00001);

        }
    }

    /**
     * double dFactorPot = -1 * D * dt / (Kb * T);
     *
     * Test of get_DFactorPot method, of class ParticleParameters.
     */
    @Test
    public void testGet_DFactorPot() {
        System.out.println("get_DFactorPot");
        double[] expResult = new double[]{-0.00400908, -0.00200454, -0.00300681};
        for (int i = 0; i < expResult.length; i++) {
            double result = particleParameters.get_DFactorPot(i);
            System.out.println("exp: " + expResult[i] + " actual: " + result);
            assertEquals(expResult[i], result, 0.0001);

        }
    }

    /**
     * Test of get_pCollisionRadius method, of class ParticleParameters.
     */
    @Test
    public void testGet_pCollisionRadius_int_int() {
        System.out.println("get_pCollisionRadius int int");

        double[] expResult_1 = new double[]{2.8, 6., 4.4};
        double[] expResult_2 = new double[]{6., 2.37, 4.185};
        double[] expResult_3 = new double[]{4.4, 4.185, 6.};

        double[][] expResult = new double[][]{expResult_1, expResult_2, expResult_3};

        for (int i = 0; i < expResult.length; i++) {
            for (int j = 0; j < expResult.length; j++) {
                double result = particleParameters.get_pCollisionRadius(i, j);
                assertEquals(expResult[i][j], result, 0.00001);
                System.out.println("exp: " + expResult[i][j] + " actual: " + result);
            }
        }
    }

    /**
     * Test of get_pReactionRadius method, of class ParticleParameters.
     */
    @Test
    public void testGet_pReactionRadius_int_int() {
        System.out.println("get_pReactionRadius int int");

        double[] expResult_1 = new double[]{2, 2, 4};
        double[] expResult_2 = new double[]{2, 2, 13};
        double[] expResult_3 = new double[]{4, 13, 6};

        double[][] expResult = new double[][]{expResult_1, expResult_2, expResult_3};

        for (int i = 0; i < expResult.length; i++) {
            for (int j = 0; j < expResult.length; j++) {
                double result = particleParameters.get_pReactionRadius(i, j);
                assertEquals(expResult[i][j], result, 0.00001);
                System.out.println("exp: " + expResult[i][j] + " actual: " + result);
            }
        }
    }

    /**
     * Test of get_pCollisionRadius method, of class ParticleParameters.
     */
    @Test
    public void testGet_pCollisionRadius_int() {
        System.out.println("get_pCollisionRadius int");
        double[] expResult = new double[]{1.4, 1.185, 3};
        for (int i = 0; i < expResult.length; i++) {
            double result = particleParameters.get_pCollisionRadius(i);
            assertEquals(expResult[i], result, 0.00001);
            System.out.println("exp: " + expResult[i] + " actual: " + result);
        }
    }

    /**
     * Test of get_pReactionRadius method, of class ParticleParameters.
     */
    @Test
    public void testGet_pReactionRadius_int() {
        System.out.println("get_pReactionRadius int");
        double[] expResult = new double[]{1, 1, 3};
        for (int i = 0; i < expResult.length; i++) {
            double result = particleParameters.get_pReactionRadius(i);
            assertEquals(expResult[i], result, 0.00001);
            System.out.println("exp: " + expResult[i] + " actual: " + result);
        }
    }

    @Test
    public void test_doesParticleTypeExist() {
        System.out.println("does particle type exist");
        boolean[] expResult = new boolean[]{false, true, true, true, true, false};
        int[] testTypeIds = new int[]{-1, 0, 1, 2, 5, 100};
        for (int i = 0; i < expResult.length; i++) {
            boolean result = particleParameters.doesTypeIdExist(testTypeIds[i]);
            assertEquals(expResult[i], result);
            System.out.println("probe: " + testTypeIds[i] + " exp: " + expResult[i] + " actual: " + result);
        }
    }

    @Test
    public void test_getParticleTypeIdFromTypeName() {
        System.out.println("getParticleTypeIdFromTypeName");
        String[] testTypes = new String[]{"A", "B", "ABcmplx"};
        int[] expResult = new int[]{0, 1, 2};

        for (int i = 0; i < expResult.length; i++) {
            int result = particleParameters.getParticleTypeIdFromTypeName(testTypes[i]);
            assertEquals(expResult[i], result);
            System.out.println("probe: " + testTypes[i] + " exp: " + expResult[i] + " actual: " + result);
        }
    }

    @Test
    public void test_getParticleTypeNameFromTypeId() {
        System.out.println("getParticleTypeNameFromTypeId");
        int[] testTypes = new int[]{0, 1, 2};
        String[] expResult = new String[]{"A", "B", "ABcmplx"};

        for (int i = 0; i < expResult.length; i++) {
            String result = particleParameters.getParticleTypeNameFromTypeId(testTypes[i]);
            assertEquals(expResult[i], result);
            System.out.println("probe: " + testTypes[i] + " exp: " + expResult[i] + " actual: " + result);
        }
    }
}
