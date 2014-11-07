//
// C++ Implementation: createGeo
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include "defs.h"
#include <assert.h>
#include <string.h>

#include "start_state_definition.h"
#include "templ_site.h"
#include "templ_molecule.h"
#include "names_manager.h"
#include "bng_rule_builder.h"
#include "reactant_template.h"
//#include "multi_mol_reactant_template.h" 
#include "bound_reactant_template.h"
#include "sr_error.h"
#include "sr_model.h"

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <sstream>
#include <fstream>

using namespace std;
using namespace SRSim_ns;


void have_fun                (int argc, char **argv);
void write_lammps_input_file (char *fname, SRModel *model);
void write_lammps_data_file  (char *fname, SRModel *model, int t_id);
void retrieve_sim_data       (SRModel *model, int t_id);
void write_geo_data          (char *fname, SRModel *model);
void write_mgeo_data         (char *fname, SRModel *model);

int  countAngles             (ReactantTemplate *rt);
int  countImpropers          (ReactantTemplate *rt);




int main (int argc, char **argv)
  {
  if (argc != 4)  SRError::critical("wrong parameter count! \n param1: fun.bngl   param2: fun.geo   param3: fun.tgeo");
  
  try { have_fun (argc, argv); }
  catch (SRException sre) {sre.report("sorry - no further info available...");}
  
  printf ("All done!\n");
  return 0;
  }


bool fileExists (char* name)
  {
  FILE *f = fopen(name, "r");
  if (f == NULL) return false;
  fclose(f);
  return true;
  }


void have_fun (int argc, char **argv)
  {  
  SRModel model(12345);
  BNGRuleBuilder p(&model);
  p.readFile(argv[1]); 

  // write Molecule-Geo?
  if (!fileExists(argv[2]))
     {
     write_mgeo_data(argv[2], &model);
     }
  else 
     {
     printf (" #### Hey, Sorry, %s is already existent. Delete it first to recalculate!\n", argv[2]);
     }
  model.geo->readMGeoFile(argv[2]);
    
  // write Template-Geo?
  if (!fileExists(argv[3]))
     {
     write_lammps_input_file ("in.createGeo" , &model);
  
     for (int i=0 ; i<model.ruleset->numTemplates() ; i++)
         {
         if ( ! model.ruleset->getRT(i)->isUsableAs( ReactantTemplate::creatableRT )) continue;
         if ( model.ruleset->getRT(i)->getRTType() != ReactantTemplate::MultiMolRT  ) continue;
         if ( model.ruleset->getRT(i)->numMolecules() < 2                           ) continue;
      
         write_lammps_data_file ("data.createGeo", &model, i/*templ_id*/);
         system("lammps < in.createGeo");
         retrieve_sim_data (&model, i/*templ_id*/);  
         }
         
     write_geo_data    (argv[3], &model);
     }
  else printf (" #### Hey, Sorry, %s is already existent. Delete it first to recalculate!\n", argv[3]);
  
  
  
  return;
  }

void write_lammps_data_file (char *fname, SRModel *model, int t_id)
  {
  MoleculeTypeManager *mtm = model->mtm;
  GeometryDefinition  *geo = model->geo;
  
  FILE *f = fopen(fname, "w");
  
  ReactantTemplate *rt = model->ruleset->getRT(t_id);
  
  fprintf (f,"LAMMPS Data File for createGeo\n");
  fprintf (f,"\n");
  fprintf (f,"%d atoms\n", rt->numMolecules());
  fprintf (f,"%d bonds\n", rt->countBonds());
  fprintf (f,"%d angles\n", countAngles(rt) );
  fprintf (f,"0  dihedrals\n");
  fprintf (f,"0  impropers\n"/*, countImpropers(rt)*/ );
  fprintf (f,"\n");
  fprintf (f,"%d bond types\n", rt->countBonds());
  fprintf (f,"%d angle types\n", geo->numAngleTypes());
  fprintf (f,"%d atom types\n", mtm->num__Mols());
  fprintf (f,"0  dihedral types\n");
  fprintf (f,"%d  improper types\n", geo->numAngleTypes());
  fprintf (f,"\n");
  fprintf (f,"-30 30 xlo xhi\n");
  fprintf (f,"-30 30 ylo yhi\n");
  fprintf (f,"-30 30 zlo zhi\n");
  fprintf (f,"\n");
  fprintf (f,"\n");
  fprintf (f,"Masses\n");
  fprintf (f,"\n");
  
  for (int i=0 ; i<mtm->numMolIDs() ; i++)
      {
      if (! mtm->isMol(i)) continue;
      fprintf (f,"%d 1.0\n", i+1);
      }
  
  printf  ("Atoms!\n");
  fprintf (f,"\n");
  fprintf (f,"Atoms\n");
  fprintf (f,"\n");
  
  // now add some Lammps::atoms = SRSim::molecules
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      RandomGenerator *rg = model->random;
      double x[3] = {rg->uniform()*50.0-25, rg->uniform()*50.0-25, rg->uniform()*50.0-25};
      
      fprintf (f,"%d %d %d %f %f %f \n",i+1, i+1, 1+rt->getMolecule(i)->getType(), x[0],x[1],x[2] );
      rt->getMolecule(i)->setRealization( i );
      }
  
  printf  ("Bonds!\n");
  fprintf (f,"\n");
  fprintf (f,"Bonds\n");
  fprintf (f,"\n");
  
  // now add some bonds:
  int cnt = 1;
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      TemplMolecule *tm = rt->getMolecule(i);
      int molId1 = tm->getRealization();
      //tm->setRealization( -1 );
      
      for (int s=0 ; s<tm->numSites() ; s++)
          {
          TemplMolecule *tm2 = dynamic_cast<TemplMolecule*>( tm->getMoleculeAtSite(s) );
          if (tm2 == NULL)                 continue;
          if (tm2->getRealization() < i)   continue;
          
          int molId2 = tm2->getRealization();
          fprintf (f, "%d %d %d %d\n",cnt, cnt, molId1+1, molId2+1);
          cnt++;
          }
      }
  
  
  printf  ("Angles!\n");
  fprintf (f,"\n");
  fprintf (f,"Angles\n");
  fprintf (f,"\n");
  
  // let's write some angles:
  cnt = 0;
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      TemplMolecule *tm = rt->getMolecule(i);
      int myMolId = tm->getRealization();
      
      printf  ("tm = %s mId=%d\n", rt->getName().c_str(), tm->getType());
      
      // what bonds do we have?
      vector<int> bondSites;
      vector<int> mtmSites;
      for (int j=0 ; j<tm->numSites() ; j++)
          if (tm->getMoleculeAtSite(j) != NULL)
             {
             bondSites.push_back( j );                                      // Template - Format
              mtmSites.push_back( mtm->getSidFromTempl(tm->getSite(j)) );   // MTM - Format
             }

      if (bondSites.size() < 2) continue;
      
      // now which sites are a good Angle - base?
      /*vector<int> base = geo->getGoodAngleBase(tm->getType(), mtmSites);
      
      int id2 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[0]] )))->getRealization();
      int id0 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[1]] )))->getRealization();
      int id1 = myMolId;
      int sid1 = mtmSites[base[0]];
      int sid2 = mtmSites[base[1]];
      int aId  = geo->getAngleId( tm->getType(), sid1, sid2);
      fprintf (f, "%d %d   %d %d %d\n",cnt+1, aId+1, id0+1, id1+1, id2+1);
      cnt++;*/
      
      for (int j=0 ; j<bondSites.size() ; j++)            // the highest amount of angles possible! Uugh!
          for (int k=j+1 ; k<bondSites.size() ; k++)
              {
              int id0 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[j] )))->getRealization();
              int id1 = myMolId;
              int id2 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[k] )))->getRealization();
              int sid1 = mtmSites[j];
              int sid2 = mtmSites[k];
              int aId  = geo->getAngleId( tm->getType(), sid1, sid2);
              fprintf (f, "%d %d   %d %d %d\n",cnt+1, aId+1, id0+1, id1+1, id2+1);
              cnt++;
              }
      
      
      /*printf  (   "AnglId: %d zu tp:%d sid:%d sid:%d   --- bs0:%d bs1:%d\n", aId, tm->getType(), sid1, sid2, bondSites[0], bondSites[1]);
      printf  (   "%d %d   %d %d %d\n",cnt+1, aId+1, id0+1, id1+1, id2+1);
      assert  (t_id != 7);*/

      // wich angle is going to be the base? taking [0] and [1] may be a problem, if 
      // the angles are close to 0° or 180° !!
      
      }
  
      
      
/*  printf  ("Impropers!\n");
  fprintf (f,"\n");
  fprintf (f,"Impropers\n");
  fprintf (f,"\n");
      
      
  // let's write the rest of the angles as impropers:
  cnt = 0;
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      TemplMolecule *tm = rt->getMolecule(i);
      int myMolId = tm->getRealization();
      
      printf  ("tm = %s mId=%d\n", rt->getName().c_str(), tm->getType());
      
      // what bonds do we have?
      vector<int> bondSites;
      vector<int> mtmSites;
      for (int j=0 ; j<tm->numSites() ; j++)
          if (tm->getMoleculeAtSite(j) != NULL)
             {
             bondSites.push_back( j );                                      // Template - Format
              mtmSites.push_back( mtm->getSidFromTempl(tm->getSite(j)) );   // MTM - Format
             }

      // problem: finding the right site from the mtm now...  :(  :(
      if (bondSites.size() < 3) continue;
      
      // now which sites are a good Angle - base?
      vector<int> base = geo->getGoodAngleBase(tm->getType(), mtmSites);
      
      // und dann hier ab drei Seiten:
      for (int k=0 ; k<bondSites.size() ; k++)
          {
          if (k == base[0] || k == base[1]) continue;
          
          int sid1 = mtmSites[base[0]];
          int sid2 = mtmSites[base[1]];
          int sid3 = mtm->getSidFromTempl( tm->getSite(bondSites[k]) );
          
          int id0 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[0]] )))->getRealization();
          int id1 = myMolId;
          int id2 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[     k ] )))->getRealization();
          int id3 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[1]] )))->getRealization();    // this is the missing base-vector!
          int aId  = geo->getAngleId( tm->getType(), sid1, sid3);
          fprintf (f, "%d %d   %d %d %d %d\n",cnt+1, aId+1, id0+1, id1+1, id2+1, id3+1);
          cnt++;
          
          id0 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[1]] )))->getRealization();
          id1 = myMolId;
          id2 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[k      ] )))->getRealization();
          id3 = ((TemplMolecule*)(tm->getMoleculeAtSite( bondSites[base[0]] )))->getRealization();
          aId  = geo->getAngleId( tm->getType(), sid2, sid3);
          fprintf (f, "%d %d   %d %d %d %d\n",cnt+1, aId+1, id0+1, id1+1, id2+1, id3+1);
          cnt++;
          }
      }*/
      
      
      
      
      
      
  printf  ("Bond Coeffs!\n");
  fprintf (f,"\n");
  fprintf (f,"Bond Coeffs\n");
  fprintf (f,"\n");
  
  // BondLengths:
  cnt = 1;
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      TemplMolecule *tm = rt->getMolecule(i);
      //tm->setRealization( -1 );
      
      for (int s=0 ; s<tm->numSites() ; s++)
          {
          TemplMolecule *tm2 = dynamic_cast<TemplMolecule*>( tm->getMoleculeAtSite(s) );
          if (tm2 == NULL)                 continue;
          if (tm2->getRealization() < i)   continue;
          
          TemplSite *ts2 = tm->getSite(s);
          TemplSite *ts1 = ts2->getOther();
          
          int mid1 = tm->getType();
          int mid2 = tm2->getType();
          int sid1 = mtm->getSidFromTempl( ts1 );
          int sid2 = mtm->getSidFromTempl( ts1 );
          
          int    bondId = geo->getBondId( mid1,mid2, sid1,sid2 );
          double bDist  = geo->getBondDistance( bondId );
          
          fprintf (f, "%d 5.0 %f\n",cnt, bDist);
          cnt++;
          }
      }
  
  printf  ("Angle Coeffs!\n");
  fprintf (f,"\n");
  fprintf (f,"Angle Coeffs\n");
  fprintf (f,"\n");
  
  cnt=1;
  for (int i=0 ; i<geo->numAngleTypes() ; i++)
      {
      double ang = geo->getAngle(i);
      fprintf (f, "%d 5.0 %f\n",cnt, ang);
      cnt++;
      }
  
  /*printf  ("Improper Coeffs - same as Angle Coeffs, but who cares ;) !\n");
  fprintf (f,"\n");
  fprintf (f,"Improper Coeffs\n");
  fprintf (f,"\n");
  
  cnt=1;
  for (int i=0 ; i<geo->numAngleTypes() ; i++)
      {
      double ang = geo->getAngle(i);
      fprintf (f, "%d 100.0 %f\n",cnt, ang);
      cnt++;
      }*/
  
  fprintf (f,"\n");
  fprintf (f,"\n");
  
  fclose (f);
  printf  ("Done writing Lammps Data File!\n");
  }

  
void write_lammps_input_file (char *fname, SRModel *model)
  {
  MoleculeTypeManager *mtm = model->mtm;
  GeometryDefinition  *geo = model->geo;
  
  FILE *f = fopen(fname, "w");
  
  fprintf (f,"dimension       3\n");
  fprintf (f,"boundary        s s s\n");
  fprintf (f,"units           real\n");
  fprintf (f,"newton          off\n");
  fprintf (f,"\n");
  fprintf (f,"atom_style      molecular\n");
  fprintf (f,"bond_style      harmonic\n");
  fprintf (f,"angle_style     harmonic\n");
  //fprintf (f,"improper_style  negang_srsim\n");
  fprintf (f,"read_data       data.createGeo\n");
  fprintf (f,"neighbor        10.0 nsq\n");
  fprintf (f,"\n");
  fprintf (f,"pair_style      soft 2.5\n");
  fprintf (f,"\n");
  

  // add the molecule-radii:
  for (int i=0 ; i<mtm->numMolIDs() ; i++)
      {
      if (! mtm->isMol(i)) continue;
      for (int j=i ; j<mtm->numMolIDs() ; j++)
          {
          if (! mtm->isMol(j)) continue;
          
          //double dist = geo->getRadius(i) + geo->getRadius(j);
          double dist = geo->getProperty(GPT_Mol_Rad,i) + geo->getProperty(GPT_Mol_Rad,j);
          fprintf (f,"pair_coeff      %d %d 60 60 %f\n",i+1,j+1, dist);
          }
      }
    
  fprintf (f,"\n");
  fprintf (f,"fix             1 all nve\n");
  fprintf (f,"fix             2 all langevin 5 5 185.0 23456\n");
  fprintf (f,"\n");
  fprintf (f,"timestep        1.0\n");
  fprintf (f,"thermo          200\n");
  fprintf (f,"dump            1 all atom 100 dump.createGeo.lammpstrj\n");
  fprintf (f,"dump_modify     1 scale no\n");
  fprintf (f,"\n");
  fprintf (f,"run             20000\n");
  
  fclose (f);
  }
  
  
  
int countAngles (ReactantTemplate *rt)
  {
  int aCnt = 0;
  
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      // count active sites:
      TemplMolecule *tm = rt->getMolecule(i);
      int sCnt = 0;
      for (int j=0 ; j<tm->numSites() ; j++)
          if (tm->getMoleculeAtSite(j) != NULL) sCnt++;
      
      // if (sCnt >= 2) aCnt ++;
      // aCnt += MolGeo::numAngles(sCnt);
      
      for (int j=0 ; j<sCnt ; j++)            // the worst (highest) amount of angles!
          for (int k=j+1 ; k<sCnt ; k++)
              aCnt ++;
      }
      
  return aCnt;
  }
  
  

int countImpropers (ReactantTemplate *rt)
  {
  assert (false);   // do we start to use impropers again now???
  
  int aCnt = 0;
  
  for (int i=0 ; i<rt->numMolecules() ; i++)
      {
      // count active sites:
      TemplMolecule *tm = rt->getMolecule(i);
      int sCnt = 0;
      for (int j=0 ; j<tm->numSites() ; j++)
          if (tm->getMoleculeAtSite(j) != NULL) sCnt++;
      
      //aCnt += MolGeo::numAngles(sCnt);
      //if (MolGeo::numAngles(sCnt) > 1)
      //   aCnt += MolGeo::numAngles(sCnt) - 1;
      aCnt += ((sCnt-1)*sCnt/2);
      }
      
  return aCnt;
  }  
  
  
void seekLastTimestep (ifstream &f)
  {
  long pos;
  int highestTS = 0;
  while (f.good())
    {
    char buffer[200];
    f >> buffer;
    
    if (strcmp(buffer, "TIMESTEP") == 0)
       {
       f >> highestTS;
       pos = f.tellg();
       }
    }
    
  f.clear ();     // clear error flag
  f.seekg (pos);
  cout << " Found last TS = " << highestTS << endl;
  }
  
  
void retrieve_sim_data( SRModel *model, int t_id)
  {
  char buffer[200];
  ifstream f;
  
  f.open ("dump.createGeo.lammpstrj", ifstream::in);
  f.width (199);

  seekLastTimestep(f);
    
  while (f.good()) {f >> buffer; if (strcmp(buffer, "ATOMS") == 0) break;}
  int nAtoms; f >> nAtoms;
  cout << "nAtoms: " << nAtoms << "   zu Tid="<< t_id<< endl;
  while (f.good()) {f >> buffer; if (strcmp(buffer, "ATOMS") == 0) break;}
  
  ReactantTemplate *rt = model->ruleset->getRT(t_id);
  assert (nAtoms == rt->numMolecules());
  
  TemplateGeo tg;
  for (int i=0 ; i<nAtoms ; i++)
      {
      f >> buffer;  // either the actual id - or a description of the following fields...
      if ( strcmp(buffer,"id") == 0 )
         {
         f >> buffer;  // "type"
         f >> buffer;  // "x"
         f >> buffer;  // "y"
         f >> buffer;  // "z"
         f >> buffer;  // the actual mol id
         f >> buffer;  // the actual mol type
         }
      else
         f >> buffer;  // the actual mol type
      
      double x,y,z;
      f >> x; f >> y; f >> z;
      tg.molPositions.push_back( Coords(x,y,z) );
      printf ("   ---  %f %f %f \n",x,y,z);
      }

  // center of mass thingy:
  Coords c = tg.centerOfMass(rt, model->geo);
  tg = tg.translate(-c.x[0],-c.x[1],-c.x[2]);

  // add the template:      
  model->geo->addTemplateGeo(rt, tg);  
  
  // done.  
  f.close ();
  
  cout << "retrieved template-simulation data!\n";
  }

  
void write_geo_data( char * fname, SRModel * model )
  {
  //cout << "Schlupp!\n";
  
  int tNum = model->ruleset->numTemplates();
  ofstream of (fname);
  
  of << "<?xml version=\"1.0\"?> \n";
  of << "   <template-geometry-definition> \n\n";
  
  for (int t=0 ; t<tNum ; t++)
      {
      ReactantTemplate *rt = model->ruleset->getRT(t);
      if ( ! rt->isUsableAs( ReactantTemplate::creatableRT )) continue;
      model->geo->writeTemplateGeo(of, rt);
      of << "\n";
      }
  
  of << "   </template-geometry-definition> \n";
  
  
  
  
  of.close();
  cout << "Written Template geometry file!\n";
  }




  
void createSites (vector<double> &phi, vector<double> &theta, int numSites)
  {
  phi.clear(); theta.clear();
  printf (" numSites = %d\n", numSites);
  
  phi.push_back( 0.0 ); theta.push_back(   0.0 );
  if (numSites< 2) return;
  if (numSites==2)
     {
     phi.push_back( 0.0 ); theta.push_back( 180.0 );
     }
  else if (numSites==3) // nothing scientific... just guessing
     {
     phi.push_back(   0.0 ); theta.push_back( 120.0 );
     phi.push_back( 180.0 ); theta.push_back( 120.0 );
     }
  else if (numSites==4) // tetraeder angles
     {
     phi.push_back(   0.0 ); theta.push_back( 109.5 );
     phi.push_back( 120.0 ); theta.push_back( 109.5 );
     phi.push_back( 240.0 ); theta.push_back( 109.5 );
     }
  else if (numSites==5)  // nothing scientific... just guessing
     {
     phi.push_back(   0.0 ); theta.push_back( 120.0 );  
     phi.push_back(  90.0 ); theta.push_back( 120.0 );
     phi.push_back( 180.0 ); theta.push_back( 120.0 );
     phi.push_back( 270.0 ); theta.push_back( 120.0 );
     }
  else if (numSites==6)  // nothing scientific... just guessing
     {
     phi.push_back(   0.0 ); theta.push_back(  90.0 );  
     phi.push_back(  90.0 ); theta.push_back(  90.0 );
     phi.push_back( 180.0 ); theta.push_back(  90.0 );
     phi.push_back( 270.0 ); theta.push_back(  90.0 );
     phi.push_back(   0.0 ); theta.push_back( 180.0 );
     }
  else if (numSites==7)  // nothing scientific... just guessing
     {
     phi.push_back(   0.0 ); theta.push_back(  90.0 );  
     phi.push_back(  90.0 ); theta.push_back(  90.0 );
     phi.push_back( 180.0 ); theta.push_back(  90.0 );
     phi.push_back( 270.0 ); theta.push_back(  90.0 );
     phi.push_back(   0.0 ); theta.push_back( 180.0 );
     phi.push_back(   1.0 ); theta.push_back(   1.0 );
     }
  else assert( false );
  }
  
  
  
void write_mgeo_data( char * fname, SRModel * model )
  {
  MoleculeTypeManager *mtm   = model->mtm;
  NamesManager        *names = model->names;
  
  ofstream of (fname);
  of << "<?xml version=\"1.0\"?> \n";
  of << "<molecule-geometry-definition> \n\n";
      
  of << "  <ReaktionGeometry>" << endl;
  of << "     <DistanceDeviation value=\"0.5\" />" << endl;
  of << "     <AngularDeviation value=\"45\" />" << endl;
  of << "  </ReaktionGeometry>\n\n\n" << endl;
  
  for (int iM=0 ; iM < mtm->numMolIDs() ; iM++)
      {
      if (!mtm->isMol(iM)) continue;   // this mol-id isn't used...
      
      of << "  <molecule name=\""<< names->getName(NamesManager::MoleculeSpeciesName, iM) << "\" >\n";
      of << "     <mass   value=\"1.0\" />\n";
      of << "     <radius value=\"1.0\" />\n";
      
      // Sites:
      vector<double> phi;
      vector<double> theta;
      createSites (phi, theta, mtm->numSites(iM));
      for (int iS=0 ; iS < mtm->numSites(iM) ; iS++)
          {
          of << "     <site name=\"";
          
          // SiteFormat = MolName(SiteName) to have unique site names!
          string sname = names->getName(NamesManager::SiteName, mtm->getSiteType(iM,iS) );
          int br1 = sname.find("("); int br2 = sname.find(")");
          sname = sname.substr(br1+1, br2-br1-1);
          
          of << sname << "\" phi=\""<< phi[iS] << "\" theta=\"" << theta[iS] << "\" ";
          of << "dist=\"1.0\" />" << endl;
          }
      
      of << "  </molecule>\n\n";
      }
  of << "</molecule-geometry-definition> \n\n";
  
  of.close();
  }
