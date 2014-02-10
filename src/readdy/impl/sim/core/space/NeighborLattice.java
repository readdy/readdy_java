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

import cern.colt.map.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import readdy.api.dtypes.IIntPair;
import readdy.api.sim.core.space.INeighborListEntry;

import readdy.api.sim.core.space.INeighborSearch;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.impl.dtypes.IntPair;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.datatypes.IIntIterator;
import statlab.base.util.Counter;
import statlab.base.util.DoubleArrays;
import statlab.base.util.IntArrays;
import statlab.base.util.MultiIndex;


/**
 * Fixed-size 3D lattice to quickly calculate neighbors between particles
 *
 * In this simple lattice we only search neighbors within the 27 boxes around the
 * box containing the particle of interest. Therefore, box length needs to be
 * equal to the largest interaction distance between any pair of particles.
 * Note that in presence of particles of unequal size or very different interaction
 * distances, this implementation may be inefficient.
 *
 * Main features:
 * - Add particles
 * - Given a unique particle ID, find the box this particle is located in
 *
 * @author noe, schoeneberg
 */
public class NeighborLattice implements INeighborSearch {

    private boolean IGNORE_PARTICLES_OUT_OF_GRID = false;
    /**
     * If this flag is enabled, a distance criterion function is applied to each neighborlist updating step.
     * It ensures, that only those neighbors enter the neighbor list, that actually are closer together than their maximal
     * particle interaction distance. This decreases the neighbor list but adds a surmount of distance computations
     * to the overall lattice maintainance. Apart from these runtime issues the simulation outcome is equivalent.
     *
     * It is best used when there exist few particles with a large interaceion radius and many with a small one.
     * In this scenario the lattice spacing has to be large to cope for the long range interactions, while only a
     * few particles are actually affected by it. Here it makes sense to use the distance criterion.
     */
    private boolean DISTANCE_CRITERION_TO_DECREASE_NEIGHBORLIST_SIZE_ENABLED = false;
    // grid coordinates
    private Box[] grid;  // each grid cell knows its particle IDs
    private int dim; // dimension
    private MultiIndex mindex; // maps box ids to box indexes
    private double[] cellSize;  // width of grid cells
    private double[] O; // grid origin
    private double[] Ext; // extension from origin
    private int[] N; // number of boxes in each direction
    private double[] bounds; // grid boundaries
    // list of all particles. link ID -> Box
    private OpenIntObjectHashMap particleId2box_map = new OpenIntObjectHashMap(1000);
    private IParticleConfiguration particleConfiguration = null;
    private IParticleParameters particleParameters = null;
    // map pair-> internal dist between the pair members
    HashMap<IIntPair, Double> neighborList = new HashMap();

    /**
     *
     * @param bounds grid boundaries in the form {{xmin,xmax}, {ymin,ymax},...}. Also determines the dimensionality of the lattice
     * @param minBoxSize minimum box size in any direction
     */
    public NeighborLattice(double[][] bounds, double minBoxSize,
            IParticleConfiguration particleConfig,
            IParticleParameters particleParameters) {
        System.out.println("---------------------------------------");
        System.out.println("NeighborLattice construction... ");


        this.dim = bounds.length;

        this.O = DoubleArrays.getColumn(bounds, 0);
        this.Ext = DoubleArrays.getColumn(bounds, 1);
        this.N = new int[dim];
        this.cellSize = new double[dim];
        for (int i = 0; i < dim; i++) {
            double d = bounds[i][1] - bounds[i][0];
            Ext[i] = d;
            N[i] = (int) (d / minBoxSize);
            cellSize[i] = d / (double) N[i];
        }

        this.mindex = new MultiIndex(new int[N.length], N);
        this.grid = new Box[mindex.size()];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = new Box(mindex.id2index(i));
        }

        System.out.print("num Dimensions:          " + dim + "\n");
        System.out.print("latice origin:           ");
        DoubleArrays.print(O);
        System.out.println();
        System.out.print("latice extension:        ");
        DoubleArrays.print(Ext);
        System.out.println();
        System.out.print("num boxes per dimension: ");
        IntArrays.print(N);
        System.out.print("box edge length per dim: ");
        DoubleArrays.print(cellSize);
        System.out.println();
        System.out.print("total num boxes:         " + grid.length + "\n");

        this.particleParameters = particleParameters;
        this.particleConfiguration = particleConfig;

        Iterator<IParticle> iter = particleConfig.particleIterator();

        while (iter.hasNext()) {

            IParticle p = iter.next();
            addParticle(p.get_id(), p.get_coords());
        }
        // after adding all particles sequentially, now determine all 
        // neighbor list distances and neighbors correctly for a correct
        // initialization
        

                

        System.out.println("... lattice ready.");
        System.out.println("---------------------------------------");
        //assembleListOfAllNeighbors();
        //printLattice();
    }

    /**
     * Returns the box containing the given coordinates
     * @param crd
     * @return
     */
    public int[] getLatticeBoxIndex(double[] coords) {
        int[] index = new int[dim];
        for (int i = 0; i < dim; i++) {
            index[i] = (int) Math.floor((coords[i] - O[i]) / cellSize[i]);
        }
        return index;
    }

    private Box crd2box(double[] pos) {
        int[] index = new int[dim];
        for (int i = 0; i < dim; i++) {
            index[i] = (int) Math.floor((pos[i] - O[i]) / cellSize[i]);
        }

        if (mindex.isValid(index)) {
            return (grid[mindex.index2id(index)]);
        } else {
            System.out.println("index out of lattice bounds:");
            int[] min = new int[]{0,0,0};
            int[] max = new int[]{0,0,0};
            for (int i = 0; i < min.length; i++) {
                min[i] = mindex.getMin(i);
                max[i] = mindex.getMax(i);
            }
            AdvancedSystemOut.println("mindex.getMin(dim) [inclusive]",min,"");
            AdvancedSystemOut.println("mindex.getMax(dim) [exclusive]",max,"");
            AdvancedSystemOut.println("index tried to evaluate in the grid: ", index, "");
            throw new RuntimeException("index out of lattice bounds!");
        }

    }

    private double[] box2crd(Box box) {
        double[] pos = new double[dim];
        for (int i = 0; i < dim; i++) {
            pos[i] = (double) box.index[i] * cellSize[i] + O[i];
        }
        return pos;

    }

 //----------------------------------------------------------------------------------------

    public void updateAdd(IParticle p) {
        addParticle(p.get_id(), p.get_coords());
    }

    /**
     * Adds particle to grid
     * @param id
     * @param pos
     */
    public void addParticle(int id, double[] pos) {

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
        // neighborlist maintainance
        addAllNeighborsOfParticleIdToNeighborList(id);
    }

    //----------------------------------------------------------------------------------------

    public void updateRemove(IParticle p) {
        removeParticle(p.get_id());
    }

    public void removeParticle(int id) {
        
        // remove all neighbors that it induced
        removeAllNeighborsOfParticleIdFromNeighborList(id);

        // check if this particle does not exist yet.
        // get box
        Box box = (Box) particleId2box_map.get(id);

        // remove from box
        box.remove(new Integer(id));
        // remove from list
        particleId2box_map.removeKey(id);
    }


    //----------------------------------------------------------------------------------------

    public void updatePosition(IParticle p) {
        updatePosition(p.get_id(), p.get_coords());
    }


    /**
     * Updates the position of an existing particle
     * @param id
     * @param pos
     */
    public void updatePosition(int id, double[] pos) {

        // what is the old box
        Box oldbox = (Box) particleId2box_map.get(id);

        // identify new box
        Box newbox = crd2box(pos);
        

        // do we have to change boxes
        if (oldbox != newbox) {

            // update the neighborList
            removeAllNeighborsOfParticleIdFromNeighborList(id);

            oldbox.remove(id);
            newbox.add(id);

            particleId2box_map.put(id, newbox);

            addAllNeighborsOfParticleIdToNeighborList(id);
        } else {            
            
            if (DISTANCE_CRITERION_TO_DECREASE_NEIGHBORLIST_SIZE_ENABLED) {
                System.out.println("distance criterion enabled");
                recheckAllNeighborsOfParticleIdAndAddThemToNeighborList(id);
            }
        }
    }
   
  

    private void addAllNeighborsOfParticleIdToNeighborList(int id) {
        IIntIterator newNeighbors = getNeighbors(id);

        while (newNeighbors.hasNext()) {
            int newNeighborPartnerId = newNeighbors.next();
            IIntPair newPair = new IntPair(id, newNeighborPartnerId);
            IParticle p0 = particleConfiguration.getParticle(id);
            IParticle p1 = particleConfiguration.getParticle(newNeighborPartnerId);
            double dist = DoubleArrays.distance(p0.get_coords(), p1.get_coords());
            if (DISTANCE_CRITERION_TO_DECREASE_NEIGHBORLIST_SIZE_ENABLED) {
                if (distanceCriterionToBeNeighborValid(p0, p1, dist)) {
                    neighborList.put(newPair, dist);

                } else {
                }
            } else {
                neighborList.put(newPair, dist);
            }
        }
    }

    private void removeAllNeighborsOfParticleIdFromNeighborList(int id) {
        IIntIterator oldNeighbors = getNeighbors(id);
        
        while (oldNeighbors.hasNext()) {
            int oldNeighborPartnerId = oldNeighbors.next();
            
            IIntPair oldPair = new IntPair(id, oldNeighborPartnerId);
            if (neighborList.containsKey(oldPair)) {
                neighborList.remove(oldPair);
            } else {
            }
        }
    }
       

    private boolean distanceCriterionToBeNeighborValid(IParticle a, IParticle b, double dist) {
        //return true;

        boolean result = false;

        result = (dist < particleParameters.get_maxPInteractionRadius(a.get_type(), b.get_type()));
        return result;

    }

    //##############################################################################
    // Lattice Maintainance and Reset Functions
    //##############################################################################
    
    public void recomputeLattice() {
        resetLattice();
        
        Iterator<IParticle> iter = particleConfiguration.particleIterator();

        while (iter.hasNext()) {

            IParticle p = iter.next();
            addParticle(p.get_id(), p.get_coords());
        }
        
    }
    
    public void resetLattice(){
        neighborList.clear();
        particleId2box_map.clear();
    }
    
    
    public void updateDistances() {
        for (IIntPair pair : neighborList.keySet()) {
            double dist = computeDistanceForParticlePair(pair);
            neighborList.put(pair, dist);
        }
    }

    private double computeDistanceForParticlePair(IIntPair pair) {
        IParticle p0 = particleConfiguration.getParticle(pair.get_i());
        IParticle p1 = particleConfiguration.getParticle(pair.get_j());
        double dist = DoubleArrays.distance(p0.get_coords(), p1.get_coords());
        return dist;
    }

    //##############################################################################
    // Neighbor List Iterators
    //##############################################################################
    /**
     * Returns all particle ids in neighboring boxes
     * @param id
     * @return
     */
    public IIntIterator getNeighbors(int id) {
        Box box = (Box) particleId2box_map.get(id);
        BoxIterator bi = new BoxIterator(grid, dim, mindex, box);
        bi.initialize(id);
        return (bi);
    }

    /**
     * Returns all particle ids in neighboring boxes
     * @param id
     * @return
     */
    public IIntIterator getNeighbors(double[] pos) {
        if(areCoordsWithinLatticeBoundaries(pos)){
        Box box = crd2box(pos);
        BoxIterator bi = new BoxIterator(grid, dim, mindex, box);
        bi.initialize();
        return (bi);
        }else{
            throw new RuntimeException("coords "+pos[0]+","+pos[1]+","+pos[2]+" not contained in lattice boundaries.");
        }
    }

    public ArrayList<IParticle> getNeighbors(IParticle p) {
        ArrayList<IParticle> list = new ArrayList();
        IIntIterator iter = getNeighbors(p.get_id());
        while (iter.hasNext()) {
            list.add(particleConfiguration.getParticle(iter.next()));
        }
        return list;


    }

    public Iterator<INeighborListEntry> getAllNeighborsPlusDist() {
        Iterator neighborListIter = new Iterator() {

            Iterator<IIntPair> pairIterator = neighborList.keySet().iterator();
            boolean hasNext;
            INeighborListEntry next;

            public boolean hasNext() {
                hasNext = this.pairIterator.hasNext();
                if (hasNext) {
                    next = precomputeNext();
                }
                return hasNext;
            }

            public Object next() {
                return next;
            }

            public void remove() {
                this.pairIterator.remove();
            }

            private INeighborListEntry precomputeNext() {
                IIntPair pair = this.pairIterator.next();
                double dist = neighborList.get(pair);
                return new NeighborListEntry(pair.get_i(), pair.get_j(), dist);
            }
        };
        return neighborListIter;
    }

    //##############################################################################
    // Persistend Neighbors management
    //##############################################################################
    public void setPersistentNeighbors(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePersistentNeighbors(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllPersistentNeighbors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //##############################################################################
    // Output
    //##############################################################################
    public void printLattice() {
        System.out.println("printing the lattice:");
        for (int i = 0; i < particleId2box_map.keys().size(); i++) {
            System.out.print("particleID " + i + " box index: ");
            Box box = (Box) particleId2box_map.get(particleId2box_map.keys().get(i));
            box.printIndex();
            System.out.print(" particlesInBox: ");
            for (int particleId : box.getList()) {
                System.out.print(particleId + " ");
            }
            System.out.println();

        }
    }
    
    public void printNeighborList() {
        System.out.println("printing the neighborlist:");
        for (IIntPair key: neighborList.keySet()) {
            System.out.println("pair: "+key.get_i()+","+key.get_j()+" dist: "+neighborList.get(key));
            
        }
    }
    
    
    //##############################################################################
    // Rarely used functions
    //##############################################################################

      public HashMap<IIntPair, Double> getNeighborList() {
        return this.neighborList;
    }

      
    

     ArrayList<Integer> possibleXIndices = new ArrayList();
    ArrayList<Integer> possibleYIndices = new ArrayList();
    ArrayList<Integer> possibleZIndices = new ArrayList();
    int[] indexDiff = new int[]{0, 0, 0};
    int[] excludedIndex = new int[]{0, 0, 0};

    HashSet<Integer> computeExcludedBoxIndices(int[] oldBoxIndex, int[] newBoxIndex) {
        HashSet<Integer> set = new HashSet();
        possibleXIndices.clear();
        possibleYIndices.clear();
        possibleZIndices.clear();
        indexDiff[0] = newBoxIndex[0] - oldBoxIndex[0];
        indexDiff[1] = newBoxIndex[1] - oldBoxIndex[1];
        indexDiff[2] = newBoxIndex[2] - oldBoxIndex[2];
        // if the dimension changed, take the new and the old value, if not, take
        // the index itself and the one left and right next to it.
        // do this for each dimension

        if (indexDiff[0] != 0) {
            possibleXIndices.add(oldBoxIndex[0]);
            possibleXIndices.add(newBoxIndex[0]);
        } else {
            possibleXIndices.add(oldBoxIndex[0] - 1);
            possibleXIndices.add(oldBoxIndex[0]);
            possibleXIndices.add(oldBoxIndex[0] + 1);
        }
        if (indexDiff[1] != 0) {
            possibleYIndices.add(oldBoxIndex[1]);
            possibleYIndices.add(newBoxIndex[1]);
        } else {
            possibleYIndices.add(oldBoxIndex[1] - 1);
            possibleYIndices.add(oldBoxIndex[1]);
            possibleYIndices.add(oldBoxIndex[1] + 1);
        }
        if (indexDiff[2] != 0) {
            possibleZIndices.add(oldBoxIndex[2]);
            possibleZIndices.add(newBoxIndex[2]);
        } else {
            possibleZIndices.add(oldBoxIndex[2] - 1);
            possibleZIndices.add(oldBoxIndex[2]);
            possibleZIndices.add(oldBoxIndex[2] + 1);
        }
        for (int x = 0; x < possibleXIndices.size(); x++) {
            for (int y = 0; y < possibleYIndices.size(); y++) {
                for (int z = 0; z < possibleZIndices.size(); z++) {
                    excludedIndex[0] = x;
                    excludedIndex[1] = y;
                    excludedIndex[2] = z;
                    if (mindex.isValid(excludedIndex)) {
                        set.add(mindex.index2id(excludedIndex));
                    } else {
                        AdvancedSystemOut.println("index tried to evaluate in the grid: ", excludedIndex, "");
                        throw new RuntimeException("index out of lattice bounds!");
                    }
                }
            }
        }
        return set;

    }

    private void recheckAllNeighborsOfParticleIdAndAddThemToNeighborList(int id) {

        // these neighbors are computed by the neighboring boxes
        IIntIterator boxNeighbors = getNeighbors(id);

        while (boxNeighbors.hasNext()) {
            int boxNeihghborPartnerId = boxNeighbors.next();
            IIntPair pairToReCheck = new IntPair(id, boxNeihghborPartnerId);
            // if the pair is already in the neighbor list we are done
            // the distance will be updated in the main for loop in the Top Class
            if (!neighborList.containsKey(pairToReCheck)) {
                IParticle p0 = particleConfiguration.getParticle(id);
                IParticle p1 = particleConfiguration.getParticle(boxNeihghborPartnerId);
                double dist = DoubleArrays.distance(p0.get_coords(), p1.get_coords());
                if (DISTANCE_CRITERION_TO_DECREASE_NEIGHBORLIST_SIZE_ENABLED) {
                    if (distanceCriterionToBeNeighborValid(p0, p1, dist)) {
                        neighborList.put(pairToReCheck, dist);

                    } else {
                    }
                } else {
                    neighborList.put(pairToReCheck, dist);
                }
            }
        }
    }

    private boolean areCoordsWithinLatticeBoundaries(double[] pos) {
        return      pos[0]>=O[0] && pos[0]<=O[0]+Ext[0]
                && pos[1]>=O[1] && pos[1]<=O[1]+Ext[1]
                && pos[2]>=O[2] && pos[2]<=O[2]+Ext[2];
    }

    
}