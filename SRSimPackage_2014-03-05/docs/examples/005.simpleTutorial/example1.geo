<?xml version="1.0"?> 
<molecule-geometry-definition> 
  <version value="1.01"/>

  <GeneralProperties>
     <property name="GPT_Devi_Dist"  value="0.2"/>
     <property name="GPT_Devi_Angle" value="40"/>
     <property name="GPT_Mol_Mass"   value="50"/>
     <property name="GPT_Mol_Rad"    value="1"/>
     <property name="GPT_Site_Dist"  value="1"/>
     
     <property name="GPT_Force_Repulsion"   value="1.5"/>
     <property name="GPT_Force_Bond"        value="1.5"/>
     <property name="GPT_Force_Angle"       value="1.5"/>
     <property name="GPT_Force_Dihedral"    value="1.5"/>
     <property name="GPT_Temperature"       value="300"/>
     
     <property name="GPT_Option_Dihedrals"  value="1"/>
     <property name="GPT_Option_Impropers"  value="0"/>
     <property name="GPT_Option_Rigid"      value="0"/>
  </GeneralProperties>

  <molecule name="A">
     <site name="a"  phi="0" theta="0"   dist="1" />
     <site name="c"  phi="0" theta="180" dist="1" />
     <site name="b"  phi="0" theta="90"  dist="1">
        <property name="GPT_Devi_Angle" value="30"/>
     </site>
  </molecule>
  
  <molecule name="B">
     <property name="GPT_Mol_Mass" value="30"/>
     <site name="b"  phi="0" theta="0"   dist="1" />
     <site name="e"  phi="0" theta="180" dist="1" />
  </molecule>
  
  <DihedralAngles>
    <dihedral around="A(b,a!1).A(c!1,b)" angle="10" />
  </DihedralAngles>

</molecule-geometry-definition> 

