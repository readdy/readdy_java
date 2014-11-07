/* ----------------------------------------------------------------------
------------------------------------------------------------------------- */

#ifndef SR_ERROR_H
#define SR_ERROR_H

#include <string>
#include <stdio.h>

using namespace std;
namespace SRSim_ns {

/** Error reporting class. */
class SRError 
   {
   public:
    static void critical(const string str);
    static void critical(const string str, const string str2);
    static void warning (const string str);
    static void warning (const string str, const string str2);
   };


class SRException
   {
   public:
    SRException (string wassamatter) : what(wassamatter) 
      { 
      printf ("Exception = %s\n", wassamatter.c_str()); 
      }
    
    void report (string moreinfo);
    string what;
    };

   
}
#endif