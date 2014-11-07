//
// C++ Implementation: start_state_srsim
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "runmodif_srsim.h"
#include "atom_vec_srsim.h"
#include "compute_reapot_atom.h"
#include "modify.h"
#include "atom.h"
#include "domain.h"
#include "comm.h"
#include "error.h"
#include "force.h"
#include "bond.h"
#include "angle.h"
#include "pair.h"

#include <SRSim/defs.h>
#include <assert.h>
#include <sstream>
#include <string.h>
#include <stdlib.h>

#include <SRSim/sr_model.h>
#include <SRSim/sr_error.h>
#include <SRSim/neuneu/neuneu_rt_builder.h>
#include <SRSim/start_state_definition.h>



/*#define  FORCE_OF_BOND          "2.0"
#define  FORCE_REPULSION          "2.0"
#define  FORCE_REPULSION_CUTOFF   "2.5"
#define  FORCE_AT_ANGLE           "52.0"*/


using namespace SRSim_ns;

namespace LAMMPS_NS {

RunModifSRSim::RunModifSRSim(class LAMMPS *lmp) : Pointers(lmp)
  {
  printf ("######################### Construction:  RunModifSRSim::RunModifSRSim\n");
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  }


RunModifSRSim::~RunModifSRSim()
  {
  printf("Destruction! [RunModifSRSim::~RunModifSRSim]\n");
  }


/**
   Until now, there are the new LAMMPS commands:
    * addMols:   add new molecules in a certain area of the reactor
    * setRigid:  Switch system to rigid-mode
 */
void RunModifSRSim::command( int argc, char **argv )
  {
  //Pointers::atom->map(123);
  if (argc < 1) error->all("RunModifSRSim::command: runmodif_srsim's second parameter should be from: {ruleSys, addMols, setRigid}");

  if (strcmp(argv[0], "addNeuNeuMap" ) == 0) 
     {
     if (argc != 9) error->all("RunModifSRSim::command: runmodif_srsim addMols needs eight further params: <xlo xhi ylo yhi zlo zhi map-file num> .");
     printf ("  trying to create new molecules:\n");
     createAtomsByMap( atof(argv[1]),atof(argv[2]),atof(argv[3]),atof(argv[4]),atof(argv[5]),atof(argv[6]), argv[7], atoi(argv[8]));
     printf ("  trying to create new molecules: ... done.\n");
     }
  else if (strcmp(argv[0], "addMols" ) == 0) 
     {
     if (argc != 9) error->all("RunModifSRSim::command: runmodif_srsim addMols needs eight further params: <xlo xhi ylo yhi zlo zhi template num> .");
     printf ("  trying to create new molecules:\n");
     createAtomsByName( atof(argv[1]),atof(argv[2]),atof(argv[3]),atof(argv[4]),atof(argv[5]),atof(argv[6]), argv[7],atoi(argv[8]));
     printf ("  trying to create new molecules: ... done.\n");
     }
//   else if (strcmp(argv[0], "setRigid" ) == 0) 
//      {
//      setSystemToRigid(argv[1]);
//      }
  else if (strcmp(argv[0], "ruleSys" ) == 0)  /** This will only instruct runmodif_srsim to prepare a rule system initially, when using restart */
     {
     if (argc != 5) error->all("RunModifSRSim::command: runmodif_srsim ruleSys needs four further params: <bnglName, mgeoName, tgeoName, rndSeed> .");

     AtomVecSRSim::SRSimBnglName    = argv[1];
     AtomVecSRSim::SRSimMgeoName    = argv[2];
     AtomVecSRSim::SRSimTgeoName    = argv[3];
     AtomVecSRSim::rndSeed          = atoi(argv[4]);
     AtomVecSRSim::restartInfoAvail = true;
     
     printf ("RunModifSRSim::runmodif_srsim: Ok, SRSim values are set for the next restart!\n");
     }
  else if (strcmp(argv[0], "ignoreBimolecularReactions" ) == 0) 
     {
     if (argc != 2) error->all("RunModifSRSim::command: runmodif_srsim ignoreBimolecularReactions needs one further param: <on/off> .");
     
     ComputeReapotAtom *crpt = NULL;
     for (int i=0 ; i < modify->ncompute ; i++)
         if (strcmp(modify->compute[i]->style,"reapot/atom") == 0) 
            crpt = dynamic_cast<ComputeReapotAtom*>( modify->compute[i] );
     if (crpt == NULL)
        error->all("RunModifSRSim::command: ignoreBimolecularReactions: Sorry - didn't find compute reapot compute to change the bimol value...");
     
     if      ( strcmp(argv[1],"on" )==0 ) crpt->ignoreBimolecularReactions = true;
     else if ( strcmp(argv[1],"off")==0 ) crpt->ignoreBimolecularReactions = false;
     else   
        error->all( ("RunModifSRSim::command: Illegal ignoreBimolecularReactions srsim command: I don't know this parameter: "+string(argv[1])).c_str() ); 
     }
  /*else if (strcmp(argv[0], "addDNATiles" ) == 0) 
     {
     if (argc != 6) error->all("RunModifSRSim::command: runmodif_srsim addDNATiles needs five further params: <Seq1 Seq2 Seq3 Seq4 num> .");
     printf ("  trying to create DNA-Tiles:\n");
     createDNATiles (atof(argv[1]),atof(argv[2]),atof(argv[3]),atof(argv[4]),atof(argv[5]),atof(argv[6]), argv[7],atoi(argv[8]));
     printf ("  trying to create DNA-Tiles: ... done.\n");
     }*/
  else 
     error->all("RunModifSRSim::command: runmodif_srsim didn't recognize command (addMols, addNeuNeuMap, ruleSys are allowed)!");
  
  //assert (false);
  }

  
  
  
  
  
  
  
void RunModifSRSim::createAtomsByMap(double xhi, double xlo, double yhi, double ylo, double zhi, double zlo, char* fname, int num2add)
  {
  
  SRSim_ns::NeuneuRtBuilder nrb( *(avec->srmodel) , fname );
  
  try{
     ReactantTemplate *nrt = nrb.build();
     createAtoms(xhi,xlo,yhi,ylo,zhi,zlo, nrt, num2add);
     } 
  catch (SRSim_ns::SRException *sre)
     {
     //printf (" Blub : %s\n", sre->what.c_str() );
     error->all( (string("RunModifSRSim::createAtomsByMap: Unable to create molecules from map: ")+sre->what).c_str() );
     }
  
  
  
  }
  
  
  
void RunModifSRSim::createAtomsByName(double xhi, double xlo, double yhi, double ylo, double zhi, double zlo, char* templ, int num2add)
  {
  //AtomVecSRSim         *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  RuleSet         *rs  = avec->srmodel->ruleset;
  
  
  // find the right template:
  ReactantTemplate *rt = NULL;
  int nTempls = rs->numTemplates();
  for (int i=0 ; i<nTempls ; i++)
      if (rs->getRT(i)->getName().compare(templ)==0 && rs->getRT(i)->isUsableAs(ReactantTemplate::creatableRT))
         rt = rs->getRT(i);
  if (rt == NULL)
     error->all("RunModifSRSim::createAtoms: Template to be created was not found. Sorry.!");
  printf ("    %d times %s as ordered!\n", num2add, rt->getName().c_str());   
  
  createAtoms(xhi,xlo,yhi,ylo,zhi,zlo, rt, num2add);
  
  }


void RunModifSRSim::createAtoms(double xhi, double xlo, double yhi, double ylo, double zhi, double zlo, ReactantTemplate *rt, int num2add)
  {
  //AtomVecSRSim         *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  RandomGenerator *rg  = avec->srmodel->random;
  
  assert( xlo >= domain->sublo[0] );
  assert( xhi <= domain->subhi[0] );
  assert( ylo >= domain->sublo[1] );
  assert( yhi <= domain->subhi[1] );
  assert( zlo >= domain->sublo[2] );
  assert( zhi <= domain->subhi[2] );
  
  double x0 = xlo, dx = xhi - xlo;
  double y0 = ylo, dy = yhi - ylo;
  double z0 = zlo, dz = zhi - zlo;
  
  // add the molecules:
  for (int i=0 ; i<num2add ; i++)
      {
      //StartStateDefinition::Element e = ssd->getNextItem();
      
      //printf ("trying to add Atom %d ...\n", atom->nlocal+1);
      
      double x = x0 + dx*rg->uniform();
      double y = y0 + dy*rg->uniform();
      double z = z0 + dz*rg->uniform();
      
      avec->addTemplate2Sim (rt, x, y, z);
      //printf ("### natoms=%f nlocal=%d \n",atom->natoms, atom->nlocal);
      }
  
  
  }



  
  
  
  
/**
 *    This function is non-official: It's used to create complex DNA tiles... 
 *      Please rather use your own implementation if you want to introduce
 *      complex structures to the simulator...
 */
/*void RunModifSRSim::createDNATiles(double xhi, double xlo, double yhi, double ylo, double zhi, double zlo, char* templ, int num2add)
  {
  //AtomVecSRSim         *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  RuleSet         *rs  = avec->srmodel->ruleset;
  RandomGenerator *rg  = avec->srmodel->random;
  
  double xlo = domain->sublo[0] + 5.0 ;
  double xhi = domain->subhi[0] - 5.0;
  double ylo = domain->sublo[1] + 5.0 ;
  double yhi = domain->subhi[1] - 5.0;
  double zlo = domain->sublo[2] + 5.0 ;
  double zhi = domain->subhi[2] - 5.0;
  
  double x0 = xlo, dx = xhi - xlo;
  double y0 = ylo, dy = yhi - ylo;
  double z0 = zlo, dz = zhi - zlo;
  
  // add the molecules:
  for (int i=0 ; i<num2add ; i++)
      {
      //printf ("trying to add Atom %d ...\n", atom->nlocal+1);
      
      double x = x0 + dx*rg->uniform();
      double y = y0 + dy*rg->uniform();
      double z = z0 + dz*rg->uniform();

      
      double dVect[] = {x+co.x[0], y+co.x[1], z+co.x[2]};
      create_atom(mType, dVect);
      
      atom->molecule[atom->nlocal-1] = myMolID;  // so rigid body dynamics can be used.

      
      // Clean Up
      assert (comm->me == 0);
      atom->natoms = atom->nlocal;
      
      // clean up: 
      // I don't know why to do it this way, but that's what create_atoms.cpp does!
      assert (atom->natoms <= 0x7FFFFFFF);   // MAXATOMS from create_atoms.cpp, line 28
      assert (atom->map_style == 1);         // means a map-array!
      atom->tag_extend();
      atom->map_init();
      atom->map_set();
  
  
  
      // Do the bonds:
      xyz...()
            
      //printf ("### natoms=%f nlocal=%d \n",atom->natoms, atom->nlocal);
      }
  
  }      */

  
  
  

// void RunModifSRSim::setSystemToRigid( char * onoff )
//   {
//   if (strcmp(onoff, "on" ) == 0) 
//      {
//      avec->rigid_system = true;
//      }
//   else if (strcmp(onoff, "off" ) == 0) 
//      {
//      avec->rigid_system = false;
//      }
//   else 
//      error->all("RunModifSRSim::setSystemToRigid: only the parameters on or off are allowed for 'setRigid'!");
//      
//   int nlocal = atom->nlocal;
//   for (int i=0 ; i<nlocal ; i++)
//       avec->activateAngles(i);
//   }


}


