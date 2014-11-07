# linux = RedHat Linux box, gnu g++, OpenMPI, FFTW

SHELL = /bin/sh

EXE =	lmp_srsim
SRC =	$(wildcard *.cpp)
INC =	$(wildcard *.h)
OBJ = 	$(SRC:.cpp=.o)

# ---------------------------------------------------------------------
# compiler/linker settings
# specify flags and libraries needed for your compiler

RULESYS_DIR =   ../RuleSys/
RULESYS_LIB =   ../RuleSys/libRuleSys.a

#CC =		mpicc.openmpi
CC =		g++
#CCFLAGS =	-O2 -funroll-loops -fstrict-aliasing -Wall -W -Wno-uninitialized -I../../RuleSys


CCFLAGS =	-O3 -funroll-loops -fstrict-aliasing -Wall -W -Wno-uninitialized -I$(RULESYS_DIR) -I/usr/lib/openmpi/include
# using debug flags:
#CCFLAGS =	-O2 -funroll-loops -fstrict-aliasing -Wall -W -Wno-uninitialized -I$(RULESYS_DIR) -g -O0 -fno-inline

                 # -g for debugging only...
DEPFLAGS =	-M
#LINK =		mpicc.openmpi
LINK =		g++
LINKFLAGS =	-O -L$(RULESYS_DIR) #-L/usr/lib/openmpi/lib
#LINKFLAGS =	-O -L$(RULESYS_DIR) --enable-libstdcxx-debug
                    #-L/data/gerdl/localLibs/xerces/lib
LIB =           -lstdc++  -lRuleSys -lxerces-c -lpthread -lmpi_cxx -lmpi
ARCHIVE =	ar
ARFLAGS =	-rcsv
SIZE =		size

# ---------------------------------------------------------------------
# LAMMPS-specific settings
# specify settings for LAMMPS features you will use

# LAMMPS ifdef options, see doc/Section_start.html

LMP_INC =	-DLAMMPS_GZIP

# MPI library, can be src/STUBS dummy lib
# INC = path for mpi.h, MPI compiler settings
# PATH = path for MPI library
# LIB = name of MPI library

MPI_INC =
MPI_PATH = 
MPI_LIB =

# FFT library, can be -DFFT_NONE if not using PPPM from KSPACE package
# INC = -DFFT_FFTW, -DFFT_INTEL, -DFFT_NONE, etc, FFT compiler settings
# PATH = path for FFT library
# LIB = name of FFT library

#FFT_INC =       -DFFT_FFTW
FFT_INC =       -DFFT_NONE
FFT_PATH = 
FFT_LIB =	

# additional system libraries needed by LAMMPS package libraries
# these settings are IGNORED if the corresponding LAMMPS package
#   (e.g. gpu, meam) is NOT included in the LAMMPS build
# SYSLIB = names of libraries
# SYSPATH = paths of libraries

gpu_SYSLIB =       -lcudart
meam_SYSLIB =      -lifcore -lsvml -lompstub -limf
reax_SYSLIB =      -lifcore -lsvml -lompstub -limf
user-atc_SYSLIB =  -lblas -llapack

gpu_SYSPATH =      -L/usr/local/cuda/lib64
meam_SYSPATH =     -L/opt/intel/fce/10.0.023/lib
reax_SYSPATH =     -L/opt/intel/fce/10.0.023/lib
user-atc_SYSPATH = 	

# ---------------------------------------------------------------------
# build rules and dependencies
# no need to edit this section

include	Makefile.package

EXTRA_INC = $(LMP_INC) $(PKG_INC) $(MPI_INC) $(FFT_INC)
EXTRA_PATH = $(PKG_PATH) $(MPI_PATH) $(FFT_PATH) $(PKG_SYSPATH)
EXTRA_LIB = $(PKG_LIB) $(MPI_LIB) $(FFT_LIB) $(PKG_SYSLIB)

# Link target

$(EXE): $(OBJ) $(RULESYS_LIB)
	$(LINK) $(LINKFLAGS) $(EXTRA_PATH) $(OBJ) $(EXTRA_LIB) $(LIB) -o $(EXE)
	$(SIZE) $(EXE)

# Library target

lib:	$(OBJ)
	$(ARCHIVE) $(ARFLAGS) $(EXE) $(OBJ)

# Compilation rules

%.o:%.cpp
	$(CC) $(CCFLAGS) $(EXTRA_INC) -c $<

%.d:%.cpp
	$(CC) $(CCFLAGS) $(EXTRA_INC) $(DEPFLAGS) $< > $@

# Individual dependencies

DEPENDS = $(OBJ:.o=.d)
include $(DEPENDS)

clean:
	rm -f *.d
	rm -f *.o
	rm -f lmp_srsim

