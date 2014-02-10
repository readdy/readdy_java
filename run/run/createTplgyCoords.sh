#!/bin/bash

##############################################################################
#		Simulate trajectory
##############################################################################
echo "create tplgy coords..."


PROGRAMPATH=/Users/Jojo/NetBeansProjects/ReaDDy_TplgyCoordsCreator/dist/
#PROGRAMPATH=../programs/tplgyCoordsCreator/

#PROGRAMPATH=/home/cocktail/ullricha/Desktop/Code/Readdy3/ReaDDy/vesicle2diskModel/programs/tplgyCoordsCreator/
PROGRAM=$PROGRAMPATH/ReaDDy_TplgyCoordsCreator.jar

INPUTFOLDER=../input/
OUTPUTFOLDER=../output/

INPUTCOPYNUMBERS=in_copyNumbers.csv
OUTPUTTPLGYCOORDS=initialConfig_tplgy_coordinates.xml
OUTPUTTPLGYGROUPS=initialConfig_tplgy_groups.xml
PARAMGLOBAL=param_global.xml
PARAMPARTICLES=param_particles.xml
PARAMGROUPS=param_groups.xml
PARAMPOTENTIALTEMPLATES=param_potentialTemplates.xml
TPLGYCOORDINATES=tplgy_coordinates.xml
TPLGYPOTENTIALS=tplgy_potentials.xml

LOGFILENAME=log_createTplgyCoords.log

START=`date '+%s'`
STARTDATE=`date`



echo $STARTDATE > $OUTPUTFOLDER/$LOGFILENAME

java -jar $PROGRAM 	\
-input_copyNumbers $INPUTFOLDER/$INPUTCOPYNUMBERS \
-param_global $INPUTFOLDER/$PARAMGLOBAL 	\
-param_particles $INPUTFOLDER/$PARAMPARTICLES 		\
-param_groups $INPUTFOLDER/$PARAMGROUPS	\
-param_potentialTemplates $INPUTFOLDER/$PARAMPOTENTIALTEMPLATES 		\
-tplgy_potentials $INPUTFOLDER/$TPLGYPOTENTIALS 				\
-output_tplgyCoords $OUTPUTFOLDER/$OUTPUTTPLGYCOORDS \
-output_tplgyGroups $OUTPUTFOLDER/$OUTPUTTPLGYGROUPS 
>> $OUTPUTFOLDER/$LOGFILENAME


END=`date '+%s'`
ENDDATE=`date`
TIMEELAPSED=`echo "$END-$START" | bc`

echo $ENDDATE >> $OUTPUTFOLDER/$LOGFILENAME
echo "seconds elapsed $TIMEELAPSED" >> $OUTPUTFOLDER/$LOGFILENAME


echo "done"
echo $OUTPUTFOLDER/$OUTPUTTPLGYCOORDS 
LOG=$OUTPUTFOLDER/$LOGFILENAME
echo "see the log in $LOG"

