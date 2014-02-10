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
package readdy.impl.io.in.tpl_coord;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileDataEntry;

/**
 *
 * @author schoeneberg
 */
public class TplgyCoordinatesFileParserTest {

    public TplgyCoordinatesFileParserTest() {
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
     * Test of parse method, of class TplgyCoordinatesFileParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String filename = "./test/testInputFiles/test_default/tplgy_coordinates.xml";
        TplgyCoordinatesFileParser instance = new TplgyCoordinatesFileParser();
        instance.parse(filename);
        ITplgyCoordinatesFileData data = instance.get_coodinatesFileData();
        ArrayList<ITplgyCoordinatesFileDataEntry> dataList = data.get_CoordinatesFileDataList();
        for (ITplgyCoordinatesFileDataEntry entry : dataList) {
            entry.print();
        }


        assertEquals(0, dataList.get(0).get_id(), 0);
        assertEquals(0, dataList.get(0).get_type(), 0);
        assertEquals(5.441379295706454, dataList.get(0).get_c()[0], 0);
        assertEquals(-23.411381253272392, dataList.get(0).get_c()[1], 0);
        assertEquals(0.0, dataList.get(0).get_c()[2], 0);
        assertEquals(3, dataList.get(0).get_c().length, 3);

        assertEquals(1, dataList.get(1).get_id(), 0);
        assertEquals(0, dataList.get(1).get_type(), 0);
        assertEquals(7.852670129898662, dataList.get(1).get_c()[0], 0);
        assertEquals(-24.83464388291621, dataList.get(1).get_c()[1], 0);
        assertEquals(0.0, dataList.get(1).get_c()[2], 0);
        assertEquals(3, dataList.get(1).get_c().length, 3);

        assertEquals(2, dataList.get(2).get_id(), 0);
        assertEquals(0, dataList.get(2).get_type(), 0);
        assertEquals(4.0181166660626335, dataList.get(2).get_c()[0], 0);
        assertEquals(-25.822672087464603, dataList.get(2).get_c()[1], 0);
        assertEquals(0.0, dataList.get(2).get_c()[2], 0);
        assertEquals(3, dataList.get(2).get_c().length, 3);

        assertEquals(3, dataList.get(3).get_id(), 0);
        assertEquals(0, dataList.get(3).get_type(), 0);
        assertEquals(6.429407500254841, dataList.get(3).get_c()[0], 0);
        assertEquals(-27.245934717108423, dataList.get(3).get_c()[1], 0);
        assertEquals(0.0, dataList.get(3).get_c()[2], 0);
        assertEquals(3, dataList.get(3).get_c().length, 3);

        assertEquals(4, dataList.get(4).get_id(), 0);
        assertEquals(0, dataList.get(4).get_type(), 0);
        assertEquals(0.0806863581053281, dataList.get(4).get_c()[0], 0);
        assertEquals(-2.7023636979413475, dataList.get(4).get_c()[1], 0);
        assertEquals(0.0, dataList.get(4).get_c()[2], 0);
        assertEquals(3, dataList.get(4).get_c().length, 3);

        assertEquals(5, dataList.get(5).get_id(), 0);
        assertEquals(0, dataList.get(5).get_type(), 0);
        assertEquals(-2.275995041673313, dataList.get(5).get_c()[0], 0);
        assertEquals(-1.1903938636068836, dataList.get(5).get_c()[1], 0);
        assertEquals(0.0, dataList.get(5).get_c()[2], 0);
        assertEquals(3, dataList.get(5).get_c().length, 3);

        assertEquals(6, dataList.get(6).get_id(), 0);
        assertEquals(0, dataList.get(6).get_type(), 0);
        assertEquals(1.5589068809620485, dataList.get(6).get_c()[0], 0);
        assertEquals(5.855870896717365, dataList.get(6).get_c()[1], 0);
        assertEquals(0.0, dataList.get(6).get_c()[2], 0);
        assertEquals(3, dataList.get(6).get_c().length, 3);

        assertEquals(7, dataList.get(7).get_id(), 0);
        assertEquals(0, dataList.get(7).get_type(), 0);
        assertEquals(-12.569801948456954, dataList.get(7).get_c()[0], 0);
        assertEquals(-17.19172732671429, dataList.get(7).get_c()[1], 0);
        assertEquals(0.0, dataList.get(7).get_c()[2], 0);
        assertEquals(3, dataList.get(7).get_c().length, 3);

        assertEquals(8, dataList.get(8).get_id(), 0);
        assertEquals(0, dataList.get(8).get_type(), 0);
        assertEquals(-25.030279760851013, dataList.get(8).get_c()[0], 0);
        assertEquals(-4.829407738308152, dataList.get(8).get_c()[1], 0);
        assertEquals(0.0, dataList.get(8).get_c()[2], 8);
        assertEquals(3, dataList.get(8).get_c().length, 3);

        assertEquals(9, dataList.get(9).get_id(), 0);
        assertEquals(1, dataList.get(9).get_type(), 0);
        assertEquals(8.111887695731276, dataList.get(9).get_c()[0], 0);
        assertEquals(-19.809576908418187, dataList.get(9).get_c()[1], 0);
        assertEquals(15.04976189991978, dataList.get(9).get_c()[2], 0);
        assertEquals(3, dataList.get(9).get_c().length, 3);

        assertEquals(10, dataList.get(10).get_id(), 0);
        assertEquals(1, dataList.get(10).get_type(), 0);
        assertEquals(11.698972412469018, dataList.get(10).get_c()[0], 0);
        assertEquals(9.541880292037028, dataList.get(10).get_c()[1], 0);
        assertEquals(18.428678943121042, dataList.get(10).get_c()[2], 0);
        assertEquals(3, dataList.get(10).get_c().length, 3);

        assertEquals(11, dataList.get(11).get_id(), 0);
        assertEquals(1, dataList.get(11).get_type(), 0);
        assertEquals(-25.129529116424738, dataList.get(11).get_c()[0], 0);
        assertEquals(-8.267794311997278, dataList.get(11).get_c()[1], 0);
        assertEquals(5.600288336795963, dataList.get(11).get_c()[2], 0);
        assertEquals(3, dataList.get(11).get_c().length, 3);

    }
}
