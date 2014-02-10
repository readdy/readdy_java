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
package readdy.impl.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileDataEntry;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.impl.sim.top.group.GroupConfiguration;

/**
 *
 * @author schoeneberg
 */
public class GroupConfigurationFactory implements IGroupConfigurationFactory {

    private ITplgyGroupsFileData tplgyGroupsFileData;
    private IGroupFactory groupFactory;
    private IGroupDisassembler groupDisassembler;
    private IGroupParameters groupParameters;
    private IParticleConfiguration particleConfig;

    public void set_particleConfiguration(IParticleConfiguration particleConfig) {
        this.particleConfig = particleConfig;
    }

    public void set_groupFactory(IGroupFactory groupFactory) {
        this.groupFactory = groupFactory;
    }

    public void set_tplgyGroupsFileData(ITplgyGroupsFileData tplgyGroupsFileData) {
        this.tplgyGroupsFileData = tplgyGroupsFileData;
    }

    public void set_groupDisassembler(IGroupDisassembler groupDisassembler) {
        this.groupDisassembler = groupDisassembler;
    }

    public void set_groupParameters(IGroupParameters groupParameters) {
        this.groupParameters = groupParameters;
    }

    public IGroupConfiguration createGroupConfiguration() {
        if (settedUpProperly()) {
            GroupConfiguration groupConfig = new GroupConfiguration();

            
            groupConfig.setGroupParameters(groupParameters);
            groupConfig.setGroupFactory(groupFactory);
            groupConfig.setGroupDisassembler(groupDisassembler);


            // create initial group configuration
            for (ITplgyGroupsFileDataEntry group : tplgyGroupsFileData.get_GroupsFileDataList()) {
                int groupTypeToCreate = group.get_typeId();
                ArrayList<Integer> positionedParticleIds = positionParticleIds(group);
                ArrayList<IParticle> positionedParticles = new ArrayList();
                for (int pId : positionedParticleIds) {
                    positionedParticles.add(particleConfig.getParticle(pId));
                }
                groupConfig.createGroup(groupTypeToCreate, positionedParticles);
            }


            return groupConfig;
        } else {
            throw new RuntimeException("ParticleConfigurationFactory not setted up properly");
        }
    }

    private boolean settedUpProperly() {
        return groupFactory != null
                && tplgyGroupsFileData != null
                && groupDisassembler != null
                && groupParameters != null
                && particleConfig != null;

    }

    private ArrayList<Integer> positionParticleIds(ITplgyGroupsFileDataEntry group) {
        ArrayList<Integer> positionedParticles = new ArrayList();
        HashMap<Integer, Integer> internalId_to_particleId_map = group.get_internalAndParticleId();
        for (int internalId : internalId_to_particleId_map.keySet()) {
            int particleId = internalId_to_particleId_map.get(internalId);
            positionedParticles.add(particleId);
        }
        return positionedParticles;
    }
}
