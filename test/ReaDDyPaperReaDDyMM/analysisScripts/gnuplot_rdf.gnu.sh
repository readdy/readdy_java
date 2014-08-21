
set xlabel "d[nm]"
set ylabel "RDF"
dt = 1e-8 # in seconds
OutputEveryXStep = 1000


index1From = 10
index1To = 10
titleIndex1 = OutputEveryXStep * index1From * dt

index2From = 25
index2To = 25
titleIndex2 = OutputEveryXStep * index2From * dt

index3From = 49
index3To = 49
titleIndex3 = OutputEveryXStep * index3From * dt

g(t) = 1
plot 	[t=0:50] g(t) lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index1From:index1To using 3:4 t sprintf("rdf %f s",titleIndex1) with lines lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index2From:index2To using 3:4 t sprintf("rdf %f s",titleIndex2) with lines lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index3From:index3To using 3:4 t sprintf("rdf %f s",titleIndex3) with lines lw 5

		
pause -1

set output "plot_rdf.png"
set term png

plot 	[t=0:50] g(t) lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index1From:index1To using 3:4 t sprintf("rdf %f s",titleIndex1) with lines lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index2From:index2To using 3:4 t sprintf("rdf %f s",titleIndex2) with lines lw 5, \
		"../ReaDDy_output/out_rdf.csv" index index3From:index3To using 3:4 t sprintf("rdf %f s",titleIndex3) with lines lw 5 
