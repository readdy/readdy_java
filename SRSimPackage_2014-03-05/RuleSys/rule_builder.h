//
// C++ Interface: rule_builder
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef RULE_BUILDER_H
#define RULE_BUILDER_H

#include <vector>
#include <SRSim/reactant_template.h>
#include <SRSim/rule_set.h>

namespace SRSim_ns {
using namespace std;

/**
  An abstract rule-builder.
@author Gerd Gruenert
*/
class RuleBuilder{
    public:
    protected:
     RuleTp::RuleTpType TransformTemplates ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out );

    private:     
     bool createSiteTemplatesIfNecessary ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out );    // A + B -> C  bind rule?
     bool createModiTemplatesIfNecessary ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out );    // A -> A'  modific. rule?
     bool createIntramolSiteTemplate     ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out );
     bool byModificationFromA2B (vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out, int in_id, int out_id);
     };

}

#endif
