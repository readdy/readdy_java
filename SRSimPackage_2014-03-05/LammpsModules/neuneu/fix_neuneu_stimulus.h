/* ----------------------------------------------------------------------
   LAMMPS - Large-scale Atomic/Molecular Massively Parallel Simulator
   http://lammps.sandia.gov, Sandia National Laboratories
   Steve Plimpton, sjplimp@sandia.gov

   Copyright (2003) Sandia Corporation.  Under the terms of Contract
   DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government retains
   certain rights in this software.  This software is distributed under 
   the GNU General Public License.

   See the README file in the top-level LAMMPS directory.
------------------------------------------------------------------------- */

#ifndef FIX_NEUNEU_STIMULUS_H
#define FIX_NEUNEU_STIMULUS_H

#include "fix.h"
#include <string>
#include <vector>
#include <set>


namespace LAMMPS_NS {

class FixNeuneuStimulus : public Fix {
 public:
  FixNeuneuStimulus(class LAMMPS *, int, char **);
  int setmask();
  void init();
  void setup(int);
  void initial_integrate(int);
  
  void loadStimFile(std::string fname);

 private:
  int    nSkipTimesteps;
  int    stimType;
         // "static_compare"  put a time series to inputs, compare 
         // ... "static_write"    put a primitive signal onto a small set of input neurons
         // ... "static_read"     read a time series from a small set of output neurons
   
  std::vector<int>               inptPattern;  //  inptPattern[i] = b   :  b is the template-id of the i-th input
  std::vector<int>               outpPattern;  //  outpPattern[i] = b   :  b is the template-id of the i-th output
  
  // stimulation data:
  std::vector<double>                stimTimes;        // when to switch the stimulation
  double                             stimDelay;  // how long to wait between two successive stimulations
  std::vector<std::vector<int> >     stimData;         // stimData[i][j] = x   :   x state of the j-th neuron type in the i-th stim phase
  std::vector<std::vector<int> >     outpExpected;     // stimData[i][j] = x   :   x state of the j-th neuron type in the i-th stim phase
  double                             outpDelay;  // delay between start of the stim and the start of the measurement
  std::string                        stimResultFile;
  
  int                                excitable_site_id;
  int                                excite_modification;
  std::set<int>                      excited_modification;
  
  // accumulate output data:
  int    iStimPhase;        // in which stim phase are we
  int    iSubPhase;         // wait for: 0,1,2:  start -0- stimDelay -1- outpDelay -2- end
  double phaseStart;        // remembers when the last iStimPhase was started...
  double outpAccumGood;     // 
  double outpAccumBad;      // 
};

// namespace ends...
}

#endif
