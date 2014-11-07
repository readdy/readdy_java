/************************************************************************
  			multi_mol_reactant_template.h - Copyright gerdl

**************************************************************************/

#ifndef MULTI_MOL_REACTANT_TEMPLATE_H
#define MULTI_MOL_REACTANT_TEMPLATE_H
#include <SRSim/names_manager.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/reactant_template.h>

#include <string>
#include <vector>
#include <stack>
#include <map>


using namespace std;

namespace SRSim_ns {


/** a ReactantTemplate for the WholeMolecule - so any part of it
 *  colliding with another molecule can react.
 *
 *  anyway: we should better rename it to MultiMolReactantTemplate !
 */
class MultiMolReactantTemplate : public ReactantTemplate
   {
   public:
    MultiMolReactantTemplate (MultiMolReactantTemplate *other) : ReactantTemplate(other) {}
    MultiMolReactantTemplate (Molecule                 *other) : ReactantTemplate(other) {}
    MultiMolReactantTemplate () {}
   
    RTType               getRTType () {return MultiMolRT;}
    ReactantTemplate*    clone     ();
    
    bool              matchMolecule (Molecule         *against);
    bool              equals        (ReactantTemplate *other);
    
    vector<int> returnMatchedMoleculeTags (Molecule *against);
    };
    
}

#endif

