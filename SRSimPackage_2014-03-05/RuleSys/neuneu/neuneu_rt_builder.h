 
/************************************************************************
  			neuneu_rt_builder.h - Copyright gerdl

**************************************************************************/

#ifndef NEUNEU_RT_BUILDER_H
  #define NEUNEU_RT_BUILDER_H

#include <SRSim/names_manager.h>
#include <SRSim/reactant_template.h>
#include <SRSim/templ_molecule.h>
#include <SRSim/templ_site.h>
#include <SRSim/sr_model.h>

#include <string>
#include <vector>

namespace SRSim_ns {

/**
  NeuneuRtBuilder: to read NeuNeu - Ascii images of connection diagrams!
 
an exemplary map file migth be looking like this:
#######
*:ed(c) ex~a
o:rep(c) ex~a
X:start(c) ex~a
a:directedA(c) ex~a
b:directedB(c) ex~a
---
                                                                          
                                                                          
                                                                          
                                                                          
    *************************                                             
                            *                                             
                            *                                             
                                                                          
                                                                          
                                                                          
#######
 
@author Gerd Gruenert
*/
class NeuneuRtBuilder
  {
  // data structure for the different symbols in the map
  private:
   struct neu_tp{
          char   symbol;
          string molTp;
          string connectorSite;
          vector<string> basicExcitationSite;
          vector<string> basicExcitationMod;
          };
  
  public:
   typedef vector< vector<int> > map_tp;
  
   NeuneuRtBuilder(SRModel &_srm, string _input);
   //~NeuneuRtBuilder();
   
   ReactantTemplate *build ();
  
  private:
   SRModel        &srm;
   NamesManager   *names;
   string          input;
   vector<neu_tp>  neu_types;
   
   map_tp                 map;         // the molecule types in the map-raster
   map_tp                 molMap;      // the molecule-indices in the molList in the map-raster
   vector<TemplMolecule*> molList;     // the individual molecules in a list.
   
   vector<vector<int> >   connectorTunnels;  // connection with index i between (cT[i][0],cT[i][1]) and (cT[i][2],cT[i][3])
   
   void           readAsciiMap (string input);  // reads & stores in this->map
   TemplMolecule* buildFreeMol (int mType);
   void           connectInMap (int i,int j, int x,int y);
   
  };


}


#endif


