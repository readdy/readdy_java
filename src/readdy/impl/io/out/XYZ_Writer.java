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
package readdy.impl.io.out;

import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.io.out.IOutputWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schoeneberg
 */
public class XYZ_Writer implements IOutputWriter {

    private BufferedWriter out;

    public void write(int stepId, IDataReadyForOutput dataToPrint) {


        ArrayList<ArrayList<String>> data = dataToPrint.get_data();
        // print number of coordinates following now
        printNewStepLine(data.size() + "");
        printNewStepLine("stepId: " + stepId);
        for (ArrayList<String> line : data) {
            printLine(line);
        }
        flush();
    }

    public void write(IDataReadyForOutput dataToPrint) {


        ArrayList<ArrayList<String>> data = dataToPrint.get_data();
        // print number of coordinates following now
        printNewStepLine(data.size() + "");
        for (ArrayList<String> line : data) {
            printLine(line);
        }
        flush();
    }

    public void open(String outFileName) {
        Date date = new Date();
        try {
            out = new BufferedWriter(new FileWriter(outFileName));
            //out.write("ReaDDY by j schoeneberg. Run on " + date+"\n");
        } catch (IOException e) {
        }
    }

    private void printNewStepLine(String commentForTheFollowingStep) {
        try {
            out.write(commentForTheFollowingStep + "\n");
        } catch (IOException ex) {
            Logger.getLogger(XYZ_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void printLine(ArrayList<String> line) {
        try {
            // skip the first data entry which is the particle ID and start with
            // the type
            for (int i = 1; i < line.size(); i++) {
                if (i == 1) {
                    out.write("C_" + line.get(i) + "\t\t\t");
                    continue;
                }
                if (i == line.size() - 1) {
                    out.write(line.get(i) + "\t\t\t");
                } else {
                    out.write(line.get(i) + "\t\t\t");
                }
            }
            out.write("\n");
        } catch (IOException ex) {
            Logger.getLogger(XYZ_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(XYZ_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(XYZ_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
