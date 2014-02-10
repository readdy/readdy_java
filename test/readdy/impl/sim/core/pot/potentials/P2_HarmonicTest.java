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
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.sim.core.particle.IParticle;
import readdy.impl.sim.core.particle.Particle;

/**
 *
 * @author schoeneberg
 */
public class P2_HarmonicTest {

    private static IPotential2 potSpring, potRepulsive, potAttractive;

    public P2_HarmonicTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        potSpring = new P2_Harmonic();
        HashMap<String, String> parametersSpring = new HashMap();
        parametersSpring.put("id", "0");
        parametersSpring.put("name", "springHarmonic");
        parametersSpring.put("type", "HARMONIC");
        parametersSpring.put("subtype", "spring");
        parametersSpring.put("forceConst", "10");
        parametersSpring.put("affectedParticleTypeIdPairs", "null");
        parametersSpring.put("affectedParticleIdPairs", "null");
        
        
        potSpring.set_parameterMap(parametersSpring);

        potRepulsive = new P2_Harmonic();
        HashMap<String, String> parametersRep = new HashMap();
        parametersRep.put("id", "1");
        parametersRep.put("name", "repulsiveHarmonic");
        parametersRep.put("type", "HARMONIC");
        parametersRep.put("subtype", "repulsive");
        parametersRep.put("forceConst", "10");
        parametersRep.put("affectedParticleTypeIdPairs", "null");
        parametersRep.put("affectedParticleIdPairs", "null");
        potRepulsive.set_parameterMap(parametersRep);

        potAttractive = new P2_Harmonic();
        HashMap<String, String> parametersAtt = new HashMap();
        parametersAtt.put("id", "2");
        parametersAtt.put("name", "attractiveHarmonic");
        parametersAtt.put("type", "HARMONIC");
        parametersAtt.put("subtype", "attractive");
        parametersAtt.put("forceConst", "10");
        parametersAtt.put("affectedParticleTypeIdPairs", "null");
        parametersAtt.put("affectedParticleIdPairs", "null");
        potAttractive.set_parameterMap(parametersAtt);
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
        potSpring.getGradient();
    }

    /**
     * Test of get_name method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testGet_name() {
        System.out.println("getName");
        assertEquals("springHarmonic", potSpring.get_name());
    }

    /**
     * Test of get_id method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testGet_id() {

        System.out.println("getId");
        assertEquals(2, potAttractive.get_id());
    }

    /**
     * Test of setCoordinates method, of class P2_HarmonicParticleAttractionPotential.
     */
    @Test
    public void testSetCoordinates_getGrad_getNrg() {
        System.out.println("setCoordinates");
        IParticle p1 = new Particle(0, 0, new double[]{1, 2, 3});
        IParticle p2 = new Particle(0, 0, new double[]{6, 5, 4});
        double dist = 5.91608;
        double radiusS = 5.5;
        double radiusL = 6.5;


        // SPRING
        System.out.println("spring");
        double[] expGrad = new double[]{3.51652, 2.10991, 0.703303};
        double expNrg = 0.865612;
        testPotential(potSpring, p1, p2, dist, radiusS, expGrad, expNrg);
        expGrad = new double[]{-4.93503, -2.96102, -0.987005};
        expNrg = 1.70481;
        testPotential(potSpring, p1, p2, dist, radiusL, expGrad, expNrg);

        // REPULSIVE
        System.out.println("repulsive");
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potRepulsive, p1, p2, dist, radiusS, expGrad, expNrg);
        expGrad = new double[]{-4.93503, -2.96102, -0.987005};
        expNrg = 1.70481;
        testPotential(potRepulsive, p1, p2, dist, radiusL, expGrad, expNrg);

        // ATTRACTIVE
        System.out.println("attractive");
        expGrad = new double[]{3.51652, 2.10991, 0.703303};
        expNrg = 0.865612;
        testPotential(potAttractive, p1, p2, dist, radiusS, expGrad, expNrg);
        expGrad = new double[]{0, 0, 0};
        expNrg = 0;
        testPotential(potAttractive, p1, p2, dist, radiusL, expGrad, expNrg);

        // there has to be at least once the assertEquals method call
        // otherweise netbeans doesn recognize the test as passed...
        assertEquals(0, 0);

    }

    private void testPotential(IPotential2 pot, IParticle p1, IParticle p2, double dist, double radius, double[] expGrad, double expNrg) {
        pot.set_coordinates(p1.get_coords(), p2.get_coords(), dist, radius);
        //GRADIENT
        double[] resultGrad = pot.getGradient();
        for (int i = 0; i < expGrad.length; i++) {
            assertEquals(expGrad[i], resultGrad[i], 0.0001);

        }
        //NRG
        double resultNrg = pot.getEnergy();
        assertEquals(expNrg, resultNrg, 0.0001);
    }
}
