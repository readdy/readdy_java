/*===========================================================================*\
 *           ReaDDy - The Library for Reaction Diffusion Dynamics              *
 * =========================================================================== *
 * Copyright (c) 2010-2013, Johannes Schöneberg, Frank Noé, FU Berlin          *
 * All rights reserved.                                                        *
 *                                                                             *
 * Redistribution and use in source and binary forms, with or without          *
 * modification, are permitted provided that the following conditions are met: *
 *                                                                             *
 *     * Redistributions of source code must retain the above copyright        *
 *       notice, this list of conditions and the following disclaimer.         *
 *     * Redistributions in binary form must reproduce the above copyright     *
 *       notice, this list of conditions and the following disclaimer in the   *
 *       documentation and/or other materials provided with the distribution.  *
 *     * Neither the name of Johannes Schöneberg or Frank Noé or the FU Berlin *
 *       nor the names of its contributors may be used to endorse or promote   *
 *       products derived from this software without specific prior written    *
 *       permission.                                                           *
 *                                                                             *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" *
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE   *
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE  *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE   *
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR         *
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF        *
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS    *
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN     *
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)     *
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  *
 * POSSIBILITY OF SUCH DAMAGE.                                                 *
 *                                                                             *
\*===========================================================================*/
package readdy;

import java.util.HashMap;
import java.util.HashSet;
import readdy.api.assembly.IReaDDySimulatorFactory;
import readdy.api.sim.IReaDDySimulator;
import readdy.impl.assembly.ReaDDySimulatorFactory;
import readdy.impl.sim.ReaDDySimulator;

/**
 * @author schoeneberg
 */
public class Main {

    
    final static boolean testingMode = false;
    final static String platformDependentPath = (String) System.getProperty("user.dir");
    final static String simulationModelDependendPath = "/test/fullTestSimulation_diskVesicle/";
    final static String input = "ReaDDy_input/";
    final static String output = "ReaDDy_output/";
    final static String path = platformDependentPath + simulationModelDependendPath;
    final static String[] args = new String[]{
        "-param_global", path + input + "param_global.xml",
        "-param_groups", path + input + "param_groups.xml",
        "-param_particles", path + input + "param_particles.xml",
        "-param_reactions", path + input + "param_reactions.xml",
        "-tplgy_coordinates", path + input + "tplgy_coordinates.xml",
        "-tplgy_groups", path + input + "tplgy_groups.xml",
        "-tplgy_potentials", path + input + "tplgy_potentials.xml",
        "-output_path", path + output};
    // version number:
    // meaning of the positions
    // 1. major release
    // 2. minor release
    // 3. in {0,1,2,3} meaning alpha, beta, release candidate, release
    // 4. maintainance, increasing number for every bug fix or submission given 1.,2., and 3.
    // the official version number consists of the first two numbers only.
    final static int[] ReaDDy_Version = ReaDDySimulator.version;

    public static void main(String[] args) {

        if(testingMode){
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("path: "+path);
        args = Main.args;
        }

        
        IReaDDySimulatorFactory readdySimulatorFactory = new ReaDDySimulatorFactory();
        String[] essentialInputFileKeys = readdySimulatorFactory.getEssentialInputFileKeys();
        String[] optionalInputFileKeys = readdySimulatorFactory.getOptionalInputFileKeys();
        String[] softwareInputKeys = readdySimulatorFactory.getSoftwareInputKeys();
        HashMap<String, String> inputValues = parseCommandLineArguments(args, essentialInputFileKeys, optionalInputFileKeys, softwareInputKeys);

        // check for software related input keys. If they are not given, run the simulation.
        boolean softwareKeysPresent = false;
        for (int i = 0; i < softwareInputKeys.length; i++) {
            if (inputValues.containsKey(softwareInputKeys[i])) {
                switch (i) {
                    case 0: // -help
                        displayHelp();
                        break;
                    case 1: // -version
                        displayVersion();
                        break;
                    case 2: // -license
                        displayLicense();
                        break;
                    default:
                        displayHelp();
                        break;
                }
                softwareKeysPresent=true;
            }
        }
        // stop the program and handle the software keys.
        if(softwareKeysPresent)return;

        // check if the essential input values are given for a simulation run
        for (String essentialKey : essentialInputFileKeys) {

            if (!inputValues.containsKey(essentialKey)) {
                displayHelp();
                throw new RuntimeException("the essential parameter '-" + essentialKey + "' is not given.");
            } else {
                if (inputValues.get(essentialKey).isEmpty()) {
                    displayHelp();
                    throw new RuntimeException("the essential parameter '-" + essentialKey + "' is empty.");
                }
            }
        }

        // run the simulation
        readdySimulatorFactory.set_inputValues(inputValues);
        IReaDDySimulator simulator = readdySimulatorFactory.createReaDDySimulator();
        simulator.runSimulation();

    }

    private static HashMap<String, String> parseCommandLineArguments(String[] args, String[] essentialInputFileKeys, String[] optionalInputFileKeys, String[] softwareInputKeys) {
        HashMap<String, String> foundInputValues = new HashMap();
        HashSet<String> essentialInputKeysSet = new HashSet();
        HashSet<String> allKeysSearchedFor = new HashSet();

        for (String essentialKey : essentialInputFileKeys) {
            essentialInputKeysSet.add("-" + essentialKey);
            allKeysSearchedFor.add("-" + essentialKey);
        }

        for (String optionalKey : optionalInputFileKeys) {
            allKeysSearchedFor.add("-" + optionalKey);
        }

        for (String softwareKey : softwareInputKeys) {
            allKeysSearchedFor.add("-" + softwareKey);
        }

        String currentFoundKey = "";

        for (String arg : args) {
            // check if a string matches a key that we search for
            if (allKeysSearchedFor.contains(arg)) {
                System.out.println("ARG arg" + arg);
                currentFoundKey = arg.replaceAll("-", "");
                foundInputValues.put(currentFoundKey, "");
                continue;
            } else {
                // if we read a key before, store the string as its value
                if (!currentFoundKey.contentEquals("")) {
                    foundInputValues.put(currentFoundKey, arg);
                    currentFoundKey = "";
                }
            }
        }

        return foundInputValues;
    }

    private static void displayHelp() {
        displayVersion();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("USAGE");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("essential input:");
        System.out.println("-param_global [param_global.xml]                            global parameters");
        System.out.println("-param_groups [param_groups.xml]                            group parameters");
        System.out.println("-param_particles [param_particles.xml]                      particle parameters");
        System.out.println("-param_reactions [param_reactions.xml]                      reaction parameters");
        System.out.println("-tplgy_coordinates [tplgy_coordinates.xml]                  particle topology");
        System.out.println("-tplgy_groups [tplgy_groups.xml]                            group topology");
        System.out.println("-tplgy_potentials [tplgy_potentials.xml]                    potential topology");
        System.out.println("");
        System.out.println("optional input:");
        System.out.println("-output_path [path]                                         output path");
        System.out.println("-react_elmtlRk [react_elmtlRk.xml]                          input the elemental, particle ");
        System.out.println("                                                            based reactions directly.");
        System.out.println("-core [BD, MC, BD_OpenMM]                                   available cores:");    
        System.out.println("                                                            BD: Default Brownian dynamics");
        System.out.println("                                                            MC: Monte Carlo core");
        System.out.println("                                                            BD_OpenMM: OpenMM core");
        System.out.println("");
        System.out.println("other:");
        System.out.println("-help                                                       display this help");
        System.out.println("-version                                                    display code version");
        System.out.println("-license                                                    display code license");
        System.out.println("--------------------------------------------------------------------------------");

    }

    private static void displayVersion() {
        System.out.println("================================================================================");
        System.out.println("ReaDDy - The Library for Reaction Diffusion Dynamics");
        System.out.println("Copyright (c) 2010-2013, Johannes Schöneberg, Frank Noé, FU Berlin. ");
        System.out.println("All rights reserved.");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(getVersionNumberString());
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void displayLicense() {
        System.out.println("/*===========================================================================*\\");
        System.out.println("*           ReaDDy - The Library for Reaction Diffusion Dynamics              *");
        System.out.println("* =========================================================================== *");
        System.out.println("* Copyright (c) 2010-2013, Johannes Schöneberg, Frank Noé, FU Berlin          *");
        System.out.println("* All rights reserved.                                                        *");
        System.out.println("*                                                                             *");
        System.out.println("* Redistribution and use in source and binary forms, with or without          *");
        System.out.println("* modification, are permitted provided that the following conditions are met: *");
        System.out.println("*                                                                             *");
        System.out.println("*     * Redistributions of source code must retain the above copyright        *");
        System.out.println("*       notice, this list of conditions and the following disclaimer.         *");
        System.out.println("*     * Redistributions in binary form must reproduce the above copyright     *");
        System.out.println("*       notice, this list of conditions and the following disclaimer in the   *");
        System.out.println("*       documentation and/or other materials provided with the distribution.  *");
        System.out.println("*     * Neither the name of Johannes Schöneberg or Frank Noé or the FU Berlin *");
        System.out.println("*       nor the names of its contributors may be used to endorse or promote   *");
        System.out.println("*       products derived from this software without specific prior written    *");
        System.out.println("*       permission.                                                           *");
        System.out.println("*                                                                             *");
        System.out.println("* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" *");
        System.out.println("* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE   *");
        System.out.println("* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE  *");
        System.out.println("* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE   *");
        System.out.println("* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR         *");
        System.out.println("* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF        *");
        System.out.println("* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS    *");
        System.out.println("* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN     *");
        System.out.println("* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)     *");
        System.out.println("* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  *");
        System.out.println("* POSSIBILITY OF SUCH DAMAGE.                                                 *");
        System.out.println("*                                                                             *");
        System.out.println("\\*===========================================================================*//");
    }

    private static String getVersionNumberString() {
        String s = "";
        s += "Version ";
        for (int i = 0; i < ReaDDy_Version.length; i++) {
            if (i == ReaDDy_Version.length - 1) {
                s += ReaDDy_Version[i] + "";
            } else {
                s += ReaDDy_Version[i] + ".";
            }
        }
        return s;
    }
}
