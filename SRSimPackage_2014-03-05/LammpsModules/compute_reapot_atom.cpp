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

#include "compute_reapot_atom.h"
#include "atom.h"
#include "neighbor.h"
#include "neigh_list.h"
#include "neigh_request.h"
#include "modify.h"
#include "comm.h"
#include "update.h"
#include "force.h"
#include "pair.h"
#include "memory.h"
#include "error.h"
#include "random_mars.h"

#include "SRSim/reactant_template.h"
#include "SRSim/site_reactant_template.h"
#include "SRSim/bound_reactant_template.h"
#include <math.h>
#include <stdlib.h>
#include <string.h>

using namespace LAMMPS_NS;
using namespace std;

#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))

#define RNG(a)   ((int)((double)a * rng->uniform()))

/* ---------------------------------------------------------------------- */

//   Syntax: compute 	id_5 group_all name_CRP rndSeed_23456
ComputeReapotAtom::ComputeReapotAtom(LAMMPS *lmp, int narg, char **arg) :
  Compute(lmp, narg, arg)
{
  printf ("ComputeReapotAtom::ComputeReapotAtom - constructor!\n");

  if (narg != 4) error->all("compute-reaPot:  name  id  group  reapot/atom | randomSeed");

  peratom_flag = 1;
  //size_peratom = 0;   // this field doesn't exist any more...
  comm_reverse = 1;

  avec = (AtomVecSRSim*)(atom->avec);
  rng  = new RanMars(lmp, atoi(arg[3]));
  geo  = avec->srmodel->geo;
  //nmax = 0;
  //energy = NULL;

  // Reaktions-Geometrie:
  //deviDist          = geo->deviDist;
  //deviAngle         = geo->deviAngle;
  double deviDist = geo->getMaxProperty( GPT_Devi_Dist );
  maxReactionDistSq = (deviDist+geo->getMaxBondLen())*(deviDist+geo->getMaxBondLen());
  
  ignoreBimolecularReactions = false;  // turn this speedup off per default

  gillespie.init (avec->srmodel);
  printf ("ComputeReapotAtom::ComputeReapotAtom  deviDist=%f  maxReactDistSq=%f!\n", deviDist, maxReactionDistSq);
  printf ("ComputeReapotAtom::ComputeReapotAtom - done!\n");
}

/* ---------------------------------------------------------------------- */

ComputeReapotAtom::~ComputeReapotAtom()
  {
  printf("Destruction! [ComputeReapotAtom::~ComputeReapotAtom] ... ");
  for (uint i=0 ; i<events.size() ; i++) delete events[i];
  events.clear();
  delete rng;
  printf("done.\n");
  }

/* ---------------------------------------------------------------------- */

void ComputeReapotAtom::init()
{
  printf ("ComputeReapotAtom::init!\n");
  
  
  // need an occasional half neighbor list
  int irequest = neighbor->request((void *) this);
  neighbor->requests[irequest]->pair = 0;
  neighbor->requests[irequest]->compute = 1;
  neighbor->requests[irequest]->occasional = 0;


  int count = 0;
  for (int i = 0; i < modify->ncompute; i++)
    if (strcmp(modify->compute[i]->style,"reapot/atom") == 0) count++;
  if (count > 1 && comm->me == 0)
    error->warning("More than one compute reapot/atom");
  
  // if the atoms were generated through a restart command, we'll need to recalc the angles and templates:
  avec->checkIfTemplateRecalculationNecessary();
}

/* ---------------------------------------------------------------------- */

void ComputeReapotAtom::init_list(int id, NeighList *ptr)
{
  printf ("ComputeReapotAtom::init_list!\n");
  list = ptr;
}


/* ---------------------------------------------------------------------- */

/**
 *   - Go over pairs of atoms: use 2nd-order-Reactions
 *   - use gillespie-like algorithm from SRSim_ns::RuleSet for zero/first-order-reactions
 */
void ComputeReapotAtom::compute_peratom()
{
  //printf ("--- ComputerReapot-PerAtom starts: %d\n", update->ntimestep);
  SRSim_ns::RuleSet            *rset   = avec->rset;
  SRSim_ns::SRModel            *model  = avec->srmodel;
  int                           nlocal = atom->nlocal;

  
  // all templates should be assigned:
  assert( avec->templatesNeedRecalculation == false );
  
//   if (update->ntimestep % 20 == 0)
//      {
//      printf ("Templates: \n");
//         for (int i=0 ; i<avec->amount_templs.size() ; i++)
//             {
//             //printf ("\n  i=%d: ", i);
//             //for (int k=0 ; k<avec->all_templs[i].size() ; k++)
//             //    printf (" Templ:%d", avec->all_templs[i][k]);
//             
//             printf (" T-%d - %d\n", i, avec->amount_templs[i]);
//             }
//      }
  
  // clear event queue:
  for (uint i=0 ; i<events.size() ; i++)
      delete events[i];
  events.clear();

  
  // first / zero-order reactions:
  
  
  double tau = 0;
  do {                  // repeat while there's a reaction happening
#ifndef USE_TEMPL_AFFIL_MANAGER
     tau += gillespie.timeToReaction (avec->amount_templs);
#else
     tau += gillespie.timeToReaction (avec->affiliations);
#endif

     if (tau >= update->dt) break;
     
     //printf ("  We're gonna see some 1st Order reaction now...\n");
     
#ifndef USE_TEMPL_AFFIL_MANAGER
     int     rule = gillespie.typeOfReaction (avec->amount_templs);
#else
     int     rule = gillespie.typeOfReaction (avec->affiliations);
#endif
     RuleTp *r    = rset->getRule( rule );
     if (r->in.size() == 1 )
        {    // so now we'll have to find a molecule for out fist-order reaction...
        int idTempl = r->in[0];
#ifndef USE_TEMPL_AFFIL_MANAGER
        int iMol    = (int)(rng->uniform() * (double)(avec->amount_templs[idTempl]) ) ;
#else
        int naff    = avec->affiliations.numTemplAffils(idTempl) ;
        int iMol    = (int)(rng->uniform() * (double)(naff) ) ;
        //int iMol    = (int)(rng->uniform() * (double)(avec->affiliations.numTemplAffils(idTempl) ) ;
#endif
        
        //printf ("  ::: searching for %dth mol of %d ... (Type=%d)\n", iMol, avec->amount_templs[idTempl], idTempl);
        
        // calculate the mid (int the standard atom-indexing) from the iMol (the iTh mol with template idTempl)
#ifndef USE_TEMPL_AFFIL_MANAGER
        int mid=-1;
        int cnt=-1;  // now we start counting through all sim-atoms, we'll take the iMol-th !
        for (int i=0 ; i<nlocal ; i++)
            {
            // here also, we could use binary search as the templates are sorted!
            // TODO: binary search!
            assert(STAGE<1.03);
            //  Another Idea would be to randomize a number betwee 0 and nlocal
            //          then we could just run forward to the next appearance of 
            //          the template we're searching for...
            for (uint j=0 ; j<avec->all_templs[i].size() ; j++)
                if (avec->all_templs[i][j] == idTempl) {cnt++; break;}
                
            if (cnt == iMol) // i is our Molecule!
               { mid = i; break;}
            }
        assert (mid != -1);   // if not, there are lesser templates idTempl in our sim as we thought...
#else
        int mid = avec->affiliations.midNthTemplReal( idTempl, iMol );
#endif
        
        
        // did this molecule already react this ts?
        if (avec->visited[mid] != -1)
           {
           printf ("*");
           //printf ("\nWARNING: ComputeReapotAtom::compute_peratom(): Reactant %d wanted to react twice per timestep %d!\n",i, update->ntimestep);
           //printf ("           Maybe we should think about smaller timesteps...\n");
           break;
           }
          
        /*if (avec->refractory[i] != -1)
          {
          printf ("\nWARNING: ComputeReapotAtom::compute_peratom(): Reactant %d wanted to react though it was in a refractory state!\n",i);
          break;
          }*/
        
        //printf ("#################\n 1st Order Reaction Selected at %f in ts %d!\n  mol %d of %d \n#######################\n", tau, update->ntimestep, iMol, avec->amount_templs[idTempl]);
        
        if (r->type == RuleTp::BreakR || r->type == RuleTp::ModifyR)
           {
           // we'll have to choose amound the possible reacting sites...
           std::vector<SiteIDs> sVec = getAllSitePairs( r, mid );
           assert(sVec.size() > 0);
           events.push_back( new Event(mid,-1,rule, sVec[RNG(sVec.size())]) );
           }
        else if (r->type == RuleTp::ExchangeR) events.push_back( new Event(mid,-1,rule) );
        else assert(false);
        
        avec->recVisit(mid, NULL, 0);      // mark graph as already reacted!
        }
     else /*rset->getRule(rule)->in.size() == 0*/     
        {   // zero-order reaction
        //printf ("#################\n Zero-Order Reaction Selected!\n#######################\n");
        events.push_back( new Event(-1,-1,rule) );
        }
     
     } while (tau < update->dt);
  //printf ("--- ComputerReapot-PerAtom: Part-II\n");
       
  
  
      
  // see how fast we can get, if we don't use bimolecular stuff:
  if (ignoreBimolecularReactions)
     {
     avec->unVisitAll();
     return;
     }
  
  // 2nd order reactions:
  // else rebuild the neighbour lists...
  neighbor->build_one(list->index);
    
  //int countPossibleInteractions=0;
  
  for (int ii = 0; ii < list->inum; ii++) 
      {
      int i = list->ilist[ii];
      //printf ("   Neigh i:%d   ts: %d\n",i, update->ntimestep);
      int *jlist = list->firstneigh[i];
      int  jnum  = list->numneigh[i];
      
      for (int jj = 0; jj < jnum; jj++) 
          {
          int    j      = jlist[jj];
          double distSq = (getAtomCoords(i)-getAtomCoords(j)).getLenSq();
                    
          //printf ("     j:=%d maxReactionDistSq=%f    distSq=%f \n", j, maxReactionDistSq, distSq);
          
          if (distSq > maxReactionDistSq) continue;
          if (avec->visited[i]!=-1 || avec->visited[j]!=-1) continue;  // already reacted.
          
          if (avec->refractory[i]!=-1 || avec->refractory[j]!=-1) continue;
          
          
          
          // do we have some possible reactions ?
          
          //printf ("Neigh i:%d j:%d\n",i,j);
          //std::vector<int> possibleRules;
          int possibleRules[333];
#ifndef USE_TEMPL_AFFIL_MANAGER
          int numPossibleRules = rset->fillFittingRules (avec->all_templs[i],avec->all_templs[j],possibleRules);
#else
          int numPossibleRules = rset->fillFittingRules( avec->affiliations, i, j, possibleRules);
#endif
          assert( numPossibleRules<333 );
          
          // just testing...
          //if (numPossibleRules>0) countPossibleInteractions++;
          //numPossibleRules = 0;
          
          // which templates do they belong to? Let's print it...
//           if ( avec->all_templs[i].size() > 0  ||  avec->all_templs[j].size() > 0 )
//              {
//              printf ("NPR = %d\n",numPossibleRules);
//              for (int k=0 ; k<avec->all_templs[i].size() ; k++)
//                  printf (" i(%d) Templ: %d\n", i, avec->all_templs[i][k]);
//              for (int k=0 ; k<avec->all_templs[j].size() ; k++)
//                  printf (" j(%d) Templ: %d\n", j, avec->all_templs[j][k]);
//              }
//           else { printf ("_no fitting templates found!_"); assert(numPossibleRules==0); }
          
              
          // now we'll have to find out if any of the reactions in possibleRules is going to fire
          // and if yes, which one will it be?
          
          double a0 = 0;
          double random1 = rng->uniform();      // now select one of them
          
          //printf ("Collision iTag:%d jTag:%d -- \n",atom->tag[i],atom->tag[j]);
          for (int k=0 ; k<numPossibleRules ; k++)
              {
              RuleTp *r = rset->getRule( possibleRules[k] );
              //printf (" possible Rule %d: [%d] = %s\n",k, possibleRules[k], r->toString().c_str());
              Event *et = new Event( i,j,possibleRules[k] );
                            
              // shall we flip i and j?
              et->maybe_flip_ij( avec,r );
              
              // what are the possible site-combinations?
              std::vector<SiteIDs> sVec = getAllSitePairs( r, et->i,et->j, distSq);
              
              //if (sVec.size() > 0) printf ("   Hallo!!! k=%d\n",possibleRules[k]);
              
              // Then we've got to test the geometric applicability of rule k:
              if (r->type==RuleTp::BindR  ||  r->type==RuleTp::BindIntramolR)
                 testSiteGeo( sVec, et->i,et->j );
                 
              // we don't have the appropriate geometry!
              if ( r->type!=RuleTp::ExchangeR && sVec.size() == 0 )    
                 { delete et; continue; }
              // TODO: understand why/if we need the line above!
              //   yes, it's ok: exchange rules don't need site-vectors!
              
              // what's the propensities?
              //
              //   actually we should use the formula: nCombinations *  (1-exp(k*dt)) 
              //                           instead of: nCombinations *  k * dt 
              //
              //   but practically, for small k, both functions behave very much the same,
              //   since adding up k*dt for very small dt is the numeric integration of 1-exp(k*dt)
              //  
              double nCombinations = sVec.size();
              if (r->type==RuleTp::ExchangeR) nCombinations = 1;
              a0 += ( nCombinations * model->kinetics->getRate(possibleRules[k]) * update->dt);
              
              
              // to next reaction if we don't take it...
              if (random1 > a0) { delete et; continue; }
              
              // choose one of the site-combinations randomly:
              if (r->type!=RuleTp::ExchangeR)
                 { 
                 assert( sVec.size() > 0 );
                 int random2 = (int)( rng->uniform() * (double)(sVec.size()) );
                 et->sites = sVec[random2];
                 }
                 
              // let's take this rule:
              events.push_back( et );
              avec->recVisit(i, NULL, 0);           // warum nur i???
              if (avec->visited[j]==-1) avec->recVisit(j, NULL, 0);    // it's conditioned in case we've got a cyclic graph!
              
              //printf ("#################\n 2nd Order Reaction Selected!\n#######################\n");
              //printf ("distSq=%f , maxReactionDistSq=%f random1=%e  a0=%e\n",distSq, maxReactionDistSq, random1, a0);
              
              break;  // no need to visit other reactions!   
              }
          //if (a0 > 1.0) printf ("\nWARNING: ComputeReapotAtom::compute_peratom(): sum of propensities > 1!\n");
          //printf ("    nts=%d\n", update->ntimestep);
              
          
          // choose Reaction from propensities:
/*          double r2 =  rng->uniform();             // now select one of them
          if (r2 > props.back()) continue;         // nope, no reaction at all.
          for (int k=props.size()-1 ; k>0 ; k--)
              if (r2 >= props[k-1] && r2 < props[k])
                 {
                 //printf ("pushing event:\n");
                 events.push_back( new Event(i,j,possibleRules[k-1],sites[k-1]) );
                 avec->recVisit(i, NULL, 0);
                 if (avec->visited[j]==-1) avec->recVisit(j, NULL, 0);    // it's conditioned in case we've got a cyclic graph!
                 printf ("#################\n 2nd Order Reaction Selected!\n#######################\n");
                 break;
                 }*/
          }
          
      }
      
  // now, all reactions are set, so we can lift the visitations:
  avec->unVisitAll();
  //printf ("--- ComputerReapot-PerAtom done: %d  (interactions: %d)\n", update->ntimestep, countPossibleInteractions);
  }

/* ---------------------------------------------------------------------- */

int ComputeReapotAtom::pack_reverse_comm(int n, int first, double *buf)
{
/*  int i,m,last;

  m = 0;
  last = first + n;
  for (i = first; i < last; i++) buf[m++] = energy[i];*/
  return 1;
}

/* ---------------------------------------------------------------------- */

void ComputeReapotAtom::unpack_reverse_comm(int n, int *list, double *buf)
{
/*  int i,j,m;

  m = 0;
  for (i = 0; i < n; i++) {
    j = list[i];
    energy[j] += buf[m++];
  }*/
}

/* ----------------------------------------------------------------------
   memory usage of local atom-based array
------------------------------------------------------------------------- */

double ComputeReapotAtom::memory_usage()
{
  //double bytes = nmax * sizeof(double);
  return 412;//bytes;
}

/* ---------------------------------------------------------------------- */

Coords LAMMPS_NS::ComputeReapotAtom::getAtomCoords( int i )
  {
  Coords c;
  double **x = atom->x;
  c[0] = x[i][0];
  c[1] = x[i][1];
  c[2] = x[i][2];
  return c;
  }

/* ---------------------------------------------------------------------- */
typedef std::pair<int,int> SiteIDs;

  
/** Also partially evaluates if the reaction geometry fits. This function only looks for the
    distances. "testSiteGeo" on the other hand does the angular deviations... sad, that is!
    
    The reason for this funny separation is, I wanted to grow smaller lists of possible 
    reacting sites.
    
    So why not include the angular tests from testSiteGeo as well?
    
    How is the angular/distance - deviation computed: just take the smaller of both values!
  */
vector<SiteIDs> ComputeReapotAtom::getAllSitePairs( RuleTp *r, int i, int j, double distSq )
  {
  std::vector<SiteIDs> sites;
  //printf ("ComputeReapotAtom::getAllSitePairs %d\n", update->ntimestep);
    
  if (r->type == RuleTp::ExchangeR) return sites;  
  
  else if (r->type == RuleTp::BindR  ||  r->type == RuleTp::BindIntramolR)
     {
     int iType = atom->type[i];
     int jType = atom->type[j];
     
     SRSim_ns::SiteReactantTemplate *srt1 = dynamic_cast<SRSim_ns::SiteReactantTemplate*>( avec->rset->getRT( r->in[0] ) );
     SRSim_ns::SiteReactantTemplate *srt2 = dynamic_cast<SRSim_ns::SiteReactantTemplate*>( avec->rset->getRT( r->in[1] ) );
     std::vector<int> sitesI = avec->findAllFittingSites( i, srt1->getStartSite()->getType(), srt1->getStartSite()->getModif(), true/*free*/ );
     std::vector<int> sitesJ = avec->findAllFittingSites( j, srt2->getStartSite()->getType(), srt2->getStartSite()->getModif(), true/*free*/ );

     /*SRSim_ns::BoundReactantTemplate *brt1 = dynamic_cast<SRSim_ns::BoundReactantTemplate*>( avec->rset->getRT( r->out[0] ) );
     printf (" SRT1-startsite-type = %d \n",srt1->getStartSite()->getType());
     printf (" SRT2-startsite-type = %d \n",srt2->getStartSite()->getType());
     printf (" BRT3-startsite-type = %d \n",brt1->getStartSite()->getType());*/
     
     for (uint ii=0 ; ii<sitesI.size() ; ii++)
         for (uint jj=0 ; jj<sitesJ.size() ; jj++)
             {
             //double myDist    = geo->getSiteLength( iType, sitesI[ii] );
             //       myDist   += geo->getSiteLength( jType, sitesJ[jj] );
             double deviDist =      geo->getProperty( GPT_Devi_Dist, iType, sitesI[ii] );
                    deviDist = min( geo->getProperty( GPT_Devi_Dist, jType, sitesJ[jj] ), deviDist );
             
             double myDist    = geo->getProperty( GPT_Site_Dist, iType, sitesI[ii] );
                    myDist   += geo->getProperty( GPT_Site_Dist, jType, sitesJ[jj] );
             double distMaxSq = (myDist+deviDist)*(myDist+deviDist);
             double distMinSq = (myDist<deviDist)?(0):(myDist-deviDist)*(myDist-deviDist);
             
             //printf ("DistMaxSq=%f   DistMinSq=%f   distSq=%f \n", distMaxSq, distMinSq, distSq);
             if (distSq > distMaxSq || distSq < distMinSq) continue;
             //printf ("       Taken! \n");
             
             SiteIDs sid( sitesI[ii], sitesJ[jj] );
             
             //printf ("  Ein paar: i=%d, j=%d \n",sitesI[ii],sitesJ[jj]);
             
             sites.push_back( sid );
             }
     }
     
  else if (r->type == RuleTp::BreakR)
     {
     // TODO: this will only output the first possible site to be breaking, not any! e.g.:
     //        A(a,b!1).A(c!1,c!2).A(b!2,a)  --> will always split up !1 first! BAAAAD....
     
     int needTemplate = r->in[0];
#ifndef USE_TEMPL_AFFIL_MANAGER
     int fittingSite  = avec->findFittingSite(i, needTemplate);
#else
     int fittingSite  = avec->affiliations.sidRandFittingSite( needTemplate, i, rng->uniform() );
#endif
     assert (fittingSite != -1);
     
     SiteIDs sid( fittingSite, -666/*no need for a second site*/ );
     sites.push_back( sid );
     }
     
  else if (r->type == RuleTp::ModifyR)
     {
     sites.push_back( SiteIDs(-666,-666) );
     
     if ( avec->rset->getRT(r->in[0])->getRTType() == ReactantTemplate::ModifRT )
#ifndef USE_TEMPL_AFFIL_MANAGER
        sites[0].first  = avec->findFittingSite(i, r->in[0]);
#else
        sites[0].first  = avec->affiliations.sidRandFittingSite( r->in[0], i, rng->uniform() );
#endif
     //printf ("   ---  f: %d    s: %d  ... ModiRT: %d \n", sites[0].first , sites[0].second, avec->rset->getRT(r->in[0])->getRTType() == ReactantTemplate::ModifRT);
     if (r->in.size() == 1) 
        {
        assert (sites[0].first>=0);
        return sites;
        }
        
     if ( avec->rset->getRT(r->in[1])->getRTType() == ReactantTemplate::ModifRT )
#ifndef USE_TEMPL_AFFIL_MANAGER
        sites[0].second = avec->findFittingSite(j, r->in[1]);
#else
        sites[0].second  = avec->affiliations.sidRandFittingSite( r->in[1], j, rng->uniform() );
#endif
     
     assert (sites[0].first>=0 || sites[0].second>=0);
     //printf ("   ---  f: %d    s: %d \n", sites[0].first , sites[0].second);
     }
     
  else assert(false);
     
  //printf ("ComputeReapotAtom::getAllSitePairs done\n");
  return sites;
  }

  
/* ---------------------------------------------------------------------- */


double ComputeReapotAtom::getAng( Coords& xa, Coords& xb )
  {
  assert( xa.isSane() );
  assert( xb.isSane() );
  
  double sp   = Coords::scalar(xa,xb);
  double aRad = xa.getLen();  // j->i
  double bRad = xb.getLen();  // j->i
  sp /= aRad * bRad;
  
  if (sp >  1.0) sp =  1.0;          // Owowowowow - one should have been aware of this error! Oh stupid me!
  if (sp < -1.0) sp = -1.0;          //   returned angle's been NaN ->  bonds with wrong angles have been established!
  
  //printf ("         sp=%f  (sp>1)=%d    aRad=%f   bRad=%f\n",sp,(sp>1.0),aRad,bRad);
  double ac = acos( sp );
  ac *= 180.0/M_PI;
  
  //printf ("         ac=%f\n",ac);
  if ( fpclassify(ac)!=FP_NORMAL && fpclassify(ac)!=FP_ZERO)
     {
     printf ("ComputeReapotAtom::getAng: That's strange: angle calculated as being: %f; aRad=%f, bRad=%f\n", ac, aRad, bRad);
     assert( false );
     error->all("ComputeReapotAtom::getAng: no standard floating point value measured.");
     }
  //assert( fpclassify(ac)==FP_NORMAL || fpclassify(ac)==FP_ZERO );
  return ac;
  }

/* ---------------------------------------------------------------------- */



void ComputeReapotAtom::testSiteGeo( std::vector<SiteIDs> &sVec, int i, int j)
  {
  //printf (" ComputeReapotAtom::testSiteGeo... starting with %d site - combinations!\n", sVec.size());
  
  Coords iPos      = getAtomCoords(i);
  Coords jPos      = getAtomCoords(j);
  Coords i2j       = jPos - iPos;
  Coords j2i       = - i2j;
  //double dist      = i2j.getLen();
  int    numSitesI = avec->type2numSites[ atom->type[i] ];
  int    numSitesJ = avec->type2numSites[ atom->type[j] ];
  int    iType     = atom->type[i];
  int    jType     = atom->type[j];
    
  assert( iPos.isSane() );                  // check for NaNs!
  assert( jPos.isSane() );                  // check for NaNs!
  assert( i2j.isSane() );                  // check for NaNs!

    
  for (uint k=0 ; k<sVec.size() ; k++)
      {
      // distance:
      //printf ("Dist = %f, deviDist = %f, taken=%d\n",dist, deviDist, (dist>deviDist));
      //if (dist > deviDist) {sVec.erase(sVec.begin()+k); k--; continue;}
      
      // angles in i:
      int    iSite     = sVec[k].first;
      double deviAngle = geo->getProperty( GPT_Devi_Angle, iType, iSite );
      double maxDevi   = 0.0;
      for (int s=0 ; s<numSitesI && maxDevi<=deviAngle ; s++)
          {
          if (s == iSite) continue;
          //int sbt = avec->site_bound_to[i][s];        // is site s bound?
          if (avec->site_bound_tag[i][s] == -1) continue;
                                                      // our desired Angle
          double desiredAngle = geo->getAngle( geo->getAngleId(iType,iSite,s) );
          
          int mol3 = avec->findOtherEnd(i,s);         // where is it bound to?
          Coords i2mol3 = getAtomCoords(mol3) - iPos;
          assert( i2mol3.isSane() );                  // check for NaNs!
          double ang = getAng(i2j, i2mol3);           // what angle do we have?
                    
          double diff = ang - desiredAngle; if (diff<0) diff=-diff;
          if (diff > maxDevi) maxDevi=diff;
          
          /*if (iType == 1 && jType==1)
             {*/
             //printf (" k%d: (%d/%d)[%d/%d] Ang = %f   maxDevi=%f    desiredAngle=%f\n",k,i,j, iType,jType, ang, maxDevi, desiredAngle);
             /*}*/
          
          }
          
      // shall we dispose of this site-proposition?
      if (maxDevi > deviAngle) 
         {
         //printf ("    deleting k=%d\n", k);
         sVec.erase(sVec.begin()+k); 
         k--; 
         continue;}

      // angles in j:
      int jSite     = sVec[k].second;
          deviAngle = geo->getProperty( GPT_Devi_Angle, jType, jSite );
          maxDevi   = 0.0;
      for (int s=0 ; s<numSitesJ ; s++)
          {
          if (s == jSite) continue;
          //int sbt = avec->site_bound_to[j][s];        // is site s bound?
          if (avec->site_bound_tag[j][s] == -1) continue;
                                                      // our desired Angle
          double desiredAngle = geo->getAngle( geo->getAngleId(jType,jSite,s) );
          
          int mol3 = avec->findOtherEnd(j,s);         // where is it bound to?
          Coords j2mol3 = getAtomCoords(mol3) - jPos;
          assert( j2i   .isSane() );                  // check for NaNs!
          assert( j2mol3.isSane() );                  // check for NaNs!
          double ang = getAng(j2i, j2mol3);           // what angle do we have?
          
          double diff = ang - desiredAngle; if (diff<0) diff=-diff;
          if (diff > maxDevi) maxDevi=diff;
          }
      
      // shall we dispose of this site-proposition?
      if (maxDevi > deviAngle) {sVec.erase(sVec.begin()+k); k--; continue;}
          
      // handedness of bond-system?
      //  forhet this one here: will be answered by improper bonds!
      assert( STAGE < 1.02 );
      }
  
  //printf ("   leaving ComputeReapotAtom::testSiteGeo... with %d combinations.\n", sVec.size());
  }



void ComputeReapotAtom::Event::maybe_flip_ij( AtomVecSRSim *avec, RuleTp *r)
  {
  // Maybe we've got to flip the sites as our i/j and the ruleset i/j are upside down:
  if (r->in.size() > 1) 
     {
     int it=r->in[0] , jt=r->in[1] ;    // i- and j-templates
#ifndef USE_TEMPL_AFFIL_MANAGER
     if ( (!avec->fitsToTemplate(i,it)) || (!avec->fitsToTemplate(j,jt)) )
#else
     if ( (!avec->affiliations.belongsToTempl(it,i)) || (!avec->affiliations.belongsToTempl(jt,j)) )
#endif
        {
        //printf ("Flipping i and j.\n");
        int buf = i; 
        i = j; 
        j = buf; 
        
        buf         =sites.first; 
        sites.first =sites.second; 
        sites.second=buf;
        }
     //printf ("et->i:%d     et->j:%d\n", et->i, et->j);
#ifndef USE_TEMPL_AFFIL_MANAGER
     assert( avec->fitsToTemplate(i,it) && avec->fitsToTemplate(j,jt) );
#else
     assert( avec->affiliations.belongsToTempl(it,i) && avec->affiliations.belongsToTempl(jt,j) );
#endif
     }
  }
