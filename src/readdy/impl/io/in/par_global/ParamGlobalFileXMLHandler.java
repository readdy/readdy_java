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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.analysis.IAnalysisInstruction;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileXMLHandler;
import readdy.impl.analysis.AnalysisInstruction;
import readdy.impl.sim.ReaDDySimulator;
import readdy.impl.tools.StringTools;

/**
 *
 * @author schoeneberg
 */
class ParamGlobalFileXMLHandler implements IParamGlobalFileXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text within a tag
    GlobalParameters globalParameters = null;
    ArrayList<IAnalysisInstruction> analysisInstructionList = new ArrayList();
    AnalysisInstruction currentAnalysisInstruction = new AnalysisInstruction();
    boolean documentFullyParsed = false;
    boolean currentlytParsingAnalysisInstruction = false;

    private void verifyVersionNumber(String docVersion) {


        boolean versionMatch = true;
        String[] strArr_docVersion = docVersion.split("\\.");


        if (version.length == strArr_docVersion.length) {
            for (int i = 0; i < strArr_docVersion.length; i++) {
                String s = strArr_docVersion[i];
                int versionSubnumber = Integer.parseInt(s);
                if (versionSubnumber != version[i]) {
                    versionMatch = false;
                    System.out.println(versionSubnumber + "," + version[i]);
                }
            }
        } else {

            versionMatch = false;

        }
        if (!versionMatch) {

            throw new RuntimeException("version mismatch: inputVersion = '" + docVersion + "' requestedVersion = '" + version[0] + "." + version[1] + "." + version[2] + "'");
        }
    }

    public ParamGlobalFileXMLHandler() {
    }

    public IGlobalParameters get_globalParameters() {
        if (documentFullyParsed) {
            return globalParameters;
        } else {
            throw new RuntimeException("globalParametersFile has not been parsed properly - exit.");
        }

    }

    public void setDocumentLocator(Locator lctr) {
    }

    public void startDocument() throws SAXException {
        documentFullyParsed = false;
        globalParameters = new GlobalParameters();

    }

    public void endDocument() throws SAXException {
        globalParameters.set_analysisInstructions(analysisInstructionList);
        documentFullyParsed = true;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private boolean first = true;

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        accumulator.setLength(0);
        //System.out.println(localName);

        if (first) {
            first = false;
            if (localName.equals("param_global")) {
                String docVersion = "";
                if (atts != null) {
                    int nAtts = atts.getLength();
                    for (int i = 0; i < nAtts; i++) {
                        if (atts.getLocalName(i).equals("version")) {
                            docVersion = atts.getValue(i);
                        }
                    }
                }
                verifyVersionNumber(docVersion);
            } else {
                throw new RuntimeException("the opening tag has to be of format <param_global version=\"x.x\">");
            }
        }

        if (localName.equals("analyser")) {
            currentAnalysisInstruction = new AnalysisInstruction();
            currentlytParsingAnalysisInstruction = true;
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("nSimulationSteps")) {
            globalParameters.set_nSimulationSteps(Long.parseLong(accumulator.toString().trim()));
        }

        if (localName.equals("outputEveryXSteps")) {
            globalParameters.set_outputEveryXSteps(Integer.parseInt(accumulator.toString().trim()));
        }

        if (localName.equals("dt")) {
            globalParameters.set_dt(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("dtReaDDy")) {
            globalParameters.set_dt(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("dtOpenMM")) {
            globalParameters.set_dtOpenMM(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("cudaDeviceIndex")) {
            globalParameters.set_cudaDeviceIndex(Integer.parseInt(accumulator.toString().trim()));
        }

        if (localName.equals("cuda")) {
            globalParameters.set_cudaDeviceIndex(Integer.parseInt(accumulator.toString().trim()));
        }

        if (localName.equals("T")) {
            globalParameters.set_T(Double.parseDouble(accumulator.toString().trim()));
        }

        /* this is no longer available as input
        if (localName.equals("Kb")) {
            globalParameters.set_Kb(Double.parseDouble(accumulator.toString().trim()));
        }
        */

        if (localName.equals("latticeBoxSize")) {
            globalParameters.set_latticeBoxSize(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("latticeBounds")) {
            String s = accumulator.toString().trim();
            double[][] dMatrix = StringTools.splitMatrixString_convertToDouble(s);
            globalParameters.set_latticeBounds(dMatrix);
        }

        if (localName.equals("outputPath")) {
            globalParameters.set_outputPath(accumulator.toString().trim());
        }

        if (localName.equals("outFileName")) {
            globalParameters.set_outFileName(accumulator.toString().trim());
        }

        if (localName.equals("outFileName")) {
            globalParameters.set_outFileName(accumulator.toString().trim());
        }

        // ##### Analysis methods

        if (localName.equals("method") && currentlytParsingAnalysisInstruction) {
            String s = (accumulator.toString().trim());
            currentAnalysisInstruction.setMethod(s);
        }

        if (localName.equals("everyXStep") && currentlytParsingAnalysisInstruction) {
            String s = (accumulator.toString().trim());
            currentAnalysisInstruction.setEveryXStep(Integer.parseInt(s));
        }

        if (localName.equals("outputFile") && currentlytParsingAnalysisInstruction) {
            String s = (accumulator.toString().trim());
            currentAnalysisInstruction.setOutputFileName(s);
        }

        if (localName.equals("outputFormat") && currentlytParsingAnalysisInstruction) {
            String s = (accumulator.toString().trim());
            currentAnalysisInstruction.setOutputFormat(s);
        }

        if (localName.equals("specialFlags") && currentlytParsingAnalysisInstruction) {
            String[] s = (accumulator.toString().trim()).split("\\|");
            currentAnalysisInstruction.setSpecialFlags(s);
        }

        if (localName.equals("analyser")) {
            analysisInstructionList.add(currentAnalysisInstruction);
            currentlytParsingAnalysisInstruction = false;
        }


    }

    public void characters(char[] buffer, int start, int length) throws SAXException {
        accumulator.append(buffer, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void processingInstruction(String target, String data) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void skippedEntity(String name) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
