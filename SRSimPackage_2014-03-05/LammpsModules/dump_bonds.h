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

#ifndef DUMP_BONDS_H
#define DUMP_BONDS_H


#include <SRSim/defs.h>
#include <assert.h>

#include "dump.h"

#include "atom_vec_bond.h"
#include "atom_vec_srsim.h"
#include "atom.h"
#include <vector>


using namespace std;
namespace LAMMPS_NS 
{
    
class DumpBonds : public Dump 
  {
  public:
    DumpBonds(LAMMPS *, int, char**);
   ~DumpBonds();
   //void init();

  private:

   int  modify_param(int, char **);
   void write_header(int);
   int  count();
   int  pack();
   void write_data(int, double *);

   enum DumpType {BOND_SET, BNGL_GRAPHS};
   DumpType  dump_type;
   
   AtomVecSRSim *avec;
   };

}

#endif
