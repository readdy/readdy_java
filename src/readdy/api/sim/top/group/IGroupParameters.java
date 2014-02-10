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
package readdy.api.sim.top.group;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author schoeneberg
 */
public interface IGroupParameters {

    /**
     * potId1 -> int[][]{ int[]{internalId}, int[]{internalId}} // order 1 potential
     * potId5 -> int[][]{ int[]{ int[]{internalId1,internalId2}} // order 2 potential
     * @param groupInternalParticleId1
     * @param groupInternalParticleId2
     * @return the potentials of order 2 influcencing the given Ids
     */
    HashMap<Integer, int[][]> getGroupInternalPotentials(int groupTypeId);

    ArrayList<Integer> getGroupInternalIds(int groupTypeId);

    int getNumberOfGroupMembers(int groupTypeId);

    public int getMaxNumberOfAssignmentsPerParticle(int groupTypeId);

    public boolean doesTypeIdExist(int groupTypeId);

    public boolean doesTypeNameExist(String groupTypeName);

    
    /**
     * return the building blocks of the group - sorted by their interal Ids
     * @param groupTypeId
     * @return
     */
    public ArrayList<IExtendedIdAndType> getBuildingBlocks(int groupTypeId);

    public int getGroupTypeIdFromTypeName(String typeName);
    
    public String getGroupTypeNameFromTypeId(int groupTypeId);

    /**
     * returns the particle behind the groupInternalId
     * @param groupInternalId
     * @return
     */
    public IExtendedIdAndType getElmementalBuildingBlock(int groupInternalId);

    /**
     * geometry construction related things.
     * @param gTypeId
     * @return
     */
    public double[] getGroupTemplateNormalVector(int gTypeId);

    public double[] getGroupTemplateOriginVector(int gTypeId);

    public ArrayList<double[]> getBuildingBlockTemplateCoordinates(int gTypeId);

}
