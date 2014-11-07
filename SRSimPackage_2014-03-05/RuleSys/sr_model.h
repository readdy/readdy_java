/************************************************************************
  			sr_model.h - Copyright gerdl

**************************************************************************/

#ifndef SR_MODEL_H
#define SR_MODEL_H

#include <string>
#include <vector>
#include <stack>
#include <map>

#include <SRSim/defs.h>
#include <SRSim/random_generator.h>
#include <SRSim/rule_set.h>
#include <SRSim/names_manager.h>
#include <SRSim/molecule_type_manager.h>
#include <SRSim/kinetics_definition.h>
#include <SRSim/geometry_definition.h>
#include <SRSim/start_state_definition.h>
#include <SRSim/observables_manager.h>


using namespace std;

namespace SRSim_ns {

// we need this forward declaration:
class GeometryDefinition;

/** Should contain the whole model... at least in some distant time... */
class SRModel
   {
   public:
     SRModel (int rndSeed);
     SRModel (int rndSeed, const char* _bnglName, const char* _mgeoName, const char* _tgeoName, bool addZeroSpecies);
    ~SRModel ();
    
    // we use pointers as there may be some virtual base classes...
    RandomGenerator      *random;
    RuleSet              *ruleset;
    NamesManager         *names;
    MoleculeTypeManager  *mtm;
    KineticsDefinition   *kinetics;
    StartStateDefinition *sstate;
    GeometryDefinition   *geo;
    ObservablesManager   *observer;
    
    const char *getBnglName ();
    const char *getMgeoName ();
    const char *getTgeoName ();

    static const char *getVersion() {return SRSIM_VERSION;}
    
    void printModelInfo ();
    
   private:
    void initialize (int rndSeed);
    string bnglName,mgeoName,tgeoName;
    };
    
}

#endif

