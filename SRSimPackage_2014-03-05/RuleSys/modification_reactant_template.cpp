
//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>

#include "modification_reactant_template.h"
#include "sr_error.h"
#include "templ_site.h"
#include "templ_molecule.h"


using namespace SRSim_ns;

/** Build a ModificationReactantTemplate from a WholeMoleculeReactantTemplate...
 *   so it's a conversion constructor.
 *   a Problem: s1 points to an old 
 *   site from the WholeMoleculeReactantTemplate. So we'll have to find the
 *   respective Site in our new Template...
 */
ModificationReactantTemplate::ModificationReactantTemplate( ReactantTemplate* molT, TemplSite* s1) :
  SiteReactantTemplate (molT, s1)
  {
  oldModif = s1->getModif();
  }
  

/**
 * a copy constructor:
 */
ModificationReactantTemplate::ModificationReactantTemplate( ModificationReactantTemplate *t1 ) :
  SiteReactantTemplate (t1)
  {
  oldModif = t1->oldModif;
  }

  
/*SiteReactantTemplate::SiteReactantTemplate () :
  startSite(NULL)
  {
  }*/
  

bool ModificationReactantTemplate::equals (ReactantTemplate *other)
  {
  if (other->getRTType() != getRTType()) return false;
  ModificationReactantTemplate *otherMRT = dynamic_cast<ModificationReactantTemplate*>(other);
  if (otherMRT == NULL) return false;
  if (otherMRT->oldModif != oldModif) return false;
  
  return SiteReactantTemplate::equals(other);
  }


ReactantTemplate * SRSim_ns::ModificationReactantTemplate::clone( )
  {
  ModificationReactantTemplate *mrt = new ModificationReactantTemplate(this);
  return mrt;
  }

  
bool ModificationReactantTemplate::matchMolecule (Molecule *against, int molSite)
  {
  assert (startSite != NULL);
  TemplMolecule *templOne = startSite->getMol();
  //printf ("matching of BoundReactantTemplate:\n");
  return( matchSingleTM(against,templOne, /*exact*/false, startSiteIdx,molSite) );
  }


