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
package readdy.api.sim.core.space;

import java.util.*;
import readdy.api.sim.core.particle.IParticle;
import statlab.base.datatypes.*;

/**
 *
 * @author noe
 */
public interface INeighborSearch {

    /**
     * Makes i and j to persistent neighbors until this is explicitly removed - irrespective of their distance
     * @param i
     * @param j
     */
    public void setPersistentNeighbors(int i, int j);

    /**
     * i and j are no longer persistent neighbors
     * @param i
     * @param j
     */
    public void removePersistentNeighbors(int i, int j);

    /**
     * Removes all persistent neighborships
     */
    public void removeAllPersistentNeighbors();

    /**
     * Updates the position of particle i
     * @param i
     * @param pos
     */
    public void updatePosition(IParticle p);

    /**
     * Call to notify the addition of particle p
     * @param p
     */
    public void updateAdd(IParticle p);

    /**
     * Call to notify the removal of particle p
     * @param p
     */
    public void updateRemove(IParticle p);

    /**
     * Return all neighbors of the specified particle
     * @param id
     * @return
     */
    public IIntIterator getNeighbors(int id);

    /**
     * Return all neighbors of the coordinate
     * @param id
     * @return
     */
    public IIntIterator getNeighbors(double[] coords);

    /**
     * Gets all neighbors that are within maximum interaction distances or are persistent neighbors.
     * Returns additionally the distance between the neighbors
     * @return
     */
    public Iterator<INeighborListEntry> getAllNeighborsPlusDist();

    /**
     * returns the lattice index of the given coordinates.
     * @param coords
     * @return
     */
    public int[] getLatticeBoxIndex(double[] coords);

    /**
     * Updates all particle positions according to the particle configuration
     * and recomputes the distances
     */
    public void recomputeLattice();

    /*
     * recomputest only the distances of the neighbourList
     */
    public void updateDistances();

    public void printLattice();
}
