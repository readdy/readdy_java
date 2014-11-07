/************************************************************************
  			rule_set.h - Copyright gerdl

**************************************************************************/

#ifndef RULE_SET_H
#define RULE_SET_H

#include <SRSim/reactant_template.h>
#include <SRSim/molecule_type_manager.h>
#include <SRSim/templ_realization.h>
//#include <SRSim/kinetics_definition.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {

/** a Rule. 
 *  rather a private class wich should only be used for the internal storage...
 */
class RuleTp 
   { public:
   enum RuleTpType {
        BindR,
        BreakR,
        ExchangeR,
        ModifyR,
        BindIntramolR
        };

   RuleTp () : type(ExchangeR)
      {
      //propensity = 0.01;   // any value's a good value for now...
      }
   
   string     toString     ();

   vector<int> in;
   vector<int> out;
   RuleTpType  type;
   
   static class RuleSet *rset;
   /*bool        bindRule;
   bool        breakRule;
   bool        exchangeRule;*/
   
   //double      propensity;
   //bool        reversible;        // implement as two separate rules, please!
   };
 

/** 
 *  Stores the templates and a set of rules built above them.
 *  Also it provides the system to chose a subset of aplicable rules from 
 *  two template vectors for 2nd-order reactions and gillespie-like computations
 *  for zero- and first-order reactions.
 */ 
class RuleSet
   {
   //class Gillespie1stOrder;
   
   public:
           RuleSet ();
          ~RuleSet ();
    
    string            toString        ();  // plots all the rules.
    
    //vector<RuleTp*> getRules     ()  {return rules;}
    int               numTemplates    ()              { return templates.size(); }
    int               numRules        ()              { return rules.size(); }
    // int               numMoleculesMax ()              { return maxTemplSize; }
    int               maxReactiveTemplateSize ()      { return maxReactiveTemplSize; }
    
    vector<int>       addNewRule   (vector<ReactantTemplate*> in, vector<ReactantTemplate*> out, RuleTp::RuleTpType rtype, bool reversible);
    ReactantTemplate* getRT        (int templID)   { return templates[templID]; }
    RuleTp*           getRule      (int ruleID)    { return rules[ruleID]; }
    vector<RuleTp*>  &getRules     ()              { return rules;         }
    
   // 2nd-Order Rules
#ifndef USE_TEMPL_AFFIL_MANAGER
    void              fillFittingTemplates (Molecule * m, vector<int> &wholeMolTempls, vector<vector<int> > &specificTempls, vector<int> &amountTempls);
    int               fillFittingRules     (vector<int> &wmTemplsA, vector<int> &wmTemplsB, int* reas);
#else
    void              fillFittingTemplates (Molecule *m, int mid, TemplAffiliationManager &affi);
    int               fillFittingRules     (TemplAffiliationManager &affi, int molA, int molB, int* reas );
#endif

    // register template and/or retrieve id    
    int  getTemplateID    (ReactantTemplate *rt);   // registers if not present
    int  searchTemplateID (ReactantTemplate *rt);   // returns -1 if not present, only compares pointers!
    
    
    vector<ReactantTemplate*>     templates;
    vector<RuleTp*>               rules;
   private:
    vector<vector<vector<int> > > TRMapping2nd;       // Template-Reaction-Mapping for 2nd order reactions
    //int                           maxTemplSize;       // number of molecules of the current largest template
    int                           maxReactiveTemplSize;       // number of molecules of the current largest template, that can participate in a reaction
    //int ***TRMapping2nd;    
    //int ** TRMappingSize;
    
    //int  getTemplateID       (ReactantTemplate *rt);   // returns -1 (UNDEFINED) if not yet known...
    //int  registerTemplate    (ReactantTemplate *rt);
    
    void buildTRMapping ();
          
    };
    
    
}

#endif

