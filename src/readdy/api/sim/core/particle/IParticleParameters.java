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
package readdy.api.sim.core.particle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author schoeneberg
 */
public interface IParticleParameters {


    public HashSet<Integer> getAllParticleTypes();


    public double get_maxPInteractionRadius(int typeId1, int typeId2);

    public int getParticleTypeIdFromTypeName(String typeName);

    public String getParticleTypeNameFromTypeId(int typeId);

    /**
     *  returns the maximal interaction radius - this is the max over all 
     *  collision radii and reaction radii between the particle type given 
     *  and any other particle type.
     * @param type
     * @return
     */
    public double get_maxPInteractionRadius(int type);

    /**
     * The parameter DfactorNoise is the precomputed scaling factor for the noise.
     * In our case this is Math.sqrt(2 * D *setup.getTimestep());
     * @param pTypeId
     * @return
     */
    public double get_DFactorNoise(int pTypeId);

    /**
     * The parameter DfactorPot is the precomputed scaling factor for the potential displacement.
     * In our case this is: -1 * D * dt / (Kb*T)
     * @param pTypeId
     * @return
     */
    public double get_DFactorPot(int pTypeId);

    public double get_pCollisionRadius(int pTypeId);

    public double get_pReactionRadius(int pTypeId);

    public double get_pCollisionRadius(int pTypeId1, int pTypeId2);

    public double get_pReactionRadius(int pTypeId1, int pTypeId2);

    public boolean doesTypeIdExist(int typeId);

    public boolean doesTypeNameExist(String typeName);

    public IParticleParametersEntry getParticleParametersEntry(int pTypeId1);

    //########################################################################
    // generated for optimization purposes -> it is much faster to get
    // data from an existing double array ask a class all the time
    //########################################################################
    public double[][] getCollisionRadiusByTypeMatrix_dMatrix();

    public double[][] getInteractionRadiusByTypeMatrix_dMatrix();

    public double[] getMaxInteractionRadiusMapByType_dArr();

    public double[][] getReactionRadiusByTypeMatrix_dMatrix();

    public double[] getdFactorNoiseMap_dArr();

    public double[] getdFactorPotMap_dArr();

    public double[] getDefaultCollRadiusMap_dArr();

    public double[] getDefaultRctRadiusMap_dArr();

    public double get_globalMaxParticleParticleInteractionRadius();
}
