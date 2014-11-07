//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <map>

#include "molecule_type_manager.h"


using namespace SRSim_ns;


MoleculeTypeManager::MoleculeTypeManager ()
  {
  maxUSID = -1;
  numExistingMols = 0;
  }

MoleculeTypeManager::~MoleculeTypeManager ()
  {
  for (int i=0 ; i<molTypes.size() ; i++)
      delete molTypes[i];
  molTypes.clear();
  }


void SRSim_ns::MoleculeTypeManager::registerMolecule( TemplMolecule * m )
  {
  assert (m != NULL);
  
  int mID = m->getType();
  if (mID >= molTypes.size())
     {
     molTypes.resize (mID+1, NULL);
     uniqueSiteIDs.resize (mID+1);
     }
  if (molTypes[mID] == NULL) 
     {
     molTypes[mID] = new TemplMolecule (mID, 0);
     numExistingMols++;
     }
  
  // if necessary add sites.
  //   check every m site against all the proto sites; blacklist used proto sites.
  TemplMolecule *proto = molTypes[mID];    assert(proto!=NULL);
  map<int,int>   black;
  for (int i=0 ; i<m->numSites() ; i++)
      {
      bool found = false;
      for (int j=0 ; j<proto->numSites() ; j++)
          if (proto->getSiteType(j) == m->getSiteType(i) && black.count(j)==0 )
             {
             black[j] = 1; found=true;
             break; // found a fitting site for i
             }
             
      if (!found)  // we have to add a new site:
         {
         int j = proto->numSites();
         int sType = m->getSiteType(i);
         proto->addSite(sType, 0, 0);
         black[j] = 1;
         
         maxUSID++;
         uniqueSiteIDs[mID].push_back( maxUSID );
         usid2molType.push_back( mID );
         usid2localSite.push_back( j );
         }
      }
  }

void SRSim_ns::MoleculeTypeManager::registerAllMolecules (ReactantTemplate *t)
  {
  for (int j=0 ; j < t->numMolecules() ; j++)
      registerMolecule( t->getMolecule(j) );
  }
  
int SRSim_ns::MoleculeTypeManager::numSites( int mID )
  {
  if (molTypes[mID] != NULL) return molTypes[mID]->numSites();
  else return 0;
  }

int SRSim_ns::MoleculeTypeManager::maxBondsPerMol( )
  {
  int maxBonds = 0;
  for (int i=0 ; i<numMolIDs() ; i++)
      if ( numSites(i) > maxBonds ) maxBonds = numSites(i);
  return maxBonds;
  }



  
int SRSim_ns::MoleculeTypeManager::getMolFromUSID( int usid )
  {  
  assert( usid < usid2molType.size() );
  return usid2molType[usid];
  }

int SRSim_ns::MoleculeTypeManager::getSidFromUSID( int usid )
  {
  assert( usid < usid2localSite.size() );
  return usid2localSite[usid];
  }

/**
 *   This is an ugly part:
 *     The MTM has all the sites in his own order - the GeoDef and Lammps-Simulations
 *     use the same Site-Order as the MTM...
 *    Now how to convert a Template-Site-ID to an MTM-Site-ID...  
 *                     This is the function:
 */
int SRSim_ns::MoleculeTypeManager::getSidFromTempl( TemplSite * ts)
  {
  TemplMolecule *tm  = ts->getMol();
  int            mTp = tm->getType();
  int            sTp = ts->getType();

  //assert (false);
  
  // find type-x-number of ts in the templ. Molecule  
  int  cnt   = -1;
  bool found = false;
  for (int s=0 ; s<tm->numSites() ; s++)
      if (tm->getSiteType(s) == sTp) 
         {
         cnt++;
         if (tm->getSite(s) == ts) { found=true; break; }
         }
   
  //printf ("Searching mTp=%d, sTp=%d,  cnt is now %d\n",mTp, sTp, cnt);
               
  assert (found);
  assert (cnt > -1);
  
  // take the cnt-th type-sTp site from the molecule-prototype:
  int cnt2 = -1;
  for (int s=0 ; s<numSites(mTp) ; s++)
      {
      //printf ("   site %d of type %d\n", s, getSiteType(mTp,s));
      if (getSiteType(mTp,s) == sTp)
         {
         cnt2++;
         if (cnt2 == cnt) return s;
         }
      }

  // we didn't find it:
  assert (false);
  
  
/*  if (ts->getRea() != -1 ) return ts->getRea();    // we already know what site it should be!
  
  int mTp = ts->getMol()->getType();
  int sTp = ts->getType();
  int mod = ts->getModif();
  
  assert (isMol(mTp));
  for (int s=0 ; s<numSites(mTp) ; s++)
      {
      if (getSiteType(mTp, s) != sTp ) continue;
      
      ts->setRea(s);
      return s;
      }
      
  // what??? We didn't find a fitting site? baaaaad!
  assert (false);
  return -666;*/
  }



