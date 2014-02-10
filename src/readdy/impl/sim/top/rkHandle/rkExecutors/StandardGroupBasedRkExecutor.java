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
package readdy.impl.sim.top.rkHandle.rkExecutors;

import readdy.impl.sim.top.rkHandle.ReactionExecutionReport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;

/**
 *
 * @author schoeneberg
 */
public class StandardGroupBasedRkExecutor implements IReactionExecutor {

    IGroupInteriorParticlePositioner particlePositioner = null;
    IParticleConfiguration particleConfiguration;
    IGroupConfiguration groupConfig;
    IPotentialManager potentialManager;

    public void setParticlePositioner(IGroupInteriorParticlePositioner particlePositioner) {
        this.particlePositioner = particlePositioner;
    }

    public void setup(IParticleConfiguration particleConfiguration, IGroupConfiguration groupConfiguration, IPotentialManager potentialManager) {
        this.particleConfiguration = particleConfiguration;
        this.groupConfig = groupConfiguration;
        this.potentialManager = potentialManager;

    }

    public IReactionExecutionReport executeReaction(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report;
        int rkTypeId = rk.get_rkTypeId();
        switch (rkTypeId) {

            case 100:
                report = execute_group(stepId, rk);
                break;

            case 101:
                report = execute_ungroup(stepId, rk);
                break;

            case 102:
                report = execute_gTypeConversion(stepId, rk);
                break;

            case 103:
                report = execute_gFission(stepId, rk);
                break;

            case 104:
                report = execute_gFusion(stepId, rk);
                break;

            case 105:
                report = execute_gEnzymatic(stepId, rk);
                break;

            case 106:
                report = execute_gDoubleTypeConversion(stepId, rk);
                break;

            default:
                throw new RuntimeException("The ReactionType " + rkTypeId + " can not be handled by this reaction executor.");
        }
        return report;

    }

    private IReactionExecutionReport execute_group(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if ((nEducts == 2 || nEducts==4) && nProducts == 1) {
            IExtendedIdAndType g = rk.get_products().get(0);
            System.out.println("action");
            ArrayList<IParticle> positionedParticles = particlePositioner.positionParticles(g.get_type(), rk.get_educts().keySet());
            System.out.println("action2");
            IGroup newGroup = groupConfig.createGroup(g.get_type(), positionedParticles);
            System.out.println("action3");
            report.addCreatedGroup(newGroup);
            report.setExecutionWasSuccessfull(true);
            System.out.println("action4");
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_ungroup(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {
            IParticle p0 = rk.get_educts().keySet().iterator().next();
            IParticle p1 = rk.get_educts().keySet().iterator().next();
            // the educt particles should be mapped onto the same group
            if (rk.get_educts().get(p0).get_isGroup() == true
                    && rk.get_educts().get(p1).get_isGroup() == true
                    && (rk.get_educts().get(p0).get_id() == rk.get_educts().get(p1).get_id())) {
                Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
                IExtendedIdAndType g = iter.next().getValue();
                IGroup removedGroup = groupConfig.getGroup(g.get_id());
                groupConfig.removeGroup(g.get_id());
                report.addRemovedGroup(removedGroup);
                report.setExecutionWasSuccessfull(true);
                return report;
            } else {
                throw new RuntimeException("both particles are not mapped to the same group that should break.");
            }
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_gTypeConversion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 1) {
            IParticle p0 = rk.get_educts().keySet().iterator().next();
            IParticle p1 = rk.get_educts().keySet().iterator().next();
            // the educt particles should be mapped onto the same group
            if (rk.get_educts().get(p0).get_isGroup() == true
                    && rk.get_educts().get(p1).get_isGroup() == true
                    && (rk.get_educts().get(p0).get_id() == rk.get_educts().get(p1).get_id())) {
                Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
                IExtendedIdAndType g0 = rk.get_educts().get(p0);

                IExtendedIdAndType gProduct = rk.get_products().get(0);

                IGroup groupTypeChange = groupConfig.getGroup(g0.get_id());
                int typeIdFrom = g0.get_type();
                int typeIdTo = gProduct.get_type();
                groupConfig.changeGroupType(g0.get_id(), typeIdFrom, typeIdTo);
                report.addGroupTypeChange(groupTypeChange, typeIdFrom, typeIdTo);
                report.setExecutionWasSuccessfull(true);

                return report;
            } else {
                throw new RuntimeException("both particles are not mapped to the same group that should convert its type.");
            }
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_gFission(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {
            IParticle p0 = rk.get_educts().keySet().iterator().next();
            IParticle p1 = rk.get_educts().keySet().iterator().next();
            // the educt particles should be mapped onto the same group
            if (rk.get_educts().get(p0).get_isGroup() == true
                    && rk.get_educts().get(p1).get_isGroup() == true
                    && (rk.get_educts().get(p0).get_id() == rk.get_educts().get(p1).get_id())) {

               
                // assumption: All Building Blocks to build the group that is cleaved now
                // have been present in the first place and remain in place when the
                // group on top is removed
                Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
                IExtendedIdAndType g = iter.next().getValue();
                IGroup removedGroup = groupConfig.getGroup(g.get_id());
                groupConfig.removeGroup(g.get_id());
                report.addRemovedGroup(removedGroup);
                report.setExecutionWasSuccessfull(true);
                return report;
            } else {
                throw new RuntimeException("both particles are not mapped to the same group that should be cleaved.");
            }
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_gFusion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 1) {

            Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
            int g_educt1Id = iter.next().getValue().get_id();
            int g_educt2Id = iter.next().getValue().get_id();
            IGroup educt1 = groupConfig.getGroup(g_educt1Id);
            IGroup educt2 = groupConfig.getGroup(g_educt2Id);

            IExtendedIdAndType g_product = rk.get_products().get(0);

            ArrayList<IParticle> positionedParticles =
                    particlePositioner.rearrangeParticlesWithinGroups_fusion(educt1, educt2, g_product.get_type());
            IGroup createdGroup = groupConfig.createGroup(g_product.get_type(), positionedParticles);
            report.addCreatedGroup(createdGroup);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }

    }

    private IReactionExecutionReport execute_gEnzymatic(int stepId, IExecutableReaction rk) {

        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {

            Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
            IExtendedIdAndType g_educt1 = iter.next().getValue();
            IExtendedIdAndType g_educt2 = iter.next().getValue();
            IExtendedIdAndType g_product1 = rk.get_products().get(0);
            IExtendedIdAndType g_product2 = rk.get_products().get(1);

            if (g_educt1.get_type() == g_product1.get_type()) {
                int groupIdToChange = g_educt2.get_id();
                int typeIdFrom = g_educt2.get_type();
                int typeIdTo = g_product2.get_type();

                IGroup groupTypeChange = groupConfig.getGroup(groupIdToChange);

                groupConfig.changeGroupType(groupIdToChange, typeIdFrom, typeIdTo);
                report.addGroupTypeChange(groupTypeChange, typeIdFrom, typeIdTo);
                report.setExecutionWasSuccessfull(true);
            }
            if (g_educt1.get_type() == g_product2.get_type()) {
                int groupIdToChange = g_educt2.get_id();

                int typeIdFrom = g_educt2.get_type();
                int typeIdTo = g_product1.get_type();
                IGroup groupTypeChange = groupConfig.getGroup(groupIdToChange);

                groupConfig.changeGroupType(groupIdToChange, typeIdFrom, typeIdTo);
                report.addGroupTypeChange(groupTypeChange, typeIdFrom, typeIdTo);
                report.setExecutionWasSuccessfull(true);
            }
            if (g_educt2.get_type() == g_product1.get_type()) {
                int groupIdToChange = g_educt1.get_id();

                int typeIdFrom = g_educt1.get_type();
                int typeIdTo = g_product2.get_type();
                IGroup groupTypeChange = groupConfig.getGroup(groupIdToChange);

                groupConfig.changeGroupType(groupIdToChange, typeIdFrom, typeIdTo);
                report.addGroupTypeChange(groupTypeChange, typeIdFrom, typeIdTo);
                report.setExecutionWasSuccessfull(true);
            }
            if (g_educt2.get_type() == g_product2.get_type()) {
                int groupIdToChange = g_educt1.get_id();

                int typeIdFrom = g_educt1.get_type();
                int typeIdTo = g_product1.get_type();
                IGroup groupTypeChange = groupConfig.getGroup(groupIdToChange);

                groupConfig.changeGroupType(groupIdToChange, typeIdFrom, typeIdTo);
                report.addGroupTypeChange(groupTypeChange, typeIdFrom, typeIdTo);
                report.setExecutionWasSuccessfull(true);
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_gDoubleTypeConversion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);

        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {

            Iterator<Entry<IParticle, IExtendedIdAndType>> iter = rk.get_educts().entrySet().iterator();
            Entry<IParticle, IExtendedIdAndType> entry = iter.next();
            IExtendedIdAndType g_educt1 = entry.getValue();
            IExtendedIdAndType g_educt2 = iter.next().getValue();

            IExtendedIdAndType g_product1 = rk.get_products().get(0);
            IExtendedIdAndType g_product2 = rk.get_products().get(1);

            // switch if the order has been shuffled
            if(g_educt1.get_id() != g_product1.get_id()){
                IExtendedIdAndType tmp = g_product1;
                g_product1 = g_product2;
                g_product2 = tmp;
            }

            // --- first
            int groupIdToChange0 = g_educt1.get_id();

            int typeIdFrom0 = g_educt1.get_type();
            int typeIdTo0 = g_product1.get_type();
            IGroup groupTypeChange0 = groupConfig.getGroup(groupIdToChange0);

            groupConfig.changeGroupType(groupIdToChange0, typeIdFrom0, typeIdTo0);
            report.addGroupTypeChange(groupTypeChange0, typeIdFrom0, typeIdTo0);
            report.setExecutionWasSuccessfull(true);

            // --- second
            int groupIdToChange1 = g_educt2.get_id();

            int typeIdFrom1 = g_educt2.get_type();
            int typeIdTo1 = g_product2.get_type();
            IGroup groupTypeChange1 = groupConfig.getGroup(groupIdToChange1);

            groupConfig.changeGroupType(groupIdToChange1, typeIdFrom1, typeIdTo1);
            report.addGroupTypeChange(groupTypeChange1, typeIdFrom1, typeIdTo1);
            report.setExecutionWasSuccessfull(true);

            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }
}
