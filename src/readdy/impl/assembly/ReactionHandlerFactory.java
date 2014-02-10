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
package readdy.impl.assembly;

import readdy.api.assembly.IReactionHandlerFactory;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.api.sim.top.rkHandle.IReactionConflictResolver;
import readdy.api.sim.top.rkHandle.IReactionHandler;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.impl.sim.top.rkHandle.ReactionHandler;

/**
 *
 * @author schoeneberg
 */
public class ReactionHandlerFactory implements IReactionHandlerFactory {

    IElmtlRkToRkMatcher elmtlRkToRkMatcher;
    IReactionValidator reactionValidator;
    IReactionConflictResolver reactionConflictResolver;
    IReactionManager reactionManager;

    public IReactionHandler createReactionHandler() {
        if (allInputPresent()) {
            ReactionHandler reactionHandler = new ReactionHandler();
            reactionHandler.setElmtlRkToRkMatcher(elmtlRkToRkMatcher);
            reactionHandler.setReactionConflictResolver(reactionConflictResolver);
            reactionHandler.setReactionManager(reactionManager);
            reactionHandler.setReactionValidator(reactionValidator);
            return reactionHandler;
        } else {
            throw new RuntimeException("not all input present.");
        }
    }

    public void set_elmtlRkToRkMatcher(IElmtlRkToRkMatcher elmtlRkToRkMatcher) {
        this.elmtlRkToRkMatcher = elmtlRkToRkMatcher;
    }

    public void set_reactionValidator(IReactionValidator reactionValidator) {
        this.reactionValidator = reactionValidator;
    }

    public void set_reactionConflictResolver(IReactionConflictResolver reactionConflictResolver) {
        this.reactionConflictResolver = reactionConflictResolver;
    }

    public void set_reactionManager(IReactionManager reactionManager) {
        this.reactionManager = reactionManager;
    }

    private boolean allInputPresent() {
        return elmtlRkToRkMatcher != null
                && reactionValidator != null
                && reactionConflictResolver != null
                && reactionManager != null;
    }
}
