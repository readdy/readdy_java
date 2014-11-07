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

#include "math.h"
#include "stdlib.h"
#include "string.h"
#include "fix_drag_membrane.h"
#include "atom.h"
#include "update.h"
#include "respa.h"
#include "domain.h"
#include "error.h"

using namespace LAMMPS_NS;

/* ---------------------------------------------------------------------- */


/**
 *  FixDragMembrane will drag particles inside until the yzRadius is reached.
 *  Parameters: 
 *   "<id>, <name>, <group>, xStart, xEnd, xRamp, yzRadius, force"
 */
FixDragMembrane::FixDragMembrane(LAMMPS *lmp, int narg, char **arg) :
  Fix(lmp, narg, arg)
{
  if (narg != 8) error->all("Illegal fix drag command");

  xStart = atof(arg[3]);
  xEnd   = atof(arg[4]);
  xRamp  = atof(arg[5]);
  yzRad  = atof(arg[6]);
  
  f_mag  = atof(arg[7]);
}

/* ---------------------------------------------------------------------- */

int FixDragMembrane::setmask()
{
  int mask = 0;
  mask |= POST_FORCE;
  return mask;
}

/* ---------------------------------------------------------------------- */

void FixDragMembrane::init()
{
  if (strcmp(update->integrate_style,"respa") == 0)
     error->all("FixDragMembrane::setup: Sorry, I don't know about respa...");
}

/* ---------------------------------------------------------------------- */

void FixDragMembrane::setup(int vflag)
{
  if (strcmp(update->integrate_style,"verlet") == 0)
    post_force(vflag);
  else {
    error->all("FixDragMembrane::setup: Sorry, I can only do verlet simulations...");
  }
}

/* ---------------------------------------------------------------------- */


/*  Use the ramp as follows:
 *      __xStart______ xEnd
 *     /              \
 * ___/                \_____
 *   xStart-xRamp       xEnd+xRamp
 */
void FixDragMembrane::post_force(int vflag)
{
  // apply drag force to atoms in group of magnitude f_mag
  // apply in direction (r-r0) if atom is further than delta away

  double **x = atom->x;
  double **f = atom->f;
  int *mask = atom->mask;
  int nlocal = atom->nlocal;

  

  for (int i = 0; i < nlocal; i++)
    if (mask[i] & groupbit) {
      
      // see, if we're on the ramp
      double ramp = 1.0;
      if (x[i][0] < xStart-xRamp || x[i][0] > xEnd+xRamp) continue;  // no force applied outside these areas.
      else if (x[i][0] < xStart)
         { // left ramp:
         ramp = (x[i][0]-(xStart-xRamp)) / xRamp;
         }
      else if (x[i][0] > xEnd)
         { // right ramp:
         ramp = (x[i][0]-xEnd) / xRamp;
         }
         
      // calc force:
      double dy = x[i][1];
      double dz = x[i][2];
      double r  = sqrt(dy*dy + dz*dz);
      
      // calc the yzRad_of_x:
      //   let's try abs( (x/5) ** 3) / 1000
      //   -> for x up to +/- 1.0, there is a yz deviation of 0.1, going up to 1.0 for x up to +/- 2.0
      double membraneLength = (xEnd-xStart)/2;
      double posInCurve     = (x[i][0] - (xStart + membraneLength)) / membraneLength * 5.0;  // first map xStart and xEnd to +/- 5
      double addCurve       = (posInCurve * posInCurve * posInCurve) * 0.001 * yzRad;
      if (addCurve < 0) addCurve=-addCurve;
      
      // are we too far outside or too far inside (drag or push)
      if (r - (yzRad+addCurve) < 0) continue;  // we're inside anyway... ignore.
      
      double forceFact = (r - (yzRad+addCurve)) * f_mag/yzRad * ramp;
      // f[i][0] -= fx;
      f[i][1] -= forceFact*dy;
      f[i][2] -= forceFact*dz;
    }
}
