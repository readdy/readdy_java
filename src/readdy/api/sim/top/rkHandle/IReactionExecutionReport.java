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
package readdy.api.sim.top.rkHandle;

import java.util.ArrayList;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IGroup;

/**
 *
 * @author schoeneberg
 */
public interface IReactionExecutionReport {
    
    
    public String getExecutionComment();
   
    public void setExecutionComment(String executionComment);
    
    
    //----- failed execution related
    public void setExecutionWasSuccessfull(boolean b);
    
    public boolean getExecutionWasSuccessfull();
    
    
    //----- general Information
    public int get_stepIdOfExecution();
   

    //----- particle related
    public void addRemovedParticle(IParticle p_educt1);

    public void addCreatedParticle(IParticle newParticle1);

    public void addParticleTypeChange(IParticle p_educt1, int _type, int _type0);

    public ArrayList<IParticle> getCreatedParticles();

    public IExecutableReaction getExecutableReaction();

    public ArrayList<IParticle> getRemovedParticles();

    public ArrayList<Integer> getTypeChangeParticles_from();

    public ArrayList<Integer> getTypeChangeParticles_to();

    public ArrayList<IParticle> getTypeChangedParticles();

    //----- group related
    public void addRemovedGroup(IGroup g);

    public void addCreatedGroup(IGroup g);

    public void addGroupTypeChange(IGroup g, int typeFrom, int typeTo);

    public ArrayList<IGroup> getCreatedGroups();

    public ArrayList<IGroup> getRemovedGroups();

    public ArrayList<Integer> getTypeChangeGroups_from();

    public ArrayList<Integer> getTypeChangeGroups_to();

    public ArrayList<IGroup> getTypeChangedGroups();

    //----- general functions

    public void print();

    public void setConflictReaction(IExecutableReaction winnerReaction);
}
