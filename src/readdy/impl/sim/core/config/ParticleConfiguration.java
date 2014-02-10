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

import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileDataEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import readdy.api.sim.core.space.INeighborSearch;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleAllAccess;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.assembly.IParticleFactory;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.space.INeighborListEntry;
import statlab.base.datatypes.IIntIterator;

/**
 *
 * @author schoeneberg
 */
public class ParticleConfiguration implements IParticleConfiguration {

    private IParticleFactory particleFactory = null;
    private INeighborSearch neighborSearch;
    private HashMap<Integer, IParticleAllAccess> particleIdToParticleMap = new HashMap();
    private IParticleParameters particleParameters;
    private double currentPotentialEnergy = 0;

    public ParticleConfiguration() {
    }

    // -----------------------------------------------------------------
    // input
    // -----------------------------------------------------------------
    public void setParticleFactory(IParticleFactory factory) {
        this.particleFactory = factory;
    }

    public void setNeighborSearch(INeighborSearch nSearch) {
        this.neighborSearch = nSearch;
    }
    // -----------------------------------------------------------------
    // setup
    // -----------------------------------------------------------------

    public void setupInitialParticleConfiguration(ArrayList<ITplgyCoordinatesFileDataEntry> inputCoordsList) {
        for (ITplgyCoordinatesFileDataEntry pData : inputCoordsList) {
            IParticleAllAccess newParticle = particleFactory.createParticle(pData);
            if (particleIdToParticleMap.containsKey(newParticle.get_id())) {
                throw new RuntimeException("particle with the same ID already exists!");
            } else {
                particleIdToParticleMap.put(newParticle.get_id(), newParticle);
            }
        }

    }

    // -----------------------------------------------------------------
    // particle manipulation functions
    // -----------------------------------------------------------------
    public IParticle createParticle(int type, double[] position) {

        IParticleAllAccess newParticle = particleFactory.createParticle(type, position);
        particleIdToParticleMap.put(newParticle.get_id(), newParticle);
        neighborSearch.updateAdd(newParticle);
        return newParticle;

    }

    public boolean removeParticle(IParticle pToRemove) {
        neighborSearch.updateRemove(pToRemove);

        boolean result = (particleIdToParticleMap.remove(pToRemove.get_id()) != null);
        if (result == false) {
            throw new RuntimeException("particle remove caused problems");
        }
        return result;
    }

    public void removeParticle(int particleId) {
        if (particleIdToParticleMap.containsKey(particleId)) {
            removeParticle(particleIdToParticleMap.get(particleId));
        } else {
            throw new RuntimeException("particle remove is not possible, particle id " + particleId + " doesnt exist.");
        }
    }

    public void changeParticleType(int particleId, int from, int to) {
        if (particleIdToParticleMap.containsKey(particleId)
                && particleIdToParticleMap.get(particleId).get_type() == from) {
            particleIdToParticleMap.get(particleId).set_typeId(to);
        } else {
            throw new RuntimeException("particleType change not possible because "
                    + "of missing particle or wrong type id before.");
        }

    }

    public boolean setCoordinates(IParticle pToSet, double[] crd) {

        IParticleAllAccess paa = null;
        paa = (IParticleAllAccess) pToSet;
        paa.set_coords(crd);
        neighborSearch.updatePosition(pToSet);
        return true;
    }

    public boolean setCoordinates(int pId, double[] crd) {


        IParticleAllAccess paa = (IParticleAllAccess) particleIdToParticleMap.get(pId);
        paa.set_coords(crd);
        neighborSearch.updatePosition(paa);
        return true;
    }

    // -----------------------------------------------------------------
    // neighbor list related
    // -----------------------------------------------------------------
    public void recomputeLattice() {
        neighborSearch.recomputeLattice();
    }

    public void updateNeighborListDistances() {
        neighborSearch.updateDistances();
    }

    // -----------------------------------------------------------------
    // particle Iterators
    // -----------------------------------------------------------------
    public Iterator<IParticle> particleIterator() {
        // if i dont do it via this construct, the iterator is not refreshed again 
        // after it has ran over the list
        Iterator<IParticle> iter = generateNewIterator();
        return iter;
    }

    public Iterator<INeighborListEntry> particlePairIterator() {
        return neighborSearch.getAllNeighborsPlusDist();
    }

    private Iterator<IParticle> generateNewIterator() {
        Iterator<IParticle> action = new Iterator() {

            Iterator<Integer> subIterator = particleIdToParticleMap.keySet().iterator();
            IParticle next;
            boolean hasNext;

            public boolean hasNext() {
                hasNext = subIterator.hasNext();
                if (hasNext) {
                    next = prepareNext();
                }
                return hasNext;
            }

            public Object next() {
                return next;
            }

            public void remove() {
                subIterator.remove();
            }

            private IParticle prepareNext() {
                return particleIdToParticleMap.get(subIterator.next());
            }
        };
        return action;
    }

    // -----------------------------------------------------------------
    // particle static information
    // -----------------------------------------------------------------
    public IParticle getParticle(int id) {
        if (particleIdToParticleMap.containsKey(id)) {
            return particleIdToParticleMap.get(id);
        } else {
            throw new RuntimeException("requested particle ID '" + id + "'is unknown");
        }

    }

    // -----------------------------------------------------------------
    // system information
    // -----------------------------------------------------------------
    public int getNParticles() {
        return particleIdToParticleMap.values().size();
    }

    public int getLargestParticleId() {
        return (particleFactory.get_highestParticleIdSoFar());

    }

    public IIntIterator getNeighboringParticleIds(double[] coords) {
        return neighborSearch.getNeighbors(coords);
    }

    public int[] getLatticeBoxIndex(double[] coords) {
        return neighborSearch.getLatticeBoxIndex(coords);
    }

    public void set_ParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void setSystemPotentialEnergy(double Epot) {
        this.currentPotentialEnergy = Epot;
    }

    public double getSystemPotentialEnergy() {
        return currentPotentialEnergy;
    }
}
