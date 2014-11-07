//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <fstream>
#include <string.h>
#include <string>
#include <cstdlib>

#include "uniform_distribution_start_state.h"
#include "multi_mol_reactant_template.h"
#include "site_reactant_template.h"
#include "bng_rule_builder.h"
#include "sr_error.h"
#include "templ_site.h"
#include "templ_molecule.h"
#include "rule_set.h"

using namespace SRSim_ns;
using namespace std;


BNGRuleBuilder::BNGRuleBuilder (SRModel *_into)
  {
  into  = _into;
  names = into->names;
  }
  
BNGRuleBuilder::~BNGRuleBuilder ()
  {
  }

/**
  *    This function will not register the molecules at the MoleculeTypeManager! 
  *      ( instead, this is done by BNGRuleBuilder::readFile below.)
  */ 
ReactantTemplate* BNGRuleBuilder::parseBNGTemplate (string t)
  {
  vector<string>         parts = partition (t,"(,).!~");
  NamesManager           n_bonds(true);
  vector<TemplSite*>     sites;
  vector<TemplMolecule*> mols;
  
  // create start-molecule:
  int typeID = 0;
  int siteID = 0;
  
  //cout << "Start Building Template ..."<<endl;
  
  int i = 0;
  while (i<parts.size())   // loop over new Molecules
     {
     //printf ("This should be a molecule: %s\n", parts[i].c_str());
     
     if (parts[i][0] == '.') parts[i].erase(0,1);
     typeID = names->getID (NamesManager::MoleculeSpeciesName, parts[i]);
     TemplMolecule *m = new TemplMolecule (typeID, PObligatory);
     mols.push_back(m);
     i++;
     
     //printf ("Created Molecule %s\n",parts[i-1].c_str());
     
     while (i<parts.size() && (parts[i][0] == '(' || parts[i][0] == ','))  // part i has sites
        {
        //printf ("  part[%d] is '%s'\n",i,parts[i].c_str());
        
        parts[i].erase(0,1);     // erase trailing ',' or '('
        if (parts[i].size() == 0)
           { 
           SRError::warning ("BNGRuleBuilder::parseBNGTemplate: are you sure about the template string: "+t+" - there seems to be an empty component...?");
           i++; 
           continue; 
           }
           // 
           //assert (parts[i].size() > 0);
        
        // in BNG each Molecule opens its own namespace for 
        // site names -> so we need to discriminate between
        // sites of different molecule species... or don't we??
        
        string complex_sitename(names->getName(NamesManager::MoleculeSpeciesName,typeID)+"("+parts[i]+")");
        //printf ("Site called %s!\n", complex_sitename.c_str());
        siteID = names->getID (NamesManager::SiteName, complex_sitename);
        
        int modID     = -1;
        int connectID = -1;
        int pattern   = PObligatory;
        i++;
        
        while (i<parts.size() && parts[i][0] != ')' && parts[i][0] != ',') // bonds and modifications
          {
          //printf ("    Mod/Con loop: c=%c\n",parts[i][0]);
          
          if (parts[i][0] == '~') // modification
             {
             if (modID != -1) SRError::critical ("improper BNG-String in BNGRuleBuilder::parseBNGTemplate; double modification!",t.c_str());
             parts[i].erase(0,1);
             modID = names->getID (NamesManager::ModificationName, parts[i]);
             i++;
             }
          else if (parts[i][0]=='!') // connection
             {
             if (pattern == PAnyBond || connectID != -1) SRError::critical ("improper BNG-String in BNGRuleBuilder::parseBNGTemplate; multiple bonds per site!",t.c_str());
             
             parts[i].erase(0,1);
             if (parts[i].size()==1 && parts[i][0]=='+')
                pattern = PAnyBond;   // has to have any bond.
             else connectID = n_bonds.getID (parts[i]);    // this is just a new names-manager, not the main-one!
                
             i++;
             
             //if (pattern == PAnyBond) printf ("Juhu ein AnyBond...\n");
             }
          else
             {
             SRError::critical ("improper BNG-String in BNGRuleBuilder::parseBNGTemplate; expected ~ or ! after site name instead of "+parts[i]+" in template "+t);
             }
          //else {printf ("What is this: %s\n",parts[i].c_str()); /*i++*/;}
          }
        
        TemplSite *s = m->addSite (siteID, modID, pattern);
        
        //printf ("   Created Site %s  (tp=%d, mod=%d, connect=%d)\n",names->getName(NamesManager::SiteName, siteID).c_str(), siteID, modID, connectID);
        
        
        // now do the connections if necessary - if it's a single molecule we don't need to,
        // but let's check the connectivity later anyway!
        if (connectID!=-1)     
           {
           if (connectID < sites.size()) // other part already exists
              {
              try   { s->connectToSite( sites[connectID] ); }
              catch (SRException e) 
                { SRError::critical ("Seems you've got a double-use of a connection-name in Template ",(t+"::"+n_bonds.getName(connectID)).c_str()); }
              }
           else   // push s onto the sites vector
              sites.push_back (s);
           }
        }
        
     if (i<parts.size() && parts[i][0] == ')') i++;
     }
     
  // are all sites with connections really connected?
  for (int i=0 ; i<sites.size() ; i++)
      {
      if (sites[i]->isConnected()) continue;
      sites[i]->setPattern( PAnyBond );
      SRError::warning ("Site should be bound but connection id never reappears. Handling site as 'any bond possible' (like A(b!+) ).", \
                        t.c_str() );
      }
  
  // let's build a template...
  ReactantTemplate *rt = new MultiMolReactantTemplate();
  for (int i=0 ; i<mols.size() ; i++)
      rt-> addMolecule (mols[i]);
  rt->setName(t);
  //printf ("Written the name '%s'\n", t.c_str());
  
  // ...and perform some sanity checks...
  if (! rt->checkConnectivity ()) 
     SRError::critical ("improper BNG-String in BNGRuleBuilder::parseBNGTemplate; a template should constitute a connected graph!",t.c_str());
     
  //if (! rt->checkOneSitePerMolecule ()) 
  //   SRError::critical ("improper BNG-String in BNGRuleBuilder::parseBNGTemplate; sites should be unique per molecule!",t.c_str());
  
  //cout << "    Building Template done."<<endl;
      
  return rt;
  }


/**   Parses the 'reaction rules' - section of a bng files and converts it to a RuleSet...
 * Important: 1) some of the bng-files I found didn't have line numbers in front
 *               of the reactions - Those files will produce errors.
 *            2) As in BNG Reactants have to be specified without spaces
 *            3) unlike BNG, Reactants, Pluses and Arrows have to be separated 
 *               by spaces.
 */
 //   bit of an ugly function with 'phase' variable... but anyway - now it works :)
void BNGRuleBuilder::readFile (string fname)
  {    
  // where to read the data into?
  RuleSet *rs    = into->ruleset;
  
  char      line[1000];
  ifstream  f;
  
  f.open (fname.c_str(), ifstream::in);
  if ( ! f.is_open() )
    SRError::critical ("cannot open file in BNGRuleBuilder::readBNGFile",fname.c_str());
  
  // read special sections:
  int savedFPos = f.tellg();
  parseVarsSection (&f);
  parseStartStateSection(&f);
    
  // locate rules section
  while (f.good())
    {
    f.getline(line,1000);
    //cout << "__"<<line<<"__\n";
    if (0 == strcmp(line,"begin reaction rules")) break;
    }
  if (! f.good())
     SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. Has no content in reaction rules-section.",fname.c_str());

  
  // go through content...
  vector<ReactantTemplate*> proto_in, proto_out;
  double                    rate1   , rate2;
  bool     reversible = false; 
  bool     contLine   = false;    // line continues next time...
  int      phase      = 0;        // phase 0   : fresh
                                  //       10  : left  side of reaction,    15 after read molecule. ~ need '+' to restre to 10/50... :)
                                  //       50  : right side of reaction,    55 after read molecule.
                                  //       56  : after having read first parameter.
                                  //       80  : after Comment:
                                  //       100 : done. proceed to next line.
  while ( f.good() )
    {
    f.getline(line,1000);
    if (0 == strcmp(line,"end reaction rules")) break;
    
    // here's a Reaction line:
    // let's split it with std-C functions...
    contLine = false;
    char *parts = strtok(line," \ŧ");
    for (int i=0 ; phase<100 && parts!=NULL ; i++)
        {
        //cout << "__"<<i<<"___"<<parts<<"__\n";

        if (i==0 && phase==0)   // do nothing: the first thingy's only a line-number
           {
           if (parts[0]=='#')   // we've got a comment... and now what?
              phase=100;        // means proceed to next line.
           else if (! isNeatNumber(parts) )
              {
              SRError::warning ("Bad file in BNGRuleBuilder::readBNGFile. Maybe a line number before the reactions has been forgotten.",line);
              // let's just be angry and skip the missing line number...
              phase = 10; reversible=false; 
              rate1 = -1; rate2 = -1;
              continue;
              }
           else
             { 
             phase = 10; reversible=false; 
             rate1 = -1; rate2 = -1;
             }  // clear - a new rule begins.
           }
        else if (parts[0]=='#')                         {phase=80; /*assert(false)*/}  // comments don't work yet...
        else if (strcmp(parts, "+" ) == 0 && phase==15) {phase=10;}
        else if (strcmp(parts, "+" ) == 0 && phase==55) {phase=50;}
        else if (strcmp(parts, "->") == 0 && phase< 80) {phase=50;}
        else if (strcmp(parts,"<->") == 0 && phase< 80) {phase=50;reversible=true;}
        else if (strcmp(parts, "\\") == 0 && phase< 80) {contLine=true;}        // line-break... continue in next line.
        else if (phase==10 || phase==50) // ok, it's a reactant...
           {
           // if the molecule's name is not "null", it will be interpreted as a template!
           if ( strcmp(parts,"null")!=0 )
              {
              // create the template
              ReactantTemplate *rt = parseBNGTemplate(string(parts));
              //printf ("      new RT: %s\n", parts);
              
              // register the molecule:
              into->mtm->registerAllMolecules (rt);
              
              // build the reaction in/out list
              if (phase==10) { proto_in .push_back(rt); }
              if (phase==50) { proto_out.push_back(rt); }
              }
           phase+=5;
           }
        else if (phase==55||phase==56)  // we can parse parameters now...
           {
           string s(parts);
           if (s.find(',') != string::npos) // no ',' read.
              {
              if (s.find(',')+1 == s.size() )  // is last character!
                 s.erase(s.size()-1, 1);
              else  // there's a second parameter behind.
                 {
                 string subStr = s.substr(s.find(',')+1, 999); // behind the ','
                 int id = names->getID( NamesManager::VariableName, subStr );
                 if (id>=vars.size()) SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. We didn't recognize the kinetic parameter here.",subStr.c_str());
                 rate2=vars[id];
                 s = s.substr(0, s.find(','));
                 }
              }
           if (s.size() > 0) // maybe we delete a single comma.
              {
              int id = names->getID( NamesManager::VariableName, s );
              if (id>=vars.size()) SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. We didn't recognize the kinetic parameter here.",s.c_str());
              if (phase==55) rate1=vars[id];
              else           rate2=vars[id];
              }
           //printf ("Read %s: rate1=%f  rate2=%f   p0='%s'  p1='%s'\n", parts, rate1, rate2, s.c_str());
           phase=56;
           }
        

        parts = strtok(NULL," \t");

        if (parts==NULL && !contLine && phase<100) 
           {
           assert( proto_in.size() > 0 || proto_out.size() > 0 );
           
           // What rule-tp will it be?
           RuleTp::RuleTpType rtype = TransformTemplates(proto_in, proto_out);
           
           // rules:
           vector<int> rid = rs->addNewRule(proto_in, proto_out, rtype, reversible);
           
           // rates:
           assert (rid.size() > 0);
           if (rid.size() >= 1)
              { if (rate1<0) SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. We don't have a kinetic parameter here.",fname.c_str());
                else into->kinetics->setRate (rid[0], rate1); }
           if (rid.size() >= 2)
              { if (rate2<0) SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. We don't have a second kinetic parameter here.",fname.c_str());
                else into->kinetics->setRate (rid[1], rate2); }
           
           //cout << "###new rule added.\n";
           phase=0;  // done. begin from start...
           
           // delete reactant Templates (ruleset 's got it's own copies):
           for (int i=0 ; i<proto_in .size() ; i++) delete proto_in [i];
           for (int i=0 ; i<proto_out.size() ; i++) delete proto_out[i];
           proto_in.clear(); proto_out.clear(); 
           }

        }
    if (phase==100) phase=0;  // new line - new luck... and maybe no comment...
    
    
    
/*    vector<string> parts = partition(string(line), " ");
    for (int i=0 ; i<parts.size() ; i++)
        cout << "__"<<i<<"___"<<parts[i]<<"__\n";*/
    }
  
  // check that we arrived at the right end - part.
  if (0 != strcmp(line,"end reaction rules"))
     SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. Haven't found the 'end reaction rules' tag.",fname.c_str());

  // read special sections: observables
  f.seekg (savedFPos);
  parseObservableSection(&f);
  
  f.close();
  
  printf (" SRSim::BNGRuleBuilder::readBNGFile: Have read %d rules. There are %d templates.\n", rs->numRules(), rs->numTemplates());
  
  //rs->getRT(2)->writeTemplateToDotFile(names, "spassT.dot");
  }

  
 
  
vector<string> BNGRuleBuilder::partition (string in, string separators)
  {
  vector<string> out;
  
  // partition string into subparts: A(glib,glob!2).Be(glib!2,klomp).BBK
  //                                 01234567890123456789012345678901234
  //                    goes to      A; (glib; ,glob; !2; ); .Be; (glib; !2; ,klomp; ); .BBK;
  int lastpos = 0;
  for (int i=0 ; i<in.size() ; i++)
      for (int j=0 ; j<separators.size() ; j++) 
          {
          if (in[i] == separators[j])
             {
             if (i-lastpos == 0) SRError::critical ("improper BNG-String passed to BNGRuleBuilder::partition",in.c_str());
             out.push_back( in.substr(lastpos, i-lastpos) );
             lastpos = i;
             }
          }
  
  // fetch the last part which wasn't terminated by a separating character!
  if (lastpos != in.size()) out.push_back( in.substr( lastpos,  in.size()-lastpos) );

  // test-output:
  //cout << "cutting "<<in<<" to strings: \n";
  //for (int i=0 ; i<out.size() ; i++)
  //    cout << i <<" ---"<<out[i]<<"--- \n";
  
    
  return out;
  }


void SRSim_ns::BNGRuleBuilder::parseVarsSection( ifstream *f )
  {
  char line[1000];
  
  // save f-position
  int savedFPos = f->tellg();
  assert (savedFPos != -1);
  
  // locate parameters section
  while (f->good())
    {
    f->getline(line,1000);
    //cout << "__"<<line<<"__\n";
    if (0 == strcmp(line,"begin parameters")) break;
    }
  if (! f->good())
     SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. Has no parameters-section.");
     
  // read content:
  while ( f->good() )
    {
    f->getline(line,1000);
    if (0 == strcmp(line,"end parameters")) break;
    
    char *parts = strtok(line," \t");
    if (parts == NULL) continue;  // empty line...
    
    // maybe there's a line-Number?
    if (isNeatNumber(parts)) parts = strtok(NULL," \t");
    
    // ok, we've got a variable name here:
    int nid = names->getID(NamesManager::VariableName, parts);
    char *varName = parts;
    
    parts = strtok(NULL," \t");
    if (parts == NULL)         continue;  // something wrong: no value behind name...
    if (! isNeatNumber(parts)) continue;  // something wrong: badw value behind name..
    if (nid >= vars.size()) vars.resize(nid+1, -1);
    vars[nid] = atof (parts);
    
    //printf ("Var(%d) %s = %f \n", nid, varName, vars[nid]);
    }
  
  // finish...
  if (0 != strcmp(line,"end parameters"))
     SRError::critical ("Bad file in BNGRuleBuilder::readBNGFile. Haven't found the 'end parameters' tag.");
     
  // let's reset the stream:
  f->seekg (savedFPos);
  printf (" SRSim::BNGRuleBuilder::parseVarsSection: Have read %d vars.\n", vars.size() );
  }

  
void SRSim_ns::BNGRuleBuilder::parseStartStateSection( ifstream *f )
  {
  char line[1000];
  
  // save f-position
  int savedFPos = f->tellg();
  assert (savedFPos != -1);
  
  // locate parameters section
  while (f->good())
    {
    f->getline(line,1000);
    //cout << "__"<<line<<"__\n";
    if (0 == strcmp(line,"begin species")) break;
    }
  if (! f->good())
     SRError::critical ("Bad file in BNGRuleBuilder::parseStartStateSection. Has no species-section.");
     
  // read content:
  int cnt=0;
  while ( f->good() )
    {
    f->getline(line,1000);
    if (0 == strcmp(line,"end species")) break;
    
    char *parts = strtok(line," \t");
    if (parts == NULL) continue;  // empty line...
    
    // maybe there's a line-Number?
    if (isNeatNumber(parts)) parts = strtok(NULL," \t");
    
    // this should be the template now:       
    char *molString = parts;
    ReactantTemplate *rt = parseBNGTemplate(string(molString));
    rt->addUse(ReactantTemplate::creatableRT);
    into->mtm->registerAllMolecules (rt);
    
    // now, what's the name of the parameter?
    int num = 0;  // how many of this kind should be created?
    parts = strtok(NULL," \t");
    if (parts == NULL)
       SRError::critical ("Bad file in BNGRuleBuilder::parseStartStateSection. didn't find number of molecules to start with.", molString);
    if (isNeatNumber(parts))
       num = atoi(parts);
    else
       {
       int nid = names->existsID(NamesManager::VariableName, parts);
       if (nid == -1)
          SRError::critical (string("Sorry, we didn't find a value for the parameter. \"") + parts +"\" .", line);
       num = (int)vars[nid];
       }

    UniformDistributionStartState *udss = dynamic_cast<UniformDistributionStartState*>(into->sstate);
    udss->addNumber(rt, num);
    cnt++;
    delete rt; 
    printf ("StartState(%d) x %d\n",cnt++, num);
    }
  
  // finish...
  if (0 != strcmp(line,"end species"))
     SRError::critical ("Bad file in BNGRuleBuilder::parseStartStateSection. Haven't found the 'end species' tag.");
     
  // let's reset the stram:
  f->seekg (savedFPos);
  printf (" SRSim::BNGRuleBuilder::parseStartStateSection: Have read %d startStates.\n", cnt );
  }

  
  
    
bool BNGRuleBuilder::isNeatNumber (char *in)
  {
  stringstream sst;
  sst << in;
  int n; sst >> n; 
  return (!sst.fail());
  }

void SRSim_ns::BNGRuleBuilder::parseObservableSection( ifstream * f )
  {
  char line[1000];
  
  // save f-position
  int savedFPos = f->tellg();
  assert (savedFPos != -1);
  
  // locate parameters section
  while (f->good())
    {
    f->getline(line,1000);
    //cout << "__"<<line<<"__\n";
    if (0 == strcmp(line,"begin observables")) break;
    }
  if (! f->good())  // we don't have observables...
     {
     f->seekg (savedFPos);       // so don't worry, just go home...
     f->clear ();
     return;
     }
     
  // read content:
  int cnt=0;
  while ( f->good() )
    {
    f->getline(line,1000);
    if (0 == strcmp(line,"end observables")) break;
    
    //cout << "     Observables____"<<line << endl;
    
    char *parts = strtok(line," \t");
    if (parts == NULL) continue;  // empty line...
    if (parts[0] == '#') continue; // comment
    
    // maybe there's a line-Number?
    if (isNeatNumber(parts)) parts = strtok(NULL," \t");

    if (0 != strcmp(parts,"Molecules")) continue;
    parts = strtok(NULL," \t");     // group name wich we will ignore... not any more!
    string groupName(parts);
    parts = strtok(NULL," \t");     // Template-Name...
    if (parts == NULL)  // empty line...
       SRError::critical ("Bad line in BNGRuleBuilder::parseObservableSection.");
    
    // Now, which are our molecules?
    cnt ++;
    char *myRtName = parts;
    bool found = false;
    for (int rt=0 ; rt<into->ruleset->numTemplates() ; rt++)
        {
        string rtName = into->ruleset->getRT(rt)->getName();
        if ( rtName.compare(myRtName) == 0 )
           {
           into->ruleset->getRT(rt)->addUse( ReactantTemplate::observableRT );
           into->observer->add(groupName,rt);
           found = true;
           break;
           }
        }
        
    if (!found)   // create Template to observe...
       {
       ReactantTemplate *rt = parseBNGTemplate(string(myRtName));
       
       // a SRT is searchable more efficiently than an MMRT.    -->     convert to SRT
       if (rt->getMolecule(0)->numSites() > 0)  
          {
          ReactantTemplate *rt2 = new SiteReactantTemplate (rt, rt->getMolecule(0)->getSite(0));
          delete rt;
          rt = rt2;
          }
                                
       rt->addUse( ReactantTemplate::observableRT );
       int tid = into->ruleset->getTemplateID( rt );
       into->observer->add(groupName,tid);
       delete rt;
       }
    
    }
  
  // finish...
  if (0 != strcmp(line,"end observables"))
     SRError::critical ("Bad file in BNGRuleBuilder::parseObservableSection. Haven't found the 'end observables' tag.");
     
  // let's reset the stram:
  f->seekg (savedFPos);
  printf (" SRSim::BNGRuleBuilder::parseObservableSection: Have read %d Observables.\n", cnt );
  }


