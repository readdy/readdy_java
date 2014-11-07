

//#define NDEBUG
#include "defs.h"
#include <assert.h>

#include "observables_manager.h"


using namespace SRSim_ns;


ObservablesManager::ObservablesManager ()
  {
  }

ObservablesManager::~ObservablesManager ()
  {
  }
  
int ObservablesManager::add (string _name, int _tid)
  {
  obsName .push_back(_name);
  obsTempl.push_back(_tid);
  }
  
  
string ObservablesManager::getName(int _oid)
  {
  assert(_oid < obsName.size());
  return obsName[_oid];
  }
  
  
int ObservablesManager::getTid (int _oid)
  {
  assert(_oid < obsTempl.size());
  return obsTempl[_oid];
  }

vector<int> ObservablesManager::getObsTidVector ()
  {
  return obsTempl;
  }


