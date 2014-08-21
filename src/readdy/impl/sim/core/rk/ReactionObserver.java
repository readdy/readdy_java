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
package readdy.impl.sim.core.rk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.rk.IElementalReactionManager;
import readdy.api.sim.core.rk.IElementalReactionToCheck;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.core.rk.IReactionObserver;
import readdy.impl.tools.AdvancedSystemOut;

/**
 *
 * @author schoeneberg
 */
public class ReactionObserver implements IReactionObserver {
    
    boolean outputHittingEvents = false;

    IElementalReactionManager elementalReactionManger = null;
    IParticleParameters particleParameters = null;
    Random rand = new Random();

    public void set_elementalReactionManager(IElementalReactionManager elementalReactionManger) {
        this.elementalReactionManger = elementalReactionManger;
    }

    public void set_particleParameters(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;
    }

    public ArrayList<IOccurredElementalReaction> checkSpontaneous(int stepId) {
        ArrayList<IOccurredElementalReaction> list = new ArrayList();
        Iterator<IElementalReactionToCheck> rkIter = elementalReactionManger.getElmtlReactions();
        while (rkIter.hasNext()) {
            IElementalReactionToCheck elmtlRkToCheck = rkIter.next();

            boolean decision = decideUponReactOrNotToReact(elmtlRkToCheck);
            if (decision == true) {
                IOccurredElementalReaction occElmtlRk =
                        new OccurredElementalReaction(elmtlRkToCheck.get_elmtlRkId(), new IParticle[]{});
                list.add(occElmtlRk);
            }

        }
        return list;
    }

    public ArrayList<IOccurredElementalReaction> checkSingle(int stepId, IParticle p) {
        ArrayList<IOccurredElementalReaction> list = new ArrayList();

        Iterator<IElementalReactionToCheck> rkIter = elementalReactionManger.getElmtlReactions(p);
        while (rkIter.hasNext()) {
            IElementalReactionToCheck elmtlRkToCheck = rkIter.next();

            boolean decision = decideUponReactOrNotToReact(elmtlRkToCheck);
            if (decision == true) {
                IOccurredElementalReaction occElmtlRk =
                        new OccurredElementalReaction(elmtlRkToCheck.get_elmtlRkId(), new IParticle[]{p});
                list.add(occElmtlRk);
            }

        }
        return list;
    }

    public ArrayList<IOccurredElementalReaction> checkPair(int stepId, IParticle p1, IParticle p2, double dist) {
        ArrayList<IOccurredElementalReaction> list = new ArrayList();
        Iterator<IElementalReactionToCheck> rkIter = elementalReactionManger.getElmtlReactions(p1, p2);
        while (rkIter.hasNext()) {
            IElementalReactionToCheck elmtlRkToCheck = rkIter.next();
            if (dist < particleParameters.get_maxPInteractionRadius(p1.get_type(), p2.get_type())) {
                if((p1.get_type()==1 && p2.get_type()==3)
                        ||
                    (p1.get_type()==3 && p2.get_type()==1)){
                    System.out.print("hittingEvent_meta2_G:stepId="+stepId+":");
                    AdvancedSystemOut.print("p0="+p1.get_id()+";"+p1.get_type()+";", p1.get_coords(),":");
                    AdvancedSystemOut.print("p1="+p2.get_id()+";"+p2.get_type()+";", p2.get_coords(),"");
                    System.out.println("");
                
                }
                
                if(outputHittingEvents){
                    System.out.println("hit: step="+stepId+" p0id="+p1.get_id()+" p0type="+p1.get_type()+" p1id="+p2.get_id()+" p1type="+p2.get_type());
                }
                
                boolean decision = decideUponReactOrNotToReact(elmtlRkToCheck);
                if (decision == true) {
                    IOccurredElementalReaction occElmtlRk =
                            new OccurredElementalReaction(elmtlRkToCheck.get_elmtlRkId(), new IParticle[]{p1, p2});
                    list.add(occElmtlRk);
                }
            }
        }
        return list;
    }

    private boolean decideUponReactOrNotToReact(IElementalReactionToCheck elmtlRkToCheck) {

        return (elmtlRkToCheck.get_p() > rand.nextDouble());
    }
}
