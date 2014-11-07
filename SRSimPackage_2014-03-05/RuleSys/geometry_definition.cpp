

// C++ Implementation: geometry_definition
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "defs.h"
#include <assert.h>
#include <math.h>

#include "geometry_definition.h"
#include "bng_rule_builder.h"
#include "sr_error.h"
#include "templ_molecule.h"

#include "xml_stuff.h"
/*#include <xercesc/dom/DOM.hpp>
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMDocumentType.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMImplementationLS.hpp>
#include <xercesc/dom/DOMNodeIterator.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMText.hpp>

#include <xercesc/parsers/XercesDOMParser.hpp>
#include <xercesc/util/XMLUni.hpp>*/

#include <stdexcept>
#include <iostream>
#include <sstream>



using namespace xercesc;
using namespace std;
namespace SRSim_ns {






////////////
// Coords:
////////////

Coords operator+ (const Coords &c1, const Coords &c2)
  { return Coords(c1.x[0]+c2.x[0], c1.x[1]+c2.x[1], c1.x[2]+c2.x[2]); }
Coords operator- (const Coords &c1, const Coords &c2)
  { return Coords(c1.x[0]-c2.x[0], c1.x[1]-c2.x[1], c1.x[2]-c2.x[2]); }
Coords operator- (const Coords &c1)
  { return Coords(-c1.x[0] , -c1.x[1] , -c1.x[2]); }

Coords operator* (const Coords &c , const double &d )
  { return Coords(c.x[0]*d, c.x[1]*d, c.x[2]*d); }

double Coords::getLenSq ()
  { 
  double d = x[0]*x[0] + x[1]*x[1] + x[2]*x[2];
  /*if (isnan(d)) 
     {
     printf ("Coords::getLenSq: Sorry - I have to return Nan; x0 x1 x2: %f %f %f\n", x[0], x[1], x[2] );
     assert ( false );
     SRError::critical ("Coords::getLenSq: Nan in getLen sqared!");
     }*/
  return d;
  }

double Coords::getLen ()
  { 
  double d = sqrt( getLenSq() ); 
  /*if (isnan(d)) 
     {
     printf ("Coords::getLen: Sorry - I have to return Nan; LenSq: %f\n", getLenSq() );
     assert( false );
     SRError::critical ("Coords::getLen: Nan in getLen!");
     }*/
  return d;
  }

double Coords::scalar( Coords & o1, Coords & o2 )
  { 
  double d = o1[0]*o2[0] + o1[1]*o2[1] + o1[2]*o2[2]; 
  /*if (isnan(d)) 
     {
     printf ("Coords::scalar: Sorry - I have to return Nan; LenSq: %f\n", getLenSq() );
     SRError::critical ("Coords::scalar: Nan in getLen!");
     }*/
  return d;
  }


bool Coords::isSane()
  {
  if (isnan(x[0])) return false;
  if (isnan(x[1])) return false;
  if (isnan(x[2])) return false;
  return true;
  }










////////////
// TemplateGeo
////////////


SRSim_ns::TemplateGeo::TemplateGeo( )
  {
  }

/*SRSim_ns::TemplateGeo::TemplateGeo( const TemplateGeo & other )
  {
  molPositions = other.molPositions;
  }*/

SRSim_ns::TemplateGeo::~ TemplateGeo( )
  {
  //molPositions.clear();
  }

Coords TemplateGeo::getCoords( int molID )
  {
  return molPositions[molID];
  }

TemplateGeo TemplateGeo::translate( double dx, double dy, double dz )
  {
  TemplateGeo tg2(*this);
  for (int i=0 ; i<molPositions.size() ; i++) 
      {
      Coords &c = tg2.molPositions[i];
      c.x[0]+=dx; c.x[1]+=dy; c.x[2]+=dz;
      }
  return tg2;
  }
  
  
Coords TemplateGeo::centerOfMass( ReactantTemplate * t, GeometryDefinition *geo)
  {
  assert (t != NULL);
  assert (t->numMolecules() == molPositions.size() );
  
  double sumMass=0;
  Coords c;
  for (int i=0 ; i<t->numMolecules() ; i++) 
      {
      double mass = geo->getProperty(GPT_Mol_Mass, t->getMolecule(i)->getType() );
      c = c + (getCoords(i) * mass);
      sumMass += mass;
      assert (mass != 0.0);
      }
      
  c = c * (1.0 / sumMass);
  
  printf (" ######### COM = %f %f %f\n",c.x[0],c.x[1],c.x[2]);
  
  return c;
  }


  
////////////
// SiteGeo
////////////



SiteGeo::SiteGeo()
  {
  visited = false;

  gptValues .resize(GPT_NUMBER);
  gptDefined.resize(GPT_NUMBER, false);
  //dihedral = 0.0;
  
  //  < 0.0 means this value will be ignored in favour of the more general deviation values...
  //deviAngle = -1.0;
  //deviDist  = -1.0;
  }



////////////
// MolGeo
////////////
MolGeo::MolGeo ()
  {
  gptValues .resize(GPT_NUMBER);
  gptDefined.resize(GPT_NUMBER, false);
  }
 
/*int MolGeo::numAngles( int numSites )
  {
  //if (numSites <= 1) return 0;
  //else               return( 1+(numSites-2)*2 );
  
  assert (false);  // who is using this one anyway?
  
  //arithmetic series
  if (numSites==0 || numSites==1) return 0;
  return ((numSites-1)*numSites/2);
  }*/


///////////////////////
// GeometryDefinition
///////////////////////
  

GeometryDefinition::GeometryDefinition(SRModel *_srm) :
   srm(_srm), mtm(_srm->mtm), names(_srm->names), rset(_srm->ruleset)
  {
  gptValues .resize(GPT_NUMBER);
  gptDefined.resize(GPT_NUMBER, false);
  
  // GPT default values:
  
  
  ready = false;
  }

/* ---------------------------------------------------------------------- */

GeometryDefinition::~GeometryDefinition()
  {
  for (int i=0 ; i<templs.size() ; i++) 
      if (templs[i] != NULL) delete templs[i];
  
  //mols.clear();
  //templs.clear();
  //printf ("Templates cleared!\n");
  }

/* ---------------------------------------------------------------------- */


string GeometryDefinition::gptGeometryPropertyStrings[] = {
      "GPT_Site_Theta",
      "GPT_Site_Phi",
      "GPT_Site_Dist",
      "GPT_Site_Dihedral",
      
      "GPT_Mol_Mass",
      "GPT_Mol_Rad",
      
      "GPT_Devi_Dist",
      "GPT_Devi_Angle",
      "GPT_Diffusion",
      "GPT_Refractory",
      
      "GPT_Force_Repulsion",
      "GPT_Force_Bond",
      "GPT_Force_Angle",         // glob req, per mol / per site ok
      "GPT_Force_Dihedral",
      "GPT_Temperature",
      
      "GPT_Option_Dihedrals",
      "GPT_Option_Impropers",
      "GPT_Option_Rigid"};


/*#define GPT_GLOB_OK  1                   13 =  *
  #define GPT_GLOB_REQ 2                         *
  #define GPT_MOL_OK   4
  #define GPT_MOL_REQ  8                         *
  #define GPT_SITE_OK  16
  #define GPT_SITE_REQ 32*/
int GeometryDefinition::gptGeometryPropertyScope[] = {53,53,53,16, 13,13, 53,53,5,13, 3,3,23,3,3, 3,3,3};
//int GeometryDefinition::gptGeometryPropertyScope[] = {53,53,53,16, 13,13, 53,53,5,5, 3,3,3,3,3, 3,3,3};
//                                                                 ^per mol req      ^ glob required
//                                                    ^- per site req     ^ per mol or glob or site           

int GeometryDefinition::gptFindPropertyIdByName(const string name)
    {
    for (int i=0 ; i<GPT_NUMBER ; i++)
        if (gptGeometryPropertyStrings[i].compare(name) == 0)
           return i;
    
    SRError::critical ("GeometryDefinition:: unknown property name used: " + name);
    return -1;
    }




/* ---------------------------------------------------------------------- */

double GeometryDefinition::getProperty (int prop)
  {
  // allowed to ask for this property globally?
  assert( (gptGeometryPropertyScope[prop] & GPT_GLOB_OK) != 0 );
  
  // is this value defined?
  assert( gptDefined[prop] );
  
  // return the value:
  return gptValues[prop];
  }
   
/* ---------------------------------------------------------------------- */

double GeometryDefinition::getProperty (int prop, int mId)
  {
  assert( (gptGeometryPropertyScope[prop] & GPT_MOL_OK) != 0 );
  
  // use the molecule-specific, if it exists...
  if (mols[mId].gptDefined[prop])
     return ( mols[mId].gptValues[prop] );
     
  // else use the more general value:
  assert( gptDefined[prop] );
  return gptValues[prop];
  }
  
/* ---------------------------------------------------------------------- */

double GeometryDefinition::getProperty (int prop, int mId, int sId)
  {
  assert( (gptGeometryPropertyScope[prop] & GPT_SITE_OK) != 0 );
  
  // use the site-specific, if it exists...
  if (mols[mId].sites[sId].gptDefined[prop])
     return ( mols[mId].sites[sId].gptValues[prop] );
  
  // use the molecule-specific, if it exists...
  if (mols[mId].gptDefined[prop])
     return ( mols[mId].gptValues[prop] );
     
  // else use the more general value:
  //assert( gptDefined[prop] );
  if (!gptDefined[prop])
     {
     printf(" requested property: %d, mid=%d, sid=%d --> sTp=%d\n", prop, mId, sId, mtm->getSiteType(mId,sId) );
     printf("                     site: %s \n", names->getName(NamesManager::SiteName, mtm->getSiteType(mId,sId)).c_str() );
     assert( gptDefined[prop] );
     }
  return gptValues[prop];
  }
  
/* ---------------------------------------------------------------------- */

double GeometryDefinition::getMaxProperty (int prop)
  {
  double val = -1;
  
  if ((gptGeometryPropertyScope[prop] & GPT_GLOB_OK) != 0)
     if (gptDefined[prop])
        val = gptValues[prop];
        
  for (int i=0 ; i<mols.size() ; i++)
      {
      // care about the molecular values?
      if ((gptGeometryPropertyScope[prop] & GPT_MOL_OK) != 0)
         {
         if (mols[i].gptDefined[prop])
            if (mols[i].gptValues[prop] > val) val = mols[i].gptValues[prop];
         }
         
      // care about the sites?
      if ((gptGeometryPropertyScope[prop] & GPT_SITE_OK) == 0) continue;
      for (int j=0 ; j<mols[i].sites.size() ; j++)
          if (mols[i].sites[j].gptDefined[prop])
             if (mols[i].sites[j].gptValues[prop] > val) val = mols[i].sites[j].gptValues[prop];
      }
      
  assert (val != -1);
  return val;
  }


/* ---------------------------------------------------------------------- */
  


/*void GeometryDefinition::initSize( )
  {
  mols.resize(mtm->numMolIDs());
  for (int iMol = 0 ; iMol < mtm->numMolIDs() ; iMol++)
      {
      if (! mtm->isMol(iMol) ) continue;
      int nSites = mtm->numSites(iMol);
      mols[iMol].sites.resize( nSites );
      }
  }*/

/* ---------------------------------------------------------------------- */




/**
 *     Parse a molecular geometry file (*.geo) including the site geometry as well
 *     as further reactor properties...
 */
void GeometryDefinition::readMGeoFile( string fname )
  {
  string nullStr;
  //initSize ();
  
  try
    {
    // init xml file stuff & read date:
    XmlStuff xf(fname);
    
    // to we support this new geometry-file-version?
    try { 
        if (! xf.goToNextTag( string("version") ) )
           throw 41;
        string ver = xf.retrieveTagOption(string("value"));  
        if (atof(ver.c_str()) < 1.01 )  // that's the first version to support this new property system.
           throw 41;
        }
    //catch ( XmlError& e )
    catch ( ... )
        { SRError::critical ("GeometryDefinition::readMGeoFile error processing input: Seems like we're trying to read an old geometry-description version without ,,version'' tag or below version 1.01!"); }
    
    
    // run over all molecules:
    while (xf.goToNextTag( string("molecule") ))
        {
        // what's this molecule's name?
        string mName = xf.retrieveTagOption( string("name") );
        int    mId   = names->getID(NamesManager::MoleculeSpeciesName,mName);

        printf ("GeometryDefinition::readMGeoFile: reading molecule %d -- %s \n", mId, mName.c_str());

        // create a new MolGeo?
        if (mId >= mols.size()) mols.resize(mId+1);
        if (mols[mId].sites.size() != 0)
           SRError::critical ( "GeometryDefinition::readMGeoFile(): molecule "+mName+" was defined twice." );
        mols[mId].sites.resize( mtm->numSites(mId) );
                
        // run over sites and data:
        MolGeo &mg = mols[mId];
        
        // enter this molecule:
        xf.goIntoTag();
        
        // per molecule properties:
        while (xf.goToNextTag( string("property") ))
           {
           string propName  = xf.retrieveTagOption("name");
           double propValue = atof( xf.retrieveTagOption("value").c_str() );
           
           printf ("                                    entering property %s -- %f \n", propName.c_str(), propValue);
           
           // gptFindProp... will crash if site name is unknown!
           int propId = gptFindPropertyIdByName( propName );
           mg.gptDefined[propId] = true;
           mg.gptValues [propId] = propValue;
           }
        xf.redoActualTag(); // otherwise, we might have missed some sites already!
        
        while(xf.goToNextTag("site"))
           {
           string sName           = xf.retrieveTagOption("name");
           string complexSiteName = mName+"("+sName+")";
           int    sType           = names->getID(NamesManager::SiteName,complexSiteName);
            
           // find the right site to assign the properties to:
           int siteId = -1;
           for (int iii=0 ; iii<mtm->numSites(mId) ; iii++)
               {
               //printf ("  found sType %d\n", mtm->getSiteType(mId,iii));
               if (mtm->getSiteType(mId,iii) != sType) continue;
               if (mg.sites[iii].visited)              continue;   // this site's already visited.
               siteId = iii;
               mg.sites[iii].visited = true;
               break;
               }
           if (siteId == -1) 
              {
              string eStr = mName +"::"+ sName;
              SRError::critical ("GeometryDefinition::readMGeoFile: unknown Site in mol!", eStr.c_str());
              }
           SiteGeo &sg = mg.sites[siteId];
           
           printf ("                                    entering site %d -- %s (as site %d)\n", sType, sName.c_str(), siteId);
                  
           // See if we have values in the tag itself:
           try{
              sg.gptValues [GPT_Site_Phi]   = atof( xf.retrieveTagOption("phi")  .c_str() );
              sg.gptDefined[GPT_Site_Phi]   = true;
              sg.gptValues [GPT_Site_Theta] = atof( xf.retrieveTagOption("theta").c_str() );
              sg.gptDefined[GPT_Site_Theta] = true;
              sg.gptValues [GPT_Site_Dist]   = atof( xf.retrieveTagOption("dist") .c_str() );
              sg.gptDefined[GPT_Site_Dist]   = true;
              }
           catch(XmlTagOptionNotFound e) {}
           
           // parse the rest of the per-site options:
           xf.goIntoTag();
           while (xf.goToNextTag("property"))
               {
               string propName  = xf.retrieveTagOption("name");
               double propValue = atof( xf.retrieveTagOption("value").c_str() );
              
               // gptFindProp... will crash if site name is unknown!
               int propId = gptFindPropertyIdByName( propName.c_str() );
               sg.gptDefined[propId] = true;
               sg.gptValues [propId] = propValue;
               }
           
           xf.returnFromTag();
           }
               
        xf.returnFromTag();
        }
        
    // general properties: e.g. Deviations of Distance and Angles:
    xf.redoActualTag();
    if (xf.goToNextTag("GeneralProperties") )
       {
       xf.goIntoTag();
       while (xf.goToNextTag("property"))
           {
           string propName  = xf.retrieveTagOption("name");
           double propValue = atof( xf.retrieveTagOption("value").c_str() );
           
           // gptFindProp... will crash if site name is unknown!
           int propId = gptFindPropertyIdByName( propName.c_str() );
           gptDefined[propId] = true;
           gptValues [propId] = propValue;
           }
       xf.returnFromTag();
       }
       
    // see if something was forgotten?
    checkIfReady();
    
    // read dihedrals now that the rest of the geometry is settled and confirmed by the check!
    xf.redoActualTag();
    if ( getProperty(GPT_Option_Dihedrals) > 0.5 )
       {
       if ( !xf.goToNextTag("DihedralAngles") )
          SRError::critical ("GeometryDefinition::readMGeoFile: dihedrals should be used, but none were defined!");
          
       xf.goIntoTag();
       while ( xf.goToNextTag("dihedral") )
           {
           string around  = xf.retrieveTagOption("around");
           double angle   = atof( xf.retrieveTagOption("angle").c_str() );
           addDihedralDef(around,angle);
           }
       
       xf.returnFromTag();
       printf ("GeometryDefinition::readMGeoFile : parsing dihedrals done.\n");
       }
    
    
    ready = true;
    printf ("GeometryDefinition::readMGeoFile : reading geometry info done.\n");
    }
  catch( XmlError& e )
    { SRError::critical ("GeometryDefinition::readMGeoFile error processing input:",e.err.c_str()); }
  
  }



string itos(int i)
  { 
  stringstream boeh; 
  boeh<<i; 
  return boeh.str(); 
  }

/* ---------------------------------------------------------------------- */
  
/** 
  *          Run over all GPT values that are defined, beginning from the lowest (=most general
  *          scope) ending at the sites. If a property is defined to be required at the site level,
  *          it suffices to find the value at the global level.
  *          On the other hand, if a value is defined to be equired at the general scope,
  *           its specification on the site level does not suffice.
  *
  *          Also, it will be checked, if there is a geometry entry for every site an molecule that
  *           is mentioned by the molecule type manager.
  */
void GeometryDefinition::checkIfReady()
  {
  //printf ("hi\n");
  bool                   propDefined [GPT_NUMBER];
  vector< vector<bool> > propDefinedM(GPT_NUMBER);  // per molecule
  
  // is there a geometry entry for every site in the mtm
  int numMolIDs = mtm->numMolIDs();
  for (int i=0 ; i<numMolIDs ; i++) 
      {
      //printf ("IsMol Frage: %d\n",i);
      if (! mtm->isMol(i)) continue;
      //printf ("IsMol Nightgut:\n");
      
      if (mols.size() <= i)
         SRError::critical ("Missing mol "+itos(i)+" ", names->getName(NamesManager::MoleculeSpeciesName, i) );
      }
  
  // are required general values present?
  for (int i=0 ; i<GPT_NUMBER ; i++)
      {
      propDefined[i] = gptDefined[i];
      //printf ("  glob: def %d     req %d\n", propDefined[i], (gptGeometryPropertyScope[i] & GPT_GLOB_REQ));
      
      if ((gptGeometryPropertyScope[i] & GPT_GLOB_REQ) != 0  &&  ! propDefined[i])
         SRError::critical ("GeometryDefinition::checkIfReady: value "+gptGeometryPropertyStrings[i]+" required in the general scope" );
      }
  
  // per molecule values:
  for (int i=0 ; i<GPT_NUMBER ; i++)
      {
      propDefinedM[i].resize( numMolIDs );
      for (int j=0 ; j<numMolIDs ; j++)
          {
          if (! mtm->isMol(j)) continue;
          propDefinedM[i][j] = propDefined[i] | mols[j].gptDefined[i];
          if ((gptGeometryPropertyScope[i] & GPT_MOL_REQ) != 0  &&  ! propDefinedM[i][j] )
             SRError::critical ("GeometryDefinition::checkIfReady: value required for the per-molecule scope:",gptGeometryPropertyStrings[i]);
          //SRError::critical ("Missing geo information for mol",names->getName(NamesManager::MoleculeSpeciesName, i).c_str() );
          
          // now remember what is defined in this molecule:
          }
      }
  
  // per site values:
  for (int j=0 ; j<numMolIDs ; j++)
      {
      if (! mtm->isMol(j)) continue;
      for (int k=0 ; k<mols[j].sites.size() ; k++)
          {
          
          string s = names->getName( NamesManager::SiteName, mtm->getSiteType(j,k) );
          printf (" site j%d k%d -> %s\n", j,k, s.c_str() );
          
          for (int i=0 ; i<GPT_NUMBER ; i++)
              {
              bool propDefinedMS = propDefined[i] | propDefinedM[i][j] | mols[j].sites[k].gptDefined[i];
              if ((gptGeometryPropertyScope[i] & GPT_SITE_REQ) != 0  &&  ! propDefinedMS)
                 {
                 string siteName = names->getName(NamesManager::SiteName, mtm->getSiteType(j,k));
                 SRError::critical ("GeometryDefinition::checkIfReady: value required for the per-site scope: "+siteName+": ",gptGeometryPropertyStrings[i]);
                 }
              }
          }
      }
      
  
  printf ("GeometryDefinition::checkIfReady(): geometry set looks fine.\n");
  }



// /**
// *   modified version of the example from http://www.yolinux.com/TUTORIALS/XML-Xerces-C.html
// *      "Parsing XML with Xerces-C C++ API"
// */
// void GeometryDefinition::readMGeoFile_oldVersion( string fname )
//   {
//   string nullStr;
//   //initSize ();
//   
//   try
//     {
//     // init xml file stuff & read date:
//     XmlStuff xf(fname);
//     
//     // run over all molecules:
//     while (xf.goToNextTag("molecule"))
//         {
//         // what's this molecule's name?
//         string mName = xf.retrieveTagOption("name");
//         int    mId   = names->getID(NamesManager::MoleculeSpeciesName,mName);
// 
//         // create a new MolGeo?
//         if (mId >= mols.size()) mols.resize(mId+1,NULL);
//         if (mols[mId] == NULL ) 
//            {
//            mols[mId] = new MolGeo();
//            mols[mId]->sites.resize( mtm->numSites(mId) );
//            }
//                 
//         // run over sites and data:
//         MolGeo &mg = *mols[mId];
//         bool mass=false; 
//         bool rad=false;
//         
//         xf.goIntoTag();
//         
//         if (xf.goToNextTag("mass"))
//            {
//            mg.mass = atof( xf.retrieveTagOption("value").c_str() ); mass=true;
//            xf.redoActualTag();
//            }
//         if (xf.goToNextTag("radius"))
//            {
//            mg.rad = atof( xf.retrieveTagOption("value").c_str() ); rad=true;
//            xf.redoActualTag();
//            }
//         
//         while(xf.goToNextTag("site"))
//            {
//            double phi   = atof( xf.retrieveTagOption("phi")  .c_str() );
//            double theta = atof( xf.retrieveTagOption("theta").c_str() );
//            double dist  = atof( xf.retrieveTagOption("dist") .c_str() );
//                
//            bool   written         = false;
//            string sName           = xf.retrieveTagOption("name");
//            string complexSiteName = mName+"("+sName+")";
//            int    sType           = names->getID(NamesManager::SiteName,complexSiteName);
//            //printf ("Searching sType = %d called %s\n",sType, complexSiteName.c_str());
//            for (int iii=0 ; iii<mtm->numSites(mId) ; iii++)
//                {
//                //printf ("  found sType %d\n", mtm->getSiteType(mId,iii));
//                if (mtm->getSiteType(mId,iii) != sType) continue;
//                if (mg.sites[iii].ready)                continue;   // this site's already visited.
//                mg.sites[iii] = SiteGeo(phi, theta, dist);
//                written = true;
//                break;
//                }
//                   
//            if (!written) 
//               {
//               string eStr = mName +"::"+ sName;
//               SRError::critical ("unknown Site in mol!", eStr.c_str());
//               }
//            }
//                
//         // is our MolGeo mg ready now?
//         if (mass && rad) mg.ready = true;
//         if (! mg.isReady())
//            SRError::critical ("incomplete mol definition.", mName.c_str());
//            
//         xf.returnFromTag();
//         }
//         
//     // Deviations of Distance and Angles:
//     xf.redoActualTag();
//     if (! xf.goToNextTag("ReaktionGeometry") )
//        SRError::critical ("Mol File without ReaktionGeometry tag!");
//     xf.goIntoTag();
//     xf.goToNextTag("AngularDeviation");
//     deviAngle = atof( xf.retrieveTagOption("value").c_str() );    
//     xf.redoActualTag(); 
//     xf.goToNextTag("DistanceDeviation");
//     deviDist = atof( xf.retrieveTagOption("value").c_str() );    
//     if (deviDist==0 || deviAngle==0) 
//        SRError::warning ("Angular or Distance deviation for reactions are Zero... this is not a nice value!!");
//     printf ("GeometryDefinition::readMGeoFile :  deviAngle=%f deviDist=%f\n", deviAngle, deviDist);
//     
//             
//     // is our GeometryDefinition ready now?
//     if (! areMolsReady() || deviAngle<0 || deviDist<0)
//        SRError::critical ("incomplete geometry definition.");
//     }
//   catch( XmlError& e )
//     { SRError::critical ("GeometryDefinition::readMGeoFile error processing input:",e.err.c_str()); }
//   
//   printf ("Done reading molecule-geo info.\n");
//   ready = true;
//   }
  
/* ---------------------------------------------------------------------- */
  
void GeometryDefinition::readTGeoFile( string fname )
  {
  try
    {
    // init xml file stuff & read date:
    XmlStuff xf(fname);
    
    // run over all molecules:
    while (xf.goToNextTag("template"))
        {
        int    tId   = atoi( xf.retrieveTagOption("id").c_str() );
        string tName =       xf.retrieveTagOption("name");
        
        if (rset->getRT(tId)->getName() != tName)
           SRError::critical( "GeometryDefinition::readTGeoFile: template-id and name don't fit!", tName.c_str());
        
        // create TemplateGeo:
        TemplateGeo tg;
                    tg.molPositions.resize( rset->getRT(tId)->numMolecules() );
           
        // insert mol Coords:
        xf.goIntoTag();
        while (xf.goToNextTag("mol"))
           {
           int mId = atoi( xf.retrieveTagOption("id").c_str() );
           assert ( mId < rset->getRT(tId)->numMolecules() );
           tg.molPositions[mId].x[0] = atof( xf.retrieveTagOption("x").c_str() );
           tg.molPositions[mId].x[1] = atof( xf.retrieveTagOption("y").c_str() );
           tg.molPositions[mId].x[2] = atof( xf.retrieveTagOption("z").c_str() );
           }
        xf.returnFromTag();
        
        // insert this peace of geometry:
        addTemplateGeo( rset->getRT(tId), tg );
        }
            
    }
  catch( XmlError& e )
    { SRError::critical ("GeometryDefinition::readTGeoFile error processing input:",e.err.c_str()); }
  
  printf ("Done reading template-geo info.\n");
  }
  

  
/* ---------------------------------------------------------------------- */
  
/*TemplateGeo SRSim_ns::GeometryDefinition::BuildGeoForTemplate( ReactantTemplate * t )
  {
  assert (false);
  int numMol = t->numMolecules();
  int molCnt = 0;
  
  stack<TemplMolecule*> toExplore;
                        toExplore.push (t->getMolecule(0));
  
  while (molCnt < numMol)
     {
     TemplMolecule *tm = toExplore.top();
                         toExplore.pop();

     // add children     
     for (int i=0 ; i<tm->numSites() ; i++)
         {
         TemplMolecule *mNext = tm->getMoleculeAtSite(i);
         if (mNext == NULL) continue;
         if (mNext->getRealization() != SRSIM_UNDEFINED) continue;
         toExplore.push(mNext);
         toExplore.setRealization( 1 );
         }
                    
     // write geometry:     
                         
     molCnt++;
     }
     
  // reset Realization to undefined.
  for (int i=0 ; i<numMol ; i++)   
      t->getMolecule(i)->setRealization(SRSIM_UNDEFINED);
  } */

/*void SRSim_ns::GeometryDefinition::initDataPostGeoFile( )
  {
  // // bond types:   unique-site-IDs: i,j
  // int maxSite = mtm->getMaxUSID();
  // bondID = maxSite*i + j;
  
  // angle types:
  // angleID = maxSite*i + j;   // i != j,  mol(i) == mol(j)
  bondTypes.resize(maxSite, <vector<int>(maxSite) );   // [maxSite][maxSite]
  for (int i=0 ; i<maxSite ; i++)
      for (int j=0 ; j<maxSite ; j++)
          {
          
          }
  }*/


/* ---------------------------------------------------------------------- */

int GeometryDefinition::numBondTypes( )
  {
  int numSites = mtm->numUniqueSites();
  return numSites * numSites;
  }

/* ---------------------------------------------------------------------- */


/**   Currently, this function returns the same as numBondTypes.
 *    So there is currently only one system of ids for bonds and angles:
 *    If an angle-id is for a bond between different molecules, 0 is returned as angle!
 */
int GeometryDefinition::numAngleTypes( )
  {
  return numBondTypes();
  /*int numSites = mtm->numUniqueSites();
  return numSites * numSites;*/
  }

/* ---------------------------------------------------------------------- */


int GeometryDefinition::numDihedralTypes( )
  {
  return allDihedrals.size();
  }

/* ---------------------------------------------------------------------- */


int GeometryDefinition::maxAnglesPerMol( )
  {
  int n = mtm->maxBondsPerMol();
  if (n<2) return 0; 
  return ((n-1)*n/2);        // just an arithmetic series
  }

/* ---------------------------------------------------------------------- */


int GeometryDefinition::maxDihedralsPerMol( )
  {
  int max = 0;
  for (uint i=0 ; i<mols.size() ; i++)
      if (mols[i].dihedrals.size() > max) max = mols[i].dihedrals.size();
  return max;
  }

/* ---------------------------------------------------------------------- */

/**
           Unique site ids are given even to different incarnations of a same-named site.
           e.g. A(a,a,a,a,x)  means that there are 5 unique sites in the molecule A.
 */
double GeometryDefinition::getBondDistance( int bId )
  {
  assert (ready);
  
  int numSites = mtm->numUniqueSites();
  int uniI = bId / numSites;
  int uniJ = bId % numSites;
  
  int  molI = mtm->getMolFromUSID( uniI );    
  int siteI = mtm->getSidFromUSID( uniI );
  int  molJ = mtm->getMolFromUSID( uniJ );
  int siteJ = mtm->getSidFromUSID( uniJ );
  
  double dist = 0.0;
  dist += getProperty(GPT_Site_Dist, molI, siteI);
  dist += getProperty(GPT_Site_Dist, molJ, siteJ);
  return dist;
  }

/* ---------------------------------------------------------------------- */

// replace by: getProperty(GPT_Site_Dist, mId, sId);
// double GeometryDefinition::getSiteLength( int mId, int sId )
//   {
//   assert(mols[mId] != NULL);
//   return mols[mId]->sites[sId].dist;
//   }







/* ---------------------------------------------------------------------- */



/// returns the medium force of both participating molecules:
double GeometryDefinition::getAngleForce( int aId )
  {
  assert (ready);
  
  // can we use buffered angles?
  if (aId >= angBuffer.size()) angBuffer.resize(aId+1, -666.0);
  else if (angBuffer[aId] >= 0.0) return angBuffer[aId];
  
  int numSites = mtm->numUniqueSites();
  int uniI = aId / numSites;
  int uniJ = aId % numSites;
  
  int  molI = mtm->getMolFromUSID( uniI );    
  int siteI = mtm->getSidFromUSID( uniI );
  int  molJ = mtm->getMolFromUSID( uniJ );
  int siteJ = mtm->getSidFromUSID( uniJ );
  
  // nonsense angles...
  if ( molI !=  molJ) return 0.0;
  if (siteI == siteJ) return 0.0;
  
  return getProperty(GPT_Force_Angle,molI,siteI) + getProperty(GPT_Force_Angle, molJ,siteJ) / 2.0;
  }



/* ---------------------------------------------------------------------- */

double GeometryDefinition::getAngle( int aId )
  {
  assert (ready);
  
  // can we use buffered angles?
  if (aId >= angBuffer.size()) angBuffer.resize(aId+1, -666.0);
  else if (angBuffer[aId] >= 0.0) return angBuffer[aId];
  
  int numSites = mtm->numUniqueSites();
  int uniI = aId / numSites;
  int uniJ = aId % numSites;
  
  int  molI = mtm->getMolFromUSID( uniI );    
  int siteI = mtm->getSidFromUSID( uniI );
  int  molJ = mtm->getMolFromUSID( uniJ );
  int siteJ = mtm->getSidFromUSID( uniJ );
  
  // nonsense angles...
  if ( molI !=  molJ) return 0.0;
  if (siteI == siteJ) return 0.0;
  
  //printf ("    getAng: mI=%d mJ=%d sI=%d sJ=%d\n",molI, molJ, siteI, siteJ); 
  //printf ("   aId-%d:: mol-%d, site-%d   zu   mol-%d, site-%d \n", aId, molI, siteI, molJ, siteJ);
  
  //  we'll project two points onto the unit-sphere-shell in carthesian coordinates... 
  //  then, to determine the angle, we'll have alpha = arccos(scalar(x*y) / (len(x)*len(y)) )
  // no radii are use, we're working on the unit-sphere...
  
  //double phi   = mols[molI]->sites[siteI].phi;
  //double theta = mols[molI]->sites[siteI].theta;
  double phi   = getProperty(GPT_Site_Phi,   molI, siteI);
  double theta = getProperty(GPT_Site_Theta, molI, siteI);
  theta *= M_PI / 180.0; phi *= M_PI / 180.0;
  double x1 = sin (theta) * cos (phi);
  double x2 = sin (theta) * sin (phi);
  double x3 = cos (theta);
  //printf ("      phi1=%3f  theta1=%3f    ---- ",phi,theta);
  
  phi   = getProperty(GPT_Site_Phi,   molJ, siteJ);
  theta = getProperty(GPT_Site_Theta, molJ, siteJ);
  theta *= M_PI / 180.0; phi *= M_PI / 180.0;
  double y1 = sin (theta) * cos (phi);
  double y2 = sin (theta) * sin (phi);
  double y3 = cos (theta);
  //printf ("  phi2=%3f  theta2=%3f    ---- ",phi,theta);
  
  double xtimesy = x1*y1 + x2*y2 + x3*y3;
  assert (xtimesy>=-1.0 && xtimesy<=1.0);
  
  
  //printf ("   x*y=%6.3f ",xtimesy);
  double alpha = acos (xtimesy);
  alpha *= 180.0 / M_PI;
  
  //if (signbit(xtimesy) != 0) alpha=-alpha;
  
  
  //printf ("     Alpha=%6.3f\n",alpha);
  
  //assert (false);
  
  angBuffer[aId] = alpha;
  return alpha;
  }


/* ---------------------------------------------------------------------- */



double GeometryDefinition::getDihedral( int dId )
  {
  return allDihedrals[dId].angle;
  }


/* ---------------------------------------------------------------------- */


double GeometryDefinition::getMaxBondLen( )
  {
  //printf ("GeometryDefinition::getMaxBondLen\n");
  double maxLen = getMaxProperty(GPT_Site_Dist);
  return 2.0*maxLen;
  }
  
  
/* ---------------------------------------------------------------------- */

int GeometryDefinition::getAngleId( int mId, int sId1, int sId2 )
  {
  return getBondId(mId,mId, sId1,sId2);
  }

/* ---------------------------------------------------------------------- */

int GeometryDefinition::getBondId( int mId1, int mId2, int sId1, int sId2 )
  {
  int uniI = mtm->getUniqueSiteID(mId1, sId1);
  int uniJ = mtm->getUniqueSiteID(mId2, sId2);
  
  int numSites = mtm->numUniqueSites();
  return uniI*numSites+uniJ;
  }

/* ---------------------------------------------------------------------- */

/*
 *  wich angle is going to be the base? taking [0] and [1] may be a problem, if 
 *  the angles are close to 0° or 180° !!
 *  So let's find a pair that's close to 90°!
 */
// vector< int > GeometryDefinition::getGoodAngleBase( int mId, vector< int > givenSites )
//   {
//   int numSites = givenSites.size();
//   assert (numSites >= 2);
//   
//   double best = 90.0;  // 90° is the worst, we'll try to get this value down to 0° ... 
//                        // meaning a zero deviation from 90°-Angle ;)
//                        
//   vector<int> bestPair(2);
//   bestPair[0] = 0; bestPair[1] = 1;
//   
//   for (int i=0 ; i<numSites ; i++)
//       for (int j=i+1 ; j<numSites ; j++)
//           {
//           double thisAng = getAngle( getAngleId(mId,givenSites[i],givenSites[j]) );
//                  thisAng -= 90.0;
//           if (thisAng<0) thisAng=-thisAng;
//           
//           if (thisAng<best) 
//              {
//              best = thisAng;
//              bestPair[0] = i;
//              bestPair[1] = j;
//              }
//           }
//           
//   assert (best < 90.0 || numSites==2);
//           
//   return bestPair;
//   }
  
/* ---------------------------------------------------------------------- */

/*
 *   Returns the Angles needed; Format:
 *    ret[0] = numAngles,
 *    ret[1],[2] -> sites of first angle
 *    ret[3],[4] -> sites of second angle
 */
// vector< int > GeometryDefinition::getAngles( int mId, vector< int > givenSites )
//   {
//   assert (false);
//   
//   int numSites = givenSites.size();
//   assert (numSites >= 2);
// 
//   
//     
//   for (int i=0 ; i<numSites ; i++)
//       for (int j=i+1 ; j<numSites ; j++)
//           {
//           double thisAng = getAngle( getAngleId(mId,givenSites[i],givenSites[j]) );
//                  thisAng -= 90.0;
//           }
//   }

/* ---------------------------------------------------------------------- */

void GeometryDefinition::addTemplateGeo( ReactantTemplate * rt, TemplateGeo tg )
  {
  // find the right templ_id:
  int templ_id = -1;
  for (int t=0 ; t<rset->numTemplates() ; t++)
      if (rset->getRT(t) == rt) templ_id = t;
  assert (templ_id != -1);
  
  // reserve mem...
  if (templ_id >= templs.size()) templs.resize( templ_id+1, NULL);
  templs[templ_id] = new TemplateGeo(tg);
  
  // inform the Template, it's got a geo!
  rt->setGeo( templs[templ_id] );
  }

/* ---------------------------------------------------------------------- */

/**
 *    if rt already has a TemplateGeo, it will be written to 'of' as xml-Tag:
 */
void GeometryDefinition::writeTemplateGeo( ostream & of, ReactantTemplate * rt )
  {
  TemplateGeo *tg  = rt->getGeo();
  int          tid = rset->searchTemplateID( rt );
  
  assert (tid != -1);  // then it would'n be from our ruleset!
  
  if ( rt->getRTType() != ReactantTemplate::MultiMolRT ) return;
  if ( rt->numMolecules() == 1) 
     {
     of << "      <template id=\""<< tid <<"\" name=\""<< rt->getName() <<"\"> \n";     
     of << "         <mol id=\"0\" x=\"0.0\" y=\"0.0\" z=\"0.0\" /> \n";      
     of << "      </template> \n";
     return;
     }
  else if (tg == NULL) return;
  
  of << "      <template id=\""<< tid <<"\" name=\""<< rt->getName() <<"\"> \n";
  for (int m=0 ; m < rt->numMolecules() ; m++)
      {
      Coords c = tg->getCoords(m);
      of << "         <mol id=\""<< m <<"\" x=\""<< c.x[0] <<"\" y=\""<< c.x[1] <<"\" z=\""<< c.x[2] <<"\" /> \n";
      }
  of << "      </template> \n";
  }


/* ---------------------------------------------------------------------- */


/// return the vector of all dihedrals that use mId as idx2 of the dihedral chain (idx1,idx2,idx3,idx4)
vector<DihedralGeo>&  GeometryDefinition::getDihedralSet  (int mId)
  {
  return mols[mId].dihedrals;
  }
   
/* ---------------------------------------------------------------------- */

/**
 *  around is BNGL string, defining which sites are rotating around which others,
 *   e.g. "A(a~0,x~0!1).B(x~0!1,b~0)" means that the axis is defined through
 *   both sites x, while the angular forces act upon the sites a and b.
 *  The modifications 0 to n denote the index of the sites that are meant as defined
 *   in the geometry manager, since there
 *   may be a various sites with the same name.
 */
void GeometryDefinition::addDihedralDef  (string around, double angle)
  {
  DihedralGeo newDihedral1;
  DihedralGeo newDihedral2;
  
  // have the around-template parsed by the rule-builder:
  BNGRuleBuilder rb( srm );
  ReactantTemplate *rt = rb.parseBNGTemplate(around);
  
  // there should be exactly two molecules:
  if (rt->numMolecules() != 2)
     SRError::critical ("GeometryDefinition::addDihedralDef: Molecule-templates for dihedral definitions have to consist of two molecules (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
     
  // each should have exactly two sites:
  TemplMolecule *tm1 = rt->getMolecule(0);
  TemplMolecule *tm2 = rt->getMolecule(1);
  if (tm1->numSites() != 2) SRError::critical ("GeometryDefinition::addDihedralDef: Molecule-templates for dihedral definitions have to consist of two molecules, two sites each (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
  if (tm2->numSites() != 2) SRError::critical ("GeometryDefinition::addDihedralDef: Molecule-templates for dihedral definitions have to consist of two molecules, two sites each (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");

  // all the molecule types and site types should be known with geometry already:
  if (mtm->numMolIDs()<=tm1->getType() || ! mtm->isMol(tm1->getType()) ) SRError::critical ("GeometryDefinition::addDihedralDef: One of the given molecules was not known before. ("+around+" was given.)");
  if (mtm->numMolIDs()<=tm2->getType() || ! mtm->isMol(tm2->getType()) ) SRError::critical ("GeometryDefinition::addDihedralDef: One of the given molecules was not known before. ("+around+" was given.)");
  
  // tm1, site1 should be bound to tm2, site0: as in A(a~0,x~0!1).B(x~0!1,b~0)
  //   otherwise we would have to switch the mapping...
  if (  tm1->getSite(0)->isConnected()) SRError::critical ("GeometryDefinition::addDihedralDef: both inner sites should be bound, the outer ones should be free (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
  if ( !tm1->getSite(1)->isConnected()) SRError::critical ("GeometryDefinition::addDihedralDef: both inner sites should be bound, the outer ones should be free (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
  if ( !tm2->getSite(0)->isConnected()) SRError::critical ("GeometryDefinition::addDihedralDef: both inner sites should be bound, the outer ones should be free (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
  if (  tm2->getSite(1)->isConnected()) SRError::critical ("GeometryDefinition::addDihedralDef: both inner sites should be bound, the outer ones should be free (e.g. \"A(a~0,x~0!1).B(x~0!1,b~0)\"). Instead, "+around+" was given.");
  
  // now find these sites through the NamesManager and the MoleculeTypeManager
  newDihedral1.site1Id       = findNthSiteOfMolecule(tm1, 0);
  newDihedral1.site3Id       = findNthSiteOfMolecule(tm1, 1);
  newDihedral1.idx3Moltype   = tm2->getType();
  newDihedral1.site3InversId = findNthSiteOfMolecule(tm2, 0);
  newDihedral1.site4Id       = findNthSiteOfMolecule(tm2, 1);
  newDihedral1.angle         = angle;
  newDihedral1.dihedralId    = allDihedrals.size();
  allDihedrals.push_back( newDihedral1 );
  mols[tm1->getType()].dihedrals.push_back( newDihedral1 );
  
//   printf (" idx2-site1id: %d\n", newDihedral1.site1Id);
//   printf (" idx2-site3id: %d\n", newDihedral1.site3Id);
//   printf (" idx3-site3iv: %d\n", newDihedral1.site3InversId);
//   printf (" idx3-site4id: %d\n", newDihedral1.site4Id);
//   
//   printf ("  idx2-type: %d\n", tm1->getType() );
//   printf ("  idx3-type: %d\n", tm2->getType() );
//   printf ("  idx2-name: %s\n", names->getName(NamesManager::MoleculeSpeciesName, tm1->getType()).c_str() );
//   printf ("  idx3-name: %s\n", names->getName(NamesManager::MoleculeSpeciesName, tm2->getType()).c_str() );
//   printf ("   idx2-site1id-name: %s\n", names->getName(NamesManager::SiteName, tm1->getSiteType(0) ).c_str() );
//   printf ("   idx2-site3id-name: %s\n", names->getName(NamesManager::SiteName, tm1->getSiteType(1) ).c_str() );
//   printf ("   idx3-site3iv-name: %s\n", names->getName(NamesManager::SiteName, tm2->getSiteType(0) ).c_str() );
//   printf ("   idx3-site4id-name: %s\n", names->getName(NamesManager::SiteName, tm2->getSiteType(1) ).c_str() );
  
  // for tm2, turn the sites around but keep the dihedralId:
//   newDihedral2 = newDihedral1;
//   newDihedral2.site1Id       = newDihedral1.site4Id;
//   newDihedral2.site3Id       = newDihedral1.site3InversId;
//   newDihedral1.idx3Moltype   = tm1->getType();
//   newDihedral2.site3InversId = newDihedral1.site3Id;
//   newDihedral2.site4Id       = newDihedral1.site1Id;
//   mols[tm2->getType()].dihedrals.push_back( newDihedral2 );
  
  delete rt;
  
  printf (("GeometryDefinition::addDihedralDef: added new dihedral: "+around+"\n").c_str());
  }

/// Interprets the name of the modification as an integer index, e.g. in a molecule like A(b,b,a,a,a,a,a,a)
///  the site a~2 would mean site id 4 (0-based).
int GeometryDefinition::findNthSiteOfMolecule(TemplMolecule *tm, int tmSiteId)
  {
  int molType  = tm->getType();
  int siteType = tm->getSiteType(tmSiteId);
  int siteCnt  = 0;
  
  //names->printAll();
  if (tm->getModificationAtSite(tmSiteId) != -1)
     {
     string modif = names->getName( NamesManager::ModificationName, tm->getModificationAtSite(tmSiteId) );
     if (EOF == sscanf( modif.c_str(), "%d", &siteCnt ))
        SRError::critical ("GeometryDefinition::findNthSiteOfMolecule: Site modification need to be a number here - denoting which instance of the site's name is to be used. ("+modif+" was given.)");
     }
        
  // now run over the mtm-molecules, search for the siteCnt-th molecule of type siteType
  int iRightType = 0;
  for (int i=0 ; i<mtm->numSites(molType) ; i++)
      {
      if (siteType == mtm->getSiteType(molType,i) ) 
         {
         if (iRightType == siteCnt) return i;
         iRightType++;
         }
      }
      
  SRError::critical ("GeometryDefinition::findNthSiteOfMolecule: an "+itos(siteCnt)+"th istance of the site type "+ names->getName( NamesManager::SiteName,siteType)+" was not found.");
  }










} // end SRSim_ns namespace!






