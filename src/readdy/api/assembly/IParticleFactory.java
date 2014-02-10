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
package readdy.api.assembly;

import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileDataEntry;
import readdy.api.sim.core.particle.IParticleAllAccess;
import readdy.api.sim.core.particle.IParticleParameters;

/**
 *
 * @author schoeneberg
 */
public interface IParticleFactory {

    //ArrayList<IParticle> createRandomCoordinates(int particleTypeId,int nParticles);
    /**
     * Creates random coordinates, uniformly distributed on a flat Disk in dimension given by the discRadiusVectir
     * e.g. {1,1,0} will create a disk in xy dimension with maximal deviation in x of -1,1 and y of -1,1
     * @param particleTypeId
     * @param nParticles
     * @param diskCenter
     * @param diskRadius
     * @return
     */
    public void set_particleParameters(IParticleParameters particleParameters);

    public IParticleAllAccess createParticle(ITplgyCoordinatesFileDataEntry coordDataEntry);

    public IParticleAllAccess createParticle(int typeId, double[] coords);

    public int get_highestParticleIdSoFar();
}
