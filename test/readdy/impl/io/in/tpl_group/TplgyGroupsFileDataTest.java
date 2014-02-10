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
package readdy.impl.io.in.tpl_group;

import java.util.HashMap;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileDataEntry;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileParser;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
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
public class TplgyGroupsFileDataTest {

    private static ITplgyGroupsFileData tplgyGroupsFileData;

    public TplgyGroupsFileDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String filename = "./test/testInputFiles/test_default/tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
        tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();
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
     * Test of get_GroupsFileDataList method, of class TplgyGroupsFileData.
     */
    @Test
    public void testGet_GroupsFileDataList() {
        System.out.println("get_GroupsFileDataList");
        ArrayList<ITplgyGroupsFileDataEntry> groupEntryList = tplgyGroupsFileData.get_GroupsFileDataList();

        int expNEntries = 2;
        assertEquals(expNEntries, groupEntryList.size());

        // ########################################################################
        int expId = 0; // group ID
        int expTypeId = 1; // group type ID, in this case 1 for the tetramer
        HashMap<Integer, Integer> expMap = new HashMap();
        expMap.put(0, 0);
        expMap.put(1, 1);
        expMap.put(2, 2);
        expMap.put(3, 3);
        ITplgyGroupsFileDataEntry resultGroupEntry = groupEntryList.get(expId);
        testExpVsResultGroup(resultGroupEntry, expId, expTypeId, expMap);

        // ########################################################################
        expId = 1;
        expTypeId = 0; // group type ID, in this case 1 for the dimer
        expMap = new HashMap();
        expMap.put(0, 4);
        expMap.put(1, 5);
        resultGroupEntry = groupEntryList.get(expId);
        testExpVsResultGroup(resultGroupEntry, expId, expTypeId, expMap);

    }

    private void testExpVsResultGroup(ITplgyGroupsFileDataEntry resultGroupEntry, int expId, int expTypeId, HashMap<Integer, Integer> expMap) {
        System.out.println("testing... expId: " + expId);
        assertEquals(expId, resultGroupEntry.get_id());
        assertEquals(expTypeId, resultGroupEntry.get_typeId());
        assertEquals(expMap.keySet().size(), resultGroupEntry.get_internalAndParticleId().keySet().size());
        for (int key : expMap.keySet()) {
            System.out.println(expMap.get(key) + " - " + resultGroupEntry.get_internalAndParticleId().get(key));
            assertEquals(expMap.get(key), resultGroupEntry.get_internalAndParticleId().get(key));
        }
    }
}
