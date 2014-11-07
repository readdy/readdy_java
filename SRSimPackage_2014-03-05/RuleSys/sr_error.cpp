/* ----------------------------------------------------------------------
------------------------------------------------------------------------- */

#include "stdlib.h"
#include "sr_error.h"

using namespace SRSim_ns;

/* ---------------------------------------------------------------------- */

#include <stdio.h>


void SRError::warning(const string str)  {warning (str,"");}
void SRError::warning(const string str, const string str2)
{
  printf("  ###################\n");
  printf("WARNING: %s: %s\n",str.c_str(),str2.c_str());
  printf("  ###################\n");
}



void SRError::critical(const string str) {critical(str,"");}
void SRError::critical(const string str, const string str2)
  {
  printf("  ###################\n");
  printf("ERROR: %s: %s\n",str.c_str(),str2.c_str());
  printf("  ###################\n");
  exit(1);
  }



void SRException::report (string moreinfo)
  {
  SRError::critical(what, moreinfo);
  }
