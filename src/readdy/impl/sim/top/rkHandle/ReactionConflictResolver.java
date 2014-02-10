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
import java.util.HashMap;
import java.util.Random;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.impl.sim.top.group.ExtendedIdAndType;

/**
 *
 * @author schoeneberg
 */
public class ReactionConflictResolver implements IReactionConflictResolver {

    private Random rand = new Random();
    IGroupConfiguration groupConfiguration;
    final static boolean verbose = false;

    public void setGroupConfiguration(IGroupConfiguration groupConfiguration) {
        this.groupConfiguration = groupConfiguration;
    }

    public boolean isThereAConflict(IExecutableReaction executableReaction, IExecutableReaction otherExecutableReaction) {
        ArrayList<IExtendedIdAndType> checkListForConflicts = getParticleAndGroupIdsToCheckForConflicts(executableReaction);
        ArrayList<IExtendedIdAndType> otherCheckListForConflicts = getParticleAndGroupIdsToCheckForConflicts(otherExecutableReaction);
        return(conflictFound(executableReaction,checkListForConflicts,otherExecutableReaction,otherCheckListForConflicts));
    }

    public boolean isTheFirstOneWinningTheConflict(IExecutableReaction executableReaction, IExecutableReaction otherExecutableReaction) {
        if (rand.nextDouble() > 0.5) {
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * sort out and decide between reactions that are in conflict with each other
     * @param fullRkCandidateList
     * @return
     */
    public ArrayList<IExecutableReaction> resolveConflicts(ArrayList<IExecutableReaction> validReactionList) {
        
        if(verbose){System.out.println("ResolveConflicts input: validReactionList Size "+validReactionList.size());}
        /**
         *  reactions are in conflict, iff:
         *  they involve the same educt particles
         *  they involve the same educt groups
         *  there is an educt group that is somewhere else a product group
         */
        ArrayList<IExecutableReaction> currentTestList = new ArrayList(validReactionList);
        ArrayList<IExecutableReaction> tmpStorageList = new ArrayList();
        boolean conflictsFound = true; // intitialisation on true for the subsequent while loop
        while (conflictsFound) {

            conflictsFound = false;
            // take the first candidate and check it against all others
            while (!currentTestList.isEmpty()) {

                IExecutableReaction currentRk = currentTestList.remove(0);
                ArrayList<IExtendedIdAndType> currentCheckListForConflicts = getParticleAndGroupIdsToCheckForConflicts(currentRk);

                for (int i = 0; i < currentTestList.size(); i++) {
                    IExecutableReaction rkToCheckAgainst = currentTestList.get(i);
                    ArrayList<IExtendedIdAndType> otherCheckListForConflicts = getParticleAndGroupIdsToCheckForConflicts(rkToCheckAgainst);
                    if (conflictFound(currentRk,currentCheckListForConflicts, rkToCheckAgainst,otherCheckListForConflicts)) {

                        conflictsFound = true;
                        //System.out.println("conflict!");
                        if (rand.nextDouble() > 0.5) {
                            tmpStorageList.add(currentRk);
                        } else {
                            // this is just to see that in this case the current Rk
                            // is removed from the game
                            tmpStorageList.add(currentTestList.remove(i));
                        }
                        break;
                    }
                }
                // if the reaction comes to this point it is with no other
                // reaction in conflict and is therefor resolved
                if (!conflictsFound) {
                    tmpStorageList.add(currentRk);
                }
            }

            // swap the lists and iterate
            if (conflictsFound) {
                for(IExecutableReaction exRk:tmpStorageList){
                    currentTestList.add(exRk);
                }
                tmpStorageList.clear();
            }
        }
        if(verbose){System.out.println("ResolveConflicts output: resolvedConflicts Size "+tmpStorageList.size());}
        return tmpStorageList;
    }

    /**
     * get a list of all topics that have to be checked for a candidate.
     * these are:
     * for each educt: the particle itself plus all groups where it is involved in
     * (plus all products) not the products
     * @param rk
     * @return
     */
    private ArrayList<IExtendedIdAndType> getParticleAndGroupIdsToCheckForConflicts(IExecutableReaction rk) {
        HashMap<IParticle, IExtendedIdAndType> educts = rk.get_educts();
        ArrayList<IExtendedIdAndType> products = rk.get_products();

        ArrayList<IExtendedIdAndType> checkList = new ArrayList();
        for (IParticle p : educts.keySet()) {
            IExtendedIdAndType particleExtIdAndType = new ExtendedIdAndType(p);
            checkList.add(particleExtIdAndType);
            ArrayList<IExtendedIdAndType> involvedGroups = groupConfiguration.getAllGroupsWhereParticleIsInvolvedIn(p.get_id());
            checkList.addAll(involvedGroups);
        }
        //checkList.addAll(products);
        return checkList;
    }

    /**
     *
     * @param currentCheckListForConflicts
     * @param otherCheckListForConflicts
     * @return true if a conflict found, false otherwise
     */
    private boolean conflictFound(IExecutableReaction currentRk,
            ArrayList<IExtendedIdAndType> currentCheckListForConflicts,
            IExecutableReaction otherRk,
            ArrayList<IExtendedIdAndType> otherCheckListForConflicts) {

         /**
         * an exception are independent reactions like spontaneous creations of
         * particles
         */

        if(currentRk.get_educts().isEmpty() && otherRk.get_educts().isEmpty()){
            return false;
        }


        /**
         *  reactions are in conflict, iff:
         *  they involve the same educt particles
         *  they involve the same educt groups
         *
         */
        for (IExtendedIdAndType check : currentCheckListForConflicts) {
            for (IExtendedIdAndType checkAgainst : otherCheckListForConflicts) {
                if (check.get_isGroup() == checkAgainst.get_isGroup()
                        && check.get_id() == checkAgainst.get_id()
                        && check.get_type() == checkAgainst.get_type()) {
                    if(verbose){System.out.println("<conflict found>");}
                    if(verbose){check.print();}
                    if(verbose){checkAgainst.print();}
                    if(verbose){System.out.println("</conflict found>");}
                    return true;
                }
            }
        }

       
        return false;
    }

   
}
