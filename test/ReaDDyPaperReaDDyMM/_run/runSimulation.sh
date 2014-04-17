#!/bin/bash

echo "start simulation..."



PROGRAMPATH=../../../dist/
PROGRAM=$PROGRAMPATH/ReaDDy.jar

INPUTFOLDER=../ReaDDy_input/
OUTPUTFOLDER=../ReaDDy_output/

PARAMGLOBAL=param_global.xml
PARAMGROUPS=param_groups.xml
PARAMPARTICLES=param_particles.xml
PARAMPOTENTIALTEMPLATES=param_potentialTemplates.xml
PARAMREACTIONS=param_reactions.xml
TPLGYCOORDINATES=tplgy_coordinates.xml
TPLGYGROUPS=tplgy_groups.xml
TPLGYPOTENTIALS=tplgy_potentials.xml

LOGFILENAME=log_runSimulation.log
ERRLOGFILENAME=log_runSimulation.err.log

START=`date '+%s'`
STARTDATE=`date`

echo $STARTDATE > $OUTPUTFOLDER/$LOGFILENAME

time java -Xmx2048m \
-Djava.library.path="$PROGRAMPATH/../lib/" \
-jar $PROGRAM 	\
-core BD_OpenMM \
-param_global $INPUTFOLDER/$PARAMGLOBAL 	\
-param_groups $INPUTFOLDER/$PARAMGROUPS 	\
-param_particles $INPUTFOLDER/$PARAMPARTICLES 		\
-param_potentialTemplates $INPUTFOLDER/$PARAMPOTENTIALTEMPLATES 		\
-param_reactions $INPUTFOLDER/$PARAMREACTIONS	\
-tplgy_coordinates $INPUTFOLDER/$TPLGYCOORDINATES 						\
-tplgy_groups $INPUTFOLDER/$TPLGYGROUPS 						\
-tplgy_potentials $INPUTFOLDER/$TPLGYPOTENTIALS 				\
-output_path $OUTPUTFOLDER
2>&1 >> $OUTPUTFOLDER$LOGFILENAME | tee -a $OUTPUTFOLDER$ERRLOGFILENAME >> $OUTPUTFOLDER$LOGFILENAME
# output both sterr and stout to the logfile but split the error to a separate errorLogfile							


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


