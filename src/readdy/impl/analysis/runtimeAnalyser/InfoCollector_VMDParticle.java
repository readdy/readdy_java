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
 * This info collector writes a file that is a kind of 'hack' for VMD to enable
 * it to display a reaction diffusion trajectory where particles may be removed
 * or added. This possibility is not provided in VMD for xyz files.
 * For this reason, this class creates a setup, in which all particles that
 * may later occur during the trajectory are already present in the initial
 * snapshot. While the particles have not occurred in the real trajectory, they
 * are hid away in a hiding spot that can be defined by the user
 * .
 * @author schoeneberg
 */
public class InfoCollector_VMDParticle implements IReaddyRuntimeAnalyser {

    IParticleConfiguration particleConfiguration;
    int currentStep;
    boolean initiallySettedUp = false;
    boolean settedUp = false;
    boolean analysed = false;
    IDataReadyForOutput dataReadyForOutput;
    HashMap<Integer, Integer> typeId_to_numberOfParticles;
    HashMap<Integer, Integer> typeId_to_totalNumberOfParticlesToDisplay;
    int totalNParticlesToBeDisplayed;
    private String vmdAtomVDWradiusGuessPrefix = "C_";
    private IParticle invisibilityCloakParticle = new Particle(-1, 100, new double[]{0, 0, -20});
    private IParticle dummyParticle = new Particle(-1, -1, new double[]{0, 0, -20});

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

        // this is the datastructure where we store for which particle types 
        // dummy particles are necessary
        typeId_to_totalNumberOfParticlesToDisplay = new HashMap();

        // take the real numbers first and store it in the toDisplay map
        for (int pTypeId : typeId_to_numberOfParticles.keySet()) {
            typeId_to_totalNumberOfParticlesToDisplay.put(pTypeId, typeId_to_numberOfParticles.get(pTypeId));
        }

        // Overwrite the real numbers where necessary

        // MetaI = MetaII
        typeId_to_totalNumberOfParticlesToDisplay.put(0, Math.max(
                typeId_to_numberOfParticles.containsKey(0) ? typeId_to_numberOfParticles.get(0) : 0,
                typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0));
        /*System.out.println(Math.max(
                typeId_to_numberOfParticles.containsKey(0) ? typeId_to_numberOfParticles.get(0) : 0,
                typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0));*/
        // MetaI = MetaII
        typeId_to_totalNumberOfParticlesToDisplay.put(1, Math.max(
                typeId_to_numberOfParticles.containsKey(0) ? typeId_to_numberOfParticles.get(0) : 0,
                typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0));


        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // The following content is specific for the rod cell system
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        // this is only necesary if we have a source that produces
        // G as it is necessary in the erban chapman example.
        // otherwise it should be deactivated

        // #G = #RG precomplexes + #G 
        int nG = typeId_to_numberOfParticles.containsKey(3) ? typeId_to_numberOfParticles.get(3) : 0; 
        int nRG= typeId_to_numberOfParticles.containsKey(8) ? typeId_to_numberOfParticles.get(8) : 0; 
        
        // #R_rack
        int nRrack= typeId_to_numberOfParticles.containsKey(9) ? typeId_to_numberOfParticles.get(9) : 0; 
                                                        
        typeId_to_totalNumberOfParticlesToDisplay.put(3, nG+nRG);
        

        // #G* = #G
        typeId_to_totalNumberOfParticlesToDisplay.put(4, nG);
        // #R*Ggdp complex = #MetaII
        typeId_to_totalNumberOfParticlesToDisplay.put(5, typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0);
        // #R*G complex = #MetaII
        typeId_to_totalNumberOfParticlesToDisplay.put(6, typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0);
        // #R*G gtp complex = #MetaII
        typeId_to_totalNumberOfParticlesToDisplay.put(7, typeId_to_numberOfParticles.containsKey(1) ? typeId_to_numberOfParticles.get(1) : 0);
        // #RG precomplex = #G + #RG precomplex
        typeId_to_totalNumberOfParticlesToDisplay.put(8, nG+nRG);
        
        // #R_rack = #R_rack + #RG precomplex
        typeId_to_totalNumberOfParticlesToDisplay.put(9, nRrack+nRG);
        


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
