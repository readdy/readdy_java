# ----------------------------------------------------------------------
# Makefile for OpenMM Preview Release 4 workshop "hello world" examples.
# August 18, 2009
# See https://simtk.org/home/openmm.
# ----------------------------------------------------------------------
# This assumes you have gcc compilers for whatever language you are
# using: g++ for C++ and C, gfortran for Fortran 95.
#
# For the C and Fortran examples, we're depending on your version of
# OpenMM to have been built with the automatically-generated API
# wrappers.
#
# This has had only minimal testing, although it has been known to
# work. It is likely to work fine for C and C++. For Fortran, you
# may need to add some of the C/C++ libraries:
#    -lc -lm -lstdc++ (or -lstdc++.6) -lgcc -lgcc_s
# but this wasn't required for these examples on Centos 5.2 using
# gcc 4.1.2.

# Check whether this is the right capitalization for your install directory.
#OpenMM_INSTALL_DIR=/usr/local/openmm
#OpenMM_INSTALL_DIR=/home/mi/biederj/programs/OpenMM/OpenMM5.2-Linux64
#OpenMM_INSTALL_DIR=/home/mi/biederj/programs/OpenMM
#OpenMM_INSTALL_DIR=/group/ag_cmb/software/openmm-git/openmm
OpenMM_INSTALL_DIR=$(OPENMM_PLUGIN_DIR)/../..
CFLAGS = -g
FFLAGS = -g -ffree-line-length-none

# Extra libraries required when gfortran links with a C++ module.
# If this doesn't work, look in /usr/lib to
# see what versions of libstdc++.so you have and try different
# versions.
FCPPLIBS = -lstdc++

LIB_DIR=$(OpenMM_INSTALL_DIR)/lib
INCLUDE_DIR=$(OpenMM_INSTALL_DIR)/include
LIBS= -lOpenMM

ALL_CPP_EXAMPLES = ReaDDyMM

ALL_PROGS = $(ALL_CPP_EXAMPLES)

default: ReaDDyMM

all : $(ALL_PROGS)

# Treat all .cpp source files the same way.
.cpp :
	g++ $(CFLAGS) -I$(INCLUDE_DIR) $< -L$(LIB_DIR) $(LIBS) -o $*

ReaDDyMM: ReaDDyMM.cpp simulation.h
	g++ -O3 -shared -m64 -g -L$(LIB_DIR) $(LIBS) -fPIC -MMD -MP -I$(INCLUDE_DIR) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I ./ -L$(JAVA_HOME)/jre/lib/amd64/server -ljvm -o libCReaDDyMM.so ReaDDyMM.cpp

clean:
	rm $(ALL_PROGS) *.o *.mod *.obj *.exe 
	rm simulation.h
	rm libCReaDDyMM.so
	rm simulation.class
	rm libCReaDDyMM.d

