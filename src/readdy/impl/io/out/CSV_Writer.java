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
public class CSV_Writer implements IOutputWriter {

    private BufferedWriter out;

    public void write(int stepId, IDataReadyForOutput dataToPrint) {
        ArrayList<ArrayList<String>> data = dataToPrint.get_data();
        for (ArrayList<String> line : data) {
            printLine(stepId, line);
        }
        flush();
    }

    public void write(IDataReadyForOutput dataToPrint) {
        ArrayList<ArrayList<String>> data = dataToPrint.get_data();
        for (ArrayList<String> line : data) {
            printLine(line);
        }
        flush();
    }

    public void open(String outFileName) {
        Date date = new Date();
        try {
            out = new BufferedWriter(new FileWriter(outFileName));
            out.write("#ReaDDY by j schoeneberg. Run on " + date + "\n");

        } catch (IOException e) {
            System.out.println(e);
            System.out.println("the opening of the output file was not successfull!");
        }
    }

    private void printNewStepLine(String commentForTheFollowingStep) {
        try {
            out.write(commentForTheFollowingStep + "\n");
        } catch (IOException ex) {
            Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void printLine(int stepId, ArrayList<String> line) {
        if (out != null) {


            if (line.size() > 0) {
                if (line.get(0).equals("#")) {
                    printCommentLine(line);
                } else {
                    if (line.get(0).equals("HEADER")) {
                        printHeaderLine(line);
                    } else {

                        try {
                            out.write(stepId + ", ");
                            for (int i = 0; i < line.size(); i++) {
                                if (i == line.size() - 1) {
                                    out.write(line.get(i) + "");
                                } else {
                                    out.write(line.get(i) + ", ");
                                }

                            }
                            out.write("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wrong here!");
        }
    }

    private void printLine(ArrayList<String> line) {
        if (out != null) {
            if (line.size() > 0) {
                if (line.get(0).equals("#")) {
                    printCommentLine(line);
                } else {
                    if (line.get(0).equals("HEADER")) {
                        printHeaderLine(line);
                    } else {
                        try {
                            for (int i = 0; i < line.size(); i++) {
                                if (i == line.size() - 1) {
                                    out.write(line.get(i) + "");
                                } else {
                                    out.write(line.get(i) + ", ");
                                }

                            }
                            out.write("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wront here!");
        }
    }

    private void printCommentLine(ArrayList<String> line) {
        if (out != null) {
            try {
                for (int i = 0; i < line.size(); i++) {
                    if (i == line.size() - 1) {
                        out.write(line.get(i) + "");
                    } else {
                        out.write(line.get(i) + ", ");
                    }
                }
                out.write("\n");
            } catch (IOException ex) {
                Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wront here!");
        }
    }

    private void printHeaderLine(ArrayList<String> line) {
        // throw away the first "HEADER" flag and there you go
        if (out != null) {
            try {
                for (int i = 1; i < line.size(); i++) {
                    if (i == line.size() - 1) {
                        out.write(line.get(i) + "");
                    } else {
                        out.write(line.get(i) + ", ");
                    }
                }
                out.write("\n");
            } catch (IOException ex) {
                Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wront here!");
        }
    }

    public void flush() {
        if (out != null) {
            try {
                out.flush();
            } catch (IOException ex) {
                Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wront here!");
        }
    }

    public void close() {
        if (out != null) {
            Date date = new Date();

            try {
                out.write("#ReaDDY by j schoeneberg. Run finished on " + date + "\n");
                out.flush();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(CSV_Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("CSVWriter_printLine(): something wront here!");
        }
    }
}
