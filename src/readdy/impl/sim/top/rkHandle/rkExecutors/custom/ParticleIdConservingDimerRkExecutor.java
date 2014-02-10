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
package readdy.impl.sim.top.rkHandle.rkExecutors.custom;

import java.util.Iterator;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.rkExecutors.ICustomReactionExecutor;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.sim.top.rkHandle.ReactionExecutionReport;

/**
 *
 * @author schoeneberg
 */
public class ParticleIdConservingDimerRkExecutor implements ICustomReactionExecutor {

    IParticleConfiguration particleConfig = null;
    IParticleCoordinateCreator particleCoordinateCreator = null;
    int forwardRkId = -1;
    int backwardRkId = -1;

    public void setParticleCoordinateCreator(IParticleCoordinateCreator particleCoordinateCreator) {
        this.particleCoordinateCreator = particleCoordinateCreator;
    }

    public void setup(IParticleConfiguration particleConfiguration, IGroupConfiguration groupConfiguration, IPotentialManager potentialManager) {
        this.particleConfig = particleConfiguration;
    }

    public void setForwardAndBackwardRkId(int forwardId, int backwardId) {
        this.forwardRkId = forwardId;
        this.backwardRkId = backwardId;
    }

    public IReactionExecutionReport executeReaction(int stepId, IExecutableReaction rk) {
        if (settedUpCorrectly()) {
            IReactionExecutionReport report;
            int rkType = rk.get_rkTypeId();
            if (rkType == forwardRkId) {
                report = execute_idConservingActRAndGCplxFormation(stepId, rk);
                report.setExecutionWasSuccessfull(true);
            } else {
                if (rkType == backwardRkId) {
                    report = execute_idConservingActRAndActGCplxFission(stepId, rk);
                    report.setExecutionWasSuccessfull(true);
                } else {
                    throw new RuntimeException("not able to execute reactions of type '" + rkType + "'.");
                }
            }
            return report;

        } else {
            throw new RuntimeException("not setted up correctly");
        }
    }

    private boolean settedUpCorrectly() {
        return particleConfig != null && particleCoordinateCreator != null && backwardRkId != -1 && forwardRkId != -1;
    }

    private IReactionExecutionReport execute_idConservingActRAndGCplxFormation(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 1) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);


            // FORWARD DIRECTION
            if (p_educt1.get_type() == 1 /* A */
                    && p_educt2.get_type() == 0 /* B */
                    && p_product1.get_type() == 2/* AB_cplx */) {
                report.addRemovedParticle(p_educt2);
                report.addParticleTypeChange(p_educt1, p_educt1.get_type(), p_product1.get_type());
                report.setExecutionWasSuccessfull(true);
                particleConfig.removeParticle(p_educt2.get_id());
                particleConfig.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product1.get_type());

            } else {
                if (p_educt2.get_type() == 1 /* A */
                        && p_educt1.get_type() == 0 /* B */
                        && p_product1.get_type() == 2/* AB_cplx */) {
                    report.addRemovedParticle(p_educt1);
                    report.addParticleTypeChange(p_educt2, p_educt2.get_type(), p_product1.get_type());
                    report.setExecutionWasSuccessfull(true);
                    particleConfig.removeParticle(p_educt1.get_id());
                    particleConfig.changeParticleType(p_educt2.get_id(), p_educt2.get_type(), p_product1.get_type());

                }
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_idConservingActRAndActGCplxFission(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 1 && nProducts == 2) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);

            //FORWARD
            if (p_educt1.get_type() == 2 /* AB_cplx * */
                    && p_product1.get_type() == 1/* A */
                    && p_product2.get_type() == 0 /* B */) {
                double[][] crds = particleCoordinateCreator.createCoordinatesNextToEachOtherFromGivenCenter(p_educt1, p_product1.get_type(), p_product2.get_type());

                // conserve the particleId of the complex for the R*
                particleConfig.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product1.get_type());
                IParticle newParticle2 = particleConfig.createParticle(p_product2.get_type(), crds[1]);
                report.addParticleTypeChange(p_educt1, p_educt1.get_type(), p_product1.get_type());
                report.addCreatedParticle(newParticle2);
                report.setExecutionWasSuccessfull(true);
            } else {
                if (p_educt1.get_type() == 2 /* AB_cplx * */
                        && p_product2.get_type() == 1/* A */
                        && p_product1.get_type() == 0 /* B */) {
                    double[][] crds = particleCoordinateCreator.createCoordinatesNextToEachOtherFromGivenCenter(p_educt1, p_product1.get_type(), p_product2.get_type());

                    // conserve the particleId of the complex for the A
                    particleConfig.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product2.get_type());
                    IParticle newParticle1 = particleConfig.createParticle(p_product2.get_type(), crds[1]);
                    report.addParticleTypeChange(p_educt1, p_educt1.get_type(), p_product2.get_type());
                    report.addCreatedParticle(newParticle1);
                    report.setExecutionWasSuccessfull(true);
                }
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }
}
