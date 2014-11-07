
//#define NDEBUG
#include "defs.h"
#include <assert.h>

#include "templ_site.h"
#include "sr_error.h"

using namespace SRSim_ns;


void TemplSite::connectToSite (TemplSite *s2) 
  {
  //assert (otherEnd == NULL && s2->otherEnd==NULL); 
  
  // let's throw an error if this site is already connected...
  if (otherEnd != NULL || s2->otherEnd!=NULL)
     throw SRException ("trying to connect already connected sites!");
    
  otherEnd     = s2;
  s2->otherEnd = this;
  }

void SRSim_ns::TemplSite::disconnect( )
  {
  assert (otherEnd != NULL); 
  TemplSite *s2 = otherEnd;
  assert (s2->otherEnd != NULL);
  
  otherEnd = NULL;
  s2->otherEnd = NULL;
  }

int SRSim_ns::TemplSite::getRea( )
  {
  return realization;
  }

void SRSim_ns::TemplSite::setModif( int _modif )
  {
  modif = _modif;
  }

void SRSim_ns::TemplSite::setRea( int _rea )
  {
  realization = _rea;
  }

void SRSim_ns::TemplSite::setPattern( PatternTp _p )
  {
  pattern = _p;
  }

