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
package readdy.impl.sim.core_mc;

import java.util.Iterator;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core_mc.IPotentialEnergyComputer;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class PotentialEnergyComputer implements IPotentialEnergyComputer {

    IPotentialManager potentialManager = null;
    IParticleParameters particleParameters = null;
    // for optimization purposes
    double[] defaultCollRadiusMap_dArr;
    double[][] collisionRadiusByTypeMatrix_dMatrix;
    double[] dFactorNoiseMap_dArr;
    double[] dFactorPotMap_dArr;

    public void set_potentialManager(IPotentialManager potentialManager) {
        this.potentialManager = potentialManager;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
        this.defaultCollRadiusMap_dArr = particleParameters.getDefaultCollRadiusMap_dArr();
        this.collisionRadiusByTypeMatrix_dMatrix = particleParameters.getCollisionRadiusByTypeMatrix_dMatrix();
        this.dFactorNoiseMap_dArr = particleParameters.getdFactorNoiseMap_dArr();
        this.dFactorPotMap_dArr = particleParameters.getdFactorPotMap_dArr();

    }

    public IPotentialManager get_potentialManager() {
        return potentialManager;
    }

    public double computeEnergy(IParticle p) {
        double E = 0;

        int pTypeId = p.get_type();
        double[] coords = p.get_coords();


        double pRadius = defaultCollRadiusMap_dArr[pTypeId];

        Iterator<IPotential1> potential1Iterator = potentialManager.getPotentials(p);

        while (potential1Iterator.hasNext()) {
            IPotential1 pot1 = potential1Iterator.next();

            pot1.set_coordinates(coords, pRadius);

            E += pot1.getEnergy();
        }
        return E;

    }

    public double computeEnergy(IParticle p1, IParticle p2) {
        double E = 0;

        int pTypeId1 = p1.get_type();
        double[] coords1 = p1.get_coords();
        int pTypeId2 = p2.get_type();
        double[] coords2 = p2.get_coords();


        double collRadius = collisionRadiusByTypeMatrix_dMatrix[pTypeId1][pTypeId2];
        
        
        double dist = DoubleArrays.distance(coords1, coords2);

        Iterator<IPotential2> potential2Iterator = potentialManager.getPotentials(p1, p2);

        while (potential2Iterator.hasNext()) {
            IPotential2 pot2 = potential2Iterator.next();

            pot2.set_coordinates(coords1, coords2, dist, collRadius);

            E += pot2.getEnergy();
        }

        return E;
    }
}
