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
package readdy.impl.sim.core.particle;

import java.util.HashMap;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.api.sim.core.particle.IParticleParametersEntry;

/**
 *
 * @author schoeneberg
 */
public class ParticleParametersEntry implements IParticleParametersEntry {

    private String typeName;
    private int typeId;
    private int numberOfDummyParticles;
    // diffusion constant
    private double D, defaultCollR, defaultRctR;
    // particle Radius, depending on interaction partner typ id
    private HashMap<Integer, Double> collisionRadiusMap;
    // reaction Radius, depending on interaction partner typ id
    private HashMap<Integer, Double> reactionRadiusMap;

    public ParticleParametersEntry(IParticleData particleData){
        this.typeName = particleData.getType();
        this.typeId = particleData.getId();
        this.numberOfDummyParticles = particleData.getNumberOfDummyParticles();
        this.D=particleData.getD();
        this.defaultCollR=particleData.get_defaultCollR();
        this.defaultRctR=particleData.get_defaultRctR();
        this.collisionRadiusMap = particleData.getCollisionRadiusMap();
        this.reactionRadiusMap = particleData.getReactionRadiusMap();
    }

    public int get_typeId() {
        return typeId;
    }

    public double get_D() {
        return D;
    }
    
    public int get_NumberOfDummyParticles() {
        return numberOfDummyParticles;
    }

    public double get_collisionRadiusWithPartnerType(int partnerTypeId) {
        if (collisionRadiusMap.containsKey(partnerTypeId)) {
            return collisionRadiusMap.get(partnerTypeId);
        } else {
            throw new RuntimeException("the partner type " + partnerTypeId + " is unknown");
        }
    }

    public double get_reactionRadiusWithPartnerType(int partnerTypeId) {
        if (reactionRadiusMap.containsKey(partnerTypeId)) {
            return reactionRadiusMap.get(partnerTypeId);
        } else {
            throw new RuntimeException("the partner type " + partnerTypeId + " is unknown");
        }
    }

    public String get_typeName() {
        return typeName;
    }

    public double get_defaultCollR() {
        return defaultCollR;
    }

    public double get_defaultRctR() {
        return defaultRctR;
    }

    public void print() {
        System.out.println("TypeName: " + typeName + " typeId: " + typeId + " D: " + D);
        System.out.println("default coll radius: " + defaultCollR);
        System.out.println("collisionRadiusMap: TypeId, radius");
        for (int key : collisionRadiusMap.keySet()) {
            System.out.println(" " + key + " " + collisionRadiusMap.get(key));
        }
        System.out.println("default reaction radius: " + defaultRctR);
        System.out.println("reactionRadiusMap: TypeId, radius");
        for (int key : reactionRadiusMap.keySet()) {
            System.out.println(" " + key + " " + reactionRadiusMap.get(key));
        }
        System.out.println();
    }
}
