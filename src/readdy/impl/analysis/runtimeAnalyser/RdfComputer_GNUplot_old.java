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
public class RdfComputer_GNUplot_old implements IReaddyRuntimeAnalyser {

    // global parameters
    int nBins = 1000;
    double binWidth = 100; // nm (rhodopsin radius is 2nm)
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
    double sphereOrDiskRadius;
    double maxDistanceOnSphere;
    private int currentTimestep;
    ArrayList<ArrayList<String>> output = new ArrayList();
    private static final boolean verbose = false;
    IGlobalParameters globalParameters = null;

    public RdfComputer_GNUplot_old() {
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {

        if (specialFlags.length == 4) {
            String typeIdsToCheckFrom_rawString = specialFlags[0];
            String typeIdsToBeChecked_rawString = specialFlags[1];
            String nBins_rawString = specialFlags[2];
            String geometry_rawString = specialFlags[3];

            nBins = Integer.parseInt(nBins_rawString);

            String[] geometryRawStringParts = geometry_rawString.split(":");
            if (geometryRawStringParts.length == 2) {
                sphericalSurface = geometryRawStringParts[0].equals("sphere");
                sphereOrDiskRadius = Double.parseDouble(geometryRawStringParts[1]);
                if (sphericalSurface) {
                    maxDistanceOnSphere = Math.PI * sphereOrDiskRadius;
                } else {
                    maxDistanceOnSphere = 2 * sphereOrDiskRadius;
                }
                binWidth = maxDistanceOnSphere / nBins;
            } else {
                throw new RuntimeException("this is not implemented.");
            }

            System.out.println("RdfComputer.setup(): sphericalSurface=" + sphericalSurface + "\n"
                    + " r[nm]:" + sphereOrDiskRadius + " maxDistanceOnSphere[nm]: " + maxDistanceOnSphere + " nBins: " + nBins + " binWidth[nm]: " + binWidth);
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
                    double dist = 0;
                    if (sphericalSurface) {
                        dist = getArcLengthBetweenTwoCartesianVectorsOnSphere_2(p1.get_coords(), p2.get_coords(), sphereOrDiskRadius);
                    } else {
                        dist = DoubleArrays.distance(p1.get_coords(), p2.get_coords());
                    }
                    if (Double.isNaN(dist)) {

                        throw new RuntimeException("this is bad");
                    }
                    //System.out.println("dist: "+dist);
                    int binId = getBinIdToIncrement(dist, binWidth, nBins);
                    if (binId != -1) {
                        rdf[binId]++;
                        counterPairsThatWentIntoTheRdfComputation++;
                    }
                }
            }
        }

        // normalize over the number of observed particles
        for (int i = 0; i < rdf.length; i++) {
            rdf[i] = rdf[i] / counterPairsThatWentIntoTheRdfComputation;
        }

        // normalize over the surface:
        // to get probability 1 everywhere.
        if (sphericalSurface) {
            // normalize over the sphere stripe area within a bin width
            // on the equator the sphere is thicker than around the poles
            double[] sphericalStripesAreaArray = computeSphericalStripesAreaArray(binWidth, nBins, sphereOrDiskRadius);
            double totalSphereArea = 4 * Math.PI * sphereOrDiskRadius * sphereOrDiskRadius;
            for (int i = 0; i < rdf.length; i++) {
                rdf[i] = rdf[i] * totalSphereArea / sphericalStripesAreaArray[i];
            }
        } else {
            double[] diskStripesAreaArray = computeDiskStripesAreaArray(binWidth, nBins, sphereOrDiskRadius);
            double totalDiskArea = Math.PI * sphereOrDiskRadius * sphereOrDiskRadius;
            for (int i = 0; i < rdf.length; i++) {
                rdf[i] = rdf[i] * totalDiskArea / diskStripesAreaArray[i];
            }
        }



        //System.out.println("currTimestep+"+currentTimestep);
        //line.add(currentTimestep + "");

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

    private double[] computeSphericalStripesAreaArray(double binWidth, int nBins, double radius) {
        // this array holds all spherical calottes from 0-0, 0-1*binWidth, 0-2*binWidth...
        double[] kalottesAreaArray = new double[nBins + 1];
        for (int i = 0; i < kalottesAreaArray.length; i++) {
            double distanceFromStandpoint = binWidth * i;
            double centriAngleBtwnStandpointAndBin = (distanceFromStandpoint - 0) / radius;
            kalottesAreaArray[i] = calotteArea(centriAngleBtwnStandpointAndBin, radius);
        }

        // compute the difference between calottes. The larger one minus the by one smaller one
        double[] sphericalStripesAreaArray = new double[nBins];
        for (int i = 0; i < sphericalStripesAreaArray.length; i++) {
            sphericalStripesAreaArray[i] = kalottesAreaArray[i + 1] - kalottesAreaArray[i];
        }

        return sphericalStripesAreaArray;

    }

    public double calotteArea(double alpha, double r) {
        // we do not divide by 2 in the alpha because we want to
        // compute the eintire sphere

        return 2 * r * r * Math.PI * (1 - Math.cos(alpha));
    }

    private double[] computeDiskStripesAreaArray(double binWidth, int nBins, double radius) {
        // this array holds all disk areas from 0-0, 0-1*binWidth, 0-2*binWidth...
        double[] diskAreaArray = new double[nBins + 1];
        for (int i = 0; i < diskAreaArray.length; i++) {
            double fractionOfMaxDistance = binWidth * i;
            double diskArea = Math.PI * fractionOfMaxDistance * fractionOfMaxDistance;
            diskAreaArray[i] = diskArea;
        }

        // compute the difference between calottes. The larger one minus the by one smaller one
        double[] diskStripesAreaArray = new double[nBins];
        for (int i = 0; i < diskStripesAreaArray.length; i++) {
            diskStripesAreaArray[i] = diskAreaArray[i + 1] - diskAreaArray[i];
        }

        return diskStripesAreaArray;
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

    public double getArcLengthBetweenTwoCartesianVectorsOnSphere_2(double[] V1, double[] V2, double radius) {
        //System.out.println(V1.length()+" "+V2.length());
        double x1, y1, z1, x2, y2, z2;
        x1 = V1[0];
        y1 = V1[1];
        z1 = V1[2];

        x2 = V2[0];
        y2 = V2[1];
        z2 = V2[2];

        if (V1.equals(V2)) {
            return 0;
        } else {

            double value = (x1 * x2 + y1 * y2 + z1 * z2) / (DoubleArrays.norm(V1) * DoubleArrays.norm(V2));

            double angle = Math.acos(value);


            if (Double.isNaN(angle)) {
                System.out.println("acos(" + value + ")");
                System.out.println("angle " + angle);
                System.out.println("V1 " + x1 + " " + y1 + " " + z1);
                System.out.println("V2 " + x2 + " " + y2 + " " + z2);
            }


            //System.out.println("radius "+ radius);
            double arcLength = angle * radius;

            //System.out.println("arc length "+ arcLength);
            return arcLength;
        }
    }
}
