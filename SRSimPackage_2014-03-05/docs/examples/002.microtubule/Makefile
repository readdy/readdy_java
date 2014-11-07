


transport: spass3.lammpstrj spass.restart

spass.restart: spass.in spass.bngl spass.geo spass.tgeo
	lmp_srsim < spass.in

spass3.lammpstrj: spass.restart spass.in2 spass.bngl spass.geo spass.tgeo
	lmp_srsim < spass.in2

#spass.tgeo: spass.bngl spass.geo
#	createGeo spass.bngl spass.geo spass.tgeo

clean:
	rm -f spass3.lammpstrj spass.restart