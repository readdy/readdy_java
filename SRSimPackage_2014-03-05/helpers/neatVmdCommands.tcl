proc gerdls1 {} {
  mol new spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  #mol new /home/jmosulli/Gerdl/002.genome3D/002.wholeGenome/spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  #mol new /home/jmosulli/Gerdl/002.genome3D/002.wholeGenome/spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  #mol new /home/jmosulli/Gerdl/002.genome3D/002.wholeGenome/spass.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all

  lmpbondsfromdata 0 spass.dat 1 20000
  lmpbondsfromdata 1 spass.dat 20000 34000
  lmpbondsfromdata 2 spass.dat 34000 50000

  setChainFromMol  0 spass.dat
  setChainFromMol  1 spass.dat
  setChainFromMol  2 spass.dat
  }

proc gerdls2 {} {
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all

  lmpbondsfromdata  9 spass.dat 1      9999
  lmpbondsfromdata 10 spass.dat 10000 14999
  lmpbondsfromdata 11 spass.dat 15000 50000

  setChainFromMol  9 spass.dat
  setChainFromMol 10 spass.dat
  setChainFromMol 11 spass.dat
  }
  
  
proc gerdls3 {} {
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all
  mol new curiosity.lammpstrj type lammpstrj first 0 last -1 step 1 filebonds 0 autobonds 0 waitfor all

  lmpbondsfromdata 0 spass.dat 1      9999
  lmpbondsfromdata 1 spass.dat 10000 14999
  lmpbondsfromdata 2 spass.dat 15000 50000

  setChainFromMol  0 spass.dat
  setChainFromMol  1 spass.dat
  setChainFromMol  2 spass.dat
  }
  

#
# display all telomeres:
#   index 0 295 296 1256 1257 1620 1621 3329 3330 4013 4014 4359 4360 5582 5583 6236 6237 6747 6748 7542 7543 8311 8312 9595 9596 10651 10652 11583 11584 12832 12833 13846 13847
#
#
  
proc resrc {} {
 # source "/home/wowbag/Documents/BioInfoWork/AucklandSVN/002.genome3D/neatVmdCommands.tcl"
 # source "/home/jmosulli/Gerdl/002.genome3D/neatVmdCommands.tcl"
 # source "/home/gerdl/work/002.genome3D/neatVmdCommands.tcl"
 # source "/home/mit/csb/gerdl/work/002.HD-Chromatin/002.genome3D/neatVmdCommands.tcl"
  source "/home/mit/csb/gerdl/work/004.HD-SRSimCytokinesis/SRSimSrc_2.0/helpers/neatVmdCommands.tcl"
  }
  

#proc reloadAll {} {
#  reload 0
#  reload 1 
#  reload 2
#  }

proc reload {args} {
    lassign $args arg1 arg2

    set viewpoints {}
    set mol [molinfo top]

    # save orientation and zoom parameters
    set viewpoints [molinfo $mol get {
        center_matrix rotate_matrix scale_matrix global_matrix}]

    # delete all frames and (re)load the latest data set.
    animate delete all
    set files [lindex [molinfo $mol get filename] 0]
    set lf [expr [llength $files] - 1]

    if {$arg1 == "waitfor"} {
        mol addfile [lindex $files $lf] \
            type [lindex [lindex [molinfo $mol get filetype] 0] $lf] \
            $arg1 $arg2
    } else {
        mol addfile [lindex $files $lf] \
            type [lindex [lindex [molinfo $mol get filetype] 0] $lf]
    }

    # restore orientation and zoom
    molinfo $mol set {center_matrix rotate_matrix \
                      scale_matrix global_matrix} $viewpoints
}






# set User-data to Lammps-Charge, wich might be modification dependent:
# setUserFromLammpstrj 1 neuneu.lammpstrj
# setUserFromLammpstrj 3 neuneu.lammpstrj
proc setUserFromLammpstrj {mol datFile} {
  # open lammps data file
  
  if {[catch {open $datFile r} FILE]} {
      puts stderr "could not open file $datFile\n"
      return -1
      }
  
  #if {[catch {open "gerdlsDebug.bug" w} DBG]} {
  #    puts stderr "could not open file gerdlsDebug.bug\n"
  #    return -1
  #    }
  
  #puts $DBG "setUserFromLammpstrj: $datFile \n"
  #return 0 

  # make an atomselect:
  set sel [atomselect $mol all]

  # number of atoms:
  set nAtoms [molinfo $mol get numatoms]
  set broken 0
  set iFrame 0
  
  while {$broken == 0} {
      #puts $DBG "in outer while:\n"
      
      # search for an item: timestep... no skip the timestep, proceed to the atoms list!
      set broken 1
      while {[gets $FILE line] >= 0} {
          if { [regexp {^ITEM: ATOMS} $line] } {
              set broken 0
              break
              }
              
          #puts $DBG "just read $line ... "
          }
          
      # start from a new empty list with user data:
      set userDataFrameX {}
          
          
      for {set i 0} {$i < $nAtoms} {incr i} {
          gets $FILE line
          # grep bond numbers from entry and adjust to VMD numbering style
          regexp {^\s*\d+\s+\d+\s+[\d\.e-]+\s+[\d\.e-]+\s[\d\.e-]+\s([\d\.e-]+)} $line dummy udat
    
          #puts $DBG "just read $udat ... "
    
          lappend userDataFrameX $udat
          }          
          
      # now set the userdata for frame "iFrame":
      $sel frame $iFrame
      $sel set user $userDataFrameX
          
      incr iFrame
      }

  $sel delete
  close $FILE
  #close $DBG
  
  return 0  
  }



# by Gerdl:  largest part copied from lmpbondsfromdata
#  Try to set the 
proc setChainFromMol {mol datFile} {
  # open lammps data file
  if {[catch {open $datFile r} FILE]} {
      puts stderr "could not open file $datFile"
      return -1
      }

  # number of atoms:
  set nAtoms [molinfo $mol get numatoms]
  set chainList {}

  # read file line by line until we hit the Atoms
  while {[gets $FILE line] >= 0} {
      if { [regexp {^\s*Atoms} $line] } {
          puts "nAtoms= $nAtoms\nnow reading Atoms section\n"
          break
          }
      }

  # skip one line
  gets $FILE line
  # read the Mol-Data from the Atoms section
  for {set i 0} {$i < $nAtoms} {incr i} {
      gets $FILE line
      # grep bond numbers from entry and adjust to VMD numbering style
      regexp {^\s*\d+\s+(\d+)} $line dummy molID

      lappend chainList $molID
      }
  close $FILE
  
  set sel [atomselect $mol all]
  $sel set chain $chainList
  puts $chainList
  $sel delete
  return 0
  }



# small script to assign atom names to type numbers in LAMMPS .
# (c) 2008 Axel Kohlmeyer <akohlmey@cmm.chem.upenn.edu>
proc lmptypetoname {mol names} {
    if {"$mol" == "top"} {
        set mol [molinfo top]
    }

    set t 1
    foreach n $names {
        incr t
        set sel [atomselect $mol "type $t"]
        $sel set name $n
        $sel delete
    }
    return 0
}



# Delete unused atoms!
proc delUnbondedAtoms {mol} {
  
  }




# Modified by Gerdl: VMD allows only 12 bonds max.
# small script to extract bonding info from a lammps data file
# so that VMD will display the correct bonds for CG-MD.
# (c) 2007 Axel Kohlmeyer <akohlmey@cmm.chem.upenn.edu>
#
#  now added params fromB, toB : from and to Bond IDs
#
#   lmpbondsfromdata top spass.dat 1 5000
#
proc lmpbondsfromdata {mol filename fromB toB} {
    
    if {"$mol" == "top"} {
        set mol [molinfo top]
    }

    # create an empty bondlist
    set na [molinfo $mol get numatoms]; # number of atoms
    set nb 0;                           # number of bonds
    set bl {};                          # bond list
    for {set i 0} {$i < $na} {incr i} {
        lappend bl {}
    }

    # open lammps data file
    if {[catch {open $filename r} fp]} {
        puts stderr "could not open file $filename"
        return -1
    }

    # read file line by line until we hit the Bonds keyword
    while {[gets $fp line] >= 0} {
        # pick number of bonds
        regexp {^\s*(\d+)\s+bonds} $line dummy nb

        if { [regexp {^\s*Bonds} $line] } {
            puts "nbonds= $nb\nnow reading Bonds section"
            break
        }
    }

    # skip one line
    gets $fp line
    # read the bonds data
    for {set i 0} {$i < $nb} {incr i} {
        gets $fp line
        # grep bond numbers from entry and adjust to VMD numbering style
        regexp {^\s*(\d+)\s+\d+\s+(\d+)\s+(\d+)} $line dummy bond_id ba bb
        incr ba -1
        incr bb -1

        # are we in the bond_id range?
        if {($bond_id > $toB) || ($bond_id < $fromB)} {
           continue
           }

        # sanity check
        if { ($ba > $na) || ($bb > $na) } {
            puts stderr "number of atoms in VMD molecule ($na) does not match data file"
            return -1
        }

        # add bond from ba to bb and...   take care, it's max 12 bonds!
        set bn [lindex $bl $ba]
        if { [llength $bn] < 12} {
           lappend bn $bb
           set bl [lreplace $bl $ba $ba $bn]
           }
        
        # from bb to ba!
        set bn [lindex $bl $bb]
        if { [llength $bn] < 12} {
           lappend bn $ba
           set bl [lreplace $bl $bb $bb $bn]
           }
    }
    close $fp

    set sel [atomselect $mol all]
    $sel setbonds $bl
    $sel delete
    return 0
}









