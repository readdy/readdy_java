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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.io.out.IOutputWriter;

/**
 *
 * @author schoeneberg
 */
public class PACSV_Writer implements IOutputWriter{

  private BufferedWriter out;
private boolean first = true;
    public void write(int stepId, IDataReadyForOutput dataToPrint) {
        try {
            //printNewStepLine("#" + stepId);
            ArrayList<ArrayList<String>> data = dataToPrint.get_data();
            if(first){
               first = false;
            }else{
                out.write(",\n");
            }
            
                    out.write("\t{" + stepId + ",");
            for (ArrayList<String> line : data) {
                printLine(stepId, line);
            }
            out.write("}");
            flush();
        } catch (IOException ex) {
            Logger.getLogger(PACSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    public void open(String outFileName) {
        Date date = new Date();
        try {
            out = new BufferedWriter(new FileWriter(outFileName));
            out.write("{\n");

        } catch (IOException e) {
        }
    }


    private void printLine(int stepId,ArrayList<String> line) {
        try {
            out.write("{");
            for (int i = 0; i < line.size(); i++) {
                if (i == line.size() - 1) {
                    out.write(line.get(i) + "");
                } else {
                    out.write(line.get(i) + ", ");
                }

            }
            out.write("}");
        } catch (IOException ex) {
            Logger.getLogger(PACSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(PACSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            out.write("\n}");
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(PACSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
