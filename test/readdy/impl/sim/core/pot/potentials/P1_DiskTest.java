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
package readdy.impl.sim.core.pot.potentials;

import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.impl.sim.core.particle.Particle;

/**
 *
 * @author schoeneberg
 */
public class P1_DiskTest {

    private static IPotential1 potRepulsive, potAttractive;

    public P1_DiskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        potAttractive = new P1_Disk();
        HashMap<String, String> parametersRepulsive = new HashMap();
        parametersRepulsive.put("id", "1");
        parametersRepulsive.put("name", "attractiveDisk_1");
        parametersRepulsive.put("type", "DISK");
        parametersRepulsive.put("subtype", "attractive");
        parametersRepulsive.put("forceConst", "1");
        parametersRepulsive.put("center", "[0,0,0]");
        parametersRepulsive.put("normal", "[-1,1,1]");
        parametersRepulsive.put("radius", "3");
        parametersRepulsive.put("considerParticleRadius", "true");
        parametersRepulsive.put("affectedParticleTypeIds", "null");
        parametersRepulsive.put("affectedParticleIds", "null");
        
        potAttractive.set_parameterMap(parametersRepulsive);

        potRepulsive = new P1_Disk();
        HashMap<String, String> parametersAttractive = new HashMap();
        parametersAttractive.put("id", "5");
        parametersAttractive.put("name", "repulsiveDisk_1");
        parametersAttractive.put("type", "DISK");
        parametersAttractive.put("subtype", "repulsive");
        parametersAttractive.put("forceConst", "1");
        parametersAttractive.put("center", "[0,0,0]");
        parametersAttractive.put("normal", "[-1,1,1]");
        parametersAttractive.put("radius", "3");
        parametersAttractive.put("considerParticleRadius", "true");
        parametersAttractive.put("affectedParticleTypeIds", "null");
        parametersAttractive.put("affectedParticleIds", "null");
        potRepulsive.set_parameterMap(parametersAttractive);


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

    @Test(expected = RuntimeException.class)
    public void testGetGradientBeforeCoordinates() {
        System.out.println("getGradientBeforeCoordinates");
        potRepulsive.getGradient();
    }

    /**
     * Test of get_name method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testGet_name() {
        System.out.println("getName");
        assertEquals("repulsiveDisk_1", potRepulsive.get_name());
    }

    /**
     * Test of get_id method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testGet_id() {

        System.out.println("getId");
        assertEquals(1, potAttractive.get_id());
    }

    /**
     * Test of getGradient method, of class P1_Disk.
     */
    /**
     * Test of setCoordinates method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testSetCoordinates_getGrad_getNrg() {
        System.out.println("testSetCoordinates_getGrad_getNrg");
        IParticle pAbove = new Particle(0, 0, new double[]{-0.5, 1.5, 0.5}); // above the plane
        IParticle pBelow = new Particle(0, 0, new double[]{1.167, -0.167, -1.167}); // mirror point below the plane
        IParticle pAboveFar = new Particle(0, 0, new double[]{-3.5, -2.5, 0.5}); // mirror point below the plane
        IParticle pBelowFar = new Particle(0, 0, new double[]{-2.5, -3.5, -0.5}); // mirror point below the plane
        double pRadiusS = 1; // not touching the disk
        double pRadiusL = 2; // rouching and crossing the disk



        // attractive
        System.out.println("ATTRACTIVE");
        // above small radius
        System.out.println("above small radius");
        double[] expGrad = new double[]{0.833333, -0.833333, -0.833333};
        double expNrg = 1.04167;
        testPotential(potAttractive, pAbove, pRadiusS, expGrad, expNrg);

        // above large radius
        System.out.println("above large radius");
        expGrad = new double[]{0.833333, -0.833333, -0.833333};
        expNrg = 1.04167;
        testPotential(potAttractive, pAbove, pRadiusL, expGrad, expNrg);

        // below small radius
        System.out.println("below small radius");
        expGrad = new double[]{-0.833667, 0.833667, 0.833667};
        expNrg = 1.0425;
        testPotential(potAttractive, pBelow, pRadiusS, expGrad, expNrg);

        // below large radius
        System.out.println("below large radius");
        expGrad = new double[]{-0.833667, 0.833667, 0.833667};
        expNrg = 1.0425;
        testPotential(potAttractive, pBelow, pRadiusL, expGrad, expNrg);

        // above far small radius
        System.out.println("above far small radius");
        expGrad = new double[]{1.78331, 0.783308, -0.5};
        expNrg = 2.88972;
        testPotential(potAttractive, pAboveFar, pRadiusS, expGrad, expNrg);

        // above far large radius
        System.out.println("above far large radius");
        expGrad = new double[]{2.0583, 1.0583, -0.5};
        expNrg = 5.63236;
        testPotential(potAttractive, pAboveFar, pRadiusL, expGrad, expNrg);

        // below far small radius
        System.out.println("below far small radius");
        expGrad = new double[]{0.783308, 1.78331, 0.5};
        expNrg = 2.88972;
        testPotential(potAttractive, pBelowFar, pRadiusS, expGrad, expNrg);

        // below far large radius
        System.out.println("below far large radius");
        expGrad = new double[]{1.0583, 2.0583, 0.5};
        expNrg = 5.63236;
        testPotential(potAttractive, pBelowFar, pRadiusL, expGrad, expNrg);


        // Repulsive
        System.out.println("REPULSIVE");
        // above small radius
        System.out.println("above small radius");
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potRepulsive, pAbove, pRadiusS, expGrad, expNrg);

        // above large radius
        System.out.println("above large radius");
        expGrad = new double[]{-0.321367, 0.321367, 0.321367};
        expNrg = 0.154915;
        testPotential(potRepulsive, pAbove, pRadiusL, expGrad, expNrg);

        // below small radius
        System.out.println("below small radius");
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potRepulsive, pBelow, pRadiusS, expGrad, expNrg);

        // below large radius
        System.out.println("below large radius");
        expGrad = new double[]{0.321034, -0.321034, -0.321034};
        expNrg = 0.154594;
        testPotential(potRepulsive, pBelow, pRadiusL, expGrad, expNrg);

        // above far small radius
        System.out.println("above far small radius");
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potRepulsive, pAboveFar, pRadiusS, expGrad, expNrg);

        // above far large radius
        System.out.println("above far large radius");
        expGrad = new double[]{-0.654701, 0.654701, 0.654701};
        expNrg = 0.642949;
        testPotential(potRepulsive, pAboveFar, pRadiusL, expGrad, expNrg);

        // below far small radius
        System.out.println("below far small radius");
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potRepulsive, pBelowFar, pRadiusS, expGrad, expNrg);

        // below far large radius
        System.out.println("below far large radius");
        expGrad = new double[]{0.654701, -0.654701, -0.654701};
        expNrg = 0.642949;
        testPotential(potRepulsive, pBelowFar, pRadiusL, expGrad, expNrg);



    }

    private void testPotential(IPotential1 pot, IParticle p, double pRadius, double[] expGrad, double expNrg) {
        pot.set_coordinates(p.get_coords(), pRadius);
        //GRADIENT
        System.out.println("test gradient...");
        double[] resultGrad = pot.getGradient();
        for (int i = 0; i < expGrad.length; i++) {
            System.out.println("i " + i);
            assertEquals(expGrad[i], resultGrad[i], 0.0001);

        }
        //NRG
        System.out.println("test energy...");
        double resultNrg = pot.getEnergy();
        assertEquals(expNrg, resultNrg, 0.0001);
    }
}
