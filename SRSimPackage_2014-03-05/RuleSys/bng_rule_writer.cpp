//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>

#include "bng_rule_writer.h"

// some helper functions for sorting...  
  

/** The output should be a unique string identifying the template
      Any two templates with the same topology should produce the same BNG-String
  */  

  

/*typedef int molIdTp;
typedef int siteIdTp;
  
class MyComparator {
  public:
   MyComparator(vector<TemplMolecule*> &m) : mols(m) {}
  
   vector<TemplMolecule*>   &mols;
   vector<molIdTp>           sortMols;
   vector<vector<siteIdTp> > sortSites;
   
   int molId;
   int compareMode;
   
   bool operator() (int i, int j);                   // runs one of the following two
   bool compSite   (molIdTp  i, molIdTp  j);         // compares two molecule
   bool compMol    (siteIdTp i, siteIdTp j);         // compares two sites
   
   string siteToString (TemplSite *s1);
   string  molToString (int        m );
   };
   
   
bool MyComparator::operator() (int i, int j)
  {
  if (compareMode == 1) return compSite (i,j);
  else                  return compMol  (i,j);
  }
  
bool MyComparator::compSite (siteIdTp i, siteIdTp j)
  {
  string str1 = siteToString(mols[molId]->getSite(i));
  string str2 = siteToString(mols[molId]->getSite(j));
  
  return( str1.compare(str2) < 0 );
  }

string MyComparator::siteToString (TemplSite *s1)
  {
  stringstream sst; 
  sst << s1->getType();
  if (s1->getModif() != UNDEFINED)  sst << "~"<<s1->getModif();
  if (s1->getPattern() == PAnyBond) sst << "!+";
  if (s1->isConnected())            sst << "!";
  return sst.str();
  }

  
bool MyComparator::compMol (molIdTp i, molIdTp j)
  {
  string str1 = molToString(i);
  string str2 = molToString(j);
  
  return( str1.compare(str2) < 0 );
  }
   
string MyComparator::molToString (int m)
  {
  TemplMolecule *m1 = mols[m];
  stringstream sst; 
  sst << m1->getType();
  if (m1->numSites() == 0) return sst.str();   // abort here... no sites defined.
  
  // else:
  sst<<"(";
  for (int i=0 ; i<m1->numSites() ; i++)
      {
      int i_site = sortSites[m][i];
      sst<<siteToString( m1->getSite(i_site) );
      if (i+1 < m1->numSites()) sst<<',';
      }
  sst<<")";
  
  return sst.str();
  }
  
    
string ReactantTemplate::writeToBNGString ()
  {
  printf ("Ping\n");
  MyComparator             c(mols);
  printf ("Pong\n");
  
  // first: sort it all...
  for (int i=0 ; i<mols.size() ; i++)
      {
      c.sortMols .push_back(i);
      c.sortSites.push_back(vector<siteIdTp>());
      
      //printf ("Added Mol %d :\n",i);
      
      for (int j=0 ; j<mols[i]->numSites() ; j++)
          {
          c.sortSites[i].push_back (j);
          //printf ("     new Site %d :\n",j);
          }
          
      c.molId = i;
      c.compareMode = 1;
      sort (c.sortSites[i].begin(), c.sortSites[i].end(), c);
      }
      
  c.compareMode = 2;
  sort (c.sortMols.begin(), c.sortMols.end(), c);
  
  
  
  // then build the big string...
  
  stringstream sst;
  NamesManager connector(true);
  for (int i=0 ; i<mols.size() ; i++)
      {
      // build sites:
      int i_mol = c.sortMols[i];
      
      sst<< mols[i_mol]->getType();
      if (mols[i_mol]->numSites() == 0) continue;
      
      sst<<"(";
      for (int j=0 ; j<mols[i_mol]->numSites() ; j++)
          {
          c.molId =  i_mol;
          int        i_site = c.sortSites[i_mol][j];
          TemplSite *ts     = mols[i_mol]->getSite(i_site);
          sst << c.siteToString (ts);
          
          if (ts->isConnected())
             {
             char buf[50] = "";
             if (ts < ts->getOther()) sprintf (buf,"%p",ts);
             else                     sprintf (buf,"%p",ts->getOther());
             sst << connector.getID(buf);
             }
          
          if (j+1 < mols[i_mol]->numSites()) sst<<',';
          }
      sst<<")";
          
      if (i+1 < mols.size()) sst<<'.';
      }
      
  
  return sst.str();
  }
  

  
  
*/  
  
// ########################### Site Comparator ################


