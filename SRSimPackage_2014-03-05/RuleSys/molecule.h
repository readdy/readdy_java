/************************************************************************
  			molecule.h - Copyright gerdl

**************************************************************************/

#ifndef MOLECULE_H
#define MOLECULE_H
#include <string>

#include "names_manager.h"

// int SRSIM_UNDEFINED = -1;

using namespace std;
namespace SRSim_ns {

// forward decl:
//class 
    
/**  This is the definition for an abstract molecule...
  *  It could be either a Template from a rule definition or 
  *  real one from a MD-simulation...
  */
class Molecule 
   {
   public:
    virtual int        getType     () = 0;
    virtual int        numSites    () = 0;
    virtual int        getSiteType           (int idx) = 0;
    virtual Molecule*  getMoleculeAtSite     (int idx) = 0;
    virtual int        getOtherSiteIDAtSite  (int idx) = 0;  // returns -1 if not connected.
    virtual int        getModificationAtSite (int idx) = 0;  // -1 if not modified at all
    virtual int        getUniqueID           ()        = 0;
//    virtual str ing     getExtraInfo () = 0;

    //void   writeToDotFile (string fname);
    string generateBngl   (NamesManager *n, vector<bool> &usedUids);
    
    void   buildMolList     (vector<Molecule*> &allMols, vector<bool> &usedUids); 
    bool   checkIfConnected (Molecule *other, vector<bool> *usedUids = NULL);
    };
}
#endif //MOLECULE_H

