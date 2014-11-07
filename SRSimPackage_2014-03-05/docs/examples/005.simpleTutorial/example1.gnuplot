set size 1.0, 0.4
set terminal postscript portrait enhanced mono dashed lw 1 "Helvetica" 14 
set output "example1.ps"



set title "Concentration trajectory"
set xlabel "timestep"
set ylabel "number of particles"


plot './example1.srsim.gdat' using 1:2 title "nA0" with lines, \
     './example1.srsim.gdat' using 1:($3+$4) title "nA1+nA11" with lines, \
     './example1.srsim.gdat' using 1:5 title "nA2" with lines, \
     './example1.srsim.gdat' using 1:6 title "nBtri" with lines, \
     './example1.srsim.gdat' using 1:7 title "nBdi" with lines

set terminal x11
set size 1,1
replot
pause mouse


