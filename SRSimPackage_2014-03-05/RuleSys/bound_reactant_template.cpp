//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>

#include "bound_reactant_template.h"
#include "sr_error.h"
#include "templ_site.h"
#include "templ_molecule.h"


using namespace SRSim_ns;


/** Build a BoundReactantTemplate from a WholeMoleculeReactantTemplate...
 *   so it's a conversion constructor.
 *   a Problem: the SiteTempl points to an old 
 *   site from the WholeMoleculeReactantTemplate. So we'll have to find the
 *   respective Site in our new Template...
 */
BoundReactantTemplate::BoundReactantTemplate( ReactantTemplate* molT, TemplSite* s1 ) :
  ReactantTemplate (molT)
  {
  // find respective Site in new Template:
  int mol_id=SRSIM_UNDEFINED, site_id=SRSIM_UNDEFINED;
  for (int i=0 ; i<mols.size() && mol_id==SRSIM_UNDEFINED ; i++)
      for (int j=0 ; j<mols[i]->numSites() && mol_id==SRSIM_UNDEFINED ; j++)
          if (molT->getMolecule(i)->getSite(j) == s1) {mol_id=i; site_id=j;}
          
  assert (mol_id  < mols.size());
  assert (site_id < mols[mol_id]->numSites());
  assert (mol_id  != SRSIM_UNDEFINED);
  assert (site_id != SRSIM_UNDEFINED);
  
  // ok, we found it -> now set as our start site.
  setStartSite (mols[mol_id]->getSite(site_id));
  }

/*TwoSiteReactantTemplate::TwoSiteReactantTemplate () :
  startSite(NULL)
  {
  }*/
  
  
/**
 *   As this is a BoundReactantTemplate and this old function can 
 *   only identify, if a given molecule in whole belongs to this 
 *   Template, you shouldn't use this function any more.
 *   Rather use matchMolecule(Molecule*, int molSite);
 */
bool BoundReactantTemplate::matchMolecule (Molecule *against)
  {
  assert( false );
  
  assert (startSite1 != NULL);
  TemplMolecule *templOne = startSite1->getMol();
  
  return( matchSingleTM(against,templOne,/*exact*/false) );
  }
  
/**
 * we're not visiting both start sites
 * but only the one given by startSite1.
 *   So the possibility of bond breaking doesn't have to be calculated twice!
 *
 *  @molSite: the mol-Site we're testing for - so later we can tell 
 *            wich site exactly is hit by the template.
 *            This is important e.g. for breaking bonds!
 */
bool BoundReactantTemplate::matchMolecule (Molecule *against, int molSite)
  {
  assert (startSite1 != NULL);
  TemplMolecule *templOne = startSite1->getMol();
  //printf ("matching of BoundReactantTemplate:\n");
  return( matchSingleTM(against,templOne, /*exact*/false, startSiteIdx,molSite) );
  }



bool BoundReactantTemplate::equals (ReactantTemplate *other)
  {
  assert (startSite1 != NULL);
  assert (startSite2 != NULL);
  
  if (other->getRTType() != getRTType()) return false;
  BoundReactantTemplate *otherSRT = dynamic_cast<BoundReactantTemplate*>(other);
  if (otherSRT == NULL) return false;

  // maybe the startsite-types don't fit!  
  if (startSite1->getType() == otherSRT->startSite1->getType() &&
      startSite2->getType() == otherSRT->startSite2->getType()    ) 
     {
     // let's try it from the first start site:
     bool res = matchSingleTM(otherSRT->startSite1->getMol(),startSite1->getMol(),/*exact*/true);
     if (res && matchSingleTM(otherSRT->startSite2->getMol(),startSite2->getMol(),/*exact*/true) ) 
        return true;
     }
  
  // let's try from the other start Site:  (2.case: sites are crossed over)
  //  CAUTION: DON't put an else here! Both cases are possible!
  if (startSite1->getType() == otherSRT->startSite2->getType() &&
      startSite2->getType() == otherSRT->startSite1->getType()    ) 
     {
     bool res = matchSingleTM(otherSRT->startSite1->getMol(),startSite2->getMol(),/*exact*/true);
     if (res && matchSingleTM(otherSRT->startSite2->getMol(),startSite1->getMol(),/*exact*/true) )
        return true;
     }

  return false;
  }
  

void BoundReactantTemplate::setStartSite( TemplSite * s1 )
  {
  assert (s1->getOther() != NULL);
  
  startSite1 = s1;
  startSite2 = s1->getOther();
  
  // now go through mols to find the startSiteIdx
  startSiteIdx = -1;
  TemplMolecule *tm = s1->getMol();
  for (int i=0 ; i<tm->numSites() ; i++)
      if (tm->getSite(i) == s1) { startSiteIdx=i; break; }
      
  assert (startSiteIdx != -1);  // killall if we don't find it ;)
  
  //printf ("StartSite idx %d, s1-tp:%d, s2-tp:%d\n", startSiteIdx, startSite1->getType(), startSite2->getType());
  //assert (false);
  }



ReactantTemplate * SRSim_ns::BoundReactantTemplate::clone( )
  {
  BoundReactantTemplate *brt = new BoundReactantTemplate(this);
  return brt;
  }
  
/**
 * a copy constructor:
 */
BoundReactantTemplate::BoundReactantTemplate( BoundReactantTemplate *t1 ) :
  ReactantTemplate (t1)
  {
  // find respective Site in new Template:
  int mol_id=SRSIM_UNDEFINED, site_id=SRSIM_UNDEFINED;
  for (int i=0 ; i<mols.size() && mol_id==SRSIM_UNDEFINED ; i++)
      for (int j=0 ; j<mols[i]->numSites() && mol_id==SRSIM_UNDEFINED ; j++)
          if (t1->getMolecule(i)->getSite(j) == t1->startSite1) {mol_id=i; site_id=j;}
          
  assert (mol_id  < mols.size());
  assert (site_id < mols[mol_id]->numSites());
  assert (mol_id  != SRSIM_UNDEFINED);
  assert (site_id != SRSIM_UNDEFINED);
  
  // ok, we found it -> now set as our start site.
  setStartSite (mols[mol_id]->getSite(site_id));
  }


