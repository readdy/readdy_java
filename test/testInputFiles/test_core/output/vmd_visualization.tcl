set trajPath "/Users/johannesschoeneberg/NetBeansProjects/svn/readdy/ReaDDy/test/testInputFiles/test_core/output/"

set simulationTraj "coreTest_coords.xyz"


mol delete top
mol load xyz $trajPath$simulationTraj
# delete the automatically generated represenation
mol delrep 0 top
display resetview



# 						sphere scale, resolution
# A
mol representation VDW 1.200000 16.000000 
mol selection name C_0 
mol material Opaque
mol color ColorID 0
mol addrep top

# B
mol representation VDW 1.200000 16.000000 
mol selection name C_1 
mol material Opaque
mol color ColorID 1
mol addrep top

# C
mol representation VDW 1.200000 16.000000
mol selection name C_2
mol material Opaque
mol color ColorID 2
mol addrep top

# D
mol representation VDW 1.200000 16.000000 
mol selection name C_3
mol material Opaque
#green for now
mol color ColorID 3
mol addrep top

# E
mol representation VDW 1.200000 16.000000 
mol selection name C_4
mol material Opaque
mol color ColorID 4
mol addrep top

# F
mol representation VDW 1.200000 16.000000 
mol selection name C_5 
mol material Opaque
mol color ColorID 5
mol addrep top

#go to first step of the trajectory
animate goto 0


color Display Background white
