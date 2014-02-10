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

import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.potentials.IPotential;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;

/**
 *
 * @author johannesschoeneberg
 */
public class LatticeBoxSizeComputer implements ILatticeBoxSizeComputer {

    double latticeBoxSize = 0;

    public LatticeBoxSizeComputer(
            IParticleParameters particleParameters,
            IPotentialInventory potentialInventory,
            IGlobalParameters globalParameters) {

        // check first, if there is a user input in the global parameters given.
        // if that is the case, stop here and use that value.
        try {
            latticeBoxSize = globalParameters.get_latticeBoxSize();
        } catch (Exception e) {

            // if there is no user input, determine the lattice box size automatically.

            // from the particle parameters, determine the maximal interaction distance between two particles.
            // this means e.g. if p0 has collision radius r0, and p1 has collision radius r1, the distance
            // compared here to determine the maximum is d=r0+r1.
            // The same applies for  interaction radii.
            double globalMaxParticleParticleInteractionDistance = particleParameters.get_globalMaxParticleParticleInteractionRadius();

            // the globalMaxParticleParticleInteractionDistance 
            // is already the lattice box size, if there are only repulsion potentials
            // between the particles.
            // if there are other potentials, like attractive ones, we have to take
            // the attraction distance into account additionally. 
            // i.e. d+potentialCutoff
            // the maximalInteractionDistance between particles plus the distance, 
            // a potential would reach out additional to that.
            double maximalPotentialCutoffDistance = 0;
            for (int potId : potentialInventory.getPotentialIds()) {
                IPotential pot = potentialInventory.getPotential(potId);
                if (pot.get_parameterMap().containsKey("cutoffDistance")) {
                    double potCutoffDistance = Double.parseDouble(pot.get_parameterMap().get("cutoffDistance"));
                    if (potCutoffDistance > maximalPotentialCutoffDistance) {
                        maximalPotentialCutoffDistance = potCutoffDistance;
                    }
                }
            }

            this.latticeBoxSize = globalMaxParticleParticleInteractionDistance + maximalPotentialCutoffDistance;

        }

    }

    public double getLatticeBoxSize() {
        return latticeBoxSize;
    }
}
