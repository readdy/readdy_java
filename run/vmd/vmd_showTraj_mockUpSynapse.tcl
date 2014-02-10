#trajectory path script:

set scriptPath "/Users/Jojo/NetBeansProjects/DiscVesicleSimulator/data/vmd/"
set trajPathScript "downloadedVMD_scripts/trajectory_path_customized.tcl"

source $scriptPath$trajPathScript

mol delete top
mol load xyz /Users/Jojo/NetBeansProjects/svn/readdy/ReaDDy/data/simulation.xyz
# delete the automatically generated represenation
mol delrep 0 top
display resetview

# 						sphere scale, resolution

mol representation VDW 1.700000 8.000000 
mol selection name C_0 
mol material Opaque
mol color ColorID 0
mol addrep top

mol representation VDW 0.400000 8.000000 
mol selection name C_1 
mol material Opaque
mol color ColorID 1
mol addrep top

mol representation VDW 1.400000 8.000000 
mol selection name C_2 
mol material Opaque
mol color ColorID 2
mol addrep top

mol representation VDW 1.700000 8.000000 
mol selection name C_3 
mol material Opaque
mol color ColorID 3
mol addrep top

mol representation VDW 0.400000 8.000000 
mol selection name C_4 
mol material Opaque
mol color ColorID 4
mol addrep top

mol representation VDW 1.700000 8.000000 
mol selection name C_5 
mol material Opaque
mol color ColorID 5
mol addrep top

mol representation VDW 0.400000 8.000000 
mol selection name C_6 
mol material Opaque
mol color ColorID 6
mol addrep top

mol representation VDW 1.200000 8.000000 
mol selection name C_7 
mol material Opaque
mol color ColorID 7
mol addrep top

mol representation VDW 0.400000 8.000000 
mol selection name C_8 
mol material Opaque
mol color ColorID 8
mol addrep top

mol representation VDW 1.700000 8.000000 
mol selection name C_9 
mol material Opaque
mol color ColorID 9
mol addrep top

mol representation VDW 14.100000 28.000000 
mol selection name C_10 
mol material Transparent
mol color ColorID 10
mol addrep top

#mol representation VDW 10.500000 28.000000 
#mol selection name C_10 
#mol material Opaque
#mol color ColorID 15
#mol addrep top

# platzhalter fuer particle 4
mol representation VDW 0.400000 8.000000 
mol selection name C_11 
mol material Opaque
mol color ColorID 4
mol addrep top

mol representation VDW 1.200000 8.000000 
mol selection name C_12 
mol material Opaque
mol color ColorID 12
mol addrep top

# draw cellular membrane
draw delete all
  draw color gray
draw material Opaque
  draw triangle {-50 -50 -50} {-50 50 -50} {50 -50 -50}
  draw triangle {50 50 -50} {-50 50 -50} {50 -50 -50}


draw material Transparent
  draw triangle {-50 -50 -45} {-50 50 -45} {50 -50 -45}
  draw triangle {50 50 -45} {-50 50 -45} {50 -50 -45}


#go to first step of the trajectory
animate goto 0

# create the trajectory path of the reaction pathway

set traj [atomselect top "name C_10"]
trajectory_path $traj scale 5
trajectory_path $traj white 1
