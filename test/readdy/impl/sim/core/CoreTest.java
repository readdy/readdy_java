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
package readdy.impl.sim.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import readdy.api.assembly.IReactionObserverFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.bd.IDiffusionEngine;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleAllAccess;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.core.space.INeighborListEntry;
import readdy.impl.assembly.Core_Default_Factory;
import readdy.impl.assembly.DiffusionEngineFactory;
import readdy.impl.assembly.ElementalReactionManagerFactory_externalFile;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.assembly.ReactionObserverFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.rct_elmtlRk.ReactElmtlRkFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.impl.io.out.XYZ_Writer;
import readdy.impl.sim.core.rk.ReactionsOccurredExeption;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.core.space.NeighborListEntry;
import statlab.base.datatypes.IIntIterator;
import statlab.base.util.DoubleArrays;

/**
 * In order to test the core, we integrate a small toy system and look if
 * everything works well.
 *
 * The toy system consists of 5 disk potentials that are in ordered in space to
 * generate a ring like shape.
 *
 * Particles of types B,C,D,E and F exist in the input topology who reside each
 * on a different disk surface. Particles A, who are not included in the system
 * would not be affected by any disk potential.
 *
 * Elemental particle reactions are fed into the system that govern particle
 * reactions from B->C, C->D, D->E, E->F and F->B
 *
 * The reactions are thrown as exceptions by the core and have to be catched
 * manually within this test engine. If everything works well, the result should
 * be the integrated dynamics of multiple particles, initially starting at one
 * point in space, first finding their respective disk membranes where they
 * should reside and then starting to react to different types which, given the
 * spatial ordering of the disk membranes, should result in a ring like particle
 * circulation.
 *
 * The output .xyz file can be viewed via e.g. VMD.
 *
 * In order to have a look on the trajectory via VMD, use the scripts provided
 * in ReaDDy/test/core_test/ouput/ there you will first have to run the
 * ./configure file in order to set the path in your visualization script
 * correctly. Then you can open the vmd_visualization.tcl script in VMD.
 *
 * REMARK: XYZ trajectories are displayed in VMD such, that the first trajectory
 * snapshot determines the color for particle i, residing in row N for the
 * entire trajectory. If particle i is changed due to reaction to a different
 * type and should be colored differently, if it still resides in row N within
 * the trajectory snapshots, it is still colored as in the first snapshot. This
 * coreTest output orders the particles in each snapshot according to their
 * particle type to avoid that problem but can not fully resolve it
 * unfortunately. For that reason you may see some particles residing in a Red
 * disk that are not colored red as all other particles residing there. But this
 * is a visualization problem, no simulation problem.
 *
 *
 * @author schoeneberg
 */
public class CoreTest {

    private static IParticleConfiguration particleConfiguration;
    private static IGlobalParameters globalParameters;
    private static IDiffusionEngine diffusionEngine;
    private static IReactionObserver reactionObserver;
    private static ICore core;
    private static XYZ_Writer out;
    private static boolean checkNeighborListConsistency = true;
    private static boolean checkNeighborListConsistency2 = true;
    private static int checkNeighborListConsistencyEveryXSteps = 100;

    private static ArrayList<INeighborListEntry> foundNeighbors_bruteForce ;
    
    public CoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //##############################################################################
        // geht the particle parameters as input
        //##############################################################################

        System.out.println("CoreTEST...:");
        System.out.println();
        System.out.println("parse param_global...");
        String paramGlobalFilename = "./test/testInputFiles/test_core/coreTest_param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_core/coreTest_param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

        

        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------

        //##############################################################################
        // get the potential parameters as input
        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        String tplgyPotentialFilename = "./test/testInputFiles/test_core/coreTest_tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();

        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        IPotentialManager potentialManager = potentialManagerFactory.createPotentialManager();


        //##############################################################################
        // create the diffusion Engine
        //##############################################################################
        IDiffusionEngineFactory diffEngineFactory = new DiffusionEngineFactory();
        diffEngineFactory.set_particleParameters(particleParameters);
        diffEngineFactory.set_potentialManager(potentialManager);
        diffusionEngine = diffEngineFactory.createDiffusionEngine();
        
        //##############################################################################
        // determine lattice box size
        // it is important, that this happens, before the particleConfiguration assembly
        //##############################################################################
        ILatticeBoxSizeComputer latticeBoxSizeComputer = new LatticeBoxSizeComputer(
                particleParameters, 
                potentialInventory, 
                globalParameters);
        double latticeBoxSize = latticeBoxSizeComputer.getLatticeBoxSize();
        System.out.println("latticeBoxSize!!! :" +latticeBoxSize);
        globalParameters.set_latticeBoxSize(latticeBoxSize);

        
        //##############################################################################
        // particleConfiguration
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_core/coreTest_tplgy_coordinates.xml";
        //String tplgyCoordinatesFileName = "./test/testInputFiles/test_core/coreTest_tplgy_coordinates_only10Particles.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        particleConfiguration = configFactory.createParticleConfiguration();
        
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        System.out.println("parse elementalReactions...");
        String filenameElementalReactions = "./test/testInputFiles/test_core/coreTest_react_elmtlRk.xml";
        IReactElmtlRkFileParser reactElmtlRkFileParser = new ReactElmtlRkFileParser();
        reactElmtlRkFileParser.parse(filenameElementalReactions);
        IReactElmtlRkFileData reactElmtlRkFileData = reactElmtlRkFileParser.get_reactElmtlRkFileData();

        ElementalReactionManagerFactory_externalFile elmtlRkManagerFactory = new ElementalReactionManagerFactory_externalFile();
        elmtlRkManagerFactory.set_particleParameters(particleParameters);
        elmtlRkManagerFactory.set_reactElmtlRkFileData(reactElmtlRkFileData);

        IElementalReactionManager elementalReactionManager = elmtlRkManagerFactory.createElementalRactionManager();

        IReactionObserverFactory reactionObserverFactory = new ReactionObserverFactory();
        reactionObserverFactory.set_elementalReactionManager(elementalReactionManager);
        reactionObserverFactory.set_particleParameters(particleParameters);
        reactionObserver = reactionObserverFactory.createReactionObserver();

        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        Core_Default_Factory coreFactory = new Core_Default_Factory();

        coreFactory.set_ParticleConfiguration(particleConfiguration);
        coreFactory.set_DiffusionEngine(diffusionEngine);
        coreFactory.set_ReactionObserver(reactionObserver);

        core = coreFactory.createCore();


        String path = "./test/testInputFiles/test_core/output/";
        String outFileName = path + "coreTest_coords.xyz";
        out = new XYZ_Writer();
        out.open(outFileName);
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
     * Test of step method, of class Core.
     */
    @Test
    public void testStep() throws Exception {
        System.out.println("step");

        printCurrentParticleCoordinates(-1);
        int nSteps = (int) globalParameters.get_nSimulationSteps();
        long t = 0;
        for (int i = 0; i < nSteps; i++) {
            //System.out.println("step " + i);

            try {
                core.step(i);
            } catch (ReactionsOccurredExeption ex) {
                System.out.println("rk");
                ArrayList<IOccurredElementalReaction> occRkList = core.get_OccurredElementalReactions();

                // manual reaction evaluation
                for (IOccurredElementalReaction occRk : occRkList) {
                    occRk.print();
                    IParticleAllAccess pAA = (IParticleAllAccess) occRk.get_involvedParticles()[0];
                    switch (occRk.get_elmtlRkId()) {
                        case 0:
                            //pAA.set_typeId(2);
                            particleConfiguration.removeParticle(pAA.get_id());
                            break;
                        case 1:
                            pAA.set_typeId(3);
                            break;
                        case 2:
                            pAA.set_typeId(4);
                            break;
                        case 3:
                            pAA.set_typeId(5);
                            break;
                        case 4:
                            pAA.set_typeId(1);
                            break;
                    }
                }


            }

            if (i % checkNeighborListConsistencyEveryXSteps == 0 && i != 0) {
                System.out.println(i);
                System.out.println("Neighbor List consistency Check...");


                
                computeBruteForceNeighborList();
                //************************************************************
                // 1. Check the algorithm neighbor list for consistency
                //************************************************************
                if (checkNeighborListConsistency == true) {
                    Iterator<INeighborListEntry> pairIterator = particleConfiguration.particlePairIterator();
                    while (pairIterator.hasNext()) {
                        System.out.println("--------");
                        INeighborListEntry nle = pairIterator.next();
                        nle.print();
                        assertEquals(true, findNeighborInBruteForceList(nle.getId1(), nle.getId2(), nle.getDist()));
                         
                    }
                }
                //************************************************************
                // 2. Check the NeighborList query for coordinates against the neighbor list for consistency
                //************************************************************
                //particleConfiguration.getNeighboringParticleIds(new double[]{1000,10000,10000});
                if (checkNeighborListConsistency2 == true) {
                    System.out.println("2. Check the NeighborList query for coordinates against the neighbor list for consistency");
                    // first, test the case that you want to have all neighbors of a coordinate
                    // that happens to be an other particle itself
                    Iterator<IParticle> particleIterator = particleConfiguration.particleIterator();
                    while (particleIterator.hasNext()) {
                        IParticle p0 = particleIterator.next();
                        IIntIterator particleIdIterator = particleConfiguration.getNeighboringParticleIds(p0.get_coords());
                        while (particleIdIterator.hasNext()) {
                            int id_p1 = particleIdIterator.next();
                            if (p0.get_id() != id_p1) {
                                IParticle p1 = particleConfiguration.getParticle(id_p1);
                                assertEquals(true, findNeighborInBruteForceList(p0.get_id(), id_p1, DoubleArrays.distance(p0.get_coords(),p1.get_coords())));
                            }
                        }

                    }
                }
            }


            // test, secondly, neighbors of coordinates that are arbitrary

            if (i % 100 == 0 && i != 0) {
                System.out.println(i);
                printCurrentParticleCoordinates(i);
            }
        }
        foundNeighbors_bruteForce.clear();
        out.close();
    }

    private void printCurrentParticleCoordinates(int stepId) {
        //out.printNewStepLine(core.get_ParticleConfiguration().getNParticles(), "comment line...");
        ArrayList<ArrayList<String>> dataReadyForOutput = new ArrayList();
        Iterator<IParticle> particleIterator = core.get_ParticleConfiguration().particleIterator();
        HashMap<Integer, ArrayList<IParticle>> sortedParticleMap = new HashMap();
        int currentHighestParticleTypeId = 0;
        while (particleIterator.hasNext()) {
            IParticle p = particleIterator.next();
            int pTypeId = p.get_type();
            if (pTypeId > currentHighestParticleTypeId) {
                currentHighestParticleTypeId = pTypeId;
            }
            if (sortedParticleMap.containsKey(pTypeId)) {
                sortedParticleMap.get(pTypeId).add(p);
            } else {
                ArrayList<IParticle> list = new ArrayList();
                list.add(p);
                sortedParticleMap.put(pTypeId, list);
            }

        }
        for (int pTypeId = 0; pTypeId <= currentHighestParticleTypeId; pTypeId++) {


            if (sortedParticleMap.containsKey(pTypeId)) {
                for (IParticle p : sortedParticleMap.get(pTypeId)) {
                    ArrayList<String> line = new ArrayList();
                    line.add(p.get_id() + "");
                    line.add(p.get_type() + "");
                    for (double c : p.get_coords()) {
                        line.add(c + "");
                    }
                    dataReadyForOutput.add(line);
                }
            }
        }
        out.write(stepId, new DataReadyForOutput(dataReadyForOutput));
    }

    private ArrayList<INeighborListEntry> computeBruteForceNeighborList() {

        //************************************************************
        // 1. Generate a brute force neighbor list
        //************************************************************
        foundNeighbors_bruteForce = new ArrayList();

        double latticeBoxSize = globalParameters.get_latticeBoxSize();
        double maximalDistanceInLattice = DoubleArrays.distance(
                new double[]{0, 0, 0},
                new double[]{2.1 * latticeBoxSize, 2.1 * latticeBoxSize, 2.1 * latticeBoxSize});
        //System.out.println("globalParameters latticeBoxSize "+latticeBoxSize);
        // generate a neighbor List in the brute force way:
        Iterator<IParticle> particleIterator0 = particleConfiguration.particleIterator();

        while (particleIterator0.hasNext()) {
            IParticle p0 = particleIterator0.next();
            Iterator<IParticle> particleIterator1 = particleConfiguration.particleIterator();
            while (particleIterator1.hasNext()) {
                IParticle p1 = particleIterator1.next();
                if (p0 != p1) {
                    double dist = DoubleArrays.distance(p0.get_coords(), p1.get_coords());
                    if (dist < maximalDistanceInLattice) {
                        foundNeighbors_bruteForce.add(new NeighborListEntry(p0.get_id(), p1.get_id(), dist));
                    }
                }
            }
        }

        return foundNeighbors_bruteForce;

    }

    private boolean findNeighborInBruteForceList(int id1, int id2, double dist) {
        
        System.out.println("list:");
        for (INeighborListEntry nle_bruteForce : foundNeighbors_bruteForce) {
            //nle_bruteForce.print();
            if ((id1 == nle_bruteForce.getId1()
                    && id2 == nle_bruteForce.getId2())
                    || (id1 == nle_bruteForce.getId2()
                    && id2 == nle_bruteForce.getId1())) {
                
                System.out.println("dist: " + dist + " vs. " + nle_bruteForce.getDist());
                return true;
            }
        }
        
            System.out.println("Not found!!!");
            particleConfiguration.getParticle(id1).print();
            particleConfiguration.getParticle(id2).print();
            /*
             for (INeighborListEntry nle_bruteForce : foundNeighbors_bruteForce) {
             nle_bruteForce.print();
             }
             */
        
        return false;
    }
}
