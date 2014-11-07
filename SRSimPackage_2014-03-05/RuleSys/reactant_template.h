/************************************************************************
  			reactant_template.h - Copyright gerdl

**************************************************************************/

#ifndef REACTANT_TEMPLATE_H
#define REACTANT_TEMPLATE_H

#include <SRSim/defs.h>
#include <assert.h>

#include <SRSim/names_manager.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {


         

/** A Molecule-Graph Pattern used in Rule-Definitions. 
 *
 *   Take a little care about the field myUse: e.g. if myUse isn't set to enable reactingRT, then
 *   RuleSet::fillFittingTemplates doesn't find this one. So use addUse(reactingRT) like it's done
 *     in RuleSet::addNewRule!
 */
class ReactantTemplate
   {
   static int inst_cnt;
   public:
    enum RTType {
       MultiMolRT,
       SiteRT,
       BoundRT,
       ModifRT
       };
         
    enum RTUse {
       senselessRT  = 0,         //         the completely unusable RT :) :) :)
       reactingRT   = 1<<0,      // 1       so we can bitwise-or them...
       creatableRT  = 1<<1,      // 2
       observableRT = 1<<2       // 4       do we really need this one?
       };
   
   public:
    ReactantTemplate ();
    ReactantTemplate (ReactantTemplate* t1);  // Copy Constructor.
    ReactantTemplate (Molecule*          m);  // Copy from arbitrary molecule
   ~ReactantTemplate ();
    
    void                addMolecule     (TemplMolecule *m);
    TemplMolecule*      getMolecule     (int idx);
    int                 numMolecules    ();
    vector<TemplSite*>  findMissingBond (vector<ReactantTemplate*> in);      
    int                 countBonds      ();
    class TemplateGeo * getGeo          ();
    void                setGeo          (class TemplateGeo *_tgeo);
    string              getName         ();
    void                setName         (string _name);
    bool                isUsableAs      (RTUse _use);
    void                addUse          (RTUse _use);
    
    void writeTemplateToDotFile (NamesManager *names, string fname);
    
    virtual bool               matchMolecule   (Molecule *against) = 0;
    virtual bool               matchMolecule   (Molecule *against, int molSite) {assert(false); return false;}
    virtual bool               equals          (ReactantTemplate *other)   = 0;
    virtual RTType             getRTType       ()                          = 0;
    virtual ReactantTemplate*  clone           ()                          = 0;
    
    // sanity checks...
    bool checkConnectivity       ();
    bool checkOneSitePerMolecule ();
    
   protected:
    vector<TemplMolecule*> mols;
    stack <TemplMolecule*> markingHistory;       // used in the template-matching process...
    map<int,int>           markedMolecules;      // saves which real-molecules are already marked.
    class TemplateGeo     *geo;
    string                 myName;
    RTUse                  myUse;
    
    
    bool matchSingleTM      (Molecule *against, TemplMolecule *start, bool exact=false, int i_startSite=-1, int j_startSite=-1);
    void unmarkTemplate     (TemplMolecule *until=NULL);
    
    // recursive function
    bool recTryMatching      (Molecule      *m, TemplMolecule *t, int recLayer, int i_startSite=-1, int j_startSite=-1);
    bool recTryMatchingExact (TemplMolecule *m, TemplMolecule *t, int recLayer);   // used by == operator...


    // works like a counter... counting through all possibilities of assignments from template->molecule...    
    class AssignmentConstructor
      { public:
       AssignmentConstructor (int ts, int ms);
      ~AssignmentConstructor ();
      
      int nTsites, nMsites;
      int* tAssignments;
      bool* mAssignments;
      vector<int >* tPossibilities;
      vector<bool>* tRecursivity;
            
      bool  incrementAssignment ();
      int   numAssignments      ();
      void  addPoss             (int ts, int ms, bool rc=false) 
                                                 {tPossibilities[ts].push_back(ms); 
                                                  tRecursivity  [ts].push_back(rc);  }  // possibilities, opportunities... :)
      int   numPoss             (int ts)         {return tPossibilities[ts].size(); }
      int   getPoss             (int ts)         {return tPossibilities[ts][tAssignments[ts]];    }
      bool  getRecursivity      (int ts)         {return tRecursivity  [ts][tAssignments[ts]];}
      };
        
    };
    
}

#endif

