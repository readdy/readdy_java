/************************************************************************
  			templmolecule.h - Copyright gerdl

**************************************************************************/

#ifndef TEMPL_MOLECULE_H
#define TEMPL_MOLECULE_H
#include <string>
#include <vector>
#include <SRSim/molecule.h>
#include <SRSim/templ_site.h>


namespace SRSim_ns {

using namespace std;

class TemplSite;

/**   This is a node of the Template-Graph-Definition...
  *
  */
class TemplMolecule : public Molecule
   {
   friend class ReactantTemplate;
   public:
              TemplMolecule (int _tp, int _pt);
     virtual ~TemplMolecule ();
    
    TemplSite*         addSite      (int _tp, int _md, int _pt);
    TemplSite*         getSite      (int idx);
    
    void setRealization (int x) {realization=x;}
    int  getRealization ()      {return realization;}
    int  getPattern     ()      {return pattern;}
    
   // implementations of abstract molecule-methods:
    int        getType ();
    int        numSites ();
    int        getSiteType           (int idx);
    Molecule*  getMoleculeAtSite     (int idx);
    int        getModificationAtSite (int idx);
    int        getOtherSiteIDAtSite  (int idx);
    int        getUniqueID           ()       ;
    
   private:
    static int         uniqueCounter;
           int         myCounter;
    vector<TemplSite*> sites;
    int type;
    int pattern;                     // not yet used... would allow to define ignore-type-molecules...
    
    int realization;            // saves the id of a possible incarnation of this template Molecule...
                                // very temporary values may reside here...
    };
}    
#endif //TEMPL_MOLECULE_H

