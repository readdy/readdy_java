//
// C++ Implementation: simple_mass_action_kinetics
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

#include "simple_mass_action_kinetics.h"

namespace SRSim_ns {

SimpleMassActionKinetics::SimpleMassActionKinetics()
 : KineticsDefinition()
  {
  }


SimpleMassActionKinetics::~SimpleMassActionKinetics()
  {
  }



double SimpleMassActionKinetics::getRate( int rid )
  {
  assert ( rid < rates.size() );
  assert ( rates[rid] >= 0    );
  return rates[rid];
  }



void SimpleMassActionKinetics::setRate( int rid, double rate )
  {
  //printf ("Rate-Size = %d\n",rates.size());
  if (rid >= rates.size()) rates.resize(rid+1, -1.0);  // enlarge list...
  //printf ("Rate-Size = %d\n",rates.size());
  rates[rid] = rate;
  }


}
