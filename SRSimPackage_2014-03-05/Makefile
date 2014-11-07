prepare:
	## 
	## setting up some symbolic links...
	## 

	cd RuleSys/SRSim/;       ln -fs -t . ../*.h
	cd RuleSys/SRSim/neuneu; ln -fs -t . ../../neuneu/*.h
	cd lammpsCompilation;    ln -fs -t . ../LammpsModules/*.h
	cd lammpsCompilation;    ln -fs -t . ../LammpsModules/*.cpp
	cd lammpsCompilation;    ln -fs -t . ../LammpsModules/neuneu/*.h
	cd lammpsCompilation;    ln -fs -t . ../LammpsModules/neuneu/*.cpp

	## 
	##   Ok, ready to compile!"
	##   Now you can compile by typing 'make lmp_srsim'"
	##   the SRSim executable can be found at 'lammpsCompilation/lmp_srsim' afterwards."
	## 



lmp_srsim: RuleSys/libRuleSys.a force_look
	cd lammpsCompilation ;  $(MAKE) $(MFLAGS) lmp_srsim

RuleSys/testNeuMap: force_look
	cd RuleSys ;  $(MAKE) $(MFLAGS) testNeuMap

RuleSys/createGeo: force_look
	cd RuleSys ;  $(MAKE) $(MFLAGS) createGeo

RuleSys/libRuleSys.a: force_look RuleSys/createGeo
	cd RuleSys ;  $(MAKE) $(MFLAGS) libRuleSys.a

	
force_look:
	true


clean:
	cd RuleSys ; make clean
	cd lammpsCompilation ; make clean




NEWPACK := SRSimPackage_`date "+%Y-%m-%d"`
package: clean
	mkdir       $(NEWPACK)
	cp Makefile $(NEWPACK)
	cp INSTALL  $(NEWPACK)
	cp LICENSE  $(NEWPACK)
	cp README   $(NEWPACK)
	
	cp -rP lammpsCompilation   $(NEWPACK)      # P to preserve symlinks
	cp -rP RuleSys             $(NEWPACK)      # P to preserve symlinks
	cp -r  helpers             $(NEWPACK)
	cp -r  LammpsModules       $(NEWPACK)
	mkdir                      $(NEWPACK)/docs
	mkdir                      $(NEWPACK)/docs/examples
	cp -r  docs/examples/001.scaffold              $(NEWPACK)/docs/examples/
	cp -r  docs/examples/002.microtubule           $(NEWPACK)/docs/examples/
	cp -r  docs/examples/005.simpleTutorial        $(NEWPACK)/docs/examples/
	cp -r  docs/examples/006.exchangeRules         $(NEWPACK)/docs/examples/
	cp -r  docs/examples/007.NeuNeuInputCounter    $(NEWPACK)/docs/examples/
	cp     docs/examples/ConversionOfOldModels.txt $(NEWPACK)/docs/examples/
	cp     docs/Manual.pdf $(NEWPACK)/docs/Manual.pdf

	# remove all symlinks
	find $(NEWPACK)/ -type l -delete

	# TODO: remove .svn directories
	find $(NEWPACK)/ -type d -path *.svn* -print | xargs /bin/rm -rf

	# TODO: tar.bz2 everything automatically!
	tar cjf $(NEWPACK).tar.bz2 $(NEWPACK)

