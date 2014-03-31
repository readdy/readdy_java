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
package readdy.impl.io.in.par_particle;

import readdy.api.io.in.par_particle.IParticleData;
import java.util.HashMap;

/**
 *
 * @author schoeneberg
 */
public class ParticleData implements IParticleData {

    private int id;
    private String type;
    private int typeId;
    private int numberOfDummyParticles;
    // diffusion constant
    private double D, defaultCollR, defaultRctR;
    // particle Radius, depending on interaction partner typ id
    private HashMap<Integer, Double> collisionRadiusMap;
    // reaction Radius, depending on interaction partner typ id
    private HashMap<Integer, Double> reactionRadiusMap;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public double getD() {
        return D;
    }

    public void setD(double D) {
        this.D = D;
    }
    
    public void setNumberOfDummyParticles(int nD) {
        this.numberOfDummyParticles = nD;
    }

    public HashMap<Integer, Double> getCollisionRadiusMap() {
        return collisionRadiusMap;
    }

    public void setCollisionRadiusMap(HashMap<Integer, Double> collisionRadiusMap) {
        this.collisionRadiusMap = collisionRadiusMap;
    }

    public int getId() {
        return id;
    }
    
    public int getNumberOfDummyParticles() {
        return numberOfDummyParticles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Integer, Double> getReactionRadiusMap() {
        return reactionRadiusMap;
    }

    public void setReactionRadiusMap(HashMap<Integer, Double> reactionRadiusMap) {
        this.reactionRadiusMap = reactionRadiusMap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ParticleData() {
    }

    public double get_defaultCollR() {
        return defaultCollR;
    }

    public double get_defaultRctR() {
        return defaultRctR;
    }

    public void setDefaultCollisionRadius(Double defaultCollisionRadius) {
        this.defaultCollR = defaultCollisionRadius;
    }

    public void setDefaultReactionRadius(Double defaultReactionRadius) {
        this.defaultRctR = defaultReactionRadius;
    }

    public void print() {
        System.out.println("ID: " + id + " Type: " + type + " typeId: " + typeId + " D: " + D);
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
