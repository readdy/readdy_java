//
// C++ Interface: gillespie_1st_order
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef GILLESPIE_1ST_ORDER_H
#define GILLESPIE_1ST_ORDER_H

#include <SRSim/defs.h>
#include <SRSim/reactant_template.h>
#include <SRSim/rule_set.h>
#include <SRSim/random_generator.h>
#include <SRSim/sr_model.h>
#include <SRSim/templ_realization.h>

#include <vector>


namespace SRSim_ns {

/**
@author Gerd Gruenert
*/
    class Gillespie1stOrder
      {
      public:
              Gillespie1stOrder () {_rs=NULL;}
       //void   init           (int rnd_seed, vector<ReactantTemplate*> *__templates, vector<RuleTp*> *__rules);
       void   init           (SRModel *model);
       
#ifndef USE_TEMPL_AFFIL_MANAGER
       double timeToReaction (vector<int> &amountTempls);  // time until next 1st-order Reaction
       int    typeOfReaction (vector<int> &amountTempls);  // type of next 1st-order Reaction:
#else
       double timeToReaction (TemplAffiliationManager &affi);  // time until next 1st-order Reaction
       int    typeOfReaction (TemplAffiliationManager &affi);  // type of next 1st-order Reaction:
#endif
    
       inline double  correctionTemplAmount (int it)
          {
          vector<ReactantTemplate*> &templates = _rs->templates;
          if (templates[it]->getRTType() == ReactantTemplate::MultiMolRT) 
             return (1.0 / templates[it]->numMolecules());
          else 
             return (1.0);
          }
          
      private:
       RuleSet                     *_rs;
       KineticsDefinition          *_kinetics;
       //vector<RuleTp*>             *_rules;
       //vector<ReactantTemplate*>   *_templates;
       RandomGenerator             *_rng;
       
       
      };

}

#endif
