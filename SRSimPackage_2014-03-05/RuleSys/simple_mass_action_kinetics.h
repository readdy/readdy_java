//
// C++ Interface: simple_mass_action_kinetics
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef SIMPLE_MASS_ACTION_KINETICS_H
#define SIMPLE_MASS_ACTION_KINETICS_H

#include <SRSim/kinetics_definition.h>
#include <vector>

using namespace std;
namespace SRSim_ns {

/**
@author Gerd Gruenert
*/
class SimpleMassActionKinetics : public KineticsDefinition
  {
  public:
   SimpleMassActionKinetics();
   ~SimpleMassActionKinetics();

   double getRate (int rid);
   void   setRate (int rid, double rate);
   
  private:
   vector<double> rates;
   };

}

#endif
