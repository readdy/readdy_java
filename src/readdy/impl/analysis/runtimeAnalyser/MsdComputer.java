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
import java.util.Iterator;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import statlab.base.util.DoubleArrays;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 */
public class MsdComputer implements IReaddyRuntimeAnalyser {

    IGlobalParameters globalParameters;
    private IParticleConfiguration particleConfiguration;
    private static HashMap<Integer, double[]> initialConfiguration;
    private int currentTimestep;
    private boolean initialConfigurationSet = false;
    ArrayList<ArrayList<String>> output = new ArrayList();
    private static final boolean verbose = false;

    public MsdComputer() {
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        this.currentTimestep = currentStep;
        this.particleConfiguration = particleConfiguration;
        if (!initialConfigurationSet) {

            set_initialConfiguration();
            initialConfigurationSet = true;
        }
    }

    public void set_initialConfiguration() {
        currentTimestep = 0;
        Iterator<IParticle> iter = particleConfiguration.particleIterator();
        initialConfiguration = new HashMap<Integer, double[]>();
        while (iter.hasNext()) {
            IParticle p = iter.next();
            initialConfiguration.put(p.get_id(), DoubleArrays.copy(p.get_coords()));
            if (verbose) {
                DoubleArrays.print(p.get_coords());
                System.out.println();
            }
        }
        initialConfigurationSet = true;
    }

    /**
     * This becomes necessary if we have a reaction that just creates particles
     * out of thin air.
     * @param p
     */
    private void addParticleToInitialConfiguration(IParticle p) {
        initialConfiguration.put(p.get_id(), DoubleArrays.copy(p.get_coords()));
        if (verbose) {
            DoubleArrays.print(p.get_coords());
            System.out.println();
        }
    }

    public IDataReadyForOutput getOutputData() {
        return new DataReadyForOutput(output);
    }

    public void setParticleConfiguration(IParticleConfiguration particleConfiguration) {
        this.particleConfiguration = particleConfiguration;
        System.out.println("take the current configuration as start for msd computation");



    }

    public void analyse() {
        boolean compute2DDiffusionAsWell = false;
        double dist_2d = 0;
        ArrayList<Integer> typeIdToAnalyse = new ArrayList();
        typeIdToAnalyse.add(2); // R
        typeIdToAnalyse.add(3); // G
        HashMap<Integer, Double> typeId2Msd_map = new HashMap();
        HashMap<Integer, Double> typeId2Msd2D_map = new HashMap();
        HashMap<Integer, Integer> typeId2couter_map = new HashMap();

        output.clear();
        //int typeIdToAnalyse = 2;

        Iterator<IParticle> iter = particleConfiguration.particleIterator();
        // compute msd
        // reset all variables
        for (int typeId : typeIdToAnalyse) {
            typeId2Msd_map.put(typeId, 0.0);
            typeId2Msd2D_map.put(typeId, 0.0);
            typeId2couter_map.put(typeId, 0);
        }
        //int counter = 0;
        //double msd_xyz = 0;
        //double msd_xy = 0;
        while (iter.hasNext()) {

            IParticle p = iter.next();
            double dist = 0;
            if (typeIdToAnalyse.contains(p.get_type())) {
                if (initialConfiguration.containsKey(p.get_id())) {
                    dist = DoubleArrays.distance(initialConfiguration.get(p.get_id()), p.get_coords());
                } else {
                    addParticleToInitialConfiguration(p);
                }
                if (compute2DDiffusionAsWell) {
                    double[] aCoord = new double[]{initialConfiguration.get(p.get_id())[0], initialConfiguration.get(p.get_id())[1]};
                    double[] bCoord = new double[]{p.get_coords()[0], p.get_coords()[1]};
                    dist_2d = DoubleArrays.distance(aCoord, bCoord);
                }

                //msd_xyz += dist * dist;
                // msd_xy += dist_2d * dist_2d;
                //counter++;
                typeId2Msd_map.put(p.get_type(), typeId2Msd_map.get(p.get_type()) + dist * dist);
                if (compute2DDiffusionAsWell) {
                    typeId2Msd2D_map.put(p.get_type(), typeId2Msd2D_map.get(p.get_type()) + dist_2d * dist_2d);
                }
                typeId2couter_map.put(p.get_type(), typeId2couter_map.get(p.get_type()) + 1);
            }
        }

        for (int typeId : typeIdToAnalyse) {
            typeId2Msd_map.put(typeId, typeId2Msd_map.get(typeId) / typeId2couter_map.get(typeId));
            System.out.println("MSD(XYZ) typeId:" + typeId + ": " + typeId2Msd_map.get(typeId) + " nParticles " + typeId2couter_map.get(typeId));
            if (compute2DDiffusionAsWell) {
                typeId2Msd2D_map.put(typeId, typeId2Msd2D_map.get(typeId) / typeId2couter_map.get(typeId));
                System.out.println("MSD(XY) typeId:" + typeId + ": " + typeId2Msd2D_map.get(typeId) + " nParticles " + typeId2couter_map.get(typeId));
            }
        }


        //msd_xyz = msd_xyz / counter;
        //msd_xy = msd_xy / counter;

        //System.out.println("MSD(XYZ)R: " + msd_xyz+" nParticles "+counter);
        //System.out.println("MSD(XY)R: " + msd_xy+" nParticles "+counter);
        //System.out.println("MSD(XYZ)G: " + msd_xyz+" nParticles "+counter);
        //System.out.println("MSD(XY)G: " + msd_xy+" nParticles "+counter);

        ArrayList<String> line = new ArrayList();
        //System.out.println("currTimestep+"+currentTimestep);
        line.add(currentTimestep + "");
        line.add(currentTimestep * globalParameters.get_dt() + "");
        //from alex 25.09.2012
        int max = 0;
        for (int typeId : typeId2Msd_map.keySet()) {
            if (typeId > max) //could be left out actually
            {
                max = typeId;
            }
        }
        for (int tc = 0; tc < max; tc++) {
            if (!typeId2Msd_map.containsKey(tc)) {
                typeId2Msd_map.put(tc, 0.);
            }
        }
        //
        for (int typeId : typeIdToAnalyse) {
            line.add(typeId2Msd_map.get(typeId) + "");
            if (compute2DDiffusionAsWell) {
                line.add(typeId2Msd2D_map.get(typeId) + "");
            }
        }
        //line.add(msd_xyz + "");
        //line.add(msd_xy + "");

        output.add(line);


    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }
}
