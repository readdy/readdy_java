/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package readdy.impl.tools.geometry;

import statlab.base.util.DoubleArrays;

/**
 *
 * @author johannesschoeneberg
 */
public class SphericalGeometry {

    public static double distance(double[] c0, double[] c1, double sphereRadius) { 
        
        double angle = Math.acos((c0[0]*c1[0]+c0[1]*c1[1]+c0[2]*c1[2])/(DoubleArrays.norm(c0)*DoubleArrays.norm(c1)));
        double arcLength=angle *sphereRadius;
        return arcLength;
        
    }
    
     public static double[] computeSphericalStripesAreaArray(double binWidth, int nBins, double radius) {
        // this array holds all spherical calottes from 0-0, 0-1*binWidth, 0-2*binWidth...
        double[] kalottesAreaArray = new double[nBins + 1];
        for (int i = 0; i < kalottesAreaArray.length; i++) {
            double distanceFromStandpoint = binWidth * i;
            double centriAngleBtwnStandpointAndBin = (distanceFromStandpoint - 0) / radius;
            kalottesAreaArray[i] = calotteArea(centriAngleBtwnStandpointAndBin, radius);
        }

        // compute the difference between calottes. The larger one minus the by one smaller one
        double[] sphericalStripesAreaArray = new double[nBins];
        for (int i = 0; i < sphericalStripesAreaArray.length; i++) {
            sphericalStripesAreaArray[i] = kalottesAreaArray[i + 1] - kalottesAreaArray[i];
        }

        return sphericalStripesAreaArray;

    }
     
     
    public static double calotteArea(double alpha, double r) {
        // we do not divide by 2 in the alpha because we want to
        // compute the eintire sphere

        return 2 * r * r * Math.PI * (1 - Math.cos(alpha));
    }

}
