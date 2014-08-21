
set trajPath "/home/mi/biederj/programs/ReaDDyTests/Paper/ReaDDy_output/"


set simulationTraj "out_traj_vmdCapable_edit.xyz"


mol delete top
mol load xyz $trajPath$simulationTraj
# delete the automatically generated represenation
mol delrep 0 top
display resetview



# 						sphere scale, resolution
# tRNA
mol representation VDW 1.000000 16.000000 
mol selection name C_0 
mol material Opaque
mol color ColorID 0
mol addrep top

# ribosome
mol representation VDW 2.000000 16.000000 
mol selection name C_1 
mol material Opaque
mol color ColorID 32
mol addrep top

# 0-20
mol representation VDW 2.000000 16.000000
mol selection name C_2
mol material Opaque
mol color ColorID 7
mol addrep top

# 40-60
mol representation VDW 1.9t00000 16.000000 
mol selection name C_3
mol material Opaque
#green for now
mol color ColorID 7
mol addrep top

# 60-80
mol representation VDW 2.10000 16.000000
mol selection name C_4
mol material Opaque
mol color ColorID 7
mol addrep top

# 80-100
mol representation VDW 2.300000 16.000000 
mol selection name C_5 
mol material Opaque
mol color ColorID 7
mol addrep top


# 100-120
mol representation VDW 2.400000 16.000000 
mol selection name C_6 
mol material Opaque
mol color ColorID 7
mol addrep top

# 120-140
mol representation VDW 2.600000 16.000000
mol selection name C_7
mol material Opaque
mol color ColorID 7
mol addrep top

# 140-160
mol representation VDW 2.70000 16.000000
mol selection name C_8
mol material Opaque
# darker orange
mol color ColorID 7
mol addrep top

# 160-180
mol representation VDW 2.80000 16.000000
mol selection name C_9
mol material Opaque
# darker darker orange
mol color ColorID 7 
mol addrep top

# 180-200
mol representation VDW 2.90000 16.000000
mol selection name C_10
mol material Opaque
#darker red
mol color ColorID 7
mol addrep top

# 200+
mol retpresentation VDW 3.500000 16.000000 
mol selection name C_11
mol material Diffuse
#black
mol color ColorID 7 
mol addrep top


# cloak particle
mol representation VDW 7.100000 16.000000 
mol selection name C_100
mol material Diffuse
#black
mol color ColorID 8 
mol addrep top


####################################################################################
# Draw Stuff
####################################################################################
# draw cellular membrane
#draw delete all
#draw color 8
#draw material Opaque
#  draw triangle {-50 -50 0} {-50 50 0} {50 -50 0}
#  draw triangle {50 50 0} {-50 50 0} {50 -50 0}
# draw cone {0 0 0} {0 0 -0.5} radius 200 resolution 100
# draw cylinder {0 0 0} {0 0 1.0} radius 200 resolution 100
####################################################################################


#go to first step of the trajectory
animate goto 0


# Axes Off

color Display Background white

