//
// C++ Implementation: rule_builder
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "rule_builder.h"
#include "site_reactant_template.h"
#include "multi_mol_reactant_template.h"
#include "bound_reactant_template.h"
#include "modification_reactant_template.h"

#include <assert.h>
#include <algorithm>

using namespace SRSim_ns;
using namespace std;



/**   It seems not to be an easy task to determine the type of rule from BNGL.
      Hence this function tries some
*/
RuleTp::RuleTpType RuleBuilder::TransformTemplates( vector< ReactantTemplate * > & in, vector< ReactantTemplate * > & out )
  {
  RuleTp::RuleTpType rtype = RuleTp::ExchangeR;    // standard assumption
  if (createSiteTemplatesIfNecessary(in, out)) 
     rtype = RuleTp::BindR;                        // Caution!  It is even set to bind, if it's a breaking rule!
  else if (createModiTemplatesIfNecessary(in, out))
     rtype = RuleTp::ModifyR;
  else if ( createIntramolSiteTemplate(in,out) )
     rtype = RuleTp::BindIntramolR;                 // Caution!  It is even set to bind, if it's a breaking rule!
     
  return rtype;
  }


/**  exchanges a Molecule-Templates with a site reactant Templates if
 *   necessary. (If we have a binding/unbinding rule.)
 *    At the Moment we allow only bind rules of the type:
 *          A + B -> C   or the reverse reaction.
 *    Other Reactions will be exchange-Rules.
 *
 * @return true if we had a bind-rule, else false.
 *
 *   Problem with returning 3 SRTs:
 *     the bound product is not unique: there's two possibilities to set the 
 *                                      startSite to. So we would need two startSites!
 */
bool RuleBuilder::createSiteTemplatesIfNecessary ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out )
  {
  //printf ("RuleBuilder::createSiteTemplatesIfNecessary  in[%d] out[%d] \n",in.size(), out.size());
  // possible bind/unbind rules are:
  //  a + b -> c        2 -> 1
  //  c     -> a + b    1 -> 2
  if ( !(in.size()==2 && out.size()==1) && 
       !(in.size()==1 && out.size()==2) )
     return false;
  //if (out.size() != 1 && in.size() != 1) return false;
  //if (out.size() == 1 && in.size() == 1) return false;
  
  // find the template with a bond less... :)
  vector<TemplSite*> tsp;
  if (out.size() == 1)
     {
     tsp = out[0] -> findMissingBond(in);      // A+B -> C
     }
  else // in.size()== 1
     {
     tsp = in[0] -> findMissingBond(out);      // A -> B + C
     }
     
  // exchanges WholeMoleculeTemplate -> SiteReactantTemplate
  if (tsp.size() != 0)
     {
     vector<ReactantTemplate*> &unbound = (in.size()==2)?(in ):(out);
     vector<ReactantTemplate*> &bound   = (in.size()==2)?(out):(in );
     assert (unbound.size() == 2);
     assert (tsp    .size() == 3);
     ReactantTemplate *t;
     for (int i=0 ; i<2 ; i++)
         {
         t = unbound[i];
         unbound[i] = new SiteReactantTemplate (t, tsp[i]);    // build a new SiteTemplate from the old WholeMolTempl...
         
         /*printf ("  unbound %d = T%d ... srt1=%d\n",i, tsp[i]->getType(), ((SiteReactantTemplate*)(unbound[i]))->getStartSite()->getType());*/
         
         delete t;
         }
     // the bound site has to become a BoundReactantTemplate:
     t = bound[0];
     bound[0] = new BoundReactantTemplate (t, tsp[2]);
     /*printf ("  bound = T%d ... brt1=%d\n", tsp[2]->getType(), ((BoundReactantTemplate*)bound[0])->getStartSite()->getType());*/
     delete t;
     
     return true;
     }
  return false;
  }


  
  
/**
 *  So, here's another funny problem: how will we tell an ordinary exchange rule from a 
 *  ModificationRule???
 *
 *  Let's try going over all pairs of sites - then exchange the mod-ids and see if it's turned to be the same!
 */  
bool RuleBuilder::createModiTemplatesIfNecessary ( vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out )
  {
  if ( out.size() != in.size() ) return false;
  
  // 0 reactants
  assert( in.size() != 0 );
  
  // 1 reactants
  if (in.size() == 1) return byModificationFromA2B(in, out, 0,0);
  
  // TODO: add another 2-reactants case here:
     // two in, two out, but both ins are modified, e.g. a phosphorilation is passed on...
  
  // 2 reactants
  else if (in.size() == 2)
     {
     bool doneSomething = false;
     
     if (byModificationFromA2B(in, out, 0,0)) doneSomething=true;
     if (byModificationFromA2B(in, out, 1,1)) doneSomething=true;
     
     if (doneSomething) return true;
     
     // Maybe if we switch the out-order?
     ReactantTemplate* buf = out[0]; out[0]=out[1]; out[1]=buf;  //swap out
     if (byModificationFromA2B(in, out, 0,0)) doneSomething=true;
     if (byModificationFromA2B(in, out, 1,1)) doneSomething=true;
     
     return doneSomething;   
     }
  
  assert(false);  // we shouldn't get here.
  return false;
  }
  

/// @returns the two sites before/after the modification!
bool RuleBuilder::byModificationFromA2B (vector<ReactantTemplate*> &in, vector<ReactantTemplate*> &out, int in_id, int out_id)
  {
  ReactantTemplate *a =  in[ in_id];
  ReactantTemplate *b = out[out_id];
  
  if (a->numMolecules() != b->numMolecules()) return false;
  if (a->equals(b))                           return false;   // they're already the same - not by modification!
  
  // make a list of all sites from a and b:
  vector<TemplSite*> ts_a;
  for (int i=0 ; i<a->numMolecules() ; i++)
      for (int j=0 ; j<a->getMolecule(i)->numSites() ; j++)
          ts_a.push_back( a->getMolecule(i)->getSite(j) );
  
  vector<TemplSite*> ts_b;
  for (int i=0 ; i<b->numMolecules() ; i++)
      for (int j=0 ; j<b->getMolecule(i)->numSites() ; j++)
          ts_b.push_back( b->getMolecule(i)->getSite(j) );
          
  // maybe it's not going to work anyway?
  if (ts_a.size() != ts_b.size()) return false;
          
  // try setting all possible modification-changes:
  for (int i=0 ; i<ts_a.size() ; i++)
      for (int j=i ; j<ts_b.size() ; j++)
          {
          if (ts_a[i]->getModif() == ts_b[j]->getModif()) continue;   // they're the same anyway!
          int old_a_modif = ts_a[i]->getModif();
          ts_a[i]->setModif( ts_b[j]->getModif() );
          
          // now's the real interesting moment:
          bool solution = a->equals(b);    // Yehaa!
          
          // undo the changes:
          ts_a[i]->setModif( old_a_modif );
          
          // are we lucky?
          if (solution)
             {
              in[ in_id] = new ModificationReactantTemplate( a, ts_a[i] );
             out[out_id] = new ModificationReactantTemplate( b, ts_b[j] );
             delete a;
             delete b;
             return true;
             }
          }
          
  // nope, sorry, we aren't lucky!
  return false;
  }

  
/**               A -> B      ==   A' + A'  -> B
*       If the input Templates seem to be the binding or breaking of 
*       Intramolecular bonds, this function will realize it and
*       split the input in two parts as it is still a bimolecular reaction
*       in the simulation run.
*/
bool RuleBuilder::createIntramolSiteTemplate( vector< ReactantTemplate * > & in, vector< ReactantTemplate * > & out )
  {
  if (out.size() != 1 || in.size() != 1) return false;
  
  int nBig = max(in[0]->countBonds() , out[0]->countBonds());
  int nSml = min(in[0]->countBonds() , out[0]->countBonds());
  if (nBig-1 != nSml) return false;
  ReactantTemplate *rtBig           = (in[0]->countBonds() == nBig)?(in[0]):(out[0]);
  ReactantTemplate *rtSml           = (in[0]->countBonds() == nSml)?(in[0]):(out[0]);
  vector<ReactantTemplate*> &vrtBig = (in[0]->countBonds() == nBig)?(in):(out);
  vector<ReactantTemplate*> &vrtSml = (in[0]->countBonds() == nSml)?(in):(out);
  
  for (int iMol=0 ; iMol<rtBig->numMolecules() ; iMol++ )
      for (int iSite=0 ; iSite<rtBig->getMolecule(iMol)->numSites() ; iSite++ )
          {
          TemplMolecule *m1 = dynamic_cast<TemplMolecule*>( rtBig->getMolecule(iMol) );  
          assert (m1 != NULL);
          if (m1->getMoleculeAtSite(iSite) == NULL) continue;
          
          // save the old site-vectors:
          TemplSite     *s1 = m1->getSite(iSite);
          TemplSite     *s2 = s1->getOther();
          //TemplMolecule *m2 = dynamic_cast<TemplMolecule*>(m1->getMoleculeAtSite(k));
          
          // delete the two site of the bond:
          s1->disconnect();
          
          // see what happens:  match using matchSingleTM...
          if (rtBig->equals(rtSml))
             {
             //printf ("Huuuuuula-Buuuuuula!\n");
             //assert(false);
             
             // add two independant site reactant templates as reactants
             string smlName = rtSml->getName();
             delete rtSml;
             vrtSml.clear();
             vrtSml.push_back( new SiteReactantTemplate(rtBig, s1) );
             vrtSml.push_back( new SiteReactantTemplate(rtBig, s2) );
             vrtSml[0]->setName(smlName);
             vrtSml[1]->setName(smlName);
             
             // add a bound-reactant template:
             s1->connectToSite(s2);
             ReactantTemplate *t = vrtBig[0];
             vrtBig[0] = new BoundReactantTemplate (t, s1);
             delete t;
             return true;
             }
          
          // restore sites if we were not successful:
          s1->connectToSite(s2);
          }
          
  return false;
  }








