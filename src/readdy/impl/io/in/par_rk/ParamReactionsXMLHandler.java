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
package readdy.impl.io.in.par_rk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsXMLHandler;
import readdy.api.io.in.par_rk.IReactionData;
import readdy.impl.sim.ReaDDySimulator;

/**
 *
 * @author schoeneberg
 */
public class ParamReactionsXMLHandler implements IParamReactionsXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text
    ArrayList<IReactionData> reactionDataList = new ArrayList();
    ReactionData reaction;
    int currentReactionTypeId = -1;
    ArrayList<String[]> educts, products;
    // list to cumulate the parsed particle data
    // and that is returned finally
    IParamReactionsFileData paramReactionsFileData = null;
    boolean currentlyEductsParsing = false;
    boolean currentlyProductsParsing = false;
    boolean fileParsed = false;
    private String currentReactiveInternalIds;
    private String currentReagentType;

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
            throw new RuntimeException("version mismatch: inputVersion = " + docVersion + " requestedVersion = " + version[0] + "." + version[1] + "." + version[2]);
        }
    }

    private int getNextReactionId() {
        currentReactionTypeId++;
        return currentReactionTypeId;
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
        // only order 0,1 and 2 are allowed!
        checkOrderOfReactions();

        this.paramReactionsFileData = new ParamReactionsFileData(reactionDataList);
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

        if (first) {
            first = false;
            if (localName.equals("param_reactions")) {
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
                throw new RuntimeException("the opening tag has to be <param_reactions version=\"x.x\">");
            }
        }


        if (localName.equals("reaction")) {
            reaction = new ReactionData();
        }
        if (localName.equals("educts")) {
            currentlyEductsParsing = true;
            currentlyProductsParsing = false;

            educts = new ArrayList();
        }

        if (localName.equals("products")) {
            currentlyEductsParsing = false;
            currentlyProductsParsing = true;

            products = new ArrayList();
        }


        if (localName.equals("educt") || localName.equals("product")) {
            if (atts != null) {
                currentReactiveInternalIds = "";
                int nAtts = atts.getLength();
                for (int i = 0; i < nAtts; i++) {
                    if (atts.getLocalName(i).equals("reactiveInternalIds")) {
                        currentReactiveInternalIds = atts.getValue(i);
                        checkCurrentReactiveInternalIdsToBeValid(currentReactiveInternalIds);
                    }
                    if (atts.getLocalName(i).equals("type")) {

                        currentReagentType = atts.getValue(i);
                    }
                }
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (localName.equals("name")) {
            reaction.setRkName(accumulator.toString().trim());
        }

        if (localName.equals("type")) {
            reaction.setRkTypeName(accumulator.toString().trim());
        }

        if (localName.equals("educts")) {
            reaction.setEducts(educts);
        }

        if (localName.equals("products")) {
            reaction.setProducts(products);
        }

        if (localName.equals("educt")) {
            String currentReagentTypeName = accumulator.toString().trim();
            if (currentlyEductsParsing) {
                educts.add(new String[]{currentReagentType, currentReactiveInternalIds, currentReagentTypeName});
            } else {
                throw new RuntimeException("this should never happen!");
            }
        }

        if (localName.equals("product")) {
            String currentReagentTypeName = accumulator.toString().trim();
            if (currentlyProductsParsing) {

                products.add(new String[]{currentReagentType, currentReactiveInternalIds, currentReagentTypeName});
            } else {
                throw new RuntimeException("this should never happen!");
            }
        }

        if (localName.equals("k_forward")) {
            reaction.setkForward(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("k_backward")) {
            reaction.setkBackward(Double.parseDouble(accumulator.toString().trim()));
        }

        if (localName.equals("reaction")) {
            reaction.setId(getNextReactionId());
            reactionDataList.add(reaction);
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

    private void checkForDublicates() {
        HashSet<String> knownNames = new HashSet();
        HashSet<Integer> knownIds = new HashSet();
        for (IReactionData rkData : reactionDataList) {
            String rkName = rkData.get_rkName();

            if (knownNames.contains(rkName)) {
                throw new RuntimeException("The specifid reaction name " + rkName + " already exists in the "
                        + "elemental reaction file.");
            } else {
                knownNames.add(rkName);
            }

            int id = rkData.get_rkId();
            if (knownIds.contains(id)) {
                throw new RuntimeException("The specifid elemental reaction id " + id + " already exists in the "
                        + "elemental reaction file.");
            } else {
                knownIds.add(id);
            }
        }
    }

    private void checkOrderOfReactions() {
        for (IReactionData rkData : reactionDataList) {
            int orderForward = rkData.get_educts().size();
            int orderBackward = rkData.get_products().size();

            if (orderForward < 0 || orderForward > 2) {
                throw new RuntimeException("The specifid reaction name " + rkData.get_rkName()
                        + " has a wrong reaction order in forward direction. "
                        + "Namely " + orderForward);
            }

            if (orderBackward < 0 || orderBackward > 2) {
                throw new RuntimeException("The specifid reaction name " + rkData.get_rkName()
                        + " has a wrong reaction order in backward direction. "
                        + "Namely " + orderBackward);
            }


        }
    }

    public IParamReactionsFileData get_ParamReactionsFileData() {
        return paramReactionsFileData;
    }

    private void checkCurrentReactiveInternalIdsToBeValid(String currentReactiveInternalIds) {
        if (currentReactiveInternalIds != null) {
            if (!(currentReactiveInternalIds.contentEquals("all")
                    || Pattern.matches("^\\[(\\[\\d,\\d\\];)*(\\[\\d,\\d\\])\\]$", currentReactiveInternalIds))) {
                throw new RuntimeException("the currentReactiveInternalIds have to match either 'all' \n"
                        + "or the following pattern '^\\[(\\[\\d,\\d\\];)*(\\[\\d,\\d\\])\\]$'.\n"
                        + " But '" + currentReactiveInternalIds + "' is given");
            }
        }

    }
}
