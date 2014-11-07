<?xml version="1.0"?> 
<molecule-geometry-definition> 
  <version value="1.01"/>

  <GeneralProperties>
     <property name="GPT_Devi_Dist"  value="0.5"/>
     <property name="GPT_Devi_Angle" value="50"/>
     <property name="GPT_Mol_Mass"   value="1"/>
     <property name="GPT_Mol_Rad"    value="1"/>
     <property name="GPT_Site_Dist"  value="1"/>
     
     <property name="GPT_Force_Repulsion"   value= "5.0"/>
     <property name="GPT_Force_Bond"        value= "5.0"/>
     <property name="GPT_Force_Angle"       value="15.0"/>
     <property name="GPT_Force_Dihedral"    value= "5.0"/>
     <property name="GPT_Temperature"       value="300"/>
     <property name="GPT_Refractory"        value="50"/>
     
     <property name="GPT_Option_Dihedrals"  value="0"/>
     <property name="GPT_Option_Impropers"  value="0"/>
     <property name="GPT_Option_Rigid"      value="0"/>
  </GeneralProperties>


  <ReaktionGeometry>
     <DistanceDeviation value="0.5" />
     <AngularDeviation value="50" />
  </ReaktionGeometry>



  <molecule name="Ta" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="a"  phi="104" theta="90" dist="1.0" />
     <site name="u"  phi="0" theta="0" dist="1.0" />
     <site name="x"  phi="0" theta="180" dist="1.0" />
     <site name="bl" phi=    "0" theta="101" dist="1.0" />
     <site name="br" phi="208.0" theta="79"  dist="1.0" />
  </molecule>

  <molecule name="Tb" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="x"  phi=    "0" theta="0"   dist="1.0" />
     <site name="a"  phi=  "104" theta="90"  dist="1.0" />
     <site name="d"  phi=    "0" theta="180" dist="1.0" />
     <site name="bl" phi=    "0" theta="101" dist="1.0" />
     <site name="br" phi="208.0" theta="79"  dist="1.0" />
  </molecule>

  <molecule name="S" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="u" phi="0" theta="0" dist="1.0" />
     <site name="d" phi="0" theta="180" dist="3.0" />
     <site name="d1" phi="0" theta="180" dist="1.0" />
     <site name="bl" phi=    "0" theta="101" dist="1.0" />
     <site name="br" phi="208.0" theta="79"  dist="1.0" />
  </molecule>
  
  <molecule name="M" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="a" phi= "0" theta="90" dist="1.0" />
     <site name="x" phi="30" theta="0"  dist="1.0" />
     <site name="f" phi="180" theta="90"  dist="1.0" />
  </molecule>
  
  <molecule name="Fr" >
     <mass   value="10.0" />
     <radius value="3.0" />
     <site name="a" phi="90" theta="90" dist="3.0" />
  </molecule>

</molecule-geometry-definition> 

