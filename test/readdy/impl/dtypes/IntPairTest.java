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
package readdy.impl.dtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import readdy.api.dtypes.IIntPair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author schoeneberg
 */
public class IntPairTest {

    public IntPairTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    /**
     * Test of equals method, of class IntPair.
     */
    static HashMap<Integer, HashSet<Integer>> references;

    @Test
    public void test() {
        references = new HashMap();
        ArrayList<IIntPair> pairList = new ArrayList();

        IIntPair AB = new IntPair(0, 1);
        IIntPair AD = new IntPair(0, 3);
        IIntPair AC = new IntPair(0, 2);
        IIntPair BD = new IntPair(1, 3);
        IIntPair BC = new IntPair(1, 2);
        IIntPair[] pairArray = new IIntPair[]{AB, AD, AC, BD, BC};
        for (int i = 0; i < pairArray.length; i++) {
            IIntPair pair = pairArray[i];
            int pos = pairList.size();
            addReferences(pair, pos);
            pairList.add(pos, pair);
        }


        printReferences();
        references.remove(0);
        System.out.println();
        printReferences();

        HashSet<IIntPair> pairHashSet = new HashSet();
        IIntPair A = new IntPair(0, 1);
        IIntPair B = new IntPair(1, 0);
        IIntPair C = new IntPair(1, 2);
        IIntPair[] testArr = new IIntPair[]{A, B, C};
        for (int i = 0; i < testArr.length; i++) {
            IIntPair iIntPair = testArr[i];
            pairHashSet.add(iIntPair);
        }

        System.out.println();
        printPairHashSet(pairHashSet);
        IIntPair Cpermuted = new IntPair(2, 1);
        pairHashSet.remove(Cpermuted);
        System.out.println("after removal");
        printPairHashSet(pairHashSet);


    }

    private void printReferences() {
        for (int id : references.keySet()) {
            System.out.print(id + "->");
            for (int pos : references.get(id)) {
                System.out.print(pos + ",");
            }
            System.out.println();
        }
    }

    private void addReferences(IIntPair A, int pos) {
        //particleId -> positions where it is involved map
        int[] ij = new int[]{A.get_i(), A.get_j()};
        for (int i = 0; i < ij.length; i++) {
            int id = ij[i];
            if (references.containsKey(id)) {
                references.get(id).add(pos);
            } else {
                HashSet<Integer> set = new HashSet();
                set.add(pos);
                references.put(id, set);
            }
        }

    }

    private void printPairHashSet(HashSet<IIntPair> pairHashSet) {
        for (IIntPair pair : pairHashSet) {
            System.out.println("ij+ " + pair.get_i() + "," + pair.get_j());
        }
    }
}
