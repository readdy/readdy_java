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
import java.util.Iterator;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core_mc.IMetropolisDecider;
import readdy.api.sim.core_mc.IPotentialEnergyComputer;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.api.sim.top.rkHandle.rkExecutors.IReactionExecutor;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core_mc.MetropolisDecider;
import readdy.impl.sim.core_mc.PotentialEnergyComputer;
import statlab.base.datatypes.IIntIterator;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class StandardParticleBasedRkExecutor implements IReactionExecutor {

    IParticleConfiguration particleConfiguration = null;
    IParticleCoordinateCreator particleCoordinatesCreator = null;
    IPotentialManager potentialManager = null;
    IParticleParameters particleParameters = null;
    IMetropolisDecider metropolisDecider = null;
    IPotentialEnergyComputer potentialEnergyComputer = null;
    private boolean checkEnergyCriterionForParticlePlacement = true;

    public void set_ParticleCoordinateCreator(IParticleCoordinateCreator particleCoordinateCreator) {
        this.particleCoordinatesCreator = particleCoordinateCreator;
    }

    public void set_ParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void set_MetropolisDecider(IMetropolisDecider metropolisDecider) {
        this.metropolisDecider = metropolisDecider;
    }

    public void set_PotentialEnergyComputer(IPotentialEnergyComputer potentialEnergyComputer) {
        this.potentialEnergyComputer = potentialEnergyComputer;
    }

    public void setup(IParticleConfiguration particleConfiguration,
            IGroupConfiguration groupConfiguration,
            IPotentialManager potentialManager) {
        this.particleConfiguration = particleConfiguration;
        this.potentialManager = potentialManager;
    }

    public IReactionExecutionReport executeReaction(int stepId, IExecutableReaction rk) {
        if (settedUpCorrectly()) {
            IReactionExecutionReport report;
            int rkType = rk.get_rkTypeId();
            switch (rkType) {
                case 0:
                    report = execute_creation(stepId, rk);
                    break;

                case 1:
                    report = execute_decay(stepId, rk);
                    break;

                case 2:
                    report = execute_doubleCreation(stepId, rk);
                    break;

                case 3:
                    report = execute_annihilation(stepId, rk);
                    break;

                case 4:
                    report = execute_typeConversion(stepId, rk);
                    break;

                case 5:
                    report = execute_birth(stepId, rk);
                    break;

                case 6:
                    report = execute_death(stepId, rk);
                    break;

                case 7:
                    report = execute_fission(stepId, rk);
                    break;

                case 8:
                    report = execute_fusion(stepId, rk);
                    break;

                case 9:
                    report = execute_enzymatic(stepId, rk);
                    break;

                case 10:
                    report = execute_doubleTypeConversion(stepId, rk);

                    break;
                default:
                    throw new RuntimeException("not able to execute reactions of type '" + rkType + "'.");
            }
            return report;

        } else {
            throw new RuntimeException("not setted up correctly");
        }
    }

    private IReactionExecutionReport execute_creation(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 0 && nProducts == 1) {
            IExtendedIdAndType p_product1 = rk.get_products().get(0);

            // generate coordinates until you find some that do not collide
            double[] newCoords = new double[3];
            do {
                newCoords = particleCoordinatesCreator.createCoordinates(p_product1.get_type());
                System.out.println("coordinates " + newCoords[0] + "," + newCoords[1] + "," + newCoords[2] + "created but do they match?");
            } while ( // collisions with other particles are forbidden
                    doesNewParticleCollideWithOtherParticles(newCoords, p_product1.get_type())
                    || doCellularGeometryForcesActOnParticle(newCoords, p_product1.get_type()) // assumption is, that cellular geometry potentials have gradient = 0 potential where particles should be
                    );



            IParticle newParticle = particleConfiguration.createParticle(p_product1.get_type(), newCoords);
            report.addCreatedParticle(newParticle);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_decay(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 1 && nProducts == 0) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            particleConfiguration.removeParticle(p_educt1.get_id());
            report.addRemovedParticle(p_educt1);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_doubleCreation(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 0 && nProducts == 2) {

            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);
            double[][] crds = particleCoordinatesCreator.createRandomCoordinatesNextToEachOther(p_product1.get_type(), p_product2.get_type());

            IParticle newParticle1 = particleConfiguration.createParticle(p_product1.get_type(), crds[0]);
            IParticle newParticle2 = particleConfiguration.createParticle(p_product2.get_type(), crds[1]);
            report.addCreatedParticle(newParticle1);
            report.addCreatedParticle(newParticle2);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_annihilation(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 0) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            particleConfiguration.removeParticle(p_educt1.get_id());
            particleConfiguration.removeParticle(p_educt2.get_id());
            report.addRemovedParticle(p_educt1);
            report.addRemovedParticle(p_educt2);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_typeConversion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 1 && nProducts == 1) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            int typeIdBefore = p_educt1.get_type();
            particleConfiguration.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product1.get_type());
            report.addParticleTypeChange(p_educt1, typeIdBefore, p_product1.get_type());
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ") instead of (1,1)");
        }
    }

    private IReactionExecutionReport execute_birth(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 1 && nProducts == 2) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);

            if (p_educt1.get_type() == p_product1.get_type()) {
                double[] crd = particleCoordinatesCreator.createCoordinatesNextToGivenParticle(
                        p_educt1, p_product2.get_type());
                IParticle newParticle = particleConfiguration.createParticle(p_product2.get_type(), crd);
                report.addCreatedParticle(newParticle);
                report.setExecutionWasSuccessfull(true);
            } else {
                double[] crd = particleCoordinatesCreator.createCoordinatesNextToGivenParticle(
                        p_educt1, p_product1.get_type());
                IParticle newParticle = particleConfiguration.createParticle(p_product1.get_type(), crd);
                report.addCreatedParticle(newParticle);
                report.setExecutionWasSuccessfull(true);
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_death(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 1) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);

            if (p_educt1.get_type() == p_product1.get_type()) {
                particleConfiguration.removeParticle(p_educt2.get_id());
                report.addRemovedParticle(p_educt2);
                report.setExecutionWasSuccessfull(true);
            } else {
                particleConfiguration.removeParticle(p_educt1.get_id());
                report.addRemovedParticle(p_educt1);
                report.setExecutionWasSuccessfull(true);
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_fission(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 1 && nProducts == 2) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);

            double[][] crds = particleCoordinatesCreator.createCoordinatesNextToEachOtherFromGivenCenter(
                    p_educt1, p_product1.get_type(), p_product2.get_type());


            double energy0 = 0, energy1 = 0;

            double dE = 0;
            if (checkEnergyCriterionForParticlePlacement) {
                energy0 += getCurrentPotentialEnergyContribution_singleParticle(p_educt1);
                IParticle product1 = new Particle(-1, p_product1.get_type(), crds[0]);
                IParticle product2 = new Particle(-1, p_product2.get_type(), crds[1]);
                energy1 += getCurrentPotentialEnergyContribution_singleParticle(product1);
                energy1 -= getCurrentPotentialEnergyContribution_pairParticle(p_educt1,product1);
                energy1 += getCurrentPotentialEnergyContribution_singleParticle(product2);
                energy1 -= getCurrentPotentialEnergyContribution_pairParticle(p_educt1,product2);
                dE = energy1 - energy0;
                System.out.println("standardParticleBasedRkExecutor:");
                p_educt1.print();
                product1.print();
                product2.print();
                
                System.out.println("energy0 "+energy0);
                System.out.println("energy1+ "+getCurrentPotentialEnergyContribution_singleParticle(product1));
                System.out.println("energy1- "+getCurrentPotentialEnergyContribution_pairParticle(p_educt1,product1));
                System.out.println("energy1+ "+getCurrentPotentialEnergyContribution_singleParticle(product2));
                System.out.println("energy1= "+getCurrentPotentialEnergyContribution_pairParticle(p_educt1,product2));
                System.out.println("dE "+dE);
            }
            System.out.println("check for fission product placement acceptance, based on energy: " + dE + " kJ/mol");
            if (metropolisDecider.doWeAcceptGivenEnergyDifference(dE)) {
                System.out.println("placement accepted...proceed fission");
                particleConfiguration.removeParticle(p_educt1.get_id());
                report.addRemovedParticle(p_educt1);
                IParticle newParticle1 = particleConfiguration.createParticle(p_product1.get_type(), crds[0]);
                IParticle newParticle2 = particleConfiguration.createParticle(p_product2.get_type(), crds[1]);
                report.addCreatedParticle(newParticle1);
                report.addCreatedParticle(newParticle2);
                report.setExecutionWasSuccessfull(true);

            } else {
                System.out.println("placement rejected...no fission!");
                report.setExecutionComment("fission rejected because of too high energy: " + dE + " kJ/mol");
                report.setExecutionWasSuccessfull(false);
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_fusion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 1) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);

            double[] crd = particleCoordinatesCreator.createCenterOfMassCoordinate(
                    p_educt1, p_educt2, p_product1.get_type());
            particleConfiguration.removeParticle(p_educt1.get_id());
            particleConfiguration.removeParticle(p_educt2.get_id());
            IParticle newParticle = particleConfiguration.createParticle(p_product1.get_type(), crd);
            report.addRemovedParticle(p_educt1);
            report.addRemovedParticle(p_educt2);
            report.addCreatedParticle(newParticle);
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }

    }

    private IReactionExecutionReport execute_enzymatic(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);
            /*
             System.out.println("insideExecutor...");
             p_educt1.print();
             p_educt2.print();
             p_product1.print();
             p_product2.print();
             */

            if (p_educt1.get_type() == p_product1.get_type()) {
                System.out.println("A");
                int typeBefore = p_educt2.get_type();
                particleConfiguration.changeParticleType(p_educt2.get_id(), p_educt2.get_type(), p_product2.get_type());
                report.addParticleTypeChange(p_educt2, typeBefore, p_product2.get_type());
                report.setExecutionWasSuccessfull(true);
            } else {
                if (p_educt1.get_type() == p_product2.get_type()) {
                    System.out.println("B");
                    int typeBefore = p_educt2.get_type();
                    particleConfiguration.changeParticleType(p_educt2.get_id(), p_educt2.get_type(), p_product1.get_type());
                    report.addParticleTypeChange(p_educt2, typeBefore, p_product1.get_type());
                    report.setExecutionWasSuccessfull(true);
                } else {
                    if (p_educt2.get_type() == p_product1.get_type()) {
                        System.out.println("C");
                        int typeBefore = p_educt1.get_type();
                        particleConfiguration.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product2.get_type());
                        report.addParticleTypeChange(p_educt1, typeBefore, p_product2.get_type());
                        report.setExecutionWasSuccessfull(true);
                    } else {
                        if (p_educt2.get_type() == p_product2.get_type()) {
                            System.out.println("D");
                            int typeBefore = p_educt1.get_type();
                            particleConfiguration.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product1.get_type());
                            report.addParticleTypeChange(p_educt1, typeBefore, p_product1.get_type());
                            report.setExecutionWasSuccessfull(true);
                        }
                    }
                }
            }
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private IReactionExecutionReport execute_doubleTypeConversion(int stepId, IExecutableReaction rk) {
        IReactionExecutionReport report = new ReactionExecutionReport(stepId, rk);
        int nEducts = rk.get_educts().size();
        int nProducts = rk.get_products().size();
        if (nEducts == 2 && nProducts == 2) {
            Iterator<IParticle> iter = rk.get_educts().keySet().iterator();
            IParticle p_educt1 = iter.next();
            IParticle p_educt2 = iter.next();
            IExtendedIdAndType p_product1 = rk.get_products().get(0);
            IExtendedIdAndType p_product2 = rk.get_products().get(1);
            int typeIdBefore0 = p_educt1.get_type();
            int typeIdBefore1 = p_educt2.get_type();
            particleConfiguration.changeParticleType(p_educt1.get_id(), p_educt1.get_type(), p_product1.get_type());
            particleConfiguration.changeParticleType(p_educt2.get_id(), p_educt2.get_type(), p_product2.get_type());
            report.addParticleTypeChange(p_educt1, typeIdBefore0, p_product1.get_type());
            report.addParticleTypeChange(p_educt2, typeIdBefore1, p_product2.get_type());
            report.setExecutionWasSuccessfull(true);
            return report;
        } else {
            throw new RuntimeException("incompatible number of educts and products in reaction. (" + nEducts + "," + nProducts + ")");
        }
    }

    private boolean settedUpCorrectly() {
        return particleConfiguration != null
                && particleCoordinatesCreator != null
                && potentialManager != null
                && particleParameters != null
                && metropolisDecider != null
                && potentialEnergyComputer != null;
    }

    private boolean doCellularGeometryForcesActOnParticle(double[] coords, int pTypeId) {
        System.out.println("check potentials...");
        double pRadius = particleParameters.get_pCollisionRadius(pTypeId);
        Iterator<IPotential1> potentialIterator = potentialManager.getPotentials(pTypeId);
        while (potentialIterator.hasNext()) {
            IPotential1 pot = potentialIterator.next();
            System.out.println("check potential \"" + pot.get_name() + "\" for having 0 gradient");
            pot.set_coordinates(coords, pRadius);
            double[] grad = pot.getGradient();
            for (int i = 0; i < grad.length; i++) {
                double d = grad[i];
                if (d != 0) {
                    System.out.println("forces acting on particle! Reject Coordinates!.");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesNewParticleCollideWithOtherParticles(double[] coords1, int pTypeId1) {

        IIntIterator neighboringParticleIds = particleConfiguration.getNeighboringParticleIds(coords1);

        while (neighboringParticleIds.hasNext()) {

            int pId2 = neighboringParticleIds.next();

            IParticle p2 = particleConfiguration.getParticle(pId2);
            int pTypeId2 = p2.get_type();
            double radius12 = particleParameters.get_pCollisionRadius(pTypeId1, pTypeId2);
            double radius21 = particleParameters.get_pCollisionRadius(pTypeId2, pTypeId1);
            double r0 = radius12 + radius21;
            double[] coords2 = p2.get_coords();
            double r = DoubleArrays.distance(coords1, coords2);
            if (r < r0) {
                System.out.println("doesNewParticleCollideWithOtherParticles: yes!");
                return true;
            }
        }
        return false;


    }

    private double getCurrentPotentialEnergyContribution_singleParticle(IParticle p1) {

        // potentials of order 1
        //double E_potOrder1 = potentialEnergyComputer.computeEnergy(p1);
        // potentials of order 2
        double E_potOrder2 = 0;
        IIntIterator iterator_particleNeighborIds_c0 = particleConfiguration.getNeighboringParticleIds(p1.get_coords());
        while (iterator_particleNeighborIds_c0.hasNext()) {
            int neighbor_pId = iterator_particleNeighborIds_c0.next();

            if (p1.get_id() != neighbor_pId) { // prevent identical particles from being computed a potential between them
                IParticle p2 = particleConfiguration.getParticle(neighbor_pId);
                E_potOrder2 += potentialEnergyComputer.computeEnergy(p1, p2);
            }
        }
        //return E_potOrder1 + E_potOrder2;
        return E_potOrder2;
    }
    
    private double getCurrentPotentialEnergyContribution_pairParticle(IParticle p1, IParticle p2) {
        return potentialEnergyComputer.computeEnergy(p1, p2);
    }
}
