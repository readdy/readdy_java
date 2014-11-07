<?xml version='1.0' encoding='ISO-8859-1' standalone='yes'?>
<tagfile>
  <compound kind="dir">
    <name>/home/stud/bid02/gerdl/RulesIn3D/SRSim/RuleSys/</name>
    <path>/home/stud/bid02/gerdl/RulesIn3D/SRSim/RuleSys/</path>
    <filename>dir_000000.html</filename>
    <file>bng_rule_builder.cpp</file>
    <file>bng_rule_builder.h</file>
    <file>bng_rule_writer.cpp</file>
    <file>bng_rule_writer.h</file>
    <file>bound_reactant_template.cpp</file>
    <file>bound_reactant_template.h</file>
    <file>defs.h</file>
    <file>geometry_definition.cpp</file>
    <file>geometry_definition.h</file>
    <file>gillespie_1st_order.cpp</file>
    <file>gillespie_1st_order.h</file>
    <file>kinetics_definition.cpp</file>
    <file>kinetics_definition.h</file>
    <file>molecule.cpp</file>
    <file>molecule.h</file>
    <file>molecule_type_manager.cpp</file>
    <file>molecule_type_manager.h</file>
    <file>moleculetypemanager_interface.h</file>
    <file>multi_mol_reactant_template.cpp</file>
    <file>multi_mol_reactant_template.h</file>
    <file>names_manager.cpp</file>
    <file>names_manager.h</file>
    <file>random_generator.cpp</file>
    <file>random_generator.h</file>
    <file>reactant_template.cpp</file>
    <file>reactant_template.h</file>
    <file>rule_builder.cpp</file>
    <file>rule_builder.h</file>
    <file>rule_set.cpp</file>
    <file>rule_set.h</file>
    <file>simple_mass_action_kinetics.cpp</file>
    <file>simple_mass_action_kinetics.h</file>
    <file>site_reactant_template.cpp</file>
    <file>site_reactant_template.h</file>
    <file>sr_error.cpp</file>
    <file>sr_error.h</file>
    <file>sr_model.cpp</file>
    <file>sr_model.h</file>
    <file>SRSim.cpp</file>
    <file>start_state_definition.cpp</file>
    <file>start_state_definition.h</file>
    <file>templ_molecule.cpp</file>
    <file>templ_molecule.h</file>
    <file>templ_site.cpp</file>
    <file>templ_site.h</file>
    <file>testmain.cpp</file>
    <file>testmem.cpp</file>
    <file>uniform_distribution_start_state.cpp</file>
    <file>uniform_distribution_start_state.h</file>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::BNGRuleBuilder</name>
    <filename>classSRSim__ns_1_1BNGRuleBuilder.html</filename>
    <base>SRSim_ns::RuleBuilder</base>
    <member kind="function">
      <type>void</type>
      <name>readFile</name>
      <anchor>a2</anchor>
      <arglist>(string fname, SRModel *_into)</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplate *</type>
      <name>parseBNGTemplate</name>
      <anchor>a3</anchor>
      <arglist>(string t)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::BoundReactantTemplate</name>
    <filename>classSRSim__ns_1_1BoundReactantTemplate.html</filename>
    <base>SRSim_ns::ReactantTemplate</base>
    <member kind="function">
      <type></type>
      <name>BoundReactantTemplate</name>
      <anchor>a0</anchor>
      <arglist>(MultiMolReactantTemplate *wmrt, TemplSite *s1)</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplateType</type>
      <name>getRTType</name>
      <anchor>a1</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>TemplSite *</type>
      <name>getStartSite</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setStartSite</name>
      <anchor>a3</anchor>
      <arglist>(TemplSite *s1)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>matchMolecule</name>
      <anchor>a4</anchor>
      <arglist>(Molecule *against)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>matchMolecule</name>
      <anchor>a5</anchor>
      <arglist>(Molecule *against, int molSite)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>equals</name>
      <anchor>a6</anchor>
      <arglist>(ReactantTemplate *other)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::SiteGeo</name>
    <filename>classSRSim__ns_1_1SiteGeo.html</filename>
    <member kind="function">
      <type></type>
      <name>SiteGeo</name>
      <anchor>a1</anchor>
      <arglist>(double _p, double _t, double _d)</arglist>
    </member>
    <member kind="variable">
      <type>double</type>
      <name>phi</name>
      <anchor>o0</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>double</type>
      <name>theta</name>
      <anchor>o1</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>double</type>
      <name>dist</name>
      <anchor>o2</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>bool</type>
      <name>ready</name>
      <anchor>o3</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::GeometryDefinition</name>
    <filename>classSRSim__ns_1_1GeometryDefinition.html</filename>
    <member kind="function">
      <type>void</type>
      <name>initSize</name>
      <anchor>a2</anchor>
      <arglist>(MoleculeTypeManager *mtm)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>readGeoFile</name>
      <anchor>a3</anchor>
      <arglist>(string fname, NamesManager *names, MoleculeTypeManager *mtm)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>areMolsReady</name>
      <anchor>a4</anchor>
      <arglist>(NamesManager *names)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::Gillespie1stOrder</name>
    <filename>classSRSim__ns_1_1Gillespie1stOrder.html</filename>
    <member kind="function">
      <type>void</type>
      <name>init</name>
      <anchor>a1</anchor>
      <arglist>(SRModel *model)</arglist>
    </member>
    <member kind="function">
      <type>double</type>
      <name>timeToReaction</name>
      <anchor>a2</anchor>
      <arglist>(vector&lt; int &gt; &amp;amountTempls)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>typeOfReaction</name>
      <anchor>a3</anchor>
      <arglist>(vector&lt; int &gt; &amp;amountTempls)</arglist>
    </member>
    <member kind="function">
      <type>double</type>
      <name>correctionTemplAmount</name>
      <anchor>a4</anchor>
      <arglist>(int it)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::KineticsDefinition</name>
    <filename>classSRSim__ns_1_1KineticsDefinition.html</filename>
    <member kind="function" virtualness="pure">
      <type>virtual double</type>
      <name>getRate</name>
      <anchor>a0</anchor>
      <arglist>(int rid)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual void</type>
      <name>setRate</name>
      <anchor>a1</anchor>
      <arglist>(int rid, double rate)=0</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::Molecule</name>
    <filename>classSRSim__ns_1_1Molecule.html</filename>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>getType</name>
      <anchor>a0</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>numSites</name>
      <anchor>a1</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>getSiteType</name>
      <anchor>a2</anchor>
      <arglist>(int idx)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual Molecule *</type>
      <name>getMoleculeAtSite</name>
      <anchor>a3</anchor>
      <arglist>(int idx)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>getModificationAtSite</name>
      <anchor>a4</anchor>
      <arglist>(int idx)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>getUniqueID</name>
      <anchor>a5</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>writeToDotFile</name>
      <anchor>a6</anchor>
      <arglist>(string fname)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::MoleculeTypeManager</name>
    <filename>classSRSim__ns_1_1MoleculeTypeManager.html</filename>
    <member kind="function">
      <type>int</type>
      <name>getSiteType</name>
      <anchor>a2</anchor>
      <arglist>(int mID, int sID)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numMols</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numSites</name>
      <anchor>a4</anchor>
      <arglist>(int mID)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>registerMolecule</name>
      <anchor>a5</anchor>
      <arglist>(TemplMolecule *m)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>registerAllMolecules</name>
      <anchor>a6</anchor>
      <arglist>(ReactantTemplate *t)</arglist>
    </member>
    <member kind="variable">
      <type>vector&lt; TemplMolecule * &gt;</type>
      <name>molTypes</name>
      <anchor>o0</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::MultiMolReactantTemplate</name>
    <filename>classSRSim__ns_1_1MultiMolReactantTemplate.html</filename>
    <base>SRSim_ns::ReactantTemplate</base>
    <member kind="function">
      <type></type>
      <name>MultiMolReactantTemplate</name>
      <anchor>a0</anchor>
      <arglist>(MultiMolReactantTemplate *other)</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplateType</type>
      <name>getRTType</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>matchMolecule</name>
      <anchor>a3</anchor>
      <arglist>(Molecule *against)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>equals</name>
      <anchor>a4</anchor>
      <arglist>(ReactantTemplate *other)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::NamesManager</name>
    <filename>classSRSim__ns_1_1NamesManager.html</filename>
    <member kind="function">
      <type></type>
      <name>NamesManager</name>
      <anchor>a0</anchor>
      <arglist>(bool singleUser=false)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getID</name>
      <anchor>a2</anchor>
      <arglist>(NameTp tp, string name)</arglist>
    </member>
    <member kind="function">
      <type>string</type>
      <name>getName</name>
      <anchor>a3</anchor>
      <arglist>(NameTp tp, int id)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getID</name>
      <anchor>a4</anchor>
      <arglist>(string name)</arglist>
    </member>
    <member kind="function">
      <type>string</type>
      <name>getName</name>
      <anchor>a5</anchor>
      <arglist>(int id)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::RandomGenerator</name>
    <filename>classSRSim__ns_1_1RandomGenerator.html</filename>
    <member kind="function">
      <type></type>
      <name>RandomGenerator</name>
      <anchor>a0</anchor>
      <arglist>(int seed)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>init</name>
      <anchor>a2</anchor>
      <arglist>(int seed)</arglist>
    </member>
    <member kind="function">
      <type>double</type>
      <name>uniform</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>double</type>
      <name>gaussian</name>
      <anchor>a4</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::ReactantTemplate</name>
    <filename>classSRSim__ns_1_1ReactantTemplate.html</filename>
    <member kind="function">
      <type></type>
      <name>ReactantTemplate</name>
      <anchor>a1</anchor>
      <arglist>(ReactantTemplate *t1)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>addMolecule</name>
      <anchor>a3</anchor>
      <arglist>(TemplMolecule *m)</arglist>
    </member>
    <member kind="function">
      <type>TemplMolecule *</type>
      <name>getMolecule</name>
      <anchor>a4</anchor>
      <arglist>(int idx)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numMolecules</name>
      <anchor>a5</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>vector&lt; TemplSite * &gt;</type>
      <name>findMissingBond</name>
      <anchor>a6</anchor>
      <arglist>(vector&lt; ReactantTemplate * &gt; in)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>countBonds</name>
      <anchor>a7</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>writeTemplateToDotFile</name>
      <anchor>a8</anchor>
      <arglist>(NamesManager *names, string fname)</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual bool</type>
      <name>matchMolecule</name>
      <anchor>a9</anchor>
      <arglist>(Molecule *against)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual bool</type>
      <name>equals</name>
      <anchor>a10</anchor>
      <arglist>(ReactantTemplate *other)=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual ReactantTemplateType</type>
      <name>getRTType</name>
      <anchor>a11</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>checkConnectivity</name>
      <anchor>a12</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>checkOneSitePerMolecule</name>
      <anchor>a13</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" protection="protected">
      <type>bool</type>
      <name>matchSingleTM</name>
      <anchor>b0</anchor>
      <arglist>(Molecule *against, TemplMolecule *start, bool exact=false, int i_startSite=-1, int j_startSite=-1)</arglist>
    </member>
    <member kind="function" protection="protected">
      <type>void</type>
      <name>unmarkTemplate</name>
      <anchor>b1</anchor>
      <arglist>(TemplMolecule *until=NULL)</arglist>
    </member>
    <member kind="function" protection="protected">
      <type>bool</type>
      <name>recTryMatching</name>
      <anchor>b2</anchor>
      <arglist>(Molecule *m, TemplMolecule *t, int recLayer, int i_startSite=-1, int j_startSite=-1)</arglist>
    </member>
    <member kind="function" protection="protected">
      <type>bool</type>
      <name>recTryMatchingExact</name>
      <anchor>b3</anchor>
      <arglist>(TemplMolecule *m, TemplMolecule *t, int recLayer)</arglist>
    </member>
    <member kind="variable" protection="protected">
      <type>vector&lt; TemplMolecule * &gt;</type>
      <name>mols</name>
      <anchor>p0</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable" protection="protected">
      <type>stack&lt; TemplMolecule * &gt;</type>
      <name>markingHistory</name>
      <anchor>p1</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable" protection="protected">
      <type>map&lt; int, int &gt;</type>
      <name>markedMolecules</name>
      <anchor>p2</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable" protection="protected">
      <type>TemplateGeo *</type>
      <name>geo</name>
      <anchor>p3</anchor>
      <arglist></arglist>
    </member>
    <class kind="class">SRSim_ns::ReactantTemplate::AssignmentConstructor</class>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::RuleBuilder</name>
    <filename>classSRSim__ns_1_1RuleBuilder.html</filename>
    <member kind="function" protection="protected">
      <type>bool</type>
      <name>createSiteTemplatesIfNecessary</name>
      <anchor>b0</anchor>
      <arglist>(vector&lt; ReactantTemplate * &gt; &amp;in, vector&lt; ReactantTemplate * &gt; &amp;out)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::RuleTp</name>
    <filename>classSRSim__ns_1_1RuleTp.html</filename>
    <member kind="function">
      <type>string</type>
      <name>toString</name>
      <anchor>a1</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="variable">
      <type>vector&lt; int &gt;</type>
      <name>in</name>
      <anchor>o0</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>vector&lt; int &gt;</type>
      <name>out</name>
      <anchor>o1</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>bool</type>
      <name>bindRule</name>
      <anchor>o2</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>bool</type>
      <name>breakRule</name>
      <anchor>o3</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::RuleSet</name>
    <filename>classSRSim__ns_1_1RuleSet.html</filename>
    <member kind="function">
      <type>int</type>
      <name>numTemplates</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numRules</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>vector&lt; int &gt;</type>
      <name>addNewRule</name>
      <anchor>a4</anchor>
      <arglist>(vector&lt; ReactantTemplate * &gt; in, vector&lt; ReactantTemplate * &gt; out, bool bindRule, bool breakRule, bool reversible)</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplate *</type>
      <name>getRT</name>
      <anchor>a5</anchor>
      <arglist>(int templID)</arglist>
    </member>
    <member kind="function">
      <type>RuleTp *</type>
      <name>getRule</name>
      <anchor>a6</anchor>
      <arglist>(int ruleID)</arglist>
    </member>
    <member kind="function">
      <type>vector&lt; RuleTp * &gt; &amp;</type>
      <name>getRules</name>
      <anchor>a7</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>fillFittingTemplates</name>
      <anchor>a8</anchor>
      <arglist>(Molecule *m, vector&lt; int &gt; &amp;wholeMolTempls, vector&lt; vector&lt; int &gt; &gt; &amp;specificTempls, vector&lt; int &gt; &amp;amountTempls)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>fillFittingRules</name>
      <anchor>a9</anchor>
      <arglist>(vector&lt; int &gt; &amp;wmTemplsA, vector&lt; int &gt; &amp;wmTemplsB, vector&lt; int &gt; &amp;reas)</arglist>
    </member>
    <member kind="variable">
      <type>vector&lt; ReactantTemplate * &gt;</type>
      <name>templates</name>
      <anchor>o0</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>vector&lt; RuleTp * &gt;</type>
      <name>rules</name>
      <anchor>o1</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::SimpleMassActionKinetics</name>
    <filename>classSRSim__ns_1_1SimpleMassActionKinetics.html</filename>
    <base>SRSim_ns::KineticsDefinition</base>
    <member kind="function">
      <type>double</type>
      <name>getRate</name>
      <anchor>a2</anchor>
      <arglist>(int rid)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setRate</name>
      <anchor>a3</anchor>
      <arglist>(int rid, double rate)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::SiteReactantTemplate</name>
    <filename>classSRSim__ns_1_1SiteReactantTemplate.html</filename>
    <base>SRSim_ns::ReactantTemplate</base>
    <member kind="function">
      <type></type>
      <name>SiteReactantTemplate</name>
      <anchor>a0</anchor>
      <arglist>(MultiMolReactantTemplate *wmrt, TemplSite *s1)</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplateType</type>
      <name>getRTType</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>TemplSite *</type>
      <name>getStartSite</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setStartSite</name>
      <anchor>a4</anchor>
      <arglist>(TemplSite *s1)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>matchMolecule</name>
      <anchor>a5</anchor>
      <arglist>(Molecule *against)</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>equals</name>
      <anchor>a6</anchor>
      <arglist>(ReactantTemplate *other)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::SRError</name>
    <filename>classSRSim__ns_1_1SRError.html</filename>
    <member kind="function" static="yes">
      <type>static void</type>
      <name>critical</name>
      <anchor>e0</anchor>
      <arglist>(const char *)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static void</type>
      <name>critical</name>
      <anchor>e1</anchor>
      <arglist>(const char *str, const char *str2)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static void</type>
      <name>warning</name>
      <anchor>e2</anchor>
      <arglist>(const char *str)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static void</type>
      <name>warning</name>
      <anchor>e3</anchor>
      <arglist>(const char *str, const char *str2)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::SRModel</name>
    <filename>classSRSim__ns_1_1SRModel.html</filename>
    <member kind="function">
      <type></type>
      <name>SRModel</name>
      <anchor>a0</anchor>
      <arglist>(int rndSeed)</arglist>
    </member>
    <member kind="variable">
      <type>RandomGenerator *</type>
      <name>random</name>
      <anchor>o0</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>RuleSet *</type>
      <name>ruleset</name>
      <anchor>o1</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>NamesManager *</type>
      <name>names</name>
      <anchor>o2</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>MoleculeTypeManager *</type>
      <name>mtm</name>
      <anchor>o3</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>KineticsDefinition *</type>
      <name>kinetics</name>
      <anchor>o4</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>StartStateDefinition *</type>
      <name>sstate</name>
      <anchor>o5</anchor>
      <arglist></arglist>
    </member>
    <member kind="variable">
      <type>GeometryDefinition *</type>
      <name>geo</name>
      <anchor>o6</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::StartStateDefinition</name>
    <filename>classSRSim__ns_1_1StartStateDefinition.html</filename>
    <member kind="function">
      <type></type>
      <name>StartStateDefinition</name>
      <anchor>a0</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual int</type>
      <name>numItems2Create</name>
      <anchor>a2</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual Element</type>
      <name>getNextItem</name>
      <anchor>a3</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function" virtualness="pure">
      <type>virtual void</type>
      <name>reset</name>
      <anchor>a4</anchor>
      <arglist>()=0</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numTemplates</name>
      <anchor>a5</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>ReactantTemplate *</type>
      <name>getRT</name>
      <anchor>a6</anchor>
      <arglist>(int sid)</arglist>
    </member>
    <member kind="function" protection="protected">
      <type>void</type>
      <name>addTemplate</name>
      <anchor>b0</anchor>
      <arglist>(ReactantTemplate *t)</arglist>
    </member>
    <member kind="variable" protection="protected">
      <type>vector&lt; ReactantTemplate * &gt;</type>
      <name>templs</name>
      <anchor>p0</anchor>
      <arglist></arglist>
    </member>
    <class kind="class">SRSim_ns::StartStateDefinition::Element</class>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::TemplMolecule</name>
    <filename>classSRSim__ns_1_1TemplMolecule.html</filename>
    <base>SRSim_ns::Molecule</base>
    <member kind="function">
      <type></type>
      <name>TemplMolecule</name>
      <anchor>a0</anchor>
      <arglist>(int _tp, int _pt)</arglist>
    </member>
    <member kind="function">
      <type></type>
      <name>~TemplMolecule</name>
      <anchor>a1</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>TemplSite *</type>
      <name>addSite</name>
      <anchor>a2</anchor>
      <arglist>(int _tp, int _md, int _pt)</arglist>
    </member>
    <member kind="function">
      <type>TemplSite *</type>
      <name>getSite</name>
      <anchor>a3</anchor>
      <arglist>(int idx)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setRealization</name>
      <anchor>a4</anchor>
      <arglist>(int x)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getRealization</name>
      <anchor>a5</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getPattern</name>
      <anchor>a6</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getType</name>
      <anchor>a7</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numSites</name>
      <anchor>a8</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getSiteType</name>
      <anchor>a9</anchor>
      <arglist>(int idx)</arglist>
    </member>
    <member kind="function">
      <type>Molecule *</type>
      <name>getMoleculeAtSite</name>
      <anchor>a10</anchor>
      <arglist>(int idx)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getModificationAtSite</name>
      <anchor>a11</anchor>
      <arglist>(int idx)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getUniqueID</name>
      <anchor>a12</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="friend" protection="private">
      <type>friend class</type>
      <name>ReactantTemplate</name>
      <anchor>n0</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::TemplSite</name>
    <filename>classSRSim__ns_1_1TemplSite.html</filename>
    <member kind="function">
      <type></type>
      <name>TemplSite</name>
      <anchor>a0</anchor>
      <arglist>(int _tp, int _mod, int _pt)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>connectToSite</name>
      <anchor>a1</anchor>
      <arglist>(TemplSite *s2)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>disconnect</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>bool</type>
      <name>isConnected</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getType</name>
      <anchor>a4</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getPattern</name>
      <anchor>a5</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getModif</name>
      <anchor>a6</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>TemplMolecule *</type>
      <name>getMol</name>
      <anchor>a7</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>TemplSite *</type>
      <name>getOther</name>
      <anchor>a8</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="friend" protection="private">
      <type>friend class</type>
      <name>TemplMolecule</name>
      <anchor>n0</anchor>
      <arglist></arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>SRSim_ns::UniformDistributionStartState</name>
    <filename>classSRSim__ns_1_1UniformDistributionStartState.html</filename>
    <base>SRSim_ns::StartStateDefinition</base>
    <member kind="function">
      <type></type>
      <name>UniformDistributionStartState</name>
      <anchor>a0</anchor>
      <arglist>(RandomGenerator *_rg)</arglist>
    </member>
    <member kind="function">
      <type>Element</type>
      <name>getNextItem</name>
      <anchor>a2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>numItems2Create</name>
      <anchor>a3</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>reset</name>
      <anchor>a4</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>addNumber</name>
      <anchor>a5</anchor>
      <arglist>(ReactantTemplate *t, int number)</arglist>
    </member>
  </compound>
</tagfile>
