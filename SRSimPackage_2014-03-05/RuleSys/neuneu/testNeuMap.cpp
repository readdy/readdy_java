
#include "defs.h"
#include <assert.h>

#include "start_state_definition.h"
#include "templ_site.h"
#include "templ_molecule.h"
#include "names_manager.h"
#include "bng_rule_builder.h"
#include "reactant_template.h"
//#include "multi_mol_reactant_template.h" 
#include "bound_reactant_template.h"
#include "sr_error.h"
#include "sr_model.h"

#include "neuneu/neuneu_rt_builder.h"

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <sstream>



using namespace std;
using namespace SRSim_ns;

void have_fun(int argc, char **argv)
  {
  
  SRModel model(12345);
  
  printf ("b-heu\n");
                   
  assert( argc >= 3 );                     
  BNGRuleBuilder p(&model);
  p.readFile(argv[1]); 
  
  printf ("c-heu\n");
  
  vector<RuleTp*> &vrt = model.ruleset->getRules();
  for (int i=0 ; i<vrt.size() ; i++)
      cout << vrt[i]->toString() << "  at Rate= " << model.kinetics->getRate(i) << endl;
      
  printf ("\n\n\n");
  
  for (int i=0 ; i<model.ruleset->numTemplates() ; i++)
      {
      ReactantTemplate *rt = model.ruleset->getRT(i);
      
      cout << "T"<<i<<" : ";
      if      (rt->getRTType() == ReactantTemplate::MultiMolRT) cout<<"[MMRT]";
      else if (rt->getRTType() == ReactantTemplate::BoundRT   ) cout<<"[BRT] ";
      else if (rt->getRTType() == ReactantTemplate::SiteRT    ) cout<<"[SRT] ";
      else if (rt->getRTType() == ReactantTemplate::ModifRT   ) cout<<"[MRT] ";
      else assert(false);
      
      if (rt->isUsableAs(ReactantTemplate::observableRT)) cout<<"<obs>";
      if (rt->isUsableAs(ReactantTemplate::creatableRT )) cout<<"<cre>";
      if (rt->isUsableAs(ReactantTemplate::reactingRT  )) cout<<"<rea>";
      
      cout <<" : "<<rt->getName()<<endl;
      }
  
  
  
  printf ("d-heu\n");
  
  NeuneuRtBuilder nrb(model, argv[2]);
  
  printf ("e-heu\n");
  
  try{
     ReactantTemplate *nrt = nrb.build();
     cout <<"  NeuneuRT : "<<nrt->getName()<<endl;
     } 
  catch (SRException *sre)
     {
     printf ("error: %s\n", sre->what.c_str() );
     }
  
  
  
  printf ("\n done. \n\n");
  return;
  }


  
  
int main (int argc, char **argv)
  {
  try { have_fun (argc, argv); }
  catch (SRException sre) {sre.report("sorry - no further info available...");}
  
  return 0;
  }
