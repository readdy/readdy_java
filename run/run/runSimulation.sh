#!/bin/bash

echo "start simulation..."


#PATH=/Users/Jojo/NetBeansProjects/
#SIMPATH=/home/cocktail/seibertj/discVesicleSimulationSoftware

#PROGRAMPATH=/home/cocktail/ullricha/Desktop/Code/Readdy3/ReaDDy/dist/
PROGRAMPATH=/Users/Jojo/NetBeansProjects/svn/readdy/ReaDDy/dist/
PROGRAM=$PROGRAMPATH/ReaDDy.jar

INPUTFOLDER=../input/
OUTPUTFOLDER=../output/

PARAMGLOBAL=param_global.xml
PARAMGROUPS=param_groups.xml
PARAMPARTICLES=param_particles.xml
PARAMPOTENTIALTEMPLATES=param_potentialTemplates.xml
#PARAMREACTIONS=param_reactions.xml
PARAMREACTIONS=param_reactions_fastfast.xml
TPLGYCOORDINATES=initialConfig_tplgy_coordinates.xml
TPLGYGROUPS=tplgy_groups.xml
TPLGYPOTENTIALS=tplgy_potentials.xml

LOGFILENAME=log_runSimulation.log

START=`date '+%s'`
STARTDATE=`date`

echo $STARTDATE > $OUTPUTFOLDER/$LOGFILENAME

java -jar $PROGRAM 	\
-param_global $INPUTFOLDER/$PARAMGLOBAL 	\
-param_groups $INPUTFOLDER/$PARAMGROUPS 	\
-param_particles $INPUTFOLDER/$PARAMPARTICLES 		\
-param_potentialTemplates $INPUTFOLDER/$PARAMPOTENTIALTEMPLATES 		\
-param_reactions $INPUTFOLDER/$PARAMREACTIONS	\
-tplgy_coordinates $OUTPUTFOLDER/$TPLGYCOORDINATES 						\
-tplgy_groups $INPUTFOLDER/$TPLGYGROUPS 						\
-tplgy_potentials $INPUTFOLDER/$TPLGYPOTENTIALS 				\
-output_path $OUTPUTFOLDER							
>> $OUTPUTFOLDER/$LOGFILENAME

#LATESTPROCESSID=$!
#echo "process started... ID $LATESTPROCESSID"

#wait $LATESTPROCESSID

END=`date '+%s'`
ENDDATE=`date`
TIMEELAPSED=`echo "$END-$START" | bc`

echo $ENDDATE >> $OUTPUTFOLDER/$LOGFILENAME
echo "seconds elapsed $TIMEELAPSED" >> $OUTPUTFOLDER/$LOGFILENAME

echo "done"
LOG=$OUTPUTFOLDER/$LOGFILENAME
echo "see the log in $LOG"


