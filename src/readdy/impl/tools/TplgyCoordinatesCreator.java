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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schoeneberg
 */
public class TplgyCoordinatesCreator {

    private static BufferedWriter out = null;
    private int currentParticleId = -1;
    private Random rand = new Random();

    public TplgyCoordinatesCreator(String filename) {
        try {
            out = new BufferedWriter(new FileWriter(filename));
            out.write("<traj> \n \t<frame id=\"0\">\n");
        } catch (IOException ex) {
            Logger.getLogger(TplgyCoordinatesCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            out.write("\t</frame>\n </traj>");
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(TplgyCoordinatesCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(TplgyCoordinatesCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getNextParticleId() {
        currentParticleId++;
        return currentParticleId;
    }

    public void createParticleWithRandomCoordinatesWithinRange(int typeId, double[][] ranges) {
        double xMin = ranges[0][0];
        double xMax = ranges[0][1];
        double yMin = ranges[1][0];
        double yMax = ranges[1][1];
        double zMin = ranges[2][0];
        double zMax = ranges[2][1];

        int id = getNextParticleId();

        double x = xMin + rand.nextDouble() * (xMax - xMin);
        double y = yMin + rand.nextDouble() * (yMax - yMin);
        double z = zMin + rand.nextDouble() * (zMax - zMin);

        try {
            out.write("\t"
                    + "<p  id=\"" + id + "\"  "
                    + "type=\"" + typeId + "\"  "
                    + "c=\"[" + x + "," + y + "," + z + "]\"/>\n");
        } catch (IOException ex) {
            Logger.getLogger(TplgyCoordinatesCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
