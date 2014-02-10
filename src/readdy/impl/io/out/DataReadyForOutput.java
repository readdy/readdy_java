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

import java.util.ArrayList;
import readdy.api.io.out.IDataReadyForOutput;

/**
 *
 * @author schoeneberg
 */
public class DataReadyForOutput implements IDataReadyForOutput {

    ArrayList<ArrayList<String>> data;
    String linePrefix;
    String lineSuffix;
    ArrayList<String> commentLines;

    public DataReadyForOutput() {
    }

    public DataReadyForOutput(ArrayList<ArrayList<String>> data) {
        this.data = data;
    }

    public ArrayList<ArrayList<String>> get_data() {
        return data;
    }

    public void set_data(ArrayList<ArrayList<String>> data){
        this.data = data;
    }

    public String get_linePrefix() {
        return this.linePrefix;
    }

    public String get_lineSuffix() {
        return this.lineSuffix;
    }

    public ArrayList<String> get_commentLines() {
        return this.commentLines;
    }

    public void set_linePrefix(String prefix) {
        this.linePrefix = prefix;
    }

    public void set_lineSuffix(String suffix) {
        this.lineSuffix = suffix;
    }

    public void set_commentLines(ArrayList<String> commentLines) {
        this.commentLines = commentLines;
    }
}
