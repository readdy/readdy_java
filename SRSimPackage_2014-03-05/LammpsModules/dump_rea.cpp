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

#include <string.h>
#include <sys/types.h>

#include "dump_rea.h"
#include "domain.h"
#include "atom.h"
#include "update.h"
#include "group.h"
#include "error.h"
#include "comm.h"

#include "lammps_molecule.h"

#include <SRSim/rule_set.h>

using namespace SRSim_ns;
using namespace LAMMPS_NS;


/* ---------------------------------------------------------------------- */

DumpRea::DumpRea(LAMMPS *lmp, int narg, char **arg) : Dump(lmp, narg, arg)
{
  printf ("DumpRea::DumpRea - constructing.\n");
    
  if (narg != 5) error->all("DumpRea::DumpRea: Illegal dump command: <id> <group> rea  <every> <file>");

  format_default = NULL;
  if (multifile == 0) openfile();
  size_one = 1;  // don't ask me why, but if we don't set it, it goes DivedeByZero!
  
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  assert( avec != NULL );
  avec->reaDumper = this;
  
  printf ("DumpRea::DumpRea - finished.\n");
}

/* ---------------------------------------------------------------------- */


DumpRea::~DumpRea()
  {
  avec->reaDumper = NULL;
  printf("Destruction! [DumpRea::~DumpRea]\n");
  }

/* ---------------------------------------------------------------------- */

void DumpRea::write_header(int ndump)
  {
  //printf ("Header.\n");
  
  if (update->ntimestep == 0) return;
  
  fprintf (fp, "\n\n\n\n# timestep %d\n", update->ntimestep);
  }

/* ---------------------------------------------------------------------- */

int DumpRea::count()
{
  //printf ("Count.\n");
  /*if (comm->me == 0) return 1;
  else               return 0;*/
  return atom->nbonds;
}

/* ---------------------------------------------------------------------- */

int DumpRea::pack()
  {
  //printf ("Pack.\n");
  return 0;
  }

/* ---------------------------------------------------------------------- */

void DumpRea::write_data(int n, double *buf)
  {
  if (update->ntimestep == 0) return;

  //printf ("DumpRea::write_data - writing now.\n");
  
  //fprintf (fp, "Hallo, hier bin ich!!\n");
  NamesManager *nm = dynamic_cast<AtomVecSRSim*>(atom->avec)->srmodel->names;
  vector<bool>  usedIds;
  
  fprintf (fp, "\n");
  fprintf (fp, "# number of molecules\n");
  fprintf (fp, "%d\n", known_species.size() );
  fprintf (fp, "# the molecules\n");
  for (int i=0 ; i < known_species.size() ; i++)
      {
      fprintf (fp, "%i\n", i );
      }
  fprintf (fp, "# real molecule names:\n");
  for (int i=0 ; i < known_species.size() ; i++)
      {
      usedIds.clear();
      string s = known_species[i]->getMolecule(0)->generateBngl(nm, usedIds);
      fprintf (fp, "# %i %s\n", i, s.c_str() );
      } 
      
  fprintf (fp, "\n\n");
  fprintf (fp, "# number of reactions\n");
  fprintf (fp, "%d\n", known_reactions.size() );
  fprintf (fp, "# the reactions\n");
  for (int i=0 ; i < known_reactions.size() ; i++)
      {
      printReaction(known_reactions[i], fp);
      fprintf (fp, "\n");
      } 
      
  fprintf (fp, "\n");
  
//   for (int i=0 ; i < atom->nlocal ; i++)
//       {
//       int from = atom->tag[i];
//       for ( uint j=0 ; j < atom->num_bond[i] ; j++) 
//           {
//           int to = atom->bond_atom[i][j];
//           int tp = atom->bond_type[i][j];
//           fprintf (fp, "%d %d %d\n", from, to, tp);
//           }
//       }
     
  //printf ("DumpRea::write_data - done writing.\n");
  }
  
/* ---------------------------------------------------------------------- */

int DumpRea::modify_param( int narg, char **arg )
  {
  error->all("DumpRea::modify_param: Illegal modify command - we cannot change this dump any more.");
  return narg;
  }

/* ---------------------------------------------------------------------- */


// returns the id in the known_species vector!
int  DumpRea::checkSpeciesKnown ( LammpsMolecule *lm )
  {
  //printf(" checkSpeciesKnown:  %p\n", lm );
    
  NamesManager *nm = dynamic_cast<AtomVecSRSim*>(atom->avec)->srmodel->names;
  vector<bool>  usedIds;
    
    
  // check if it's already known:
  for (int i=0 ; i<known_species.size() ; i++) 
      {
      //printf(" checkSpeciesKnown: testing %p against known species %d\n", lm, i);
      if ( known_species[i]->matchMolecule(lm) ) 
         {
	 usedIds.clear();
         string s = known_species[i]->getMolecule(0)->generateBngl(nm, usedIds);
         //printf("DumpRea::checkSpeciesKnown:  remembered species %d, %s\n", i, s.c_str() );
         return i;
         }
      }
      
  // since we arrived here, it's not yet known - so remember in known_species:
  //printf("DumpRea::checkSpeciesKnown:  remember a new template!\n");
  MultiMolReactantTemplate *species = new MultiMolReactantTemplate( lm );  // does a deep 
  known_species.push_back( species );
  //printf("DumpRea::checkSpeciesKnown:  added species %d\n", known_species.size()-1 );
  return ( known_species.size()-1 );
  }
  
  
// returns the id in the reaction in known_reactions!
//  if not yet known, reaction will be registered!
int DumpRea::checkReactionKnown( vector< vector<int> > &rea )
  {
  for (int i=0 ; i<known_reactions.size() ; i++) 
      if ( reactionEquals(rea, known_reactions[i]) ) return i;

  // since we arrived here, it's not yet known - so remember in known_reactions:
  known_reactions.push_back( rea );
  }
  
  
void DumpRea::printReaction( vector< vector<int> > &rea, FILE* fout) 
  {
  if (fout == NULL) 
     { 
     for (int i=0 ; i<rea[0].size() ; i++)
         printf ( "%d ", rea[0][i] );
     printf ( " ->  ");
     for (int i=0 ; i<rea[1].size() ; i++)
         printf ( "%d ", rea[1][i] );
     printf ( "\n");
     }
  else 
     {
     for (int i=0 ; i<rea[0].size() ; i++)
         fprintf ( fout, "1 %d ", rea[0][i] );
     fprintf ( fout, " ->  ");
     for (int i=0 ; i<rea[1].size() ; i++)
         fprintf ( fout, "1 %d ", rea[1][i] );
     }
  }
  
  
bool DumpRea::reactionEquals( vector< vector<int> > &rea1, vector< vector<int> > &rea2) 
  {
  //printf ("  reactionEquals: ");  printReaction(rea1, NULL);
  //printf ("     to reaction: ");  printReaction(rea2, NULL);
    
  if (rea1[0].size() != rea2[0].size()) return false;  // left  site of reactions
  if (rea1[1].size() != rea2[1].size()) return false;  // right site of reactions
    
  for (int i=0 ; i<rea1[0].size() ; i++)
      if (rea1[0][i] != rea2[0][i]) return false;
  for (int i=0 ; i<rea1[1].size() ; i++)
      if (rea1[1][i] != rea2[1][i]) return false;
  
  //printf ("  ->true\n");
  return true;
  }
  
  

void DumpRea::setCurrentReactionLHS(LammpsMolecule *lmA, LammpsMolecule *lmB)
  {
  //printf ("Start setCurrentReactionLHS %p %p\n", lmA, lmB);
    
  // if lmA and lmB are connected, they are the same molecule, so we set lmB to zero.
  assert( lmA != NULL );
  if (lmB != NULL && lmA->checkIfConnected(lmB) ) lmB = NULL;
  
  
  
  current_lhs.clear();
  current_lhs.push_back( checkSpeciesKnown(lmA) );
  if (lmB != NULL) 
     {
     current_lhs.push_back( checkSpeciesKnown(lmB) );
  
     // swap first/second reactant, such that the first one is always smaller/equal
     if (current_lhs[0] > current_lhs[1]) {
        int buf = current_lhs[0];
        current_lhs[0] = current_lhs[1];
        current_lhs[1] = buf;
        }
     }
  
  //printf (" Finish setCurrentReactionLHS\n");
  }
  
  
void DumpRea::setCurrentReactionRHS(LammpsMolecule *lmC, LammpsMolecule *lmD)
  {
  //printf ("Start setCurrentReactionRHS %p %p\n", lmC, lmD);
  
  // if lmC and lmD are connected, they are the same molecule, so we set lmD to zero.
  if (lmD != NULL && lmC->checkIfConnected(lmD) ) lmD = NULL;
    
  // products
  vector<int> rhs;
                   rhs.push_back( checkSpeciesKnown(lmC) );
  if (lmD != NULL) {
     rhs.push_back( checkSpeciesKnown(lmD) );
     
     // swap first/second reactant, such that the first one is always smaller/equal
     if (rhs[0] > rhs[1]) {
        int buf = rhs[0];
        rhs[0] = rhs[1];
        rhs[1] = buf;
        }
     
     }
  
  // reaction:
  vector< vector<int> > rea;
  rea.push_back(current_lhs);
  rea.push_back(rhs);
  
  int res = checkReactionKnown( rea );
  
  //printf("Checked reaction now has id %d\n    ", res);
  //printReaction( rea, NULL );
  
  }



