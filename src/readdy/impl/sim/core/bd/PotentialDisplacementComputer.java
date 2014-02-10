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

import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.sim.core.bd.IPotentialDisplacementComputer;
import readdy.api.sim.core.pot.potentials.IPotential1;

/**
 *
 * @author schoeneberg
 */
public class PotentialDisplacementComputer implements IPotentialDisplacementComputer {

    public PotentialDisplacementComputer() {
    }

    public double[] computeSingleDisplacement(double[] coords,
            double DFactorPot,
            double pRadius,
            IPotential1 pot) {
        pot.set_coordinates(coords, pRadius);
        // displacement = -1 * Grad(U)
        double[] displacement = new double[3];
        displacement[0] = -1 * DFactorPot * pot.getGradient()[0];
        displacement[1] = -1 * DFactorPot * pot.getGradient()[1];
        displacement[2] = -1 * DFactorPot * pot.getGradient()[2];
        return displacement;

    }
    double[] displacement1 = new double[3];
    double[] displacement2 = new double[3];
    double[][] pairDisplacement = new double[2][];

    public double[][] computePairDisplacement(double[] coords1,
            double DFactorPot1,
            double[] coords2,
            double DFactorPot2,
            double radius,
            double dist,
            IPotential2 pot) {

        pot.set_coordinates(coords1, coords2, dist, radius);

        double[] grad = pot.getGradient();
        double precompute = -1 * DFactorPot1;


        // displacement = -1 * Grad(U)
        displacement1[0] = precompute * grad[0];
        displacement1[1] = precompute * grad[1];
        displacement1[2] = precompute * grad[2];

        // displacement in opposite direction = -1* direction
        // => -1* (-1* grad(U))
        displacement2[0] = DFactorPot2 * grad[0];
        displacement2[1] = DFactorPot2 * grad[1];
        displacement2[2] = DFactorPot2 * grad[2];

        return new double[][]{displacement1, displacement2};
    }
}
