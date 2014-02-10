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
package readdy.impl.sim.top.rkHandle;

import java.util.ArrayList;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;

/**
 *
 * @author schoeneberg
 */
public class ReactionExecutionReport implements IReactionExecutionReport {

    int stepIdOfExecution;
    IExecutableReaction executableReaction;
    boolean executionWasSuccessfull = false; 
    String executionComment = "";
    IExecutableReaction conflictReaction = null;
    ArrayList<IParticle> removedParticles = new ArrayList();
    ArrayList<IParticle> createdParticles = new ArrayList();
    ArrayList<IParticle> typeChangedParticles = new ArrayList();
    ArrayList<Integer> typeChangeParticles_from = new ArrayList();
    ArrayList<Integer> typeChangeParticles_to = new ArrayList();
    ArrayList<IGroup> removedGroups = new ArrayList();
    ArrayList<IGroup> createdGroups = new ArrayList();
    ArrayList<IGroup> typeChangedGroups = new ArrayList();
    ArrayList<Integer> typeChangeGroups_from = new ArrayList();
    ArrayList<Integer> typeChangeGroups_to = new ArrayList();

    public ReactionExecutionReport(int stepId, IExecutableReaction executableReaction) {
        this.stepIdOfExecution = stepId;
        this.executableReaction = executableReaction;
    }
    //----- standard

    public String getExecutionComment() {
        return executionComment;
    }

    public void setExecutionComment(String executionComment) {
        this.executionComment = executionComment;
    }
    
    
    //----- failed execution related
    public void setExecutionWasSuccessfull(boolean b){
        this.executionWasSuccessfull = b;
    }
    
    public boolean getExecutionWasSuccessfull(){
        return(this.executionWasSuccessfull);
    }
    
    public void setConflictReaction(IExecutableReaction conflictReaction) {
        this.conflictReaction = conflictReaction;
    }
    
    public IExecutableReaction getConflictReaction() {
        return this.conflictReaction;
    }
    

    //----- particle related
    public void addRemovedParticle(IParticle p) {
        removedParticles.add(p);
    }

    public void addCreatedParticle(IParticle p) {
        createdParticles.add(p);
    }

    public void addParticleTypeChange(IParticle p, int typeFrom, int typeTo) {
        typeChangedParticles.add(p);
        typeChangeParticles_from.add(typeFrom);
        typeChangeParticles_to.add(typeTo);
    }

    public ArrayList<IParticle> getCreatedParticles() {
        return createdParticles;
    }

    public IExecutableReaction getExecutableReaction() {
        return executableReaction;
    }

    public ArrayList<IParticle> getRemovedParticles() {
        return removedParticles;
    }

    public ArrayList<Integer> getTypeChangeParticles_from() {
        return typeChangeParticles_from;
    }

    public ArrayList<Integer> getTypeChangeParticles_to() {
        return typeChangeParticles_to;
    }

    public ArrayList<IParticle> getTypeChangedParticles() {
        return typeChangedParticles;
    }
    //----- group related

    public void addRemovedGroup(IGroup g) {
        removedGroups.add(g);
    }

    public void addCreatedGroup(IGroup g) {
        createdGroups.add(g);
    }

    public void addGroupTypeChange(IGroup g, int typeFrom, int typeTo) {
        typeChangedGroups.add(g);
        typeChangeGroups_from.add(typeFrom);
        typeChangeGroups_to.add(typeTo);
    }

    public ArrayList<IGroup> getCreatedGroups() {
        return createdGroups;
    }

    public ArrayList<IGroup> getRemovedGroups() {
        return removedGroups;
    }

    public ArrayList<Integer> getTypeChangeGroups_from() {
        return typeChangeGroups_from;
    }

    public ArrayList<Integer> getTypeChangeGroups_to() {
        return typeChangeGroups_to;
    }

    public ArrayList<IGroup> getTypeChangedGroups() {
        return typeChangedGroups;
    }

    public void print() {
        System.out.println();
        System.out.println("###################################################");
        System.out.println("<ReactionExecutionReport>");
        System.out.println("***************************************************");
        System.out.println("occurred in timestep id:" + stepIdOfExecution);
        System.out.println("based on following reaction:");
        executableReaction.print();
        System.out.println("***************************************************");
        System.out.println("the following results came out:");
         System.out.println("\t--- execution related ---");
        System.out.println("was the execution successfull: "+this.executionWasSuccessfull);
        System.out.println();
        System.out.println("\t--- common ---");
        System.out.println("execution comment: "+this.executionComment);
        System.out.println();
        System.out.println("\t--- particle based ---");
        System.out.println("\t removedParticles:");
        for (IParticle p : removedParticles) {
            p.print();
        }
        System.out.println("\t createdParticles:");
        for (IParticle p : createdParticles) {
            p.print();
        }
        System.out.println("\t typeChangedParticles:");
        for (int i = 0; i < typeChangedParticles.size(); i++) {
            IParticle p = typeChangedParticles.get(i);
            p.print();
            int typeChangeFrom = typeChangeParticles_from.get(i);
            int typeChangeTo = typeChangeParticles_to.get(i);
            System.out.println("typeChange from: " + typeChangeFrom + " to " + typeChangeTo);
        }
        System.out.println();
        System.out.println("\t--- group based ---");
        System.out.println("\t removedGroups:");
        for (IGroup g : removedGroups) {
            g.print();
        }
        System.out.println("\t createdGroups:");
        for (IGroup g : createdGroups) {
            g.print();
        }
        System.out.println("\t typeChangedGroups:");
        for (int i = 0; i < typeChangedGroups.size(); i++) {
            IGroup g = typeChangedGroups.get(i);
            g.print();
            int typeChangeFrom = typeChangeGroups_from.get(i);
            int typeChangeTo = typeChangeGroups_to.get(i);
            System.out.println("typeChange from: " + typeChangeFrom + " to " + typeChangeTo);
        }
        System.out.println("</ReactionExecutionReport>");
        System.out.println("###################################################");
    }

    public int get_stepIdOfExecution() {
        return stepIdOfExecution;
    }

    
}
