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

#ifndef FIX_PUSH_MEMBRANE_H
#define FIX_PUSH_MEMBRANE_H

#include "fix.h"

namespace LAMMPS_NS {

class FixPushMembrane : public Fix {
 public:
  FixPushMembrane(class LAMMPS *, int, char **);
  int setmask();
  void init();
  void setup(int);
  void post_force(int);

 private:
  double xStart, xEnd, xRamp;
  double yzRad;
  double f_mag;
};

}

#endif
