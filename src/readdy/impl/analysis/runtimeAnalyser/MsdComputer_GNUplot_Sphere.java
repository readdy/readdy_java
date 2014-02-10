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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.impl.tools.geometry.SphericalGeometry;
import statlab.base.util.DoubleArrays;

/**
 *
 * @author schoeneberg
 */
public class MsdComputer_GNUplot_Sphere implements IReaddyRuntimeAnalyser {

    IParticleParameters particleParameters;
    IGlobalParameters globalParameters;
    private IParticleConfiguration particleConfiguration;
    private static HashMap<Integer, double[]> initialConfiguration;
    private int currentTimestep;
    private boolean initialConfigurationSet = false;
    ArrayList<ArrayList<String>> doc = new ArrayList();
    private static final boolean verbose = false;
    static boolean firstWritingToFile = true;
    HashMap<Integer, Integer> particleTypeId2ParticleCounts_map = new HashMap();
    HashMap<Integer, String> particleTypeId2ParticleName_map = new HashMap();
    ArrayList<Integer> particleTypeIds = new ArrayList();
    private double sphereRadius = 0;

    public MsdComputer_GNUplot_Sphere(IParticleParameters particleParameters) {
        this.particleParameters = particleParameters;

        HashSet<Integer> particleTypes = particleParameters.getAllParticleTypes();
        Iterator<Integer> iterator = particleTypes.iterator();
        while (iterator.hasNext()) {
            int particleTypeId = iterator.next();
            particleTypeIds.add(particleTypeId);

            particleTypeId2ParticleCounts_map.put(particleTypeId, 0);
            particleTypeId2ParticleName_map.put(particleTypeId, particleParameters.getParticleTypeNameFromTypeId(particleTypeId));
        }
        // ids come from the hash set in an unsorted fashion.
        Collections.sort(particleTypeIds);

        doc.clear();
        ArrayList<String> headersLine = new ArrayList();
        headersLine.add("HEADER");
        headersLine.add("timeStep");

        ArrayList<String> commentIdLine = new ArrayList();
        commentIdLine.add("#");
        commentIdLine.add("typeIds");
        for (int i = 0; i < particleTypeIds.size(); i++) {
            headersLine.add(particleTypeId2ParticleName_map.get(particleTypeIds.get(i)));
            commentIdLine.add(particleTypeIds.get(i) + "");
        }

        doc.add(commentIdLine);
        doc.add(headersLine);
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {
        if (allInputAvailable()) {
            this.currentTimestep = currentStep;
            this.particleConfiguration = particleConfiguration;
            this.sphereRadius = parseSpecialFlags(MSD_Computer_Sphere_Special_Flags.SPHERE_RADIUS, specialFlags);
            if (!initialConfigurationSet) {

                set_initialConfiguration();
                initialConfigurationSet = true;
            }
        } else {
            throw new RuntimeException("not all input available");
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
     *
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
        return new DataReadyForOutput(doc);
    }

    public void setParticleConfiguration(IParticleConfiguration particleConfiguration) {
        this.particleConfiguration = particleConfiguration;
        System.out.println("take the current configuration as start for msd computation");



    }

    public void analyse() {
        if (firstWritingToFile) {
            firstWritingToFile = false;
        } else {
            doc.clear();
        }

        
        HashMap<Integer, Double> typeId2Msd_map = new HashMap();
        HashMap<Integer, Integer> typeId2couter_map = new HashMap();

        Iterator<IParticle> iter = particleConfiguration.particleIterator();

        while (iter.hasNext()) {

            IParticle p = iter.next();
            double dist = 0;

            if (initialConfiguration.containsKey(p.get_id())) {
                dist = SphericalGeometry.distance(initialConfiguration.get(p.get_id()), p.get_coords(),sphereRadius);
            } else {
                addParticleToInitialConfiguration(p);
            }

            if (typeId2Msd_map.containsKey(p.get_type())) {
                typeId2Msd_map.put(p.get_type(), typeId2Msd_map.get(p.get_type()) + dist * dist);
            } else {
                typeId2Msd_map.put(p.get_type(), dist * dist);
            }

            if (typeId2couter_map.containsKey(p.get_type())) {
                typeId2couter_map.put(p.get_type(), typeId2couter_map.get(p.get_type()) + 1);
            } else {
                typeId2couter_map.put(p.get_type(), 1);
            }

        }

        for (int typeId : typeId2Msd_map.keySet()) {
            typeId2Msd_map.put(typeId, typeId2Msd_map.get(typeId) / typeId2couter_map.get(typeId));
            System.out.println("MSD(XYZ) typeId:" + typeId + ": " + typeId2Msd_map.get(typeId) + " nParticles " + typeId2couter_map.get(typeId));

        }


        //msd_xyz = msd_xyz / counter;
        //msd_xy = msd_xy / counter;

        //System.out.println("MSD(XYZ)R: " + msd_xyz+" nParticles "+counter);
        //System.out.println("MSD(XY)R: " + msd_xy+" nParticles "+counter);
        //System.out.println("MSD(XYZ)G: " + msd_xyz+" nParticles "+counter);
        //System.out.println("MSD(XY)G: " + msd_xy+" nParticles "+counter);

        ArrayList<String> line = new ArrayList();

        for (int typeId : particleTypeIds) {
            // ordinary 3D diffusion

            if (typeId2Msd_map.containsKey(typeId)) {
                line.add(typeId2Msd_map.get(typeId) + "");
            } else {
                line.add(0 + "");
            }

            //line.add(msd_xyz + "");
            //line.add(msd_xy + "");
        }
            doc.add(line);
        
    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    private boolean allInputAvailable() {
        return particleParameters != null;
    }

    private double parseSpecialFlags(MSD_Computer_Sphere_Special_Flags flagTag, String[] specialFlags) {
        switch (flagTag) {
            case SPHERE_RADIUS:
                if (specialFlags != null && specialFlags.length != 0) {
                    for (String flag : specialFlags) {
                        String[] splittedFlag = flag.split("=");
                        if (splittedFlag.length == 2 && splittedFlag[0].equals("sphereRadius")) {
                            return Double.parseDouble(splittedFlag[1]);
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("special flag not recognized");


        }
        throw new RuntimeException("special flag could not be parsed");
    }
}

/**
 *
 * @author johannesschoeneberg
 */
enum MSD_Computer_Sphere_Special_Flags {

    SPHERE_RADIUS
}