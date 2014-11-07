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

#include "dump_bonds.h"
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

DumpBonds::DumpBonds(LAMMPS *lmp, int narg, char **arg) : Dump(lmp, narg, arg)
{
  printf ("DumpBonds::DumpBonds - constructing.\n");
    
  if (narg != 5 && narg != 6) error->all("DumpBonds::DumpBonds: Illegal dump command: <id> <group> bonds <every> <file> <BOND_SET|BNGL_GRAPHS>");

  format_default = NULL;
  
  // standard dump-type is BOND_SET
  dump_type = BOND_SET;
  if (narg == 6) 
     {
     if      (strcmp( arg[5],"BOND_SET")    == 0 ) dump_type = BOND_SET;
     else if (strcmp( arg[5],"BNGL_GRAPHS") == 0 ) dump_type = BNGL_GRAPHS;
     else
         error->all("DumpBonds::DumpBonds: Illegal dump command: allowed dump types are (BOND_SET|BNGL_GRAPHS)");
     }
  
  /*observedTemplates.push_back( 0 );
  observedTemplates.push_back( 1 );
  observedTemplates.push_back( 2 );
  observedTemplates.push_back( 3 );
  observedTemplates.push_back( 4 );*/
  
  // one-time file open
  // multifile=0;
  if (multifile == 0) openfile();
  
  size_one = 1;  // don't ask me why, but if we don't set it, it goes DivedeByZero!
  
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  assert( avec != NULL );
  
  printf ("DumpBonds::DumpBonds - finished.\n");
}

/* ---------------------------------------------------------------------- */


DumpBonds::~DumpBonds()
  {
  printf("Destruction! [DumpBonds::~DumpBonds]\n");
  }

/* ---------------------------------------------------------------------- */

void DumpBonds::write_header(int ndump)
  {
  //printf ("Header.\n");
  
  //if (update->ntimestep != 0) return;
  
  fprintf (fp, "# timestep %d %d", update->ntimestep, atom->nbonds);
  fprintf (fp, "\n");
  }

/* ---------------------------------------------------------------------- */

int DumpBonds::count()
{
  //printf ("Count.\n");
  /*if (comm->me == 0) return 1;
  else               return 0;*/
  return atom->nbonds;
}

/* ---------------------------------------------------------------------- */

int DumpBonds::pack()
  {
  //printf ("Pack.\n");
  return 0;
  }

/* ---------------------------------------------------------------------- */

void DumpBonds::write_data(int n, double *buf)
  {
  //printf ("DumpBonds::write_data - writing now.\n");
    
  // Just write a list of realized bonds to the output file.
  if (dump_type == BOND_SET) 
     {
     for (int i=0 ; i < atom->nlocal ; i++)
         {
         int from = atom->tag[i];
         for ( uint j=0 ; j < atom->num_bond[i] ; j++) 
             {
             int to = atom->bond_atom[i][j];
             int tp = atom->bond_type[i][j];
             fprintf (fp, "%d %d %d\n", from, to, tp);
             }
         }
     }
  
  // Write BNGL graphs of all the simulation molecules to the file:
  else if (dump_type == BNGL_GRAPHS)
     {
     // we need to blacklist the graphs that we already had.
     vector<bool> blacklist(atom->nlocal+1, false);
     //vector<bool> blacklist;
     NamesManager *names = avec->srmodel->names;
      
     NamesManager  species( true /*single user*/ );   // remembers the graphs that we have in the sim.
     vector<int>   quantities;
     
     for (int i=0 ; i < atom->nlocal ; i++)
         {
         // continue, if the molecule was visited yet:
         int uid = atom->tag[i];
         //printf ("DumpBonds::write_data - mol uid = %d.\n", uid);
         //printf ("DumpBonds::write_data - blacklist size = %d.\n", blacklist.size());
         //printf ("DumpBonds::write_data - mol ixd = %d.\n", i);
         
         if (uid+1 <= blacklist.size() )
            if ( blacklist[uid] == true )
               continue;
         
         //printf ("DumpBonds::write_data - trying to obtain LM = %d.\n", i);
         LammpsMolecule lm(i);
         //printf ("DumpBonds::write_data - gnererated LM: uid= %d.\n", lm.getUniqueID() );
         
         vector<bool> localBlacklist (atom->nlocal+1, false);
         string bngl = lm.generateBngl(names, localBlacklist);
         int    id   = species.getID (bngl);
         if (id+1 > quantities.size()) quantities.resize(id+1, 0);
         quantities[id]++;
         //printf ("DumpBonds::write_data - generated bngl.\n");
         
         // add the local Blacklist from this molecule to the global one for this dump step:
         if (blacklist.size() < localBlacklist.size())
            blacklist.resize(localBlacklist.size(), false);
         for (int j=0 ; j<localBlacklist.size() ; j++)
             if (localBlacklist[j] == true) 
                {
                assert( blacklist[j] == false );  // this should be the first time, that it will be set to true.
                blacklist[j] = true;
                }
         }
     
     // new write the species with its quantities:
     for (int i=0 ; i<quantities.size() ; i++) 
         {
         if (quantities[i] == 0) continue;
         fprintf (fp, "  %s %d\n",species.getName(i).c_str(), quantities[i] );
         }
     
     }
  //printf ("DumpBonds::write_data - done writing.\n");
  }
  
/* ---------------------------------------------------------------------- */

int DumpBonds::modify_param( int narg, char **arg )
  {
  error->all("DumpBonds::modify_param: Illegal modify command - we cannot change this dump any more.");
  return narg;
  }

/* ---------------------------------------------------------------------- */


