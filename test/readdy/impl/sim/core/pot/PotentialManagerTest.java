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
package readdy.impl.sim.core.pot;

import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.potentials.IPotential1;
import readdy.api.sim.core.pot.potentials.IPotential2;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.impl.sim.core.particle.Particle;

/**
 *
 * @author schoeneberg
 */
public class PotentialManagerTest {
    private static IParticleParameters particleParameters;
    private static IPotentialManager potentialManager;


    public PotentialManagerTest() {
    }


    @BeforeClass
    public static void setUpClass() throws Exception {


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
        particleParameters= particleParamFactory.createParticleParameters();

        //##############################################################################

        IPotentialFactory potentialFactory = new PotentialFactory();
        IPotentialInventoryFactory potInvFactory = new PotentialInventoryFactory();
        potInvFactory.set_potentialFactory(potentialFactory);
        IPotentialInventory potentialInventory = potInvFactory.createPotentialInventory();

        String tplgyPotentialFilename = "./test/testInputFiles/test_default/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();
        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        potentialManager = potentialManagerFactory.createPotentialManager();

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
     * Test of getPotentials method, of class PotentialManager.
     */
    @Test
    public void testGetPotentials_IParticle() {
        System.out.println("getPotentials");
        // id, typeId, coords[]
        IParticle p = new Particle(0, 0, new double[]{0, 0, 0});
        //Iterator expResult = ;
        String[] expNames = new String[]{""};
        int counter = 0;
        Iterator<IPotential1> result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;
        }
        assertEquals(1, counter);

        //######## ######## ######## ######## ######## ########

        p = new Particle(1, 0, new double[]{0, 0, 0});
        //Iterator expResult = ;
        expNames = new String[]{"attractiveDisk_1"};
        boolean[] found = new boolean[expNames.length];
        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;
            for (int i = 0; i < expNames.length; i++) {

                if (expNames[i].contentEquals(pot.get_name())) {
                    found[i] = true;
                }

            }
        }
        assertEquals(1, counter);
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);

        }

        //######## ######## ######## ######## ######## ########

        p = new Particle(2, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;
        expNames = new String[]{"attractiveCylinder_1"};
        found = new boolean[expNames.length];
        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;
            for (int i = 0; i < expNames.length; i++) {

                if (expNames[i].contentEquals(pot.get_name())) {
                    found[i] = true;
                }

            }
        }
        assertEquals(1, counter);
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);

        }

        //######## ######## ######## ######## ######## ########

        p = new Particle(1000, 0, new double[]{0, 0, 0});
        //Iterator expResult = ;
        expNames = new String[]{"attractiveDisk_1", "attractiveCylinder_1"};
        found = new boolean[expNames.length];
        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.println("found potential: "+pot.get_name());
            counter++;
            for (int i = 0; i < expNames.length; i++) {

                if (expNames[i].contentEquals(pot.get_name())) {
                    found[i] = true;
                }

            }
        }
        assertEquals(2, counter);
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);

        }


    }

    /**
     * Test of getPotentials method, of class PotentialManager.
     */
    @Test
    public void testGetPotentials_IParticle_IParticle() {
        System.out.println("getPotentials 2");
        IParticle p1, p2;
        String[] expNames;
        boolean[] found;
        int counter, expNumberOfPotentials;
        Iterator<IPotential2> result;

        //######## ######## ######## ######## ######## ########

        p1 = new Particle(10, 0, new double[]{0, 0, 0});
        p2 = new Particle(11, 0, new double[]{0, 0, 0});
        //Iterator expResult = ;
        expNames = new String[]{"harmonic_idBound_repulsive"};
        expNumberOfPotentials = 1;
        found = new boolean[expNames.length];
        counter = 0;
        result = potentialManager.getPotentials(p1, p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            counter++;
            for (int i = 0; i < expNames.length; i++) {

                if (expNames[i].contentEquals(pot.get_name())) {
                    found[i] = true;
                }

            }
        }
        assertEquals(expNumberOfPotentials, counter);
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);

        }

        //######## ######## ######## ######## ######## ########

        p1 = new Particle(3, 1, new double[]{0, 0, 0});
        p2 = new Particle(4, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;
        expNames = new String[]{"harmonic_typeBound_attractive", "harmonic_typeBound_spring"};
        expNumberOfPotentials = 2;
        found = new boolean[expNames.length];
        counter = 0;
        result = potentialManager.getPotentials(p1, p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            counter++;
            System.out.println(pot.get_name());
            for (int i = 0; i < expNames.length; i++) {
                
                if (expNames[i].contentEquals(pot.get_name())) {
                    found[i] = true;
                }

            }
        }
        assertEquals(expNumberOfPotentials, counter);
        for (int i = 0; i < found.length; i++) {
            assertEquals(true, found[i]);

        }
    }

    /**
     * Test of addPotentialByType method, of class PotentialManager.
     */
    @Test
    public void testAddPotentialByType_int_int() {
        System.out.println("addPotentialByType");
        int particleTypeId = 1;
        int potId = 0;

        //######## ######## ######## ######## ######## ########

        IParticle p = new Particle(0, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential1> result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;

        }
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByType(particleTypeId, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;

        }
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore + 1, nPotentialsAfter);

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByType(particleTypeId, potId);

        //######## ######## ######## ######## ######## ########
        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;

        }
        int nPotentialsAfterAfter = counter;

        assertEquals(nPotentialsAfter, nPotentialsAfterAfter);


    }

    /**
     * Test of addPotentialByID method, of class PotentialManager.
     */
    @Test
    public void testAddPotentialByID_int_int() {
        System.out.println("addPotentialByID");
        int particleId = 0;
        int potId = 1;

        //######## ######## ######## ######## ######## ########

        IParticle p = new Particle(0, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential1> result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
         System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByID(particleId, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
         System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore + 1, nPotentialsAfter);

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByID(particleId, potId);

        //######## ######## ######## ######## ######## ########
        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
         System.out.println();
        int nPotentialsAfterAfter = counter;

        assertEquals(nPotentialsAfter, nPotentialsAfterAfter);

    }

    /**
     * Test of addPotentialByType method, of class PotentialManager.
     */
    @Test
    public void testAddPotentialByType_3args() {
        System.out.println("testAddPotentialByType_3args");
        int particleTypeId1 = 0;
        int particleTypeId2 = 1;
        int potId = 3;

        //######## ######## ######## ######## ######## ########

        IParticle p1 = new Particle(5, 0, new double[]{0, 0, 0});
        IParticle p2 = new Particle(6, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential2> result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByType(particleTypeId1,particleTypeId2, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore + 1, nPotentialsAfter);

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByType(particleTypeId1,particleTypeId2, potId);

        //######## ######## ######## ######## ######## ########
        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfterAfter = counter;

        assertEquals(nPotentialsAfter, nPotentialsAfterAfter);

    }

    /**
     * Test of addPotentialByID method, of class PotentialManager.
     */
    @Test
    public void testAddPotentialByID_3args() {
        System.out.println("testAddPotentialByID_3args");
        int particleId1 = 10;
        int particleId2 = 11;
        int potId = 2;

        //######## ######## ######## ######## ######## ########

        IParticle p1 = new Particle(10, 0, new double[]{0, 0, 0});
        IParticle p2 = new Particle(11, 0, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential2> result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.println("pot name: "+pot.get_name());
            System.out.println(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByID(particleId1,particleId2, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.println("pot name: "+pot.get_name());
            System.out.println(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore + 1, nPotentialsAfter);

        //######## ######## ######## ######## ######## ########

        potentialManager.addPotentialByID(particleId1,particleId2, potId);

        //######## ######## ######## ######## ######## ########
        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfterAfter = counter;

        assertEquals(nPotentialsAfter, nPotentialsAfterAfter);

    }

    /**
     * Test of removePotentialByType method, of class PotentialManager.
     */
    @Test
    public void testRemovePotentialByType_int_int() {
        System.out.println("removePotentialByType");
       int particleTypeId = 1;
        int potId = 0;

        //######## ######## ######## ######## ######## ########

        IParticle p = new Particle(0, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential1> result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;

        }
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.removePotentialByType(particleTypeId, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            counter++;

        }
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore - 1, nPotentialsAfter);
    }

    /**
     * Test of removePotentialByID method, of class PotentialManager.
     */
    @Test
    public void testRemovePotentialByID_int_int() {
        System.out.println("-----removePotentialByID");
        int particleId = 1000;
        int potId = 1;

        //######## ######## ######## ######## ######## ########

        IParticle p = new Particle(1000, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential1> result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.println("pot name: "+pot.get_name()+" | potId: "+pot.get_id());
            counter++;

        }
         System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        System.out.println("remove from particle Id '"+particleId+"' potential ID '"+potId+"' ...");
        System.out.println("");
        potentialManager.removePotentialByID(particleId, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p);
        while (result.hasNext()) {
            IPotential1 pot = result.next();
            System.out.println("pot name: "+pot.get_name()+" | potId: "+pot.get_id());
            counter++;

        }
         System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore - 1, nPotentialsAfter);
    }

    /**
     * Test of removePotentialByType method, of class PotentialManager.
     */
    @Test
    public void testRemovePotentialByType_3args() {
        System.out.println("removePotentialByType");
        int particleTypeId1 = 0;
        int particleTypeId2 = 1;
        int potId = 3;

        //######## ######## ######## ######## ######## ########

        IParticle p1 = new Particle(5, 0, new double[]{0, 0, 0});
        IParticle p2 = new Particle(6, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential2> result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.removePotentialByType(particleTypeId1,particleTypeId2, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore - 1, nPotentialsAfter);

        
    }

    /**
     * Test of removePotentialByID method, of class PotentialManager.
     */
    @Test
    public void testRemovePotentialByID_3args() {
        System.out.println("removePotentialByID");
        int particleId1 = 10;
        int particleId2 = 11;
        int potId = 3;

        //######## ######## ######## ######## ######## ########

        IParticle p1 = new Particle(10, 1, new double[]{0, 0, 0});
        IParticle p2 = new Particle(11, 1, new double[]{0, 0, 0});
        //Iterator expResult = ;

        int counter = 0;
        Iterator<IPotential2> result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsBefore = counter;

        //######## ######## ######## ######## ######## ########

        potentialManager.removePotentialByID(particleId1,particleId2, potId);

        //######## ######## ######## ######## ######## ########

        counter = 0;
        result = potentialManager.getPotentials(p1,p2);
        while (result.hasNext()) {
            IPotential2 pot = result.next();
            System.out.print(pot.get_id()+", ");
            counter++;

        }
        System.out.println();
        int nPotentialsAfter = counter;

        assertEquals(nPotentialsBefore - 1, nPotentialsAfter);

    }
}
