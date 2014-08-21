set boxwidth 0.3
set style fill solid
set ylabel "nReactions"
set xtic rotate by -45 scale 0 font ",8"
set ytics

set grid nopolar
set grid noxtics


plot "../ReaDDy_output_0/out_reactions_lastLineTransposed.csv" using 2:xtic(1) title "nReactions" with histogram
		
#pause -1

set output "plot_reactionResults.png"
set term png

plot "../ReaDDy_output_0/out_reactions_lastLineTransposed.csv" using 2:xtic(1) title "nReactions" with histogram