Installation:
a) install OpenMM (or use "module load compiler/4.6.3 openmm/git" in our system)
b) install currend ReaDDy version (with ReaDDyMM extension)
c) make
        - set OPENMM_PLUGIN_DIR (part of OpenMM instal)
        - set JAVA_HOME system variable
        - make
d) set in runSimulation.sh
        PROGRAMPATH=/path/to/your/ReaDDy/
        -Djava.library.path="/path/to/your/ReaDDyMM/library/" (the one you just complied)
        (we could use here the PROGRAMPATH variable, if we put the ReaDDyMM library somwere in the 	ReaDDy directory)
        -core BD_OpenMM
e) set ReaDDyMM specific parameter
        - in param_global.xml:
        <dtO>1e-10</dtO>	// the diffusion timestep in OpenMM
        <cuda>0</cuda>		// the CUDA device index

        - in param_particles.xml

        <numberOfDummyParticles>2358</numberOfDummyParticles>   // the number of dummy particles for the respective particle


f) run ReaDDy
