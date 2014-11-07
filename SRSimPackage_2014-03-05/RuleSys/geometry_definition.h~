//
// C++ Interface: geometry_definition
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef GEOMETRY_DEFINITION_H
#define GEOMETRY_DEFINITION_H

#include <vector>
#include <string>

#include <SRSim/reactant_template.h>
#include <SRSim/names_manager.h>
#include <SRSim/molecule_type_manager.h>
#include <SRSim/rule_set.h>
#include <SRSim/sr_model.h>

using namespace std;
namespace SRSim_ns {

/**
@author Gerd Gruenert
*/



// How to handle various properties about the reactor (GeoProperties):
#define GPT_Site_Theta       0
#define GPT_Site_Phi         1
#define GPT_Site_Dist        2
#define GPT_Site_Dihedral    3
#define GPT_Mol_Mass         4
#define GPT_Mol_Rad          5
// multi-level options:
#define GPT_Devi_Dist        6
#define GPT_Devi_Angle       7
#define GPT_Diffusion        8
#define GPT_Refractory       9
// purely general options:
#define GPT_Force_Repulsion  10
#define GPT_Force_Bond       11
#define GPT_Force_Angle      12
#define GPT_Force_Dihedral   13
#define GPT_Temperature      14
#define GPT_Option_Dihedrals 15
#define GPT_Option_Impropers 16
#define GPT_Option_Rigid     17
#define GPT_NUMBER           18

// we need a matrix saying, which GPTs are allowed in which scope ( globally, per Mol, per site?)
#define GPT_GLOB_OK  1
#define GPT_GLOB_REQ 2
#define GPT_MOL_OK   4
#define GPT_MOL_REQ  8
#define GPT_SITE_OK  16
#define GPT_SITE_REQ 32



class Coords {
   public:
    Coords () {x[0]=0; x[1]=0; x[2]=0;}
    Coords (double _x, double _y, double _z) {x[0]=_x; x[1]=_y; x[2]=_z;}
    Coords (const Coords &other) {x[0]=other.x[0]; x[1]=other.x[1]; x[2]=other.x[2];}
    
    double x[3];
    
    inline double& operator[] (const int i) { return x[i]; }
    double  getLenSq   ();
    double  getLen     ();
    bool    isSane     ();   // check, if there are NaNs or similar...
    
    static double scalar (Coords &o1, Coords &o2);
    };
Coords  operator+  (const Coords &c1, const Coords &c2);
Coords  operator-  (const Coords &c1, const Coords &c2);
Coords  operator-  (const Coords &c1);
Coords  operator*  (const Coords &c , const double &d );
    
class TemplateGeo {
   public:
     TemplateGeo ();
     //TemplateGeo (const TemplateGeo &other);
    ~TemplateGeo ();
   
    vector<Coords> molPositions;
    
    Coords      getCoords         (int molID);
    TemplateGeo getRotatedVersion (double alpha, double beta);
    TemplateGeo translate         (double dx, double dy, double dz);
    Coords      centerOfMass      (ReactantTemplate *t, class GeometryDefinition *geo);
    };


/**
 * s1 
 *  \  s3 s3i
 *   A---> <---B
 *              \ s4
 */
class DihedralGeo {
   public:
    int site1Id;
    int idx3Moltype;
    int site3Id;
    int site3InversId;
    int site4Id;
    double angle;
    int    dihedralId;
    };



class SiteGeo {
   public:
    SiteGeo ();

    vector<double> gptValues;
    vector<bool>   gptDefined;
        
    /// remember, if this site was already visited in the process of filling the geometry data (readMGeoFile)
    bool           visited;
    };

class MolGeo {
   public:
    MolGeo ();
    
    vector<SiteGeo> sites;    // sites as listed in the mtm
    
    // new values (since 1.01)
    vector<double> gptValues;
    vector<bool>   gptDefined;
    
    // which dihedral angles can use this molecule as idx2? (idx2 is the ,,central one'' for dihedrals...)
    vector<DihedralGeo> dihedrals;
    
    double     getAlphaBetween    (int siteI, int siteJ);
    double     getImproperBetween (int siteI, int siteJ, int siteK); //TODO!
    double     getDihedralBetween (int SiteI, int aroundSiteJ, MolGeo otherMol, int aroundSiteK, int siteL);  //TODO!
    };


// we need this forward declaration...
class SRModel;

/**
 *   For every molecule that the mtm (MoleculeTypeManager) knows, there's now also a MolGeo class
 *   which stores information about the angle and length of each site.
 *   The GeometryDefinition manages these informations.
 *
 *   At Least in Lammps we'll have to use a predefined set of Angles and Bond-Distances, so 
 *   we'll calculate them through the getAngle... etc. functions!
 */
class GeometryDefinition
  {
  public:
   GeometryDefinition(SRModel *_srm);
  ~GeometryDefinition();
   
   void        readMGeoFile (string fname);
   void        readTGeoFile (string fname);
   //TemplateGeo getGeo      (int templID)   { return templates[templID]; }
   
   
   static int  gptFindPropertyIdByName (const string name);
   double      getProperty      (int prop);
   double      getProperty      (int prop, int mId);
   double      getProperty      (int prop, int mId, int sId);
   double      getMaxProperty   (int prop);
   
   double      getBondDistance  (int bId);
   double      getAngle         (int aId);
   double      getAngleForce    (int aId);
   double      getDihedral      (int dId);
   double      getMaxBondLen    ();
   
   // angle and bond numbering
   int         numBondTypes      ();
   int         numAngleTypes     ();
   int         numDihedralTypes  ();
   int         maxAnglesPerMol   ();      // migrated from MoleculeTypeManager!
   int         maxDihedralsPerMol();      // migrated from MoleculeTypeManager!
   
   int         getAngleId       (int mId , int sId1, int sId2);
   int         getBondId        (int mId1, int mId2, int sId1, int sId2);
   //vector<int> getGoodAngleBase (int mId, vector<int> givenSites);
   //vector<int> getAngles        (int mId, vector<int> givenSites);
   
   void        addTemplateGeo   (ReactantTemplate *rt, TemplateGeo tg);
   void        writeTemplateGeo (ostream &of, ReactantTemplate *rt);
      
   void        checkIfReady     ();
   
   // for dihedrals:
   vector<DihedralGeo>&  getDihedralSet  (int mId);
   void                  addDihedralDef  (string around, double angle);
   
  private:
   bool                          ready;
   
   SRModel                      *srm;
   MoleculeTypeManager          *mtm;
   NamesManager                 *names;
   RuleSet                      *rset;
                              
   // some default values for all species:
   vector<double>                gptValues;
   vector<bool>                  gptDefined;
   // actual strings are defined in geometry_definition.cpp
   static string                 gptGeometryPropertyStrings[];
   static int                    gptGeometryPropertyScope[];
   
   // and some more specific values, if specified...
   vector<MolGeo>                mols;               // in the MTM-order
   vector<TemplateGeo*>          templs;             // in the RuleSet-order!
   
   vector<double>                angBuffer;          // so we don't have to recalculate...
   vector<DihedralGeo>           allDihedrals;
   
   // some helper functions:
   int findNthSiteOfMolecule     (TemplMolecule *tm, int tmSiteId);
   };


} // namespace srsim
//} // namespace std

#endif

