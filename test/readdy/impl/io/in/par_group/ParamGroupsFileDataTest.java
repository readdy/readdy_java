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
package readdy.impl.io.in.par_group;

import java.util.HashMap;
import readdy.api.io.in.par_group.IGroupData;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
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
public class ParamGroupsFileDataTest {

    private static IParamGroupsFileData paramGroupsFileData;

    public ParamGroupsFileDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String filename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(filename);
        paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();
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
     * Test of get_groupDataList method, of class ParamGroupsFileData.
     */
    @Test
    public void testGet_groupDataList() {
        System.out.println("get_groupDataList");
        ArrayList<IGroupData> paramGroupDataList = paramGroupsFileData.get_groupDataList();
        int expNGroups = 2;
        assertEquals(expNGroups, paramGroupDataList.size());


        //########################################################################
        int expId = 0;
        IGroupData resultGd = paramGroupDataList.get(expId);
        String expTypeName = "A_DIMER";
        ArrayList<String[]> expBuildingBlocks = new ArrayList();
        double[] expTemplateOrigin = new double[]{0, 0, 0};
        double[] expTemplateNormal = new double[]{0, 0, 1};
        expBuildingBlocks.add(new String[]{"particle", "0", "A", "[0,0,0]"});
        expBuildingBlocks.add(new String[]{"particle", "1", "A", "[0,2.8,0]"});

        ArrayList<HashMap<String, String>> expInvolvedPotentials = new ArrayList();
        HashMap<String, String> map = new HashMap();

        map.put("type", "HARMONIC");
        map.put("forceConst", "1");
        map.put("affectedInternalIdPairs", "[[0,1]]");
        expInvolvedPotentials.add(map);

        int maxNumberOfGroupAssignmentsPerParticle = 1;



        testGroupData(resultGd,
                expId,
                expTypeName,
                expTemplateOrigin,
                expTemplateNormal,
                expBuildingBlocks,
                expInvolvedPotentials,
                maxNumberOfGroupAssignmentsPerParticle);

        //########################################################################
        expId = 1;
        resultGd = paramGroupDataList.get(expId);
        expTypeName = "A_TETRAMER";
        expTemplateOrigin = new double[]{0, 0, 0};
        expTemplateNormal = new double[]{0, 0, 1};
        expBuildingBlocks = new ArrayList();
        expBuildingBlocks.add(new String[]{"particle", "0", "A", "[0,0,0]"});
        expBuildingBlocks.add(new String[]{"particle", "1", "A", "[0,2.8,0]"});
        expBuildingBlocks.add(new String[]{"particle", "2", "A", "[2.8,0,0]"});
        expBuildingBlocks.add(new String[]{"particle", "3", "A", "[2.8,2.8,0]"});

        expInvolvedPotentials = new ArrayList();
        map = new HashMap();

        map.put("type", "HARMONIC");
        map.put("forceConst", "1");
        map.put("affectedInternalIdPairs", "[[0,1];[0,2];[1,3];[2,3]]");
        expInvolvedPotentials.add(map);

        maxNumberOfGroupAssignmentsPerParticle = 2;



        testGroupData(resultGd,
                expId,
                expTypeName,
                expTemplateOrigin,
                expTemplateNormal,
                expBuildingBlocks,
                expInvolvedPotentials,
                maxNumberOfGroupAssignmentsPerParticle);

    }

    private void testGroupData(IGroupData resultGd,
            int expId,
            String expTypeName,
            double[] expTemplateOrigin,
            double[] expTemplateNormal,
            ArrayList<String[]> expBuildingBlocks,
            ArrayList<HashMap<String, String>> expInvolvedPotentials,
            int maxNumberOfGroupAssignmentsPerParticle) {

        System.out.println("testing id : " + expId + " ...");
        assertEquals(expId, resultGd.getTypeId());
        assertEquals(expTypeName, resultGd.getTypeName());

        double[] resTemplateOrigin = resultGd.getTemplateOrigin();
        double[] resTemplateNormal = resultGd.getTemplateNormal();
        assertEquals(expTemplateOrigin.length, resTemplateOrigin.length);
        assertEquals(expTemplateNormal.length, resTemplateNormal.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(expTemplateOrigin[i], resTemplateOrigin[i], 0.0001);
            assertEquals(expTemplateNormal[i], resTemplateNormal[i], 0.0001);
        }

        ArrayList<String[]> resultBuildingBlocks = resultGd.getBuildingBlocks();
        assertEquals(expBuildingBlocks.size(), resultBuildingBlocks.size());
        for (int i = 0; i < expBuildingBlocks.size(); i++) {
            String[] expBlock = expBuildingBlocks.get(i);
            String[] resBlock = resultBuildingBlocks.get(i);
            assertEquals(expBlock.length, resBlock.length);
            for (int j = 0; j < expBlock.length; j++) {
                assertEquals(expBlock[j], resBlock[j]);
            }
        }

        ArrayList<HashMap<String, String>> resInvolvedPotentials = resultGd.getInvolvedPotentials();
        assertEquals(expInvolvedPotentials.size(), resInvolvedPotentials.size());
        for (int i = 0; i < expInvolvedPotentials.size(); i++) {
            HashMap<String, String> expInvPotMap = expInvolvedPotentials.get(i);
            HashMap<String, String> resInvPotMap = resInvolvedPotentials.get(i);
            assertEquals(expInvPotMap.keySet().size(), resInvPotMap.keySet().size());
            for (String expKey : expInvPotMap.keySet()) {
                assertEquals(true, resInvPotMap.keySet().contains(expKey));
                assertEquals(expInvPotMap.get(expKey), resInvPotMap.get(expKey));
            }
        }

        assertEquals(maxNumberOfGroupAssignmentsPerParticle, resultGd.getMaxNumberOfGroupAssignmentsPerParticle());
    }
}
