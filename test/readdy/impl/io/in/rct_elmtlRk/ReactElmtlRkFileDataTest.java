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
package readdy.impl.io.in.rct_elmtlRk;

import readdy.api.io.in.rct_elmtlRk.IElementalReactionData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileData;
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
public class ReactElmtlRkFileDataTest {

    private static IReactElmtlRkFileData reactElmtlRkFileData = null;

    public ReactElmtlRkFileDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        String filename = "./test/testInputFiles/test_elementalReactions/react_elmtlRk.xml";
        ReactElmtlRkFileParser reactElmtlRkFileParser = new ReactElmtlRkFileParser();
        reactElmtlRkFileParser.parse(filename);
        reactElmtlRkFileData = reactElmtlRkFileParser.get_reactElmtlRkFileData();
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

        int expNReactions = 4;
        assertEquals(expNReactions, reactElmtlRkFileData.get_elementalReactionDataList().size());

        int expId = 0;
        String expName = "ABcmplx_formation";
        ArrayList<String> expEductTypeNames = new ArrayList();
        expEductTypeNames.add("A");
        expEductTypeNames.add("B");
        ArrayList<Integer> expEductParticleIds = new ArrayList();
        ArrayList<String> expProductTypeNames = new ArrayList();
        expProductTypeNames.add("ABcmplx");
        ArrayList<Integer> expProductParticleIds = new ArrayList();
        double expP = 5e-8;
        IElementalReactionData result = reactElmtlRkFileData.get_elementalReactionDataList().get(0);

        testExpectedResult(result,
                expId,
                expName,
                expEductTypeNames,
                expEductParticleIds,
                expProductTypeNames,
                expProductParticleIds,
                expP);

        expId = 1;
        expName = "ABcmplx_enzymatic_decay";
        expEductTypeNames = new ArrayList();
        expEductTypeNames.add("ABcmplx");
        expEductParticleIds = new ArrayList();
        expEductParticleIds.add(278);
        expProductTypeNames = new ArrayList();
        expProductParticleIds = new ArrayList();
        expProductParticleIds.add(278);
        expP = 1e-8;
        result = reactElmtlRkFileData.get_elementalReactionDataList().get(1);

        testExpectedResult(result,
                expId,
                expName,
                expEductTypeNames,
                expEductParticleIds,
                expProductTypeNames,
                expProductParticleIds,
                expP);

        expId = 2;
        expName = "ABcmplx_spontaneous_decay";
        expEductTypeNames = new ArrayList();
        expEductTypeNames.add("ABcmplx");
        expEductParticleIds = new ArrayList();
        expProductTypeNames = new ArrayList();
        expProductParticleIds = new ArrayList();
        expP = 3e-8;
        result = reactElmtlRkFileData.get_elementalReactionDataList().get(2);

        testExpectedResult(result,
                expId,
                expName,
                expEductTypeNames,
                expEductParticleIds,
                expProductTypeNames,
                expProductParticleIds,
                expP);

        expId = 3;
        expName = "spontaneousCreationOfA";
        expEductTypeNames = new ArrayList();
        expEductParticleIds = new ArrayList();
        expProductTypeNames = new ArrayList();
        expProductTypeNames.add("A");
        expProductParticleIds = new ArrayList();
        expP = 2e-8;
        result = reactElmtlRkFileData.get_elementalReactionDataList().get(3);

        testExpectedResult(result,
                expId,
                expName,
                expEductTypeNames,
                expEductParticleIds,
                expProductTypeNames,
                expProductParticleIds,
                expP);


    }

    private void testExpectedResult(IElementalReactionData result,
            int expId,
            String expName,
            ArrayList<String> expEductTypeNames,
            ArrayList<Integer> expEductParticleIds,
            ArrayList<String> expProductTypeNames,
            ArrayList<Integer> expProductParticleIds,
            double expP) {

        assertEquals(expId, result.get_elmtlRkId());
        assertEquals(expName, result.get_name());
        assertEqualsArrayListString(expEductTypeNames, result.get_eductTypeNames());
        assertEqualsArrayListInteger(expEductParticleIds, result.get_eductParticleIds());
        assertEqualsArrayListString(expProductTypeNames, result.get_productTypeNames());
        assertEqualsArrayListInteger(expProductParticleIds, result.get_productParticleIds());
        assertEquals(expP, result.get_p(), 0.0001);
    }

    private void assertEqualsArrayListString(ArrayList<String> expEductTypeNames, ArrayList<String> eductTypeNames) {
        assertEquals(expEductTypeNames.size(), eductTypeNames.size());
        for (int i = 0; i < expEductTypeNames.size(); i++) {
            System.out.println("exp: " + expEductTypeNames.get(i) + " res: " + eductTypeNames.get(i));
            assertEquals(expEductTypeNames.get(i), eductTypeNames.get(i));

        }
    }

    private void assertEqualsArrayListInteger(ArrayList<Integer> expProductParticleIds, ArrayList<Integer> productParticleIds) {
        assertEquals(expProductParticleIds.size(), productParticleIds.size());
        for (int i = 0; i < expProductParticleIds.size(); i++) {
            System.out.println("exp: " + expProductParticleIds.get(i) + " res: " + productParticleIds.get(i));
            assertEquals(expProductParticleIds.get(i), productParticleIds.get(i));

        }
    }
}
