/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdy.impl.tools.geometry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author johannesschoeneberg
 */
public class SphericalGeometryTest {
    
    public SphericalGeometryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of distance method, of class SphericalGeometry.
     */
    @Test
    public void testDistance() {
        System.out.println("distance");
        double[] c0 = new double[]{0,0,1};
        double[] c1 = new double[]{0,1,0};
        double sphereRadius = 1.0;
        double expResult = Math.PI/2;
        double result = SphericalGeometry.distance(c0, c1, sphereRadius);
        assertEquals(expResult, result, 0.0);
        
        c0 = new double[]{0,0,5};
        c1 = new double[]{0,0,-5};
        sphereRadius = 5.0;
        expResult = 5*Math.PI;
        result = SphericalGeometry.distance(c0, c1, sphereRadius);
        assertEquals(expResult, result, 0.0);
        
    }
}
