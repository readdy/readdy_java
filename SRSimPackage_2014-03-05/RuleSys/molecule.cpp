/************************************************************************
  			molecule.h - Copyright gerdl

**************************************************************************/

#include "molecule.h"
#include "sr_error.h"
#include "names_manager.h"
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
#include <assert.h>

using namespace std;
namespace SRSim_ns {

 
/*void recAddToMols (vector<Molecule*> &mvv, Molecule *m)
  {
  //printf ("pushing mol %d\n", m->getUniqueID());
  mvv.push_back(m);
  
  for (int i=0 ; i<m->numSites() ; i++)
      {
      Molecule *mNew = m->getMoleculeAtSite(i);
      if (mNew == NULL) continue;
      
      int  mNewId   = mNew->getUniqueID();
      bool existent = false;
      for (int k=0 ; !existent && k<mvv.size() ; k++)
          if (mNewId == mvv[k]->getUniqueID())
             existent = true;
      
      if (!existent) recAddToMols (mvv, mNew);
      }
  //printf ("Done.\n");
  }*/



/**  Writes a .dot file as input for the graphviz-tools
     so you can visualize the template as a pretty graph.
 */
/*void Molecule::writeToDotFile (string fname)
  {
  // create Vector Mols:
  vector<Molecule*> mols;
  
  recAddToMols (mols, this);
  
  FILE *f = fopen (fname.c_str(), "w");
//  FILE *f     = stdout;
  char *idstr   = new char[100];
  char *idstr2  = new char[100];
  int   nullcnt = 0;
  
  fprintf (f,"digraph G {\n");
  for (int i=0 ; i<mols.size() ; i++)
      {
      int    MTp    = mols[i]->getType();
      string MName;
      
      sprintf (idstr ,"%d" ,mols[i]->getUniqueID());
      sprintf (idstr2,"T%d",MTp);
      MName  = MName + idstr2 + "_" + idstr;
      
      bool noEdges=true;
      for (int j=0 ; j<mols[i]->numSites() ; j++)
          {
          string TName;
          if (mols[i]->getMoleculeAtSite(j) == NULL)
             {
             TName = "null";
             sprintf (idstr,"_%d",nullcnt++);
             TName = TName+idstr;
             }
          else
             {
             int TTp   = mols[i]->getMoleculeAtSite(j)->getType();
             //TName = names->getName(NamesManager::MoleculeSpeciesName, TTp);
             sprintf (idstr ,"%d",mols[i]->getMoleculeAtSite(j)->getUniqueID());
             sprintf (idstr2,"T%d",TTp);
             TName = TName + idstr2 + "_" + idstr;
             noEdges = false;
             }
             
          int STp   = mols[i]->getSiteType(j);
          string SName;// = names->getName(NamesManager::SiteName, STp);
          sprintf (idstr2,"S%d-id%d",STp,j);
          SName = SName+idstr2;

          fprintf (f, "\"%s\" -> \"%s\" [label=\"%s\"] ;\n",MName.c_str(), TName.c_str(), SName.c_str());          
          
          }
      
      if (noEdges) 
         {
         SRError::warning("This should not have happened! unconnected template graph in ReactantTemplate::writeDot! Uhhggg!!");
         fprintf (f,"%s ; \n",MName.c_str());
         }
          
      }
  fprintf (f,"}\n");
  
  fclose (f);
  delete [] idstr;
  delete [] idstr2;
  }*/



/// a user has to give usedUids: an empty vector, that will
/// have the uids set to TRUE, if they are used for this BNGL graph.
string Molecule::generateBngl (NamesManager *n, vector<bool> &usedUids)
  {
  // stores the connection id at the position of its UID
  vector<int>        knownUids;
  vector<Molecule*>  allMols;
  NamesManager       connectors(true /*singleUser*/);
  
  stringstream       bngl;
                     //bngl.clear();
                     //bngl.str("");
  
  //printf("Molecule::generateBngl: will generate MolList now...\n");
  
  // get us one list of molecules:
  buildMolList(allMols, usedUids);
  
  for (int mid=0 ; mid < allMols.size() ; mid++) 
      {
      Molecule *m     = allMols[mid];
      int       myUid = m->getUniqueID();
      
      if (mid != 0) bngl << ".";
      string mName = n->getName( NamesManager::MoleculeSpeciesName , m->getType() );
      bngl << mName << "(";
      
      //printf("Molecule::generateBngl: mid = %d...  good:%s\n", mid, bngl.good() ? "ok":"bad" );
      //printf("           good2:%s\n", bngl.good() ? "ok":"bad" );
      //printf("  current bngl: %s\n", bngl.str().c_str() );
      
      int nSites = m->numSites();
      for (int sid=0 ; sid<nSites ; sid++) 
          {
          if (sid != 0) bngl << ",";

          // get the site name and cut out the actual site name, because
          // the name format is "mol(site)" to get the site identifiers unique.
          string sName = n->getName( NamesManager::SiteName , m->getSiteType(sid) );
          int p1 = sName.find_first_of("(");
          int p2 = sName.find_first_of(")");
          assert( p1 != string::npos  &&  p2 != string::npos );
          sName = sName.substr( p1+1,p2-p1-1 );
          
          bngl << sName;
          
          //printf("  current bngl: %s\n", bngl.str().c_str() );
          
          // find connections:
          //  to find out if we've already seen a connection we will use the names manager
          Molecule *mas    = m->getMoleculeAtSite(sid);
          if (mas!=NULL)
             {
             int otherUid = mas->getUniqueID ();
             int otherSid = m->getOtherSiteIDAtSite (sid);

             // two variations of the ident: me first, or the other first:
             stringstream connIdent1,connIdent2;
             connIdent1 << myUid << ":" << otherUid << "-" << sid << ":" << otherSid;  // me First
             connIdent2 << otherUid << ":" << myUid << "-" << otherSid << ":" << sid;  // other mol First
             //string connIdent1 = (ostringstream() << myUid << ":" << otherUid << "-" << sid << ":" << otherSid).str();  // me first
             
             //printf (" cI1: %s  - %d \n", connIdent1.str().c_str(), connectors.existsID(connIdent1.str()) );
             //printf (" cI2: %s  - %d \n", connIdent2.str().c_str(), connectors.existsID(connIdent2.str()) );
             
             
             int cid = -1;
             if (connectors.existsID(connIdent2.str()) == -1) 
                cid = connectors.getID(connIdent1.str()); // no other connect exists here -> lets put us-first in here:
             else 
                cid = connectors.getID(connIdent2.str()); // connIdent2 for the other-first already exists - so we take this conn identifier!
             
             bngl << "!" << cid;
             }
          
          // find modifications:
          int modi = m->getModificationAtSite(sid);
          if (modi != -1)
             {
             //bngl += "~"+( n->getName(NamesManager::ModificationName, modi) ); 
             bngl << "~" << ( n->getName(NamesManager::ModificationName, modi) );
             }
          }
      bngl << ")";
      }
  
  //printf("  current bngl: %s\n", bngl.str().c_str() );
  return bngl.str();
  }

///
///  Recursively generate a list of all the molecules constituting a connected 
///  molecule graph.
///  This approach might be problematic, when large Uids are used!
///
void Molecule::buildMolList (vector<Molecule*> &allMols, vector<bool> &usedUids)
  {
  int myUid = getUniqueID();
  
  //printf ("Molecule::buildMolList: myUid = %d, used.sz = %d\n", myUid, usedUids.size() );
  
  if (usedUids.size() < myUid+1)
     {
     //printf ("Molecule::buildMolList: resizing to %d\n", myUid+1);
     usedUids.resize( myUid+1, false );  // resize, standard value = -1
     }
  
  //printf ("Molecule::buildMolList: myUid = %d  -->  used=%s\n", myUid, usedUids[myUid]?"true":"false" );
  
  assert( usedUids[myUid] == false );
  usedUids[myUid] = true;
  allMols.push_back( this );
          
  // go over all sites, collecting the site names:
  for (int i=0 ; i<numSites() ; i++) 
      {
      Molecule *mas   = getMoleculeAtSite (i);
      
      if (mas != NULL) // we have a connection here!
         {
         // did we visit the other molecule yet?
         int otherUid = mas->getUniqueID();
         
         // go into that mol recursively:
         if (usedUids.size() < otherUid+1)
            usedUids.resize( otherUid+1, false );  // resize, standard value = -1
         if (usedUids[otherUid] == false) // unknown!
            mas->buildMolList ( allMols, usedUids );
         }
      }
  }



///
/// If usedUids is NULL, a vector will be created for this purpose:
///
bool Molecule::checkIfConnected (Molecule *other, vector<bool> *usedUids) 
  {
  //if (true) return false;
    
  int myUid     = getUniqueID();
  int targetUid = other->getUniqueID();
  
  //printf("    Molecule::checkIfConnected: %d <-> %d ?\n", myUid, targetUid);
  if (myUid == targetUid) return true;

  

  // take care we're not visiting a node twice
  vector<bool> *myUsedUids = usedUids;
  if (myUsedUids == NULL)
     myUsedUids = new vector<bool>(1);
  
  if (myUsedUids->size() < myUid+1)
     myUsedUids->resize( myUid+1, false );  // resize, standard value = -1
  assert( (*myUsedUids)[myUid] == false );
  (*myUsedUids)[myUid] = true;
  
  
  // go over all sites, collecting the site names:
  for (int i=0 ; i<numSites() ; i++) 
      {
      Molecule *mas   = getMoleculeAtSite (i);
      
      if (mas != NULL) // we have a connection here!
         {
         // did we visit the other molecule yet?
         int otherUid = mas->getUniqueID();
         
         if (otherUid == targetUid) return false;
      
         // go into that mol recursively:
         if (myUsedUids->size() < otherUid+1)
            myUsedUids->resize( otherUid+1, false );  // resize, standard value = false
         if ((*myUsedUids)[otherUid] == false) // unknown!
            {
            bool res = mas->checkIfConnected ( other, myUsedUids );
            if (res) // yes, we've found a connection!
               {
               if (usedUids == NULL) delete myUsedUids;       // clear memory if we're on the outermost recursion layer 
               return res;
               }
            }
         }
      }

  // nothing found -> return false!
  //printf("    Molecule::checkIfConnected: %d <-> %d   ==>  nothing found\n", myUid, targetUid);
  return false;
  }
  
  


}  // end of namespace!


