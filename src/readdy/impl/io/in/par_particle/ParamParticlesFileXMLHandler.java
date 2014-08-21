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
package readdy.impl.io.in.par_particle;

import readdy.api.io.in.par_particle.IParamParticlesFileData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import readdy.api.io.in.par_particle.IParamParticlesFileXMLHandler;
import readdy.api.io.in.par_particle.IParticleData;
import readdy.impl.sim.ReaDDySimulator;

/**
 *
 * @author schoeneberg
 */
public class ParamParticlesFileXMLHandler implements IParamParticlesFileXMLHandler {

    static final int[] version = ReaDDySimulator.version;
    StringBuffer accumulator = new StringBuffer();  // Accumulate parsed text
    ParticleData particleData;
    int currentParticleTypeId = 0;
    // list to cumulate the parsed particle data
    // and that is returned finally
    ArrayList<IParticleData> particleDataList = new ArrayList();
    IParamParticlesFileData paramParticlesFileData = null;
    HashSet<String> seenParticleTypeNames = new HashSet();
    boolean collisionRadiusParsing = false;
    HashMap<String, Double> collisionRadii = new HashMap();
    ArrayList<HashMap<String, Double>> collisionRadii_tmpStorage = new ArrayList();
    String collisionRadiusPartnerType = "";
    double collisionRadiusValue = 0;
    boolean reactionRadiusParsing = false;
    HashMap<String, Double> reactionRadii = new HashMap();
    ArrayList<HashMap<String, Double>> reactionRadii_tmpStorage = new ArrayList();
    String reactionRadiusParnterType = "";
    double reactionRadiusValue = 0;
    HashMap<String, Integer> typeName2typeId = new HashMap();
    //to access the array list of the particles
    int idIterator = 0;

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
        compute_typeName_to_typeId_map();
        insertParticleIdIntoAllParticleDataObjects();
        //Now we are able to process the collected maps that
        //relate typeName to radii and transform them to relate typeIDs to radii
        transformTypeStringMapsToTypeIdMapsAndGiveItToParticles();

        paramParticlesFileData = new ParamParticlesFileData(particleDataList);

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
            if (localName.equals("param_particles")) {
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
                throw new RuntimeException("the opening tag has to be <param_particles version=\"x.x\">");
            }
        }

        if (localName.equals("particles")) {
            String docVersion = "";
            if (atts != null) {
                int nAtts = atts.getLength();
                for (int i = 0; i < nAtts; i++) {
                    if (atts.getLocalName(i).equals("version")) {
                        docVersion = atts.getValue(i);
                    }
                }
            }
        }

        if (localName.equals("particle")) {
            particleData = new ParticleData();
            //Particles.particleList.add(1,particleData);
        }
        // RADII
        if (localName.equals("collisionRadiusMap")) {
            collisionRadii = new HashMap();
            //Particles.particleList.add(1,particleData);
        }
        if (localName.equals("collisionRadius")) {
            collisionRadiusPartnerType = "";
            collisionRadiusValue = 0;
            collisionRadiusParsing = true;
            //Particles.particleList.add(1,particleData);
        }

        if (localName.equals("reactionRadiusMap")) {
            reactionRadii = new HashMap();
            //Particles.particleList.add(1,particleData);
        }
        if (localName.equals("reactionRadius")) {
            reactionRadiusParnterType = "";
            reactionRadiusValue = 0;
            reactionRadiusParsing = true;
            //Particles.particleList.add(1,particleData);
        }


    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        // if we have collected all data of a particle we store it in the array list
        // of the particles file
        if (localName.equals("particle")) {
            // get current particle ID:
            particleData.setId(getNextParticleTypeId());
            particleDataList.add(particleData);

            collisionRadii_tmpStorage.add(collisionRadii);
            collisionRadii = new HashMap();

            reactionRadii_tmpStorage.add(reactionRadii);
            reactionRadii = new HashMap();

        }

        // store the obtained data from accumulator in the according particle
        // datastructure
        if (localName.equals("type")) {
            String particleTypeName = accumulator.toString().trim();
            if (seenParticleTypeNames.contains(particleTypeName)) {
                throw new RuntimeException("the particle type " + particleTypeName + " is a dublicate. Please remove one.");
            } else {
                seenParticleTypeNames.add(particleTypeName);
                particleData.setType(particleTypeName);
            }
        }

        if (localName.equals("diffusionConstant")) {
            particleData.setD(Double.parseDouble(accumulator.toString().trim()));
        }
        
        if (localName.equals("numberOfDummyParticles")) {
            particleData.setNumberOfDummyParticles(Integer.parseInt(accumulator.toString().trim()));
        }

        // collision radius and reaction radius
        if (localName.equals("partnerType")) {
            if (collisionRadiusParsing) {
                collisionRadiusPartnerType = accumulator.toString().trim();
            }
            if (reactionRadiusParsing) {
                reactionRadiusParnterType = accumulator.toString().trim();
            }
        }

        if (localName.equals("radius")) {
            if (collisionRadiusParsing) {
                collisionRadiusValue = Double.parseDouble(accumulator.toString().trim());
            }
            if (reactionRadiusParsing) {
                reactionRadiusValue = Double.parseDouble(accumulator.toString().trim());
            }
        }

        if (localName.equals("collisionRadius")) {
            if (collisionRadiusParsing) {
                String[] partnerTypes = collisionRadiusPartnerType.split(",");
                for (String partnerType : partnerTypes) {
                    collisionRadii.put(partnerType, collisionRadiusValue);
                }
            }
            collisionRadiusParsing = false;
        }

        if (localName.equals("reactionRadius")) {
            if (reactionRadiusParsing) {
                String[] partnerTypes = reactionRadiusParnterType.split(",");
                for (String partnerType : partnerTypes) {
                    reactionRadii.put(partnerType, reactionRadiusValue);
                }
            }
            reactionRadiusParsing = false;
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

    public ArrayList<IParticleData> get_particleDataList() {
        return particleDataList;
    }

    private int getNextParticleTypeId() {
        int currentId = currentParticleTypeId;
        currentParticleTypeId++;
        return currentId;

    }

    private void compute_typeName_to_typeId_map() {
        for (IParticleData pd : particleDataList) {
            typeName2typeId.put(pd.getType(), pd.getId());

        }

    }

    private void transformTypeStringMapsToTypeIdMapsAndGiveItToParticles() {


        for (int i = 0; i < particleDataList.size(); i++) {

            Double defaultCollisionRadius = null;
            Double defaultReactionRadius = null;

            // -------------------collisions----------------------
            HashMap<String, Double> collTypeNameMap = collisionRadii_tmpStorage.get(i);
            HashMap<Integer, Double> collTypeIdMap = new HashMap();

            for (String typeName : collTypeNameMap.keySet()) {
                if (typeName.equals("default")) {
                    defaultCollisionRadius = collTypeNameMap.get(typeName);

                    // for all types the same radius
                    for (int typeId : typeName2typeId.values()) {
                        collTypeIdMap.put(typeId, collTypeNameMap.get(typeName));
                    }
                }

            }
            // if we have more specific radii the default value will be overwritten
            for (String typeName : collTypeNameMap.keySet()) {
                if (!typeName.equals("default")) {
                    // for all types the same radius
                    if (typeName2typeId.get(typeName) != null) {
                        collTypeIdMap.put(typeName2typeId.get(typeName), collTypeNameMap.get(typeName));
                    } else {
                        throw new RuntimeException("particle type name '" + typeName + "' not known. Maybe a typo?");
                    }
                }

            }
            if (collTypeIdMap.keySet().size() != particleDataList.size()) {
                for (int typeId : collTypeIdMap.keySet()) {
                    System.out.println("present type id: " + typeId);
                }
                System.out.println("particleDataList size:" + particleDataList.size() + " collTypeIdMap size: " + collTypeIdMap.keySet().size());
                throw new RuntimeException("there are some definitions missing in the "
                        + "collisionRadius map of '" + particleDataList.get(i).getType() + "' - Abort!");
            }
            particleDataList.get(i).setCollisionRadiusMap(collTypeIdMap);

            // -------------------reactions----------------------
            HashMap<String, Double> reactTypeNameMap = reactionRadii_tmpStorage.get(i);
            HashMap<Integer, Double> reactTypeIdMap = new HashMap();
            // convert the strings to the fitting particle type ids
            for (String typeName : reactTypeNameMap.keySet()) {
                if (typeName.equals("default")) {
                    defaultReactionRadius = reactTypeNameMap.get(typeName);
                    // for all types the same radius
                    for (int typeId : typeName2typeId.values()) {
                        reactTypeIdMap.put(typeId, reactTypeNameMap.get(typeName));
                    }
                }

            }
            // if we have more specific radii the default value will be overwritten
            for (String typeName : reactTypeNameMap.keySet()) {
                if (!typeName.equals("default")) {
                    // for all types the same radius
                    // check if the type name is actually known and not e.g. a typo:
                    if (typeName2typeId.get(typeName) != null) {


                        reactTypeIdMap.put(typeName2typeId.get(typeName), reactTypeNameMap.get(typeName));
                    } else {
                        throw new RuntimeException("particle type name '" + typeName + "' not known. Maybe a typo?");
                    }

                }

            }

            if (reactTypeIdMap.keySet().size() != particleDataList.size()) {
                throw new RuntimeException("there are some definitions missing in the "
                        + "reactionRadius map of '" + particleDataList.get(i).getType() + "' - Abort!");
            }



            particleDataList.get(i).setReactionRadiusMap(reactTypeIdMap);
            if (defaultCollisionRadius != null) {
                particleDataList.get(i).setDefaultCollisionRadius(defaultCollisionRadius);
            } else {
                throw new RuntimeException("for '" + particleDataList.get(i).getType() + "' there"
                        + "is no default collsion radius given : - Abort!");
            }

            if (defaultReactionRadius != null) {
                particleDataList.get(i).setDefaultReactionRadius(defaultReactionRadius);
            } else {
                throw new RuntimeException("for '" + particleDataList.get(i).getType() + "' there"
                        + "is no default reaction radius given : - Abort!");
            }
        }
    }

    public HashMap<String, Integer> get_typeName2typeIdMap() {
        return typeName2typeId;
    }

    private void insertParticleIdIntoAllParticleDataObjects() {
        for (IParticleData pd : particleDataList) {
            pd.setTypeId(typeName2typeId.get(pd.getType()));
        }
    }

    public IParamParticlesFileData get_paramParticlesFileData() {
        return paramParticlesFileData;
    }
}
