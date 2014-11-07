set terminal postscript 25 color solid
set output "spass.graph.ps"

set xlabel "time x 1000 ts"
set ylabel "phosphorylated A"
set title "no angular force"
set key right bottom

plot \
     "w_scf.srsim.gdat" using ($1/1000):3 with lines ti "SRSim w-Scf",\
     "wo_scf.srsim.gdat" using ($1/1000):3 with lines ti "SRSim wo-Scf",\
     "w_scf.gdat" using ($1*1):3 with lines ti "BNG w-Scf",\
     "wo_scf.gdat" using ($1*1):3 with lines ti "BNG wo-Scf"
set output
set terminal x11  
replot

     
pause mouse



