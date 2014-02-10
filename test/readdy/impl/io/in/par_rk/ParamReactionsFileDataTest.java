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
package readdy.impl.io.in.par_rk;

import readdy.api.io.in.par_rk.IReactionData;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsFileParser;
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
public class ParamReactionsFileDataTest {

    private static IParamReactionsFileData paramReactionsFileData;

    public ParamReactionsFileDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String filename = "./test/testInputFiles/test_default/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filename);
        paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();
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
     * Test of get_reactionDataList method, of class ParamReactionsFileData.
     */
    @Test
    public void testGet_reactionDataList() {
        System.out.println("get_reactionDataList");
        ArrayList<IReactionData> reactionDataList = paramReactionsFileData.get_reactionDataList();

        int expNReactions = 6;
        assertEquals(expNReactions, reactionDataList.size());

        // ########################################################################
        int expId = 0;
        System.out.println(expId);

        String expName = "A_dimerization";
        String expTypeName = "group";
        String[] expEductTypes = new String[]{"particle", "particle"};
        String[] expEductTypeNames = new String[]{"A", "A"};
        String[] expProductTypes = new String[]{"group"};
        String[] expProductTypeNames = new String[]{"A_DIMER"};
        double expKForward = 10000000.;
        double expKBackward = 15;

        int idToTest = 0;
        IReactionData resultRkData = reactionDataList.get(idToTest);
        System.out.println("idToTest: " + idToTest);
        testSingleReactionDataEntry(expId, expName,
                expTypeName,
                expEductTypes,
                expEductTypeNames,
                expProductTypes,
                expProductTypeNames,
                expKForward,
                expKBackward,
                resultRkData);

        // ########################################################################
        expId = 1;
        System.out.println(expId);
        expName = "A_twoDimers_2_tetramer";
        expTypeName = "gFusion";
        expEductTypes = new String[]{"group", "group"};
        expEductTypeNames = new String[]{"A_DIMER", "A_DIMER"};
        expProductTypes = new String[]{"group"};
        expProductTypeNames = new String[]{"A_TETRAMER"};
        expKForward = 12;
        expKBackward = 1;

        idToTest = 1;
        resultRkData = reactionDataList.get(idToTest);
        System.out.println("idToTest: " + idToTest);
        testSingleReactionDataEntry(expId, expName,
                expTypeName,
                expEductTypes,
                expEductTypeNames,
                expProductTypes,
                expProductTypeNames,
                expKForward,
                expKBackward,
                resultRkData);
        // ########################################################################
        expId = 2;
        System.out.println(expId);
        expName = "A1_2_A2";
        expTypeName = "typeConversion";
        expEductTypes = new String[]{"particle"};
        expEductTypeNames = new String[]{"A1"};
        expProductTypes = new String[]{"particle"};
        expProductTypeNames = new String[]{"A2"};
        expKForward = 15;
        expKBackward = 5;

        idToTest = 2;
        resultRkData = reactionDataList.get(idToTest);
        System.out.println("idToTest: " + idToTest);
        testSingleReactionDataEntry(expId, expName,
                expTypeName,
                expEductTypes,
                expEductTypeNames,
                expProductTypes,
                expProductTypeNames,
                expKForward,
                expKBackward,
                resultRkData);
    }

    private void testSingleReactionDataEntry(
            int expId,
            String expName,
            String expTypeName,
            String[] expEductTypes,
            String[] expEductTypeNames,
            String[] expProductTypes,
            String[] expProductTypeNames,
            double expKForward,
            double expKBackward,
            IReactionData resultRkData) {

        assertEquals(expId, resultRkData.get_rkId());
        assertEquals(expName, resultRkData.get_rkName());
        assertEquals(expTypeName, resultRkData.get_typeName());
        assertEquals(expEductTypes.length, resultRkData.get_educts().size());
        for (int i = 0; i < expEductTypes.length; i++) {
            assertEquals(expEductTypes[i], resultRkData.get_educts().get(i)[0]);
            assertEquals(expEductTypeNames[i], resultRkData.get_educts().get(i)[2]);
        }

        assertEquals(expProductTypes.length, resultRkData.get_products().size());
        for (int i = 0; i < expProductTypes.length; i++) {
            assertEquals(expProductTypes[i], resultRkData.get_products().get(i)[0]);
            assertEquals(expProductTypeNames[i], resultRkData.get_products().get(i)[2]);
        }

        assertEquals(expKForward, resultRkData.get_kForward(), 0.00001);
        assertEquals(expKBackward, resultRkData.get_kBackward(), 0.00001);



    }
}
