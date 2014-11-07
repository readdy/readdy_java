/************************************************************************
  			site_reactant_template.h - Copyright gerdl

**************************************************************************/

#ifndef SITE_REACTANT_TEMPLATE_H
#define SITE_REACTANT_TEMPLATE_H
#include <SRSim/names_manager.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/reactant_template.h>
#include <SRSim/multi_mol_reactant_template.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {


/** 
 *  a reactant-Template which is only reactive at a single defined site.
 */
class SiteReactantTemplate : public ReactantTemplate
   {
   public:
    SiteReactantTemplate (ReactantTemplate *wmrt, TemplSite *s1);
    SiteReactantTemplate (SiteReactantTemplate *t1);
    //SiteReactantTemplate ();
    
    RTType             getRTType () {return SiteRT;}
    ReactantTemplate*  clone     ();
    
    TemplSite* getStartSite      ()              {return startSite;}
    void       setStartSite      (TemplSite *s1) {startSite=s1;}
    bool       matchMolecule     (Molecule *against);
    bool       equals            (ReactantTemplate *other);
    
   protected:
    TemplSite *startSite;
    int        startSiteIdx;   // site-id of startSide1 in startMol.
    };
    
}

#endif

