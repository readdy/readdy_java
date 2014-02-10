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

import java.util.HashMap;

/**
 *
 * @author schoeneberg 02.09.2011
 */
public class ProcessorStopWatch {

    private HashMap<Integer, TimeAccumulator> timeAccumulators = new HashMap();
    private HashMap<Integer, BreakPoint> breakPoints = new HashMap();
    private HashMap<Integer, String> accumulatorNames = new HashMap();

    public void measureTime(int breakpointId, long time) {
        if (breakPoints.containsKey(breakpointId)) {

            BreakPoint bp = breakPoints.get(breakpointId);
            bp.updateTime(time);
        } else {
            BreakPoint bpNew = new BreakPoint(breakpointId);
            bpNew.updateTime(time);
            breakPoints.put(breakpointId, bpNew);
        }
    }

    public void setAccumulatorName(int accumulatorId, String name) {
        if (timeAccumulators.keySet().contains(accumulatorId)) {
            accumulatorNames.put(accumulatorId, name);
        }
    }

    public String getAccumulatorName(int accumulatorId) {
        if (accumulatorNames.containsKey(accumulatorId)) {
            return accumulatorNames.get(accumulatorId);
        } else {
            throw new RuntimeException("the accumulatorId " + accumulatorId + " doesnt exist");
        }
    }

    public void accumulateTime(int accumulatorID, int breakpointId1, int breakpointId2) {

        BreakPoint bp1, bp2;


        if (breakPoints.containsKey(breakpointId1)) {
            bp1 = breakPoints.get(breakpointId1);
        } else {
            throw new RuntimeException("breakPoint not existent.");
        }

        if (breakPoints.containsKey(breakpointId2)) {
            bp2 = breakPoints.get(breakpointId2);
        } else {
            throw new RuntimeException("breakPoint not existent.");
        }


        long timeDifference = bp2.getTime() - bp1.getTime();
        if (timeAccumulators.containsKey(accumulatorID)) {
            TimeAccumulator tacc = timeAccumulators.get(accumulatorID);
            tacc.accumulateTime(timeDifference);
        } else {
            TimeAccumulator taccNew = new TimeAccumulator(accumulatorID);
            taccNew.accumulateTime(timeDifference);
            timeAccumulators.put(accumulatorID, taccNew);
        }
    }

    public long getAverageTime(int accumulatorID) {
        if (timeAccumulators.containsKey(accumulatorID)) {
            TimeAccumulator tacc = timeAccumulators.get(accumulatorID);
            return tacc.getAverageTime();
        } else {
            throw new RuntimeException("timeAccumulator not existent.");
        }
    }

    ;

    public long getTotalTime(int accumulatorID) {
        if (timeAccumulators.containsKey(accumulatorID)) {
            TimeAccumulator tacc = timeAccumulators.get(accumulatorID);
            return tacc.getTotalTime();
        } else {
            throw new RuntimeException("timeAccumulator not existent.");
        }
    }

    ;

    public int getNAccumulations(int accumulatorID) {
        if (timeAccumulators.containsKey(accumulatorID)) {
            TimeAccumulator tacc = timeAccumulators.get(accumulatorID);
            return tacc.getNAccumulations();
        } else {
            throw new RuntimeException("timeAccumulator not existent.");
        }
    }

    ;
}

class TimeAccumulator {

    private int id;
    private long totalTime = 0;
    private int nAccumulations = 0;

    public TimeAccumulator(int id) {
        this.id = id;
    }

    public void accumulateTime(long time) {


        totalTime += time;
        nAccumulations++;
    }

    public int getId() {
        return this.id;
    }

    public long getAverageTime() {
        if (nAccumulations != 0) {
            return totalTime / nAccumulations;
        } else {
            return 0;
        }
    }

    ;

    public long getTotalTime() {
        return totalTime;
    }

    ;

    public int getNAccumulations() {
        return nAccumulations;
    }

    ;
}

class BreakPoint {

    private int id;
    private long currentTime = 0;

    public BreakPoint(int id) {
        this.id = id;
    }

    public void updateTime(long time) {
        this.currentTime = time;
    }

    public int getId() {
        return id;
    }

    public long getTime() {
        return currentTime;
    }
}
