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
package readdy.impl.sim.top.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import readdy.api.assembly.IGroupFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupAllAccess;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupParameters;

/**
 *
 * @author schoeneberg
 */
public class GroupConfiguration implements IGroupConfiguration {

    private HashMap<Integer, IGroupAllAccess> groupIdToGroupMap = new HashMap();
    // partile id -> groupId[]
    private HashMap<Integer, HashSet<Integer>> particleGroupMembership = new HashMap();
    private IGroupFactory groupFactory;
    private IGroupDisassembler groupDisassembler;
    private IGroupParameters groupParameters;

    public IGroup getGroup(int groupId) {
        if (groupIdToGroupMap.containsKey(groupId)) {
            return groupIdToGroupMap.get(groupId);
        } else {
            throw new RuntimeException("the requested group id " + groupId + " doesnt exist. Abort!");
        }
    }

    public IGroup createGroup(int groupTypeToCreate, ArrayList<IParticle> positionedParticleIds) {
        IGroupAllAccess newGroup = groupFactory.createGroup(groupTypeToCreate, positionedParticleIds);
        groupIdToGroupMap.put(newGroup.get_id(), newGroup);
        addToParticleMembership(newGroup);
        return newGroup;
    }

    public void removeGroup(int groupId) {
        if (groupIdToGroupMap.containsKey(groupId)) {
            IGroup group = groupIdToGroupMap.get(groupId);
            groupDisassembler.removeGroup(group);
            removeFromParticleMembership(group);
            groupIdToGroupMap.remove(groupId);
        } else {
            throw new RuntimeException("the group with id " + groupId + " doesnt exist and can not be removed.");
        }
    }

    public void changeGroupType(int groupId, int from, int to) {
        if (groupIdToGroupMap.containsKey(groupId)) {
            if (groupParameters.getNumberOfGroupMembers(from) == groupParameters.getNumberOfGroupMembers(to)) {
                IGroupAllAccess groupToChangeType = groupIdToGroupMap.get(groupId);
                if (groupToChangeType.get_typeId() == from) {

                    System.out.println("typeId before: " + groupToChangeType.get_typeId());
                    groupToChangeType.set_typeId(to);
                    System.out.println("typeId after: " + groupToChangeType.get_typeId());
                    System.out.println("typeId after 2: " + groupIdToGroupMap.get(groupId).get_typeId());

                } else {
                    throw new RuntimeException(" the requested group had not the right original type " + from + " for a type change to " + to);
                }
            } else {
                throw new RuntimeException(" type change not possible: the number of group member particles is not the same.");
            }
        } else {
            throw new RuntimeException("the requested group id " + groupId + " doesnt exist. Abort type change!");
        }
    }

    public int getCurrNAssignmentsOfGroupType(int involvedParticleId, int groupTypeId) {
        return getGroupsWhereParticleIsInvolved(involvedParticleId, groupTypeId).size();
    }

    public ArrayList<IExtendedIdAndType> getGroupsWhereParticleIsInvolved(int particleId, int groupTypeIdToSearch) {
        ArrayList<IExtendedIdAndType> result = new ArrayList();
        if (particleGroupMembership.containsKey(particleId)) {
            for (int groupId : particleGroupMembership.get(particleId)) {
                IGroup g = this.getGroup(groupId);
                if (g.get_typeId() == groupTypeIdToSearch) {
                    result.add(new ExtendedIdAndType(g));
                }
            }
        }
        return result;
    }

    public ArrayList<IExtendedIdAndType> getAllGroupsWhereParticleIsInvolvedIn(int particleId) {
        ArrayList<IExtendedIdAndType> result = new ArrayList();
        if (particleGroupMembership.containsKey(particleId)) {
            for (int groupId : particleGroupMembership.get(particleId)) {
                IGroup g = this.getGroup(groupId);
                result.add(new ExtendedIdAndType(g));
            }
        }
        return result;
    }

    private void addToParticleMembership(IGroup newGroup) {
        for (IParticle p : newGroup.get_positionedMemberParticles()) {
            int particleId = p.get_id();
            if (particleGroupMembership.containsKey(particleId)) {
                if (particleGroupMembership.get(particleId).contains(newGroup.get_id())) {
                    throw new RuntimeException("particle " + particleId + " is already assigned to group " + newGroup.get_id());
                } else {
                    particleGroupMembership.get(particleId).add(newGroup.get_id());
                }
            } else {
                HashSet<Integer> groupIdSet = new HashSet();
                groupIdSet.add(newGroup.get_id());
                particleGroupMembership.put(particleId, groupIdSet);
            }
        }
    }

    private void removeFromParticleMembership(IGroup group) {
        for (IParticle p : group.get_positionedMemberParticles()) {
            int particleId = p.get_id();
            if (particleGroupMembership.containsKey(particleId)) {
                HashSet<Integer> groupIds = particleGroupMembership.get(particleId);
                if (groupIds.contains(group.get_id())) {

                    groupIds.remove(group.get_id());
                } else {
                    throw new RuntimeException("before group removal the particle should know that it belongs to the group. "
                            + "\n This was not the case -> Error somewhere. \n Maybe GroupRemoval while Group was not existent. 2");
                }
            } else {
                throw new RuntimeException("before group removal the particle should know that it belongs to the group. "
                        + "\n This was not the case -> Error somewhere. \n Maybe GroupRemoval while Group was not existent. 1");
            }
        }
    }

    public void setGroupFactory(IGroupFactory groupFactory) {
        this.groupFactory = groupFactory;
    }

    public void setGroupDisassembler(IGroupDisassembler groupDisassembler) {
        this.groupDisassembler = groupDisassembler;
    }

    public Iterator<IGroup> groupIterator() {
        Iterator<IGroup> iter = generateNewIterator();
        return iter;
    }

    private Iterator<IGroup> generateNewIterator() {
        Iterator<IGroup> action = new Iterator() {

            Iterator<IGroupAllAccess> subIterator = groupIdToGroupMap.values().iterator();

            public boolean hasNext() {
                return subIterator.hasNext();
            }

            public Object next() {
                IGroup next = subIterator.next();
                return next;
            }

            public void remove() {
                subIterator.remove();
            }
        };
        return action;
    }

    public int getNGroups() {
        return groupIdToGroupMap.size();
    }

    public void setGroupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }
}
