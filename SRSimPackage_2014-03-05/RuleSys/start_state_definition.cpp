//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "start_state_definition.h"

namespace SRSim_ns {

/**
@author Gerd Gruenert
*/
StartStateDefinition::StartStateDefinition (RuleSet *_rs) 
  {
  rset = _rs;
  }
  
  
StartStateDefinition::~StartStateDefinition ()
  {
  templs.clear();
  }

int SRSim_ns::StartStateDefinition::numTemplates( )
  {
  return templs.size();
  }

ReactantTemplate * SRSim_ns::StartStateDefinition::getRT( int sid )
  {
  return rset->templates[ templs[sid] ];
  }

void SRSim_ns::StartStateDefinition::addTemplate( ReactantTemplate * t )
  {
  int id = rset->getTemplateID( t );
  templs.push_back(id);
  }


}

