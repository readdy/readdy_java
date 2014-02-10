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

import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileDataEntry;
import readdy.api.sim.core.particle.IParticleAllAccess;
import readdy.api.assembly.IParticleFactory;
import java.util.ArrayList;
import java.util.Random;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.tools.GeometryTools;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class ParticleFactory implements IParticleFactory {

    private Random rand = new Random();
    private static int highestUsedParticleID = -1;
    private static int usedDimensionality = 3;
    private GeometryTools geometryTools = new GeometryTools();
    private IParticleParameters particleParameters = null;

    public ArrayList<IParticle> createRandomCoordinates(int type, int nParticles) {
        double spreadFactor = 2;//2
        ArrayList<IParticle> list = new ArrayList();
        for (int i = 0; i < nParticles; i++) {

            double[] coords = new double[]{spreadFactor * rand.nextGaussian(), spreadFactor * rand.nextGaussian(), 0};
            //System.out.println(coords[0] + "," + coords[1] + "," + coords[2]);
            Particle p = new Particle(-1, type, coords);
            /*
            int potID=Math.abs(rand.nextInt())%2;
            potID=1;
            p.addInfluencingPotential(potID);
             * 
             */
            list.add(p);
            //System.out.println("particle generated with ID: " + highestUsedParticleID);


        }
        return list;
    }

    public ArrayList<IParticle> createCoordinates_uniform_onDisk(int type, int nParticles, double[] diskCenter, double diskRadius) {

        ArrayList<IParticle> list = new ArrayList();
        double x, y, z;
        for (int i = 0; i < nParticles; i++) {

            do {
                x = 2 * rand.nextDouble() * diskRadius - diskRadius;
                y = 2 * rand.nextDouble() * diskRadius - diskRadius;
                z = 0;
            } while (DoubleArrays.norm(new double[]{x, y, z}) > diskRadius);
            double[] coords = new double[]{x + diskCenter[0], y + diskCenter[1], z + diskCenter[2]};
            System.out.println(coords[0] + "," + coords[1] + "," + coords[2]);
            Particle p = new Particle(-1, type, coords);

            list.add(p);
            System.out.println("particle generated with ID: " + highestUsedParticleID);


        }
        return list;
    }

    public ArrayList<IParticle> createRandomCoordinates1(int type, int nParticles) {
        double spreadFactor = 5;
        ArrayList<IParticle> list = new ArrayList();
        for (int i = 0; i < nParticles; i++) {

            double[] coords = new double[]{spreadFactor * rand.nextGaussian(), spreadFactor * rand.nextGaussian(), 0};
            System.out.println(coords[0] + "," + coords[1] + "," + coords[2]);
            Particle p = new Particle(-1, type, coords);
            /*
            int potID=Math.abs(rand.nextInt())%2;
            potID=1;
            p.addInfluencingPotential(potID);
             *
             */
            list.add(p);
            System.out.println("particle generated with ID: " + highestUsedParticleID);


        }
        return list;
    }

    public IParticleAllAccess generateParticle(IParticle p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getHighestUsedParticleId() {
        return highestUsedParticleID;
    }

    private int getNextId() {
        int id = highestUsedParticleID + 1;
        highestUsedParticleID++;
        return id;
    }

    private int peekNextId() {

        return highestUsedParticleID + 1;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    /**
     * this is for the initial setup of the topology
     * @param coordDataEntry
     * @return
     */
    public IParticleAllAccess createParticle(ITplgyCoordinatesFileDataEntry coordDataEntry) {

        int newId = coordDataEntry.get_id();
        if (newId == -1 || !(newId > highestUsedParticleID)) {
            System.out.print("ATTENTION: new particle ID was created for particle with ID "+newId+". Was smaller than highestUsed Particle ID: "+highestUsedParticleID);
            newId = peekNextId();
        }
        int typeId = coordDataEntry.get_type();
        double[] coords = coordDataEntry.get_c();

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

        //System.out.println("id:" + newId + " typeId " + typeId + " coords " + coords[0] + "," + coords[1] + "," + coords[2]);
        return createParticle(newId, typeId, coords);

    }

    public IParticleAllAccess createParticle(int typeId, double[] coords) {

        if (newParticleOK(peekNextId(), typeId, coords)) {
            int newId = getNextId();
            Particle p = new Particle(newId, typeId, coords);
            highestUsedParticleID = newId;
            return p;
        } else {
            throw new RuntimeException("the particle that is supposed to be created is corrupted:"
                    + "id: " + peekNextId()
                    + "type:" + typeId
                    + "coords:" + coords[0]
                    + "," + coords[1]
                    + "," + coords[2]
                    + " + [additional dimensions not displayed]...");
        }
    }

    public IParticleAllAccess createParticle(int newId, int typeId, double[] coords) {
        if (settedUpProperly()) {
            // if this is the first particle that is created from the input file
            // we take its dimensionality for the rest of all other particles
            if (usedDimensionality == -1) {
                usedDimensionality = coords.length;
            }
            if (newParticleOK(newId, typeId, coords)) {
                Particle p = new Particle(newId, typeId, coords);
                highestUsedParticleID = newId;
                //System.out.println("particleFactory createSpecifiedParticle: createParticle:");
                //p.print();
                return p;
            } else {
                throw new RuntimeException("the particle that is supposed to be created is corrupted: "
                        + "id: " + newId
                        + "type: " + typeId
                        + "coords: " + coords[0]
                        + "," + coords[1]
                        + "," + coords[2]
                        + " + [additional dimensions not displayed]...");
            }
        } else {
            throw new RuntimeException("particle factory not setted up properly");
        }


    }

    private boolean newParticleOK(int newId, int typeId, double[] coords) {
        if (!(newId > highestUsedParticleID)) {
            System.out.println("newId > highestUsedParticleID " + newId + " " + highestUsedParticleID);
        }
        return (newId > highestUsedParticleID && newParticleOK(typeId, coords));

    }

    private boolean newParticleOK(int typeId, double[] coords) {

        boolean result = (particleParameters.doesTypeIdExist(typeId)
                && coords.length == usedDimensionality);
        if (!particleParameters.doesTypeIdExist(typeId)) {
            throw new RuntimeException("Particle Type " + typeId + " of particle to be created not existent");
        }
        if (coords.length != usedDimensionality) {
            throw new RuntimeException("used dimensionality problem");
        }
        return result;
    }

    private boolean settedUpProperly() {
        return (particleParameters != null);
    }

    public int get_highestParticleIdSoFar() {
        return highestUsedParticleID;
    }
}
