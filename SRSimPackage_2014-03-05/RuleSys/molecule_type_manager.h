/************************************************************************
  			molecule_type_manager.h - Copyright gerdl

**************************************************************************/

#ifndef MOLECULE_TYPE_MANAGER_H
#define MOLECULE_TYPE_MANAGER_H
#include <string>
#include <vector>

#include <SRSim/templ_molecule.h>
#include <SRSim/reactant_template.h>

namespace SRSim_ns {

using namespace std;


/**   Initialized from the building of a reaction system, the MoleculeTypeManager knows what
 *    sites a molecule has...
 *    Until now I don't see a reason why I should save the types of possible modifications.
 *
 *    When you ask for example numSites(x), then x is the mol type-ID, not the index of the order it was created in-
 *     But not all mol-type have to be present continuously - so be careful using numMols -> 
 *     I suppose you'd rather like to use numMolIDs!
 *
 *    Also, as we've got the problem that site-Types aren't unique and local site IDs(per-molecule 
 *    site-IDs) aren't continuous. To get around this we now also have the uniqueSiteIDs...!
 *
 *    USIDs != site ids from the Names Manager    [imagine ed(ex,c,c,c)]
 *                            every c needs its own USID!
 *        sID: 0 .. numSites()
 *        getSiteType(mID,sID)  ==  site-id from NamesManager!
 *
 *    Note: Unique Site IDs are globally unique - so when you know the usid, you also know the 
 *          molecule and local site id!
 */ 
 
 
class MoleculeTypeManager
   {
   public:
    MoleculeTypeManager ();
   ~MoleculeTypeManager ();

    /// local mol-ID & local site-ID ==> siteType :
    inline int  getSiteType      (int mID, int sID)  {return molTypes[mID]->getSiteType(sID);}
    /// local mol-ID & local site-ID ==> unique site ID :
    inline int  getUniqueSiteID  (int mID, int sID)  {return uniqueSiteIDs[mID][sID];}
    inline int  isMol            (int mID)           {return (molTypes[mID] != NULL); }
    inline int  num__Mols        ()                  {return numExistingMols; }  // evil function - you'd rather like to use numMolIDs...
           int  numSites         (int mID);
           
           int  maxBondsPerMol   ();
           //int  maxAnglesPerMol  ();        // this function should rather be part of the geometry_definition!
           
    inline int  numUniqueSites   ()                  {return maxUSID+1;}
    inline int  numMolIDs        ()                  {return (molTypes.size());}
           int  getMolFromUSID   (int usid);
           int  getSidFromUSID   (int usid);
           int  getSidFromTempl  (TemplSite *tm);
                                                                                              
    void registerMolecule     (TemplMolecule *m);
    void registerAllMolecules (ReactantTemplate *t);
    
   private:   
    vector<TemplMolecule*> molTypes;
    vector<vector<int> >   uniqueSiteIDs;   //  [mID][local-Site-ID]
    vector<int>            usid2localSite;
    vector<int>            usid2molType;
    int                    numExistingMols;
    int                    maxUSID;
    };
    
    
}    



#endif 

