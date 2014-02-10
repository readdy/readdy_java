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

import readdy.api.assembly.IElmtlRkToRkMatcherFactory;
import readdy.api.sim.core.particle.IParticle;
import readdy.api.sim.core.rk.IOccurredElementalReaction;
import readdy.api.sim.top.rkHandle.IElmtlRkToRkMatcher;
import readdy.api.sim.top.rkHandle.IExecutableReaction;
import readdy.impl.assembly.ElmtlRkToRkMatcherFactory;
import readdy.impl.sim.core.particle.Particle;
import readdy.impl.sim.core.rk.OccurredElementalReaction;
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
import readdy.api.assembly.IReactionParametersFactory;
import readdy.api.sim.top.group.IExtendedIdAndType;
import readdy.api.sim.top.rkHandle.IReactionParameters;
import readdy.impl.assembly.ReactionParametersFactory;
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
import readdy.api.assembly.IRkAndElmtlRkFactory;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.io.in.par_group.IParamGroupsFileData;
import readdy.api.io.in.par_group.IParamGroupsFileParser;
import readdy.api.io.in.par_particle.IParamParticlesFileData;
import readdy.api.io.in.par_particle.IParamParticlesFileParser;
import readdy.api.io.in.par_rk.IParamReactionsFileData;
import readdy.api.io.in.par_rk.IParamReactionsFileParser;
import readdy.api.io.in.tpl_coord.ITplgyCoordinatesFileData;
import readdy.api.sim.core.config.IParticleConfiguration;
import readdy.api.sim.core.particle.IParticleParameters;
import readdy.api.sim.core.pot.IPotentialInventory;
import readdy.api.sim.core.space.ILatticeBoxSizeComputer;
import readdy.api.sim.top.group.IGroupParameters;
import readdy.api.sim.top.rkHandle.IReaction;
import readdy.api.sim.top.rkHandle.IReactionManager;
import readdy.impl.assembly.GroupParametersFactory;
import readdy.impl.assembly.ParticleConfigurationFactory;
import readdy.impl.assembly.ParticleParametersFactory;
import readdy.impl.assembly.PotentialFactory;
import readdy.impl.assembly.PotentialInventoryFactory;
import readdy.impl.assembly.RkAndElmtlRkFactory;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;
import readdy.impl.io.in.par_group.ParamGroupsFileParser;
import readdy.impl.io.in.par_particle.ParamParticlesFileParser;
import readdy.impl.io.in.par_rk.ParamReactionsFileParser;
import readdy.impl.io.in.tpl_coord.TplgyCoordinatesFileParser;
import readdy.impl.sim.core.space.LatticeBoxSizeComputer;
import readdy.impl.sim.top.group.ExtendedIdAndType;

/**
 *
 * @author schoeneberg
 */
public class ElmtlRkToRkMatcherTest {

    private static IElmtlRkToRkMatcher elmtlRkToRkMatcher;
    private static IReactionParameters reactionParameters;
    private static IGroupConfiguration groupConfig;

    public ElmtlRkToRkMatcherTest() {
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
        IGroupParameters groupParameters = groupParametersFactory.createGroupParameters();
        
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
        IParticleConfiguration particleConfig = configFactory.createParticleConfiguration();

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
        groupConfigurationFactory.set_particleConfiguration(particleConfig);

        groupConfig = groupConfigurationFactory.createGroupConfiguration();
        ArrayList<IParticle> positionedParticles = new ArrayList();
        positionedParticles.add(new Particle(0, 0, new double[]{0, 0, 0}));
        positionedParticles.add(new Particle(1, 0, new double[]{0, 0, 0}));
        groupConfig.createGroup(0, positionedParticles);

        //##############################################################################
        // param Reactions ...
        //##############################################################################

        String filenameParamReactions = "./test/testInputFiles/test_default/param_reactions.xml";
        IParamReactionsFileParser paramReactionsFileParser = new ParamReactionsFileParser();
        paramReactionsFileParser.parse(filenameParamReactions);
        IParamReactionsFileData paramReactionsFileData = paramReactionsFileParser.get_paramReactionsFileData();

        //##############################################################################


        IReactionManager reactionManager = new ReactionManager();

        IRkAndElmtlRkFactory rkAndElmtlRkFactory = new RkAndElmtlRkFactory();
        rkAndElmtlRkFactory.set_globalParameters(globalParameters);
        rkAndElmtlRkFactory.set_reactionManager(reactionManager);
        rkAndElmtlRkFactory.set_groupParameters(groupParameters);
        rkAndElmtlRkFactory.set_particleParameters(particleParameters);
        rkAndElmtlRkFactory.createReactionsAndElmtlReactions(paramReactionsFileData);
        HashMap<Integer, IReaction> reactions = rkAndElmtlRkFactory.get_reactions();
        HashMap<Integer, Integer> elmtlRkId_to_rkId_map = rkAndElmtlRkFactory.get_elmtlRkToRkMapping();

        IReactionParametersFactory reactionParametersFactory = new ReactionParametersFactory();
        reactionParametersFactory.set_reactions(reactions);
        reactionParametersFactory.set_elmtlRkId_to_rkId_map(elmtlRkId_to_rkId_map);
        reactionParameters = reactionParametersFactory.createReactionParameters();



        IElmtlRkToRkMatcherFactory elmtlRkToRkMatcherFactory = new ElmtlRkToRkMatcherFactory();
        elmtlRkToRkMatcherFactory.set_groupConfiguration(groupConfig);
        elmtlRkToRkMatcherFactory.set_reactionParameters(reactionParameters);
        elmtlRkToRkMatcher = elmtlRkToRkMatcherFactory.createElmtlRkToRkMatcher();
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
     * Test of matchReactions method, of class ElmtlRkToRkMatcher.
     */
    @Test
    public void testMatchReactions() {
        System.out.println("Test matchReactions");
        ArrayList<IOccurredElementalReaction> occElmtlRkList = new ArrayList();

        //#####################################################################################
        int elmtlRkId = 1; // ungrouping of a dimer with group id 1
        System.out.println("testing elmtlRkId " + elmtlRkId + " ...");
        IParticle[] involvedParticles = new IParticle[]{
            new Particle(0, 0, new double[]{0, 0, 0}),
            new Particle(1, 0, new double[]{0, 0, 0})};

        IOccurredElementalReaction occElmtlRk = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk);

        ArrayList<IExecutableReaction> expResult = new ArrayList();
        HashMap<IParticle, IExtendedIdAndType> educts = new HashMap();
        educts.put(involvedParticles[0], new ExtendedIdAndType(true, 1, 0)); // both particles belong to group id 1
        educts.put(involvedParticles[1], new ExtendedIdAndType(true, 1, 0)); // both particles belong to group id 1

        ArrayList<IExtendedIdAndType> products = new ArrayList();
        products.add(new ExtendedIdAndType(false, -1, 0));
        products.add(new ExtendedIdAndType(false, -1, 0));
        // reaction id 1, reaction type 101=ungrouping, derived from elemental reaction 1 = dimerformation_backward
        ExecutableReaction expResultEntry = new ExecutableReaction(1, 101, 1, educts, products);
        expResult.add(expResultEntry);

        ArrayList<IExecutableReaction> result = new ArrayList();
        for(IOccurredElementalReaction elmtlRk:occElmtlRkList){
            if(elmtlRkToRkMatcher.matchReaction(elmtlRk)!=null){
        result.add(elmtlRkToRkMatcher.matchReaction(elmtlRk));
            }
        }

        for (IExecutableReaction exRk : result) {
            System.out.println("RK idToTest: " + elmtlRkId + " exRk:");
            exRk.print();
        }

        compareExpectedAndActualResultArrays(expResult, result);



        //#####################################################################################
        elmtlRkId = 8; // type conversion forward
        System.out.println("testing elmtlRkId " + elmtlRkId + " ...");
        involvedParticles = new IParticle[]{
                    new Particle(5, 3, new double[]{0, 0, 0}) // rhodopsinMetaI
                };

        occElmtlRkList.clear();
        occElmtlRk = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk);

        expResult = new ArrayList();
        educts = new HashMap();
        educts.put(involvedParticles[0], new ExtendedIdAndType(false, 5, 3));

        products = new ArrayList();
        products.add(new ExtendedIdAndType(false, -1, 4));

        expResultEntry = new ExecutableReaction(4, 4, 8, educts, products);
        expResult.add(expResultEntry);

        result = new ArrayList();
        for(IOccurredElementalReaction elmtlRk:occElmtlRkList){
            if(elmtlRkToRkMatcher.matchReaction(elmtlRk)!=null){
        result.add(elmtlRkToRkMatcher.matchReaction(elmtlRk));
            }
        }

        for (IExecutableReaction exRk : result) {
            System.out.println("RK idToTest: " + elmtlRkId + " exRk:");
            exRk.print();
        }

        compareExpectedAndActualResultArrays(expResult, result);

        //#####################################################################################
        elmtlRkId = 11; // dummy fission backward -> = fusion
        System.out.println("----------------------------------------------------");
        System.out.println("dummy fission backward -> = fusion");
        System.out.println("testing elmtlRkId " + elmtlRkId + " ...");
        involvedParticles = new IParticle[]{
                    new Particle(0, 6, new double[]{0, 0, 0}), // fission product 1
                    new Particle(1, 7, new double[]{0, 0, 0})}; // fission product 2

        occElmtlRkList.clear();
        occElmtlRk = new OccurredElementalReaction(elmtlRkId, involvedParticles);
        occElmtlRkList.add(occElmtlRk);

        expResult = new ArrayList();
        educts = new HashMap();
        educts.put(involvedParticles[0], new ExtendedIdAndType(false, 0, 6));
        educts.put(involvedParticles[1], new ExtendedIdAndType(false, 1, 7));

        products = new ArrayList();
        products.add(new ExtendedIdAndType(false, -1, 5)); // dummy fissile
        expResultEntry = new ExecutableReaction(7, 8, elmtlRkId, educts, products);
        expResult.add(expResultEntry);

        result = new ArrayList();
        for(IOccurredElementalReaction elmtlRk:occElmtlRkList){
            if(elmtlRkToRkMatcher.matchReaction(elmtlRk)!=null){
        result.add(elmtlRkToRkMatcher.matchReaction(elmtlRk));
            }
        }

        for (IExecutableReaction exRk : result) {
            System.out.println("RK idToTest: " + elmtlRkId + " exRk:");
            exRk.print();
        }

        compareExpectedAndActualResultArrays(expResult, result);

    }

    private void compareExpectedAndActualResultArrays(ArrayList<IExecutableReaction> expResult, ArrayList<IExecutableReaction> result) {
        assertEquals(expResult.size(), result.size());
        for (int i = 0; i < expResult.size(); i++) {
            IExecutableReaction expResultEntry = expResult.get(i);
            IExecutableReaction resultEntry = result.get(i);
            assertEquals(expResultEntry.get_rkId(), resultEntry.get_rkId());
            assertEquals(expResultEntry.get_derivedFromElmtlRkId(), resultEntry.get_derivedFromElmtlRkId());
            assertEquals(expResultEntry.get_rkTypeId(), resultEntry.get_rkTypeId());
            System.out.println("<compare...>");
            System.out.println("<expectedResult>");
            IExecutableReaction expExRk = expResult.get(i);
            expExRk.print();
            System.out.println("</expectedResult>");
            System.out.println("<result>");
            IExecutableReaction resExRk = result.get(i);
            resExRk.print();
            System.out.println("</result>");
            System.out.println("</compare...>");
        }

    }
}
