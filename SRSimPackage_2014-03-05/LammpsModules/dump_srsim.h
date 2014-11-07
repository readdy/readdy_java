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

#ifndef DUMP_SRSIM_H
#define DUMP_SRSIM_H


#include <SRSim/defs.h>
#include <assert.h>

#include "dump.h"

#include "atom_vec_srsim.h"
#include <vector>


using namespace std;
namespace LAMMPS_NS 
{

class DumpSRSim : public Dump 
  {
  public:
    DumpSRSim(LAMMPS *, int, char**);
   ~DumpSRSim();
   //void init();

  private:

   int  modify_param(int, char **);
   void write_header(int);
   int  count();
   int  pack();
   void write_data(int, double *);
   
   void add_all_templates ();
   void calculate_quantities ();
   
   vector<int>   observedTemplates;
   vector<int>   quantities;
   AtomVecSRSim *avec;
   };

}

#endif
