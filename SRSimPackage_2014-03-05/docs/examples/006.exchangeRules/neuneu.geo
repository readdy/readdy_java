<?xml version="1.0"?> 
<molecule-geometry-definition> 

  <version value="1.01"/>

  <GeneralProperties>
     <property name="GPT_Devi_Dist"  value="2"/>
     <property name="GPT_Devi_Angle" value="15"/>
     <property name="GPT_Mol_Mass"   value="50"/>
     <property name="GPT_Mol_Rad"    value="1.0"/>
     
     <property name="GPT_Site_Dist"         value="1.0"/>
     <property name="GPT_Refractory"        value="50.0"/>
     
     <property name="GPT_Force_Repulsion"   value="5"/>
     <property name="GPT_Force_Bond"        value="5"/>
     <property name="GPT_Force_Angle"       value="5"/>
     <property name="GPT_Force_Dihedral"    value="100"/>
     <property name="GPT_Temperature"       value="300"/>
     
     <property name="GPT_Option_Dihedrals"  value="0"/>
     <property name="GPT_Option_Impropers"  value="0"/>
     <property name="GPT_Option_Rigid"      value="0"/>
  </GeneralProperties>


  <molecule name="ed" >
     <mass   value="1.0" />
     <radius value="1.0" />
     <site name="ex" phi="0"   theta="0"    dist="1.0" />
     <site name="ok" phi="0"   theta="0"    dist="1.0" />
     <site name="c"  phi="0"   theta="0"    dist="1.0" />
     <site name="c"  phi="0"   theta="170"  dist="1.0" />
     <site name="c"  phi="180" theta="120"  dist="1.0" />
     <!--
     <site name="c" phi="270" theta="90" dist="1.0" />
     <site name="c" phi="0" theta="180"  dist="1.0" />
     <site name="c" phi="0" theta="0"    dist="1.0" />
     -->
  </molecule>
  
  
  <DihedralAngles>
    <!--
    <dihedral around="A(x,a!1).B(b!1,x)" angle="270" />
    <dihedral around="D(x,d!1).D(e!1,x)" angle="0" />
    -->
  </DihedralAngles>

</molecule-geometry-definition> 

