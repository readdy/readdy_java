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
package readdy.impl.io.in.par_group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.io.in.par_group.IGroupData;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileXMLHandler;
import readdy.impl.sim.ReaDDySimulator;
import readdy.impl.tools.StringTools;

/**
 *
 * @author schoeneberg
 */
public class ParamGroupsFileXMLHandler implements IParamGroupsFileXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text
    GroupData groupData;
    int currentParticleTypeId = -1;
    // list to cumulate the parsed particle data
    // and that is returned finally
    ArrayList<IGroupData> groupDataList = new ArrayList();
    IParamGroupsFileData paramGroupsFileData = null;
    ArrayList<HashMap<String, String>> involvedPotentials = null;

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

    public void setDocumentLocator(Locator locator) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void startDocument() throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endDocument() throws SAXException {
        // only now we have read all particle type strings and assigned an id
        // for them.
        checkForParticleTypeNameDublicates();



        paramGroupsFileData = new ParamGroupsFileData(groupDataList);

    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    boolean nowBuildingBlocksParsing = false;
    String currentBuildingBlockParticleOrGroup;
    String currentInternalIds;
    String currentBuildingBlockTypeName;
    String currentBuildingBlockTemplateC;
    String currentBuildingBlockTemplateCoords;
    ArrayList<String[]> buildingBlocks;
    private boolean first = true;

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        accumulator.setLength(0);

        if (first) {
            first = false;
            if (localName.equals("param_groups")) {
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
                throw new RuntimeException("the opening tag has to be <param_groups version=\"x.x\">");
            }
        }



        if (localName.equals("particleGroup")) {
            currentBuildingBlockParticleOrGroup = "";
            currentInternalIds = "";
            currentBuildingBlockTypeName = "";
            currentBuildingBlockTemplateCoords = "";
            if (!nowBuildingBlocksParsing) {
                groupData = new GroupData();
            } else {
                currentBuildingBlockParticleOrGroup = "group";
                if (atts != null) {
                    int nAtts = atts.getLength();
                    for (int i = 0; i < nAtts; i++) {
                        if (atts.getLocalName(i).equals("internalIds")) {
                            currentInternalIds = atts.getValue(i);
                        }
                        if (atts.getLocalName(i).equals("type")) {
                            currentBuildingBlockTypeName = atts.getValue(i);
                        }
                        if (atts.getLocalName(i).equals("templateCs")) {
                            currentBuildingBlockTemplateCoords = atts.getValue(i);
                        }
                    }
                }
                buildingBlocks.add(new String[]{currentBuildingBlockParticleOrGroup,
                            currentInternalIds,
                            currentBuildingBlockTypeName,currentBuildingBlockTemplateCoords});
            }
        }

        if (localName.equals("particle")) {
            currentBuildingBlockParticleOrGroup = "";
            currentInternalIds = "";
            currentBuildingBlockTypeName = "";
            currentBuildingBlockTypeName = "";
            currentBuildingBlockParticleOrGroup = "particle";
            if (atts != null) {
                int nAtts = atts.getLength();
                for (int i = 0; i < nAtts; i++) {
                    if (atts.getLocalName(i).equals("internalId")) {
                        currentInternalIds = atts.getValue(i);
                    }
                    if (atts.getLocalName(i).equals("type")) {

                        currentBuildingBlockTypeName = atts.getValue(i);

                    }
                    if (atts.getLocalName(i).equals("templateC")) {

                        currentBuildingBlockTemplateC = atts.getValue(i);

                    }
                }
            }
            buildingBlocks.add(new String[]{currentBuildingBlockParticleOrGroup,
                        currentInternalIds,
                        currentBuildingBlockTypeName,
                        currentBuildingBlockTemplateC});
        }


        if (localName.equals("buildingBlocks")) {
            nowBuildingBlocksParsing = true;
            buildingBlocks = new ArrayList();
        }
        if (localName.equals("potentials")) {
            involvedPotentials = new ArrayList();
        }

        if (localName.equals("pot")) {
            HashMap<String, String> potParamNameToValueMap = new HashMap();

            if (atts != null) {
                int nAtts = atts.getLength();
                for (int i = 0; i < nAtts; i++) {
                    String paramName = atts.getLocalName(i);
                    String paramValue = atts.getValue(i);
                    potParamNameToValueMap.put(paramName, paramValue);
                }

            }
            involvedPotentials.add(potParamNameToValueMap);
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        // if we have collected all data of a particle we store it in the array list
        // of the particles file

        if (localName.equals("type")) {
            groupData.setTypeName(accumulator.toString().trim());
            groupData.setId(getNextGroupTypeId());
        }

        if (localName.equals("templateOrigin")) {
            groupData.setTemplateOrigin(StringTools.splitArrayString_convertToDouble(accumulator.toString().trim()));
        }

        if (localName.equals("templateNormal")) {
            groupData.setTemplateNormal(StringTools.splitArrayString_convertToDouble(accumulator.toString().trim()));
        }

        if (localName.equals("buildingBlocks")) {
            nowBuildingBlocksParsing = false;
            groupData.setBuildingBlocks(buildingBlocks);
        }

        if (localName.equals("potentials")) {
            groupData.setInvolvedPotentials(involvedPotentials);
        }

        if (localName.equals("maxNumberOfGroupAssignmentsPerParticle")) {
            if (accumulator.toString().trim().equals("inf")) {
                groupData.setMaxNumberOfGroupAssignmentsPerParticle(-1);
            } else {
                int maxNumberOfGroupAssignmentsPerParticle = Integer.parseInt(accumulator.toString().trim());
                groupData.setMaxNumberOfGroupAssignmentsPerParticle(maxNumberOfGroupAssignmentsPerParticle);
            }
        }


        if (localName.equals("particleGroup")) {

            if (!nowBuildingBlocksParsing) {

                groupDataList.add(groupData);
            }
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

    private int getNextGroupTypeId() {
        currentParticleTypeId++;
        return currentParticleTypeId;

    }

    public IParamGroupsFileData get_paramGroupsFileData() {
        return paramGroupsFileData;
    }

    private void checkForParticleTypeNameDublicates() {
        HashSet<String> seenTypeNames = new HashSet();
        HashSet<Integer> seenTypeIds = new HashSet();
        for (IGroupData gd : groupDataList) {
            String typeName = gd.getTypeName();
            if (seenTypeNames.contains(typeName)) {
                throw new RuntimeException("typeName: " + typeName
                        + " has already been seen - no dublicates allowed");
            } else {
                seenTypeNames.add(typeName);
            }

            int typeId = gd.getTypeId();
            if (seenTypeIds.contains(typeId)) {
                throw new RuntimeException("typeId: " + typeId
                        + " has already been seen - no dublicates allowed");
            } else {
                seenTypeIds.add(typeId);
            }

        }
    }
}
