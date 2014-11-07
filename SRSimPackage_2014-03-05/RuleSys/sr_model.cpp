//#define NDEBUG
#include "defs.h"
#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdio.h>

#include "sr_model.h"
#include "simple_mass_action_kinetics.h"
#include "uniform_distribution_start_state.h"
#include "bng_rule_builder.h"


using namespace SRSim_ns;


void SRSim_ns::SRModel::initialize( int rndSeed )
  {
  printf ("SRModel::SRModel ... construction...\n");
  random   = new RandomGenerator(rndSeed);
  ruleset  = new RuleSet ();
  names    = new NamesManager ();
  mtm      = new MoleculeTypeManager ();
  sstate   = new UniformDistributionStartState (ruleset, random);
  geo      = new GeometryDefinition ( this );
  kinetics = new SimpleMassActionKinetics ();
  observer = new ObservablesManager ();
  printf ("SRModel::SRModel ... construction... done.\n");
  }

SRModel::SRModel (int rndSeed)
  {
  initialize (rndSeed);
  }

SRModel::SRModel( int rndSeed, const char * _bnglName, const char * _mgeoName, const char * _tgeoName, bool addZeroSpecies)
  {
  initialize (rndSeed);
  
  bnglName = _bnglName;
  mgeoName = _mgeoName;
  tgeoName = _tgeoName;
  
  // add this zero-name because we don't have type zero atoms in Lammps:
  if (addZeroSpecies)
     names->getID( SRSim_ns::NamesManager::MoleculeSpeciesName, "Zero_Molecule");  
  
  // now read some reaction-data... (also fills kinetics and startstate.)
  BNGRuleBuilder  b(this);
  //printf ("Ping3\n");
  b.readFile (_bnglName);
  printf ("SRModel::SRModel(): Have read %d reaction rules and %d templates.\n", ruleset->numRules(), ruleset->numTemplates());
  
  // Read some geometry information:
  geo->readMGeoFile (_mgeoName);
  geo->readTGeoFile (_tgeoName);
  
  }

  
SRModel::~SRModel ()
  {
  delete observer;
  delete random;
  delete names;
  delete ruleset;
  delete mtm;
  delete kinetics;
  delete sstate;
  delete geo;
  }

const char * SRModel::getBnglName( )
  {
  return bnglName.c_str();
  }

const char * SRModel::getMgeoName( )
  {
  return mgeoName.c_str();
  }

const char * SRModel::getTgeoName( )
  {
  return tgeoName.c_str();
  }


void SRModel::printModelInfo ()
  {
  printf ("\n\n\n SRSim Rules:");
  
  vector<RuleTp*> &vrt = ruleset->getRules();
  for (int i=0 ; i<vrt.size() ; i++)
      cout << vrt[i]->toString() << "  at Rate= " << kinetics->getRate(i) << endl;
      
  printf ("\n\n\n SRSim Templates:");
  
  for (int i=0 ; i<ruleset->numTemplates() ; i++)
      {
      ReactantTemplate *rt = ruleset->getRT(i);
      
      cout << "T"<<i<<" : ";
      if      (rt->getRTType() == ReactantTemplate::MultiMolRT) cout<<"[MMRT]";
      else if (rt->getRTType() == ReactantTemplate::BoundRT   ) cout<<"[BRT] ";
      else if (rt->getRTType() == ReactantTemplate::SiteRT    ) cout<<"[SRT] ";
      else if (rt->getRTType() == ReactantTemplate::ModifRT   ) cout<<"[MRT] ";
      else assert(false);
      
      if (rt->isUsableAs(ReactantTemplate::observableRT)) cout<<"<obs>";
      if (rt->isUsableAs(ReactantTemplate::creatableRT )) cout<<"<cre>";
      if (rt->isUsableAs(ReactantTemplate::reactingRT  )) cout<<"<rea>";
      if (rt->isUsableAs(ReactantTemplate::senselessRT )) cout<<"<sls>";
      
      cout <<" : "<<rt->getName()<<endl;
      }
  
  printf ("\n\n\n");  }




