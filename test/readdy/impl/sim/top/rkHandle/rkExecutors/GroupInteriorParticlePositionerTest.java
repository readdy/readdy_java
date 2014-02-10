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
package readdy.impl.sim.top.rkHandle.rkExecutors;

import readdy.impl.sim.core.particle.Particle;
import java.util.HashSet;
import readdy.api.sim.top.group.IGroupInteriorParticlePositioner;
import readdy.impl.assembly.GroupInteriorParticlePositionerFactory;
import readdy.api.assembly.IGroupInteriorParticlePositionerFactory;
import java.util.Set;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.api.assembly.IGroupParametersFactory;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author schoeneberg
 */
public class GroupInteriorParticlePositionerTest {

    private static IGroupParameters groupParameters;
    private static IGroupInteriorParticlePositioner particlePositioner;

    public GroupInteriorParticlePositionerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        //##############################################################################
        // create group Parameters ...
        //##############################################################################

        String filename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(filename);
        IParamGroupsFileData paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();

        //##############################################################################

        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_default/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        IGlobalParameters globalParameters = paramGlobalFileParser.get_globalParameters();

        System.out.println("parse ParamParticles");
        String paramParticlesFilename = "./test/testInputFiles/test_default/param_particles.xml";
        IParamParticlesFileParser paramParticlesFileParser = new ParamParticlesFileParser();
        paramParticlesFileParser.parse(paramParticlesFilename);
        IParamParticlesFileData paramParticlesFileData = paramParticlesFileParser.get_paramParticlesFileData();

        IParticleParametersFactory particleParamFactory = new ParticleParametersFactory();
        particleParamFactory.set_globalParameters(globalParameters);
        particleParamFactory.set_paramParticlesFileData(paramParticlesFileData);
        IParticleParameters particleParameters = particleParamFactory.createParticleParameters();

        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        //##############################################################################

        IGroupParametersFactory groupParametersFactory = new GroupParametersFactory();
        groupParametersFactory.set_paramGroupsFileData(paramGroupsFileData);
        groupParametersFactory.set_particleParameters(particleParameters);
        groupParametersFactory.set_potentialInventory(potentialInventory);
        groupParameters = groupParametersFactory.createGroupParameters();

        //##############################################################################
        // create GroupINteriorParticlePositioner
        //##############################################################################

        IGroupInteriorParticlePositionerFactory groupInteriorParticlePositionerFactory = new GroupInteriorParticlePositionerFactory();
        groupInteriorParticlePositionerFactory.set_groupParameters(groupParameters);
        particlePositioner = groupInteriorParticlePositionerFactory.createGroupInteriorParticlePositioner();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of positionParticles method, of class GroupInteriorParticlePositioner.
     */
    @Test
    public void testPositionParticles() {
        System.out.println("positionParticles");

        // ----------------------------------------------------------------------------
        // position particles for a dimer
        // ----------------------------------------------------------------------------
        System.out.println("positionParticles of a Dimer...");
        int groupTypeId = 0;  // dimer
        Set<IParticle> eductParticles = new HashSet();
        eductParticles.add(new Particle(0, 0, new double[]{0, 0, 0}));
        eductParticles.add(new Particle(1, 0, new double[]{0, 0, 0}));



        //ArrayList expResult = null;
        ArrayList<IParticle> positionedParticles = particlePositioner.positionParticles(groupTypeId, eductParticles);
        assertEquals(eductParticles.size(), positionedParticles.size());
        int counter = 0;
        for (IParticle p : positionedParticles) {
            System.out.println("counter: " + counter);
            p.print();
            counter++;
        }

        // ----------------------------------------------------------------------------
        // position particles for a tetramer
        // ----------------------------------------------------------------------------
        System.out.println("positionParticles of a Tetramer...");
        groupTypeId = 1;  // tetramer
        eductParticles = new HashSet();
        eductParticles.add(new Particle(0, 0, new double[]{0, 0, 0}));
        eductParticles.add(new Particle(1, 0, new double[]{0, 0, 0}));
        eductParticles.add(new Particle(2, 0, new double[]{0, 0, 0}));
        eductParticles.add(new Particle(3, 0, new double[]{0, 0, 0}));



        //ArrayList expResult = null;
        positionedParticles = particlePositioner.positionParticles(groupTypeId, eductParticles);
        assertEquals(eductParticles.size(), positionedParticles.size());
        counter = 0;
        for (IParticle p : positionedParticles) {
            System.out.println("counter: " + counter);
            p.print();
            counter++;
        }
    }
}
