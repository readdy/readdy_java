# try testmain

# TARGET = /usr
# TARGET = /usr/local
# TARGET = /home/stud/bid02/gerdl/local

# official configuration:
# LOCALBIN = $(TARGET)/bin
# LIBDIR = $(TARGET)/lib
INCDIR = .


# ###############################################
#  Do not change this stuff, please...

OBJS = templ_molecule.o templ_site.o names_manager.o bng_rule_builder.o sr_error.o \
       reactant_template.o multi_mol_reactant_template.o rule_set.o \
       rule_builder.o site_reactant_template.o molecule_type_manager.o \
       bound_reactant_template.o random_generator.o gillespie_1st_order.o \
       simple_mass_action_kinetics.o molecule.o sr_model.o \
       uniform_distribution_start_state.o start_state_definition.o \
       geometry_definition.o xml_stuff.o kinetics_definition.o \
       modification_reactant_template.o observables_manager.o templ_realization.o \
       neuneu/neuneu_rt_builder.o
       
CC       = g++-4.7
CPPFLAGS = -g -I$(INCDIR)
LIBS     = -lxerces-c
# -g means debug enabled...

%.o : %.cpp %.h
	$(CC) $(CPPFLAGS) -c $< -o $@

all: libRuleSys.a testmain createGeo

libRuleSys.a: $(OBJS) testmain
	if [ -e libRuleSys.a ] ; then rm libRuleSys.a ; fi
	ar cr libRuleSys.a $(OBJS)
	echo "Done creating libRuleSys.a"

testmain: $(OBJS) testmain.cpp
	$(CC) $(CPPFLAGS) testmain.cpp -o testmain $(OBJS) $(LIBS)
	echo "Done compiling testmain."
	
testNeuMap: $(OBJS) neuneu/testNeuMap.cpp
	$(CC) $(CPPFLAGS) neuneu/testNeuMap.cpp -o neuneu/testNeuMap $(OBJS) $(LIBS)
	echo "Done compiling testNeuMap."
	
createGeo: $(OBJS) createGeo.cpp
	$(CC) $(CPPFLAGS) createGeo.cpp -o createGeo $(OBJS) $(LIBS)
	echo "Done compiling createGeo."

prepare:
	mkdir -p $(INCDIR)/SRSim
	mkdir -p $(LOCALBIN)
	mkdir -p $(LIBDIR)
	cp *.h $(INCDIR)/SRSim/

#install: createGeo libRuleSys.a
#	cp createGeo $(LOCALBIN)/createGeo
#	cp testmain  $(LOCALBIN)/testSRSim
#	cp libRuleSys.a $(LIBDIR)
			
run:	testmain
	./testmain

dot:
	circo rt1.dot -o rt1.ps -Tps
	circo rt2.dot -o rt2.ps -Tps
	gv rt1.ps & gv rt2.ps &

clean:
	rm -f *.o
	rm -f neuneu/*.o
	rm -f libRuleSys.a

# install_includes:
