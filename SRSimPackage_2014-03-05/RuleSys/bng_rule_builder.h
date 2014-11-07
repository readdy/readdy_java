/************************************************************************
  			bng_rule_builder.h - Copyright gerdl

**************************************************************************/

#ifndef BNG_RULE_BUILDER_H
#define BNG_RULE_BUILDER_H
#include <SRSim/names_manager.h>
#include <SRSim/reactant_template.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/rule_set.h>
#include <SRSim/rule_builder.h>
#include <SRSim/sr_model.h>

#include <string>
#include <vector>


using namespace std;

namespace SRSim_ns {


/** Builds rules from BNGL (BioNetGen) -Files*/
class BNGRuleBuilder : public RuleBuilder
   {
   public:
     BNGRuleBuilder (SRModel *_into);
    ~BNGRuleBuilder ();
    
    void              readFile         (string fname);
    ReactantTemplate* parseBNGTemplate (string t);
    
    
   private:
    SRModel        *into;
    NamesManager   *names;
    vector<double>  vars;
    
    vector<string> partition               (string in, string separators);
    bool           isNeatNumber            (char *in);
    void           parseVarsSection        (ifstream *f);
    void           parseStartStateSection  (ifstream *f);
    void           parseObservableSection  (ifstream *f);
    };
    
}

#endif  // BNG_RULE_BUILDER_H

