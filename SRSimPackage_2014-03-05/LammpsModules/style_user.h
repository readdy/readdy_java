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

// add a style class to LAMMPS by adding 2 lines to this file
// add new include files in appropriate Include ifdef
// add new style keywords and class names in appropriate Class ifdef
// see style.h for examples

#ifdef AngleInclude
#endif

#ifdef AngleClass
#endif

#ifdef AtomInclude
  #include "atom_vec_srsim.h"
#endif

#ifdef AtomClass
  AtomStyle(srsim,AtomVecSRSim)
#endif

#ifdef BondInclude
#endif

#ifdef BondClass
#endif

#ifdef CommandInclude
  #include "start_state_srsim.h"
  #include "runmodif_srsim.h"
#endif

#ifdef CommandClass
  CommandStyle(start_state_srsim,StartStateSRSim)
  CommandStyle(runmodif_srsim,RunModifSRSim)
//  CommandStyle(sss,StartStateSRSim)
#endif

#ifdef ComputeInclude
  #include "compute_reapot_atom.h"
#endif

#ifdef ComputeClass
  ComputeStyle(reapot/atom,ComputeReapotAtom)
#endif

#ifdef DihedralInclude
  #include "dihedral_harmonic_srsim.h"
#endif

#ifdef DihedralClass
  DihedralStyle(harmonic/srsim, DihedralHarmonicSRSim)
#endif

#ifdef DumpInclude
  #include "dump_srsim.h"
  #include "dump_atom_neuneucharge.h"
  #include "dump_bonds.h"
  #include "dump_rea.h"
#endif

#ifdef DumpClass
  DumpStyle(srsim,DumpSRSim)
  DumpStyle(atom/neuneu/charge,DumpAtomNeuneucharge)
  DumpStyle(bonds,DumpBonds)
  DumpStyle(rea,DumpRea)
#endif

#ifdef FixInclude
  #include "fix_srsim.h"
  #include "fix_drag_membrane.h"
  #include "fix_push_membrane.h"
  #include "fix_neuneu_stimulus.h"
#endif

#ifdef FixClass
  FixStyle(srsim,FixSRSim)
  FixStyle(push/membrane,FixPushMembrane)
  FixStyle(drag/membrane,FixDragMembrane)
  FixStyle(neuneu/stimulus,FixNeuneuStimulus)
#endif

#ifdef ImproperInclude
#endif

#ifdef ImproperClass
#endif

#ifdef IntegrateInclude
#endif

#ifdef IntegrateClass
# endif

#ifdef KSpaceInclude
#endif

#ifdef KSpaceClass
#endif

#ifdef MinimizeInclude
#endif

#ifdef MinimizeClass
#endif

#ifdef PairInclude
#endif

#ifdef PairClass
#endif

#ifdef RegionInclude
#endif

#ifdef RegionClass
#endif