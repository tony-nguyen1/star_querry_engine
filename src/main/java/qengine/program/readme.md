gnuplot

set title "Data from answers.dat"
set xlabel "X-axis"
set ylabel "Nombre de solutions"
plot "answers.dat" using 1:2 with linespoints title "Answers"

set terminal png
set output "plot.png"
replot
set output

TODO identifier les templates qui donnent pas beaucoup de réponses (0)