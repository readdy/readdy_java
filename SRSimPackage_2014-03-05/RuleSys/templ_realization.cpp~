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

#include "templ_realization.h"

#include <assert.h>
#include <stdio.h>

namespace SRSim_ns {
using namespace std;


bool TemplAffiliationManager::belongsToTempl  (int tid, int mid)
  {
  //printf ("belongs to template %d m%d\n", tid,mid);
  
  //assert( tid < reals.size() );
  if (reals.size() <= tid)
     reals.resize( tid+1 );
  //assert( mid < reals[tid].mol.size() );
  if (reals[tid].mol.size() <= mid)
     reals[tid].mol.resize( mid+1 );
  
  return (reals[tid].mol[mid].size() > 0);
  }
  
  
// bool TemplAffiliationManager::belongsToTempl  (int tid, int mid, int site)
//   {
//   }

int TemplAffiliationManager::sidRandFittingSite (int tid, int mid, double rand)
  {
  assert( tid < reals.size() );
  assert( mid < reals[tid].mol.size() );
  
  vector<int> &m2r   = reals[tid].mol[mid];
  int         realID = m2r[ rand * m2r.size() ];
  
  assert( mid == reals[tid].realizationM[realID] );
  
  return ( reals[tid].realizationS[realID] );
  }
  
  
// retrieves the moleculeID of this template realiz.
int  TemplAffiliationManager::midNthTemplReal (int tid, int n)
  {
  assert( tid < reals.size() );
  assert( n   < reals[tid].realizationM.size() );
  
  return ( reals[tid].realizationM[n] );
  }
  
  
int TemplAffiliationManager::numTemplAffils (int tid)
  {
  //printf ("asking for template %d\n", tid);
  //assert( tid < reals.size() );
  if (reals.size() <= tid)
     reals.resize( tid+1 );
  return ( reals[tid].realizationM.size() );
  }
  
  
  
/// for whole-molecule templates: not site specific templates
void TemplAffiliationManager::addTemplToMol (int tid, int mid)
  {
  // expand templates?
  if (reals.size() <= tid)
     reals.resize( tid+1 );
  
  TemplRealization &tr = reals[tid];
  
  // expand molecules?
  if (tr.mol.size() <= mid)
     tr.mol.resize( mid+1 );
     
  tr.realizationM.push_back( mid );
  tr.realizationS.push_back( -1 );
  int realID = tr.realizationM.size()-1 ;
  tr.mol[mid].push_back( realID );
  }


/// for site-specific templates
void TemplAffiliationManager::addTemplToMol   (int tid, int mid, int site)
  {
  // expand templates?
  if (reals.size() <= tid)
     reals.resize( tid+1 );
  
  TemplRealization &tr = reals[tid];
  
  // expand molecules?
  if (tr.mol.size() <= mid)
     tr.mol.resize( mid+1 );
     
  tr.realizationM.push_back( mid );
  tr.realizationS.push_back( site );
  int realID = tr.realizationM.size()-1 ;
  tr.mol[mid].push_back( realID );
  }


void TemplAffiliationManager::clearMolAffil (int mid)
  {
  // clear molecule 'mid's affiliation in every template:
  for (int tid=0 ; tid<reals.size() ; tid++)
      {
      // expand molecules?
      TemplRealization &tr = reals[tid];
      if (tr.mol.size() <= mid)
        tr.mol.resize( mid+1 );
      
      // delete all the realizations in molecule mid:
      vector<int> &molmid = tr.mol[mid];
      for (int rid=0 ; rid<molmid.size() ; rid++)
          {
          int realID = molmid[rid];
          tr.deleteRealizationByMoveFromBehind( realID );  // does not affect mol[ realizationM[mid] ]
          }
      molmid.clear();
      }
  }


/// e.g. because of deleting physical molecule 'to'
///    -> first step is to delete the affils of 'to'
void TemplAffiliationManager::copyMol (int from, int to)
  {
  clearMolAffil( to );
  // now there are no realizations left, that refer to 'to'
  
  // now move from->to for all templates:
  for (int tid=0 ; tid<reals.size() ; tid++)
      {
      // expand molecules?  I don't thing we need to, here:
      assert (reals[tid].mol.size() > from);
      assert (reals[tid].mol.size() > to  );
      
      // move molecules in the reals[tid]
      reals[tid].copyMol(from, to);
      }
  }


//   we can just delete the realizations from a mol, if we need to...
// void TemplAffiliationManager::delMol (int mid)
//   {
//   }
  
// void TemplAffiliationManager::addMol (int n)
//   {
//   // do we have to expand the molecule number?
//   if ( reals[0].mol.size() <= n )
//   }


/**
 * delete realization 'real2del' in tr by moving the last realization to the place of real2del
 *   and changing the molecule-realization indices in mol:
 *
 * Does not change the values of mol[ realizationM[real2del] ], only that of
 *                               mol[ realizationM[lastReal] ]
 */
void TemplAffiliationManager::TemplRealization::deleteRealizationByMoveFromBehind (int real2del)
  {
  int lastReal = realizationM.size() -1 ;
  
  // change realization-indices in 'mol'
  vector<int> &m2r = mol[ realizationM[lastReal] ];           // mol-to-realization vector ;-)
  for (int rid=0 ; rid<m2r.size() ; rid++)
      if (m2r[rid] == lastReal) m2r[rid] = real2del;
  
  // and move lastReal to the place of the real2del:
  realizationM[real2del] = realizationM[lastReal];
  realizationS[real2del] = realizationS[lastReal];
  realizationS.pop_back();
  realizationM.pop_back();
  }

void TemplAffiliationManager::TemplRealization::copyMol (int from, int to)
  {
  assert( mol[to].empty() );  // should already be cleared by clearMolAffil
  for (int i=0 ; i<mol[to].size() ; i++)
      {
      int realID = mol[from][i];
      realizationM[realID] = to;
      mol[to].push_back( realID );
      }
  }


} // ends namespace



