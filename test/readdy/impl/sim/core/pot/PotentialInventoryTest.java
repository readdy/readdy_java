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
package readdy.impl.sim.core.pot;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;

/**
 *
 * @author schoeneberg
 */
public class PotentialInventoryTest {

    private static IPotentialInventory potentialInventory;

    public PotentialInventoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        //##############################################################################

        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_default/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        potentialInventory = potInvFactory.createPotentialInventory();
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
     * Test of createPotential method, of class PotentialInventory.
     */
    @Test
    public void testCreatePotential() {

        int[] exp_typeId = new int[]{0, 1};
        String[] exp_type = new String[]{"HARMONIC", "DISK"};
        int[] exp_order = new int[]{2, 1};
        String[][][] exp_paramData = new String[][][]{
            new String[][]{
                new String[]{"name", "", "attractiveDisk_1"},
                new String[]{"type", "", "DISK"},
                new String[]{"subtype", "", "attractive"},
                new String[]{"forceConst", "double", "1"},
                new String[]{"affectedParticleTypeIdPairs", "int[][]", "null"},
                new String[]{"affectedParticleIdPairs", "int[][]", "null"}
            },
            new String[][]{
                new String[]{"name", "", "harmonic_typeBound_spring"},
                new String[]{"type", "", "HARMONIC"},
                new String[]{"subtype", "", "spring"},
                new String[]{"forceConst", "double", "1"},
                new String[]{"center", "double[]", "[0.0,0.0,0.0]"},
                new String[]{"normal", "double[]", "[0.0,0.0,1.0]"},
                new String[]{"radius", "double", "1"},
                new String[]{"affectedParticleTypeIds", "int[]", "null"},
                new String[]{"affectedParticleIds", "int[]", "null"}
            }
        };

        ArrayList<HashMap<String, String>> parameters = new ArrayList();
        for (int i = 0; i < exp_paramData.length; i++) {
            HashMap<String, String> map = new HashMap();
            for (int j = 0; j < exp_paramData[i].length; j++) {
                map.put(exp_paramData[i][j][0], exp_paramData[i][j][2]);
            }
            parameters.add(map);
        }
        potentialInventory.createPotential(parameters.get(0));
        potentialInventory.createPotential(parameters.get(1));


    }

    /**
     * Test of doesPotentialExist method, of class PotentialInventory.
     */
    @Test
    public void testDoesPotentialExist() {
        System.out.println("doesPotentialExist");
        int[] potIds = new int[]{0, 1, 88};
        boolean[] expResult = new boolean[]{true, true, false};
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], potentialInventory.doesPotentialExist(potIds[i]));

        }
    }

    /**
     * Test of getPotential method, of class PotentialInventory.
     */
    @Test
    public void testGetPotential() {


        IPotential1 pot1 = (IPotential1) potentialInventory.getPotential(0);
        IPotential2 pot2 = (IPotential2) potentialInventory.getPotential(1);
    }

    /**
     * Test of getPotential method, of class PotentialInventory.
     */
    @Test(expected = java.lang.ClassCastException.class)
    public void testGetPotential_wronCast() {
        // IPotential1 pot1 = (IPotential1) potentialInventory.getPotential(0);
        IPotential1 pot2 = (IPotential1) potentialInventory.getPotential(1);
        IPotential2 pot1 = (IPotential2) potentialInventory.getPotential(0);
    }

    /**
     * Test of getPotentialOrder method, of class PotentialInventory.
     */
    @Test
    public void testGetPotentialOrder() {
        int[] potId = new int[]{0, 1};
        int[] expOrder = new int[]{1, 2};
        for (int i = 0; i < expOrder.length; i++) {
            assertEquals(expOrder[i], potentialInventory.getPotentialOrder(potId[i]));
        }

    }
}
