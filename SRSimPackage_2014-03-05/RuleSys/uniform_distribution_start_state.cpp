//
// C++ Implementation: uniform_distribution_start_state
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

#include "uniform_distribution_start_state.h"
#include "reactant_template.h"

namespace SRSim_ns {

UniformDistributionStartState::UniformDistributionStartState(RuleSet *_rs, RandomGenerator *_rg) :
  rg(_rg), StartStateDefinition(_rs)
  {
  sumOfParts = 0;
  reset();
  }


UniformDistributionStartState::~UniformDistributionStartState()
  {
  initNumbers.clear();
  }


StartStateDefinition::Element UniformDistributionStartState::getNextItem()
  {
  Element e;
  
  e.x    = rg->uniform();
  e.y    = rg->uniform();
  e.z    = rg->uniform();
  
  
  idIndiv++;
  while (idIndiv > initNumbers[idType])     // while --> we can hop over zero-species...
    {
    idIndiv=1;
    idType++;
    assert (idType < templs.size());
    }
  
  e.rt = getRT( idType );
  return e;
  }

int UniformDistributionStartState::numItems2Create()
  {
  return sumOfParts;
  }

void UniformDistributionStartState::reset()
  {
  idType = 0; idIndiv = 0;
  }

void UniformDistributionStartState::addNumber(ReactantTemplate *t, int number)
  {
  addTemplate (t);
  initNumbers.push_back(number);
  sumOfParts += number;
  }



}


