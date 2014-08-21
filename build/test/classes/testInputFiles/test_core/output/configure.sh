#!/bin/bash

ADDITIONALLINE='set trajPath "'$PWD'/"' 
echo "$ADDITIONALLINE"  > ./vmd_visualization.tcl
echo "`cat ./_vmd_visualizationTemplate.tcl`" >> ./vmd_visualization.tcl
