/************************************************************************
  		bound_reactant_template.h - Copyright gerdl

**************************************************************************/

#ifndef BOUND_REACTANT_TEMPLATE_H
#define BOUND_REACTANT_TEMPLATE_H
#include <SRSim/names_manager.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/multi_mol_reactant_template.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {


/** 
 *    This Reactant Templates creates the possibility of
 *    identifying the exact site of a molecule to which this
 *    Template fits.
 *    (Usual Reactant Templates can only tell you, if a Whole Molecule
 *     belongs to them - even the site reactant templates!)
 */
class BoundReactantTemplate : public ReactantTemplate
   {
   public:
    BoundReactantTemplate (BoundReactantTemplate *t1);
    BoundReactantTemplate (ReactantTemplate *wmrt, TemplSite *s1);
    //BoundReactantTemplate ();
    
    RTType             getRTType () {return BoundRT;}
    ReactantTemplate*  clone     ();
    
    TemplSite* getStartSite      ()              {return startSite1;}
    void       setStartSite      (TemplSite *s1);
    bool       matchMolecule     (Molecule *against);
    bool       matchMolecule     (Molecule *against, int molSite);
    bool       equals            (ReactantTemplate *other);
    
   private:
    TemplSite *startSite1;
    TemplSite *startSite2;
    int        startSiteIdx;   // site-id of startSide1 in startMol.
    };
    
}

#endif

