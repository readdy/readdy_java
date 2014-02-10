#!/bin/bash

cd ./_run
#./createTplgyCoords.sh ../ReaDDy_input/
./runSimulation_MC.sh ../ReaDDy_output/
./runSimulation_BD.sh ../ReaDDy_output/
cd ../

