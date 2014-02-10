package readdy.impl.io.in.tpl_pot;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileDataEntry;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileXMLHandler;
import readdy.impl.sim.ReaDDySimulator;

/**
 *
 * @author schoeneberg
 */
public class TplgyPotentialsFileXMLHandler implements ITplgyPotentialsFileXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text
    TplgyPotentialsFileDataEntry potentialEntry;
    // list to cumulate the parsed particle data
    // and that is returned finally
    ArrayList<ITplgyPotentialsFileDataEntry> potentialEntryList = new ArrayList();
    ITplgyPotentialsFileData tplgyPotentialsFileData = null;
    private boolean fileParsedAlready = false;

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

        tplgyPotentialsFileData = new TplgyPotentialsFileData(potentialEntryList);
        fileParsedAlready = true;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    private boolean first = true;

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        accumulator.setLength(0);  // Ready to accumulate new text

        if (first) {
            first = false;
            if (localName.equals("tplgy_potentials")) {
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
                throw new RuntimeException("the opening tag has to be <tplgy_potentials version=\"x.x.x\">");
            }
        }

        if (localName.equals("pot")) {
            potentialEntry = new TplgyPotentialsFileDataEntry();
            HashMap<String, String> paramNameToValueMap = new HashMap();
            if (atts != null) {
                int nAtts = atts.getLength();

                for (int i = 0; i < nAtts; i++) {
                    paramNameToValueMap.put(atts.getLocalName(i), atts.getValue(i));

                }

            }
            potentialEntry.set_paramNameToValueMap(paramNameToValueMap);
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        // if we have collected all data of a particle we store it in the array list
        // of the particles file
        if (localName.equals("pot")) {
            //particleData.print();
            // get current particle ID:
            potentialEntryList.add(potentialEntry);
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

    public ITplgyPotentialsFileData get_tplgyPotentialsFileData() {
        if (fileParsedAlready) {

            return tplgyPotentialsFileData;
        } else {
            throw new RuntimeException("tplgy_potentials file not parsed yet.");
        }
    }
}
