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

import java.util.Random;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.rkHandle.rkExecutors.IParticleCoordinateCreator;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class ParticleCoordinateCreator implements IParticleCoordinateCreator {

    private Random rand = new Random();
    private IParticleParameters particleParameters;
    private IGlobalParameters globalParameters;
    private IPotentialManager potentialManager;
    private double[][] latticeBounds_ordered = new double[3][2];

    public ParticleCoordinateCreator() {
    }

    public double[] createCoordinates(int pTypeId) {
        double pRadius = particleParameters.getParticleParametersEntry(pTypeId).get_defaultCollR();
        return getRandomCoordinates(pRadius);
    }

    public void setParticleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public void setGlobalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;

        double[][] latticeBounds = globalParameters.get_latticeBounds();
        for (int i = 0; i < latticeBounds.length; i++) {

            if (latticeBounds[i][0] < latticeBounds[i][1]) {
                latticeBounds_ordered[i][0] = latticeBounds[i][0];
                latticeBounds_ordered[i][1] = latticeBounds[i][1];

            } else {
                latticeBounds_ordered[i][1] = latticeBounds[i][1];
                latticeBounds_ordered[i][0] = latticeBounds[i][0];
            }
        }
    }

    public double[][] createRandomCoordinatesNextToEachOther(int p1TypeId, int p2TypeId) {
        double r1 = particleParameters.getParticleParametersEntry(p1TypeId).get_collisionRadiusWithPartnerType(p2TypeId);
        double r2 = particleParameters.getParticleParametersEntry(p2TypeId).get_collisionRadiusWithPartnerType(p1TypeId);
        double[] centerOfMass = getRandomCoordinates(r1 + r2);
        double[] direction = getNextRandomVectorOnUnitSphere();
        double[] newPos1 = DoubleArrays.add(centerOfMass, DoubleArrays.multiply(-1 * r1, direction));
        double[] newPos2 = DoubleArrays.add(centerOfMass, DoubleArrays.multiply(r2, direction));
        return new double[][]{newPos1, newPos2};
    }

    public double[] createCoordinatesNextToGivenParticle(IParticle p1, int p2_typeId) {
        // go in a random direction to a point on the sphere surface around p
        int type1 = p1.get_type();
        int type2 = p2_typeId;
        double r1 = particleParameters.getParticleParametersEntry(type1).get_collisionRadiusWithPartnerType(type2);
        double r2 = particleParameters.getParticleParametersEntry(type2).get_collisionRadiusWithPartnerType(type1);
        double[] direction = getNextRandomVectorOnUnitSphere();

        double[] posOnSurfaceOfP = DoubleArrays.add(p1.get_coords(), DoubleArrays.multiply(r1, direction));
        double[] newCoord = DoubleArrays.add(posOnSurfaceOfP, DoubleArrays.multiply(r2, direction));
        return newCoord;
    }

    public double[][] createCoordinatesNextToEachOtherFromGivenCenter(IParticle p, int pTypeId1, int pTypeId2) {
        double r1 = particleParameters.getParticleParametersEntry(pTypeId1).get_collisionRadiusWithPartnerType(pTypeId2);
        double r2 = particleParameters.getParticleParametersEntry(pTypeId2).get_collisionRadiusWithPartnerType(pTypeId1);
        double[] centerOfMass = p.get_coords();
        double[] direction = getNextRandomVectorOnUnitSphere();
        double[] newPos1 = DoubleArrays.add(centerOfMass, DoubleArrays.multiply(-1 * r1, direction));
        double[] newPos2 = DoubleArrays.add(centerOfMass, DoubleArrays.multiply(r2, direction));
        return new double[][]{newPos1, newPos2};
    }

    public double[] createCenterOfMassCoordinate(IParticle p1, IParticle p2, int pTypeId) {
        int pTypeId1 = p1.get_type();
        int pTypeId2 = p2.get_type();
        double r1 = particleParameters.getParticleParametersEntry(pTypeId1).get_collisionRadiusWithPartnerType(pTypeId2);
        double r2 = particleParameters.getParticleParametersEntry(pTypeId2).get_collisionRadiusWithPartnerType(pTypeId1);
        double[] coord1 = p1.get_coords();
        double[] coord2 = p2.get_coords();
        // assumed mass of the particle concerning its volume
        double m1 = r1*r1*r1;
        double m2 = r2*r2*r2;
        double M = m1+m2;
        double[] centerOfMass =DoubleArrays.multiply(1/M,
                DoubleArrays.add(
                    DoubleArrays.multiply(m1, coord1),
                    DoubleArrays.multiply(m2, coord2)
                    )
                );
        //AdvancedSystemOut.println("centerOfMass: ",centerOfMass,"");
        return centerOfMass;
    }

    /**
     * returns random coordinates for a particle that are uniformly distributed inside the lattice bounds.
     * @return
     */
    private double[] getRandomCoordinates(double pRadius) {

        // the rand.nextDouble returns a pseudorandom number in [0,1]

        double randCoordX = latticeBounds_ordered[0][0] + pRadius + rand.nextDouble() * (latticeBounds_ordered[0][1] - latticeBounds_ordered[0][0] - pRadius);
        double randCoordY = latticeBounds_ordered[1][0] + pRadius + rand.nextDouble() * (latticeBounds_ordered[1][1] - latticeBounds_ordered[1][0] - pRadius);
        double randCoordZ = latticeBounds_ordered[2][0] + pRadius + rand.nextDouble() * (latticeBounds_ordered[2][1] - latticeBounds_ordered[2][0] - pRadius);

        return new double[]{randCoordX, randCoordY, randCoordZ};
    }

    private double[] getNextRandomVectorOnUnitSphere() {
        double[] angles = getUniformDistributedAngles();
        return sphericalToCartesian(1, angles[0], angles[1]);
    }

    // 0 and 1.
    private double inverseCDF_for_theta(double rand) {
        return 2 * Math.asin(Math.sqrt(rand));
    }

    private double[] getUniformDistributedAngles() {

        double theta = inverseCDF_for_theta(rand.nextDouble());
        double phi = rand.nextDouble() * 2 * Math.PI;
        return new double[]{theta, phi};
    }

    private double[] sphericalToCartesian(double r, double theta, double phi) {
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.sin(theta) * Math.sin(phi);
        double z = r * Math.cos(theta);
        return new double[]{x, y, z};
    }
}
