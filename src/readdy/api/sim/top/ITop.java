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
package readdy.api.sim.top;

import readdy.api.sim.core.ICore;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.sim.top.rkHandle.IReactionHandler;

/**
 *
 * @author schoeneberg
 */
public interface ITop {

    void set_GlobalParameters(IGlobalParameters globalParameters);

    /**
     * Here the simulation core is set. 
     * It can perform the particle dynamics on its own.
     * Reactions are checked on the particle level and occurring reactions are
     * not handled but are reported to the Top level.
     * Here the reported reactions are executed.
     * It is also in the Top level where it is determined, if the reported
     * particle level reaction was actually a reaction between groups of
     * particles. All reactions are handled in the Top and the results reported
     * back to the Core.
     * @param core
     */
    void set_Core(ICore core);

    void set_ReactionHandler(IReactionHandler reactionHandler);

    /**
     * this method runs the simulation as it is specified in the global
     * parameters files and the other input files like particle parameters,
     * initial topology and so on.
     * For running the simulation, this method calls the core simulation method
     * and adds the reaction execution on top of it (the reason for the name
     * of the class).
     * All together, this method does the whole job of reaction diffusion dynamics
     */
    void runSimulation();
}
