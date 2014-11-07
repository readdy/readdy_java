//
// C++ Interface: uniform_distribution_start_state
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef UNIFORM_DISTRIBUTION_START_STATE_H
#define UNIFORM_DISTRIBUTION_START_STATE_H

#include <SRSim/start_state_definition.h>
#include <SRSim/random_generator.h>
#include <SRSim/reactant_template.h>

#include <vector>

using namespace std;
namespace SRSim_ns {

/**
@author Gerd Gruenert
*/
class UniformDistributionStartState : public StartStateDefinition
   {
   public:
     UniformDistributionStartState(RuleSet *_rs, RandomGenerator *_rg);
    ~UniformDistributionStartState();

    // StartStateDefinition parts:
    Element getNextItem();
    int     numItems2Create();
    void    reset();
    
    // methods to setup this class:
    void    addNumber (ReactantTemplate *t, int number);
    
   private:
    vector<int>      initNumbers;
    int              sumOfParts;
    int              idType, idIndiv;
    RandomGenerator *rg;
    };

}

#endif
