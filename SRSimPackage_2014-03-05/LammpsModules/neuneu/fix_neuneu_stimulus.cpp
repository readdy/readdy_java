/* ----------------------------------------------------------------------
   LAMMPS - Large-scale Atomic/Molecular Massively Parallel Simulator
   http://lammps.sandia.gov, Sandia National Laboratories
   Steve Plimpton, sjplimp@sandia.gov

   Copyright (2003) Sandia Corporation.  Under the terms of Contract
   DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government retains
   certain rights in this software.  This software is distributed under 
   the GNU General Public License.

   See the README file in the top-level LAMMPS directory.
------------------------------------------------------------------------- */

#include <math.h>
#include <sys/types.h>

#include "fix_neuneu_stimulus.h"
#include "atom.h"
#include "update.h"
#include "error.h"
#include "atom_vec_srsim.h"

#include <SRSim/bng_rule_builder.h>
#include <SRSim/molecule_type_manager.h>
#include <SRSim/rule_set.h>
// #include <SRSim/bng_rule_builder.h>



#include <iostream>
#include <fstream>
#include <sstream>

using namespace LAMMPS_NS;
using namespace std;
using namespace SRSim_ns;

/* ---------------------------------------------------------------------- */


/**
 *  FixPushMembrane will push particles outwards until the yzRadius is reached.
 *  Parameters: 
 *   "<id>  <group> neuneu/stimulus <stimtype> <stimFile>
 *       <stimtype>: static_compare
 *
 *
 */
FixNeuneuStimulus::FixNeuneuStimulus(LAMMPS *lmp, int narg, char **arg) :
  Fix(lmp, narg, arg)
{
  stimDelay         = 100.0;
  outpDelay         = 100.0;
  nSkipTimesteps    = 100;
  excitable_site_id = -1;
  
  iStimPhase = 0;
  iSubPhase  = 0;
  phaseStart = 0;
  
  outpAccumGood = 0.0;
  outpAccumBad  = 0.0;

  if (narg != 5) error->all("Illegal fix neuneu/stimulus command");
  if (string(arg[3]) != "static_compare") error->all("at the moment, the only known neuneu/stimulus class stimtype is: static_compare.");

  // load stim file data:
  loadStimFile(arg[4]);
  
  // since we might have added new templates now, we should recalculate the complete template structure:
  // also we have to use setRuleSys to adjust some array lengths...
  int nlocal = atom->nlocal;
  AtomVecSRSim *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  avec->setRuleSys( avec->srmodel );
  for (int i=0 ; i<nlocal ; i++)
      {
      avec->updateSingleTemplateData( i );
      }
  printf ("FixNeuneuStimulus::FixNeuneuStimulus ... updated Template Affiliations.\n");
  
}

/* ---------------------------------------------------------------------- */

int FixNeuneuStimulus::setmask()
{
  int mask = 0;
  mask |= INITIAL_INTEGRATE;
  return mask;
}

/* ---------------------------------------------------------------------- */

// initialization before a run
void FixNeuneuStimulus::init()
{
  // 1. run over all particles - remember which ones are our inputs, which ones are the outputs
  //     -> inTags, outTags
  
  //AtomVecSRSim *avec = dynamic_cast<AtomVecSRSim*>(atom->avec);
  //avec->srmodel->printModelInfo();
  
}

/* ---------------------------------------------------------------------- */

// called before the 1st timestep!
void FixNeuneuStimulus::setup(int vflag)
{
  ofstream f( stimResultFile.c_str() );    // pfffft... how stupid is this?? What doesn't ifstream have a string-based constructor???
  if (! f.is_open() )
    error->all( "FixNeuneuStimulus::setup - cannot write output file. " );
  f << "Time outpAccumGood outpAccumBad \n";
  f.close();
  
  //phaseStart = update->ntimestep;
}

/* ---------------------------------------------------------------------- */


void FixNeuneuStimulus::initial_integrate(int vflag)
{
  // see if we have the hangup anyway...
  //return;
  
  // skip, if we're not in nevery
  if (iStimPhase >= (int)stimTimes.size())  return;
  
  // see if we have to switch the phases:
  double time = update->ntimestep;
  if (update->ntimestep % nSkipTimesteps != 0) return;
  //printf ("    FixNeuneuStimulus::initial_integrate - time = %f   phase = %d    subPhase = %d\n", time, iStimPhase, iSubPhase);
  
  // do we have to advance the phases?
  if      (iSubPhase==0 && time-phaseStart > stimDelay)             {iSubPhase++;}
  else if (iSubPhase==1 && time-phaseStart > stimDelay+outpDelay)   {iSubPhase++;}
  else if (iSubPhase==2 && time-phaseStart > stimTimes[iStimPhase]) 
     {
     // evaluate output:
     printf ("\nStimPhase %d results:\n", iStimPhase);
     printf ("  outpAccumGood == %f\n", outpAccumGood);
     printf ("  outpAccumBad  == %f\n", outpAccumBad);
     
     ofstream f( stimResultFile.c_str(), ios_base::app );    // pfffft... how stupid is this?? What doesn't ifstream have a string-based constructor???
     if (! f.is_open() )
        error->all( "FixNeuneuStimulus::initial_integrate - cannot write output file. " );
     f << time << " " << outpAccumGood << " " << outpAccumBad << "\n";
     f.close();
     
     // reset output counters: 
     outpAccumGood = 0;
     outpAccumBad  = 0;
     
     // switch to next phase:
     iSubPhase=0; 
     iStimPhase++; 
     phaseStart=time;
     }
  if (iStimPhase >= (int)stimTimes.size()) return;
  
  // input
  AtomVecSRSim *avec   = dynamic_cast<AtomVecSRSim*>(atom->avec);
  int           nlocal = atom->nlocal;
  if (iSubPhase >= 1)
     for (int i=0 ; i<nlocal ; i++)
         {
         //for (int k=0 ; k<avec->all_templs[i].size() ; k++)
         //    printf ("mol %3d ->  t %d\n", i, avec->all_templs[i][k]);
        
         // is i an input-molecule? For input channel j?
         for (uint j=0 ; j<inptPattern.size() ; j++)
             {
             // can we skip this pattern? Is it inactive in this phase anyway?
             if (stimData[iStimPhase][j] == false) continue;
            
             // does this molecule fit the template?
#ifndef USE_TEMPL_AFFIL_MANAGER
             if (avec->fitsToTemplate(i, inptPattern[j] ))
#else
             if ( avec->affiliations.belongsToTempl(inptPattern[j],i) )
#endif
               {
               // is it already stimulated?
               if ( avec->site_modified[i][ excitable_site_id ]  ==  excite_modification ) continue;
              
               // now stimulate this neuron!
               avec->site_modified[i][ excitable_site_id ] = excite_modification;
              
               // recalculate this neuron's template affiliation
               avec->updateSubgraphTemplateData( i );
              
               //printf ("Stimulated neuron %d \n", i);
               }
             }
         }
      
  // output:
  if (iSubPhase == 2)
     for (int i=0 ; i<nlocal ; i++)
         {
         for (uint j=0 ; j<outpPattern.size() ; j++)
#ifndef USE_TEMPL_AFFIL_MANAGER
             if (avec->fitsToTemplate(i, outpPattern[j] ))
#else
             if ( avec->affiliations.belongsToTempl(outpPattern[j],i) )
#endif
                {
                // if the expected output == -1   --> means we should ignore this one!
                if (outpExpected[iStimPhase][j] == -1) continue;
              
                // is it already stimulated?
                int mod = avec->site_modified[i][ excitable_site_id ];
                
                // is this good or bad?
                bool exci = ( excited_modification.count( mod ) == 1 );
                
                // if there's no excitation, we can't say anything...
                if (! exci) continue;
                
                bool good = ( exci == outpExpected[iStimPhase][j]    );
                if (good) outpAccumGood += (1.0 / (stimTimes[iStimPhase] -stimDelay -outpDelay) );
                else      outpAccumBad  += (1.0 / (stimTimes[iStimPhase] -stimDelay -outpDelay) );
                
                //printf ("found active output neuron %d\n", i);
                }
         }
  
}




void FixNeuneuStimulus::loadStimFile (string fname)
{
  // we will need the rule set and the a rule-builder
  SRModel             *srm  = ((AtomVecSRSim*)(atom->avec))->srmodel;
  RuleSet             *rset = srm->ruleset;
  MoleculeTypeManager *mtm  = srm->mtm;
  BNGRuleBuilder rb( srm );

  printf ("  FixNeuneuStimulus::loadStimFile  opening stimfile.\n");
  ifstream f( fname.c_str() );    // pfffft... how stupid is this?? What doesn't ifstream have a string-based constructor???
  if (! f.is_open())
     error->all( string("FixNeuneuStimulus::loadStimFile - cannot open file: "+fname).c_str() );
  
  // first read parameters
  printf ("  FixNeuneuStimulus::loadStimFile  reading parameters:\n");
  bool params = true;
  while (params)
    {
    string name, value;
           f >> name;
    
    if      (name == "stim:")      {params=false;}
    else if (name == "outpDelay:") {f >> outpDelay;}
    else if (name == "stimDelay:") {f >> stimDelay;}
    else if (name == "excitable_site_id:")
       { // e.g. "ed(ex)"
       string sname;  f >> sname;
       int siteID = srm->names->existsID(NamesManager::SiteName,sname);
       //printf ("        excitable_site_id: searching for %s ==> %d    \n", sname.c_str(), siteID);
       
       // unfortunately, we have to run overy every molecule & site in the mtm, to find the right one
       for (int i=0 ; i < mtm->numMolIDs() ; i++)
           {
           //printf ("  mol %d with %d sites: \n", i, mtm->numSites(i) );
           for (int j=0 ; j < mtm->numSites(i) ; j++)
               {
               //printf ("             testing mol %d site %d\n", i,j);
               //printf ("                                   = %d\n", mtm->getSiteType(i,j));
               if (mtm->getSiteType(i,j) == siteID)
                  excitable_site_id = j;
               }
           }
       }
    else if (name == "excite_modification:")  // as an input
       {
       printf ("        excite_modification:\n");
       string mod; f >> mod;
       int modID = srm->names->existsID(NamesManager::ModificationName,mod);
       excite_modification = modID;
       }
    else if (name == "excited_modification:")  // which states are considered an output?
       {
       printf ("        excited_modification:\n");
       string mod; f >> mod;
       int modID = srm->names->existsID(NamesManager::ModificationName,mod);
       excited_modification.insert( modID );
       }
    else if (name == "nevery:")            {f >> nevery;}
    else if (name == "stimResultFile:")    {f >> stimResultFile;}
    else if (name == "") {}
    else error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... unknown parameter - "+name).c_str() );
    
    if (! f.good())
       error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... no stim data! "+fname).c_str() );
    }
    
  // did we find all necessary information?
  if (excitable_site_id == -1) 
     error->all( string("FixNeuneuStimulus::loadStimFile - I didn't find an excitable_site_id!! "+fname).c_str() );
  if (stimResultFile == "")
     error->all( string("FixNeuneuStimulus::loadStimFile - I didn't find a stimResultFile!! "+fname).c_str() );
  
    
  // then read the line with the pattern names:
  printf ("  FixNeuneuStimulus::loadStimFile  identify in/out patterns:\n");
  string names;
  getline(f, names);  // one first getline to skip the \n after stim:
  getline(f, names);
  printf ("  Line with excitation pattern names: %s\n", names.c_str());
  stringstream namestream(names);
  string buf;
    namestream >> buf;  // discard the first element: it's just "Time"...
  while (namestream.good())
    {
    string in;
    namestream >> in;
     
    // read the input  names:   e.g.   i:ed(~in2)
    // read the output names:   e.g.   o:ed(~out2)
    char type = in[0];
    
    in = in.substr(2);  // get rid of the "i:"
    ReactantTemplate *rt = rb.parseBNGTemplate(in);
                      rt->addUse( ReactantTemplate::observableRT );
                      rt->addUse( ReactantTemplate::reactingRT );    // otherwise, it won't be updated!
    int pattern_id = rset->getTemplateID( rt );
    delete rt;
    
    if      (type == 'i') {inptPattern.push_back(pattern_id); printf ("   inpt Pattern = %d\n", pattern_id);}
    else if (type == 'o') outpPattern.push_back(pattern_id);
    else error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... maybe we forgot the i/o before the template names... "+fname).c_str() );
    }
    
    
    
  // then read the input and the expected output:
  printf ("  FixNeuneuStimulus::loadStimFile  read input/expected output:\n");
  int phase = 0;
  while (f.good())
    {
    string in;
    int    x;
    double d;
    
    // at what time will the stimulation change?
    f >> in;
    if (in == "") continue;
    
    // stimTime:
    stringstream sstr(in);
    if (! (sstr >> d))
       error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... double expected. \""+in+"\" in file "+fname).c_str() );
    stimTimes.push_back( d );
    //printf (" Time %f from %s\n", d, in.c_str());
    
    // inputs: stimData
    stimData.push_back( vector<int>() );
    for (uint i=0 ; i<inptPattern.size() ; i++)
        {
        if (! (f >> x))
           error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... int expected."+fname).c_str() );
        stimData[phase].push_back(x);
        }
    
    // outputs: outpExpected
    outpExpected.push_back( vector<int>() );
    for (uint i=0 ; i<outpPattern.size() ; i++)
        {
        if (! (f >> x))
           error->all( string("FixNeuneuStimulus::loadStimFile - bad stim file... int expected."+fname).c_str() );
        outpExpected[phase].push_back(x);
        }
        
    phase++;
    }
    
  printf ("FixNeuneuStimulus::loadStimFile: ok, we read %d stimulation phases.\n", phase);
  f.close();
}

