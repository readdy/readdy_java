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
package readdy.impl.tools;

/**
 *
 * @author schoeneberg
 */
public class AdvancedSystemErr {

    public static void print(String prefix, double[] arr, String suffix) {
        System.err.print(prefix);

        for (int i = 0; i < arr.length; i++) {
            if (i == arr.length - 1) {
                System.err.print(" " + arr[i]);

            } else {
                System.err.print(" " + arr[i] + ",");
            }

        }

        System.err.print(suffix);
    }

    public static void println(String prefix, double[] arr, String suffix) {
        print(prefix, arr, suffix);
        System.err.println();
    }

    public static void print(String prefix, int[] arr, String suffix) {
        System.err.print(prefix);

        for (int i = 0; i < arr.length; i++) {
            if (i == arr.length - 1) {
                System.err.print(" " + arr[i]);

            } else {
                System.err.print(" " + arr[i] + ",");
            }

        }

        System.err.print(suffix);
    }

    public static void println(String prefix, int[] arr, String suffix) {
        print(prefix, arr, suffix);
        System.err.println();
    }

    public static void print(String prefix, int[][] matrix, String suffix) {
        System.err.print(prefix);

        System.err.print("[");
        for (int i = 0; i < matrix.length; i++) {
            int[] row = matrix[i];
            if (i == matrix.length - 1) {
                print("[", row, "]");
            } else {
                print("[", row, "]");
                System.err.print(",");
            }

        }
        System.err.print("]");

        System.err.print(suffix);
    }

    public static void println(String prefix, int[][] matrix, String suffix) {
        print(prefix, matrix, suffix);
        System.err.println();
    }

    public static void print(String prefix, String[] arr, String suffix) {
        System.err.print(prefix);

        for (int i = 0; i < arr.length; i++) {
            if (i == arr.length - 1) {
                System.err.print(" " + arr[i]);

            } else {
                System.err.print(" " + arr[i] + ",");
            }

        }

        System.err.print(suffix);
    }

    public static void println(String prefix, String[] arr, String suffix) {
        print(prefix, arr, suffix);
        System.err.println();
    }
}
