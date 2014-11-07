plot: staticstim.out
	echo "plot 'staticstim.out' using 1:2 with steps, 'staticstim.out' using 1:3 with steps; pause mouse" | gnuplot
	
	
staticstim.out: staticstim.in circuit001.neu neuneu.in neuneu.bngl neuneu.geo neuneu.tgeo
	lmp_srsim -in neuneu.in
	
	
	
