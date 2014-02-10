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

import cern.colt.map.OpenIntObjectHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.sim.core.particle.IParticle;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.util.DoubleArrays;
import statlab.base.util.MultiIndex;

/**
 *
 * @author johannesschoeneberg
 */
public class BoxIteratorTest {
    // the actual lattice

    private static int dim;
    private static double[] cellSize;
    private static Box[] grid;
    private static MultiIndex mindex;
    private static double[] O;
    private static OpenIntObjectHashMap particleId2box_map = new OpenIntObjectHashMap(1000);
    
    // the particles
    
    private static ArrayList<IParticle>  pListOthers ;
    private static IParticle pCenter;
    // the iterator
    private static BoxIterator boxIterator;

    public BoxIteratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        double minBoxSize = 2;
        double[][] bounds = new double[][]{new double[]{-3, 3}, new double[]{-3, 3}, new double[]{-3, 3}};
        dim = bounds.length;
        O = DoubleArrays.getColumn(bounds, 0);
        AdvancedSystemOut.println("Origin O ", O, "");
        double[] Ext = DoubleArrays.getColumn(bounds, 1);
        int[] N = new int[dim];
        cellSize = new double[dim];
        for (int i = 0; i < dim; i++) {
            double d = bounds[i][1] - bounds[i][0];
            Ext[i] = d;
            N[i] = (int) (d / minBoxSize);
            cellSize[i] = d / (double) N[i];
        }

        System.out.println("N.length " + N.length);
        AdvancedSystemOut.println("N ", N, "");
        AdvancedSystemOut.println("int[N.length] ", new int[N.length], "");
        System.out.println("Ext.length " + Ext.length);
        AdvancedSystemOut.println("Ext ", Ext, "");


        mindex = new MultiIndex(new int[N.length], N);

        grid = new Box[mindex.size()];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = new Box(mindex.id2index(i));
        }

        // now we setted up our dummy lattice...
        // now go and input some particles there


        // in box of center particle
        pCenter = new Particle(0, 0, new double[]{0.5, -0.5, 0.5});

        // generate other particles
        pListOthers = new ArrayList();
        int counter = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {

                    IParticle pNew = new Particle(counter, 0, new double[]{1.5 * (i - 1), 1.5 * (j - 1), 1.5 * (k - 1)});
                    counter++;
                    pListOthers.add(pNew);

                }
            }
        }


        System.out.println("<particleList>");
        for (IParticle p : pListOthers) {
            p.print();
            addParticle(p.get_id(), p.get_coords());
        }
        System.out.println("</particleList>");



        // add all particles to the lattice
        System.out.println("");
        System.out.println("add center particle from which we want to compute neighbors");
        addParticle(pCenter.get_id(),pCenter.get_coords());
        
        
        
        // now, get the iterator for the center particle
        System.out.println("");
        System.out.println("construct the iterator...");
        boxIterator = new BoxIterator(grid, dim, mindex, crd2box(pCenter.get_coords()));

        System.out.println("");
        System.out.println("start testing...");
    }

    private static Box crd2box(double[] pos) {
        System.out.println("<crd2box()>");
        int[] index = new int[dim];
        for (int i = 0; i < dim; i++) {
            index[i] = (int) Math.floor((pos[i] - O[i]) / cellSize[i]);
        }
        AdvancedSystemOut.println("idx: ", index, "");
        System.out.println("multiIndexId " + mindex.index2id(index));
        System.out.println("</crd2box()>");
        if (mindex.isValid(index)) {
            return (grid[mindex.index2id(index)]);
        } else {
            System.out.println("index out of lattice bounds:");
            int[] min = new int[]{0, 0, 0};
            int[] max = new int[]{0, 0, 0};
            for (int i = 0; i < min.length; i++) {
                min[i] = mindex.getMin(i);
                max[i] = mindex.getMax(i);
            }
            AdvancedSystemOut.println("mindex.getMin(dim) [inclusive]", min, "");
            AdvancedSystemOut.println("mindex.getMax(dim) [exclusive]", max, "");
            AdvancedSystemOut.println("index tried to evaluate in the grid: ", index, "");
            throw new RuntimeException("index out of lattice bounds!");
        }

    }

    /**
     * Adds particle to grid
     *
     * @param id
     * @param pos
     */
    public static void addParticle(int id, double[] pos) {

        // check if this particle does not exist yet.
        if (particleId2box_map.containsKey(id)) {
            throw (new IllegalArgumentException("Particle " + id + " already exists in grid."));
        }

        // identify box
        Box box = crd2box(pos);
        // add particle id to grid
        box.add(id);

        // add particle id to particle list
        particleId2box_map.put(id, box);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class BoxIterator.
     */
    @Test
    public void testInitialize_int() {
        System.out.println("--------------------------------------------------");
        System.out.println("TEST initialize excluded index");
        System.out.println("print status before initialization:");
        boxIterator.printStatus();
        System.out.println("initialize with the particle id 0...");
        boxIterator.initialize(0);
        System.out.println("print status after initialization:");
        boxIterator.printStatus();
        
        HashSet<Integer> expectedParticleIdsToFindAsNeighbors = new HashSet();
        // we expect to find all particles, except the particle from which we
        // initiated the neighbor search from
        for(IParticle p:pListOthers){
            expectedParticleIdsToFindAsNeighbors.add(p.get_id());
        }
        
        
        
        // start investigating the neighbor iterator
        assertTrue(!expectedParticleIdsToFindAsNeighbors.isEmpty());
        while(boxIterator.hasNext()){
            boxIterator.printStatus();
            int particleId = boxIterator.next();
            boolean particleIdIsPresent = expectedParticleIdsToFindAsNeighbors.contains(particleId);
            if(!particleIdIsPresent){
                System.out.println("we did not expect the following particleID: "+particleId);
            }
            assertTrue(particleIdIsPresent);
            if(particleIdIsPresent){
                expectedParticleIdsToFindAsNeighbors.remove(particleId);
            }
            
        }
        
        // make shure, that we found all expected particle Ids!
        assertTrue(expectedParticleIdsToFindAsNeighbors.isEmpty());
    }

    /**
     * Test of initialize method, of class BoxIterator.
     */
    @Test
    public void testInitialize_0args() {
        System.out.println("--------------------------------------------------");
        System.out.println("TEST initialize no arguments");
        System.out.println("print status before initialization:");
        boxIterator.printStatus();
        System.out.println("initialize...");
        boxIterator.initialize();
        System.out.println("print status after initialization:");
        boxIterator.printStatus();
        
        HashSet<Integer> expectedParticleIdsToFindAsNeighbors = new HashSet();
        // we expect to find all particles, plus the particle from which we
        // initiated the neighbor search from
        for(IParticle p:pListOthers){
            expectedParticleIdsToFindAsNeighbors.add(p.get_id());
        }
        expectedParticleIdsToFindAsNeighbors.add(pCenter.get_id());
        
        
        // start investigating the neighbor iterator
        assertTrue(!expectedParticleIdsToFindAsNeighbors.isEmpty());
        while(boxIterator.hasNext()){
            boxIterator.printStatus();
            int particleId = boxIterator.next();
            boolean particleIdIsPresent = expectedParticleIdsToFindAsNeighbors.contains(particleId);
            assertTrue(particleIdIsPresent);
            if(particleIdIsPresent){
                expectedParticleIdsToFindAsNeighbors.remove(particleId);
            }
            
        }
        
        // make shure, that we found all expected particle Ids!
        assertTrue(expectedParticleIdsToFindAsNeighbors.isEmpty());
        
    }

    /**
     * Test of hasNext method, of class BoxIterator.
     */
    @Test
    public void testHasNext() {
        System.out.println("--------------------------------------------------");
        System.out.println("TEST hasNext");
        System.out.println("print status before initialization:");
        
        // we expect it to have nothing because it is not yet initialized
        boxIterator.printStatus();
        boolean expectedResult = false;
        assertEquals(expectedResult, boxIterator.hasNext());
        
        
        System.out.println("initialize with the particle id 0...");
        boxIterator.initialize(0);
        System.out.println("print status after initialization:");
        boxIterator.printStatus();
        // now we expect it to have something because it is properly initialized
        expectedResult = true;
        assertEquals(expectedResult, boxIterator.hasNext());
    }

    /**
     * Test of next method, of class BoxIterator.
     */
    @Test
    public void testNext() {
        System.out.println("--------------------------------------------------");
        System.out.println("TEST next");
        
        
        
        
        System.out.println("initialize with the particle id 0...");
        boxIterator.initialize(0);
        System.out.println("print status after initialization:");
        boxIterator.printStatus();
        int expectedResult = 1;
        assertEquals(expectedResult, boxIterator.next());
    }

    /**
     * Test of printStatus method, of class BoxIterator.
     */
    @Test
    public void testPrintStatus() {
        System.out.println("--------------------------------------------------");
        System.out.println("TEST printStatus");

        boxIterator.printStatus();

    }
}
