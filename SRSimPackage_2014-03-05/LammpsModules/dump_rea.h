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

#ifndef DUMP_REA_H
#define DUMP_REA_H


#include <SRSim/defs.h>
#include <SRSim/multi_mol_reactant_template.h>
#include <assert.h>

#include "dump.h"

#include "atom_vec_bond.h"
#include "atom_vec_srsim.h"
#include "atom.h"
#include "lammps_molecule.h"

#include <vector>


using namespace std;
namespace LAMMPS_NS 
{
  
// forward declarations: 
class AtomVecSRSim;
class LammpsMolecule;
    
class DumpRea : public Dump 
  {
  public:
    DumpRea (LAMMPS *, int, char**);
   ~DumpRea ();
   //void init();

    // reaction syntax:  A + B  -->  C + D     Where B and D can be NULL
    void setCurrentReactionLHS (LammpsMolecule *lmA, LammpsMolecule *lmB);
    void setCurrentReactionRHS (LammpsMolecule *lmC, LammpsMolecule *lmD);
   
   
  private:

   int  modify_param(int, char **);
   void write_header(int);
   int  count();
   int  pack();
   void write_data(int, double *);

   //enum DumpType {BOND_SET, BNGL_GRAPHS};
   //DumpType  dump_type;
   
   int  checkSpeciesKnown ( LammpsMolecule *lm );
   int  checkReactionKnown( vector< vector<int> > &rea );
   bool reactionEquals    ( vector< vector<int> > &rea1, vector< vector<int> > &rea2 );
   void printReaction     ( vector< vector<int> > &rea, FILE* fout);

   AtomVecSRSim      *avec;
   
   vector<SRSim_ns::MultiMolReactantTemplate*> known_species;
   vector< vector< vector<int> > >             known_reactions;    //  known_reactions[i_rea].first.second[i_site][i_reactant]
   
   vector<int> current_lhs;
   };

}

#endif
