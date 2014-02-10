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
package readdy.impl.io.in.rct_elmtlRk;

import java.util.ArrayList;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.io.in.rct_elmtlRk.IElementalReactionData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileData;
import readdy.api.io.in.rct_elmtlRk.IReactElmtlRkFileXMLHandler;
import readdy.impl.sim.ReaDDySimulator;

/**
 *
 * @author schoeneberg
 */
public class ReactElmtlRkFileXMLHandler implements IReactElmtlRkFileXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text
    ArrayList<IElementalReactionData> elementalReactionDataList = new ArrayList();
    IElementalReactionData elementalReaction = new ElementalReactionData();
    int currentPotentialTypeId = 0;
    // list to cumulate the parsed particle data
    // and that is returned finally
    IReactElmtlRkFileData reactElmtlRkFileData = null;
    boolean currentlyEductsParsing = false;
    boolean currentlyProductsParsing = false;
    ArrayList<String> particleTypeNameList = new ArrayList();
    ArrayList<Integer> particleIdList = new ArrayList();
    boolean fileParsed = false;
    int currentId = -1;

    private void verifyVersionNumber(String docVersion) {
        boolean versionMatch = true;
        String[] strArr_docVersion = docVersion.split("\\.");
        if (version.length == strArr_docVersion.length) {
            for (int i = 0; i < strArr_docVersion.length; i++) {
                String s = strArr_docVersion[i];
                int versionSubnumber = Integer.parseInt(s);
                if (versionSubnumber != version[i]) {
                    versionMatch = false;
                }
            }
        } else {
            versionMatch = false;
        }
        if (!versionMatch) {
            throw new RuntimeException("version mismatch: inputVersion = " + docVersion + " requestedVersion = " + version[0] + "." + version[1]);
        }
    }

    public void setDocumentLocator(Locator locator) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void startDocument() throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endDocument() throws SAXException {
        // only now we have read all particle type strings and assigned an id
        // for them.

        checkForDublicates();
        checkOrderOfReactions();

        this.reactElmtlRkFileData = new ReactElmtlRkFileData(elementalReactionDataList);
        fileParsed = true;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    private boolean first = true;

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        accumulator.setLength(0);
        //System.out.println(localName);

        if (first) {
            first = false;
            if (localName.equals("react_elmtlRk")) {
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
                throw new RuntimeException("the opening tag has to be <react_elmtlRk version=\"x.x\">");
            }
        }


        if (localName.equals("educts")) {
            currentlyEductsParsing = true;
            currentlyProductsParsing = false;

            particleTypeNameList = new ArrayList();
            particleIdList = new ArrayList();
        }

        if (localName.equals("products")) {
            currentlyEductsParsing = false;
            currentlyProductsParsing = true;

            particleTypeNameList = new ArrayList();
            particleIdList = new ArrayList();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        // if we have collected all data of a particle we store it in the array list
        // of the particles file
        if (localName.equals("elementalReaction")) {
            elementalReactionDataList.add(elementalReaction);
            elementalReaction = new ElementalReactionData();
        }

        if (localName.equals("elmtlRkId")) {
            System.out.println("trimed: "+accumulator.toString().trim());
            elementalReaction.set_elmtlRkId(Integer.parseInt(accumulator.toString().trim()));
        }

        if (localName.equals("name")) {
            elementalReaction.set_name(accumulator.toString().trim());
        }

        // store the obtained data from accumulator in the according particle
        // datastructure
        if (localName.equals("p")) {
            elementalReaction.set_p(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("particleType")) {
            particleTypeNameList.add(accumulator.toString().trim());
        }

        if (localName.equals("particleId")) {
            particleIdList.add(Integer.parseInt(accumulator.toString().trim()));
        }

        if (localName.equals("educts")) {
            elementalReaction.set_eductTypeNames(particleTypeNameList);
            elementalReaction.set_eductParticleIds(particleIdList);
        }

        if (localName.equals("products")) {
            elementalReaction.set_productTypeNames(particleTypeNameList);
            elementalReaction.set_productParticleIds(particleIdList);
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

    public IReactElmtlRkFileData get_reactElmtlRkFileData() {
        if (fileParsed) {
            return reactElmtlRkFileData;
        } else {
            throw new RuntimeException("The reaction input file has to be parsed before extracting data from it.");
        }
    }

    private void checkForDublicates() {
        HashSet<String> knownNames = new HashSet();
        HashSet<Integer> knownIds = new HashSet();
        for (IElementalReactionData elmtlRkData : elementalReactionDataList) {
            String rkName = elmtlRkData.get_name();

            if (knownNames.contains(rkName)) {
                throw new RuntimeException("The specifid reaction name " + rkName + " already exists in the "
                        + "elemental reaction file.");
            } else {
                knownNames.add(rkName);
            }

            int id = elmtlRkData.get_elmtlRkId();
            if (knownIds.contains(id)) {
                throw new RuntimeException("The specifid elemental reaction id " + id + " already exists in the "
                        + "elemental reaction file.");
            } else {
                knownIds.add(id);
            }
        }
    }

    private void checkOrderOfReactions() {
        for (IElementalReactionData elmtlRkData : elementalReactionDataList) {
            int orderForward = elmtlRkData.get_eductTypeNames().size() + elmtlRkData.get_eductParticleIds().size();
            int orderBackward = elmtlRkData.get_productTypeNames().size() + elmtlRkData.get_productParticleIds().size();

            if (orderForward < 0 || orderForward > 2) {
                throw new RuntimeException("The specifid reaction name " + elmtlRkData.get_name()
                        + " has a wrong reaction order in forward direction. "
                        + "Namely " + orderForward);
            }

            if (orderBackward < 0 || orderBackward > 2) {
                throw new RuntimeException("The specifid reaction name " + elmtlRkData.get_name()
                        + " has a wrong reaction order in backward direction. "
                        + "Namely " + orderBackward);
            }
        }
    }
}
