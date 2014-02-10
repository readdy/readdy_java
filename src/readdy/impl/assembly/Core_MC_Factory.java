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

import readdy.api.assembly.ICoreFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.ICore;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.impl.sim.core.bd.NoiseDisplacementComputer;
import readdy.impl.sim.core_mc.Core_MC;
import readdy.impl.sim.core_mc.MetropolisDecider;
import readdy.impl.sim.core_mc.PotentialEnergyComputer;


/**
 *
 * @author schoeneberg
 */
public class Core_MC_Factory implements ICoreFactory {

    // used from classical readdy
    IGlobalParameters globalParameters = null;
    IPotentialManager potentialManager = null;
    IParticleConfiguration particleConfiguration = null;
    IParticleParameters particleParameters = null;
    IReactionObserver reactionObserver = null;

    public void set_GlobalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public void set_PotentialManager(IPotentialManager potentialManager) {
        this.potentialManager = potentialManager;
    }

    public void set_ParticleConfiguration(IParticleConfiguration particleConfig) {
        this.particleConfiguration = particleConfig;
    }

    public void set_ParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void set_ReactionObserver(IReactionObserver reactionObserver) {
        this.reactionObserver = reactionObserver;
    }

    public ICore createCore() {
        if (allInputAvailable()) {
            Core_MC core = new Core_MC();

            // monte carlo specific
            MetropolisDecider metropolisDecider = new MetropolisDecider();
            metropolisDecider.set_GlobalParameters(globalParameters);
            core.set_MetropolisDecider(metropolisDecider);

            PotentialEnergyComputer potentialEnergyComputer = new PotentialEnergyComputer();
            potentialEnergyComputer.set_particleParameters(particleParameters);
            potentialEnergyComputer.set_potentialManager(potentialManager);
            core.set_PotentialEnergyComputer(potentialEnergyComputer);

            // used from classical readdy
            NoiseDisplacementComputer noiseDisplacementComputer = new NoiseDisplacementComputer();
            core.set_NoiseDisplacementComputer(noiseDisplacementComputer);
            core.set_ParticleConfiguration(particleConfiguration);
            core.set_ParticleParameters(particleParameters);
            core.set_ReactionObserver(reactionObserver);

            // initialize the core to have the current potential energy value available
            core.initialize();

            return core;
        } else {
            throw new RuntimeException("necessary building blocks not present for assembly."
                    + "construction cancelled!");
        }
    }

    private boolean allInputAvailable() {
        return (globalParameters != null
                && potentialManager != null
                && particleConfiguration != null
                && particleParameters != null
                && reactionObserver != null);
    }
}
