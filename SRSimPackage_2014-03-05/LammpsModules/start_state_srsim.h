//
// C++ Interface: start_state_srsim
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef START_STATE_SRSIM_H
#define START_STATE_SRSIM_H

#include "pointers.h"
#include "atom_vec_srsim.h"

namespace LAMMPS_NS {

/**
@author Gerd Gruenert
*/
class StartStateSRSim : protected Pointers
  {
  public:
    StartStateSRSim(class LAMMPS *lmp);
   ~StartStateSRSim();
  
    void command(int, char **);

    
   private:
    AtomVecSRSim *avec;
   
    // simulation dependant parameters:
    //double      deviAngle;
    //double      deviBondLength;
    //double      forceAngle;           // angular forces...
    //double      forceBondLength;      // spring forces keeping bound atoms at a certain distance
    //double      forceRepulsion;       // when melecules get too close to each other!
    
    void setAmountsAndCoeffs ( );
    void createAtoms         ( );
    //void addTemplate2Sim     ( SRSim_ns::ReactantTemplate * rt, double x, double y, double z );
    };

}

#endif
