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
package readdy.impl.io.in.par_particle;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParticleData;

/**
 *
 * @author schoeneberg
 */
public class ParamParticlesFileParserTest {

    public ParamParticlesFileParserTest() {
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
     * Test of parse method, of class ParamParticlesFileParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse param_particles...");
        String filename = "./test/testInputFiles/test_default/param_particles.xml";
        ParamParticlesFileParser instance = new ParamParticlesFileParser();
        instance.parse(filename);
        IParamParticlesFileData fileData = instance.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = fileData.get_particleDataList();

        IParticleData pd = dataList.get(0);
        assertEquals(pd.getId(), 0);
        assertEquals(pd.getTypeId(), 0);
        assertEquals(pd.getType(), "A");
        assertEquals(pd.getD(), 1E+6, 0);
        assertEquals(1.4, pd.get_defaultCollR(), 0);
        HashMap<Integer, Double> collisionRadiusMap = pd.getCollisionRadiusMap();
        assertEquals(1.4, collisionRadiusMap.get(0), 0);
        assertEquals(1, collisionRadiusMap.get(1), 0);
        assertEquals(1.4, collisionRadiusMap.get(2), 0);
        HashMap<Integer, Double> reactionRadiusMap = pd.getReactionRadiusMap();
        assertEquals(1, pd.get_defaultRctR(), 0);
        assertEquals(1, reactionRadiusMap.get(0), 0);
        assertEquals(1, reactionRadiusMap.get(1), 0);
        assertEquals(1, reactionRadiusMap.get(2), 0);

        pd = dataList.get(1);
        assertEquals(1, pd.getId(), 1);
        assertEquals(1, pd.getTypeId(), 1);
        assertEquals("B", pd.getType(), "B");
        assertEquals(0.5e+6, pd.getD(), 0);
        assertEquals(1.185, pd.get_defaultCollR(), 0);
        collisionRadiusMap = pd.getCollisionRadiusMap();
        assertEquals(5, collisionRadiusMap.get(0), 0);
        assertEquals(1.185, collisionRadiusMap.get(1), 0);
        assertEquals(1.185, collisionRadiusMap.get(2), 0);
        assertEquals(1, pd.get_defaultRctR(), 0);
        reactionRadiusMap = pd.getReactionRadiusMap();
        assertEquals(1, reactionRadiusMap.get(0), 0);
        assertEquals(1, reactionRadiusMap.get(1), 0);
        assertEquals(10, reactionRadiusMap.get(2), 0);

        pd = dataList.get(2);
        assertEquals(2, pd.getId());
        assertEquals(2, pd.getTypeId());
        assertEquals("ABcmplx", pd.getType());
        assertEquals(0.75e+6, pd.getD(), 0);
        assertEquals(3, pd.get_defaultCollR(), 0);
        collisionRadiusMap = pd.getCollisionRadiusMap();
        assertEquals(3, collisionRadiusMap.get(0), 0);
        assertEquals(3, collisionRadiusMap.get(1), 0);
        assertEquals(3, collisionRadiusMap.get(2), 0);
        assertEquals(3, pd.get_defaultRctR(), 0);
        reactionRadiusMap = pd.getReactionRadiusMap();
        assertEquals(3, reactionRadiusMap.get(0), 0);
        assertEquals(3, reactionRadiusMap.get(1), 0);
        assertEquals(3, reactionRadiusMap.get(2), 0);


        for (IParticleData pdata : dataList) {
            pdata.print();
        }
    }

    @Test(expected = RuntimeException.class)
    public void testParse_typo() {
        System.out.println("parse Param Particles");
        String filename = "./test/testInputFiles/test_default/param_particles_typoInside.xml";
        ParamParticlesFileParser instance = new ParamParticlesFileParser();
        instance.parse(filename);
    }
}
