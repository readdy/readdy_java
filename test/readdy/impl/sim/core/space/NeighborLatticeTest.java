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
package readdy.impl.sim.core.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.dtypes.IIntPair;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.core.space.INeighborListEntry;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.datatypes.IIntIterator;

/**
 *
 * @author schoeneberg
 */
public class NeighborLatticeTest {

    private static NeighborLattice neighborSearch;
    private static IParticleConfiguration particleConfiguration;

    public NeighborLatticeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //##############################################################################
        // geht the particle parameters as input
        //##############################################################################

        System.out.println("Neighbor Lattice Test...:");
        System.out.println();
        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_lattice/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_lattice/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();
        ArrayList<IParticleData> dataList = paramParticlesFileData.get_particleDataList();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();
        
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
        // get the topology coordinates File data as input
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_lattice/tplgy_coordinates.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();

        //##############################################################################
        // build up the actual class
        //##############################################################################


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        particleConfiguration = configFactory.createParticleConfiguration();



        double[][] latticeBounds = globalParameters.get_latticeBounds();
        double minBoxSize = globalParameters.get_latticeBoxSize();
        neighborSearch = new NeighborLattice(latticeBounds, minBoxSize,
                particleConfiguration,
                particleParameters);
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
     * Test of addParticle method, of class NeighborLattice2.
     */
    @Test
    public void testAdd_remove_andUpdate_Particle() {
        System.out.println("testAdd_remove_andUpdate_Particle");
        System.out.println();

        //##########################################################################################
        // CURRENT STATUS OF THE LATTICE
        //##########################################################################################
        int id = particleConfiguration.getLargestParticleId() + 1;
        double[] pos = new double[]{0, 0, 0.1};

        System.out.println("before insertion:");

        IIntIterator neighborIter = neighborSearch.getNeighbors(pos);
        printIter("neighborIter: ", neighborIter);

        Iterator<INeighborListEntry> entireNeighborList = neighborSearch.getAllNeighborsPlusDist();
        printEntireNeighborList("entireNeighborList ", entireNeighborList);
        HashMap<IIntPair, Double> internalNeighborList = neighborSearch.getNeighborList();
        printInternalNeighborList("internalNeighborList ", internalNeighborList);

        //##########################################################################################
        // ADD
        //##########################################################################################
        IParticle pNew = particleConfiguration.createParticle(1, pos);
        AdvancedSystemOut.println("new particle at position", pos, "");
        pNew.print();
        // this is necessary because the particleConfiguration itself has its own neighbor search object
        // changes there are not present in the neighborSearch object that is tested here.
        // therefor we have to manually add the particle here again.
        neighborSearch.addParticle(id, pos);

        System.out.println();
        System.out.println("after insertion:");


        neighborIter = neighborSearch.getNeighbors(pos);
        printIter("neighborIter: ", neighborIter);

        entireNeighborList = neighborSearch.getAllNeighborsPlusDist();
        printEntireNeighborList("entireNeighborList2 ", entireNeighborList);
        internalNeighborList = neighborSearch.getNeighborList();
        printInternalNeighborList("internalNeighborList ", internalNeighborList);

        System.out.println();
        //##########################################################################################
        // REMOVE
        //##########################################################################################
        int idToRemove = 0;
        neighborSearch.removeParticle(idToRemove);
        System.out.println();
        System.out.println("after removal of id :" + idToRemove);


        neighborIter = neighborSearch.getNeighbors(pos);
        printIter("neighborIter: ", neighborIter);

        entireNeighborList = neighborSearch.getAllNeighborsPlusDist();
        printEntireNeighborList("entireNeighborList2 ", entireNeighborList);
        internalNeighborList = neighborSearch.getNeighborList();
        printInternalNeighborList("internalNeighborList ", internalNeighborList);

        //##########################################################################################
        // UPDATE
        //##########################################################################################

        int idToUpdate = 1;
        double[] newPos = new double[]{-1, -2, 0};
        IParticle pToUpdate = particleConfiguration.getParticle(idToUpdate);
        particleConfiguration.setCoordinates(pToUpdate, newPos);


        System.out.println("---before update:");

        IIntIterator neighborIterNewPos = neighborSearch.getNeighbors(newPos);
        printIter("expected partner ids after update: ", neighborIterNewPos);

        entireNeighborList = neighborSearch.getAllNeighborsPlusDist();
        printEntireNeighborList("entireNeighborList ", entireNeighborList);
        internalNeighborList = neighborSearch.getNeighborList();
        printInternalNeighborList("internalNeighborList ", internalNeighborList);


        System.out.println("---afterUpdate :");

        neighborSearch.updatePosition(idToUpdate, newPos);

        entireNeighborList = neighborSearch.getAllNeighborsPlusDist();
        printEntireNeighborList("entireNeighborList ", entireNeighborList);
        internalNeighborList = neighborSearch.getNeighborList();
        printInternalNeighborList("internalNeighborList ", internalNeighborList);
        
        
        
        

    }

    private void printIter(String prefix, IIntIterator neighborIter) {
        System.out.print(prefix);
        while (neighborIter.hasNext()) {
            System.out.print(neighborIter.next() + ",");
        }
        System.out.println();
    }

    private void printEntireNeighborList(String prefix, Iterator<INeighborListEntry> entireNeighborList) {
        System.out.println(prefix);
        while (entireNeighborList.hasNext()) {
            INeighborListEntry nle = entireNeighborList.next();
            System.out.println(nle.getId1() + "," + nle.getId2() + "|" + nle.getDist() + ";");
        }
        System.out.println();
    }

    private void printInternalNeighborList(String prefix, HashMap<IIntPair, Double> internalNeighborList) {
        System.out.println(prefix);
        for (IIntPair pair : internalNeighborList.keySet()) {

            System.out.println(pair.get_i() + "," + pair.get_j() + "|" + internalNeighborList.get(pair) + ";");
        }
        System.out.println();
    }
}
