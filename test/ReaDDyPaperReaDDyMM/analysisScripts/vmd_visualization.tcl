set trajPath "/home/mi/biederj/programs/ReaDDyTests/Paper/ReaDDy_output/"

set simulationTraj "editout_traj_vmdCapable.xyz"


mol delete top
mol load xyz $trajPath$simulationTraj
# delete the automatically generated represenation
mol delrep 0 top
display resetview



# 						sphere scale, resolution
# syx
mol representation VDW 1.200000 16.000000 
mol selection name C_0 
mol material Opaque
mol color ColorID 0
mol addrep top

# syx_closed
mol representation VDW 1.200000 16.000000 
mol selection name C_1 
mol material Opaque
mol color ColorID 10
mol addrep top

# snap
mol representation VDW 1.200000 16.000000
mol selection name C_2
mol material Opaque
mol color ColorID 2
mol addrep top

# binaryComplex
mol representation VDW 2.000000 16.000000 
mol selection name C_3
mol material Opaque
#green for now
mol color ColorID 1
mol addrep top

# calciumChannel
mol representation VDW 3.00000 16.000000
mol selection name C_4
mol material Opaque
mol color ColorID 8
mol addrep top

# calciumChannel_open
mol representation VDW 3.000000 16.000000 
mol selection name C_5 
mol material Opaque
mol color ColorID 7
mol addrep top


# calcium
mol representation VDW 0.400000 16.000000 
mol selection name C_6 
mol material Opaque
mol color ColorID 7
mol addrep top

# vesicle
mol representation VDW 7.00000 16.000000
mol selection name C_7
mol material Opaque
mol color ColorID 32
mol addrep top

# vesicle_bound1
mol representation VDW 7.00000 16.000000
mol selection name C_8
mol material Opaque
# darker orange
mol color ColorID 31
mol addrep top

# vesicle_bound2
mol representation VDW 7.00000 16.000000
mol selection name C_9
mol material Opaque
# darker darker orange
mol color ColorID 1 
mol addrep top

# two_binaryComplexes
mol representation VDW 3.00000 16.000000
mol selection name C_10
mol material Opaque
#darker red
mol color ColorID 30
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
draw delete all
draw color 8
draw material Glass1

draw triangle {-50 -50 0} {50 -50 0} {-50 50 0}
draw triangle {-50 50 0} {50 50 0} {50 -50 0}

draw triangle {50 -50 0} {50 -50 100} {50 50 0}
draw triangle {50 50 0} {50 50 100} {50 -50 100}

draw triangle {-50 -50 100} {-50 50 100} {50 -50 100}
draw triangle {50 -50 100} {-50 50 100} {50 50 100}

draw triangle {-50 -50 0} {-50 50 0} {-50 -50 100}
draw triangle {-50 50 0} {-50 50 100} {-50 -50 100}

draw triangle {-50 -50 0} {50 -50 0} {50 -50 100}
draw triangle {-50 -50 0} {-50 -50 100} {50 0 100}

draw triangle {-50 50 0} {-50 50 100} {50 50 100}
draw triangle {-50 50 0} {50 50 100} {50 50 0}
####################################################################################


#go to first step of the trajectory
animate goto 0


# Axes Off

#color Display Background white
