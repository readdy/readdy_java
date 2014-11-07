set terminal postscript
set output "spass.graph.ps"

set title "with / without scaffold protein"
plot "with_scaffold_155.srsim.gdat" using 1:2 with lines ti "w/ 155",\
       "wo_scaffold_155.srsim.gdat" using 1:2 with lines ti "wo/ 155",\
       "wo_scaffold_150.srsim.gdat" using 1:2 with lines ti "wo/ 150",\
       "with_scaffold_57.5.srsim.gdat" using 1:2 with lines ti "w/ 57.5",\
       "wo_scaffold_57.5.srsim.gdat" using 1:2 with lines ti "wo/ 57.5",\
       "wo_scaffold_50.srsim.gdat" using 1:2 with lines ti "wo/ 50"
set output
set terminal x11  
replot

     
pause mouse



