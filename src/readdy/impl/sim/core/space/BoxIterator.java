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
package readdy.impl.sim.core.space;

import java.util.Iterator;
import readdy.impl.tools.AdvancedSystemOut;
import statlab.base.datatypes.IIntIterator;
import statlab.base.util.Counter;
import statlab.base.util.IntArrays;
import statlab.base.util.MultiIndex;

/**
 *
 * @author johannesschoeneberg
 *
 * an iterator consists of an advancePointer() method and a
 * getCurrentPointerValue() method. Usually they are called prepareNext() and
 * next() in an iterator context
 *
 */
public class BoxIterator implements IIntIterator {

    private Box[] grid;  // each grid cell knows its particle IDs
    private int dim; // dimension
    private MultiIndex mindex; // maps box ids to box indexes
    // this is the starting box index from which this BoxIterator starts
    private int[] box_index_0 = null;
    // these are the running pointers to the current box and the 
    // current particle id iterator within the current box
    private int[] current_box_index = null;
    private Iterator<Integer> current_boxEntryIterator = null;
    private int currentEntry = -1;
    private Counter counter = null;
    private boolean hasNext = false;
    private int next = -1;
    // maybe we want to exclude one entry, e.g. the particle id from which we
    // called the neighbor search
    private boolean excludeEntry = false;
    private int excludedEntry = -1;

    public BoxIterator(Box[] _grid, int _dim, MultiIndex _mindex, Box box) {
        this.grid = _grid;
        this.dim = _dim;
        this.mindex = _mindex;


        // assuming we started the iterator from box index 0,0,0
        // set initial index to -1, -1, -1 
        this.box_index_0 = IntArrays.copy(box.getIndex());
        for (int i = 0; i < this.box_index_0.length; i++) {
            this.box_index_0[i]--;
        }
        
        reset();


    }

    private void reset() {
        // this is the pointer to the current box
        this.current_box_index = IntArrays.copy(box_index_0);

        // have 3 increments in each dimension
        int[] ncount = new int[dim];
        java.util.Arrays.fill(ncount, 3);
        counter = new Counter(ncount);

        current_boxEntryIterator = null;
        currentEntry = -1;


        hasNext = false;
        next = -1;


        excludeEntry = false;
        excludedEntry = -1;
    }

    public void initialize(int excl) {
        reset();
        excludeEntry = true;
        excludedEntry = excl;
        hasNext = prepareNext();
    }

    public void initialize() {
        reset();
        hasNext = prepareNext();
    }

    /**
     * returns the hasNext value
     *
     * @return
     */
    private boolean prepareNext() {

        // at the initialization, we are already in the first box. Now check
        // if there are entries in it and check them
        do {
            while (jumpToNextBoxEntry()) {    // if the current box has no more entries, this returns false
                // set the pointer forward by the jumpToNextBoxEntryFunction
                // and set it to currentEntry
                // now validate, if the currentEntry is ok.
                if (isBoxEntryValid(currentEntry)) {
                    next = currentEntry;
                    return true;	// exits both while loops if we have found something
                }

            }


        } while (jumpToNextBox()); // if there are no more boxes to look for entries in, this returns false


        return false;

    }

    /**
     * given the current box, jump to the next boxEntry and load it to the
     * currentEntry field.
     *
     * @return
     */
    private boolean jumpToNextBoxEntry() {
        // the box entries are based on an iterator themselves
        if (current_boxEntryIterator == null) {
            int gridIndex = mindex.index2id(current_box_index);
            if (gridIndex < 0 || gridIndex >= grid.length) {
                return (false);
            }
            Box b = grid[gridIndex];
            current_boxEntryIterator = b.getList().listIterator();
        }

        if (current_boxEntryIterator.hasNext()) {
            currentEntry = current_boxEntryIterator.next();
            return (true);
        } else {
            current_boxEntryIterator = null;
        }

        return (false);
    }

    private boolean jumpToNextBox() {
        // position grid index on an exisiting box
        do {
            boolean incrementWasPossible = counter.inc();
            if (!incrementWasPossible) {
                // we incremented all that was possible
                return (false);
            }
            // generate next index, do that until it is valid (mindex.isValid())
            updateIndex();
        } while (!mindex.isValid(current_box_index));
        return (true);
    }

    private void updateIndex() {
        for (int i = 0; i < current_box_index.length; i++) {
            current_box_index[i] = box_index_0[i] + counter.get()[i];
        }
    }

    private boolean isBoxEntryValid(int entry) {
        if (excludeEntry) {
            if (entry == excludedEntry) {
                return false;
            }
        }
        return true;
    }

    public boolean hasNext() {
        return (hasNext);
    }

    /**
     * return the current value, the iterator points to and advance the pointer
     * by one step.
     *
     * @return
     */
    public int next() {
        int res = next;
        hasNext = prepareNext();
        return (res);
    }

    public void printStatus() {
        AdvancedSystemOut.println("box_index_0: ", box_index_0, "");
        AdvancedSystemOut.println("current_box_index: ", current_box_index, "");
        System.out.println("hasNext: " + hasNext);
        System.out.println("next: " + next);
    }
}
