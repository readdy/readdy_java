//
// C++ Interface: kinetics_definition
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef KINETICS_DEFINITION_H
#define KINETICS_DEFINITION_H

#include <SRSim/rule_set.h>

namespace SRSim_ns {

/**
@author Gerd Gruenert
*/
class KineticsDefinition
  {
  public:
   virtual double getRate (int rid) = 0;
   virtual void   setRate (int rid, double rate) = 0;
  
   void           scaleRates (RuleSet *rs, double pfBind, double pfBreak, double pfExchange, double pfModif1, double pfModif2);
   
   };

}

#endif
