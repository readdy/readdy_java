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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class RdfComputer_GNUplot implements IReaddyRuntimeAnalyser {

    // global parameters
    int nBins = 0;
    double binWidth = 0;
    /**
     * typeIdsToCheckFrom is the set of particleType Ids from
     * which we compute our rdf from.
     * E.G. We only sit on R* to check all G's around it
     * the Type Id of R* would be in typeIdsToCheckFrom
     * the type Id of G would be in the typeIdsToBeChecked
     */
    Set<Integer> typeIdsToCheckFrom = new HashSet();
    /**
     * see description of typeIdsToCheckFrom
     */
    Set<Integer> typeIdsToBeChecked = new HashSet();
    private IParticleConfiguration particleConfiguration;
    private double[] rdf;
    // if we live on a spherical surface we cannot simply use the catesian distance
    boolean sphericalSurface = false;
    double totalSystemVolume = 0;
    private int currentTimestep;
    ArrayList<ArrayList<String>> output = new ArrayList();
    private static final boolean verbose = false;
    IGlobalParameters globalParameters = null;
    HashSet particlesAlreadyTakenIntoAccount = new HashSet();

    public RdfComputer_GNUplot() {
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {

        if (specialFlags.length == 4) {
            String typeIdsToCheckFrom_rawString = specialFlags[0];
            String typeIdsToBeChecked_rawString = specialFlags[1];
            String maxDistance_rawString = specialFlags[2];
            String binWidth_rawString = specialFlags[3];

            binWidth = Double.parseDouble(binWidth_rawString);

            double maxDistanceInSystem = Double.parseDouble(maxDistance_rawString);
            totalSystemVolume = 4 / 3 * Math.PI * maxDistanceInSystem * maxDistanceInSystem * maxDistanceInSystem;
            nBins = (int) Math.ceil(maxDistanceInSystem / binWidth);

            System.out.println("RdfComputer.setup(): \n"
                    + "maxDist = " + maxDistanceInSystem + "\n"
                    + "binWidth = " + binWidth + "\n"
                    + "nBins = " + nBins);

            typeIdsToCheckFrom = parseTypeIdSet_rawString(typeIdsToCheckFrom_rawString);
            for (int id : typeIdsToCheckFrom) {
                System.out.println("check From " + id);
            }
            typeIdsToBeChecked = parseTypeIdSet_rawString(typeIdsToBeChecked_rawString);
            for (int id : typeIdsToBeChecked) {
                System.out.println("check to " + id);
            }

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
        System.out.println("take the current configuration as start for rdf computation");
    }

    public void analyse() {

        rdf = new double[nBins];
        // initialize the array
        for (int j = 0; j < rdf.length; j++) {
            rdf[j] = 0.0;
        }
        output.clear();


        Iterator<IParticle> iter = particleConfiguration.particleIterator();//particleConfiguration.particleIterator());


        // compute rdf
        int counterPairsThatWentIntoTheRdfComputation = 0;
        while (iter.hasNext()) {
            // particle to compute from
            IParticle p1 = iter.next();


            Iterator<IParticle> iter2 = particleConfiguration.particleIterator();
            while (iter2.hasNext()) {
                // particle to compute against
                IParticle p2 = iter2.next();

                if (particlePairIsValidForRdfComputation(p1, p2)) {
                    // actual computation
                    double dist = DoubleArrays.distance(p1.get_coords(), p2.get_coords());

                    //System.out.println("dist: "+dist);
                    int binId = getBinIdToIncrement(dist, binWidth, nBins);
                    if (binId != -1) {
                        rdf[binId]++;
                        counterPairsThatWentIntoTheRdfComputation++;
                    }

                }

            }

        }

        // normalization

        /**
         * the formula to compute the RDF is:
         * g(r) = #shell(r)/  #system * Volume_system / Volume_shell(r)
         */
        double currentSmallerSphereVolume = 0;
        double currentSmallerSphereRadius = 0;
        for (int i = 0; i < rdf.length; i++) {

            // normalize over the number of observed particles
            rdf[i] = rdf[i] / counterPairsThatWentIntoTheRdfComputation;

            // normalize over the volume to get probability 1 everywhere.
            double largerSphereRadius = currentSmallerSphereRadius + binWidth;
            double largerSphereVolume = 4 / 3 * Math.PI * largerSphereRadius * largerSphereRadius * largerSphereRadius;
            double volumeOfSphereShell = largerSphereVolume - currentSmallerSphereVolume;
            rdf[i] = rdf[i] * totalSystemVolume / volumeOfSphereShell;

            currentSmallerSphereRadius = largerSphereRadius;
            currentSmallerSphereVolume = largerSphereVolume;

        }

        ArrayList<String> line = new ArrayList();
        for (int i = 0; i < rdf.length; i++) {
            if (!line.isEmpty()) {
                output.add(line);
            }
            line = new ArrayList();
            line.add(currentTimestep * globalParameters.get_dt() + "");
            String widthVsRdf = ((i + 0.5) * binWidth) + ", " + rdf[i];
            line.add(widthVsRdf);
        }
        line.add("\n\n");
        output.add(line);

    }

    private int getBinIdToIncrement(double dist, double binWidth, int nBins) {
        for (int i = 0; i < nBins; i++) {
            if (dist >= (i) * binWidth && dist < (i + 1) * binWidth) {
                return i;
                //System.out.println(dist+""+i*binWidth+(i+1)*binWidth)
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

    /**
     * checks if a particle pair of computed from particle and computed against particle
     * is valid for rdf computation.
     * This means: p1 != p2
     * p1 is in the ComputedFromList
     * p2 is in the ComputedAgainstList
     *
     * @param p1
     * @param p2
     * @return
     */
    private boolean particlePairIsValidForRdfComputation(IParticle p1, IParticle p2) {
        boolean unEqual = p1 != p2 && p1.get_id() != p2.get_id();
        boolean fromValid = typeIdsToCheckFrom.contains(p1.get_type());
        boolean againstValid = typeIdsToBeChecked.contains(p2.get_type());

        //System.out.println("p1,p2: "+p1.get_type()+","+p2.get_type()+" unEqual"+ unEqual+" fromValid "+fromValid+" againsValid "+againstValid);

        return (unEqual && fromValid && againstValid);
    }
}
