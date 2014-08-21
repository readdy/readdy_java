#!/bin/bash

ADDITIONALLINE='set trajPath "'$PWD'/ReaDDy_output_0/"' 
echo "$ADDITIONALLINE"  > ./analysisScripts/vmd_visualization.tcl
echo "`cat ./analysisScripts/_vmd_visualizationTemplate.tcl`" >> ./analysisScripts/vmd_visualization.tcl
