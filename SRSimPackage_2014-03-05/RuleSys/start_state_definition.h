//
// C++ Interface: start_state_definition
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef START_STATE_DEFINITION_H
#define START_STATE_DEFINITION_H

#include <SRSim/reactant_template.h>
#include <SRSim/rule_set.h>

namespace SRSim_ns {

/**
@author Gerd Gruenert
*/

class StartStateDefinition
  {
  public:
            StartStateDefinition (RuleSet *_rs);
   virtual ~StartStateDefinition ();
  
   class Element {
    public:
     double            x,y,z;
     ReactantTemplate *rt;
     };
  
   virtual int     numItems2Create () = 0;
   virtual Element getNextItem     () = 0;
   virtual void    reset           () = 0;
   
   int               numTemplates();
   ReactantTemplate* getRT       (int sid);
   
  protected:
   void         addTemplate  (ReactantTemplate *t);
   
   vector<int>  templs;  // these template-ids are used to populate the system
   RuleSet     *rset;    // stores the reactant-templates.
   };

}

#endif
