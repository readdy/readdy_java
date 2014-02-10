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

import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ProcessorStopWatchTest {

    private static ProcessorStopWatch stopWatch;

    public ProcessorStopWatchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        stopWatch = new ProcessorStopWatch();
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
     * Test of measureTime method, of class ProcessorStopWatch.
     */
    @Test
    public void testStopWatch() {
        System.out.println("testStopWatch");
        int breakpointId = 0;
        long time = 0L;

        for (int i = 0; i < 10; i++) {
            stopWatch.measureTime(0, System.nanoTime());
            int counter = 0;
            for (int j = 0; j < 100; j++) {
                try {
                    //System.out.println("sleep...");
                    Thread.sleep(1);
                    if (counter % 10 == 0) {
                        System.out.println(counter);
                    }
                    ;
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcessorStopWatchTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                counter++;
            }
            stopWatch.measureTime(1, System.nanoTime());
            stopWatch.accumulateTime(0, 0, 1);
        }

        long totalTime = stopWatch.getTotalTime(breakpointId);
        long avgTime = stopWatch.getAverageTime(breakpointId);

        System.out.print("totalTime: ");
        System.out.format("%f", (float) totalTime);
        System.out.print(", avgTime: ");
        System.out.format("%f", (float) avgTime); //%n for the newline

        int expNAcc = 10;
        int resultNAcc = stopWatch.getNAccumulations(breakpointId);
        assertEquals(expNAcc, resultNAcc);
    }
}
