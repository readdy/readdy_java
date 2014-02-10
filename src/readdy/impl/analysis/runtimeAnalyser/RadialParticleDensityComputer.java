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
package readdy.impl.analysis.runtimeAnalyser;

import readdy.api.analysis.IReaddyRuntimeAnalyser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.impl.tools.StringTools;
import statlab.base.util.DoubleArrays;

/**
 * This analyzer computes the radial density along the radius outwards from a
 * given point to a treshold. It can be used e.g. to calculate how exact,
 * particles are held on a spherical potential. For this purpose, the center of
 * the analyser is set to the spherical potential and the treshold is chosen
 * such, that it is greater than the sphere potential radius.
 *
 * @author schoeneberg
 */
public class RadialParticleDensityComputer implements IReaddyRuntimeAnalyser {

    // global parameters
    int nBins = 1000;
    double binWidth = 100; // nm (rhodopsin radius is 2nm)
    int axis = -1; // 0,1,2 for x,y,z
    /**
     * typeIdsToCheckFrom is the set of particleType Ids from which we compute
     * our rdf from. E.G. We only sit on R* to check all G's around it the Type
     * Id of R* would be in typeIdsToCheckFrom the type Id of G would be in the
     * typeIdsToBeChecked
     */
    Set<Integer> typeIdsToCheck = new HashSet();
    private IParticleConfiguration particleConfiguration;
    private double[] particleDensity;
    // if we live on a spherical surface we cannot simply use the catesian distance
    boolean sphericalSurface = false;
    double sphereOrDiskRadius;
    double maxDistanceOnSphere;
    private int currentTimestep;
    ArrayList<ArrayList<String>> output = new ArrayList();
    IGlobalParameters globalParameters = null;
    double tresholdRadius = 0; // the radius up to which the radial particle density is computed
    double[] center = new double[3];
    HashMap<String, Radial_Density_Computer_Flags> possibleSpecialFlagKeys;

    /**
     * within the lattice dimensions i bin particles along a specified axis
     */
    public RadialParticleDensityComputer() {
        possibleSpecialFlagKeys = new HashMap();
        possibleSpecialFlagKeys.put("TYPE_IDS_TO_CHECK", Radial_Density_Computer_Flags.TYPE_IDS_TO_CHECK);
        possibleSpecialFlagKeys.put("CENTER", Radial_Density_Computer_Flags.CENTER);
        possibleSpecialFlagKeys.put("TRESHOLD_RADIUS", Radial_Density_Computer_Flags.TRESHOLD_RADIUS);
        possibleSpecialFlagKeys.put("BIN_WIDTH", Radial_Density_Computer_Flags.BIN_WIDTH);
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {

        if (specialFlags.length == 4) {
            parseSpecialFlags(specialFlags);
            nBins = (int) Math.ceil(tresholdRadius / binWidth);

            /*
            System.out.println("RadialParticleDensityComputer.setup():\n"
                    + " compute from " + center[0] + "," + center[1] + "," + center[2] + " up to r=" + tresholdRadius + " with bin width " + binWidth + " nm");
                    */

        } else {
            throw new RuntimeException("number of special flags is not 4");
        }

        this.currentTimestep = currentStep;
        this.particleConfiguration = particleConfiguration;
    }

    public IDataReadyForOutput getOutputData() {
        return new DataReadyForOutput(output);
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public void setParticleConfiguration(IParticleConfiguration particleConfiguration) {
        this.particleConfiguration = particleConfiguration;
    }

    public void analyse() {

        particleDensity = new double[nBins];
        // initialize the array
        for (int j = 0; j < particleDensity.length; j++) {
            particleDensity[j] = 0.0;
        }
        output.clear();


        Iterator<IParticle> iter = particleConfiguration.particleIterator();


        // bin particle numbers along radius
        int counterPairsThatWentIntoTheRdfComputation = 0;
        while (iter.hasNext()) {
            // particle to compute from
            IParticle p = iter.next();

            if (typeIdsToCheck.contains(p.get_type())) {
                double distanceFromCenter = DoubleArrays.norm(DoubleArrays.subtract(center,p.get_coords()));
                int binId = getBinIdToIncrement(distanceFromCenter, binWidth, nBins);
                particleDensity[binId]++;
                counterPairsThatWentIntoTheRdfComputation++;
            }
        }
        /*
        double currentSmallerSphereVolume = 0;
        double currentSmallerSphereRadius = 0;
        double totalSystemVolume = 4/3*Math.PI*tresholdRadius*tresholdRadius*tresholdRadius;
        */
        
        // normalize
        for (int i = 0; i < particleDensity.length; i++) {
            // normalize over the number of observed particles
            particleDensity[i] = particleDensity[i] / counterPairsThatWentIntoTheRdfComputation;
            
            // normalize over the volume 
            /*
            double largerSphereRadius = currentSmallerSphereRadius + binWidth;
            double largerSphereVolume = 4 / 3 * Math.PI * largerSphereRadius * largerSphereRadius * largerSphereRadius;
            double volumeOfSphereShell = largerSphereVolume - currentSmallerSphereVolume;
            particleDensity[i] = particleDensity[i] * totalSystemVolume / volumeOfSphereShell;

            currentSmallerSphereRadius = largerSphereRadius;
            currentSmallerSphereVolume = largerSphereVolume;
            */
        }


        //output
        ArrayList<String> line = new ArrayList();
        for (int i = 0; i < particleDensity.length; i++) {
            if (!line.isEmpty()) {
                output.add(line);
            }
            line = new ArrayList();
            line.add(currentTimestep * globalParameters.get_dt() + "");
            String coordVsDensity =  ((i + 0.5) * binWidth) + ", " + particleDensity[i];
            line.add(coordVsDensity);
        }
        line.add("\n\n");
        output.add(line);

    }

    private int getBinIdToIncrement(double coord, double binWidth, int nBins) {
        for (int i = 0; i < nBins; i++) {
            if (coord >=  (i) * binWidth
                    && coord <  (i + 1) * binWidth) {
                return i;
            }
        }
        return -1;
    }

    private Set<Integer> parseTypeIdSet_rawString(String typeIdsToCheckFrom_rawString) {
        // eg. form: "[0;1;2]"
        Set<Integer> resultSet = new HashSet();
        if (typeIdsToCheckFrom_rawString.charAt(0) == '[' && typeIdsToCheckFrom_rawString.charAt(typeIdsToCheckFrom_rawString.length() - 1) == ']') {
            // start parsing
            typeIdsToCheckFrom_rawString = typeIdsToCheckFrom_rawString.replaceAll("\\[", "");
            typeIdsToCheckFrom_rawString = typeIdsToCheckFrom_rawString.replaceAll("\\]", "");
            String[] ids = typeIdsToCheckFrom_rawString.split(";");
            for (String id : ids) {
                resultSet.add(Integer.parseInt(id));
            }
            return resultSet;
        } else {
            throw new RuntimeException("parsing of string " + typeIdsToCheckFrom_rawString + " failed!");
        }
    }

    private boolean parseSpecialFlags(String[] specialFlags) {
        if (specialFlags != null && specialFlags.length != 0) {
            for (String flag : specialFlags) {
                String[] splittedFlag = flag.split("=");
                if (splittedFlag.length == 2) {
                    String key = splittedFlag[0];
                    String value = splittedFlag[1];
                    handleKeyValuePair(key, value);
                } else {
                    System.out.println("splitting by '=' failed");
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void handleKeyValuePair(String key, String value) {
        if (possibleSpecialFlagKeys.containsKey(key)) {

            switch (possibleSpecialFlagKeys.get(key)) {
                case TYPE_IDS_TO_CHECK:
                    typeIdsToCheck = parseTypeIdSet_rawString(value);
                    for (int typeId : typeIdsToCheck) {
                        System.out.println("particles to check  " + typeId);
                    }
                    break;
                case CENTER:
                    center = StringTools.splitArrayString_convertToDouble(value);
                    break;

                case TRESHOLD_RADIUS:
                    tresholdRadius = Double.parseDouble(value);
                    break;

                case BIN_WIDTH:
                    binWidth = Double.parseDouble(value);
                    break;
                default:
                    throw new RuntimeException("special flag not recognized");


            }
            

        } else {
            throw new RuntimeException("key " + key + " is unknown");
        }

    }
}

enum Radial_Density_Computer_Flags {

    TYPE_IDS_TO_CHECK,
    CENTER,
    TRESHOLD_RADIUS,
    BIN_WIDTH
}
