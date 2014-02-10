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
package readdy.impl.sim.top.rkHandle;

import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.api.sim.core.particle.IParticle;
import readdy.impl.sim.core.particle.Particle;
import java.util.ArrayList;
import readdy.api.assembly.IGroupConfigurationFactory;
import readdy.api.assembly.IGroupFactory;
import readdy.api.assembly.IPotentialManagerFactory;
import readdy.api.disassembly.IGroupDisassembler;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileData;
import readdy.api.io.in.tpl_groups.ITplgyGroupsFileParser;
import readdy.api.io.in.tpl_pot.ITplgyPotentialsFileData;
import readdy.api.sim.core.pot.IPotentialManager;
import readdy.api.sim.top.group.IGroupConfiguration;
import readdy.impl.assembly.GroupConfigurationFactory;
import readdy.impl.assembly.GroupFactory;
import readdy.impl.assembly.PotentialManagerFactory;
import readdy.impl.disassembly.GroupDisassembler;
import readdy.impl.io.in.tpl_group.TplgyGroupsFileParser;
import readdy.impl.io.in.tpl_pot.TplgyPotentialsFileParser;
import readdy.api.sim.top.group.IExtendedIdAndType;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.assembly.IGroupParametersFactory;
import readdy.api.assembly.IParticleConfigurationFactory;
import readdy.api.assembly.IParticleParametersFactory;
import readdy.api.assembly.IPotentialFactory;
import readdy.api.assembly.IPotentialInventoryFactory;
import readdy.api.assembly.IReactionValidatorFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReactionValidator;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.ReactionValidatorFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.top.group.ExtendedIdAndType;

/**
 *
 * @author schoeneberg
 */
public class ReactionValidatorTest {

    private static IGroupConfiguration groupConfiguration;
    private static IGroupParameters groupParameters;
    private static IReactionValidator reactionValidator;
    private static ArrayList<IExecutableReaction> input_executableReactionsList = new ArrayList();
    private static ArrayList<Boolean> expectedResultList = new ArrayList();

    public ReactionValidatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


        //##############################################################################
        // group Topology
        //##############################################################################

        String filename = "./test/testInputFiles/test_default/tplgy_groups.xml";
        ITplgyGroupsFileParser tplgyGroupsFileParser = new TplgyGroupsFileParser();
        tplgyGroupsFileParser.parse(filename);
        ITplgyGroupsFileData tplgyGroupsFileData = tplgyGroupsFileParser.get_groupsFileData();


        //##############################################################################
        // potentialManager
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

        String tplgyPotentialFilename = "./test/testInputFiles/test_default/tplgy_potentials.xml";
        TplgyPotentialsFileParser tplgyPotentialsFileParser = new TplgyPotentialsFileParser();
        tplgyPotentialsFileParser.parse(tplgyPotentialFilename);
        ITplgyPotentialsFileData potFileData = tplgyPotentialsFileParser.get_tplgyPotentialsFileData();

        IPotentialManagerFactory potentialManagerFactory = new PotentialManagerFactory();
        potentialManagerFactory.set_potentialInventory(potentialInventory);
        potentialManagerFactory.set_tplgyPotentialsFileData(potFileData);
        potentialManagerFactory.set_particleParameters(particleParameters);

        IPotentialManager potentialManager = potentialManagerFactory.createPotentialManager();


        //##############################################################################
        // groupParameters
        //##############################################################################

        String paramGroupsfilename = "./test/testInputFiles/test_default/param_groups.xml";
        IParamGroupsFileParser paramGroupsFileParser = new ParamGroupsFileParser();
        paramGroupsFileParser.parse(paramGroupsfilename);
        IParamGroupsFileData paramGroupsFileData = paramGroupsFileParser.get_paramGroupsFileData();

        IGroupParametersFactory groupParametersFactory = new GroupParametersFactory();
        groupParametersFactory.set_paramGroupsFileData(paramGroupsFileData);
        groupParametersFactory.set_particleParameters(particleParameters);
        groupParametersFactory.set_potentialInventory(potentialInventory);
        groupParameters = groupParametersFactory.createGroupParameters();

        
        //##############################################################################
        // determine lattice box size
        // it is important, that this happens, before the particleConfiguration assembly
        //##############################################################################
        ILatticeBoxSizeComputer latticeBoxSizeComputer = new LatticeBoxSizeComputer(
                particleParameters, 
                potentialInventory, 
                globalParameters);
        double latticeBoxSize = latticeBoxSizeComputer.getLatticeBoxSize();
        globalParameters.set_latticeBoxSize(latticeBoxSize);


        //##############################################################################
        // ParticleConfiguration
        //##############################################################################

        System.out.println("parse tplgyCoordinatesFile");
        String tplgyCoordinatesFileName = "./test/testInputFiles/test_default/tplgy_coordinates.xml";

        TplgyCoordinatesFileParser tplgyCoordsParser = new TplgyCoordinatesFileParser();
        tplgyCoordsParser.parse(tplgyCoordinatesFileName);
        ITplgyCoordinatesFileData tplgyCoordsFileData = tplgyCoordsParser.get_coodinatesFileData();


        IParticleConfigurationFactory configFactory = new ParticleConfigurationFactory();
        configFactory.set_particleParameters(particleParameters);
        configFactory.set_tplgyCoordinatesFileData(tplgyCoordsFileData);
        configFactory.set_globalParameters(globalParameters);
        IParticleConfiguration particleConfiguration = configFactory.createParticleConfiguration();


        //##############################################################################
        // put it all together
        //##############################################################################

        IGroupFactory groupFactory = new GroupFactory();
        groupFactory.set_potentialManager(potentialManager);
        groupFactory.set_groupParameters(groupParameters);

        IGroupDisassembler groupDisassembler = new GroupDisassembler();
        groupDisassembler.set_potentialManager(potentialManager);
        groupDisassembler.set_groupParameters(groupParameters);

        IGroupConfigurationFactory groupConfigurationFactory = new GroupConfigurationFactory();
        groupConfigurationFactory.set_tplgyGroupsFileData(tplgyGroupsFileData);
        groupConfigurationFactory.set_groupFactory(groupFactory);
        groupConfigurationFactory.set_groupDisassembler(groupDisassembler);
        groupConfigurationFactory.set_groupParameters(groupParameters);
        groupConfigurationFactory.set_particleConfiguration(particleConfiguration);

        groupConfiguration = groupConfigurationFactory.createGroupConfiguration();

        IReactionValidatorFactory reactionValidatorFactory = new ReactionValidatorFactory();
        reactionValidatorFactory.set_groupConfiguration(groupConfiguration);
        reactionValidatorFactory.set_groupParameters(groupParameters);
        reactionValidator = reactionValidatorFactory.createReactionValidator();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

        input_executableReactionsList = new ArrayList();
        expectedResultList = new ArrayList();

        // create executableReactions
        // ---------------------------------------------------------------------------
        int rkId = 0;
        int rkTypeId = 0;
        int derivedFromElmtlRkId = 0;
        HashMap<IParticle, IExtendedIdAndType> educts = new HashMap();
        IParticle e_involvedParticle1 = new Particle(0, 0, new double[]{0, 0, 0});
        IExtendedIdAndType e_extIdAndType1 = new ExtendedIdAndType(true, 5, 0);
        IParticle e_involvedParticle2 = new Particle(0, 0, new double[]{0, 0, 0});
        IExtendedIdAndType e_extIdAndType2 = new ExtendedIdAndType(true, 5, 0);
        educts.put(e_involvedParticle1, e_extIdAndType1);
        educts.put(e_involvedParticle2, e_extIdAndType2);

        IExtendedIdAndType p_extIdAndType1 = new ExtendedIdAndType(true, 0, 0);
        IExtendedIdAndType p_extIdAndType2 = new ExtendedIdAndType(true, 0, 0);
        ArrayList<IExtendedIdAndType> products = new ArrayList();
        products.add(p_extIdAndType1);
        products.add(p_extIdAndType2);
        IExecutableReaction executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        input_executableReactionsList.add(executableReaction);

        boolean expectedValidationResult = false;

        expectedResultList.add(expectedValidationResult);

        // ---------------------------------------------------------------------------
        rkId = 1;
        rkTypeId = 8;
        derivedFromElmtlRkId = 11;
        educts = new HashMap();
        e_involvedParticle1 = new Particle(1, 7, new double[]{0, 0, 0});
        e_extIdAndType1 = new ExtendedIdAndType(false, 5, 0);
        e_involvedParticle2 = new Particle(0, 6, new double[]{0, 0, 0});
        e_extIdAndType2 = new ExtendedIdAndType(false, -1, 6);
        educts.put(e_involvedParticle1, e_extIdAndType1);
        educts.put(e_involvedParticle2, e_extIdAndType2);

        p_extIdAndType1 = new ExtendedIdAndType(false, -1, 5);
        products = new ArrayList();
        products.add(p_extIdAndType1);
        executableReaction = new ExecutableReaction(rkId, rkTypeId, derivedFromElmtlRkId, educts, products);

        input_executableReactionsList.add(executableReaction);

        expectedValidationResult = true;

        expectedResultList.add(expectedValidationResult);


    }

    @After
    public void tearDown() {
    }

    /**
     * Test of validateReactions method, of class ReactionValidator.
     */
    @Test
    public void testValidateReaction() {
        System.out.println("validateReaction");

        assertEquals(input_executableReactionsList.size(), expectedResultList.size());
        for (int i = 0; i < input_executableReactionsList.size(); i++) {
            IExecutableReaction inputExecutableReaction = input_executableReactionsList.get(i);
            boolean expectedResult = expectedResultList.get(i);

            boolean result = reactionValidator.validateReaction(inputExecutableReaction);
            System.out.println(input_executableReactionsList.get(i).hashCode() + " expValid " + expectedResult + " result " + result);
            assertEquals(expectedResult, result);

        }



    }

    /**
     * Test of validateReactions method, of class ReactionValidator.
     */
    @Test
    public void testValidateReactions() {
        System.out.println("validateReactions");

        assertEquals(input_executableReactionsList.size(), expectedResultList.size());
        System.out.println("inputListSize() " + input_executableReactionsList.size());
        ArrayList<IExecutableReaction> resultList = new ArrayList();
        for(IExecutableReaction rk:input_executableReactionsList){
        if(reactionValidator.validateReaction(rk)){
            resultList.add(rk);
        }
        }
        System.out.println("inputListSize() " + input_executableReactionsList.size());
        System.out.println("resultListSize() " + resultList.size());

        for (int i = 0; i < input_executableReactionsList.size(); i++) {
            IExecutableReaction findCandidate = input_executableReactionsList.get(i);
            boolean expectedFindResult = expectedResultList.get(i);
            boolean findResult = false;
            for (IExecutableReaction findMatchPartner : resultList) {
                System.out.println(input_executableReactionsList.get(i).hashCode() + " " + findMatchPartner.hashCode());
                if (input_executableReactionsList.get(i).hashCode() == findMatchPartner.hashCode()) {
                    findResult = true;
                    break;
                }
            }


            assertEquals(expectedFindResult, findResult);

        }

    }
}
