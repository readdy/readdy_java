/************************************************************************
  			observables_manager.h - Copyright gerdl

**************************************************************************/

#ifndef OBSERVABLES_MANAGER_H
#define OBSERVABLES_MANAGER_H
#include <string>
#include <vector>


namespace SRSim_ns {

using namespace std;

/** Manages Molecule, Site, etc... names. */
class ObservablesManager
   {
   public:
    ObservablesManager ();
   ~ObservablesManager ();
    	
    int         add    (string _name, int _tid);
    string      getName(int _oid);
    int         getTid (int _oid);
    vector<int> getObsTidVector ();
   
   private:
    vector<string> obsName;
    vector<int>    obsTempl;
    };

    
    
}    



#endif 

