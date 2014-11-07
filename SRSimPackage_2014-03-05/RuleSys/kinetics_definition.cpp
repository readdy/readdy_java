//
// C++ Implementation: kinetics_definition
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "defs.h"
#include <assert.h>

#include "kinetics_definition.h"


namespace SRSim_ns {



void KineticsDefinition::scaleRates( RuleSet * rs, double pfBind, double pfBreak, double pfExchange, double pfModif1, double pfModif2)
  {
  for (int i=0 ; i<rs->numRules() ; i++)
      {
      if      (rs->getRule(i)->type == RuleTp::BindR        ) setRate( i, pfBind     * getRate(i) );
      else if (rs->getRule(i)->type == RuleTp::BindIntramolR) setRate( i, pfBind     * getRate(i) );
      else if (rs->getRule(i)->type == RuleTp::BreakR       ) setRate( i, pfBreak    * getRate(i) );
      else if (rs->getRule(i)->type == RuleTp::ExchangeR    ) setRate( i, pfExchange * getRate(i) );
      else if (rs->getRule(i)->type == RuleTp::ModifyR      ) 
         {
         if      (rs->getRule(i)->in.size() == 1) setRate( i, pfModif1 * getRate(i) );
         else if (rs->getRule(i)->in.size() == 2) setRate( i, pfModif2 * getRate(i) );
         else assert(false);
         }
      else assert(false);
      }
  }

}

