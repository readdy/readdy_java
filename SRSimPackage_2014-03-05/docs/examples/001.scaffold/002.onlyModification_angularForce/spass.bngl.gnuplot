set terminal postscript
set output "spass.graph.ps"

set title "with / without scaffold protein"
plot "spass.gdat" using 1:2 with lines ti "A-nP",\
     "spass.gdat" using 1:3 with lines ti "A-P",\
     "spass.gdat" using 1:4 with lines ti "S",\
     "spass.gdat" using 1:5 with lines ti "Sb",\
     "spass.gdat" using 1:6 with lines ti "Sbb",\
     "spass.gdat" using 1:7 with lines ti "Sbbb",\
     "spass.gdat" using 1:8 with lines ti "Sbbbb"
set output
set terminal x11  
replot

     
pause mouse



