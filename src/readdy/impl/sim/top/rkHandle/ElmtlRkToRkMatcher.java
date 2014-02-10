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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.group.IGroup;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import readdy.impl.sim.top.group.ExtendedIdAndType;

/**
 *
 * @author schoeneberg
 */
public class ElmtlRkToRkMatcher implements IElmtlRkToRkMatcher {
    boolean verbose = false;
    
    IReactionParameters reactionParameters;
    IGroupConfiguration groupConfiguration;
    Random rand = new Random();

    public void setGroupConfiguration(IGroupConfiguration groupConfiguration) {
        this.groupConfiguration = groupConfiguration;
    }

    public void setReactionParameters(IReactionParameters reactionParameters) {
        this.reactionParameters = reactionParameters;
    }

    public ArrayList<IExecutableReaction> matchReactions(ArrayList<IOccurredElementalReaction> occElmtlRkList) {
        ArrayList<IExecutableReaction> executableReactionsList = new ArrayList();
        for (IOccurredElementalReaction occElmtlRk : occElmtlRkList) {
            IExecutableReaction executableReaction = match(occElmtlRk);
            executableReactionsList.add(executableReaction);
        }
        return executableReactionsList;
    }
    
    public IExecutableReaction matchReaction(IOccurredElementalReaction occElmtlRk) {
        return match(occElmtlRk);
    }

    private IExecutableReaction match(IOccurredElementalReaction occElmtlRk) {
        int elmtlRkId = occElmtlRk.get_elmtlRkId();
        int rkId = reactionParameters.getCorrespondingTopLevelReactionId(elmtlRkId);
        int rkTypeId = reactionParameters.getReactionTypeId(rkId);

        // the matchedEducts consist of a particleId and a extendedIdAndType which
        // resembles either a particle or a particle group to which it has been matched
        HashMap<IParticle, IExtendedIdAndType> matchedEducts = matchReactionEducts(rkId, occElmtlRk);
        ArrayList<IExtendedIdAndType> matchedProducts = matchReactionProducts(rkId);

        IExecutableReaction executableReaction = new ExecutableReaction(
                rkId,
                rkTypeId,
                elmtlRkId,
                matchedEducts,
                matchedProducts);
        return executableReaction;
    }

    /**
     * returns an hash map particleId->matched particle or group
     * @param involvedParticles
     * @param rkId
     * @return
     */
    private HashMap<IParticle, IExtendedIdAndType> matchReactionEducts(int rkId, IOccurredElementalReaction occElmtlRk) {

        IParticle[] involvedParticles = occElmtlRk.get_involvedParticles();
        HashMap<IParticle, IExtendedIdAndType> matches = new HashMap();

        IExtendedIdAndType[] topLevelEductsToMatchArray = reactionParameters.getEducts(rkId);

        
        // test if the topLevelComponents are in fact particles or groups
        // if they are particles, we have nothing to check and the matching is done
        // if they are groups, we have to ensure that the particle is member of the requested group
        switch (topLevelEductsToMatchArray.length) {
            case 0:
                if (involvedParticles.length == 0) {
                    // everything fine, creation from nothing
                } else {
                    printStatus(involvedParticles, topLevelEductsToMatchArray);
                    throw new RuntimeException("involvedParticles given but no educts to match.");
                }
                break;
            case 1:
                IExtendedIdAndType topLevelEductToMatch = topLevelEductsToMatchArray[0];
                IParticle involvedParticle = involvedParticles[0];
                switch (involvedParticles.length) {
                    case 0:
                        printStatus(involvedParticles, topLevelEductsToMatchArray);
                        throw new RuntimeException("no involved particles but matched educts. Fail.");
                    case 1:
                        // one involved particle, one educt to match - standard
                        if (topLevelEductToMatch.get_isGroup() == false) {
                            // the eductToMatch is a particle...
                            if (involvedParticle.get_type() == topLevelEductToMatch.get_type()) {
                                matches.put(involvedParticle, new ExtendedIdAndType(involvedParticle));
                            } else {
                                if(verbose){System.out.println("<match not successfull:>");};
                                if(verbose){System.out.println("involvedParticle 1");};
                                if(verbose){involvedParticle.print();};
                                if(verbose){System.out.println("top LevelEductToMatch 1");};
                                if(verbose){topLevelEductToMatch.print();};
                                if(verbose){System.out.println("</match not successfull:>");};
                            }
                        } else {
                            // the eductToMatch is a group... find the correct one where the particle is involved in
                            int groupTypeToSearch = topLevelEductToMatch.get_type();
                            ArrayList<IExtendedIdAndType> matchingGroups = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedParticle.get_id(), groupTypeToSearch);
                            if (!matchingGroups.isEmpty()) {
                                IExtendedIdAndType matchingGroup = decideWhichGroupToTake(rkId, occElmtlRk, matchingGroups);
                                int matchingGroupId = matchingGroup.get_id();
                                IGroup matchedGroup = groupConfiguration.getGroup(matchingGroupId);
                                matches.put(involvedParticle, new ExtendedIdAndType(matchedGroup));
                            } else {
                                throw new RuntimeException("no match could be found case 1, 1");
                            }
                        }
                        break;
                    case 2:
                        // two involved particle, only one educt to match... only possible for a group
                        IParticle involvedP1 = involvedParticles[0];
                        IParticle involvedP2 = involvedParticles[1];
                        if (topLevelEductToMatch.get_isGroup() == true) {
                            // search for the one group that matches both particles
                            int groupTypeToSearch = topLevelEductToMatch.get_type();
                            ArrayList<IExtendedIdAndType> matchingGroups1 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP1.get_id(), groupTypeToSearch);
                            ArrayList<IExtendedIdAndType> matchingGroups2 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP2.get_id(), groupTypeToSearch);
                            ArrayList<IExtendedIdAndType> matchingGroups = mergeTwoMatchingGroups(matchingGroups1, matchingGroups2);
                            if (!matchingGroups.isEmpty()) {
                                IExtendedIdAndType matchingGroup = decideWhichGroupToTake(rkId, occElmtlRk, matchingGroups);
                                int matchingGroupId = matchingGroup.get_id();
                                IGroup matchedGroup = groupConfiguration.getGroup(matchingGroupId);
                                matches.put(involvedP1, new ExtendedIdAndType(matchedGroup));
                                matches.put(involvedP2, new ExtendedIdAndType(matchedGroup));
                            } else {
                                throw new RuntimeException("no match could be found case 1, 2");
                            }
                        } else {
                            printStatus(involvedParticles, topLevelEductsToMatchArray);
                            throw new RuntimeException("two involved particles and the match educt is not a group doesnt work. fail.");
                        }
                        break;
                    default:
                        printStatus(involvedParticles, topLevelEductsToMatchArray);
                        throw new RuntimeException("# involvedParticles is not in {0,1,2} but is '" + involvedParticles.length + "'. Fail.");
                }
                break;
            case 2:
                IExtendedIdAndType topLevelEductToMatch1 = topLevelEductsToMatchArray[0];
                IExtendedIdAndType topLevelEductToMatch2 = topLevelEductsToMatchArray[1];

                switch (involvedParticles.length) {

                    case 2:


                        // two involved particle, two educts to match - standard
                        IParticle involvedP1 = involvedParticles[0];
                        IParticle involvedP2 = involvedParticles[1];

                        //-----------------------------------------------------------------------------------------------
                        // both topLevelEducts are particles
                        if (topLevelEductToMatch1.get_isGroup() == false && topLevelEductToMatch2.get_isGroup() == false) {
                            // the eductToMatch is a particle... nothing to be done
                            if (involvedP1.get_type() == topLevelEductToMatch1.get_type()
                                    && involvedP2.get_type() == topLevelEductToMatch2.get_type()) {
                                matches.put(involvedP1, new ExtendedIdAndType(involvedP1));
                                matches.put(involvedP2, new ExtendedIdAndType(involvedP2));
                            } else {
                                if ((involvedP1.get_type() == topLevelEductToMatch2.get_type()
                                        && involvedP2.get_type() == topLevelEductToMatch1.get_type())) {
                                    matches.put(involvedP1, new ExtendedIdAndType(involvedP2));
                                    matches.put(involvedP2, new ExtendedIdAndType(involvedP1));
                                } else {
                                    if(verbose){System.out.println("<match not successfull:>");};
                                    if(verbose){System.out.println("involvedParticle 1");};
                                    if(verbose){involvedP1.print();};
                                    if(verbose){System.out.println("top LevelEductToMatch 1");};
                                    if(verbose){topLevelEductToMatch1.print();};
                                    if(verbose){System.out.println("involvedParticle 2");};
                                    if(verbose){involvedP2.print();};
                                    if(verbose){System.out.println("top LevelEductToMatch 2");};
                                    if(verbose){topLevelEductToMatch2.print();};
                                    if(verbose){System.out.println("</match not successfull:>");};
                                }
                            }
                        }


                        //-----------------------------------------------------------------------------------------------
                        if (topLevelEductToMatch1.get_isGroup() == true && topLevelEductToMatch2.get_isGroup() == false) {
                            // TODO
                            throw new UnsupportedOperationException("Not yet implemented");
                        }

                        //-----------------------------------------------------------------------------------------------
                        if (topLevelEductToMatch1.get_isGroup() == false && topLevelEductToMatch2.get_isGroup() == true) {
                            // TODO
                            throw new UnsupportedOperationException("Not yet implemented");
                        }

                        //-----------------------------------------------------------------------------------------------
                        // both topLevel Educts are groups
                        if (topLevelEductToMatch1.get_isGroup() == true && topLevelEductToMatch2.get_isGroup() == true) {
                            boolean directMatchingWorked = false,
                                    swappedMatchingWorked = false;
                            int groupTypeToSearch1 = topLevelEductToMatch1.get_type();
                            int groupTypeToSearch2 = topLevelEductToMatch2.get_type();
                            // search one way:

                            ArrayList<IExtendedIdAndType> matchingGroups11 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP1.get_id(), groupTypeToSearch1);
                            ArrayList<IExtendedIdAndType> matchingGroups22 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP2.get_id(), groupTypeToSearch2);
                            ArrayList<IExtendedIdAndType> matchingGroupPairDirect = decideWhichGroupToTakeForBothArrays(rkId, occElmtlRk, matchingGroups11, matchingGroups22);
                            if (matchingGroupPairDirect.size() == 2) {
                                directMatchingWorked = true;
                            }

                            // search the other way
                            ArrayList<IExtendedIdAndType> matchingGroups12 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP1.get_id(), groupTypeToSearch2);
                            ArrayList<IExtendedIdAndType> matchingGroups21 = groupConfiguration.getGroupsWhereParticleIsInvolved(involvedP2.get_id(), groupTypeToSearch1);
                            ArrayList<IExtendedIdAndType> matchingGroupPairSwapped = decideWhichGroupToTakeForBothArrays(rkId, occElmtlRk, matchingGroups12, matchingGroups21);
                            if (matchingGroupPairSwapped.size() == 2) {
                                swappedMatchingWorked = true;
                            }

                            int matchingGroupId1,
                                    matchingGroupId2;
                            if (directMatchingWorked && swappedMatchingWorked) {
                                // decide very cruedly which group pair to take...
                                if (rand.nextDouble() > 0.5) {
                                    matchingGroupId1 = matchingGroupPairDirect.get(0).get_id();
                                    matchingGroupId2 = matchingGroupPairDirect.get(1).get_id();
                                } else {
                                    matchingGroupId1 = matchingGroupPairSwapped.get(0).get_id();
                                    matchingGroupId2 = matchingGroupPairSwapped.get(1).get_id();
                                }


                            } else {
                                if (directMatchingWorked) {
                                    matchingGroupId1 = matchingGroupPairDirect.get(0).get_id();
                                    matchingGroupId2 = matchingGroupPairDirect.get(1).get_id();
                                } else {
                                    if (swappedMatchingWorked) {
                                        matchingGroupId1 = matchingGroupPairSwapped.get(0).get_id();
                                        matchingGroupId2 = matchingGroupPairSwapped.get(1).get_id();

                                    } else {
                                        throw new RuntimeException("no matching worked in either way.");
                                    }
                                }
                            }

                            // swap has already been done during group search that would match
                            IGroup matchedGroup1 = groupConfiguration.getGroup(matchingGroupId1);
                            matches.put(involvedP1, new ExtendedIdAndType(matchedGroup1));
                            IGroup matchedGroup2 = groupConfiguration.getGroup(matchingGroupId2);
                            matches.put(involvedP2, new ExtendedIdAndType(matchedGroup2));
                        }
                        break;
                    default:
                        printStatus(involvedParticles, topLevelEductsToMatchArray);
                        throw new RuntimeException("# involvedParticles is not in {2} but is '" + involvedParticles.length + "'. Fail.");
                }

                break;

            default:
                printStatus(involvedParticles, topLevelEductsToMatchArray);
                throw new RuntimeException("# topLevelEductsToMatchArray is not in {0,1,2} but is '" + topLevelEductsToMatchArray.length + "'. Fail.");
        }





        return matches;
    }

    private ArrayList<IExtendedIdAndType> matchReactionProducts(int rkId) {
        ArrayList<IExtendedIdAndType> matchedProducts = new ArrayList();
        IExtendedIdAndType[] products = reactionParameters.getProducts(rkId);
        matchedProducts.addAll(Arrays.asList(products));
        return matchedProducts;


    }

    private void printStatus(IParticle[] involvedParticles, IExtendedIdAndType[] topLevelEductsToMatchArray) {
        System.out.println("involvedParticles:");


        for (IParticle p : involvedParticles) {
            p.print();


        }
        System.out.println("topLevelEductsToMatchArray:");


        for (IExtendedIdAndType e : topLevelEductsToMatchArray) {
            e.print();


        }

    }

    private ArrayList<IExtendedIdAndType> mergeTwoMatchingGroups(ArrayList<IExtendedIdAndType> matchingGroups1, ArrayList<IExtendedIdAndType> matchingGroups2) {
        

        if (verbose) {
            System.out.println("<mergeTwoMatchingGroups()>");
        }
        
        ArrayList<IExtendedIdAndType> list = new ArrayList();

        for (IExtendedIdAndType e1 : matchingGroups1) {
            for (IExtendedIdAndType e2 : matchingGroups2) {
                if (verbose) {
                    System.out.println("compare...:");
                    e1.print();
                    e2.print();
                }

                if (e1.equals(e2)) {
                    list.add(e1);
                }
            }
        }
        if (verbose) {
            System.out.println("</mergeTwoMatchingGroups()>");
        }
        return list;
    }

    private IExtendedIdAndType decideWhichGroupToTake(int rkId, IOccurredElementalReaction occElmtlRk, ArrayList<IExtendedIdAndType> matchingGroups) {
        

        if (verbose) {
            System.out.println("decide which group to take...:");
            System.out.println("decision between:");
            for (IExtendedIdAndType e : matchingGroups) {
                e.print();
            }
        }
        if (!matchingGroups.isEmpty()) {
            int nMatches = matchingGroups.size();


            double share = 1 / (double) nMatches;
            double decision = rand.nextDouble();


            if (verbose) {
                System.out.println("nMatches " + nMatches + " share " + share + " decision: " + decision);
            }
            for (int i = 0; i
                    < nMatches; i++) {
                if (decision < (i + 1) * share) {
                    IExtendedIdAndType decidedGroup = matchingGroups.get(i);
                    if (verbose) {
                        System.out.println("decision was:");
                        decidedGroup.print();
                    }
                    return decidedGroup;
                }
            }
        }
        throw new RuntimeException("this line of code should be never reached.");
    }

    private ArrayList<IExtendedIdAndType> decideWhichGroupToTakeForBothArrays(int rkId, IOccurredElementalReaction occElmtlRk, ArrayList<IExtendedIdAndType> matchingGroups1, ArrayList<IExtendedIdAndType> matchingGroups2) {
        ArrayList<IExtendedIdAndType> resultPair = new ArrayList();


        if (!matchingGroups1.isEmpty()) {
            resultPair.add(decideWhichGroupToTake(rkId, occElmtlRk, matchingGroups1));
        }
        if (!matchingGroups2.isEmpty()) {
            resultPair.add(decideWhichGroupToTake(rkId, occElmtlRk, matchingGroups2));
        }
        return resultPair;
    }

    
}
