//
// C++ Interface: random_generator
//
// Description: 
//
//
// Marsaglia random number generator
//   taken from the Lammps-MD Simulator sources.
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef RANDOM_GENERATOR_H
#define RANDOM_GENERATOR_H

namespace SRSim_ns {

/**
 *   a random number generator... who would have guessed it?
 */

class RandomGenerator
   {
   public:
    RandomGenerator(int seed);
   ~RandomGenerator();
    RandomGenerator();
    
    void   init    (int seed);
    double uniform ();
    double gaussian();

   private:
    bool inited;
    
    int seed,save;
    double second;
    double *u;
    int i97,j97;
    double c,cd,cm;
    };

}

#endif
