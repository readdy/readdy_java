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
package readdy.impl.sim.top.rkHandle.rkExecutors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IParticleCoordinateCreatorFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.impl.assembly.ParticleCoordinateCreatorFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class ParticleCoordinateCreatorTest {

    private static IParticleCoordinateCreator particleCoordinateCreator;
    private static IGlobalParameters globalParameters;

    public ParticleCoordinateCreatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_default/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();


        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

        //----------------------------------------------------------------------------------------

        IParticleCoordinateCreatorFactory particleCoordinateCreatorFactory = new ParticleCoordinateCreatorFactory();
        particleCoordinateCreatorFactory.set_particleParameters(particleParameters);
        particleCoordinateCreatorFactory.set_globalParameters(globalParameters);
        particleCoordinateCreator = particleCoordinateCreatorFactory.createParticleCoordinateCreator();
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
     * Test of createCoordinates method, of class ParticleCoordinateCreator.
     */
    @Test
    public void testCreateCoordinates() {
        System.out.println("createCoordinates");
        int pTypeId = 0;
        double[] result = particleCoordinateCreator.createCoordinates(pTypeId);
        assertEquals(3, result.length);
        AdvancedSystemOut.println("created coords:", result, "");
        double[][] latticeBounds = globalParameters.get_latticeBounds();
        double[] min = new double[]{latticeBounds[0][0], latticeBounds[1][0], latticeBounds[2][0]};
        AdvancedSystemOut.println("min coords:", min, "");
        double[] max = new double[]{latticeBounds[1][1], latticeBounds[1][1], latticeBounds[2][1]};
        AdvancedSystemOut.println("max coords:", max, "");
        assertEquals(true, result[0] >= min[0] && result[0] <= max[0]);
        assertEquals(true, result[1] >= min[1] && result[1] <= max[1]);
        assertEquals(true, result[2] >= min[2] && result[2] <= max[2]);
    }

    /**
     * Test of createCoordinatesNextToEachOther method, of class ParticleCoordinateCreator.
     */
    @Test
    public void testCreateCoordinatesNextToEachOther() {
        System.out.println("createCoordinatesNextToEachOther");
        int pTypeId1 = 0;
        int pTypeId2 = 0;
        double expDist = 1.4 + 1.4;
        double[][] result = particleCoordinateCreator.createRandomCoordinatesNextToEachOther(pTypeId1, pTypeId2);
        assertEquals(2, result.length);
        double resDist = DoubleArrays.norm(DoubleArrays.subtract(result[0], result[1]));
        assertEquals(expDist, resDist, 0.0001);

        pTypeId1 = 0;
        pTypeId2 = 1;
        expDist = 1 + 5;
        result = particleCoordinateCreator.createRandomCoordinatesNextToEachOther(pTypeId1, pTypeId2);
        assertEquals(2, result.length);
        resDist = DoubleArrays.norm(DoubleArrays.subtract(result[0], result[1]));
        assertEquals(expDist, resDist, 0.0001);
    }

    /**
     * Test of createCoordinatesNextToGivenParticle method, of class ParticleCoordinateCreator.
     */
    @Test
    public void testCreateCoordinatesNextToGivenParticle() {
        System.out.println("createCoordinatesNextToGivenParticle");
        IParticle p = new Particle(0, 0, new double[]{0, 0, 0});
        int pTypeId = 1;
        double expDist = 1 + 5;
        double[] result = particleCoordinateCreator.createCoordinatesNextToGivenParticle(p, pTypeId);
        assertEquals(3, result.length);
        double resDist = DoubleArrays.norm(DoubleArrays.subtract(p.get_coords(), result));
        assertEquals(expDist, resDist, 0.0001);

    }
    /*
    @Test
    public void testCreateCoordinatesNextToGivenParticle2() {
        System.out.println("createCoordinatesNextToGivenParticle test distribution");
        for (int i = 0; i < 1000; i++) {
            
            
        IParticle p = new Particle(0, 0, new double[]{-100, -50, 0});
        int pTypeId = 1;
        double expDist = 1 + 5;
        double[] result = particleCoordinateCreator.createCoordinatesNextToGivenParticle(p, pTypeId);
        assertEquals(3, result.length);
        double resDist = DoubleArrays.norm(DoubleArrays.subtract(p.get_coords(), result));
        assertEquals(expDist, resDist, 0.0001);
        AdvancedSystemOut.println("", result, "");
            
        }

    }
    */

    /**
     * Test of createCoordinatesNextToEachOtherFromGivenCenter method, of class ParticleCoordinateCreator.
     */
    @Test
    public void testCreateCoordinatesNextToEachOtherFromGivenCenter() {
        System.out.println("createCoordinatesNextToEachOtherFromGivenCenter");
        IParticle p = new Particle(0, 0, new double[]{0, 0, 0});
        int pTypeId1 = 0;
        int pTypeId2 = 1;

        double expTotalDist = 1 + 5;
        double expDistOneToOld = 1;
        double expDistTwoToOld = 5;
        double[][] result = particleCoordinateCreator.createCoordinatesNextToEachOtherFromGivenCenter(p, pTypeId1, pTypeId2);
        assertEquals(2, result.length);
        double[] newCoord1 = result[0];
        double[] newCoord2 = result[1];
        double[] oneToOld = DoubleArrays.subtract(p.get_coords(), newCoord1);
        double[] twoToOld = DoubleArrays.subtract(p.get_coords(), newCoord2);
        double[] oneToTwo = DoubleArrays.subtract(newCoord1, newCoord2);
        double resTotalDist = DoubleArrays.norm(oneToTwo);
        double respDistOneToOld = DoubleArrays.norm(oneToOld);
        double resDistTwoToOld = DoubleArrays.norm(twoToOld);
        assertEquals(expTotalDist, resTotalDist, 0.0001);
        assertEquals(expDistOneToOld, respDistOneToOld, 0.0001);
        assertEquals(expDistTwoToOld, resDistTwoToOld, 0.0001);
    }

    /**
     * Test of createCenterOfMassCoordinate method, of class ParticleCoordinateCreator.
     */
    @Test
    public void testCreateCenterOfMassCoordinate() {
        System.out.println("createCenterOfMassCoordinate");
        IParticle p1 = new Particle(0, 0, new double[]{0, 0, 0});
        IParticle p2 = new Particle(1, 1, new double[]{10, 0, 0});
        int pTypeId = 0;

        double[] result = particleCoordinateCreator.createCenterOfMassCoordinate(p1, p2, pTypeId);
        assertEquals(3, result.length);

        double expTotalDist = 2 + 8;
        double expDistOneToNew = 9.920634920634921;
        double expDistTwoToNew = 0.0793651;


        double[] coord1 = p1.get_coords();
        double[] coord2 = p2.get_coords();
        double[] oneToNew = DoubleArrays.subtract(result, coord1);
        double[] twoToNew = DoubleArrays.subtract(result, coord2);
        double[] oneToTwo = DoubleArrays.subtract(coord1, coord2);
        double resTotalDist = DoubleArrays.norm(oneToTwo);
        double respDistOneToNew = DoubleArrays.norm(oneToNew);
        double resDistTwoToNew = DoubleArrays.norm(twoToNew);
        assertEquals(expTotalDist, resTotalDist, 0.0001);
        assertEquals(expDistOneToNew, respDistOneToNew, 0.0001);
        assertEquals(expDistTwoToNew, resDistTwoToNew, 0.0001);
    }
}
