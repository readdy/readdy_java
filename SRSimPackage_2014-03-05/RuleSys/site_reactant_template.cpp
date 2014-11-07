
//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>

#include "site_reactant_template.h"
#include "sr_error.h"
#include "templ_site.h"
#include "templ_molecule.h"


using namespace SRSim_ns;

/** Build a SiteReactantTemplate from a WholeMoleculeReactantTemplate...
 *   so it's a conversion constructor.
 *   a Problem: the SiteTempl points to an old 
 *   site from the WholeMoleculeReactantTemplate. So we'll have to find the
 *   respective Site in our new Template...
 */
SiteReactantTemplate::SiteReactantTemplate( ReactantTemplate* molT, TemplSite* s1 ) :
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
  startSiteIdx = site_id;
  }
  

/*SiteReactantTemplate::SiteReactantTemplate () :
  startSite(NULL)
  {
  }*/
  
bool SiteReactantTemplate::matchMolecule (Molecule *against)
  {
  assert (startSite != NULL);
  TemplMolecule *templOne = startSite->getMol();
  
  return( matchSingleTM(against,templOne) );
  }


bool SiteReactantTemplate::equals (ReactantTemplate *other)
  {
  assert (startSite != NULL);
  TemplMolecule *myOne = startSite->getMol();
  
  if (other->getRTType() != getRTType()) return false;
  SiteReactantTemplate *otherSRT = dynamic_cast<SiteReactantTemplate*>(other);
  if (otherSRT == NULL) return false;
  
  if (startSite->getType() != otherSRT->startSite->getType()) return false;
  
  TemplMolecule *otherOne = otherSRT->startSite->getMol();
  return( matchSingleTM(otherOne,myOne,true) );        // exact=true
  }


ReactantTemplate * SRSim_ns::SiteReactantTemplate::clone( )
  {
  /**///int tp_old = startSite->getType();
  
  SiteReactantTemplate *srt = new SiteReactantTemplate(this);
  
  /**///int tp_new = srt->startSite->getType();
  /**///assert (tp_old == tp_new);
  
  return srt;
  }

/**
 * a copy constructor:
 */
SiteReactantTemplate::SiteReactantTemplate( SiteReactantTemplate *t1 ) :
  ReactantTemplate (t1)
  {
  // find respective Site in new Template:
  int mol_id=SRSIM_UNDEFINED, site_id=SRSIM_UNDEFINED;
  for (int i=0 ; i<mols.size() && mol_id==SRSIM_UNDEFINED ; i++)
      for (int j=0 ; j<mols[i]->numSites() && mol_id==SRSIM_UNDEFINED ; j++)
          if (t1->getMolecule(i)->getSite(j) == t1->startSite) {mol_id=i; site_id=j;}
          
  assert (mol_id  < mols.size());
  assert (site_id < mols[mol_id]->numSites());
  assert (mol_id  != SRSIM_UNDEFINED);
  assert (site_id != SRSIM_UNDEFINED);
  
  // ok, we found it -> now set as our start site.
  startSite = mols[mol_id]->getSite(site_id);
  startSiteIdx = site_id;
  }



