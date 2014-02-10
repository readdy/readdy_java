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
package readdy.impl.sim.core.bd;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import java.util.Date;
import readdy.api.sim.core.bd.INoiseDisplacementComputer;

/**
 * The noise displacement computer is responsible to generate the diffusive
 * displacements for every particle.
 * It operates with the cern.colt.jet library and uses a mersenne twister
 * for random number generation.
 * @author schoeneberg
 */
public class NoiseDisplacementComputer implements INoiseDisplacementComputer {

    //Normal cernJetNormal = new Normal(0.0, 1.0, new MersenneTwister(new Date()));
    // the date() has not enough precision for me
    // note, that the casting from long to int makes errors. But here they do not matter. 
    // in effect, the enhance the wanted effect of generating multiple different seeds when
    // starting simulations more or less simultaneously
    Normal cernJetNormal = new Normal(0.0, 1.0, new MersenneTwister((int)System.nanoTime()));
    double[] displacement = new double[3];

    public double[] computeDisplacement(double[] coords, double DfactorNoise) {

        displacement[0] = DfactorNoise * cernJetNormal.nextDouble();
        displacement[1] = DfactorNoise * cernJetNormal.nextDouble();
        displacement[2] = DfactorNoise * cernJetNormal.nextDouble();


        return displacement;
    }
    
    
}
