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

#include "math.h"
#include "stdlib.h"
#include "string.h"
#include "atom.h"
#include "update.h"
#include "respa.h"
#include "error.h"
#include "random_mars.h"
#include "memory.h"
#include "modify.h"
#include "compute.h"
#include "compute_reapot_atom.h"
#include "domain.h"

#include "fix_srsim.h"
#include <SRSim/names_manager.h>
#include <SRSim/bng_rule_builder.h>
#include <SRSim/site_reactant_template.h>
#include "lammps_molecule.h"

#include <set>

// only need this one for debug:
#include <SRSim/bound_reactant_template.h>
#include <SRSim/modification_reactant_template.h>

using namespace LAMMPS_NS;

#define COMP_IDENT_STRING "internal_reapot_computation"

/* ---------------------------------------------------------------------- */

//   Syntax: fix 	id_5 group_all name_srsim nevery_1 rndSeed_23456
FixSRSim::FixSRSim(LAMMPS *lmp, int narg, char **arg) :
  Fix(lmp, narg, arg)
{
  printf ("FixSRSim::FixSRSim - constructor!\n");
  
  if (narg != 10) error->all("fix-SRSim:  id  group srsim | nEvery  randomSeed preFactBinR preFactBreakR preFactExchangeR preFactModifyR_1 preFactModifyR_2");
  nevery = atoi(arg[3]);
  if (nevery <= 0) error->all("Illegal fix SRSim command: nevery too small");

  // random number generator:
  rng = new RanMars(lmp, atoi(arg[4]));
  
  // init Rule Sys: nope - we take the RuleSys from the aVec.
  avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  rset = avec->rset;
  
  // add Event-Queue computation
  computeCmd = new char*[4];
  computeCmd[0] = COMP_IDENT_STRING;
  computeCmd[1] = "all";//arg[1]; // group
  computeCmd[2] = "reapot/atom";
  computeCmd[3] = arg[4];
  modify->add_compute(4,computeCmd);
  delete[] computeCmd;
  
  // Reaction-Rate-Prefactors: maybe scale some rates!
  avec->srmodel->kinetics->scaleRates( rset, atof(arg[5]),atof(arg[6]),atof(arg[7]), atof(arg[8]), atof(arg[9]) );

  // refractory time for molecules until they can react after a break-Reaction:
  //refractoryTime = atoi(arg[10]);
  //  --> now given through geometry_definition -> getProperty(GPT_Refractory)
  
  //  MPI_Comm_rank(world,&me);
  
  // TODO: The avec->nspecial / avec->special properties are not correctly set by our bond forming functions.
  // It would be better to have SRSim form the bonds making use of these "special" data structures.
  for (int i=0 ; i<atom->nlocal ; i++) 
      {
      atom->nspecial[i][0] = 0;
      atom->nspecial[i][1] = 0;
      atom->nspecial[i][2] = 0;
      }
}

/* ---------------------------------------------------------------------- */

FixSRSim::~FixSRSim()
{
  printf("Destruction! [FixSRSim::~FixSRSim]\n");
  modify->delete_compute(COMP_IDENT_STRING);
  //delete rng;
}

/* ---------------------------------------------------------------------- */

int FixSRSim::setmask()
{
  int mask = 0;
  mask |= FINAL_INTEGRATE;
  return mask;
}

/* ---------------------------------------------------------------------- */

// before every run:
void FixSRSim::init()
  {
  printf ("initing FixSRSim!\n");
  
  int icompute = modify->find_compute(COMP_IDENT_STRING);
  if (icompute < 0) error->all("Reapot-Compute ID for fix reapot/atom does not exist!");
  computeReapot = (ComputeReapotAtom*) (modify->compute[icompute]);
  //computeReapot->init();              // not yet called by Modify::init()
  
  // setup fitting-templates-array...
  //updateInitial ();
  //  Not necessary any more - we're now updating the template information just when we create the molecules!
  }

/* ---------------------------------------------------------------------- */

// before the very first run:
//void FixSRSim::setup()
//{  
/*  int  nlocal   = atom->nlocal;
  int *visited  = atom->visited;
  
  for (int i = 0; i < nlocal; i++)
      {
      visited[i] = -1;  // the standard-unvisited value when we walk through
                        // the graph recursively later.
      }*/
  // There's no need to init 'visited', as it's inited by 
  //    atomVecSRSim::create_atom!
 
  
  
//}

/* ---------------------------------------------------------------------- */



/**
 *    The problem with modifying rules is, that the site to be modified does not
 *      have to be one of the sites that collide!
 *    A(a,b).B(b,a).A(a,b) + XYZ() -> A(a~blub,b).B(b,a).A(a,b) + XYZ()
 *      Here, we don't have any clue, which sites are actually colliding
 */
void FixSRSim::final_integrate() 
  {
  SRSim_ns::GeometryDefinition *geo       = avec->srmodel->geo;
  DumpRea                      *reaDumper = avec->reaDumper;
  
  #ifndef SRSIM_IGNORE_SLOW_ASSERTIONS
    //assert (update->ntimestep < 56);
    for (int i=0 ; i<atom->nlocal ; i++)       // all nodes should be unvisited!
        assert (avec->visited[i] == -1);
  #endif
  
  
  // clear refactory-state?
  avec->unRefract();
  
  // Reaction Potential?
  computeReapot->compute_peratom();
  //printf ("--- final_integrate starts: %d    nlocal=%d\n", update->ntimestep,atom->nlocal);
  
  ComputeReapotAtom::Event *et;
  
  // When deleting molecules, we have to be sure that we did everything else before.
  // Reason is, the indices are likely to change after deleting molecules.
  // So we're building up a general toDelete and 
  set<int> toDelete;
  set<int> toUpdate;

  int imax = computeReapot->events.size();
  for (int i=0 ; i<imax ; i++)
      {
      et = computeReapot->events[i];
      SRSim_ns::RuleTp *r = rset->getRule( et->reaID );
      
      //printf ("printfiprin ntimestep=%d   (%d/%d), %d (%d,%d)\n", update->ntimestep, i, imax, r->in.size(), et->i, et->j );

      // if applicable (ReaDumper is present), remember the rection educts
      int secondReactionProduct = -1;  // only applicable for breaking-reactions
      if (reaDumper != NULL) {
         LammpsMolecule *lmA = NULL,*lmB = NULL; 
         
         if (r->in.size() >= 1) lmA = new LammpsMolecule( et->i );
         if (r->in.size() >= 2) lmB = new LammpsMolecule( et->j );
         
         reaDumper->setCurrentReactionLHS( lmA,lmB );
         
         if (r->in.size() >= 1) delete lmA;
         if (r->in.size() >= 2) delete lmB;
         
         //printf ("Done with lhs, refCnt = %d.\n", LammpsMolecule::refCnt );
         }

  
  
      if (r->type == RuleTp::BindR)
         {
         //printf ("B");
         printf (" |ts %5d| %s   (%d-%d & %d-%d) \n", update->ntimestep, r->toString().c_str(), et->i,et->sites.first,et->j,et->sites.second);
         
         
         avec->addNewBond (et->i, atom->tag[et->j], et->sites.first, et->sites.second);
      
         avec->updateSubgraphTemplateData (et->i);
         //printf ("   maxReactiveTemplateSize = %d\n", rset->maxReactiveTemplateSize() );
         
         //avec->activateAngles(et->i);
         //avec->activateAngles(et->j);
         
         //reactivateSubgraphAngles(et->i, 2);
         //reactivateSubgraphAngles(et->j, 2);  // some molecules will be triggered twice now
         avec->reactivateSubgraphAngles(et->i, 3);    // going too far in some directions, but no mols will be triggered twice.
         
         
         
         //printf ("Done.\n");
         
         /*if (update->ntimestep == 87)
            {
            LammpsMolecule lm(145);
            lm.writeToDotFile ("spass.dot");
            
            printf ("S1 = %d, S2 = %d\n",et->sites.first, et->sites.second);
            
            //int ti = atom->type[et->i];
            avec->srmodel->names->printAll();
            }*/
         }
         
      else if (r->type == RuleTp::BreakR)   
         {
           //printf ("R");
         printf (" |ts %5d| %s \n", update->ntimestep, r->toString().c_str());
      
      
         //printf (" [i:%d, site:%d] ", et->i,et->sites.first);
                           
         /*if (update->ntimestep == 87)
            {
            LammpsMolecule lm(145);
            lm.writeToDotFile ("spass.dot");
            
            //BoundReactantTemplate *brt = dynamic_cast<BoundReactantTemplate*>(avec->rset->getRT(2));
            //brt->getStartSite()->getMol()->writeToDotFile("spassT.dot");
            }
            
         printf ("et->i = %d (tag=%d) to templates: ", et->i, atom->tag[et->i]);
         for (int i=0 ; i<avec->all_templs[et->i].size() ; i++)
             printf ("%d ", avec->all_templs[et->i][i]);
         int type = atom->type[et->i];
         for (int i=0 ; i<avec->type2numSites[type] ; i++)
             for (int j=0 ; j<avec->site_templs[et->i][i].size() ; j++)
                 printf ("S%d(%d) ", i, avec->site_templs[et->i][i][j] );
         printf ("\n");*/
         
         // we have to know, which site exactly is breaking, so find it:  run over sites:
         //int needTemplate = r->in[0];
         //int fittingSite  = avec->findFittingSite(et->i, needTemplate);
         ////if (fittingSite == -1) continue;
         //assert (fittingSite != -1);
         int otherEnd     = avec->findOtherEnd   (et->i, /*fittingSite*/et->sites.first);
         secondReactionProduct = otherEnd;
         
         // first break the bond, then update the template data!
         avec->breakBond (et->i, et->sites.first);
         avec->updateSubgraphTemplateData (et->i);
         avec->updateSubgraphTemplateData (otherEnd);
         
         avec->reactivateSubgraphAngles(et->i,    2);
         avec->reactivateSubgraphAngles(otherEnd, 2);
         
         int ref_i     = geo->getProperty( GPT_Refractory, atom->type[et->i   ] );
         int ref_other = geo->getProperty( GPT_Refractory, atom->type[otherEnd] );
         avec->refractMol(et->i   , update->ntimestep + ref_i);
         avec->refractMol(otherEnd, update->ntimestep + ref_other);
         
         //printf ("  (%d & %d)\n",et->i,otherEnd);
         }

      else if (r->type == RuleTp::ModifyR)
         {
           //printf ("M");
         printf (" |ts %5d| %s \n", update->ntimestep, r->toString().c_str());
         
         if (et->sites.first >= 0) // modification on first mol of Rule...
            {
            ModificationReactantTemplate *mrt = dynamic_cast<ModificationReactantTemplate*>( rset->getRT(r->out[0]) );
            assert( mrt != NULL );
            int iSite    = et->sites.first;
            int newModif = mrt->getModif();

            //printf ("   old modif: %d",avec->site_modified[et->i][iSite]);
            avec->site_modified[et->i][iSite] = newModif;
            //printf ("   new modif: %d",avec->site_modified[et->i][iSite]);
            avec->updateSubgraphTemplateData (et->i);
            }
         if (et->sites.second >= 0) // modification on second mol of Rule...
            {
            ModificationReactantTemplate *mrt = dynamic_cast<ModificationReactantTemplate*>( rset->getRT(r->out[1]) );
            assert( mrt != NULL );
            int jSite    = et->sites.second;
            int newModif = mrt->getModif();
            //printf ("   2old modif: %d",avec->site_modified[et->j][jSite]);
            avec->site_modified[et->j][jSite] = newModif;
            //printf ("   2new modif: %d",avec->site_modified[et->j][jSite]);
            avec->updateSubgraphTemplateData (et->j);
            }
            
         assert (et->sites.first >= 0 || et->sites.second >= 0 );
         }
               
      else if (r->type == RuleTp::ExchangeR)// exchange rule 
         {
           //printf ("E");
         printf (" |ts %5d| %s \n", update->ntimestep, r->toString().c_str());
         
         // remember from which area we deleted molecules in min&max:
         SRSim_ns::Coords  min,max;
         if ( r->in.size() == 0 ) // use the whole reactor size, to create new molecules
            {
            min = SRSim_ns::Coords( domain->sublo[0], domain->sublo[1], domain->sublo[2] );
            max = SRSim_ns::Coords( domain->subhi[0], domain->subhi[1], domain->subhi[2] );
            }
         
         // delete old molecules:
         for (uint ii=0 ; ii < r->in.size() ; ii++)
             {
             // retrieve the appropriate template:
             MultiMolReactantTemplate *mmt = dynamic_cast<MultiMolReactantTemplate*>( rset->getRT(r->in[ii]) );
             assert( mmt != NULL );
             
             // what other molecules are mentioned in the template?
             int molid = (ii==0) ? (et->i) : (et->j);
             LammpsMolecule lm(molid);
             vector<int> toDeleteHere = mmt->returnMatchedMoleculeTags( &lm );
             toDelete.insert( toDeleteHere.begin(), toDeleteHere.end() );
             
             
             // find all connected molecules -> their angles will have to be updated later on!
             // also remember from which area we took the deleted molecules!
             //set<int> toUpdate;
             for (uint iii=0 ; iii<toDeleteHere.size() ; iii++)
                 {
                 int id     = atom->map(toDeleteHere[iii]);
                 int type   = atom->type[ id ];
                 int nsites = avec->type2numSites[type];
                 for (int iiii=0 ; iiii<nsites ; iiii++)
                     if (avec->site_bound_tag[id][iiii] != -1)
                        toUpdate.insert( avec->site_bound_tag[id][iiii] );  // that should be what was actually indended...
                        //toUpdate.insert( atom->tag[id] );    // Gerdl: I believe this line is wrong - it should be the connected molecule that has to be updated!
                        
                 // remember the area, from which we deleted molecules; later we will create new molecules in this area!
                 if (atom->x[id][0] < min[0]) min[0] = atom->x[id][0];
                 if (atom->x[id][1] < min[1]) min[1] = atom->x[id][1];
                 if (atom->x[id][2] < min[2]) min[2] = atom->x[id][2];
                 if (atom->x[id][0] > max[0]) max[0] = atom->x[id][0];
                 if (atom->x[id][1] > max[1]) max[1] = atom->x[id][1];
                 if (atom->x[id][2] > max[2]) max[2] = atom->x[id][2];
                 
                 // if it's the first considered deletable atom, min & max will be initially set:
                 if (ii==0 && iii==0)
                    {
                    max[0] = min[0] = atom->x[id][0];
                    max[1] = min[1] = atom->x[id][1];
                    max[2] = min[2] = atom->x[id][2];
                    }
                 }
                 
             // deleting will happen at the end of the timestep
             }
         
         //printf ("    %d molecules to delete.\n", toDelete.size() );
         
         // create new molecules:
         //assert ( false );
         for (uint ii=0 ; ii < r->out.size() ; ii++)
             {
             // retrieve the appropriate template:
             MultiMolReactantTemplate *mmt = dynamic_cast<MultiMolReactantTemplate*>( rset->getRT(r->out[ii]) );
             assert( mmt != NULL );
             
             // search for the area to create the new molecules in!
             double x = min[0] + rng->uniform() * (max[0] - min[0]);
             double y = min[1] + rng->uniform() * (max[1] - min[1]);
             double z = min[2] + rng->uniform() * (max[2] - min[2]);
             avec->addTemplate2Sim( mmt, x,y,z);
             
             // We don't need to calculate the tempaltes for the new molecules now:
             //   That was already done by addTemplate2Sim(...) !
             //printf ("    %d molecules to be created.\n", mmt->numMolecules() );
             }
         }
         
      else if (r->type == RuleTp::BindIntramolR)
         {
           //printf ("I");
         printf (" |ts %5d| %s   (%d & %d) \n", update->ntimestep, r->toString().c_str(), et->i,et->j);
         
         ReactantTemplate *tgtTemplate = rset->getRT(r->out[0]);
         LammpsMolecule *lm1 = new LammpsMolecule(et->i);
         LammpsMolecule *lm2 = new LammpsMolecule(et->j);
         assert (! tgtTemplate->matchMolecule(lm1, et->sites.first ) );
         assert (! tgtTemplate->matchMolecule(lm2, et->sites.second) );
         
         //assert (false);    // ===>  muss matchMolecule nehmen, nicht fitsToTemplate, denn u.U. ist das tgtTemplate
                            //        gar kein reactive Template!
         //assert (! avec->fitsToTemplate(et->i, r->out[0]) );
         //assert (! avec->fitsToTemplate(et->j, r->out[0]) );
         
         avec->addNewBond (et->i, atom->tag[et->j], et->sites.first, et->sites.second);
         
         // test if the new bond had the right effect!
         // namespace-brackets will be used to have lm deleted after the first matching process!
         bool success = false;
         if ( !success && tgtTemplate->matchMolecule(lm1, et->sites.first ) ) success=true; 
         if ( !success && tgtTemplate->matchMolecule(lm2, et->sites.second) ) success=true;
         delete lm1; 
         delete lm2;
         
         if (!success)
            {
            printf ("  FixSRSim::final_integrate: faulty IntramolBinding reaction - but no worries - restoring.\n");
#ifndef USE_TEMPL_AFFIL_MANAGER
            //printf ("   tgt-Template = %d \n", tgtTemplate);
            for (uint nt=0 ; nt<avec->all_templs[et->i].size() ; nt++)
                printf ("     iT%d \n", avec->all_templs[et->i][nt]);
            for (uint nt=0 ; nt<avec->all_templs[et->j].size() ; nt++)
                printf ("     jT%d \n", avec->all_templs[et->j][nt]);
#endif
            
            // now we've gotta clean up the mess:
            avec->breakBond (et->i, et->sites.first);
            }
         else // success! We actually formed a new bond!
            {
            avec->updateSubgraphTemplateData (et->i);
            avec->reactivateSubgraphAngles(et->i, 3);
            }
         }
         
      else assert( false ); // this doesn't exist at all!
      
      //printf ("new Bond: %d %d  -- ts:%d \n", et->i,et->j, update->ntimestep);
      
      
      // if applicable (ReaDumper is present), remember the rection products
      if (reaDumper != NULL) {
         //printf ("rhs: %d -> %d\n", r->in.size(), r->out.size() );
         if (r->type == RuleTp::ExchangeR)
            error->all("FixSRSim::final_integrate: Dumping the reactions does not yet work with ExchangeRules.");
         if (r->type == RuleTp::ModifyR)
            secondReactionProduct = et->j;
        
         LammpsMolecule *lmC = NULL,*lmD = NULL; 
         
         if (r->out.size() >= 1) lmC = new LammpsMolecule( et->i );
         if (r->out.size() >= 2) lmD = new LammpsMolecule( secondReactionProduct );
         
         reaDumper->setCurrentReactionRHS( lmC,lmD );
         
         if (r->out.size() >= 1) delete lmC;
         if (r->out.size() >= 2) delete lmD;
         
         //printf ("Done with rhs, refCnt = %d.\n", LammpsMolecule::refCnt );
         }
      
      
      
      }
      
      
  // toUpdate: remove those tags, that are already in toDelete - we don't want to update them any more!
  set<int>::iterator it;
  for ( it=toDelete.begin() ; it != toDelete.end(); it++ )
      if (toUpdate.count( *it ) > 0)
         toUpdate.erase( *it );
  
  // now delete the molecules that we wanted to get rid of, in the exchange rules:
  //printf ("TS %d -> deleting %d particles.  nlocal before:%d %f\n", update->ntimestep, toDelete.size(), atom->nlocal, atom->natoms );
  //for ( it=toDelete.begin() ; it != toDelete.end(); it++ )
  //    printf ("    We have to delete tag: %d  ==  id: %d %d\n", *it, atom->map(*it));
  avec->removeAtomsByTag( toDelete );
  //printf ("TS %d -> deleting %d particles.  nlocal after:%d %f\n", update->ntimestep, toDelete.size(), atom->nlocal, atom->natoms );
  
  // and now update the Angles and Templates of the remaining, but connected molecules:
  //printf ("         %d leftovers - updating %d particles.\n",atom->nlocal, toUpdate.size() );
  for ( it=toUpdate.begin() ; it != toUpdate.end(); it++ )
      {
      int u = *it;
      int uId = atom->map( u );
      avec->updateSubgraphTemplateData (uId);
      avec->reactivateSubgraphAngles   (uId,2);
      
      // TODO: this update/reactiviate isn't ideal yet - we might try avoiding 
      //        calling this function for the same molecules multiple times!
      }
  
  //printf ("=== final_integrate done: %d\n", update->ntimestep);
  }



