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
public class StringTools {

    //##################################################################
    // String Array Tools
    //##################################################################
    /**
     * splits an array string of the form
     * [a,b,c]
     * into a matrix of strings
     * @param s
     * @return
     */
    public static String[] splitArrayString(String s) {
        s=stripWhitespaces(s);
        if (s.length() != 0) {
            s = s.replaceAll("\\[", "");
            s = s.replaceAll("\\]", "");
            String[] firstLevel = s.split(",");
            return firstLevel;
        } else {
            return new String[]{};
        }
    }

    public static double[] splitArrayString_convertToDouble(String s) {
        s=stripWhitespaces(s);
        String[] sArr = splitArrayString(s);
        double[] dArr = new double[sArr.length];
        for (int i = 0; i < dArr.length; i++) {
            dArr[i] = Double.parseDouble(sArr[i]);

        }

        return dArr;
    }

    public static int[] splitArrayString_convertToInt(String s) {
        s=stripWhitespaces(s);

        String[] sArr = splitArrayString(s);
        int[] iArr = new int[sArr.length];
        for (int i = 0; i < iArr.length; i++) {
            iArr[i] = Integer.parseInt(sArr[i]);

        }

        return iArr;
    }

    //##################################################################
    // String Matrix Tools
    //##################################################################
    /**
     * splits a matrix string of the form
     * [[a,b];[c,d];[e,f]]
     * into a matrix of strings
     * @param s
     * @return
     */
    public static String[][] splitMatrixString(String s) {
        s=stripWhitespaces(s);
        if (s.length() != 0) {
            s = s.replaceAll("\\[", "");
            s = s.replaceAll("\\]", "");
            String[] firstLevel = s.split(";");
            String[][] matrix = new String[firstLevel.length][];
            int i = 0;
            for (String ss : firstLevel) {
                String[] scndLevel = ss.split(",");
                matrix[i] = scndLevel;
                i++;
            }
            return matrix;
        } else {
            return new String[][]{};
        }
    }

    public static double[][] splitMatrixString_convertToDouble(String s) {
        s=stripWhitespaces(s);
        String[][] sMatrix = StringTools.splitMatrixString(s);
        double[][] dMatrix = new double[sMatrix.length][];
        for (int i = 0; i < sMatrix.length; i++) {
            double[] d = new double[sMatrix[i].length];
            for (int j = 0; j < d.length; j++) {
                d[j] = Double.parseDouble(sMatrix[i][j]);
            }
            dMatrix[i] = d;
        }
        return dMatrix;
    }

    public static int[][] splitMatrixString_convertToInt(String s) {
        s=stripWhitespaces(s);
        String[][] sMatrix = StringTools.splitMatrixString(s);
        int[][] iMatrix = new int[sMatrix.length][];
        for (int i = 0; i < sMatrix.length; i++) {
            int[] iRow = new int[sMatrix[i].length];
            for (int j = 0; j < iRow.length; j++) {
                iRow[j] = Integer.parseInt(sMatrix[i][j]);
            }
            iMatrix[i] = iRow;
        }
        return iMatrix;
    }

    private static String stripWhitespaces(String s) {
        return s.replaceAll("\\s", "");
    }
}
