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
package readdy.impl.sim.core.config;

import java.util.ArrayList;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IDiffusionEngineFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.core.space.INeighborListEntry;
import readdy.impl.assembly.DiffusionEngineFactory;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;

/**
 *  This test makes sure, that the particle configurations duties are
 *  executed correctly.
 *  These duties include:
 * 
 *  1. create the initial particle setup correctly
 *  2. provide iterators over the particle list
 *  3. provide iterators over all pairs of particles by using 
 *      a particle lattice implementation 
 *  4. provide the possibility to get particle information
 *  5. provide particle creation functionality
 *      (make sure that particle iterators and the neighbor list is 
 *      updated correctly)
 *  6. provide particle removal functionality
 *      (same as 5: check if lists are updated correctly)
 *
 *  if these tests run correctly, the particle configuration does its job well.
 *
 * @author schoeneberg
 */
public class ParticleConfigurationTest {

    private static IParticleConfiguration particleConfiguration = null;

    public ParticleConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


        //##############################################################################
        // geht the particle parameters as input
        //##############################################################################

        System.out.println("ParticleParametersTEST...:");
        System.out.println();
        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_particleConfig/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();
        System.out.println("global max p interaction Radius: " + particleParameters.get_globalMaxParticleParticleInteractionRadius());


        //##############################################################################
        // geht the potential parameters as input
        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();


        //##############################################################################
        // determine lattice box size
        // it is important, that this happens, before the particleConfiguration assembly
        //##############################################################################
        ILatticeBoxSizeComputer latticeBoxSizeComputer = new LatticeBoxSizeComputer(
                particleParameters, 
                potentialInventory, 
                globalParameters);
        double latticeBoxSize = latticeBoxSizeComputer.getLatticeBoxSize();
        globalParameters.set_latticeBoxSize(latticeBoxSize);

        

        //##############################################################################
        // particle configuration
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_particleConfig/tplgy_coordinates.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();

        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        particleConfiguration = configFactory.createParticleConfiguration();

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

    @Test
    public void testGetNParticles() {
        System.out.println("=============================");
        System.out.println("getNParticles");
        int expResult = 3;
        int result = particleConfiguration.getNParticles();
        assertEquals(expResult, result);
    }

    /**
     * Test of particleIterator method, of class ParticleConfiguration.
     */
    @Test
    public void testParticleIterator() {
        System.out.println("=============================");
        System.out.println("testParticleIterator");
        Iterator<IParticle> iterator = particleConfiguration.particleIterator();

        int[] expResult_id = new int[]{0, 1, 2};
        int[] expResult_typeId = new int[]{0, 0, 0};
        double[][] expResult_coords = new double[][]{
            new double[]{2, 2, 1.5},
            new double[]{1, 3, 4.5},
            new double[]{0, 1, 2.5}
        };

        int counter = 0;
        while (iterator.hasNext()) {
            IParticle p = iterator.next();
            assertEquals(expResult_id[counter], p.get_id());
            assertEquals(expResult_typeId[counter], p.get_type());
            assertEquals(expResult_coords[counter][0], p.get_coords()[0], 0);
            assertEquals(expResult_coords[counter][1], p.get_coords()[1], 0);
            assertEquals(expResult_coords[counter][2], p.get_coords()[2], 0);

            System.out.println("p: " + p.get_id() + ", " + p.get_type() + ", "
                    + "[" + p.get_coords()[0]
                    + "," + p.get_coords()[1]
                    + "," + p.get_coords()[2] + "]");

            counter++;
        }

        assertEquals(counter, 3);
    }

    /**
     * Test of particlePairIterator method, of class ParticleConfiguration.
     */
    @Test
    public void testParticlePairIterator() {
        System.out.println("=============================");
        System.out.println("particlePairIterator");
        Iterator<INeighborListEntry> iter = particleConfiguration.particlePairIterator();
        int counter = 0;
        int[] expIds = new int[]{0, 2};
        double expectedDist = 2.44949;
        double resultDist = 0;
        int expNNeighbors = 3;
        boolean found = false;
        INeighborListEntry desiredEntry = null;
        while (iter.hasNext()) {

            INeighborListEntry entry = iter.next();
            System.out.println("neighborEntry: <id1,id2> <" + entry.getId1() + "," + entry.getId2() + "> dist: " + entry.getDist());
            if ((entry.getId1() == expIds[0] && entry.getId2() == expIds[1]) || (entry.getId1() == expIds[1] && entry.getId2() == expIds[0])) {
                System.out.println("desired entry!!!!");
                desiredEntry = entry;
                found = true;
            }
            ;



            counter++;
        }
        assertEquals(true, found);
        resultDist = desiredEntry.getDist();
        assertEquals(expectedDist, resultDist, 0.0001);
        assertEquals(expNNeighbors, counter);


    }

    /**
     * Test of getLargestParticleId method, of class ParticleConfiguration.
     */
    @Test
    public void testGetLargestParticleId() {
        System.out.println("=============================");
        System.out.println("testGetLargestParticleId");
        int expResult = 2;
        int result = particleConfiguration.getLargestParticleId();
        assertEquals(expResult, result);

    }

    /**
     * Test of getParticle method, of class ParticleConfiguration.
     */
    @Test
    public void testGetParticle() {
        System.out.println("=============================");
        System.out.println("getParticle");
        int id = 1;

        int[] expResult_id = new int[]{0, 1, 2};
        int[] expResult_typeId = new int[]{0, 0, 0};
        double[][] expResult_coords = new double[][]{
            new double[]{2, 2, 1.5},
            new double[]{1, 3, 4.5},
            new double[]{0, 1, 2.5}
        };

        for (int i = 0; i < expResult_id.length; i++) {

            IParticle p = particleConfiguration.getParticle(expResult_id[i]);
            assertEquals(expResult_id[i], p.get_id());
            assertEquals(expResult_typeId[i], p.get_type());
            assertEquals(expResult_coords[i][0], p.get_coords()[0], 0);
            assertEquals(expResult_coords[i][1], p.get_coords()[1], 0);
            assertEquals(expResult_coords[i][2], p.get_coords()[2], 0);

            System.out.println("p: " + p.get_id() + ", " + p.get_type() + ", "
                    + "[" + p.get_coords()[0]
                    + "," + p.get_coords()[1]
                    + "," + p.get_coords()[2] + "]");
        }
    }

    /**
     * Test of createParticle method, of class ParticleConfiguration.
     */
    @Test
    public void testCreateParticle() {
        System.out.println("=============================");
        System.out.println("createParticle");
        int type = 0;
        double[] position = new double[]{1, 2, 3};



        int nParticlesBefore = particleConfiguration.getNParticles();
        System.out.println("nParticles before creation: " + nParticlesBefore);
        IParticle newParticle = particleConfiguration.createParticle(type, position);
        int nParticlesAfter = particleConfiguration.getNParticles();
        System.out.println("nParticles after creation: " + nParticlesAfter);
        assertEquals(1, nParticlesAfter - nParticlesBefore);


        int newId = particleConfiguration.getLargestParticleId();
        System.out.println("getLargest particle ID: " + newId);
        assertEquals(3, newId);
        assertEquals(newId, newParticle.get_id());
        System.out.println("newParticle.get_id(): " + newParticle.get_id());
        assertEquals(3, newParticle.get_id(), 0);
        assertEquals(0, newParticle.get_type(), 0);
        assertEquals(1, newParticle.get_coords()[0], 0);
        assertEquals(2, newParticle.get_coords()[1], 0);
        assertEquals(3, newParticle.get_coords()[2], 0);

        IParticle newParticleReceived = particleConfiguration.getParticle(newId);
        assertEquals(3, newParticleReceived.get_id());
        assertEquals(0, newParticleReceived.get_type(), 0);
        assertEquals(1, newParticleReceived.get_coords()[0], 0);
        assertEquals(2, newParticleReceived.get_coords()[1], 0);
        assertEquals(3, newParticleReceived.get_coords()[2], 0);


        // the particle above should be in the middle of all three particles from
        // before and should create neighbors with all of them.
        // before we should have 2 neighbors, afterwards 4 neighbor list entries.

        Iterator<INeighborListEntry> iter = particleConfiguration.particlePairIterator();

        int counter = 0;
        int[][] expIds = new int[][]{new int[]{0, 2},
            new int[]{1, 2},
            new int[]{0, 1},
            new int[]{3, 0}, // new
            new int[]{3, 1}, // new
            new int[]{3, 2} // new
        };
        double[] expectedDist = new double[]{2.44949,
            3.0,
            3.316624,
            1.802776,
            1.802776,
            1.5
        };


        int expNNeighbors = 6;
        boolean found[] = new boolean[]{false, false, false, false, false, false};
        while (iter.hasNext()) {
            INeighborListEntry entry = iter.next();
            System.out.println("neighborEntry: <id1,id2> <" + entry.getId1() + "," + entry.getId2() + "> dist: " + entry.getDist());
            int i = 0;
            for (int j = 0; j < expIds.length; j++) {
                int[] expIdsPair = expIds[j];
                if (entry.getId1() == expIdsPair[0] && entry.getId2() == expIdsPair[1]
                        || entry.getId1() == expIdsPair[1] && entry.getId2() == expIdsPair[0]) {
                    System.out.println("found pair id " + j);
                    found[j] = true;
                    assertEquals(expectedDist[j], entry.getDist(), 0.0001);
                }
                i++;

            }

            counter++;
        }
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);
        }
        assertEquals(expNNeighbors, counter);
    }

    /**
     * Test of removeParticle method, of class ParticleConfiguration.
     */
    @Test
    public void testRemoveParticle() {
        System.out.println("=============================");
        System.out.println("removeParticle");
        int pIdToRemove = 3;
        IParticle pToRemove = particleConfiguration.getParticle(pIdToRemove);

        int nParticlesBefore = particleConfiguration.getNParticles();
        particleConfiguration.removeParticle(pToRemove.get_id());
        int nParticlesAfter = particleConfiguration.getNParticles();
        assertEquals(-1, nParticlesAfter - nParticlesBefore);


        Iterator<IParticle> iterator = particleConfiguration.particleIterator();

        int[] expResult_id = new int[]{0, 1, 2};
        int[] expResult_typeId = new int[]{0, 0, 0};
        double[][] expResult_coords = new double[][]{
            new double[]{2, 2, 1.5},
            new double[]{1, 3, 4.5},
            new double[]{0, 1, 2.5}
        };

        int counter = 0;
        while (iterator.hasNext()) {
            IParticle p = iterator.next();
            assertEquals(expResult_id[counter], p.get_id());
            assertEquals(expResult_typeId[counter], p.get_type());
            assertEquals(expResult_coords[counter][0], p.get_coords()[0], 0);
            assertEquals(expResult_coords[counter][1], p.get_coords()[1], 0);
            assertEquals(expResult_coords[counter][2], p.get_coords()[2], 0);

            System.out.println("p: " + p.get_id() + ", " + p.get_type() + ", "
                    + "[" + p.get_coords()[0]
                    + "," + p.get_coords()[1]
                    + "," + p.get_coords()[2] + "]");

            counter++;
        }

        assertEquals(counter, 3);

        //###############################################################
        //### check new neighboring Configuration...

        Iterator<INeighborListEntry> iter = particleConfiguration.particlePairIterator();

        counter = 0;
        int[][] expIds = new int[][]{new int[]{0, 2},
            new int[]{1, 2},
            new int[]{0, 1},};
        double[] expectedDist = new double[]{2.44949,
            3.0,
            3.316624,};


        int expNNeighbors = 3;
        boolean found[] = new boolean[]{false, false, false};
        while (iter.hasNext()) {
            INeighborListEntry entry = iter.next();
            System.out.println("neighborEntry: <id1,id2> <" + entry.getId1() + "," + entry.getId2() + "> dist: " + entry.getDist());
            int i = 0;
            for (int j = 0; j < expIds.length; j++) {
                int[] expIdsPair = expIds[j];
                if (entry.getId1() == expIdsPair[0] && entry.getId2() == expIdsPair[1]
                        || entry.getId1() == expIdsPair[1] && entry.getId2() == expIdsPair[0]) {
                    System.out.println("found pair id " + j);
                    found[j] = true;
                    assertEquals(expectedDist[j], entry.getDist(), 0.0001);
                }
                i++;

            }

            counter++;
        }
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);
        }
        assertEquals(expNNeighbors, counter);


    }

    /**
     * Test of setCoordinates method, of class ParticleConfiguration.
     */
    @Test
    public void testSetCoordinates() {
        System.out.println("=============================");
        System.out.println("setCoordinates");
        int pIdToChange = 1;
        IParticle pToChangeCoords = particleConfiguration.getParticle(pIdToChange);
        double[] newCoords = new double[]{1, 2, 3};
        particleConfiguration.setCoordinates(pToChangeCoords, newCoords);

        Iterator<IParticle> iterator = particleConfiguration.particleIterator();

        int[] expResult_id = new int[]{0, 1, 2};
        int[] expResult_typeId = new int[]{0, 0, 0};
        double[][] expResult_coords = new double[][]{
            new double[]{2, 2, 1.5},
            new double[]{1, 2, 3},
            new double[]{0, 1, 2.5}
        };

        int counter = 0;
        while (iterator.hasNext()) {
            IParticle p = iterator.next();
            assertEquals(expResult_id[counter], p.get_id());
            assertEquals(expResult_typeId[counter], p.get_type());
            assertEquals(expResult_coords[counter][0], p.get_coords()[0], 0);
            assertEquals(expResult_coords[counter][1], p.get_coords()[1], 0);
            assertEquals(expResult_coords[counter][2], p.get_coords()[2], 0);

            System.out.println("p: " + p.get_id() + ", " + p.get_type() + ", "
                    + "[" + p.get_coords()[0]
                    + "," + p.get_coords()[1]
                    + "," + p.get_coords()[2] + "]");

            counter++;
        }

        assertEquals(counter, 3);

        //###############################################################
        //### check new neighboring Configuration...

        Iterator<INeighborListEntry> iter = particleConfiguration.particlePairIterator();
        counter = 0;
        int[][] expIds = new int[][]{new int[]{0, 2},
            new int[]{1, 0},
            new int[]{1, 2}
        };
        double[] expectedDist = new double[]{2.44949,
            1.802776,
            1.5
        };


        int expNNeighbors = 3;
        boolean found[] = new boolean[]{false, false, false};
        while (iter.hasNext()) {
            INeighborListEntry entry = iter.next();
            System.out.println("neighborEntry: <id1,id2> <" + entry.getId1() + "," + entry.getId2() + "> dist: " + entry.getDist());

            int i = 0;
            for (int[] expIdsPair : expIds) {
                if (entry.getId1() == expIdsPair[0] && entry.getId2() == expIdsPair[1]
                        || entry.getId1() == expIdsPair[1] && entry.getId2() == expIdsPair[0]) {
                    break;
                }
                i++;

            }
            int foundPairId = i;
            found[foundPairId] = true;
            assertEquals(expectedDist[foundPairId], entry.getDist(), 0.0001);



            counter++;
        }
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);
        }
        assertEquals(expNNeighbors, counter);

    }
}
