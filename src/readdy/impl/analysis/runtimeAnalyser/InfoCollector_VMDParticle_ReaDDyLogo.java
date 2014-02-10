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
import java.util.HashMap;
import java.util.Iterator;
import readdy.api.analysis.IReaddyRuntimeAnalyser;
import readdy.api.io.out.IDataReadyForOutput;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.rkHandle.IReactionExecutionReport;
import readdy.impl.io.out.DataReadyForOutput;
import readdy.impl.sim.core.particle.Particle;
import readdy.api.io.in.par_global.IGlobalParameters;

/**
 *
 * @author schoeneberg
 * this vmd output reader is designed for the sfb 958 summer school tutorial on 26.09.2012
 * and works precisely for the model system of the tutorial
 *
 * particle - type id - copiesStart - copiesMax
 *
 * letterParticle   -   0   - 426   -   426
 * sprayer_close    -   1   - 9     -   9
 * sprayer_open     -   2   - 0     -   9
 * predator0        -   3   - 20    -   200
 * prey             -   4   - 0     -   1000
 * predator1        -   5   - 0     -   200
 * predator2        -   6   - 0     -   200
 * predator3        -   7   - 0     -   200
 * predator4        -   8   - 0     -   200
 * predator5        -   9   - 0     -   200
 * predator6        -   10  - 0     -   200
 * predator7        -   11  - 0     -   200
 * predator8        -   12  - 0     -   200
 * predator9        -   13  - 0     -   200
 * predator10       -   14  - 0     -   200
 * predator0_dimer  -   15  - 0     -   200
 * 
 */
public class InfoCollector_VMDParticle_ReaDDyLogo implements IReaddyRuntimeAnalyser {

    IParticleConfiguration particleConfiguration;
    int currentStep;
    static boolean initiallySettedUp = false;
    boolean settedUp = false;
    boolean analysed = false;
    IDataReadyForOutput dataReadyForOutput;
    // user defined
    double[] cloakAndDummyParticleCoords = new double[]{500, -250, -60};
    HashMap<Integer, Integer> typeId_to_numberOfParticles;
    HashMap<Integer, Integer> typeId_to_totalNumberOfParticlesToDisplay;
    int totalNParticlesToBeDisplayed;
    // generate a cloak particle with type id 100
    private IParticle invisibilityCloakParticle = new Particle(-1, 100, cloakAndDummyParticleCoords);
    // generate a cloak particle with type id -1
    private IParticle dummyParticle = new Particle(-1, -1, cloakAndDummyParticleCoords);

    public void initial_setup(IParticleConfiguration particleConfiguration) {

        // put all particles from the lattice first into bins with respect to their
        // type Id. Put dummy particles if some particles numbers are different from
        // the initial step. Then create xyz coordinates from them
        // this is necessary because VMD takes the first step of a xyz trajectory to
        // reserve colors and set the particle number. Throughout the whole following
        // trajectory the same number of particles has to be present in every step.

        typeId_to_numberOfParticles = new HashMap();
        Iterator<IParticle> particleIterator = particleConfiguration.particleIterator();
        while (particleIterator.hasNext()) {
            IParticle p = particleIterator.next();
            int pTypeId = p.get_type();
            if (typeId_to_numberOfParticles.containsKey(pTypeId)) {
                int nParticles = typeId_to_numberOfParticles.get(pTypeId);
                typeId_to_numberOfParticles.put(pTypeId, nParticles + 1);
            } else {
                typeId_to_numberOfParticles.put(pTypeId, 1);
            }
        }

        // this is the datastructure where we store for which particle types dummy particles
        // are necessary
        typeId_to_totalNumberOfParticlesToDisplay = new HashMap();

        // take the real numbers first and store it in the toDisplay map
        for (int pTypeId : typeId_to_numberOfParticles.keySet()) {
            typeId_to_totalNumberOfParticlesToDisplay.put(pTypeId, typeId_to_numberOfParticles.get(pTypeId));
        }

        // Overwrite the real numbers where necessary

        typeId_to_totalNumberOfParticlesToDisplay.put(0,  426);  // letterParticle
        typeId_to_totalNumberOfParticlesToDisplay.put(1,  9);    // sprayer_close
        typeId_to_totalNumberOfParticlesToDisplay.put(2,  9);    // sprayer_open
        typeId_to_totalNumberOfParticlesToDisplay.put(3,  500);  // predator0
        typeId_to_totalNumberOfParticlesToDisplay.put(4,  5000); // prey
        typeId_to_totalNumberOfParticlesToDisplay.put(5,  500);  // predator1
        typeId_to_totalNumberOfParticlesToDisplay.put(6,  500);  // predator2
        typeId_to_totalNumberOfParticlesToDisplay.put(7,  500);  // predator3
        typeId_to_totalNumberOfParticlesToDisplay.put(8,  500);  // predator4
        typeId_to_totalNumberOfParticlesToDisplay.put(9,  500);  // predator5
        typeId_to_totalNumberOfParticlesToDisplay.put(10, 500);  // predator6
        typeId_to_totalNumberOfParticlesToDisplay.put(11, 500);  // predator7
        typeId_to_totalNumberOfParticlesToDisplay.put(12, 500);  // predator8
        typeId_to_totalNumberOfParticlesToDisplay.put(13, 500);  // predator9
        typeId_to_totalNumberOfParticlesToDisplay.put(14, 500);  // predator10
        typeId_to_totalNumberOfParticlesToDisplay.put(15, 500);  // predator0_dimer


        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



        // count the total number of particles that has to be displayed - this is a larger value than the number
        // of real particles
        int counter = 0;
        for (int pTypeId : typeId_to_totalNumberOfParticlesToDisplay.keySet()) {
            counter = counter + typeId_to_totalNumberOfParticlesToDisplay.get(pTypeId);
        }
        totalNParticlesToBeDisplayed = counter;
        // the invisibilityCloakParticle has to be counted as well
        totalNParticlesToBeDisplayed++;

        initiallySettedUp = true;
    }

    public void setup(int currentStep, IParticleConfiguration particleConfiguration, ArrayList<IReactionExecutionReport> rkReports, String[] specialFlags) {

        this.currentStep = currentStep;
        this.particleConfiguration = particleConfiguration;
        if (!initiallySettedUp) {
            initial_setup(particleConfiguration);
        }
        settedUp = true;

    }
    ArrayList<ArrayList<String>> doc = new ArrayList();

    public void analyse() {
        doc.clear();
        if (settedUp) {


            // put all particles from the lattice first into bins with respect to their
            // type Id. Put dummy particles if some particles numbers are different from
            // the initial step. Then create xyz coordinates from them
            // this is necessary because VMD takes the first step of a xyz trajectory to
            // reserve colors and set the particle number. Throughout the whole following
            // trajectory the same number of particles has to be present in every step.

            HashMap<Integer, ArrayList<IParticle>> pTypeId_to_listOfRealParticlesToDisplay = new HashMap();
            Iterator<IParticle> particleIterator = particleConfiguration.particleIterator();
            while (particleIterator.hasNext()) {
                IParticle p = particleIterator.next();
                int pTypeId = p.get_type();
                if (pTypeId_to_listOfRealParticlesToDisplay.containsKey(pTypeId)) {
                    pTypeId_to_listOfRealParticlesToDisplay.get(pTypeId).add(p);
                } else {
                    ArrayList<IParticle> list = new ArrayList();
                    list.add(p);
                    pTypeId_to_listOfRealParticlesToDisplay.put(pTypeId, list);
                }
            }

            // now, all real particles are assigned
            // display now the real particles and fill up the gaps with dummy particles hidden somewhere
            for (int pTypeId : typeId_to_totalNumberOfParticlesToDisplay.keySet()) {
                ArrayList<IParticle> realParticleList = pTypeId_to_listOfRealParticlesToDisplay.containsKey(pTypeId) ? pTypeId_to_listOfRealParticlesToDisplay.get(pTypeId) : new ArrayList();
                int nRealParticles = realParticleList.size();
                for (int i = 0; i < nRealParticles; i++) {
                    // print the REAL particle
                    ArrayList<String> line = new ArrayList();
                    IParticle p = realParticleList.get(i);
                    line.add(p.get_id() + "");
                    line.add(p.get_type() + "");
                    for (double c : p.get_coords()) {
                        line.add(c + "");
                    }

                    doc.add(line);

                }
                // if there is space left - fill it with dummy particles:
                int totalNParticlesToDisplay = typeId_to_totalNumberOfParticlesToDisplay.get(pTypeId);
                int nPlacesLeftForDummyParticles = totalNParticlesToDisplay - nRealParticles;
                for (int i = 0; i < nPlacesLeftForDummyParticles; i++) {
                    // print a DUMMY Particle ...
                    ArrayList<String> line = new ArrayList();
                    IParticle p = dummyParticle;
                    line.add(p.get_id() + "");
                    line.add(pTypeId + "");
                    for (double c : p.get_coords()) {
                        line.add(c + "");
                    }

                    doc.add(line);
                }
            }

            // print the invisibilityCloak particle
            ArrayList<String> lineArr = new ArrayList();
            IParticle p = invisibilityCloakParticle;
            lineArr.add(p.get_id() + "");
            lineArr.add(p.get_type() + "");
            for (double c : p.get_coords()) {
                lineArr.add(c + "");
            }

            doc.add(lineArr);


            dataReadyForOutput = new DataReadyForOutput(doc);

            if (doc.size() < totalNParticlesToBeDisplayed) {
                throw new RuntimeException("VMD XYZ analyser says: \"we wanted to display " + totalNParticlesToBeDisplayed + " but we show only " + doc.size() + " \"");
            }

            analysed = true;
        } else {
            throw new RuntimeException("not setted up yet");
        }
    }

    public IDataReadyForOutput getOutputData() {
        if (analysed) {
            return dataReadyForOutput;
        } else {
            throw new RuntimeException("not analysed yet");
        }

    }

    public void set_globalParameters(IGlobalParameters globalParameters) {
        //   throw new UnsupportedOperationException("Not supported yet.");
    }
}
