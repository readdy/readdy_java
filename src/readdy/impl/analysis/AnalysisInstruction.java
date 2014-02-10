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
package readdy.impl.analysis;

import readdy.api.analysis.IAnalysisInstruction;

/**
 *
 * @author schoeneberg
 */
public class AnalysisInstruction implements IAnalysisInstruction {

    String method, outputFileName, outputFormat;
    String[] specialFlags;
    int everyXStep;

    public void setEveryXStep(int everyXStep) {
        this.everyXStep = everyXStep;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void setSpecialFlags(String[] specialFlags) {
        this.specialFlags = specialFlags;
    }

    public String get_method() {
        return this.method;
    }

    public int get_everyXStep() {
        return this.everyXStep;
    }

    public String get_outputFile() {
        return this.outputFileName;
    }

    public String get_outputFormat() {
        return this.outputFormat;
    }

    public String[] get_specialFlags() {
        return this.specialFlags;
    }

    public void print() {
        System.out.println("<analysisInstruction>");
        System.out.println("method: " + method);
        System.out.println("outputFileName: " + outputFileName);
        System.out.println("outputFormat: " + outputFormat);
        System.out.println("everyXStep: " + everyXStep);
        System.out.print("specialFlags: ");
        for (String specialFlag : specialFlags) {
            System.out.print(specialFlag + ",");
        }
        System.out.println();
        System.out.println("</analysisInstruction>");
    }
}
