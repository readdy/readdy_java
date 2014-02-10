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
package readdy.impl.tools;

/**
 *
 * @author schoeneberg
 */
public class GeometryTools {

   
    public double getDistance(double[] coords1, double[]coords2){
        return Math.sqrt((coords1[0]-coords2[0])*(coords1[0]-coords2[0])+
                (coords1[1]-coords2[1])*(coords1[1]-coords2[1])+
                (coords1[2]-coords2[2])*(coords1[2]-coords2[2]));
    }

    public double[] cartesianToSpherical(double[] vectorOnSphere){
        double x = vectorOnSphere[0];
        double y = vectorOnSphere[1];
        double z = vectorOnSphere[2];
        double r= Math.sqrt(x*x+y*y+z*z);
        double theta= Math.acos(z/(r));
        double phi=Math.atan2(y, x);
        return new double[]{r,theta,phi};
    }

    public double[] sphericalToCartesian(double[] sphericalCoordVector){
        double r = sphericalCoordVector[0];
        double theta = sphericalCoordVector[1];
        double phi = sphericalCoordVector[2];
        double x = r*Math.sin(theta)*Math.cos(phi);
        double y = r*Math.sin(theta)*Math.sin(phi);
        double z = r*Math.cos(theta);
        return new double[]{x,y,z};
    }

    public static double[] crossProduct3d(double[] a, double[] b){
        double[] result = new double[3];

            result[0]=a[1]*b[2]-a[2]*b[1];
            result[1]=a[2]*b[0]-a[0]*b[2];
            result[2]=a[0]*b[1]-a[1]*b[0];

        return result;
    }
}
