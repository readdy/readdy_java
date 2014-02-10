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
package readdy.impl.sim.core.pot;

import java.util.HashMap;
import java.util.Set;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.potentials.IPotential;


/**
 *
 * The Potential Inventory knows about all potential objects and their parameters.
 * The information about which particles are assigned to which 
 * potential reside in the potentialManager.
 * 
 * @author schoeneberg
 */
public class PotentialInventory implements IPotentialInventory {

    IPotentialFactory potentialFactory;
    private HashMap<Integer, IPotential> potId_toPotential_map = new HashMap();
    private HashMap<Integer, Integer> potId_to_potOrder_map = new HashMap();
    public static int currentHighestPotId = -1;
    

    public void setPotentialFactory(IPotentialFactory potentialFactory) {
        this.potentialFactory = potentialFactory;
    }


    public boolean doesPotentialExist(int potId) {
        return (potId_to_potOrder_map.keySet().contains(potId));
    }

    public IPotential getPotential(int potId) {
        return potId_toPotential_map.get(potId);
    }

    public int getPotentialOrder(int potId) {
        return potId_to_potOrder_map.get(potId);
    }

    private int getNextPotentialId() {
        currentHighestPotId++;
        return currentHighestPotId;
    }

       public int createPotential(HashMap<String, String> parameters) {
        if (potentialFactory != null) {
            
                int newPotentialId = getNextPotentialId();            
                IPotential newPotential = potentialFactory.createPotential(newPotentialId, parameters);
                int potOrder = newPotential.get_order();
                potId_toPotential_map.put(newPotentialId,newPotential);
                potId_to_potOrder_map.put(newPotentialId,potOrder);
                return newPotentialId;

        } else {
            throw new RuntimeException("potentialFactory offline. Abort.");
        }
    }
    
    public Set<Integer> getPotentialIds() {
        return potId_toPotential_map.keySet();
    }
}
