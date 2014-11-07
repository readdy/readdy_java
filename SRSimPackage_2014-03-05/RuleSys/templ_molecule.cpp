#include <assert.h>

#include "templ_molecule.h"

using namespace SRSim_ns;

/// Constants:
int TemplMolecule::uniqueCounter = 0;

/// KONSTRUKTOR:
TemplMolecule::TemplMolecule  (int _tp, int _pt) : type(_tp), pattern(_pt), myCounter(uniqueCounter)
  { 
  uniqueCounter++; /*printf(" templMol %d \n", myCounter);*/
  }

/// DESTRUKTOR:
TemplMolecule::~TemplMolecule ()
  {
  for (int i=0 ; i<sites.size() ; i++) 
      delete sites[i];
  }
               
TemplSite* TemplMolecule::addSite (int _tp, int _md, int _pt)
  {
  TemplSite *ts = new TemplSite (_tp,_md,_pt);
  ts->mol = this;
  sites.push_back (ts);
  
  return ts;
  }
  
TemplSite* TemplMolecule::getSite (int idx) { return sites[idx]; }





// implementations of abstract molecule-methods:
int TemplMolecule::getType  () {return type;}

int TemplMolecule::numSites () {return sites.size(); }

int TemplMolecule::getSiteType             (int idx) {return sites[idx]->type; }

Molecule* TemplMolecule::getMoleculeAtSite (int idx) 
  {
  if (sites[idx]->isConnected())
     return (Molecule*)(sites[idx]->otherEnd->mol);
  else return NULL;
  }
int TemplMolecule::getModificationAtSite   (int idx) {return sites[idx]->modif; }

int TemplMolecule::getOtherSiteIDAtSite    (int idx) 
  {
    TemplSite     *mySite = getSite(idx);
    TemplMolecule *other  = sites[idx]->otherEnd->mol;
    if (other == NULL) return -1;  // unconnected site
    
    int nOtherSites = other->numSites();
    for (int i=0 ; i<nOtherSites ; i++)
        if (other->getSite(i)->getOther() == mySite)
            return i;
    
    // we shouldn't get here - otherwise the site (idx) is not connected!
    assert( false );
  }

int TemplMolecule::getUniqueID             ()        {return myCounter;}

