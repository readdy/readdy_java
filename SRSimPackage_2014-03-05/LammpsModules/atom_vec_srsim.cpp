/* ----------------------------------------------------------------------
			atom_vec_srsim
------------------------------------------------------------------------- */

#include <math.h>   // by Gerdl, for isnan(double)
#include "stdlib.h"
#include <algorithm> // for stl::sort

#include "atom_vec_srsim.h"
#include "atom.h"
#include "domain.h"
#include "modify.h"
#include "fix.h"
#include "memory.h"
#include "error.h"
#include "comm.h"
#include "update.h"

#include <SRSim/defs.h>
#include <assert.h>
#include <SRSim/molecule_type_manager.h>
#include <SRSim/bng_rule_builder.h>
#include <SRSim/names_manager.h>
#include <SRSim/molecule_type_manager.h>

#include "lammps_molecule.h"

using namespace LAMMPS_NS;

#define DELTA 10000

bool   AtomVecSRSim::restartInfoAvail = false;
string AtomVecSRSim::SRSimBnglName;
string AtomVecSRSim::SRSimMgeoName;
string AtomVecSRSim::SRSimTgeoName;
int    AtomVecSRSim::rndSeed;

/* ---------------------------------------------------------------------- */

AtomVecSRSim::AtomVecSRSim(LAMMPS *lmp, int narg, char **arg) :
  SUPERCLASS(lmp, narg, arg)
  {
  printf ("AtomVecSRSim::AtomVecSRSim \n");
  
  mol_Id_Cnt            = 0;
  
  templatesNeedRecalculation = false;
  
  site_bound_tag        = NULL;
  site_other_site       = NULL;
  site_bond_id          = NULL;
  site_modified         = NULL;
  type2numSites         = NULL;
  visited               = NULL;
  refractory            = NULL;
  
  srmodel               = NULL;
  rset                  = NULL;
  reaDumper             = NULL;
  
  // this seems to be the best time/space to init the rule-System: 
  //    (We need to know how the atoms look when we create them!)    
  if (narg == 0 && restartInfoAvail == true )
     {
     printf ("initing AtomVecSRSim from a restart file apparently!!\n");
     setRuleSys( new SRSim_ns::SRModel( rndSeed, SRSimBnglName.c_str(), SRSimMgeoName.c_str(), SRSimTgeoName.c_str(), true/*add Zero Species*/) );
     }
  else if (narg == 4)
     {
     printf ("Using standard approach to initt a reactor with SRSim particles.\n");
     setRuleSys( new SRSim_ns::SRModel( atoi(arg[3])/*random seed*/, arg[0], arg[1], arg[2], true/*add Zero Species*/) );
     }
  else error->all("AtomVecSRSim::AtomVecSRSim: atom_style srsim needs four params. <bnglName, mgeoName, tgeoName, rndSeed> ... look for runmodif_srsim if in trouble.");
  
  printf ("AtomVecSRSim::AtomVecSRSim: Construction finished!\n");
  }

/* ---------------------------------------------------------------------- */

/** using the constructor alone does not suffice to initiate the
    SRSim-Atom-Vector. 
    The Lammps-Command runModifSRSim has to be used to create a
    rule model!
 */
void AtomVecSRSim::setRuleSys( SRSim_ns::SRModel *_srmodel )
  {
  srmodel = _srmodel;
  rset    = srmodel->ruleset;
  
  // as we know the number of templates now, we can init the amount_templs - array.
#ifndef USE_TEMPL_AFFIL_MANAGER
  amount_templs.resize (rset->numTemplates());
#endif
  
  // we will now create the type2numSites array here.
  SRSim_ns::MoleculeTypeManager *mtm = srmodel->mtm;
  int numMolIDs = mtm->numMolIDs();
  
  if (type2numSites != NULL) delete[] type2numSites;
  type2numSites = new int [numMolIDs];   // reserve mem
  for (int i=0 ; i<numMolIDs ; i++)
      type2numSites[i] = mtm->numSites(i);
      
  // as we've got the mtm now, we can init the Lammps-Molecules:
  LammpsMolecule::init(atom, this);
  //printf ("Ping6\n");
  
  printf ("AtomVecSRSim::setRuleSys - Reaction System: \n %s\n\n", rset->toString().c_str() );
  
  printf ("Done setting the Rule System.\n");
  }

/* ---------------------------------------------------------------------- */

AtomVecSRSim::~AtomVecSRSim()
{
  printf("Destruction! [AtomVecSRSim::~AtomVecSRSim]\n");
  // template-arrays:
  /*amount_templs.clear();
  delete[] all_templs;
  for (int i=0 ; i<nmax ; i++)
      delete[] site_templs[i];*/
  
  //num_fitting_overall = 0;
  
  /*free (num_fitting_templates);  num_fitting_templates=NULL;
  for (int i=0 ; i<nmax ; i++)
      if (fitting_templates[i]!=NULL) free( fitting_templates[i] );
  free (fitting_templates    );  fitting_templates    =NULL;*/
  
  delete[] type2numSites; type2numSites = NULL;
  // delete site-specific arrays
  int &nlocal = atom->nlocal;
  if (site_bound_tag != NULL) 
     { for (int i=0 ; i<nlocal ; i++) free (site_bound_tag [i]); free (site_bound_tag); }
  if (site_bond_id != NULL) 
     { for (int i=0 ; i<nlocal ; i++) free (site_bond_id   [i]); free (site_bond_id); }
  if (site_other_site != NULL) 
     { for (int i=0 ; i<nlocal ; i++) free (site_other_site[i]); free (site_other_site); }
  if (site_modified != NULL) 
     { for (int i=0 ; i<nlocal ; i++) free (site_modified  [i]); free (site_modified); }
  
  assert( visited != NULL );
  assert( refractory != NULL );
  //delete[] visited;    visited = NULL;
  //delete[] refractory; refractory = NULL;
  free( visited );    visited = NULL;
  free( refractory ); refractory = NULL;
  
  refractoryList.clear();
  
  LammpsMolecule::staticDelete();
  
  // TODO: Check that the arrays (fitting_templates, site_bound_to and site_modified)
  //       are all reserved or NULL when not...!
}


/* ----------------------------------------------------------------------
   grow atom arrays
   n = 0 grows arrays by DELTA
   n > 0 allocates arrays to size n 
------------------------------------------------------------------------- */

void AtomVecSRSim::grow(int n)
{
  printf ("AtomVecSRSim:::grow(%d)\n", nmax);
  if (srmodel == NULL)
     error->all("The rule system is not initialized yet: use runmodif_srsim ruleSys first! (e.g. runmodif_srsim ruleSys spass.bngl spass.geo spass.tgeo 12345)");

  printf ("grow AtomVecSRSim %d!\n",n);
  SUPERCLASS::grow(n);
  printf ("...grow AtomVecSRSim %d!\n",nmax);

  visited         = (int*) realloc ( visited        , nmax*sizeof(int ));
  refractory      = (int*) realloc ( refractory     , nmax*sizeof(int ));
  site_bound_tag  = (int**)realloc ( site_bound_tag , nmax*sizeof(int*));
  site_modified   = (int**)realloc ( site_modified  , nmax*sizeof(int*));
  site_bond_id    = (int**)realloc ( site_bond_id   , nmax*sizeof(int*));
  site_other_site = (int**)realloc ( site_other_site, nmax*sizeof(int*));
  
#ifndef USE_TEMPL_AFFIL_MANAGER
  site_templs.resize(nmax);
   all_templs.resize(nmax);
#endif
   
  LammpsMolecule::grow(nmax);
  
  printf ("AtomVecSRSim grown to nmax=%d!\n", nmax);
}

/* ---------------------------------------------------------------------- */

// void AtomVecSRSim::reset_special()
// {
//   SUPERCLASS::reset_special();
// }

/* ---------------------------------------------------------------------- */

void AtomVecSRSim::copy(int i, int j)   // i --> j
{
  SUPERCLASS::copy(i, j);
  
  //printf ("AtomVecSRSim::copy %d -> %d\n",i,j);
  
#ifndef USE_TEMPL_AFFIL_MANAGER
  // subtract from amount_templs what has been in j:
  for (uint ii=0 ; ii<all_templs[j].size() ; ii++) amount_templs[ all_templs[j][ii] ]--;

  // fitting templates...  
  site_templs[j] = site_templs[i];
   all_templs[j] =  all_templs[i];

  // add to amount_templs what will be in j:
  for (uint ii=0 ; ii<all_templs[j].size() ; ii++) amount_templs[ all_templs[j][ii] ]++;
#else
  affiliations.copyMol( i,j );
#endif
  
  // site arrays kopieren.
  int numSites = type2numSites[atom->type[j]];
  site_bound_tag [j] = (int*) realloc (site_bound_tag [j], sizeof(int)*numSites);
  site_modified  [j] = (int*) realloc (site_modified  [j], sizeof(int)*numSites);
  site_bond_id   [j] = (int*) realloc (site_bond_id   [j], sizeof(int)*numSites);
  site_other_site[j] = (int*) realloc (site_other_site[j], sizeof(int)*numSites);
  for (int ii=0 ; ii<numSites ; ii++)
      {
      site_bound_tag [j][ii] = site_bound_tag [i][ii];
      site_modified  [j][ii] = site_modified  [i][ii];
      site_bond_id   [j][ii] = site_bond_id   [i][ii];
      site_other_site[j][ii] = site_other_site[i][ii];
      }
      
  // visitation:       // I don't think we should have to use this:
  assert( visited[j] == -1 && visited[i] == -1 );    // if this hurts, uncomment the next line:
  //visited[j] = visited[i];
  
  
  // since molecules that are copied might be in the refractory state,
  // we're copying this as well:
  //assert( refractory[i]==-1 );   // otherwise we'd have to adjust the refractoryList as well!
  //assert( refractory[j]==-1 );
  list<int>::iterator it;
  refractory[j] = refractory[i];
  for (it=refractoryList.begin() ; it!=refractoryList.end() ; it++)
      if (*it == i) {refractoryList.erase(it); break;}
  refractoryList.push_back( j );
  
}

/* ---------------------------------------------------------------------- */

int AtomVecSRSim::pack_comm(int n, int *list, double *buf,
			    int pbc_flag, int *pbc)
{
  //printf ("A\n");
  return SUPERCLASS::pack_comm(n,list,buf,pbc_flag,pbc);
  assert( false );
}

/* ---------------------------------------------------------------------- */

void AtomVecSRSim::unpack_comm(int n, int first, double *buf)
{
  //printf ("B\n");
  SUPERCLASS::unpack_comm(n, first, buf);
  assert( false );
}

/* ---------------------------------------------------------------------- */

int AtomVecSRSim::pack_reverse(int n, int first, double *buf)
{
  //printf ("C\n");
  SUPERCLASS::pack_reverse (n,first, buf);
  assert( false );
}

/* ---------------------------------------------------------------------- */

void AtomVecSRSim::unpack_reverse(int n, int *list, double *buf)
{
  //printf ("D\n");
  SUPERCLASS::unpack_reverse (n,list,buf);
  assert( false );
}

/* ---------------------------------------------------------------------- */

// seems as we don't have to pack the fitting template - Info: Bond and Angle 
// info aren't communicated either...
int AtomVecSRSim::pack_border(int n, int *list, double *buf,
			      int pbc_flag, int *pbc)
{
  //printf ("E\n");
  return SUPERCLASS::pack_border(n,list,buf,pbc_flag,pbc);
  assert( false );
}

/* ---------------------------------------------------------------------- */

int AtomVecSRSim::pack_border_one(int i, double *buf)
{
  //printf ("F\n");
  return SUPERCLASS::pack_border_one(i,buf);
  assert( false );
}

/* ---------------------------------------------------------------------- */

void AtomVecSRSim::unpack_border(int n, int first, double *buf)
{
  //printf ("G\n");
  SUPERCLASS::unpack_border(n,first,buf);
  printf ("AtomVecSRSim::unpack_border - maybe you're trying to use periodic boundary conditions. This is not supported by SRSim at the moment. Sorry.\n");
  assert( false );
}

/* ---------------------------------------------------------------------- */

int AtomVecSRSim::unpack_border_one(int i, double *buf)
{
  //printf ("H\n");
  return SUPERCLASS::unpack_border_one(i,buf);
  assert( false );
}

/* ----------------------------------------------------------------------
   pack data for atom I for sending to another proc
   xyz must be 1st 3 values, so comm::exchange() can test on them 
------------------------------------------------------------------------- */

int AtomVecSRSim::pack_exchange(int i, double *buf)
{
  int m = SUPERCLASS::pack_exchange(i,buf);
  printf ("I = %d    nlocal=%d     ts = %d\n",i,atom->nlocal, update->ntimestep);
  assert(false);
  
  // ############
  
  // subtract from amount_templs what has been in j:
  //for (int ii=0 ; ii<all_templs[i].size() ; ii++) amount_templs[ all_templs[i][ii] ]--;

  // fitting templates...  
//   printf (" A  m = %d\n",m);
//   int num2;
//   int num1 = buf[m++] = site_templs[i].size();
//   for (int ii=0 ; ii<num1 ; ii++)
//       {
//       num2 = site_templs[i][ii].size();
//       for (int iii=0 ; iii<num2 ; iii++) buf[m++] = site_templs[i][ii][iii];
//       printf (" B  m = %d   num2=%d\n",m,num2);
//       }
//   
//   num1 = buf[m++] = all_templs[i].size();
//   for (int ii=0 ; ii<num1 ; ii++) buf[m++] = all_templs[i][ii];
//   printf (" C  m = %d\n",m);
//   
//   // site arrays kopieren.
//   int numSites = type2numSites[atom->type[i]];
//   for (int ii=0 ; ii<numSites ; ii++)
//       {
//       buf[m++] = site_bound_tag [i][ii];
//       buf[m++] = site_modified  [i][ii];
//       buf[m++] = site_bond_id   [i][ii];
//       buf[m++] = site_other_site[i][ii];
//       printf (" D  m = %d\n",m);
//       }
//       
//   // visitation:       // I don't think we should have to use this:
//   assert( visited[i] == -1 );    // if this hurts, uncomment the next line:
//   //visited[j] = visited[i];
//   
//   buf[m++] = refractory[i];
//   
//   
//   buf[0] = m;
// //  assert (false);   // ... not yet implemented!
//   
//   printf ("     bufsize = %d\n",m);
//   return m;

  return -1;
}

/* ---------------------------------------------------------------------- */

int AtomVecSRSim::unpack_exchange(double *buf)
{
  int m = SUPERCLASS::unpack_exchange (buf);
  int j = atom->nlocal - 1;
  printf ("J = %d\n",j);
  assert(false);
  
  
  
  
  // add to amount_templs what will be in j:
  //for (int ii=0 ; ii<all_templs[j].size() ; ii++) amount_templs[ all_templs[j][ii] ]++;
  
  
  
  
  
  /*int j = atom->nlocal - 1;
  
  int jNum = num_fitting_templates[j] = static_cast<int> (buf[m++]);
  fitting_templates[j] = (int*) realloc (fitting_templates[j], sizeof(int)*jNum);  // realloc mem.
  for (int k=0 ; k<jNum ; k++)
      fitting_templates[j][k] = static_cast<int> (buf[m++]);                // write data
  num_fitting_overall += jNum;
  */
  
  //assert (false);   // ... not yet implemented!
  
  return m;
}

/* ----------------------------------------------------------------------
   size of restart data for all atoms owned by this proc
   include extra data stored by fixes
------------------------------------------------------------------------- */

int AtomVecSRSim::size_restart()
{
  int num = SUPERCLASS::size_restart();
      
  for (int i=0 ; i<atom->nlocal ; i++)
      {
      int nsi = type2numSites[atom->type[i]];
      num += 1 + nsi*4;
      }
  
  
  
  //assert (false);   // ... not yet implemented!
  printf (" :::  Restart size ;= %d \n",num);
  
  return num;
}

/* ----------------------------------------------------------------------
   pack atom I's data for restart file including extra quantities
   xyz must be 1st 3 values, so that read_restart can test on them
   molecular types may be negative, but write as positive   
------------------------------------------------------------------------- */

int AtomVecSRSim::pack_restart(int i, double *buf)
{
  int num = 0;
  num += SUPERCLASS::pack_restart(i,buf);
  
  /*for (int i=0 ; i<nlocal ; i++)
      {*/
      int nsi = type2numSites[atom->type[i]];
      for (int is=0 ; is<nsi ; is++)
          {
          buf[num++] = site_bond_id   [i][is];
          buf[num++] = site_other_site[i][is];
          buf[num++] = site_modified  [i][is];
          buf[num++] = site_bound_tag [i][is];
          }
      buf[num++] = refractory[i];
      //}
      
/*  for (int i=0 ; i<refractoryList.size() ; i++)
      {
      buf[num++] = refractory[i];
      }
      
  buf[num++] = rigid_system;
  buf[num++] = mol_Id_Cnt;
  */
  //assert (false);   // ... not yet implemented!
  
  assert( atom->nspecial[i][0] == 0 );
  assert( atom->nspecial[i][1] == 0 );
  assert( atom->nspecial[i][2] == 0 );
  
  printf (" ::::  Packing restart for %d \n",i);
  
  return num;
}

/* ----------------------------------------------------------------------
   unpack data for one atom from restart file including extra quantities
------------------------------------------------------------------------- */

int AtomVecSRSim::unpack_restart(double *buf)
{
  //assert( false );  // because of TemplAffiliationManager

   printf ("unpacking!!!\n");
   int num = SUPERCLASS::unpack_restart(buf);
   
   int i   = atom->nlocal-1;
   int nsi = type2numSites[atom->type[i]];
   
   // resize this molecule's template storage:
#ifndef USE_TEMPL_AFFIL_MANAGER
   assert( site_templs[i].size() == 0 );  // otherwise they were initialized before, which would be strange...
   site_templs[i].resize (nsi);
   assert( all_templs [i].size() == 0 ); 
#endif

   // additional site information
   visited        [i] = -1;
   site_bond_id   [i] = (int*) realloc( NULL/*site_modified[nlocal]*/ , nsi*sizeof(int) );
   site_other_site[i] = (int*) realloc( NULL/*site_modified[nlocal]*/ , nsi*sizeof(int) );
   site_modified  [i] = (int*) realloc( NULL/*site_modified[nlocal]*/ , nsi*sizeof(int) );
   site_bound_tag [i] = (int*) realloc( NULL/*site_bound_to[nlocal]*/ , nsi*sizeof(int) );
     
   for (int is=0 ; is<nsi ; is++)
       {
       site_bond_id   [i][is] = (int)buf[num++];
       site_other_site[i][is] = (int)buf[num++];
       site_modified  [i][is] = (int)buf[num++];
       site_bound_tag [i][is] = (int)buf[num++];
       }
   refractory[i] = (int)buf[num++];
   
   // what about the mol_Id_Cnt:
   if (atom->molecule[i]+1 > mol_Id_Cnt) mol_Id_Cnt = atom->molecule[i]+1;

   
   //assert( atom->nspecial[i][0] == 0 );
   //assert( atom->nspecial[i][1] == 0 );
   //assert( atom->nspecial[i][2] == 0 );
   atom->nspecial[i][0] = 0;   // Why do we need to do this??? I don't know?!
   atom->nspecial[i][1] = 0;   //   I don't even know what this nspecial is all about!!
   atom->nspecial[i][2] = 0;
   
   // we cannot set templates here, yet - so it will have to be done later!
   templatesNeedRecalculation = true;
   
   return num;
}

/* ----------------------------------------------------------------------
   create one atom of itype at coord
   set other values to defaults
   set template affiliation to "nothing"
------------------------------------------------------------------------- */

void AtomVecSRSim::create_atom(int itype, double *coord)
  {
  if (srmodel == NULL)
     error->all("The rule system is not initialized yet: use runmodif_srsim ruleSys first! (e.g. runmodif_srsim ruleSys spass.bngl spass.geo spass.tgeo 12345)");
  
  //printf ("AtomVecSRSim::create_atom : we would like type %d, coords %f %f %f.\n",itype, coord[0],coord[1],coord[2] );
  
  //printf ("AtomVecSRSim::create_atom: \n");
  SUPERCLASS::create_atom(itype, coord);   // increases nlocal by +1
  //printf ("   invoked superclass!\n");
  int nlocal = atom->nlocal - 1;
  assert( ! isnan(atom->x[nlocal][0]) );
  assert( ! isnan(atom->x[nlocal][1]) );
  assert( ! isnan(atom->x[nlocal][2]) );
  
  //printf ("AtomVecSRSim::create_atom : created %d atoms.\n",nlocal);
  //if (nlocal%1000 == 0) printf ("AtomVecSRSim::create_atom : created %d atoms so far...\n",nlocal);
  
  visited              [nlocal] = -1;
  refractory           [nlocal] = -1;
  //printf ("   visited!\n");
  
  int numSites = type2numSites[ itype ];
  //printf ("   A!\n");
  site_bound_tag [nlocal] = (int*) realloc( NULL/*site_bound_to[nlocal]*/ , numSites*sizeof(int) );
  site_modified  [nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  site_bond_id   [nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  site_other_site[nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  //printf ("   CV!\n");
  for (int i=0 ; i<numSites ; i++)
      {site_bound_tag [nlocal][i]=-1; 
       site_modified  [nlocal][i]=-666;            // -666 for initial, unset value!
       site_bond_id   [nlocal][i]=-1; 
       site_other_site[nlocal][i]=-1; 
       }
  //printf ("   sbt & smod vergroessert für numsites=%d!\n", numSites);
      
#ifndef USE_TEMPL_AFFIL_MANAGER
  site_templs[nlocal].resize (numSites);
  
  // assert, all site/mol templates are zero: (otherwise we'll have to clear them, here)
  for (int j=0 ; j<numSites ; j++)
      assert( site_templs[nlocal][j].size() == 0 );
  assert(     all_templs [nlocal].size()    == 0 );
  //printf ("   site_templs vergroessert für numsites=%d!\n", numSites);
#endif
  
  atom->nspecial[nlocal][0] = 0;   // Why do we need to do this??? I don't know?!
  atom->nspecial[nlocal][1] = 0;   //   I don't even know what this nspecial is all about!!
  atom->nspecial[nlocal][2] = 0;
  //printf ("AtomVecSRSim::create_atom... done creating Atom.\n");
  }

/* ----------------------------------------------------------------------
   unpack one line from Atoms section of data file
   initialize other atom quantities
------------------------------------------------------------------------- */

void AtomVecSRSim::data_atom(double *coord, int imagetmp, char **values)
{
  if (srmodel == NULL)
     error->all("The rule system is not initialized yet: use runmodif_srsim ruleSys first! (e.g. runmodif_srsim ruleSys spass.bngl spass.geo spass.tgeo 12345)");
  
  SUPERCLASS::data_atom (coord, imagetmp, values);

  printf ("Data Atom1\n");
  int nlocal = atom->nlocal - 1;
      
  visited              [nlocal] = -1;
  refractory           [nlocal] = -1;
  
  int numSites = type2numSites[ atom->type[nlocal] ];
  printf ("Data Atom2 ; type=%d ; nlocal=%d ; numSites=%d\n", atom->type[nlocal], nlocal, numSites);
  site_bound_tag [nlocal] = (int*) realloc( NULL/*site_bound_to[nlocal]*/ , numSites*sizeof(int) );
  site_modified  [nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  site_bond_id   [nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  site_other_site[nlocal] = (int*) realloc( NULL/*site_modified[nlocal]*/ , numSites*sizeof(int) );
  for (int i=0 ; i<numSites ; i++)
      {site_bound_tag [nlocal][i]=-1; 
       site_modified  [nlocal][i]=-1;
       site_bond_id   [nlocal][i]=-1; 
       site_other_site[nlocal][i]=-1; 
       }
      
#ifndef USE_TEMPL_AFFIL_MANAGER
  site_templs[nlocal].resize (numSites);
#endif
  printf ("Data Atom3\n");
}

/* ----------------------------------------------------------------------
   unpack hybrid quantities from one line in Atoms section of data file
   initialize other atom quantities for this sub-style
------------------------------------------------------------------------- */

int AtomVecSRSim::data_atom_hybrid(int nlocal, char **values)
{
  assert( false );
  return SUPERCLASS::data_atom_hybrid(nlocal, values);
}

/* ----------------------------------------------------------------------
   return # of bytes of allocated memory 
------------------------------------------------------------------------- */

double AtomVecSRSim::memory_usage()
{
  double bytes = SUPERCLASS::memory_usage();
  
  bytes += (nmax * 50)*sizeof(int);
  
  return bytes;
}







/** ----------------------------------------------------------------------
 *  finds all fitting site of a molecule...
 *
 *  idx                Lammps atom index
 *  needType  = -1     don't care for site type
 *  needModif = -1     don't care for modification.
 * ----------------------------------------------------------------------- */
vector<int> LAMMPS_NS::AtomVecSRSim::findAllFittingSites( int idx, int needType, int needModif, bool needFree )
  {
  SRSim_ns::MoleculeTypeManager *mtm = srmodel->mtm;
  int myType   = atom->type[idx];
  int numSites = type2numSites[myType];

  vector<int> sites;
    
  //printf (" Searching for: idx:%d stp:%d mod:%d free:%d \n",idx, needType, needModif, needFree);
  for (int i=0 ; i<numSites ; i++)
      {
      //printf ("   mtp:%d Site-%d: stp:%d mod:%d siteBoundTag:%d \n", myType, i, mtm->getSiteType(myType,i), site_modified[idx][i], site_bound_tag[idx][i]);
      
      if ( mtm->getSiteType(myType,i) != needType && needType !=-1) continue;
      if ( needModif != site_modified[idx][i]     && needModif!=-1) continue;
      if ( needFree  != (site_bound_tag[idx][i]==-1) )              continue;
      sites.push_back( i );    // ok, found.
      
      //printf ("      Taking site %d \n", i);
      }
      
      
  assert (sites.size() > 0);  // when the template says it fits, there 
                              //  should really be at least one fitting site!
  // printf (" Done AtomVecSRSim::findAllFittingSites. Found %d sites. \n",sites.size());
  return sites;
  }
  
  
#ifndef USE_TEMPL_AFFIL_MANAGER
/** ----------------------------------------------------------------------
 *  finds a fitting site of a molecule...
 *       ... by the fitting site_templs...
 *
 *    will randomly choose one of the fitting sites:
 * ----------------------------------------------------------------------- */
int LAMMPS_NS::AtomVecSRSim::findFittingSite( int idx, int needTemplate )
  {
  int myType      = atom->type[idx];
  int numSites    = type2numSites[myType];
  int solutions[100];
  int numSol = 0;
  
  for (int i=0 ; i<numSites ; i++)
      {
      int numTempls = site_templs[idx][i].size();
      
      // ------- begin: only for testing ------
      // TODO: remove this stuff...
      /*bool found=false;
      for (int j=0 ; j<numTempls ; j++)
          if (needTemplate == site_templs[idx][i][j])
             {
             if (! found) found=true;
             else         assert (false);      // here are two fitting sites: we should rather randomize!
             }*/
      // ------- end: only for testing ------
      
      for (int j=0 ; j<numTempls ; j++)
          if (needTemplate == site_templs[idx][i][j])
             {
             assert(numSol < 100);
             solutions[numSol] = i;
             numSol++;
             }
      }
      
  if (numSol==1) return solutions[0];
  else if (numSol>1)
     {
     int r = (double)numSol * srmodel->random->uniform();
     return solutions[r];
     }
  else
     {
     assert (false);  // we do not want to reach this place!!!
     return -1;
     }
      
  }
#endif

/** ----------------------------------------------------------------------
 *  wich Other molecule is linked to molecule 'idx' at site 'site'
 *    returns the local idx, not the tag.
 * ----------------------------------------------------------------------- */
int LAMMPS_NS::AtomVecSRSim::findOtherEnd( int idx, int site )
  {
  int nextTag = site_bound_tag[idx][site];
  
  if (nextTag == -1)
     {
     int tp  = atom->type[idx];
     int num = type2numSites[tp];
     for (int i=0 ; i<num ; i++)
         { printf ("site %d is %d\n", i, site_bound_tag[idx][i] ); }
     }
  //assert (nextTag != -1);  // would be unbound!
  
  // idx of the next Molecule:
  int nextIdx = atom->map(nextTag);
  
  return nextIdx;
  }


// int LAMMPS_NS::AtomVecSRSim::findSiteByTargetTag( int idx, int targetTag )
//   {
//   assert( false );   // this is an old and evil method. should not be used!!
//   
//   int type     = atom->type[idx];
//   int numSites = type2numSites[type];
//   for (int site=0 ; site<numSites ; site++)
//       {
//       /*if (update->ntimestep == 13843)
//          {
//          string sname = srmodel->names->getName(SRSim_ns::NamesManager::SiteName, srmodel->mtm->getSiteType(type, site));
//          printf ("  site %d   [%s]\n", site, sname.c_str());   
//          }*/
//       
//       int sbt = site_bound_to[idx][site];
//           
//       if (sbt == -1)   continue;
//       if (sbt <= -100) sbt = -sbt-100;
//       else             sbt = atom->bond_atom[idx][sbt];// if (sbt>=0)
//       
//       /*if (update->ntimestep == 13843)
//          {
//          printf ("    idx=%d  tgt=%d  sbt==%d   smod==%d\n", idx, targetTag, sbt, site_modified[idx][site]);   
//          }*/
//       
//       if (sbt == targetTag) return site;
//       }
//       
//   //printf ("AtomVecSRSim::findSiteByTargetTag: searching in %d for tag %d!\n",idx, targetTag);
//       
//   assert (false);  // when this function is called an appropriate site really should exist!
//   return -1;
//   }

#ifndef USE_TEMPL_AFFIL_MANAGER
bool LAMMPS_NS::AtomVecSRSim::fitsToTemplate( int idx, int needTemplate )
  {
  int numTempls = all_templs[idx].size();
  for (int i=0 ; i<numTempls ; i++)
      if (all_templs[idx][i] == needTemplate) return true;
      
  return false;
  }
#endif

void AtomVecSRSim::recVisit( int idx, SRSimCallback *cback, int depth, int maxDepth)
  {
  //printf ("AtomVecSRSim::recVisit of idx=%d in depth=%d\n", idx, depth);
  assert (visited[idx] == -1);  // should be unvisited.
  visited[idx] = depth; // ok, we won't check this one again.
  visitedStack.push( idx );
  
  // calculate something if we want to...
  if (cback != NULL) cback->run(idx);

  // maybe we have already reached the last last level of detail...
  if (maxDepth!=-1 && depth >= maxDepth) return;
    
  // check the molecules connected to this one:  
  int type     = atom->type[idx];
  int numSites = type2numSites[type];
  for (int i=0 ; i<numSites ; i++)
      {
      //printf ("  looking at site: %d    sbt: %d\n", i, avec->site_bound_to[startIdx][i]);
      if (site_bound_tag[idx][i] == -1) continue;
      int sbt = findOtherEnd(idx,i);
         
      //printf ("  has %d been visited: %d\n", sbt, visited[sbt]);
      if (visited[sbt] == -1) 
         {
         //printf ("  __going rec to %d here.\n",sbt);
         recVisit( sbt, cback, depth+1, maxDepth);
         }
      }
  
  }

void AtomVecSRSim::unVisitAll( )
  {
  while (! visitedStack.empty())
     {
     int i = visitedStack.top();
             visitedStack.pop();
     //printf ("delling visitation of %d.\n",i);
     visited[i] = -1;
     }
  }

  
  
/**
 *    Until v 1.01, activate_angles was run by this function as well - now it has to be run
 *      ,,manually'' after the bonds were creted. This is necessary because dihedrals participants 
 *      cannot be identified in the process of dihedral creation - so when there are some
 *      unfinished, dihedral containing molecules...
 *
 *      in short: angles will not be activated by this function.
 *
 *  //  the mol with smaller tag will be i, the one with the grear tag j.
 *  //   mol i will link to mol j.
 */
void AtomVecSRSim::addNewBond (int i, int j_tag, int i_site, int j_site)
  {
  int j        = atom->map(j_tag);
  int i_tag    = atom->tag[i];
  int i_type   = atom->type[i];
  int j_type   = atom->type[j];
    
  int bondType = srmodel->geo->getBondId( i_type, j_type, i_site, j_site ) + 1;    // + 1 as lammps types begin with 1
  
  //printf ("adding new bond... of Type %d, length is %f, t%d t%d   between %d and %d.\n", bondType, srmodel->geo->getBondDistance(bondType-1), i_type,j_type, i,j);
  
  // Lammps-Bond:
  int oldMeng = atom->num_bond[i];
  assert (oldMeng+1 <= atom->bond_per_atom);
  atom->bond_atom[i][oldMeng] = j_tag;
  atom->bond_type[i][oldMeng] = bondType;           //printf ("  Old Meng = %d \n", oldMeng);
  atom->num_bond [i]++;
  atom->nbonds      ++;
  
  // Site Bond:
  //site_bound_to[i][i_site] = oldMeng;
  //site_bound_to[j][j_site] = -(100+i_tag);
  site_bound_tag [i][i_site] = j_tag;
  site_other_site[i][i_site] = j_site;
  site_bond_id   [i][i_site] = oldMeng;
  
  site_bound_tag [j][j_site] = i_tag;
  site_other_site[j][j_site] = i_site;
  site_bond_id   [j][j_site] = -1;

  // reset the angles:
  //activateAngles( i );
  //activateAngles( j );
    
  
//   if (true)
//      {
//      printf ("###############\n");
//      printf ("AtomVecSRSim::addNewBond   i=%d  j=%d \n",i,j);
//      printf ("AtomVecSRSim::addNewBond   iTag=%d  jTag=%d \n", i_tag,j_tag);
//      printf ("AtomVecSRSim::addNewBond   iSite=%d  jSite=%d \n", i_site,j_site);
//      printf ("AtomVecSRSim::addNewBond   iSBT=%d  jSBT=%d \n", site_bound_tag[i][i_site],site_bound_tag[j][j_site] );
//      printf ("###############\n");
//      //assert (false);
//      }
  
  /*if (update->ntimestep == 13592 || i==1754)
     {
     printf ("###############\n#################\n#############\n###############\n");
     printf ("AtomVecSRSim::addNewBond   i %d, j %d \n",i,j);
     printf ("AtomVecSRSim::addNewBond   iSite=%d  jSite=%d \n", i_site,j_site);
     printf ("AtomVecSRSim::addNewBond   iSBT=%d  jSBT=%d \n", site_bond_id[i][i_site],site_bond_id[j][j_site] );
     printf ("###############\n#################\n#############\n###############\n");
     //assert (false);
     }*/
  
  //printf ("Done.\n");
  //printf ("adding new bond... (%d-%d)  bType=%d  oldM=%d nb=%d  sbt_i=%d sbt_j=%d  nBonds=%d\n",i,j, bondType, oldMeng, atom->num_bond[i],site_bound_to[i][i_site], site_bound_to[j][j_site], atom->nbonds);
  
/*  if (i==422 || j==422)
     {
     printf ("Aber Hallo! i=%d j=%d\n",i,j);
            LammpsMolecule l(422);
            l.writeToDotFile ("mol422.dot");
     assert (false);
     }*/
  }
  
/**
 *   1 x bond_atom shifting to the left
 *   2 x del site_bound_to
 *
 *   if (sbt <= -100)
 *
 *   idx : site which does the bind:
 *
 *   This function does not affect the bond angles!
 *   This function does not affect the template affiliation!
 */
void AtomVecSRSim::breakBond( int i, int site )
  {
  //int sbt = site_bound_tag[i][site];
  
  int masterIdx , slaveIdx;
  int masterSite, slaveSite;
  int masterBond;
  
  //printf ("Trying to break bond i=%d, s=%d,  \n",i,site);

  assert (site_bound_tag[i][site] != -1);   // then it would already be unbound!
  if (site_bond_id[i][site] != -1)
     {
     masterIdx = i; masterSite = site; masterBond = site_bond_id[i][site];
     slaveIdx = atom->map( site_bound_tag[masterIdx][masterSite] );
     
     // now missing: slaveSite
     slaveSite = site_other_site[masterIdx][masterSite];
     }
  else // i is slave, not master
     {
     slaveIdx = i; slaveSite = site;
     masterIdx = atom->map( site_bound_tag[slaveIdx][slaveSite] );  // now the id of the L.b.a.
     masterSite = site_other_site[slaveIdx][slaveSite];
     masterBond = site_bond_id[masterIdx][masterSite];
     }

  //printf ("  SIdx:%d SSt:%d   MIdx:%d MSt:%d\n ", slaveIdx, slaveSite, masterIdx, masterSite);
  //printf ("MasterBond = %d   ... trying to break %d\n ", masterBond, i);
  assert (masterBond >= 0);
  
  // delete master's bond_atom: shift the values from behind to the left
  //  ALSO: we'll have to adjust some site_bound_to values now!
  int oldMeng  = atom->num_bond [masterIdx];
  int mType    = atom->type[masterIdx];
  int numSites = type2numSites[mType];
  for (int ii=masterBond+1 ; ii<oldMeng ; ii++)
      atom->bond_atom[masterIdx][ii-1] = atom->bond_atom[masterIdx][ii];
  for (int ii=masterBond+1 ; ii<oldMeng ; ii++)
      atom->bond_type[masterIdx][ii-1] = atom->bond_type[masterIdx][ii];
  atom->num_bond [masterIdx]-- ;
  atom->nbonds              --;

  // now we'll probably have some left over site_bond_id's, which is bad.
  for (int ii=0 ; ii<numSites ; ii++)
      if (site_bond_id[masterIdx][ii] > masterBond)           // Otherwise our sites will point to wrong atoms!
         site_bond_id[masterIdx][ii]--;
         
  
  /*if (masterIdx==1754)
     {
     printf ("~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~\n");
     printf ("AtomVecSRSim::breakBond   i %d, j %d \n",masterIdx,slaveIdx);
     printf ("AtomVecSRSim::breakBond   iSite=%d  jSite=%d \n", masterSite,slaveSite);
     printf ("AtomVecSRSim::breakBond   iSBT=%d  jSBT=%d \n", site_bond_id[masterIdx][masterSite],site_bond_id[slaveIdx][slaveSite] );
     printf ("~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~\n");
     assert (false);
     }*/
  
  // free sites:
  site_bond_id   [masterIdx][masterSite] = -1;
  site_bound_tag [masterIdx][masterSite] = -1;
  site_other_site[masterIdx][masterSite] = -1;
  
  site_bond_id   [ slaveIdx][ slaveSite] = -1;
  site_bound_tag [ slaveIdx][ slaveSite] = -1;
  site_other_site[ slaveIdx][ slaveSite] = -1;
  
  // reset the angles:
  // not done here any more!
  //activateAngles( masterIdx );
  //activateAngles( slaveIdx );
    
  // that's it.
  //printf ("Deleted bond: (%d -- %d)\n", masterIdx, slaveIdx);
  }
  
  

/*void LAMMPS_NS::AtomVecSRSim::addTemplate2Sim( SRSim_ns::ReactantTemplate * rt, double x, double y, double z )
  {
  assert (false);
  
  // maybe we'll have to grow:
  //printf ("before nmax = %d  nlocal = %d\n", atom->nmax, atom->nlocal);
  // Nope - AtomVecAngle does the growing thingy...!
  
  // We should:
  //    determine if it's the right machine!!
  assert (comm->me == 0);
  
  // add:
  double dat[] = {x,y,z};
  create_atom(2, dat);
  
  //atom->tag[atom->nlocal-1] = 3;
  
  //printf ("after nmax = %d  nlocal = %d  \n", atom->nmax, atom->nlocal);
  
  // new total # of atoms
  //double nlocal = atom->nlocal;
  //MPI_Allreduce(&nlocal,&atom->natoms,1,MPI_DOUBLE,MPI_SUM,world);
  atom->natoms = atom->nlocal;
  
  // clean up: 
  // I don't know why to do it this way, but that's what create_atoms.cpp does!
  assert (atom->natoms <= 0x7FFFFFFF);   // MAXATOMS from create_atoms.cpp, line 28
  assert (atom->map_style == 1);         // means a map-array!
  atom->tag_extend();
  atom->map_init();
  atom->map_set();
  //assert (!atom->molecular);
  }*/

/**
 *   Adding angles, dihedrals and impropers.
 *     --> angles and impropers are relatively straight (there's a central atom)
 *   For dihedrals, we need four molecules in a row... so assume 'idx' is number two in the row.
 *
 *   Dihedrals: basically run over every site of mol i, except for that site going to site j:
 *      
 */  
void LAMMPS_NS::AtomVecSRSim::activateAngles( int idx )
  {
  SRSim_ns::GeometryDefinition *geo = srmodel->geo;
  
  int type          = atom->type[idx];
  int numSites      = type2numSites[type];
  int nAnglesBefore = atom->num_angle[idx];
  
  bool rigid_system = geo->getProperty(GPT_Option_Rigid);
  
  // which bonds do we have?
  vector<int> bonds;
  for (int i=0 ; i<numSites ; i++) if (site_bound_tag[idx][i] != -1) bonds.push_back( i );
  
  // now add all possible bonds
  int angle_cnt=0;
  for (uint k=0 ; k < bonds.size() ; k++)
      for (uint kk=k+1 ; kk < bonds.size() ; kk++)
          {  
          // sind wir ein rigid-body system?
          int p1   = findOtherEnd( idx, bonds[k]  );
          int mol1 = atom->molecule[idx];
          int p3   = findOtherEnd( idx, bonds[kk] );
          
          if (rigid_system)
             {
             //printf ("Mol = %d,%d,%d \n", mol1, atom->molecule[findOtherEnd( idx, bonds[k] )], atom->molecule[findOtherEnd( idx, bonds[kk] )]);
             if (mol1 == atom->molecule[p1] && mol1 == atom->molecule[p3])
                continue;
             }
          
          
          atom->angle_type [idx][angle_cnt] = 1 + geo->getAngleId( type, bonds[k], bonds[kk] );
          atom->angle_atom1[idx][angle_cnt] = atom->tag[ p1  ];
          atom->angle_atom2[idx][angle_cnt] = atom->tag[ idx ];
          atom->angle_atom3[idx][angle_cnt] = atom->tag[ p3 ];
          
          //printf ("   ... AnglyType = %d!  for atom %d!\n", atom->angle_type[idx][angle_cnt], idx);
          angle_cnt++;
          }
  atom->nangles += angle_cnt - nAnglesBefore ;
  atom->num_angle[idx] = angle_cnt;
  
  
  // ########################  Dihedrals:  ##########################################################
  
  if (geo->getProperty(GPT_Option_Dihedrals))
     {
     int idx2 = idx;          // the central 'idx' we're processing is idx2 in the four-atom-chain
     int dihedral_cnt = 0;
     int nDihedralsBefore = atom->num_dihedral[idx2];
     vector<SRSim_ns::DihedralGeo> &dgeo = geo->getDihedralSet( type );
     
     // now see if all sites are defined, so that we can activate them...
     for (unsigned i=0 ; i<dgeo.size() ; i++)
         {
         // find idx1 and idx3:
         if (site_bound_tag[idx2][ dgeo[i].site1Id ] == -1) continue;
         if (site_bound_tag[idx2][ dgeo[i].site3Id ] == -1) continue;
         int idx1 = findOtherEnd( idx2, dgeo[i].site1Id );
         int idx3 = findOtherEnd( idx2, dgeo[i].site3Id );
         
         // all dihedrals are defined in both directions - select one of them:
         //   imagine the dihedral: 125 - 075 - 655 - 124!   
         //   It can be seen from the other end as well: 124 - 655 - 075 - 125!
         //   so we're only activating the ones where idx2<idx3!
         //if (idx2>idx3) continue;
         
         // is idx3 of the right molecule type?
         if (atom->type[idx3] != dgeo[i].idx3Moltype) continue;
            //assert( false ); // printf ("  --> found the wrong atom-type here!\n");
          
         // assert that for idx3, the right site goes to us (=idx2):
         if (site_bound_tag[idx3][ dgeo[i].site3InversId ] == -1) continue;
         if (findOtherEnd( idx3, dgeo[i].site3InversId ) != idx2) continue;
         
         // ok, now hope that idx4 is bound as well:
         if (site_bound_tag[idx3][ dgeo[i].site4Id ] == -1) continue;
         int idx4 = findOtherEnd( idx3, dgeo[i].site4Id );
         
         printf (" new dihedral: idx2 = %d, idx3 = %d ; sbt[idx3][s3i] = %d, s3i = %d\n", idx2,idx3, site_bound_tag[idx3][ dgeo[i].site3InversId ], dgeo[i].site3InversId);
         // Yippeyayeah! Let's realize this dihedral:
         atom->dihedral_type [idx2][dihedral_cnt] = 1 + dgeo[i].dihedralId;
         atom->dihedral_atom1[idx2][dihedral_cnt] = atom->tag[ idx1 ];
         atom->dihedral_atom2[idx2][dihedral_cnt] = atom->tag[ idx2 ];
         atom->dihedral_atom3[idx2][dihedral_cnt] = atom->tag[ idx3 ];
         atom->dihedral_atom4[idx2][dihedral_cnt] = atom->tag[ idx4 ];
         dihedral_cnt++;
         }
         
     // update the general Lammps-atom values:
     atom->ndihedrals += dihedral_cnt - nDihedralsBefore ;
     atom->num_dihedral[idx2] = dihedral_cnt;
     }
  
     
  if (geo->getProperty(GPT_Option_Impropers))
     {
     assert( false );
     }
  
  //printf ("\n\n\n\n\n  atom->nangles = %d  (having added %d angles!) \n\n\n\n\n\n", atom->nangles, cnt - nAnglesBefore);
  }

  
  
void LAMMPS_NS::AtomVecSRSim::refractMol( int i, int until_ts )
  {
  //printf ("  Marking %d as refractory until %d \n",i,until_ts);
  if (refractory[i] != -1) 
     {
     // we'll first have to delete i from the list, then push it onto the end again!#
     refractoryList.remove( i );
     }
  //assert( refractory[i] == -1 );
  
  refractory[i] = until_ts;
  refractoryList.push_back( i );
  }
  
void LAMMPS_NS::AtomVecSRSim::unRefract( )
  {
  //printf ("DeRefracting in ts %d.    (%d refractory molecules)\n", update->ntimestep, refractoryList.size() );
  while (! refractoryList.empty() )
     {
     int ts = refractory[ refractoryList.front() ];
     if (ts > update->ntimestep) return;
     
     // we have to delete this one:
     refractory[ refractoryList.front() ] = -1;
     refractoryList.pop_front();
     }
  }

/**
  The reactant Template *rt is created in the reactor at position (x,y,z)
  If no tgeo for this template is known, molecules are positioned randomly.
  
  Angles and dihedrals will be set up for these new molecule graphs.
  Template information will also be generated.
*/
void LAMMPS_NS::AtomVecSRSim::addTemplate2Sim( SRSim_ns::ReactantTemplate * rt, double x, double y, double z )
  {
  //double epsilon = 0.01;   // just a small buffer distance away from the reactor borders
  
  if (srmodel == NULL)
     error->all("The rule system is not initialized yet: use runmodif_srsim ruleSys first! (e.g. runmodif_srsim ruleSys spass.bngl spass.geo spass.tgeo 12345)");
  SRSim_ns::MoleculeTypeManager *mtm = srmodel->mtm;
  
  int myMolID = mol_Id_Cnt++;

  // what are we going to do in muliproc-usage?
  assert( comm->me == 0 );
      
  //assert( rt->getGeo() != NULL );
  
  assert( rt->isUsableAs(SRSim_ns::ReactantTemplate::creatableRT) );
  //assert( false );

  double x0 = domain->sublo[0], xm = domain->subhi[0];
  double y0 = domain->sublo[1], ym = domain->subhi[1];
  double z0 = domain->sublo[2], zm = domain->subhi[2];
    
  // locally store a reactant template geometry, if it is known - otherwise randomize:
  vector<SRSim_ns::Coords> positions;
  if (rt->getGeo() != NULL)
     {
     for (int m=0 ; m<rt->numMolecules() ; m++)
         positions.push_back( rt->getGeo()->getCoords(m) );
     }
  else // we have to randomize a set of coords:
     { 
     SRSim_ns::RandomGenerator *random = srmodel->random;
     for (int m=0 ; m<rt->numMolecules() ; m++)
         {
         SRSim_ns::Coords c;
                          c.x[0] = ((xm-x0)/1000.0) * (random->uniform()-0.5);
                          c.x[1] = ((ym-y0)/1000.0) * (random->uniform()-0.5);
                          c.x[2] = ((zm-z0)/1000.0) * (random->uniform()-0.5);
         positions.push_back( c );
         }
     }
  
    
  // first, adjust xyz to make the whole molecule graph fit into the reactor:
  for (uint m=0 ; m<positions.size() ; m++)
      {
      //printf("   x1 %f\n", x);
      SRSim_ns::Coords co = positions[m];
      
      // Gerdl: what was this one for?
      if (x+co.x[0] < x0) x -= x+co.x[0] - x0 - 0.01;
      if (y+co.x[1] < y0) y -= y+co.x[1] - y0 - 0.01;
      if (z+co.x[2] < z0) z -= z+co.x[2] - z0 - 0.01;
      if (x+co.x[0] > xm) x -= x+co.x[0] - xm + 0.01;
      if (y+co.x[1] > ym) y -= y+co.x[1] - ym + 0.01;
      if (z+co.x[2] > zm) z -= z+co.x[2] - zm + 0.01;
      
      //printf("   x1 %f\n", x);
      }
  
    
  // add the molecules:
  int nOld = atom->nlocal;                         // the old local atom count...
  for (int m=0 ; m<rt->numMolecules() ; m++)
      {
      SRSim_ns::TemplMolecule   *tm    = rt->getMolecule(m);
      int                        mType = tm->getType();
      SRSim_ns::Coords           co = positions[m];
          
      // are all sites of this molecule defined?
      //if (tm->numSites() != mtm->numSites(tm->getType()))
      //   printf("############\n  Warning: in Template %s not all sites are defined as they should be!\n##########\n", rt->getName().c_str() );
      
      tm->setRealization (m);
      
      //printf("   x %f  ---  co.x %f  --  xm %f \n", x, co.x[0], xm);
      
      assert(x+co.x[0]>=x0);
      assert(y+co.x[1]>=y0);
      assert(z+co.x[2]>=z0);
      assert(x+co.x[0]<=xm);
      assert(y+co.x[1]<=ym); //printf ("z = %f   co = %f\n",z,co.x[2]);
      assert(z+co.x[2]<=zm);
      
      double dVect[] = {x+co.x[0], y+co.x[1], z+co.x[2]};
      create_atom(mType, dVect);
      
      atom->molecule[atom->nlocal-1] = myMolID;  // so rigid body dynamics can be used.
      }
  
  
  //printf ("after nmax = %d  nlocal = %d  \n", atom->nmax, atom->nlocal);
  
  // new total # of atoms
  //double nlocal = atom->nlocal;
  //MPI_Allreduce(&nlocal,&atom->natoms,1,MPI_DOUBLE,MPI_SUM,world);
  assert (comm->me == 0);
  atom->natoms = atom->nlocal;  // TODO: That one looks evil if we go to multiprocessor!
  
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
      SRSim_ns::TemplMolecule *tm = rt->getMolecule(m);
      tm->setRealization( -1 );
      for (int s=0 ; s<tm->numSites() ; s++)
          {
          // site modification?
          bool modified = false;
          int rmSiteCount = mtm->numSites(tm->getType());
          for (int rms=0 ; rms < rmSiteCount ; rms++)            // searching for the right site to modify:
              {
              //printf ("Searching for Site %d and this is %d [M%d S%d RMS%d]\n",tm->getSiteType(s), mtm->getSiteType(tm->getType(),rms), m,s,rms );
              
              if (site_modified[nOld+m][rms]          != -666)               continue;
              if (mtm->getSiteType(tm->getType(),rms) != tm->getSiteType(s)) continue;
              site_modified[nOld+m][rms] = tm->getModificationAtSite(s);
              modified = true;
              //printf ("    mod[%d][%d] = %d\n", nOld+m, rms, avec->site_modified[nOld+m][rms] );
              break;
              }
          assert( modified );
          
          // bonding:          
          SRSim_ns::TemplMolecule *tm2 = dynamic_cast<SRSim_ns::TemplMolecule*>( tm->getMoleculeAtSite(s) );
          if (tm2 == NULL)                 continue;
          if (tm2->getRealization() == -1) continue;
          
          int sType2 = tm->getSite(s)->getOther()->getType();
          
          int rea1  = nOld + m;
          int rea2  = nOld + tm2->getRealization();

          int site1 = findAllFittingSites (rea1, tm ->getSiteType(s), -1/*needModif*/, true/*needFree*/)[0];
          int site2 = findAllFittingSites (rea2, sType2             , -1/*needModif*/, true/*needFree*/)[0];
          
          addNewBond(rea1, atom->tag[rea2], site1, site2);
          }
          
      // Do all mol-sites have their proper modifications? None is -666?
      for (int rms=0 ; rms<mtm->numSites(tm->getType()) ; rms++)
          if (site_modified[nOld+m][rms] == -666) site_modified[nOld+m][rms] = -1;
      }
      
  // Now that all the bonds are formed, we can activate the angles for all newly create molecules:
  for (int m=0 ; m<rt->numMolecules() ; m++)
      {
      int newMol  = nOld + m;
      activateAngles( newMol );
      }
  
  // finally add Template information for the generated molecules:
  for (int m=0 ; m<rt->numMolecules() ; m++)
      {
      int newMol  = nOld + m;
      
      LammpsMolecule lm(newMol);
      #ifndef USE_TEMPL_AFFIL_MANAGER
      rset->fillFittingTemplates(&lm, all_templs[newMol], site_templs[newMol], amount_templs);
      #else
      rset->fillFittingTemplates(&lm, newMol, affiliations);
      #endif
      }  
  }




void LAMMPS_NS::AtomVecSRSim::removeAtomsByTag( set<int> atomTagsToRemove )
  {
  vector<int> ids;
  
  set<int>::iterator it;
  for ( it=atomTagsToRemove.begin() ; it != atomTagsToRemove.end(); it++ )
      {
      int tagToDel = *it;
      int  idToDel = atom->map( tagToDel );
      
      ids.push_back( idToDel );
      //printf ("    adding %d to the dellist!\n", idToDel );
      }
      
  removeAtomsById( ids );
  }



/**
 * Let's try to remove some atoms...
 *
 *  but take CARE: Deleting Atoms will change the indices of all the atoms in the simulator!
 */
void LAMMPS_NS::AtomVecSRSim::removeAtomsById( vector<int> atomIdsToRemove )
  {
  // Thanks to Lammps' delete_atoms command here:
  int nlocal = atom->nlocal;
  //int i      = 0;
  
  // we have to delete the largest index first, so let's sort:
  sort (atomIdsToRemove.begin(), atomIdsToRemove.end());
  
  // overwrite these atoms with the ones from the end of the nlocal list!
  for (int i=atomIdsToRemove.size()-1 ; i>=0 ; i--)
      {
      //printf ("        approaching %d with a knife from behind!\n", atomIdsToRemove[i]);
      
      int  idToDel = atomIdsToRemove[i];
      
      // release all the bonds that are going out from these atoms:
      int type   = atom->type[idToDel];
      int nsites = type2numSites[type];
      for (int site=0 ; site<nsites ; site++)
          if (site_bound_tag[idToDel][site] != -1)
             breakBond(idToDel, site);
             
             
      // release all angles going out from this atom... 
      //   nope - we don't need to do this; if this atom is beyong nlocal,
      //   these angles will not be considered!
      //  But we still need the number of angles, dihedrals etc. that were emerging from this atom!
      atom->nangles    -= atom->num_angle   [idToDel];
      atom->ndihedrals -= atom->num_dihedral[idToDel];
      atom->nimpropers -= atom->num_improper[idToDel];
      
      int deleted;  // "deleted" is the index of which we will erase the template information!
      if (idToDel < nlocal-1)
         {
         copy(nlocal-1,idToDel);
         deleted = nlocal-1;
#ifdef USE_TEMPL_AFFIL_MANAGER
         affiliations.clearMolAffil( deleted );
#endif
         }
      else 
         {
         deleted = idToDel;
#ifdef USE_TEMPL_AFFIL_MANAGER
         affiliations.clearMolAffil( deleted );
#endif
         }
      
      //printf ("deleted id %d through %d\n", idToDel, deleted);
      nlocal--;
      
#ifndef USE_TEMPL_AFFIL_MANAGER
      // now release the templates, that this atom belonged to!
      // this->copy() will take care of site_templs, all_templs and amount_templs
      // that were overwritten in idToDel.
      // But we still need to decrease the amount_templs by the nlocal-1 
      for (uint ii=0 ; ii<all_templs[deleted].size() ; ii++) 
          {
          //printf ("Releasing template %d \n", all_templs[deleted][ii]);
          amount_templs[ all_templs[deleted][ii] ]--;
          }
      all_templs [deleted].clear( );
      site_templs[deleted].clear( );
#endif
      }
  atom->nlocal = nlocal;
  assert (comm->me == 0);
  atom->natoms = atom->nlocal;  // TODO: That one looks evil if we go to multiprocessor!
   

  // reset atom->map if it exists
  if (atom->map_style) 
     {
     atom->nghost = 0;
     atom->map_init();
     atom->map_set();
     }  
  }













  
void LAMMPS_NS::AtomVecSRSim::updateSingleTemplateData (int idx)
  {
  LammpsMolecule lm(idx);
#ifndef USE_TEMPL_AFFIL_MANAGER
  rset->fillFittingTemplates(&lm, all_templs[idx], site_templs[idx], amount_templs);
#else
  rset->fillFittingTemplates(&lm, idx, affiliations);
#endif
  }
  
  
/**   Scanning of Template Data changes:
 *  After bond breaking or tying, new fitting SiteTemplates can be 
 *  added / lost from the molecules. Also the whole graph can add / lose 
 *  WholeMoleculeReactantTemplates.
 *
 *  1) go through every molecule of the graph recursively
 *  2) calculate template data.
 */
void LAMMPS_NS::AtomVecSRSim::updateSubgraphTemplateData( int startIdx)
  {
  //printf (" updating subgraph, beginning at %d\n", startIdx);
  assert (visited[startIdx] == -1);  // should be unvisited.

  UpdateCallback uc (this);
  recVisit(startIdx, &uc, 1, 1+rset->maxReactiveTemplateSize() );
  // we added one to the maxReactiveTemplateSize, because also the molecule sites of the 
  // next molecules may be affected!
  
  
  //printf (" mRTS = %d\n", rset->maxReactiveTemplateSize() );

  // clear the 'visited'  tag from all the nodes we visited.
  unVisitAll();
  }
 
void LAMMPS_NS::UpdateCallback::run( int localIdx )
  {
  myAvec->updateSingleTemplateData (localIdx);
  }
 


  
// vector<double> LAMMPS_NS::FixSRSim::recDelLocalizeGraph( int idx, bool del, int distance )
//   {
//   assert (false);
//   }


/** To get the subgraph (usually up to a distance of 3 molecules) to reconsider 
 *    the use of angle, dihedral and impropers.
 */
void LAMMPS_NS::AtomVecSRSim::reactivateSubgraphAngles(int startIdx, int depth)
  {
  //printf (" updating subgraph, beginning at %d\n", startIdx);
  assert (visited[startIdx] == -1);  // should be unvisited.

  ReactivateAnglesCallback rc (this);
  recVisit(startIdx, &rc, 1, depth );

  // clear the 'visited'  tag from all the nodes we visited.
  unVisitAll();
  
  }
void LAMMPS_NS::ReactivateAnglesCallback::run( int localIdx )
  {
  myAvec->activateAngles(localIdx);
  }



/** basically does the same as updateSubgraphTemplateData, only for the whole reactor, if necessary */
void LAMMPS_NS::AtomVecSRSim::checkIfTemplateRecalculationNecessary() 
  {
  if ( ! templatesNeedRecalculation ) return;
  
  printf ("AtomVecSRSim - we have to recalculate the template patterns and the angles for all molecules...  ");
  
  int nlocal = atom->nlocal;
  for (int i=0 ; i<nlocal ; i++) 
      {
      updateSubgraphTemplateData(i);
      activateAngles(i);
      }
  
  printf ("done.\n");
  templatesNeedRecalculation = false;
  }

