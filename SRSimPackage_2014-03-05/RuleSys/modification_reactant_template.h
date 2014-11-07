/************************************************************************
              modification_reactant_template.h - Copyright gerdl

**************************************************************************/

#ifndef MODIFICATION_REACTANT_TEMPLATE_H
#define MODIFICATION_REACTANT_TEMPLATE_H
#include <SRSim/names_manager.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/site_reactant_template.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {


/** 
 *  a reactant-Template which stores before/after states of a site-modification.
 */
class ModificationReactantTemplate : public SiteReactantTemplate
   {
   public:
    ModificationReactantTemplate (ReactantTemplate *wmrt, TemplSite *s1);
    ModificationReactantTemplate (ModificationReactantTemplate *t1);
    //SiteReactantTemplate ();
    
    int                getModif  () {return oldModif;}
    RTType             getRTType () {return ModifRT;}
    ReactantTemplate*  clone     ();
    
    //bool       matchMolecule     (Molecule *against);     // keep the original from SRT
    bool       matchMolecule     (Molecule *against, int molSite);
    bool       equals            (ReactantTemplate *other);
    
   private:
    int oldModif;
    };
    
}

#endif

