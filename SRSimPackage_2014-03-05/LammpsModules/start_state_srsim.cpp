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
#include "start_state_srsim.h"
#include "atom_vec_srsim.h"
#include "atom.h"
#include "domain.h"
#include "comm.h"
#include "error.h"
#include "force.h"
#include "bond.h"
#include "angle.h"
#include "dihedral.h"
#include "pair.h"
#include "neighbor.h"

#include <SRSim/defs.h>
#include <assert.h>
#include <sstream>
#include <string.h>

#include <SRSim/sr_model.h>
#include <SRSim/start_state_definition.h>



/*#define  FORCE_OF_BOND            "2.0"
#define  FORCE_REPULSION          "2.0"
#define  FORCE_REPULSION_CUTOFF   "2.5"
#define  FORCE_AT_ANGLE           "52.0"*/


using namespace SRSim_ns;

namespace LAMMPS_NS {

StartStateSRSim::StartStateSRSim(class LAMMPS *lmp) : Pointers(lmp)
  {
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  }


StartStateSRSim::~StartStateSRSim()
  {
  printf("Destruction! [StartStateSRSim::~StartStateSRSim]\n");
  }


void StartStateSRSim::command( int argc, char **argv )
  {
  //Pointers::atom->map(123);
  if (argc < 1) error->all("StartStateSRSim::command: start_state should have at least 1 param {coeffs, atoms}");

  if      (strcmp(argv[0], "atoms" ) == 0) createAtoms ();
  else if (strcmp(argv[0], "coeffs") == 0) 
     {
     if (argc != 1) error->all("StartStateSRSim::command: start_state coeffs needs no further params.");
     setAmountsAndCoeffs ();
     }
  else 
     error->all("StartStateSRSim::command: start_state didn't recognize command!");
  
  //assert (false);
  }

  
  
/**
 *   We should add the now-fixed force parameters as parameters to the start_state_srsim command!
 */

void StartStateSRSim::setAmountsAndCoeffs()
  {
  printf ("StartStateSRSim::setAmountsAndCoeffs ...\n");
  GeometryDefinition  *geo = avec->srmodel->geo;
  MoleculeTypeManager *mtm = avec->srmodel->mtm;
  
  char **fun = new char*[5];
  for (int i=0 ; i<5 ; i++) fun[i] = new char[50];
  
  // styles 
  /*stringstream sst;
               sst << "soft " << geo->getMaxRadius();     // e.g. "soft 2.5"
  printf ("pair style = '%s'\n",sst.str().c_str());
  force->create_pair (sst.str().c_str());*/
  force->create_pair    ("soft");
  force->create_bond    ("harmonic");
  force->create_angle   ("harmonic");
  if (geo->getProperty(GPT_Option_Dihedrals) > 0.5) force->create_dihedral("harmonic/srsim");
  printf ("   ... force models\n");
  
  // numbers:
  atom->nbondtypes     = geo->numBondTypes();      // stupid lammps isn't beginning to count from zero!!!
  atom->nangletypes    = geo->numAngleTypes();     // so there's the minus 1 everywhere...
  atom->ndihedraltypes = geo->numDihedralTypes(); 
  atom->ntypes         = mtm->numMolIDs()-1;
  printf ("   ... atom/bond/angle numbers %d %d %d\n", mtm->numMolIDs()-1, geo->numBondTypes(), geo->numAngleTypes());
  
  int maxBonds = mtm->maxBondsPerMol();
  atom->bond_per_atom     = maxBonds;
  atom->angle_per_atom    = geo->maxAnglesPerMol();  //( maxBonds<=1 )?( 0 ):( 1+(maxBonds-2)*2 );
  atom->dihedral_per_atom = geo->maxDihedralsPerMol();
  atom->improper_per_atom = 0;
  printf ("   ... maximum numbers\n");
  
  // masses:
  for (int i=1 ; i<=atom->ntypes ; i++)
      atom->set_mass (i, geo->getProperty(GPT_Mol_Mass,i));
  printf ("   ... masses\n");

  // pair-cutoff distance:
  sprintf (fun[0], "%f", (2.0*geo->getMaxProperty(GPT_Mol_Rad)) );
  force->pair->settings(1,fun);
  // don't forget to expand the neighbor cutoff:
  sprintf (fun[0], "%f", (2.0*geo->getMaxProperty(GPT_Mol_Rad)) );
  sprintf (fun[1], "bin");
  neighbor->set(2, fun);
  printf ("   ... pair-cutoffs and neighbor skin dist\n");
  
  // pair-forces... means repulsion because of molecule radii  
  for (int i=1 ; i<=atom->ntypes ; i++)
      for (int j=1 ; j<=atom->ntypes ; j++)
          {
          if (i>j) continue;   // i<=j required by lammps!
          sprintf (fun[0], "%d", i);
          sprintf (fun[1], "%d", j);
          sprintf (fun[2], "%f", geo->getProperty(GPT_Force_Repulsion));
          sprintf (fun[3], "%f", geo->getProperty(GPT_Force_Repulsion));
          //printf ("pre get Radius: %s %s %s %s %s\n",fun[0],fun[1],fun[2],fun[3]);
          sprintf (fun[4], "%f", geo->getProperty(GPT_Mol_Rad,i) + geo->getProperty(GPT_Mol_Rad,j));
          
          //printf ("      pre coeff pair-force: %s %s %s %s %s\n",fun[0],fun[1],fun[2],fun[3],fun[4]);
          force->pair->coeff(5,(char**)fun);
          }
  printf ("   ... pair forces\n");
            
  // bond-distances:
  for (int i=1 ; i<=atom->nbondtypes ; i++)
      {
      //printf ("pre bond-type (%d/%d = %d) :",i,atom->nbondtypes, geo->numBondTypes() );
      //printf (" Bond-Dist %d = %f with force = %f \n", i, geo->getBondDistance(i-1), geo->getProperty(GPT_Force_Bond) );
      //printf ("   Setting force->bond->coeff()\n");
      sprintf (fun[0], "%d", i);
      sprintf (fun[1], "%f", geo->getProperty(GPT_Force_Bond));
      sprintf (fun[2], "%f", geo->getBondDistance(i-1));
      force->bond->coeff(3,(char**)fun);
      
      }
  printf ("   ... bond-distances %d\n", atom->nbondtypes);
  
  // bond-angles:
  for (int i=1 ; i<=atom->nangletypes ; i++)
      {
      sprintf (fun[0], "%d", i);
      //sprintf (fun[1], "%f", geo->getProperty(GPT_Force_Angle));
      sprintf (fun[1], "%f", geo->getAngleForce(i-1));
      sprintf (fun[2], "%f", geo->getAngle     (i-1));
        
      force->angle->coeff(0,3,(char**)fun);
      
      //printf ("      bond-angle (%d/%d) = %f %f\n",i,atom->nangletypes, ang, geo->getProperty(GPT_Force_Angle) );
      }
  printf ("   ... bond-angles\n");
          
  // dihedral-angles:
  for (int i=1 ; i<=atom->ndihedraltypes ; i++)
      {
      sprintf (fun[0], "%d", i);
      sprintf (fun[1], "%f", geo->getProperty(GPT_Force_Dihedral) );
      
      sprintf (fun[2], "1");             // the 'd' - parameter, -1 means cis, +1 means trans, if no angle is given!
      sprintf (fun[3], "1");              // the 'n' - parameter... periodicity! (irrelev. for us.)
      
      sprintf (fun[4], "%f", geo->getDihedral(i-1) );
      
      force->dihedral->coeff(0,5,(char**)fun);
      
      //printf ("      bond-angle (%d/%d) = %f \n",i,atom->nangletypes, ang);
      }
  printf ("   ... bond-dihedrals %d\n", atom->ndihedraltypes);
          
  for (int i=0 ; i<5 ; i++) delete[] fun[i];
  delete[] fun; 
  //assert (false);   // let's see until here first...
  
  printf ("StartStateSRSim::setAmountsAndCoeffs: done setting up stuff.\n");
  }
  
  

void StartStateSRSim::createAtoms( )
  {
  AtomVecSRSim         *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  StartStateDefinition *ssd  = avec->srmodel->sstate;
  
  double x0 = domain->sublo[0], dx = domain->subhi[0] - domain->sublo[0];
  double y0 = domain->sublo[1], dy = domain->subhi[1] - domain->sublo[1];
  double z0 = domain->sublo[2], dz = domain->subhi[2] - domain->sublo[2];
  
  printf ("sulo0 = %f\n", domain->sublo[0] );
  printf ("suhi0 = %f\n", domain->subhi[0] );
  printf ("sulo1 = %f\n", domain->sublo[1] );
  printf ("suhi1 = %f\n", domain->subhi[1] );
  printf ("sulo2 = %f\n", domain->sublo[2] );
  printf ("suhi2 = %f\n", domain->subhi[2] );
  
  int numMols = ssd->numItems2Create ();
  printf ("  Will create %d atoms\n", numMols);
  for (int i=0 ; i<numMols ; i++)
      {
      StartStateDefinition::Element e = ssd->getNextItem();
      
      //printf ("trying to add Atom %d ...\n", atom->nlocal+1);
      avec->addTemplate2Sim (e.rt, e.x*dx+x0, e.y*dy+y0, e.z*dz+z0);
      //printf ("### natoms=%f nlocal=%d \n",atom->natoms, atom->nlocal);
      }
      
  
  }

  
/*void StartStateSRSim::addTemplate2Sim( SRSim_ns::ReactantTemplate * rt, double x, double y, double z )
  {
  MoleculeTypeManager *mtm = avec->srmodel->mtm;

  // what are we going to do in muliproc-usage?
  assert( comm->me == 0 );
      
  assert( rt->getGeo() != NULL );
  
  assert( rt->isUsableAs(ReactantTemplate::creatableRT) );
  //assert( false );

  double x0 = domain->sublo[0], xm = domain->subhi[0];
  double y0 = domain->sublo[1], ym = domain->subhi[1];
  double z0 = domain->sublo[2], zm = domain->subhi[2];
    
  // add:
  int nOld = atom->nlocal;                         // the old local atom count...
  for (int m=0 ; m<rt->numMolecules() ; m++)
      {
      TemplMolecule   *tm    = rt->getMolecule(m);
      int              mType = tm->getType();
      SRSim_ns::Coords co    = rt->getGeo()->getCoords(m);
      
      // are all sites of this molecule defined?
      if (tm->numSites() != mtm->numSites(tm->getType()))
         printf("############\n  Warning: in Template %s not all sites are defined as they should be!\n##########\n", rt->getName().c_str() );
      
      tm->setRealization (m);
      
      if (x+co.x[0] < x0) x -= x+co.x[0] - x0 - 0.01;
      if (y+co.x[1] < y0) y -= y+co.x[1] - y0 - 0.01;
      if (z+co.x[2] < z0) z -= z+co.x[2] - z0 - 0.01;
      if (x+co.x[0] > xm) x -= x+co.x[0] - xm + 0.01;
      if (y+co.x[1] > ym) y -= y+co.x[1] - ym + 0.01;
      if (z+co.x[2] > zm) z -= z+co.x[2] - zm + 0.01;
      
      assert(x+co.x[0]>=x0);
      assert(y+co.x[1]>=y0);
      assert(z+co.x[2]>=z0);
      assert(x+co.x[0]<=xm);
      assert(y+co.x[1]<=ym); //printf ("z = %f   co = %f\n",z,co.x[2]);
      assert(z+co.x[2]<=zm);
      
      double dVect[] = {x+co.x[0], y+co.x[1], z+co.x[2]};
      avec->create_atom(mType, dVect);
      }
  
  
  //printf ("after nmax = %d  nlocal = %d  \n", atom->nmax, atom->nlocal);
  
  // new total # of atoms
  //double nlocal = atom->nlocal;
  //MPI_Allreduce(&nlocal,&atom->natoms,1,MPI_DOUBLE,MPI_SUM,world);
  assert (comm->me == 0);
  atom->natoms = atom->nlocal;
  
  // clean up: 
  // I don't know why to do it this way, but that's what create_atoms.cpp does!
  assert (atom->natoms <= 0x7FFFFFFF);   // MAXATOMS from create_atoms.cpp, line 28
  assert (atom->map_style == 1);         // means a map-array!
  atom->tag_extend();
  atom->map_init();
  atom->map_set();
    
  // let's do the bonding and site modifications:
  for (int m=0 ; m<rt->numMolecules() ; m++)
      {
      TemplMolecule *tm = rt->getMolecule(m);
      tm->setRealization( -1 );
      for (int s=0 ; s<tm->numSites() ; s++)
          {
          // site modification?
          bool modified = false;
          int rmSiteCount = mtm->numSites(tm->getType());
          for (int rms=0 ; rms < rmSiteCount ; rms++)            // searching for the right site to modify:
              {
              //printf ("Searching for Site %d and this is %d [M%d S%d RMS%d]\n",tm->getSiteType(s), mtm->getSiteType(tm->getType(),rms), m,s,rms );
              
              if (avec->site_modified[nOld+m][rms]    != -666)               continue;
              if (mtm->getSiteType(tm->getType(),rms) != tm->getSiteType(s)) continue;
              avec->site_modified[nOld+m][rms] = tm->getModificationAtSite(s);
              modified = true;
              //printf ("    mod[%d][%d] = %d\n", nOld+m, rms, avec->site_modified[nOld+m][rms] );
              break;
              }
          assert( modified );
          
          // bonding:          
          TemplMolecule *tm2 = dynamic_cast<TemplMolecule*>( tm->getMoleculeAtSite(s) );
          if (tm2 == NULL)                 continue;
          if (tm2->getRealization() == -1) continue;
          
          int sType2 = tm->getSite(s)->getOther()->getType();
          
          int rea1  = nOld + m;
          int rea2  = nOld + tm2->getRealization();
*/
//          int site1 = avec->findAllFittingSites (rea1, tm ->getSiteType(s), -1/*needModif*/, true/*needFree*/)[0];
//          int site2 = avec->findAllFittingSites (rea2, sType2             , -1/*needModif*/, true/*needFree*/)[0];
/*          
          avec->addNewBond(rea1, atom->tag[rea2], site1, site2);
          }
          
      // Do all mol-sites have their proper modifications? None is -666?
      for (int rms=0 ; rms<mtm->numSites(tm->getType()) ; rms++)
          if (avec->site_modified[nOld+m][rms] == -666) avec->site_modified[nOld+m][rms] = -1;
      }
  
  }*/
  
  
  
  
  
}






