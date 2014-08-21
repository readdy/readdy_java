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

import readdy.api.sim.core.particle.IParticleAllAccess;

/**
 *
 * @author schoeneberg
 */
public class Particle implements IParticleAllAccess {

    private int particleId;
    private int typeId;
    private double[] coords;
    private int pos;

    public void setIndex(int pos){
        this.pos=pos;
    }
    
    public int getIndex(){
        return(this.pos);
    }
    
    public Particle(int id, int typeId, double[] coords) {
        this.particleId = id;
        this.typeId = typeId;
        this.coords = coords;

    }

    public double[] get_coords() {
        return coords;
    }

    public void set_coords(double[] newCoords) {
        this.coords = newCoords;
    }

    public int get_id() {
        return particleId;
    }

    public int get_type() {
        return typeId;
    }

    public void set_id(int newId) {
        this.particleId = newId;
    }

    public void set_typeId(int newTypeId) {
        this.typeId = newTypeId;
    }

    public void print() {
        System.out.print("<p id=" + particleId + " typeId=" + typeId + " c=");
        for (int i = 0; i < coords.length; i++) {
            double c = coords[i];
            if (i == coords.length - 1) {
                System.out.print(c + " ");
            } else {
                System.out.print(c + ",");
            }
        }
        System.out.println("\\>");
    }
}
