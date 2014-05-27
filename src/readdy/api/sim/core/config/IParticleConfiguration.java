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
package readdy.api.sim.core.config;

import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.space.INeighborSearch;
import java.util.Iterator;
import readdy.api.assembly.IParticleFactory;
import readdy.api.sim.core.space.INeighborListEntry;
import statlab.base.datatypes.IIntIterator;

/**
 * Administrates the complete list of particle coordinates.
 * Only this class can change particle positions and generate new particles
 * @author schoeneberg
 */
public interface IParticleConfiguration {

    /**
     * sets the neighbor searching Engine
     * @param nSearch
     */
    public void setNeighborSearch(INeighborSearch nSearch);

    public void setParticleFactory(IParticleFactory particleFactory);

    /**
     * Returns the current number of particles
     * @return
     */
    public int getNParticles();

    public int getLargestParticleId();

    /**
     * Attempts to create a new particle at given position
     * @param type particle type
     * @param position particle position
     * @return an object referring to the created particle or null if unsuccessful
     */
    public IParticle getParticle(int id);

    public IParticle createParticle(int type, double[] crd);

    /**
     * Removes a particle altogether
     * @param p a reference to the particle to be destroyed
     * @return true if successful, false otherwise
     */
    public void removeParticle(int particleId);

    /**
     * sets the coordinates of particle p
     * @param p
     * @return false if unsuccessful
     */
    public boolean setCoordinates(IParticle p, double[] crd);

    public boolean setCoordinates(int pId, double[] crd);

    /**
     * Returns an iterator over all particles
     * @return
     */
    public Iterator<IParticle> particleIterator();

    /**
     * Returns an iterator over all particle pairs
     * @return
     */
    public Iterator<INeighborListEntry> particlePairIterator();

    // neighbors
    public IIntIterator getNeighboringParticleIds(double[] coords);

    public int[] getLatticeBoxIndex(double[] coords);

    // could maybe moved to an other location
    public void recomputeLattice();

    public void updateNeighborListDistances();

    public void changeParticleType(int particleId, int from, int to);

    
    public void setSystemPotentialEnergy(double E0);

    public double getSystemPotentialEnergy();
    
}
