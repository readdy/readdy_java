/************************************************************************
  			templ_site.h - Copyright gerdl

**************************************************************************/

#ifndef TEMPL_SITE_H
#define TEMPL_SITE_H
#include <string>
#include <SRSim/templ_molecule.h>


namespace SRSim_ns {
using namespace std;

enum PatternTp {
     PObligatory = 0,                  // follow template specification exactly
     PIgnoreType = 1,                  // don't care for the site/molecule type...
     PAnyBond    = 2,                  // has to be bound to something
     PWildcard   = 100                 // all numbers from 100 to 100+x are wildcards ...
     };


class TemplMolecule;

/** a modifyable site of a molecule. Can bind to exactly one Molecule and one other site. */
class TemplSite{
   friend class TemplMolecule;
// Public stuff
public:
   /// _mod == -1: no modification
   inline TemplSite        (int _tp, int _mod, int _pt) : type(_tp), modif(_mod), pattern(_pt), otherEnd(NULL), mol(NULL), realization(-1) {}
   
   void   connectToSite    (TemplSite *s2);
   void   disconnect       ();
   
   inline bool           isConnected () {return (otherEnd!=NULL);}
   inline int            getType     () {return type; }
   inline int            getPattern  () {return pattern;}
   inline int            getModif    () {return modif;}
   inline TemplMolecule* getMol      () {return mol;}
   inline TemplSite*     getOther    () {return otherEnd;}
   
          void           setModif    (int _modif);
          void           setPattern  (PatternTp _p);
          void           setRea      (int _rea);
          int            getRea      ();
private:
   int                  type;
   int                  modif;
   int                  pattern;     // at the moment only PObligatory and POnePlus are working
   class TemplSite*     otherEnd;                  
   class TemplMolecule* mol;
   
   int                  realization;
   };

}

#endif //TEMPL_SITE_H

