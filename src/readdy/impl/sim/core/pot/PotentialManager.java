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
import java.util.HashSet;
import java.util.Iterator;
import readdy.api.dtypes.IIntPair;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.impl.dtypes.IntPair;

/**
 *
 * The Potential Manager knows, which particles are assigned to which 
 * potential. The information about the potentials themselves and the potential
 * Objects reside in the potentialInventory
 * 
 * 
 * there are two types of potentials. Order 1 and order 2.
 * Order 1 potentials operate on only one particle coordinate.
 * Order 2 potentials operate on a set of two particle coordinates.
 * 
 * a potential of either kind can be assigned to a particle either via
 * the particle type or via the particle ID.
 * 
 * Assignment via particle type means, that all particles of that specific type
 * are governed by that potential. There are no exeptions possible and an 
 * individual particle can not be treated differently if it has a certain type
 * to which a potential is assigned. 
 * It is possible to add or remove potentials from a particle type however.
 * 
 * Assignment via particle ID means, that only that specific particle that has
 * that specific ID is governed by that potential. The potential assignment 
 * to that id can be created or broken. 
 * 
 * If one has, e.g. 1000 particle of type 1 that are governed by a disk membrane
 * potential, but wants to have one of these particles to be not bound to that
 * disk. This is not possible via potentials. 
 * Usually, in ReaDDy this is modeled by that particlular particle changing
 * its type by conserving all other particle parameters. Now it can be treated
 * differently.
 *
 * @author schoeneberg
 */
public class PotentialManager implements IPotentialManager {

    private IPotentialInventory potentialInventory;
    private HashMap<Integer, HashSet<Integer>> pTypeId_to_pot1Id_map = new HashMap();
    // particle id 2 potential1 ID map
    private HashMap<Integer, HashSet<Integer>> pId_to_pot1Id_map = new HashMap();
    private HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_pot2Id_map = new HashMap();
    private HashMap<IIntPair, HashSet<Integer>> pIdPair_to_pot2Id_map = new HashMap();

    public void addPotentialByType(int particleTypeId, int potId) {

        // does the requested potentialId exist?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already assigned a potential to it?
            if (pTypeId_to_pot1Id_map.containsKey(particleTypeId)) {
                // already a potential presrent
                HashSet<Integer> setAlready = pTypeId_to_pot1Id_map.get(particleTypeId);
                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }
            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                pTypeId_to_pot1Id_map.put(particleTypeId, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }


    }

    public void addPotentialByID(int particleId, int potId) {
        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pId_to_pot1Id_map.containsKey(particleId)) {
                // already a potential present
                HashSet<Integer> setAlready = pId_to_pot1Id_map.get(particleId);
                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }
            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                pId_to_pot1Id_map.put(particleId, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }

    }

    public void addPotentialByType(int particleTypeId1, int particleTypeId2, int potId) {
        IIntPair typeIdPair = new IntPair(particleTypeId1, particleTypeId2);

        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pTypeIdPair_to_pot2Id_map.containsKey(typeIdPair)) {
                // already a potential present
                HashSet<Integer> setAlready = pTypeIdPair_to_pot2Id_map.get(typeIdPair);
                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }
            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                pTypeIdPair_to_pot2Id_map.put(typeIdPair, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }
    }

    public void addPotentialByID(int particleId1, int particleId2, int potId) {
        IIntPair idPair = new IntPair(particleId1, particleId2);
        //System.out.println("PotentialManager.addPotentialById " + particleId1 + "," + particleId2 + "," + potId + " potName " + potentialInventory.getPotential(potId).get_name());

        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pIdPair_to_pot2Id_map.containsKey(idPair)) {
                // already a potential present
                HashSet<Integer> setAlready = pIdPair_to_pot2Id_map.get(idPair);

                if (!setAlready.contains(potId)) {
                    setAlready.add(potId);
                }

            } else {
                // not yet present
                HashSet<Integer> set = new HashSet();
                set.add(potId);
                pIdPair_to_pot2Id_map.put(idPair, set);

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }
    }

    public boolean removePotentialByType(int particleTypeId, int potId) {
        // is the requested potentialId existent?
        boolean success = true;
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pTypeId_to_pot1Id_map.containsKey(particleTypeId)) {
                // already a potential present
                HashSet<Integer> setAlready = pTypeId_to_pot1Id_map.get(particleTypeId);
                if (setAlready.contains(potId)) {
                    success = setAlready.remove(potId);
                }

            } else {
                throw new RuntimeException(" the requested potentialId to be removed is not known. Abort.");
            }

        }
        return success;
    }

    public boolean removePotentialByID(int particleId, int potId) {
        // is the requested potentialId existent?
        boolean success = true;
        if (potentialInventory.doesPotentialExist(potId)) {

            if (pId_to_pot1Id_map.containsKey(particleId)) {

                if (pId_to_pot1Id_map.get(particleId).contains(potId)) {
                    // already a potential present
                    HashSet<Integer> setAlready = pId_to_pot1Id_map.get(particleId);
                    success = setAlready.remove(potId);
                    pId_to_pot1Id_map.put(particleId, setAlready);
                }
            }


        } else {
            throw new RuntimeException(" the requested potentialId to be removed is not known. Abort.");
        }
        return success;
    }

    public boolean removePotentialByType(int particleTypeId1, int particleTypeId2, int potId) {
        IIntPair typeIdPair = new IntPair(particleTypeId1, particleTypeId2);
        boolean success = true;
        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pTypeIdPair_to_pot2Id_map.containsKey(typeIdPair)) {
                // already a potential present
                HashSet<Integer> setAlready = pTypeIdPair_to_pot2Id_map.get(typeIdPair);
                if (setAlready.contains(potId)) {
                    success = setAlready.remove(potId);
                }
            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }
        return success;
    }

    public boolean removePotentialByID(int particleId1, int particleId2, int potId) {
        IIntPair idPair = new IntPair(particleId1, particleId2);
        boolean success = true;
        // is the requested potentialId existent?
        if (potentialInventory.doesPotentialExist(potId)) {
            // has the particleType already a potential assigned?
            if (pIdPair_to_pot2Id_map.containsKey(idPair)) {
                // already a potential present
                HashSet<Integer> setAlready = pIdPair_to_pot2Id_map.get(idPair);
                if (setAlready.contains(potId)) {
                    success = setAlready.remove(potId);
                }

            }
        } else {
            throw new RuntimeException(" the requested potentialId to add is not known. Abort.");
        }
        return success;
    }

    public Iterator<IPotential1> getPotentials_slow(IParticle p) {
        ArrayList<IPotential1> queryResult = new ArrayList();
        HashSet<Integer> requestedPotIds = new HashSet();
        if (pTypeId_to_pot1Id_map.containsKey(p.get_type())) {
            requestedPotIds.addAll(pTypeId_to_pot1Id_map.get(p.get_type()));
        }
        if (pId_to_pot1Id_map.containsKey(p.get_id())) {
            requestedPotIds.addAll(pId_to_pot1Id_map.get(p.get_id()));
        }

        for (int potId : requestedPotIds) {
            if (potentialInventory.doesPotentialExist(potId)) {

                if (potentialInventory.getPotentialOrder(potId) == 1) {
                    queryResult.add((IPotential1) potentialInventory.getPotential(potId));
                } else {
                    throw new RuntimeException("the returned potential is from the wrong order! abort!");
                }
            }
        }
        return queryResult.iterator();
    }

    public Iterator<IPotential1> getPotentials(final IParticle p) {

        Iterator<IPotential1> potIterator = new Iterator() {

            int pTypeId = p.get_type();
            int pId = p.get_id();
            boolean typeGrabable = pTypeId_to_pot1Id_map.containsKey(pTypeId);
            Iterator<Integer> potByTypeIterator;
            boolean idGrabable = pId_to_pot1Id_map.containsKey(pId);
            Iterator<Integer> potByIdIterator;
            boolean hasNext;
            boolean settedUp = false;
            IPotential1 next;
            

            private void setup() {
                if (typeGrabable) {
                    potByTypeIterator = pTypeId_to_pot1Id_map.get(pTypeId).iterator();
                }
                if (idGrabable) {
                    potByIdIterator = pId_to_pot1Id_map.get(pId).iterator();
                }
                settedUp = true;
            }

            public boolean hasNext() {
                if (!settedUp) {
                    setup();
                }
                if (typeGrabable) {
                    hasNext = potByTypeIterator.hasNext();
                    // switch to the other list if the first one is empty
                    if (!hasNext) {
                        typeGrabable = false;
                    }
                } else {
                    if (idGrabable) {
                        hasNext = potByIdIterator.hasNext();
                        // switch to the other list if the first one is empty
                        if (!hasNext) {
                            idGrabable = false;
                        }

                    } else {
                        hasNext = false;
                    }
                }
                if (hasNext) {
                    next = precomputeNext();
                }
                return hasNext;

            }

            public Object next() {
                if (!settedUp) {
                    setup();
                }
                return next;

            }

            private IPotential1 precomputeNext() {
                int potId;
                if (typeGrabable) {
                    potId = potByTypeIterator.next();
                    // switch to the other list if the first one is empty
                    if (!potByTypeIterator.hasNext()) {
                        typeGrabable = false;
                    }
                } else {
                    if (idGrabable) {
                        potId = potByIdIterator.next();
                        // switch to the other list if the first one is empty
                        if (!potByIdIterator.hasNext()) {
                            idGrabable = false;
                        }

                    } else {
                        throw new RuntimeException("this should never be reached.");
                    }
                }

                if (potentialInventory.getPotentialOrder(potId) == 1) {
                    return (IPotential1) potentialInventory.getPotential(potId);
                } else {
                    throw new RuntimeException("this is weird.");
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return potIterator;
    }

    public Iterator<IPotential1> getPotentials(final int pTypeId) {

        Iterator<IPotential1> potIterator = new Iterator() {

            boolean typeGrabable = pTypeId_to_pot1Id_map.containsKey(pTypeId);
            Iterator<Integer> potByTypeIterator;
            boolean hasNext;
            boolean settedUp = false;
            IPotential1 next;

            private void setup() {
                if (pTypeId_to_pot1Id_map.containsKey(pTypeId)) {
                    potByTypeIterator = pTypeId_to_pot1Id_map.get(pTypeId).iterator();
                } else {
                    ArrayList<Integer> emptyList = new ArrayList<Integer>();
                    potByTypeIterator = emptyList.iterator();
                }
                settedUp = true;
            }

            public boolean hasNext() {
                if (!settedUp) {
                    setup();
                }

                hasNext = potByTypeIterator.hasNext();
                // switch to the other list if the first one is empty
                if (!hasNext) {
                    typeGrabable = false;
                }

                if (hasNext) {
                    next = precomputeNext();
                }
                return hasNext;

            }

            public Object next() {
                if (!settedUp) {
                    setup();
                }
                return next;

            }

            private IPotential1 precomputeNext() {
                int potId;

                potId = potByTypeIterator.next();
                // switch to the other list if the first one is empty
                if (!potByTypeIterator.hasNext()) {
                    typeGrabable = false;
                }


                if (potentialInventory.getPotentialOrder(potId) == 1) {
                    return (IPotential1) potentialInventory.getPotential(potId);
                } else {
                    throw new RuntimeException("this is weird.");
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return potIterator;

    }

    public Iterator<IPotential2> getPotentials_slow(IParticle p1, IParticle p2) {
        if (p1.get_id() != p2.get_id()) {
            ArrayList<IPotential2> queryResult = new ArrayList();
            HashSet<Integer> requestedPotIds = new HashSet();
            // not shure if it is more efficient to go over the referenced particle functions
            // or to create those 4 helper elements.
            IIntPair idPair = new IntPair(p1.get_id(), p2.get_id());
            IIntPair typeIdPair = new IntPair(p1.get_type(), p2.get_type());


            if (pTypeIdPair_to_pot2Id_map.containsKey(typeIdPair)) {
                requestedPotIds.addAll(pTypeIdPair_to_pot2Id_map.get(typeIdPair));
            }
            if (pIdPair_to_pot2Id_map.containsKey(idPair)) {
                requestedPotIds.addAll(pIdPair_to_pot2Id_map.get(idPair));
            }
            for (int potId : requestedPotIds) {
                if (potentialInventory.doesPotentialExist(potId)) {

                    if (potentialInventory.getPotentialOrder(potId) == 2) {
                        queryResult.add((IPotential2) potentialInventory.getPotential(potId));
                    } else {
                        throw new RuntimeException("the returned potential is from the wrong order! abort!");
                    }
                }
            }
            return queryResult.iterator();
        } else {
            throw new RuntimeException("treatment of identical particles forbidden.");
        }
    }

    public Iterator<IPotential2> getPotentials(final IParticle p1, final IParticle p2) {
        if (p1.get_id() != p2.get_id()) {

            Iterator<IPotential2> pot2Iterator = new Iterator() {

                IIntPair typeIdPair = new IntPair(p1.get_type(), p2.get_type());
                IIntPair idPair = new IntPair(p1.get_id(), p2.get_id());
                boolean typeGrabable = pTypeIdPair_to_pot2Id_map.containsKey(typeIdPair);
                Iterator<Integer> pot2ByTypeIterator;
                boolean idGrabable = pIdPair_to_pot2Id_map.containsKey(idPair);
                Iterator<Integer> pot2ByIdIterator;
                boolean hasNext;
                boolean settedUp = false;
                IPotential2 next;

                private void setup() {

                    if (typeGrabable) {
                        pot2ByTypeIterator = pTypeIdPair_to_pot2Id_map.get(typeIdPair).iterator();
                    }
                    if (idGrabable) {
                        pot2ByIdIterator = pIdPair_to_pot2Id_map.get(idPair).iterator();
                    }
                    settedUp = true;
                }

                public boolean hasNext() {
                    if (!settedUp) {
                        setup();
                    }
                    if (typeGrabable) {
                        hasNext = pot2ByTypeIterator.hasNext();
                        // switch to the other list if the first one is empty
                        if (!hasNext) {
                            typeGrabable = false;
                        }
                    } else {
                        if (idGrabable) {
                            hasNext = pot2ByIdIterator.hasNext();
                            // switch to the other list if the first one is empty
                            if (!hasNext) {
                                idGrabable = false;
                            }

                        } else {
                            hasNext = false;
                        }
                    }
                    if (hasNext) {
                        next = precomputeNext();
                    }
                    return hasNext;

                }

                public Object next() {
                    if (!settedUp) {
                        setup();
                    }
                    return next;

                }

                private IPotential2 precomputeNext() {
                    int potId;
                    if (typeGrabable) {
                        potId = pot2ByTypeIterator.next();
                        // switch to the other list if the first one is empty
                        if (!pot2ByTypeIterator.hasNext()) {
                            typeGrabable = false;
                        }
                    } else {
                        if (idGrabable) {
                            potId = pot2ByIdIterator.next();
                            // switch to the other list if the first one is empty
                            if (!pot2ByIdIterator.hasNext()) {
                                idGrabable = false;
                            }

                        } else {
                            throw new RuntimeException("this should never be reached.");
                        }
                    }

                    if (potentialInventory.getPotentialOrder(potId) == 2) {
                        return (IPotential2) potentialInventory.getPotential(potId);
                    } else {
                        throw new RuntimeException("this is weird.");
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            return pot2Iterator;
        } else {
            throw new RuntimeException("treatment of identical particles forbidden.");
        }
    }

    public Iterator<IPotential2> getPotentials(final int pTypeId1, final int pTypeId2) {


        Iterator<IPotential2> pot2Iterator = new Iterator() {

            IIntPair typeIdPair = new IntPair(pTypeId1, pTypeId2);
            boolean typeGrabable = pTypeIdPair_to_pot2Id_map.containsKey(typeIdPair);
            Iterator<Integer> pot2ByTypeIterator;
            boolean idGrabable = false;
            Iterator<Integer> pot2ByIdIterator;
            boolean hasNext;
            boolean settedUp = false;
            IPotential2 next;

            private void setup() {

                if (typeGrabable) {
                    pot2ByTypeIterator = pTypeIdPair_to_pot2Id_map.get(typeIdPair).iterator();
                }
                settedUp = true;
            }

            public boolean hasNext() {
                if (!settedUp) {
                    setup();
                }
                if (typeGrabable) {
                    hasNext = pot2ByTypeIterator.hasNext();
                    // switch to the other list if the first one is empty
                    if (!hasNext) {
                        typeGrabable = false;
                    }
                } else {
                    if (idGrabable) {
                        hasNext = pot2ByIdIterator.hasNext();
                        // switch to the other list if the first one is empty
                        if (!hasNext) {
                            idGrabable = false;
                        }

                    } else {
                        hasNext = false;
                    }
                }
                if (hasNext) {
                    next = precomputeNext();
                }
                return hasNext;

            }

            public Object next() {
                if (!settedUp) {
                    setup();
                }
                return next;

            }

            private IPotential2 precomputeNext() {
                int potId;
                if (typeGrabable) {
                    potId = pot2ByTypeIterator.next();
                    // switch to the other list if the first one is empty
                    if (!pot2ByTypeIterator.hasNext()) {
                        typeGrabable = false;
                    }
                } else {
                    if (idGrabable) {
                        potId = pot2ByIdIterator.next();
                        // switch to the other list if the first one is empty
                        if (!pot2ByIdIterator.hasNext()) {
                            idGrabable = false;
                        }

                    } else {
                        throw new RuntimeException("this should never be reached.");
                    }
                }

                if (potentialInventory.getPotentialOrder(potId) == 2) {
                    return (IPotential2) potentialInventory.getPotential(potId);
                } else {
                    throw new RuntimeException("this is weird.");
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return pot2Iterator;
    }

    public void set_pTypeId_to_pot1Id_map(HashMap<Integer, HashSet<Integer>> pTypeId_to_pot1Id_map) {
        this.pTypeId_to_pot1Id_map = pTypeId_to_pot1Id_map;
    }

    public void set_pId_to_pot1Id_map(HashMap<Integer, HashSet<Integer>> pId_to_pot1Id_map) {
        this.pId_to_pot1Id_map = pId_to_pot1Id_map;
    }

    public void set_pTypeIdPair_to_pot2Id_map(HashMap<IIntPair, HashSet<Integer>> pTypeIdPair_to_pot2Id_map) {
        this.pTypeIdPair_to_pot2Id_map = pTypeIdPair_to_pot2Id_map;
    }

    public void set_pIdPair_to_pot2Id_map(HashMap<IIntPair, HashSet<Integer>> pIdPair_to_pot2Id_map) {
        this.pIdPair_to_pot2Id_map = pIdPair_to_pot2Id_map;
    }

    public void set_potentialInventory(IPotentialInventory potentialInventory) {
        this.potentialInventory = potentialInventory;
    }
}
