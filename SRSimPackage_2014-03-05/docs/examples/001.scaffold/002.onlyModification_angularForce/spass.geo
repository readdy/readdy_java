<?xml version="1.0"?> 
<molecule-geometry-definition> 
   <version value="1.01"/>

  <GeneralProperties>
    <property name="GPT_Devi_Dist"  value="0.5"/>
    <property name="GPT_Devi_Angle" value="180"/>
    <property name="GPT_Mol_Mass"   value="1"/>
    <property name="GPT_Mol_Rad"    value="1"/>
    <property name="GPT_Site_Dist"  value="1"/>
    
    <property name="GPT_Refractory"        value= "50.0"/>
    <property name="GPT_Force_Repulsion"   value= "5.0"/>
    <property name="GPT_Force_Bond"        value= "5.0"/>
    <property name="GPT_Force_Angle"       value=" 1.0"/>
    <property name="GPT_Force_Dihedral"    value= "5.0"/>
    <property name="GPT_Temperature"       value="300"/>
    
    <property name="GPT_Option_Dihedrals"  value="0"/>
    <property name="GPT_Option_Impropers"  value="0"/>
    <property name="GPT_Option_Rigid"      value="0"/>
  </GeneralProperties>
  
  

  <molecule name="S" >
     <mass   value="5.0" />
     <radius value="8.0" />
     <site name="t" phi="  0" theta="0" dist="8.0" />
     <site name="t" phi=" 72" theta="0" dist="8.0" />
     <site name="t" phi="144" theta="0" dist="8.0" />
     <site name="t" phi="216" theta="0" dist="8.0" />
  </molecule>

  <molecule name="A" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="r" phi="0" theta="0" dist="1.0" />
     <site name="s" phi="0" theta="100" dist="1.0" />
  </molecule>

</molecule-geometry-definition> 

