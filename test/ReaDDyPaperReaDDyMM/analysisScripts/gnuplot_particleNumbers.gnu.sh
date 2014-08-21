

set xlabel "time[s]"
set ylabel "nParticles"
dt = 1e-8 # in seconds

plot 	"../ReaDDy_output/out_particleNumbers.csv" using ($1*dt):2 title column(2) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" using ($1*dt):3 title column(3) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):4 t column(4)  with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):5 t column(5) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):6 t column(6) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):7 t column(7) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):8 t column(8) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):9 t column(9) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):10 t column(10) with lines lw 5
		
		pause -1
		

set output "plot_particleNumbers.png"
set term png

plot 	"../ReaDDy_output/out_particleNumbers.csv" using ($1*dt):2 title column(2) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" using ($1*dt):3 title column(3) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):4 t column(4)  with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):5 t column(5) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):6 t column(6) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):7 t column(7) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):8 t column(8) with lines lw 5, \
		"../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):9 t column(9) with lines lw 5, \
                "../ReaDDy_output/out_particleNumbers.csv" u ($1*dt):10 t column(10) with lines lw 5
