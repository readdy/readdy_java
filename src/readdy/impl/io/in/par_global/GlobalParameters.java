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
package readdy.impl.io.in.par_global;

import java.util.ArrayList;
import readdy.api.analysis.IAnalysisInstruction;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 */
public class GlobalParameters implements IGlobalParameters {

    long nSimulationSteps;
    int outputEveryXSteps;
    double dt, T, dtOpenMM;
    int cudaDeviceIndex;
    double latticeBoxSize = -1;
    boolean latticeBoxSizeSet = false;
    double Kb = 0.0083144621; // Boltzmann constant in kJ/mol/K
    double[][] latticeBounds;
    String outputPath, outFileName;
    ArrayList<IAnalysisInstruction> analysisInstructions;

    public void set_nSimulationSteps(long n) {
        this.nSimulationSteps = n;
    }

    public void set_outputEveryXSteps(int m) {
        this.outputEveryXSteps = m;
    }

    public void set_cudaDeviceIndex(int cudadeviceIndex) {
        this.cudaDeviceIndex = cudadeviceIndex;
    }
    
    public void set_dt(double dt) {
        this.dt = dt;
    }
    
    public void set_dtOpenMM(double dtOpenMM) {
        this.dtOpenMM = dtOpenMM;
    }

    public void set_T(double T) {
        this.T = T;
    }


    public long get_nSimulationSteps() {
        return nSimulationSteps;
    }

    public int get_outputEveryXSteps() {
        return outputEveryXSteps;
    }

    public double get_dt() {
        return dt;
    }
    
    public double get_dtOpenMM() {
        return dtOpenMM;
    }

    public int get_cudaDeviceIndex() {
        return cudaDeviceIndex;
    }

    public double get_T() {
        return T;
    }

    public double get_Kb() {
        return Kb;
    }

    public void print() {
        System.out.println("globalParameters:  ");
        System.out.println("nSimulationSteps:  " + nSimulationSteps);
        System.out.println("outputEveryXSteps: " + outputEveryXSteps);
        System.out.println("dt:                " + dt);
        System.out.println("T:                 " + T);
        System.out.println("Kb:                " + Kb);
        System.out.println("minLatticeBoxSize: " + latticeBoxSize);
        System.out.print("latticeBounds:     ");
        System.out.print("[");
        for (int i = 0; i < latticeBounds.length; i++) {
            if (i < latticeBounds.length - 1) {
                System.out.print("[");
                double[] ds = latticeBounds[i];
                for (int j = 0; j < ds.length; j++) {

                    double d = ds[j];
                    if (j < ds.length - 1) {
                        System.out.print(d + ",");
                    } else {
                        System.out.print(d);
                    }
                }
                System.out.print("];");
            } else {
                System.out.print("[");
                double[] ds = latticeBounds[i];
                for (int j = 0; j < ds.length; j++) {

                    double d = ds[j];
                    if (j < ds.length - 1) {
                        System.out.print(d + ",");
                    } else {
                        System.out.print(d);
                    }
                }
                System.out.print("]");
            }
        }
        System.out.println("]");
//       System.out.println(" "+);

    }

    public double get_latticeBoxSize() {
        if(latticeBoxSize==-1 || latticeBoxSizeSet == false){
            throw new RuntimeException("lattice Box size not set porperly!");
        }
        return latticeBoxSize;
    }

    public void set_latticeBounds(double[][] latticeBounds) {
        this.latticeBounds = latticeBounds;
    }

    public void set_latticeBoxSize(double latticeBoxSize) {
        latticeBoxSizeSet = true;
        this.latticeBoxSize = latticeBoxSize;
    }

    public double[][] get_latticeBounds() {
        return latticeBounds;
    }

    public String get_outputPath() {
        return outputPath;
    }

    public String get_outFileName() {
        return outFileName;
    }

    public void set_outputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void set_outFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public ArrayList<IAnalysisInstruction> get_analysisInstructions() {
        return this.analysisInstructions;
    }

    void set_analysisInstructions(ArrayList<IAnalysisInstruction> analysisInstructionList) {
        this.analysisInstructions = analysisInstructionList;
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }
}
