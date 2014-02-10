/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package readdy.impl.sim.core_mc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import readdy.api.io.in.par_global.IGlobalParameters;
import readdy.api.io.in.par_global.IParamGlobalFileParser;
import readdy.api.sim.core_mc.IMetropolisDecider;
import readdy.impl.io.in.par_global.ParamGlobalFileParser;


/**
 *
 * @author johannesschoeneberg
 */
public class MetropolisDeciderTest {

    private static IGlobalParameters globalParameters;
    private static IMetropolisDecider metropolisDecider;

    public MetropolisDeciderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println();
        System.out.println("parse globalParameters...");
        String paramGlobalFilename = "./test/testInputFiles/test_MonteCarlo_potentialEnergyComputer/param_global.xml";
        IParamGlobalFileParser paramGlobalFileParser = new ParamGlobalFileParser();
        paramGlobalFileParser.parse(paramGlobalFilename);
        globalParameters = paramGlobalFileParser.get_globalParameters();

        MetropolisDecider metroDec = new MetropolisDecider();
        metroDec.set_GlobalParameters(globalParameters);
        metropolisDecider = metroDec;

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
     * Test of doWeAcceptGivenEnergyDifference method, of class MetropolisDecider.
     */
    @Test
    public void testDoWeAcceptGivenEnergyDifference() {
        System.out.println("test doWeAcceptGivenEnergyDifference...");

        double dE = 0.0;
        boolean expResult = true;
        boolean result = metropolisDecider.doWeAcceptGivenEnergyDifference(dE);
        System.out.println("dE: "+dE+" expResult: "+expResult+" result "+result);
        assertEquals(expResult, result);

        dE = -1000.0;
        expResult = true;
        result = metropolisDecider.doWeAcceptGivenEnergyDifference(dE);
        System.out.println("dE: "+dE+" expResult: "+expResult+" result "+result);
        assertEquals(expResult, result);

        dE = 1000.0;
        expResult = false;
        result = metropolisDecider.doWeAcceptGivenEnergyDifference(dE);
        System.out.println("dE: "+dE+" expResult: "+expResult+" result "+result);
        assertEquals(expResult, result);
        
    }


}