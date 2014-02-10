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

import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author franknoe
 */
public class Box {
    

    LinkedList<Integer> list;
    int[] index;

    public void printIndex() {
        System.out.print(index[0] + " " + index[1] + " " + index[2]);
    }

    public Box(int[] _index) {
        this.list = new LinkedList<Integer>();
        this.index = _index;
    }

    public int[] getIndex() {
        return (index);
    }

    public LinkedList<Integer> getList() {
        return (list);
    }

    public void add(int id) {
        list.add(id);
    }

    /**
     * Removes the particle id from the given box. Attention: This is O(N). It may
     * be possible to implement this more efficient by saving not only the box but
     * also the box-particle object each particle is associated to and to then
     * remove this directly without iteration.
     * @param box
     * @param id
     */
    public void remove(int id) {
        ListIterator<Integer> iterator = list.listIterator();

        while (iterator.hasNext()) {
            int ido = iterator.next();

            if (id == ido) {


                iterator.remove();
                return;
            }
        }

        throw (new RuntimeException("Could not find id " + id + " in the current box"));
    }


}
