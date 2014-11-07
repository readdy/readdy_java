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

#include "dump_srsim.h"
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

DumpSRSim::DumpSRSim(LAMMPS *lmp, int narg, char **arg) : Dump(lmp, narg, arg)
{
  if (narg != 5) error->all("DumpSRSim::DumpSRSim: Illegal dump command");

  format_default = NULL;
  
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  assert( avec != NULL );
  
  /*observedTemplates.push_back( 0 );
  observedTemplates.push_back( 1 );
  observedTemplates.push_back( 2 );
  observedTemplates.push_back( 3 );
  observedTemplates.push_back( 4 );*/
  
  // one-time file open
  // multifile=0;
  if (multifile == 0) openfile();
  
  size_one = 1;  // don't ask me why, but if we don't set it, it goes DivedeByZero!
}

/* ---------------------------------------------------------------------- */


DumpSRSim::~DumpSRSim()
  {
  printf("Destruction! [DumpSRSim::~DumpSRSim]\n");
  }

/* ---------------------------------------------------------------------- */

void DumpSRSim::write_header(int ndump)
  {
  //printf ("Header.\n");
  
  if (update->ntimestep != 0) return;
  if (observedTemplates.size() == 0) add_all_templates();
  
  fprintf (fp, "#time");
  
  for (uint i=0 ; i<observedTemplates.size() ; i++)
      {
      //int tid = observedTemplates[i];
      //string tName = avec->rset->getRT(tid)->getName();
      string tName = avec->srmodel->observer->getName(i);
      fprintf (fp, "     %16s", tName.c_str());
      /*if (avec->rset->getRT(tid)->getRTType() == ReactantTemplate::MultiMolRT) fprintf (fp, "[MMRT]");
      if (avec->rset->getRT(tid)->getRTType() == ReactantTemplate::BoundRT   ) fprintf (fp, "[BRT] ");
      if (avec->rset->getRT(tid)->getRTType() == ReactantTemplate::SiteRT    ) fprintf (fp, "[SRT] ");
      if (avec->rset->getRT(tid)->getRTType() == ReactantTemplate::ModifRT   ) fprintf (fp, "[MRT] ");*/
      }
  
  fprintf (fp, "\n");
  }

/* ---------------------------------------------------------------------- */

int DumpSRSim::count()
{
  //printf ("Count.\n");
  /*if (comm->me == 0) return 1;
  else               return 0;*/
  return 1;
}

/* ---------------------------------------------------------------------- */

int DumpSRSim::pack()
  {
  //printf ("Pack.\n");
  return 0;
  }

/* ---------------------------------------------------------------------- */

void DumpSRSim::write_data(int n, double *buf)
  {
  RuleSet *rs = avec->rset;
  
  calculate_quantities();
  
  //printf ("Write.\n");
  fprintf (fp,"%5d",update->ntimestep);
  for (uint i=0 ; i<observedTemplates.size() ; i++)
      {
      int tid = observedTemplates[i];
      
      fprintf (fp, "%21d", quantities[i]);
      }
  fprintf (fp,"\n");
  }
  
/* ---------------------------------------------------------------------- */

int LAMMPS_NS::DumpSRSim::modify_param( int narg, char **arg )
  {
  assert( false );    // Nope, we want to add templates if they aren't on yet...
  
  if (narg<1) error->all("DumpSRSim::modify_param: Illegal modify command");
  if (strcmp(arg[0], "addTemplates") != 0)
     error->all("DumpSRSim::modify_param: the only legal command is 'addTemplates'!");
  
  for (int i=1 ; i<narg ; i++)
      {
      // find Template id:
      int myId = -1;
      for (int rt=0 ; rt<avec->rset->numTemplates() ; rt++)
          {
          string rtName = avec->rset->getRT(rt)->getName();
          if ( rtName.compare(arg[i]) == 0 )
             {
             myId = rt;
             break;
             }
          }
          
      if (myId == -1) error->all( (string("DumpSRSim::modify didn't find Template with name: ")+arg[i]).c_str() );
      observedTemplates.push_back( myId );
      }
  
  return narg;
  }

/* ---------------------------------------------------------------------- */

void LAMMPS_NS::DumpSRSim::add_all_templates( )
  {
  
  observedTemplates = avec->srmodel->observer->getObsTidVector();
  
  /*for (int rt=0 ; rt<avec->rset->numTemplates() ; rt++)
      {
      //if (avec->rset->getRT(rt)->isUsableAs( ReactantTemplate::reactingRT ))
      if (avec->rset->getRT(rt)->isUsableAs( ReactantTemplate::observableRT ))
         {
         observedTemplates.push_back( rt );
         printf("DumpSRSim::add_all_templates ... adding Template: %d - %s\n", rt, avec->rset->getRT(rt)->getName().c_str());
         }
      }*/
  }

/* ---------------------------------------------------------------------- */

void LAMMPS_NS::DumpSRSim::calculate_quantities( )
  {
  RuleSet *rset   = avec->srmodel->ruleset;
  int      nlocal = atom->nlocal;

  // only necessary when write_header() wasn't used before... e.g. when we're in a restart!
  if (observedTemplates.size() == 0) add_all_templates();

  
  // set to size zero
  quantities.resize( observedTemplates.size(), 0 );
  for (uint t=0 ; t<quantities.size() ; t++) quantities[t] = 0;
  
  for (uint t=0 ; t<observedTemplates.size() ; t++)
      {
      ReactantTemplate *rt = rset->getRT( observedTemplates[t] );
      
      // maybe srsim already calculated it?
      if ( rt->isUsableAs(ReactantTemplate::reactingRT) )
         {
#ifndef USE_TEMPL_AFFIL_MANAGER
         quantities[t] = avec->amount_templs[ observedTemplates[t] ];
#else
         quantities[t] = avec->affiliations.numTemplAffils(t);
#endif
         if (rt->getRTType() == ReactantTemplate::MultiMolRT) quantities[t] /= rt->numMolecules();
         continue;
         }
      //printf ("Calculating %s-quantity:\n", rt->getName().c_str());
         
      // not precalculated, let's do it ourselves:
      for (int i=0 ; i<nlocal ; i++)
          {
          LammpsMolecule lm(i);
          if (rt->matchMolecule(&lm)) quantities[t]++;
          }
      // scale MMRTs:
      if (rt->getRTType() == ReactantTemplate::MultiMolRT) quantities[t] /= rt->numMolecules();
      
      //printf ("              quantity = %d\n", quantities[t]);
      }
  }

/* ---------------------------------------------------------------------- */

