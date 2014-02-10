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
package readdy.impl.io.in.par_global;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 */
public class ParamGlobalFileParserTest {

    public ParamGlobalFileParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of parse method, of class ParamGlobalFileParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String filename = "./test/testInputFiles/test_default/param_global.xml";
        ParamGlobalFileParser instance = new ParamGlobalFileParser();
        instance.parse(filename);
        IGlobalParameters globalParameters = instance.get_globalParameters();

        assertEquals(50000, globalParameters.get_nSimulationSteps());
        assertEquals(1e-8, globalParameters.get_dt(), 0);
        assertEquals(300, globalParameters.get_T(), 0);
        assertEquals(0.0083144621, globalParameters.get_Kb(), 0);

        double[][] expResult = new double[][]{new double[]{-50, 50}, new double[]{-50, 50}, new double[]{-50, 50}};
        double[][] lboounds = globalParameters.get_latticeBounds();
        for (int i = 0; i < lboounds.length; i++) {
            double[] ds = lboounds[i];
            for (int j = 0; j < ds.length; j++) {
                double d = ds[j];
                assertEquals(d, expResult[i][j], 0);

            }

        }



        globalParameters.print();
        // TODO review the generated test code and remove the default call to fail.

    }
    /**
     * Test of get_globalParameters method, of class ParamGlobalFileParser.
     */
}
