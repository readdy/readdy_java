//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <string.h>

#include "reactant_template.h"
#include "sr_error.h"
#include "templ_site.h"
#include "templ_molecule.h"


using namespace SRSim_ns;

int ReactantTemplate::inst_cnt = 0;

ReactantTemplate::ReactantTemplate () : geo(NULL), myUse(senselessRT)
  {
  inst_cnt++;
  }

/// Copy Constructor... getestet: funktioniert...
ReactantTemplate::ReactantTemplate( ReactantTemplate * t1 )
  {
  map<TemplSite*,TemplSite*> connection;
  //printf("AA\n");
  
  for (int i=0 ; i < t1->mols.size() ; i++)
      {
      TemplMolecule *newM = new TemplMolecule(t1->mols[i]->getType(), t1->mols[i]->getPattern());
      for (int j=0 ; j<t1->mols[i]->numSites() ; j++)
          {
          TemplSite *oldS = t1->mols[i]->getSite(j);
          TemplSite *newS = newM->addSite(oldS->getType(), oldS->getModif(), oldS->getPattern());

          // connect if nece to the equipartition theorem, it is possible tssary          
          if (oldS->getOther() != NULL)
             {
             // take the smaller pointer as identifier for the connection:
             TemplSite *connectionID = (oldS < oldS->getOther())?(oldS):(oldS->getOther());    
             if (connection.count(connectionID) == 0)  // Site not yet known.
                connection[connectionID] = newS;
             else  
                {  // so we've already had this connection: let's connect the new sites too.
                newS->connectToSite( connection[connectionID] );
                }
             }
          }
      addMolecule (newM);
      }
      
  geo    = t1->geo;
  myName = t1->myName;
  myUse  = t1->myUse;
  
  inst_cnt++;
  }       


/// Copy Constructor: Copy from arbitrary molecule structure
ReactantTemplate::ReactantTemplate( Molecule *m )
  {
  //printf ("ReactantTemplate::ReactantTemplate(Molecule *m) -> copy from arbitrary molecule\n");
    
  // generate a list of all the molecules:
  vector<Molecule*>      allMols;
  vector<TemplMolecule*> newMols;
  
  vector<bool>           usedUids;
  m->buildMolList(allMols, usedUids);       //    vector<Molecule*> &allMols, vector<bool> &usedUids
  
  //printf ("ReactantTemplate::ReactantTemplate(Molecule *m) -> list of %d mols.\n", allMols.size() );
  
  // first replicate all the molecules and sites:
  for (int i=0 ; i < allMols.size() ; i++)
      {
      TemplMolecule *newM = new TemplMolecule(allMols[i]->getType(), 0/*pattern = obligatory*/ );
      for (int j=0 ; j<allMols[i]->numSites() ; j++)
          TemplSite *newS = newM->addSite(allMols[i]->getSiteType(j), allMols[i]->getModificationAtSite(j), 0/*pattern = obligatory*/ );
      newMols.push_back( newM );
      addMolecule ( newM );
  
      //printf (" ReactantTemplate::ReactantTemplate(Molecule *m) -> added mol %d with %d sites.\n", i, newM->numSites() );
      }

  //printf ("ReactantTemplate::ReactantTemplate(Molecule *m) -> replicated molecules.\n" );

  // Now that all the molecules and sites exist, we connect them:  
  for (int i=0 ; i < allMols.size() ; i++)
      {
      for (int j=0 ; j<allMols[i]->numSites() ; j++)
          {
            
          //printf (" ReactantTemplate::ReactantTemplate(Molecule *m) -> connect mol %d and site %d.\n", i,j );
          
          // connect, if other site already exists:
          Molecule *otherMol = allMols[i]->getMoleculeAtSite(j);
          if (otherMol != NULL)
             {
             // find the index in allMols and newMols that otherMol corresponds to, store in k
             int otherMolIndex = -1;
             for (int k=0 ; k<allMols.size() ; k++)
                 {
                 //printf ("  ReactantTemplate -> test uid %d against uid %d.\n", otherMol->getUniqueID(), allMols[k]->getUniqueID() );
                 if ( otherMol->getUniqueID() == allMols[k]->getUniqueID() )
                    otherMolIndex = k;
                 }
             assert( otherMolIndex >= 0 );

             // connect from smaller to higher id:
             if (otherMolIndex < i) continue;  // skip of the other molecule is smaller than us
                 
             // now find the site index in otherMol, that 
             int otherSiteIndex = allMols[i]->getOtherSiteIDAtSite(j);
             assert( otherSiteIndex >= 0 );
          
             // and connect:
             //printf ("  ReactantTemplate::ReactantTemplate(Molecule *m) -> try to connect (%d,%d) to (%d,%d).\n",i,j, otherMolIndex, otherSiteIndex);
             //printf ("  ReactantTemplate::ReactantTemplate(Molecule *m) -> size of newMols(%d):  %d.\n", otherMolIndex, newMols[otherMolIndex]->numSites() );
             //printf ("  ReactantTemplate::ReactantTemplate(Molecule *m) -> size of newMols(%d):  %d.\n", i,             newMols[i]->numSites() );
             
             newMols[i]->getSite(j) -> connectToSite( newMols[otherMolIndex]->getSite(otherSiteIndex) );
             }
          }
      }
      
  //printf ("ReactantTemplate::ReactantTemplate(Molecule *m) -> replicated bonds.\n" );
      
      
  geo    = NULL;
  myName = "copiedMolecule";
  myUse  = observableRT;
  
  inst_cnt++;
  }       




/*
/**  This constructor steals the molecules from another template.
 *   WARNING: This does NOT creat a deep copy. The other molecule is killed.
 */
/*SRSim_ns::ReactantTemplate::steal( ReactantTemplate * t1 )
  {
  for (int i=0 ; i < t1->mols.size() ; i++)
      addMolecule (t1->mols[i]);
  t1->mols.resize(0);
  delete t1;
  }       */

ReactantTemplate::~ReactantTemplate ()
  {
  for (int i=0 ; i < mols.size() ; i++)
      delete mols[i];
  mols.resize(0);
  while (!markingHistory.empty()) markingHistory.pop();
  markedMolecules.clear();
  
  inst_cnt--;
  //printf ("me too!!!! Now there are %d ReactantTemplates left over!\n", inst_cnt);  
  }

void ReactantTemplate::addMolecule (TemplMolecule *m)
  {
  mols.push_back(m);
  m->setRealization(SRSIM_UNDEFINED);
  }
  
int            ReactantTemplate::numMolecules ()        {return mols.size();}
TemplMolecule *ReactantTemplate::getMolecule  (int idx) 
  {
  assert( idx < mols.size() ); 
  return mols[idx];
  }

  

/**  Writes a .dot file as input for the graphviz-tools
     so you can visualize the template as a pretty graph.
 */
void ReactantTemplate::writeTemplateToDotFile (NamesManager *names, string fname)
  {
  FILE *f = fopen (fname.c_str(), "w");
//  FILE *f     = stdout;
  char *idstr   = new char[100];
  int   nullcnt = 0;
  
  fprintf (f,"digraph G {\n");
  for (int i=0 ; i<mols.size() ; i++)
      {
      int    MTp    = mols[i]->getType();
      string MName  = names->getName(NamesManager::MoleculeSpeciesName, MTp);
      
      sprintf (idstr,"%d",mols[i]->getUniqueID());
      MName  = MName + "_" + idstr;
      
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
             TName = names->getName(NamesManager::MoleculeSpeciesName, TTp);
             sprintf (idstr,"%d",mols[i]->getMoleculeAtSite(j)->getUniqueID());
             TName = TName + "_" + idstr;
             noEdges = false;
             }
             
          int STp   = mols[i]->getSiteType(j);
          string SName = names->getName(NamesManager::SiteName, STp);

          fprintf (f, "\"%s\" -> \"%s\" [label=\"%s\"] ;\n",MName.c_str(), TName.c_str(), SName.c_str());          
          
          }
      
      if (noEdges && mols.size()>1) 
         {
         SRError::warning("This should not have happened! unconnected template graph in ReactantTemplate::writeDot! Uhhggg!!", fname.c_str());
         fprintf (f,"%s ; \n",MName.c_str());
         }
          
      }
  fprintf (f,"}\n");
  
  fclose (f);
  delete [] idstr;
  }
    
  
  
  
/**
  start has to be part of the TemplMolecule!!
 */
bool ReactantTemplate::matchSingleTM (Molecule *against, TemplMolecule *start, bool exact/*=false*/, int i_startSite/*=-1*/, int j_startSite/*=-1*/)
  {
  // Type:
  // if (against->getType() != start->getType()) return false; 
  // commented, as recTryMatching checks this too
    
  //cout << "\n\n now going recursive...\n";
  
  //return false;
  assert (against!=NULL);
  assert (start  !=NULL);
  bool ret;
   
  if (exact) ret = recTryMatchingExact ((TemplMolecule*)against, start, 1);
  else       ret = recTryMatching      (                against, start, 1, i_startSite, j_startSite);
  
  /*for (int i=0 ; !exact && i<mols.size() ; i++)
      {
      if (ret) printf ("_-|-_-|-_ Marked mol: %d with mol: %d \n",mols[i]->getUniqueID(), mols[i]->getRealization());
      //else     printf ("xxxxxxxxx Marked mol: %d with mol: %d \n",mols[i]->getUniqueID(), mols[i]->getRealization());
      //printf ("------------------------ \n");
      }*/
  
  unmarkTemplate();
  return ret;
  }


void ReactantTemplate::unmarkTemplate(TemplMolecule *until)
  {
  while (!markingHistory.empty())
    {
    TemplMolecule *t = markingHistory.top(); 
    if (t==until) return;           // if we reach 'until' we quit without unmarking this one...
    assert (t->getRealization() != SRSIM_UNDEFINED);
    markedMolecules.erase(t->getRealization());  // delete from map...
    markingHistory.pop();
    t->setRealization (SRSIM_UNDEFINED);
    }
  }


  
/**  returns true if matching successful,
     marks t with the id of m.
     ...
     i_startSite and j_startSite are use when we're trying to pin siteReactantTemplates to a 
     distinct site.
 */
bool ReactantTemplate::recTryMatching (Molecule *m, TemplMolecule *t, int recLayer, int i_startSite/*==-1*/, int j_startSite/*==-1*/)
  {
  int uniqueIDm = m->getUniqueID();
 
  // check type
  if (m->getType() != t->getType()) return false;
  
  // did we already use this molecule or template?
  if (markedMolecules.count(uniqueIDm) > 0 || t->getRealization() != SRSIM_UNDEFINED)
     return (t->getRealization() == uniqueIDm);

  //printf (" ########## recTryMatching(%d) mID=%d  tID=%d...  mAnz=%d, tAnz=%d\n",recLayer, m->getUniqueID(),t->getUniqueID(),m->numSites(), t->numSites());
       
  int nMsites = m->numSites();
  int nTsites = t->numSites();
  assert (nMsites >= nTsites);                      // num sites

  t -> setRealization( uniqueIDm );
  markingHistory.push( t );
  markedMolecules[uniqueIDm] = uniqueIDm;    // add this molecule to the map.
      
  // iterate over all sites of the template
  // write all the sites j of m into tPossibilities, which fit to TemplateSite i
  AssignmentConstructor ac(nTsites, nMsites);
  for (int i=0 ; i<nTsites ; i++)
      {
      int            needSiteType = t->getSiteType (i);
      int            needPattern  = t->getSite(i)->getPattern();
      int            needModif    = t->getModificationAtSite(i);
      TemplMolecule *nextTempl    = (TemplMolecule*) t->getMoleculeAtSite(i);
      
      int nextRealiz = (nextTempl==NULL)?(SRSIM_UNDEFINED):( nextTempl->getRealization());

      //printf ("looking for site: %d-type  mod=%d  pat=%d  i_sside=%d  j_sside=%d\n",needSiteType, needModif, needPattern, i_startSite, j_startSite);
            
      // check every site of the molecule m:
      for (int j=0 ; j<nMsites ; j++)
          {
          if (i==i_startSite && j_startSite!=j) continue;   // if we know one of the sites
          
          //printf ("A\n");
          // if it's of the required SiteType, 
          if (m->getSiteType(j) != needSiteType ) continue;
          
          // has the correct modification, 
          //printf ("B mol-site-modif=%d\n",m->getModificationAtSite(j));
          if (needModif!=SRSIM_UNDEFINED && m->getModificationAtSite(j) != needModif ) continue;
          
          // what about the bound state?
          //printf ("C\n");
          Molecule *nextMol = m->getMoleculeAtSite(j);

          // site should be bound to anything...          
          //printf ("D\n");
          if (needPattern == PAnyBond)
             {
             //printf ("D1\n");
             if (nextMol!=NULL) {ac.addPoss(i,j); continue;}
             else                                 continue;
             }
          
          // site should be unbound!
          //printf ("E\n");
          if (nextTempl == NULL) 
             {
             if (nextMol == NULL) {ac.addPoss(i,j); continue;} // Fine! That's it
             else continue;               // Nope, the site we're searching is unbound...
             }
          
          // do the remaining nodes fit to the template?
          // assert (nextTempl != NULL);
          //printf ("F\n");
          if (nextMol == NULL) continue;   // nope, this site is a dead end.
          
          // ok, maybe the other site is already known and should have a specific value?
          //printf ("G nextTemplRealiz=%d \n",nextRealiz);
          if (nextRealiz != SRSIM_UNDEFINED && nextRealiz != nextMol->getUniqueID())
             continue;
          //   {tPossibilities[i].push_back(j); continue;}   // Yehaa, that's what we've been searching for.
          
          // if we haven't succeeded or broken up by now, we'll have to dive into the next layer.
          //if (recTryMatching(nextMol, nextTempl)) {foundSite=true; break;}
          //tPossibilities[i].push_back(-j);  
          ac.addPoss(i,j, true);            // pushing true means we have to go into recursion for this element.
          
          }
      }
      
  // so, now we'll test our way through all the assignments...
  /*      |-        /-> a|
          |a      /     a|
     Templ|a ---/       a|Molecule
          |b            a|
          |b ---\       b|
          |-     \----> b|
          
   */
  
  // go to next possible assignment:
  //printf ("we'll have %d possible assignments to check:\n",ac.numAssignments());
  while (ac.incrementAssignment())
     {
     //printf ("assignment:\n");
                                         
     bool foundState = true;
     for (int i=0 ; i<nTsites ; i++)
         {
         int siteIDm = ac.getPoss(i);
         //printf ("Templ %d, siteIDm=%d\n",i,siteIDm);
         if (ac.getRecursivity(i) && ! recTryMatching( m->getMoleculeAtSite(siteIDm) , (TemplMolecule*)t->getMoleculeAtSite(i), recLayer+1 ) )
            { /*printf ("war nix.\n");*/foundState = false; break; }
         //else {printf ("war was.\n");}
         }
         
     if (foundState) {/*printf ("returning successful; \n");*/ return true;}
     else            unmarkTemplate(t);  // unmark until t
     
     }
      
  // none of the assignments returned successful -> we unmark ourself and quit.
  // ah, sorry, we don't unmark ourself... the unmarkTemplate in the next-lower function will do...
  //printf ("returning painful.; \n");
  return false; 
  }

  
  
  
  
  
/**     Used in the same sense as recTryMatching, but compares two different Templates for equality...
        so wildcards are not ignored but compared against each other... :)
  */  
bool ReactantTemplate::recTryMatchingExact (TemplMolecule *m, TemplMolecule *t, int recLayer)
  {
  int uniqueIDm = m->getUniqueID();
 
  // check type
  if (m->getType() != t->getType()) return false;
  
  // did we already use this molecule or template?
  if (markedMolecules.count(uniqueIDm) > 0 || t->getRealization() != SRSIM_UNDEFINED)
     return (t->getRealization() == uniqueIDm);

  //printf (" ########## recExactMatching(%d) mID=%d  tID=%d...  mAnz=%d, tAnz=%d\n",recLayer, m->getUniqueID(),t->getUniqueID(),m->numSites(), t->numSites());
       
  int nMsites = m->numSites();
  int nTsites = t->numSites();
  if (nMsites != nTsites) return false;        // here, we even need the same amount of sites...

  t -> setRealization( uniqueIDm );
  markingHistory.push( t );
  markedMolecules[uniqueIDm] = uniqueIDm;    // add this molecule to the map.
      
  // iterate over all sites of the template
  // write all the sites j of m into tPossibilities, which fit to TemplateSite i
  AssignmentConstructor ac(nTsites, nMsites);
  for (int i=0 ; i<nTsites ; i++)
      {
      int            needSiteType = t->getSiteType (i);
      int            needPattern  = t->getSite(i)->getPattern();
      int            needModif    = t->getModificationAtSite(i);
      TemplMolecule *nextTempl    = (TemplMolecule*) t->getMoleculeAtSite(i);
      
      int nextRealiz = (nextTempl==NULL)?(SRSIM_UNDEFINED):( nextTempl->getRealization());

      // check every site of the molecule m:
      for (int j=0 ; j<nMsites ; j++)
          {
          // type?
          if (m->getSiteType(j) != needSiteType ) continue;
          
          // modification, 
          if (m->getModificationAtSite(j) != needModif ) continue;
          
          // Pattern:
          if (m->getSite(j)->getPattern() != needPattern ) continue;
          
          // what about the bound state?
          TemplMolecule *nextMol = (TemplMolecule*) (m->getMoleculeAtSite(j));

          // site should be unbound?
          if (nextTempl == NULL) 
             {
             if (nextMol == NULL) {ac.addPoss(i,j); continue;} // Fine! That's it
             else continue;               // Nope, the site we're searching is unbound...
             }

          // nextTempl != NULL
          if (nextMol == NULL) continue;   // nope, this site is a dead end.
          if (nextRealiz != SRSIM_UNDEFINED && nextRealiz != nextMol->getUniqueID())
             continue;  // nope. Has different realization
          ac.addPoss(i,j, true);  // pushing true means we have to go into recursion for this element.
          }
      }
      
  // so, now we'll test our way through all the assignments...
  // go to next possible assignment:
  //printf ("we'll have %d possible assignments to check:\n",ac.numAssignments());
  while (ac.incrementAssignment())
     {
     //printf ("assignment:\n");
     
     bool foundState = true;
     for (int i=0 ; i<nTsites ; i++)
         {
         int siteIDm = ac.getPoss(i);
         //printf ("Templ %d, siteIDm=%d\n",i,siteIDm);
         if (ac.getRecursivity(i) && ! recTryMatchingExact( dynamic_cast<TemplMolecule*>(m->getMoleculeAtSite(siteIDm)) , dynamic_cast<TemplMolecule*>(t->getMoleculeAtSite(i)), recLayer+1 ) )
            { /*printf ("war nix.\n");*/foundState = false; break; }
         //else {printf ("war was.\n");}
         }
         
     if (foundState) {/*printf ("returning successful; \n");*/ return true;}
     else            unmarkTemplate(t);  // unmark until t
     
     }
  return false; 
  }

  
  
  
  
ReactantTemplate::AssignmentConstructor::AssignmentConstructor (int ts, int ms) : 
  nTsites(ts), nMsites(ms) 
  {
  tPossibilities = new vector<int >[nTsites];  // saves the possible incarnations of a template site.
  tRecursivity   = new vector<bool>[nTsites];  // saves if we have to check recursively
  
  tAssignments   = new int [nTsites]; memset (tAssignments, 0, sizeof(int )*nTsites);
  mAssignments   = new bool[nMsites]; memset (mAssignments, 0, sizeof(bool)*nMsites);
  
  if (nTsites>0) tAssignments[0]=-1;  // to reach the inicial case...
  }
  
ReactantTemplate::AssignmentConstructor::~AssignmentConstructor ()
  {
  delete [] tPossibilities;
  delete [] tRecursivity;
  delete [] tAssignments;
  delete [] mAssignments;
  }

int ReactantTemplate::AssignmentConstructor::numAssignments ()
  {
  int meng = 1;
  
  for (int i=0 ; i<nTsites ; i++)
      meng *= tPossibilities[i].size();
      
  return meng;
  }
  
/// needed by recTryMatching...
bool ReactantTemplate::AssignmentConstructor::incrementAssignment ()
  {
  bool accepted=false;
  while (! accepted)
     {     
     for (int i=0 ; i<nMsites ; i++)
         mAssignments[i] = false;
     tAssignments[0]++;
      
     
/*     printf ("  -- ");
     for (int i=0 ; i<nTsites ; i++)
         printf (" %d",tAssignments[i]);
     printf ("  -- \n");*/

      
     accepted=true;
     for (int i=0 ; i<nTsites ; i++)    // see if we have to flip more numbers...
         {
         if (tPossibilities[i].size() == 0) return false;
         if (tAssignments[i] >= tPossibilities[i].size())     // increment in next layer
            {
            tAssignments[i]=0;
            if (i<nTsites-1) tAssignments[i+1]++;
            else             return false;  // there are no more cases to visit.
            }
            
         int mID = tPossibilities[i][tAssignments[i]];
         if (mAssignments[mID] == true) accepted=false; // someone already used this mSite
         assert (mID < nMsites && mID>=0);
         mAssignments[mID] = true;
         }
         
     // is the assignment acceptable? -> returns true... we should try this case...
     // no stays in the loop until we've got an acceptable one or we quit because we ran out of cases...
     }
  return true;
  
/*  tAssignments[0]=0;
  return true;*/
  }
  
  
  
/// the recursive function to run through the graph - setting all the nodes to 1.
void recSetMoleculesTo1 (TemplMolecule *m1)
  {
  if (m1->getRealization() == -1) return;
  m1->setRealization (-1);
  
  for (int i=0 ; i<m1->numSites() ; i++)
      {
      TemplMolecule *m2 = (TemplMolecule*) m1->getMoleculeAtSite(i);
      if (m2 != NULL) recSetMoleculesTo1(m2);
      }
  }

  
bool ReactantTemplate::checkConnectivity ()
  {
  // set all realization values to -1... that's the unchecked molecules.
  for (int i=0 ; i<mols.size() ; i++)
      mols[i]->setRealization(1);
  
  recSetMoleculesTo1( mols[0] );

  bool connected = true;
  for (int i=0 ; i<mols.size() ; i++)
      {
      //printf (" i=%d  state=%d\n",i,mols[i]->getRealization());
      if (mols[i]->getRealization() == 1) 
         {
         connected = false;
         mols[i]->setRealization(SRSIM_UNDEFINED);
         }
      }
  
  return connected;
  }
  
  
/**  at the moment we are still using the constraint of only supporting 
     one site of type X per Molecule.
     ...
     
    outdated! We don't need this constraint any more!
 */
/*bool ReactantTemplate::checkOneSitePerMolecule ()
  {
  for (int i=0 ; i<mols.size() ; i++)
      {
      NamesManager n(true);
      
      for (int j=0 ; j<mols[i]->numSites() ; j++)
          {
          stringstream sbf("hallo"); 
                       sbf<<(mols[i]->getSiteType(j));
          
          int id = n.getID(sbf.str());
          if (id != j) return false;
          }
      }
  
  return true;
  }*/

  
  
/**   This function is used when creating Rules (by RuleBuilder::isBindRule) 
  *        put Templates A+B into the vector in
  *        if findMissingBond returns != NULL, then (*this) is the bound molecule A+B...
  *
  *   how to do this?  Go over every Bond, delete it, see if we obtain the one/two parts of in...
  *   
  *   At the moment we only allow 'in' to be of size = 2...   
  *        So now a Bond-rule cannot consist of only one Template...
  */
vector<TemplSite*> ReactantTemplate::findMissingBond( vector< ReactantTemplate * > in )
  {
  vector<TemplSite*> resolution;
  assert (in.size() == 2);
  
  // check same molecule numbers.
  int molCount = in[0]->mols.size() + in[1]->mols.size(); 
  if (molCount != mols.size()) return resolution;    // nope...       (resolution is still empty.)
  
  // check right number of bonds...
  int bondCount = in[0]->countBonds() + in[1]->countBonds();
  if (countBonds() != bondCount+1) return resolution;    // nope...
  
  
  // delete all the sites and check...
  for (int j=0 ; resolution.size()==0 && j<mols.size() ; j++)
      for (int k=0 ; resolution.size()==0 && k<mols[j]->numSites() ; k++)
          {
          if (mols[j]->getMoleculeAtSite(k) == NULL) continue;
          
          // save the old site-vectors:
          TemplSite     *s1 = mols[j]->getSite(k);
          TemplSite     *s2 = s1->getOther();
          TemplMolecule *m1 = mols[j];
          TemplMolecule *m2 = dynamic_cast<TemplMolecule*>(m1->getMoleculeAtSite(k));
          
          // delete the two site of the bond:
          s1->disconnect();
          
          // see what happens:  match using matchSingleTM...
          if (! checkConnectivity())  // we should obtain two separated partial-graphs now.
             {
             // find the molecules at the connection points.
             int in_m[] = {SRSIM_UNDEFINED,SRSIM_UNDEFINED};
             for (int p=0 ; p < in[0]->mols.size() ; p++)          
                 if ( matchSingleTM( in[0]->mols[p], m1, true ) )  // here we don't have to
                    { in_m[0]=p; break;}                           // exchange m1/m2, because
             for (int p=0 ; p < in[1]->mols.size() ; p++)              // we'll go through the other
                 if ( matchSingleTM( in[1]->mols[p], m2, true ) )  // combination anyway int there
                    { in_m[1]=p; break;}                           // j/k - loop.                     
                    
             if (in_m[0]!=SRSIM_UNDEFINED && in_m[1]!=SRSIM_UNDEFINED)
                { //FOUND IT! We. Are. Heroes!
                
                // now just find the sites which are to be connected.
                for (int q=0 ; q<2 ; q++)
                    for (int p=0 ; p < in[q]->mols[in_m[q]]->numSites() ; p++ )
                        {
                        TemplSite *qSite = (q==0)?(s1):(s2);
                        TemplSite *pSite = in[q]->mols[in_m[q]]->getSite(p);
                        if (qSite->getType()    == pSite->getType() &&
                            qSite->getPattern() == pSite->getPattern() &&
                            qSite->getModif()   == pSite->getModif())
                            { resolution.push_back (pSite); break; }
                        }
                
                // one of our own template sites is needed too:
                //   A + B -> A.B ... auch das A.B fiech ist ein siteTemplate!
                resolution.push_back (s1);
                        
                // if we arrive here, we should have a resolution!!
                assert (resolution.size() == 3);
                }
             }
          
          // restore sites:
          s1->connectToSite(s2);
          }
          
  assert (resolution.size() == 0 || resolution.size()==3);   // can be 0 or 3
  return resolution;
  }

int ReactantTemplate::countBonds( )
  {
  int count=0;
  for (int i=0 ; i<mols.size() ; i++)
      for (int j=0 ; j<mols[i]->numSites() ; j++)
          if (mols[i]->getMoleculeAtSite(j) != NULL) count++;
   
  assert (count%2 == 0);
  return (count/2);
  }

class TemplateGeo * ReactantTemplate::getGeo( )
  {
  return geo;
  }

void ReactantTemplate::setGeo( class TemplateGeo * _tgeo )
  {
  geo = _tgeo;
  }

string ReactantTemplate::getName( )
  {
  return myName;
  }

void ReactantTemplate::setName( string _name )
  {
  myName = _name;
  }

bool SRSim_ns::ReactantTemplate::isUsableAs( RTUse _use )
  {
  //printf ("RT %s with usability=%d against requested %d\n", getName().c_str(), myUse, _use);
  bool usable = ((RTUse)((int)myUse & (int)_use) != senselessRT);
  //printf ("RT %s says usable=%d\n", getName().c_str(), usable);
  //assert(false);
  return usable;
  }

void SRSim_ns::ReactantTemplate::addUse( RTUse _use )
  {
  //printf ("  %s MyOldUse = %d ...", getName().c_str(), myUse);
  myUse = (RTUse)((int)myUse | (int)_use);
  //printf ("      MyNewUse = %d ...\n", myUse);
  }


  
  
  
