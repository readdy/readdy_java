#!/bin/bash
# this script is running all gnuplot analysis scripts



gnuplot gnuplot_msd.gnu.sh
gnuplot gnuplot_rdf.gnu.sh
gnuplot gnuplot_particleNumbers.gnu.sh
gnuplot gnuplot_reactions.gnu.sh


#---------------------------------------------------------------------------------
# processing of the reactions output file
headers=`head -3 ../ReaDDy_output/out_reactions.csv | tail -1`

# the following line does the following:
# returns the last two lines
# returns the first line of these last two lines
# transposes this first line
#tail -2 ../ReaDDy_output/out_reactions.csv | head -1 | rs -T >> ../ReaDDy_output/out_reactions_lastLineTransposed.csv
data=`tail -2 ../ReaDDy_output/out_reactions.csv | head -1`
nLinesWhenTransposed=`echo $data | rs -T | wc -l`
nLinesIWantToDisplayFromTheEnd=$[$nLinesWhenTransposed-1]


echo -e "$headers\n$data" | rs -T | tail -$nLinesIWantToDisplayFromTheEnd > ../ReaDDy_output/out_reactions_lastLineTransposed.csv

#---------------------------------------------------------------------------------

gnuplot _gnuplot_reactionResults.gnu.sh