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

/**
 *
 * @author schoeneberg
 */
public class ParticleDensityAlongAxisComputer implements IReaddyRuntimeAnalyser {

    // global parameters
    int nBins = 1000;
    double binWidth = 100; // nm (rhodopsin radius is 2nm)
    int axis = -1; // 0,1,2 for x,y,z
    /**
     * typeIdsToCheckFrom is the set of particleType Ids from
     * which we compute our rdf from.
     * E.G. We only sit on R* to check all G's around it
     * the Type Id of R* would be in typeIdsToCheckFrom
     * the type Id of G would be in the typeIdsToBeChecked
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
    private static final boolean verbose = false;
    IGlobalParameters globalParameters = null;
    double dimensionStartValue;
    double dimensionEndValue;
    double dimensionWidth;

    /**
     * within the lattice dimensions i bin particles along a specified axis
     */
    public ParticleDensityAlongAxisComputer() {
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {

        if (specialFlags.length == 3) {
            // these particle type ids are checked for particle density
            String typeIdsToCheck_rawString = specialFlags[0];
            String axis_rawString = specialFlags[1];
            String binWidth_rawString = specialFlags[2];

            typeIdsToCheck = parseTypeIdSet_rawString(typeIdsToCheck_rawString);
            for (int typeId : typeIdsToCheck) {
                System.out.println("particles to check  " + typeId);
            }

            if (axis_rawString.equals("X")) {
                axis = 0;
            } else {
                if (axis_rawString.equals("Y")) {
                    axis = 1;
                } else {
                    if (axis_rawString.equals("Z")) {
                        axis = 2;
                    } else {
                        throw new RuntimeException("axisString '" + axis_rawString + "' not supported. (supported are 'X','Y','Z'");
                    }
                }
            }

            binWidth = Double.parseDouble(binWidth_rawString);

            double[][] latticeBounds = globalParameters.get_latticeBounds();
            dimensionStartValue = latticeBounds[axis][0];
            dimensionEndValue = latticeBounds[axis][1];
            dimensionWidth = Math.abs(latticeBounds[axis][1] - latticeBounds[axis][0]);
            nBins = (int) Math.ceil(dimensionWidth / binWidth);


            System.out.println("ParticleDensityAlongAxisComputer.setup():\n"
                    + " compute along " + axis_rawString + "-axis "
                    + "from " + dimensionStartValue + " to " + dimensionEndValue + " with bin width " + dimensionWidth + " nm");

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


        Iterator<IParticle> iter = particleConfiguration.particleIterator();//particleConfiguration.particleIterator());


        // bin particle numbers along axis
        int counterPairsThatWentIntoTheRdfComputation = 0;
        while (iter.hasNext()) {
            // particle to compute from
            IParticle p = iter.next();

            if (typeIdsToCheck.contains(p.get_type())) {
                double relevantCoordDimension = p.get_coords()[axis];

                int binId = getBinIdToIncrement(relevantCoordDimension, binWidth, nBins);
                particleDensity[binId]++;
                counterPairsThatWentIntoTheRdfComputation++;
            }
        }

        // normalize over the number of observed particles
        for (int i = 0; i < particleDensity.length; i++) {
            particleDensity[i] = particleDensity[i] / counterPairsThatWentIntoTheRdfComputation;
        }


        //output
        ArrayList<String> line = new ArrayList();
        for (int i = 0; i < particleDensity.length; i++) {
            if (!line.isEmpty()) {
                output.add(line);
            }
            line = new ArrayList();
            line.add(currentTimestep * globalParameters.get_dt() + "");
            String coordVsDensity = dimensionStartValue + ((i + 0.5) * binWidth) + ", " + particleDensity[i];
            line.add(coordVsDensity);
        }
        line.add("\n\n");
        output.add(line);

    }

    private int getBinIdToIncrement(double coord, double binWidth, int nBins) {
        for (int i = 0; i < nBins; i++) {
            if (coord >= dimensionStartValue + (i) * binWidth
                    && coord < dimensionStartValue + (i + 1) * binWidth) {
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
}
