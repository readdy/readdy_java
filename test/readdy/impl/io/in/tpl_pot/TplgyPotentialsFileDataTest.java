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
package readdy.impl.io.in.tpl_pot;

import java.util.HashMap;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileDataEntry;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author schoeneberg
 */
public class TplgyPotentialsFileDataTest {

    private static ITplgyPotentialsFileData potFileData = null;

    public TplgyPotentialsFileDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        String filename = "./test/testInputFiles/test_default/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(filename);
        potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();
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
     * Test of get_potentialsDataList method, of class TplgyPotentialsFileData.
     */
    @Test
    public void testGet_potentialsDataList() {



        String[][][] expResults = new String[][][]{
            new String[][]{
                new String[]{"name", "attractiveDisk_1"},
                new String[]{"type", "DISK"},
                new String[]{"subtype", "attractive"},
                new String[]{"forceConst", "100.0"},
                new String[]{"center", "[0.0,0.0,0.0]"},
                new String[]{"normal", "[0.0,0.0,1.0]"},
                new String[]{"radius", "30.0"},
                new String[]{"affectedParticleTypeIds", "[0]"},
                new String[]{"affectedParticleIds", ""}
            },
            new String[][]{
                new String[]{"name", "attractiveCylinder_1"},
                new String[]{"type", "CYLINDER"},
                new String[]{"subtype", "attractive"},
                new String[]{"forceConst", "100"},
                new String[]{"center", "[0,0,10]"},
                new String[]{"normal", "[0,0,1]"},
                new String[]{"radius", "30"},
                new String[]{"height", "20"},
                new String[]{"affectedParticleTypeIds", "[1,2]"},
                new String[]{"affectedParticleIds", "[1000]"}
            },
            new String[][]{
                new String[]{"name", "harmonic_typeBound_attractive"},
                new String[]{"type", "HARMONIC"},
                new String[]{"subtype", "attractive"},
                new String[]{"forceConst", "3"},
                new String[]{"affectedParticleTypeIdPairs", "[[1,1];[0,1]]"},
                new String[]{"affectedParticleIdPairs", ""}
            },
            new String[][]{
                new String[]{"name", "harmonic_idBound_repulsive"},
                new String[]{"type", "HARMONIC"},
                new String[]{"subtype", "repulsive"},
                new String[]{"forceConst", "1"},
                new String[]{"affectedParticleTypeIdPairs", ""},
                new String[]{"affectedParticleIdPairs", "[[10,11];[0,2]]"}
            },
            new String[][]{
                new String[]{"name", "harmonic_typeBound_spring"},
                new String[]{"type", "HARMONIC"},
                new String[]{"subtype", "spring"},
                new String[]{"forceConst", "0.1"},
                new String[]{"affectedParticleTypeIdPairs", "[[1,1]]"},
                new String[]{"affectedParticleIdPairs", ""}
            }
        };


        ArrayList<ITplgyPotentialsFileDataEntry> dataList = potFileData.get_potentialsDataList();
        int i = 0;
        for (ITplgyPotentialsFileDataEntry entry : dataList) {
            System.out.println(i + " ...");
            String[][] expResult = expResults[i];
            HashMap<String, String> map = entry.get_paramNameToValueMap();
            for (int j = 0; j < expResult.length; j++) {
                String expKey = expResult[j][0];
                String expValue = expResult[j][1];
                System.out.println("expKey and value: " + expKey + " -> " + expValue);
                assertEquals(true, map.containsKey(expKey));
                assertEquals(expValue, map.get(expKey));

            }
            entry.print();
            i++;
        }

    }
}
