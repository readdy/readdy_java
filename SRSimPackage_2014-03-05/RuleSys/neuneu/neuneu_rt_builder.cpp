 
/************************************************************************
  			neuneu_rt_builder.cpp - Copyright gerdl

**************************************************************************/
 
//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <string>
#include <sstream>
#include <iostream>
#include <fstream>

#include "neuneu_rt_builder.h"
#include "sr_error.h"
#include "multi_mol_reactant_template.h"

using namespace SRSim_ns;
using namespace std;

/// Reads a NeuNeu Ascii-image and interprets different signs as molecules of one type.
NeuneuRtBuilder::NeuneuRtBuilder(SRModel & _srm, string _input) : srm(_srm), input(_input)
  {
  printf ("NeuneuRtBuilder::NeuneuRtBuilder - reading ascii map\n");
  readAsciiMap(input);
  names = srm.names;
  }

/**
 *  creates the template-Molecules;
 *  Neighbouring characters are connected.
 * @param input: the filename of the ascii image
 * @return a new MultiMol Reactant Template
 */
ReactantTemplate *NeuneuRtBuilder::build()
  {
  printf ("  NeuneuRtBuilder::build - creating molecules\n");
  
  // create all the molecules:
  molMap.resize( map.size() );
  for (int j=0 ; j<map.size() ; j++)
      {
      molMap[j].resize( map[j].size() );
      for (int i=0 ; i<map[j].size() ; i++)
          {
          molMap[j][i] = -1;
          printf ("  NeuneuRtBuilder::build - i=%d j=%d \n",i,j);
          
          if ( map[j][i] != -1 )
             {
             printf ("    map[j][i] = %d \n",map[j][i]);
             printf ("    neu_types.molTp = %s \n", neu_types[ map[j][i] ].molTp.c_str() );
             
             // build a new mol
             int typeID = names->existsID (NamesManager::MoleculeSpeciesName, neu_types[ map[j][i] ].molTp );
             printf ("    mType = %d \n",typeID);
             if (typeID == -1)
                throw new SRException("NeuneuRtBuilder::build: sorry - we didn't find a molecule type with the name "+neu_types[ map[j][i] ].molTp);
                
             TemplMolecule *m = buildFreeMol(typeID);
          
          
             // do initial modifications, if necessary:
             int nExcitations = neu_types[ map[j][i] ].basicExcitationSite.size();
             for (int k=0 ; k<nExcitations ; k++)
                 {
                 string sName = neu_types[ map[j][i] ].molTp + "(" + neu_types[ map[j][i] ].basicExcitationSite[k] + ")";
                 //printf ("    sName = %s \n",sName.c_str());
                 int    sType = names->existsID (NamesManager::SiteName        , sName );
                 //printf ("    sType = %d \n",sType);
                 if (sType == -1)
                     throw new SRException("NeuneuRtBuilder::build: sorry - we didn't find the site name.\n");
                 int    mType = names->existsID (NamesManager::ModificationName, neu_types[ map[j][i] ].basicExcitationMod[k] );
                 if (mType == -1)
                     throw new SRException("NeuneuRtBuilder::build: sorry - we didn't find the modification name.\n");
                 //printf ("    mType = %d \n",mType);
                 //printf ("    numSites = %d \n",m->numSites() );
                 for (int k=0 ; k < m->numSites() ; k++)
                     if (m->getSiteType(k) == sType)
                         {
                         //printf ("        setting site k=%d.\n", k);
                         m->getSite(k)->setModif( mType );
                         }
                 }
          
             printf("    pushing a new molecule onto the template.\n");
             // now remember this molecule in the molList and the molMap!
             molList.push_back( m );
             molMap[j][i] = molList.size() - 1;
             }
          }
      }
      
      
  // print map
  printf ("Molecule Type Map:\n");
  for (int j=0 ; j<map.size() ; j++)
      {
      for (int i=0 ; i<map[j].size() ; i++)
          printf ("%3d", map[j][i]);
      printf ("\n");
      }
  // print molMap
  printf ("Molecule Id Map:\n");
  for (int j=0 ; j<map.size() ; j++)
      {
      for (int i=0 ; i<map[j].size() ; i++)
          printf ("%3d", molMap[j][i]);
      printf ("\n");
      }
      
      
  printf ("  NeuneuRtBuilder::build - connecting molecules\n");
  // now do all the connections:
  for (int j=0 ; j<map.size() ; j++)
      for (int i=0 ; i<map[j].size() ; i++)
          {
          // search for neighbouring connections
	  if (molMap[j][i] == -1) continue;     // don't connect an empty field
          printf ("  NeuneuRtBuilder::build Connector: i=%d j=%d \n",i,j);
	  
	  // sweep a 3x3 square around i,j
          //    stop - no 3x3, only look forward!
	  for (int jj=-1 ; jj<=1 ; jj++)
	      for (int ii=-1 ; ii<=1 ; ii++)
	          {
		  // skip, if we're over the borders:
		  int x = i+ii;
	          int y = j+jj;
		  if (y <  0)                      continue;
		  if (y >= molMap.size())          continue;
		  if (x <  0)                      continue;
		  if (x >= molMap[y].size())       continue;
		  
		  if (jj == 0 && ii == 0)          continue;  // don't connect to itself
		  if (molMap[y][x] == -1)          continue;  // don't connect to an empty field
		  if (molMap[j][i] > molMap[y][x]) continue;  // only connect once - otherwise, we'll have two connections per pair of molecules
		  
                  connectInMap(i,j, x,y);
		  }
	  }
	
  // see if we find ,,tunneled'' connections:
  printf ("  NeuneuRtBuilder::build - connections through tunnels...\n");
  for (int k=0 ; k<connectorTunnels.size() ; k++ )
      {
      vector<int> knownCoords;
      
      // now go over the tunnel-pairs in steps of two:
      for (int l=0 ; l<connectorTunnels[k].size() ; l+=2 )
          {
          int i = connectorTunnels[k][l+0];
          int j = connectorTunnels[k][l+1];
          
	  for (int jj=-1 ; jj<=1 ; jj++)
	      for (int ii=-1 ; ii<=1 ; ii++)
	          {
		  // skip, if we're over the borders:
		  int x = i+ii;
	          int y = j+jj;
		  if (y <  0)                      continue;
		  if (y >= molMap.size())          continue;
		  if (x <  0)                      continue;
		  if (x >= molMap[y].size())       continue;
		  
		  if (jj == 0 && ii == 0)          continue;  // don't connect to itself
		  if (molMap[y][x] == -1)          continue;  // don't connect to an empty field
		  
                  // connect (x,y) to all known coords:
                  for (int m=0 ; m<knownCoords.size() ; m+=2)
                      connectInMap(x,y, knownCoords[m], knownCoords[m+1]);
                      
                  // remember x,y as known for this tunnel:
                  knownCoords.push_back(x);
                  knownCoords.push_back(y);
		  }
	  }
       }
  
  
  // now we still have to build a ReactantTemplate from our molecules:
  printf ("  NeuneuRtBuilder::build - adding molecules to rt\n");
  MultiMolReactantTemplate *rt = new MultiMolReactantTemplate();
  for (int i=0 ; i<molList.size() ; i++)
      rt->addMolecule( molList[i] );
  
  printf ("  NeuneuRtBuilder::build - adding a use to rt\n");
  rt->addUse( ReactantTemplate::creatableRT );
  
  printf ("  NeuneuRtBuilder::build - checking connectivity\n");
  if (! rt->checkConnectivity()) 
     throw new SRException("NeuneuRtBuilder::build: The molecule graph that we built is not connected!");;
  
  return rt;
  }


// the map has to be adressed [j][i]
void NeuneuRtBuilder::readAsciiMap(string input)
  {
  ifstream infile( input.c_str() );
  if (! infile)
     throw new SRException("NeuneuRtBuilder::readAsciiMap: Cannot read input: "+input);
  //infile.open (input.c_str(), ifstream::in);
  
  // search for the break (---): after the break, the ascii image begins:
  //  before the break, we have the definition of the used symbols!
  while (infile.good())
      {
      string in;
      getline(infile, in);
      if (in.compare("---") != 0)
         {
         if (in.at(1) != ':')                       throw new SRException("NeuneuRtBuilder::readAsciiMap: Unknown Format for Symbol description.");
         if (in.find_first_of("(") == string::npos) throw new SRException("NeuneuRtBuilder::readAsciiMap: Unknown Format for Symbol description.");
         if (in.find_first_of(")") == string::npos) throw new SRException("NeuneuRtBuilder::readAsciiMap: Unknown Format for Symbol description.");
         
         neu_tp nn;
                nn.symbol = in.at(0);
                
         // mol tp:
         int start = 2;
         int end   = in.find_first_of("(") - 1;
         nn.molTp  = in.substr(start, end-start+1);
                
         // connector site type:
         end   = in.find_first_of(")");
         nn.connectorSite  = in.substr(start, end-start+1);
         
         // basic excitations:
         //         selected site can obtain initial modifications here!
         stringstream sin(in);
         string       in2;
         sin >> in2; // skip first part: that the mol(connector)
         while (sin.good()) // here come the basic excitations!
             {
             sin >> in2;
             int tilde = in2.find_first_of("~");
             if (tilde == string::npos)
                //throw new SRException("NeuneuRtBuilder::readAsciiMap: Didn't recognize the right format for the site modifications....");
                SRError::critical("NeuneuRtBuilder::readAsciiMap: Didn't recognize the right format for the site modifications....");
             
             nn.basicExcitationSite.push_back( in2.substr(0, tilde) );
             nn.basicExcitationMod .push_back( in2.substr(tilde+1) );
             }
         neu_types.push_back(nn);
         
         printf ("type %d: %c - %s %s  (%d modifications)\n", neu_types.size()-1, nn.symbol, nn.molTp.c_str(), nn.connectorSite.c_str(), nn.basicExcitationSite.size());
         }
      else
         break;
      }
  
  printf ("NeuneuRtBuilder::readAsciiMap ... now parsing the actual map:\n");
  
  // now, line by line, generate the map:
  int lCount = 0;
  while (infile.good())
      {
      string in;
      getline(infile, in);
      
      printf ("line %d is %s\n", lCount, in.c_str() );
      
      lCount++;
      map.resize( lCount );
      
      map[lCount-1].resize( in.length() );
      for (int i=0 ; i<in.length() ; i++)
          {
          char c = in.at(i);
          
          // set -1 as standard, if a symbol is found, the neu_type-index is set!
          map[lCount-1][i] = -1;
          for (int k=0 ; k<neu_types.size() ; k++)
              if (c == neu_types[k].symbol)
                 map[lCount-1][i] = k;
                 
          // connectorTunnels to build non-planar graphs:
          //  these are generated by the characters 0 .. 9 !
          if (c>='0' && c<='9' && map[lCount-1][i] == -1)
             {
             int cid = c-'0';  // connector ID
             if (connectorTunnels.size() < cid+1) connectorTunnels.resize(cid+1);
             connectorTunnels[cid].push_back( i );        // first y-coord
             connectorTunnels[cid].push_back( lCount-1 ); // then  x-coord
             }
          // unrecognized symbol?
          else if ( c != ' ' && map[lCount-1][i] == -1 )
             {
             string problem("\" \"");
                    problem[1] = c;
             throw new SRException("NeuneuRtBuilder::readAsciiMap: Didn't recognize the symbol: "+problem);
             //SRError::critical("NeuneuRtBuilder::readAsciiMap: Didn't recognize the symbol!");
             }
          }
      }

  infile.close();

  printf ("returning from NeuneuRtBuilder::readAsciiMap\n");
  }



TemplMolecule* NeuneuRtBuilder::buildFreeMol (int mType)
  {
  MoleculeTypeManager *mtm = srm.mtm;
    
  TemplMolecule *m = new TemplMolecule (mType, PObligatory);
  
  // now add the sites:
  for (int i=0 ; i<mtm->numSites(mType) ; i++)
      {
      int sType = mtm->getSiteType(mType,i);
      printf (" ---  Adding site %d\n", sType);
      m->addSite( sType, -1, PObligatory);
      }
      
  return m;
  }



void NeuneuRtBuilder::connectInMap (int i,int j, int x,int y)
  {
  printf ("  coords. (%d,%d) to (%d,%d)  \n", i,j,x,y);
  printf ("  molMap= (%d)    to (%d)  \n", molMap[j][i], molMap[y][x]);
  printf ("     Map= (%d)    to (%d)  \n",    map[j][i],    map[y][x]);
  printf ("  NeuneuRtBuilder::build: connecting - i,j(%d,%d)=%d to x,y(%d,%d)=%d \n",i,j,map[j][i],x,y,map[y][x] );
  
  // ok, molecule at position x,y has to be connected to mol at pos i,j:
  TemplMolecule *m1 = molList[ molMap[j][i] ];
  TemplMolecule *m2 = molList[ molMap[y][x] ];
  
  int sType1 = names->existsID (NamesManager::SiteName, neu_types[ map[j][i] ].connectorSite );
  int sType2 = names->existsID (NamesManager::SiteName, neu_types[ map[y][x] ].connectorSite );
  if (sType1==-1  ||  sType2==-1)
      throw new SRException("NeuneuRtBuilder::build: sorry - we didn't find the site name to connect molecules.\n");
  
  // search for a free site in m1:
  int site1 = -1;
  for (int k=0 ; k<m1->numSites() ; k++)
      {
      if (m1->getSiteType(k)       != sType1) continue;
      if (m1->getMoleculeAtSite(k) != NULL)   continue;
      site1 = k;
      }
  if (site1 == -1) 
      throw new SRException("NeuneuRtBuilder::build: Didn't find a site to connect this molecule to.");
  
  // search for a free site in m2:
  int site2 = -1;
  for (int k=0 ; k<m2->numSites() ; k++)
      {
      if (m2->getSiteType(k)       != sType2) continue;
      if (m2->getMoleculeAtSite(k) != NULL)   continue;
      site2 = k;
      }
  if (site2 == -1)
      throw new SRException("NeuneuRtBuilder::build: Didn't find a site to connect this molecule to.");;
  
  // the final connection:
  m1->getSite(site1)->connectToSite( m2->getSite(site2) );
  }



