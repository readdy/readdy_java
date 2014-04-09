/*===========================================================================*\
 *           ReaDDy - The Library for Reaction Diffusion Dynamics              *
 * =========================================================================== *
 *           ReaDDyMM - A combination of OpenMM and ReaDDy                     *
 *                    written by Johann Biedermann                             *
 *     code based on the OpenMM(tm) HelloArgon example in C++ (June 2009)      *
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

#include "OpenMM.h"
#include <cstdio>
#include <iostream>
#include <fstream>
#include <string>
#include <stdlib.h>
#include <vector>
#include <time.h>
#include <sstream>
#include <stdio.h>
#include <sys/time.h>
#include <jni.h>
#include <simulation.h>
#include <string.h>
#include <algorithm>

using namespace std;
using namespace OpenMM;

void writePdbFrame(int frameNum, const OpenMM::State&, FILE* myOfile, char* typeList, vector<int> partTypes);
string* split(string s, string c);
double string_to_double( const std::string& s );
double getTime(timeval start);
void simulate(double stepNum, double stepSize);

JNIEXPORT void JNICALL Java_readdy_impl_sim_top_TopMM_cCreateSimulation(JNIEnv *env, jobject obj, jboolean testmode, jstring tplgyDir, jstring grpDir, jint cudaDevNr, jdouble jstepNum, jdouble jstepSize, jdouble jstepsPerFrame, jdouble jkB, jdouble jT, jdoubleArray jperiodicBoundaries, jint jnTypes, jdoubleArray jDiff, jdoubleArray jcollRadii, jdoubleArray jparamPot1, jdoubleArray jparamPot2, jfloatArray jReactions, jint groupforce, jintArray jNumberOfDummyParticles){

    /// Timer
    timeval startClock;
    gettimeofday(&startClock, 0);

    /// in testmode the program gives serveral outputs for simulation progress and for debugging
    if(testmode){
        printf("c code\n");
    }

/// System ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Load any shared libraries containing GPU implementations.
    OpenMM::Platform::loadPluginsFromDirectory(OpenMM::Platform::getDefaultPluginsDirectory());

    /// Create a system with nonbonded forces.
    OpenMM::System system;

    /// get path to initial coordinate file, to read later the initial particle positions
    const char *tplgyCoord = env->GetStringUTFChars(tplgyDir, 0);
    char * tplgyCrd;
    tplgyCrd = (char*) malloc(strlen(tplgyCoord) + 1);
    strcpy(tplgyCrd, tplgyCoord);
    env->ReleaseStringUTFChars( tplgyDir, tplgyCoord);

    /// get path to group topology
    const char *tplgyGroups = env->GetStringUTFChars(grpDir, 0);
    char * tplgyGrp;
    tplgyGrp = (char*) malloc(strlen(tplgyGroups) + 1);
    strcpy(tplgyGrp, tplgyGroups);
    env->ReleaseStringUTFChars( grpDir, tplgyGroups);

    /// obtain important simulation parameter
    double totalNumberOfFrames=round(jstepNum);
    double stepSizeInPs=round(jstepSize);  /// in picoseconds
    int    stepsPerFrame   = round(jstepsPerFrame) ;

/** UNITS  and PARAMETER //////////////////////////////////////////////////////////////////////////////////////
    These units are based on daltons, nanometers, and
    picoseconds for the mass, length, and time dimensions, respectively. When using the C++
    API, it is very important to ensure that quantities being manipulated are always expressed in
    terms of these units.
    these variables may simplify unit-handling
     */

    /// every length in nanometer
    double angstrom     = 0.1;
    double picometer    = 0.001;
    double micrometer   = 1000;
    double meter        = 1e+9;
    /// every time in picoseconds
    double second       = 1e+12;
    double microsecond  = 1e+6;
    double nanosecond   = 1e+3;
    /// every weight in dalton
    double kilogram     = 6.02214129e+26;
    double gram         = 6.02214129e+23;
    double kilodalton   = 1000;
    /// Avogadroconstant and Boltzmann constant
    double          mol = 6.02214129e+23;
    /// obtain Boltzmann-constant from ReaDDy
    const double    kB  = (double)jkB;

    /// Parameter
    /// obtain Temperature from ReaDDy
    double  temperature     = (double)jT;              /// kelvin
    float  friction        = 1  ;              /// picoseconds

/// obtain number of different types //////////////////////////////////////////////////////////////////////////
    int nTypes=jnTypes;
    if(testmode)
        cout << "nTypes: " << nTypes << endl;

/// Diffusion-coefficients ///////////////////////////////////////////////////////////////////////////////////
    jdouble * Dtemp= (jdouble*)(env)->GetPrimitiveArrayCritical( jDiff, NULL);
    double* D = new double[nTypes];
    for(int i=0; i<nTypes; i++){
        D[i]=(double)Dtemp[i]/second;
        if(testmode)
            cout <<"D[" <<i<<"]: "<< D[i] <<  endl;
    }
    (env)->ReleasePrimitiveArrayCritical( jDiff, Dtemp, 0);

/// collision radii per particle type /////////////////////////////////////////////////////////////////////////
    // different radii for different pairs are not supportet jet
    jdouble * Rtemp= (jdouble*)(env)->GetPrimitiveArrayCritical( jcollRadii, NULL);
    double* R = new double[nTypes];
    for(int i=0; i<nTypes; i++){
        R[i]=(double)Rtemp[i*nTypes+i]/2;
        if(testmode)
            cout << "R["<<i<<"]: "<< R[i] << endl;
    }
    (env)->ReleasePrimitiveArrayCritical( jcollRadii, Rtemp, 0);


/// read tplgy_coordinates file and obtain particle-positions and types /////////////////////////////////////////////////////////
    if(testmode){
        cout << "reading particle positions"<<endl;
    }
    /// stores positions
    vector<OpenMM::Vec3> initPosInNm;
    /// stores ID for ReaDDy/Java (OpenMM particleID[ReaDDy particleID] => index: OpenMM particleID, value: ReaDDy particleID )
    vector<int> jID;
    /// stores type per particle (OpenMM particleID[typeID])
    vector<int> partType;
    /// stores particle per type (typeID[OpenMM particleID])
    vector< set<int> > typeArray;
    for(int i=0; i<nTypes; i++){
        typeArray.push_back(*new set<int>);
    }
    int NumOfParticles = 0;
    string line;
    ifstream myfile (tplgyCrd);
    if(testmode)
        cout << "from file " << "\"" << tplgyCrd << "\"" << endl;
    if (myfile.is_open()){
        int i=0;
        while ( myfile.good() ){
            getline (myfile,line);
            if(line[1]=='p'){
                /// example line + parsing type and positions
                //<p id="0" type="1" c="-31.09250630276887,-42.34489538041901,34.654154873058644"/>
                /// read and store type of current particle
                int type=atoi(split(line,"\"")[3].c_str());
                partType.push_back(type);
                typeArray[type].insert(i);
                /// read and store position of current particle
                string pos=split(line,"\"")[5];
                initPosInNm.push_back(OpenMM::Vec3(string_to_double(split(pos,",")[0].c_str()),string_to_double(split(pos,",")[1].c_str()),string_to_double(split(pos,",")[2].c_str()))); // location, nm
                /// add particle to system (parameter m (mass))
                /// D=kB*T/(friction*m) -> m=kB*T/(friction*D)
                system.addParticle(kB*temperature/(friction*D[type]));
                /// save ID for ReaDDy/Java (should initially be same as in OpenMM)
                jID.push_back(i);
                i++;
                NumOfParticles++;
            }
        }
        myfile.close();
    }


/// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Forces ////////////////////////////////////////////////////////////////////////////////////////////////////
/// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    if(testmode){
        cout << "creating forces" << endl;
    }
/// External Forces ///////////////////////////////////////////////////////////////////////////////////////////

    if(testmode)
        cout << "external forces" << endl;
    /// vector for storing custom external forces
    vector<CustomExternalForce*> customExternalForces;
    /// parameter for the forces ( forces[types[parameter]] )
    vector<vector<vector<double> > > ParametersCustomExternalForces;
    /// which particletypes are affected by this force (force[typeID])
    vector<vector<int> > affectedParticleTypesCustomExternalForces;
    /// OpenMM needs an additional index for applied particles (particleID[termIndex])
    vector<vector<int> > termIndices;
    /// get array from JNI
    jdouble * paramPot1 = (jdouble*)(env)->GetPrimitiveArrayCritical( jparamPot1, NULL);
    /// paramPot[0]: amount of Forces, paramPot[1]: length of paramPot

    for(int i=2; i<paramPot1[1];){
        if(testmode)
            cout << "#####################################################################" << endl<< customExternalForces.size() << endl;

        /// local vectors, to may be added to globals in the end
        vector<vector<double> > localParametersCustomExternalForces;
        localParametersCustomExternalForces.resize(nTypes);
        CustomExternalForce* force = new CustomExternalForce("0");
        affectedParticleTypesCustomExternalForces.push_back(vector<int>(nTypes, 0));
        vector<int> localTermIndices = vector<int>(NumOfParticles);
        /// possible force parameters:
        int subtype;
        bool considerParticleRadius;
        double K;
        double height;
        double radius;
        double sphereRadius;
        double cylinderRadius;
        double *center = new double[3];
        double *normal = new double[3];
        double *origin = new double[3];
        double *extension = new double[3];
        vector<int> localAffectedParticleTypeIds;
        vector<int> localAffectedParticleIds;
        /// define youre own parameter variables here...
        // double myParameterVariable;
        // vector<int> myParameterVariablesArray;

        /// bools for usage of parameters
        bool useO = false;
        bool useR = false;

        /// first value indicates the force type
        double type=paramPot1[i];
        i++;

        /// read in the force parameters
        while(paramPot1[i]<1000 && i<paramPot1[1]){
            /// center
            if(paramPot1[i]==1){
                i++;
                center[0]= paramPot1[i];
                i++;
                center[1]= paramPot1[i];
                i++;
                center[2]= paramPot1[i];
                if(testmode)
                    cout << "center: [" << center[0] << "," << center[1] << "," << center[2] << "]" << endl;
                i++;
            }
            /// height
            else if(paramPot1[i]==2){
                i++;
                height=paramPot1[i];
                if(testmode)
                cout << "height: " << height<< endl;
                i++;
            }
            /// normalvector
            else if(paramPot1[i]==3){
                i++;
                normal[0]= paramPot1[i];
                i++;
                normal[1]= paramPot1[i];
                i++;
                normal[2]= paramPot1[i];
                if(normal[0]!=0 || normal[1]!=0 || normal[2]!=1){
                    cerr << "Sorry, other normalvector then [0,0,1] not supported jet!" << endl;
                    return;
                }
                if(testmode)
                cout << "normalvector: [" << normal[0] << "," << normal[1] << "," << normal[2] << "]" << endl;
                i++;
            }
            /// subtype
            else if(paramPot1[i]==4){
                i++;
                subtype=paramPot1[i];
                if(testmode)
                cout << "subtype: " << subtype << endl;
                i++;
            }
            /// affectedParticleIds, not supported jet
            else if(paramPot1[i]==5){
                i++;
                int length=paramPot1[i];
                if(testmode)
                cout << "affectedParticleIds"<< endl;
                /// TODO add "affectedParticleIds"
                i+=length+1;
                if(length>0){
                    cout << "affectedParticleIds not supported jet, length: "<<length << endl;
                    return;
                }
            }
            /// radius
            else if(paramPot1[i]==6){
                i++;
                radius=paramPot1[i];
                if(testmode)
                cout << "radius: " << radius << endl;
                i++;
            }
            /// force constant
            else if(paramPot1[i]==7){
                i++;
                K=paramPot1[i];
                if(testmode)
                cout << "forceconstant: " << K << endl;
                i++;
            }
            /// affectedParticleTypeIds
            else if(paramPot1[i]==8){
                i++;
                int length=paramPot1[i];
                if(testmode)
                cout << "amount of affectedParticleTypeIds: " << length << endl;
                i++;
                for(int j=0; j< length; j++){
                    localAffectedParticleTypeIds.push_back(paramPot1[i]);
                    if(testmode)
                    cout<< paramPot1[i] << ", " ;
                    i++;
                }
                if(testmode)
                cout << endl;
            }
            /// consider particle radius?
            else if(paramPot1[i]==9){
                i++;
                considerParticleRadius=(paramPot1[i]==1 ? true:false);
                if(testmode)
                cout << "consider particle radius: " << considerParticleRadius << endl;
                i++;
            }
            /// sphere radius
            else if(paramPot1[i]==10){
                i++;
                sphereRadius=paramPot1[i];
                if(testmode)
                cout << "sphereRadius: " << sphereRadius << endl;
                i++;
            }
            /// cylinder radius
            else if(paramPot1[i]==11){
                i++;
                cylinderRadius=paramPot1[i];
                if(testmode)
                cout << "cylinderRadius: " << cylinderRadius << endl;
                i++;
            }
            /// cylinder height
            else if(paramPot1[i]==12){
                // actually CylinderHeight ...
                i++;
                height=paramPot1[i];
                if(testmode)
                cout << "height: " << height<< endl;
                i++;
            }
            /// origin
            else if(paramPot1[i]==13){
                i++;
                origin[0]= paramPot1[i];
                i++;
                origin[1]= paramPot1[i];
                i++;
                origin[2]= paramPot1[i];
                if(testmode)
                cout << "origin: [" << origin[0] << "," << origin[1] << "," << origin[2] << "]" << endl;
                i++;
            }
            /// extension
            else if(paramPot1[i]==14){
                i++;
                extension[0]= paramPot1[i];
                i++;
                extension[1]= paramPot1[i];
                i++;
                extension[2]= paramPot1[i];
                if(testmode)
                cout << "extension: [" << extension[0] << "," << extension[1] << "," << extension[2] << "]" << endl;
                i++;
            }
            /* /// you can ad your own parameter here (after defining it in ReaDDy)
            else if(paramPot1[i]==yourUniqueParameterKey){
                i++;
                yourParameterVariable= paramPot1[i];
                if(testmode)
                cout << "myParameterValue: "<< yourParameterVariable << endl;
                i++;
            }
            /// you may want to read more than one Value
            else if(paramPot1[i]==yourUniqueParameterKey){
                i++;
                int numberOfMyParameterValues = paramPot1[i];
                i++;
                for(int j=0; j< numberOfMyParameterValues; j++){
                    myParameterVariablesArray.push_back((int)paramPot1[i]);
                    i++;
                }
            }
            */
            else if(paramPot1[i]>=1000){
                /// -> next force definition begins
                break;
            }
            else{
                /// this line should better not be reached. If, then the creation or the interpretation of the force definition array went wrong
                cerr << "confused" << endl;
                return;
            }
        }

        stringstream ss;
        string forceFormula="";

        /// DISK
        if(type==1000){
            if(testmode)
                cout << "DISK" << endl;
            /// example: 0.5*K2D*((z)^2 + min(200-sqrt(x^2+y^2),0)^2)
            ss << "O*0.5*" << K << "*((" << center[2] << "+z)^2 + min(200-sqrt((x-" << center[0]<<")^2 + (y-" << center[1] << ")^2),0)^2)" ;
            cout << "TODO: consider particle radius" << endl;
            useO = true;
            useR = true;
        }
        /// CYLINDER
        if(type==1001){
            if(testmode)
                cout << "CYLINDER" << endl;
            /// "attractive"
            if(subtype==1){
                /// example: "0.5*K*( (min(0,z-3.2)+min(5.2-z,0))^2 + min(200-sqrt(x       ^2+y^2),0)^2)";
                ss << "O*0.5*" << K << "*((min(0,z-" << center[2]  - 0.5*height<<"-"<<  (considerParticleRadius ? "R" : "0") << ")+min(0," << center[2]+0.5*height << "+" << (considerParticleRadius ? "R" : "0" ) << "-z))^2 + min(" << radius <<"-sqrt((x-" << center[0] << "-" << (considerParticleRadius ?    "R" : "0") << ")^2+(y-" << center[1] << "-" << (considerParticleRadius ? "R" : "0") << ")^2),0)^2)";
                useO = true;
                useR = true;
                /// TODO: not sure with consider particle radius ...
            }
            /// "repulsive"stringstream ss;
            if(subtype==2){
                cout << "TODO: CYLINDER repulsive" <<endl;
                //ss << "0.5*" << K << "((min(0,z-" << center[0] <<"-"<<  (considerParticleRadius ? "R" : "0") << ")+min(0," << center[0]+height << "-" << (considerParticleRadius ? "R" : "0" ) << "-z))^2 + min(" << radius <<"-sqrt((x-" << center[1] << "-" << (considerParticleRadius ? radius : 0) << ")^2+(y-" << center[2] << "-" << (considerParticleRadius ? "R" : "0") << ")^2),0)^2)";
                return;
            }
            /// "membrane"
            if(subtype==3){
                ss << "O*0.5*" << K << "*(((z-" << center[2] <<"-"<<  (considerParticleRadius ? "R" : "0") << ")+(" << center[2]+height << "-" << (considerParticleRadius ? "R" : "0" ) << "-z))^2 + (" << radius <<"-sqrt((x-" << center[0] << "-" << (considerParticleRadius ? radius : 0) << ")^2+(y-" << center[1] << "-" << (considerParticleRadius ? "R" : "0") << ")^2))^2)";
                cout << ss <<endl;
                cout << "TODO test CYLINDER membrane" <<endl;
                useO = true;
                useR = true;
                return;
            }
        }
        /// CUBE
        else if(type==1002){
            if(testmode)
                cout << "CUBE" <<endl;
            /// "attractive"
            if(subtype==1){
                /// example:"0.5*Kw*( (min(0, x))^2+(min(0, boxSize[0]-x))^2+(min(0, y))^2+(min(0, boxSize[1]-y))^2+(min(0, z))^2+(min(0, boxSize[2]-z))^2 )"
                ss << "O*0.5*" << K << "*( min(0,(x-"<< (considerParticleRadius ? "R" : "0") << ")-"<< origin[0] << ")^2+min(0,(-x-" << (considerParticleRadius ? "R" : "0")<< ")+" << origin[0]+extension[0]<<")^2" /* */ << "+ min(0,(y-"<< (considerParticleRadius ? "R" : "0") << ")-"<< origin[1] << ")^2+min(0,(-y-" << (considerParticleRadius ? "R" : "0")<< ")+" << origin[1]+extension[1]<<")^2" /* */ << "+ min(0,(z-"<< (considerParticleRadius ? "R" : "0") << ")-"<< origin[2] << ")^2+min(0,(-z-" << (considerParticleRadius ? "R" : "0")<< ")+" << origin[2]+extension[2]<<")^2 )" ;
                /// TODO not sure about particle radius...
                useO = true;
                useR = true;
            }
            /// "repulsive"
            if(subtype==2){
                cout << "TODO CUBE repulsive" <<endl;
                return;
            }
            /// "repulsive"
            if(subtype==3){
                cout << "TODO CUBE membrane" <<endl;
                return;
            }
        }
        /// LOLLYPOP
        else if(type==1003){
            if(testmode)
                cout << "LOLLIPOP" <<endl;
            /// "attractive"
            if(subtype==1){
                cout << "TODO Lolli attractive" << endl;
                return;
            }
            /// "repulsive"
            if(subtype==2){
                cout << "TODO Lolli repulsive" <<endl;
                return;
            }
            /// "membrane"
            if(subtype==3){
                /// example: "0.5*K2D*((min(0,z)^2+(1-step(z+2-20))*((sqrt(x^2+y^2)-10-2)^2)+step(z+2-20)*(sqrt(x^2+y^2+(z-20-sqrt(50^2-10^2))^2)-50-2)^2))"
                ss << "O*0.5*" << K << "*((min(0,z)^2+(1-step(z+" << (considerParticleRadius ? "R" : "0") << "-"<< height<<"))*((sqrt(x^2+y^2)-" << cylinderRadius << "-" << (considerParticleRadius ? "R": "0")<< ")^2)+step(z+"<< (considerParticleRadius? "R" :"0") << "-" << height <<")*(sqrt(x^2+y^2+(z-" << height << "-sqrt("<< sphereRadius << "^2-" << cylinderRadius << "^2))^2)-" << sphereRadius <<"-" << (considerParticleRadius ? "R" : "0") << ")^2))";
                useO = true;
                useR = true;
            }

        }
        /// SPHERE
        else if(type==1004){
            if(testmode)
                cout << "SPHERE" <<endl;
            /// "attractive"
            if(subtype==1){
                ss << "O*0.5*" << K << "*min(0," << radius <<"-sqrt((x-)^2+(y-)^2+(z-)^2))^2";
                cout << "TODO test sphere attractive" << endl;
                useO = true;
                useR = true;
                return;
            }
            /// "repulsive"
            if(subtype==2){
                cout << "TODO sphere repulsive" <<endl;
                return;
            }
            /// "membrane"
            if(subtype==3){
                cout << "TODO sphere membrane" << endl;
                return;
            }
        }
        /* /// add your own potential here
        else if(type==yourPotentialID){
            if(testmode)
                cout << "myPotential" <<endl;
            /// you can define subtypes
            if(subtype==1){
                // define here a OpenMM-style algebraic expression for your potential
                // use youre varables directly or as OpenMMs per-particle-parameter
                ss << "O*0.5*" << K << "*min(0," << radius <<"-sqrt((x-)^2+(y-)^2+(z-)^2))^2";
                useO = true;    // you may want to use the predefined per-particle-parameters
                useR = true;    // the usage of "O" is recommendet for the usage with reaction
                // or add own per-particle-parameter
                force->addPerParticleParameter("myParam");
                for(int type=0; type<nTypes; type++){
                    // add a value for every particle-type
                    localParametersCustomExternalForces[type].push_back(myParameterValue);
                }
            }
            if(subtype==2){
                cout << "TODO: a second subtype" <<endl;
                return;
            }
        }*/

        /// save all affected particle type ID s
        for(int t=0; t<localAffectedParticleTypeIds.size(); t++){
            affectedParticleTypesCustomExternalForces.back()[localAffectedParticleTypeIds[t]]=1;
        }

        forceFormula=ss.str();
        if(testmode)
            cout << forceFormula << endl;
        force->setEnergyFunction(forceFormula);
        customExternalForces.push_back(force);

        if(useO){
            /// "O" artificial parameter, which is used by every force. It is boolean and indicates, whether a particle is currently active or inactive (force actson it or not). This is important for possible reactions, to make particles to dummy particles
            force->addPerParticleParameter("O");
            for(int type=0; type<nTypes; type++){
                localParametersCustomExternalForces[type].push_back(1);
            }
        }
        if(useR){
            /// "R" particle Radius variable is also used by every force.
            force->addPerParticleParameter("R");
            for(int type=0; type<nTypes; type++){
                localParametersCustomExternalForces[type].push_back(R[type]);
            }
        }
        ParametersCustomExternalForces.push_back(localParametersCustomExternalForces);

        /// add all parameters to affected particles
        /// go through affected types
        for(int t=0; t<localAffectedParticleTypeIds.size(); t++){
            /// all particle of that type
            int type = localAffectedParticleTypeIds[t];
            for (std::set<int>::iterator it=typeArray[type].begin(); it!=typeArray[type].end(); ++it){
                /// set parameter and add particle to force
                double *para = new double[localParametersCustomExternalForces[0].size()];
                for(int parameter=0; parameter<localParametersCustomExternalForces[0].size(); parameter++){
                    para[parameter]=localParametersCustomExternalForces[type][parameter];
                }
                const std::vector<double> param (para, para + localParametersCustomExternalForces[0].size() );
                localTermIndices[*it] = force->addParticle(*it, param);
            }
        }

        termIndices.push_back(localTermIndices);

        delete[] center;
        delete[] normal;
        delete[] origin;
        delete[] extension;
    }
    (env)->ReleasePrimitiveArrayCritical( jparamPot1, paramPot1, 0);

/// Pairwise Forces ///////////////////////////////////////////////////////////////////////////////////////////

    if(testmode)
        cout << endl << "pairwise forces" <<endl;
    /// vector for storing custom nonbond forces
    vector<CustomNonbondedForce*> customNonbondForces;
    /// parameter for the forces ( forces[types[parameter]] )
    vector<vector<vector<double> > > ParametersCustomNonbondForces;
    /// which particletypes are affected by this force (force[typeID])
    vector<vector<int> > affectedParticleTypeIdPairsCustomNonbondForces;
    /// global cutoff for the pairwise (nonbond) forces
    double cutoff=0;

    jdouble * paramPot2 = (jdouble*)(env)->GetPrimitiveArrayCritical( jparamPot2, NULL);
    /// enumerate forces
    int pairwiseForcesNr=customExternalForces.size();
    /// paramPot[0]: amount of Forces, paramPot[1]: length of paramPot
    int i;
    for(i=2; i<paramPot2[1];){
            /// stop if group forces begin
            if(pairwiseForcesNr>=groupforce)
                break;
            pairwiseForcesNr++;

            if(testmode)
                cout << "#####################################################################" << endl<< pairwiseForcesNr-1 << endl;

            /// local vectors, to may be added to globals in the end
            CustomNonbondedForce* force = new CustomNonbondedForce("0");
            vector<vector<double> > localParametersCustomNonbondForces;
            localParametersCustomNonbondForces.resize(nTypes);
            vector<int> localAffectedParticleTypeIds;
            localAffectedParticleTypeIds.resize(nTypes);
            /// possible force parameters
            double K;
            int subtype;
            double radius;
            double depth;
            double length;
            vector<int> affectedParticleTypeIdPairs;
            vector<int> affectedParticleIdPairs;
            /// define youre own parameter variables here...
            // double myParameterVariable;
            // vector<int> myParameterVariablesArray;

            /// first value indicates the force type
            double type = paramPot2[i];
            i++;

            /// bools for usage of parameters
            bool useO = false;
            bool useR = false;

            /// read in the force parameters
            while(paramPot2[i]<1000 && i<paramPot2[1]){
                /// subtype
                if(paramPot2[i]==4){
                    i++;
                    subtype=paramPot2[i];
                    if(testmode)
                        cout << "subtype: " << subtype << endl;
                    i++;
                }
                /// affectedParticleIds, not supported jet
                else if(paramPot2[i]==5){
                    i++;
                    int tlength=paramPot2[i];
                    if(testmode)
                    cout << "affected particle IDs"<< endl;
                    /// TODO add "affectedParticleIds"
                    i+=tlength+1;
                    if(tlength>0){
                        cout << "not supported jet" << endl;
                        return;
                    }
                }
                /// ineraction radius
                else if(paramPot2[i]==13){
                    i++;
                    radius=paramPot2[i];
                    if(testmode)
                    cout << "interactionradius: " << radius << endl;
                    i++;
                }
                /// depth
                else if(paramPot2[i]==14){
                    i++;
                    depth=paramPot2[i];
                    if(testmode)
                    cout << "depth: " << depth << endl;
                    i++;
                }
                /// length
                else if(paramPot2[i]==15){
                    i++;
                    length=paramPot2[i];
                    if(testmode)
                    cout << "length: " << length << endl;
                    i++;
                }
                /// force constant
                else if(paramPot2[i]==7){
                    i++;
                    K=paramPot2[i];
                    if(testmode)
                    cout << "forceconstant: " << K << endl;
                    i++;
                }
                /// affectedParticleTypeIds
                else if(paramPot2[i]==8){
                    i++;
                    int length=paramPot2[i];
                    if(testmode)
                        cout << "affectedParticleTypeIdPairs ("<< length/2 <<")" << endl;
                    if(length==-1){
                        cout << "\"null\" at affectedParticleTypeIdPairs is not allowed" << endl;
                        return;
                    }
                    i++;
                    for(int j=0; j< length; j++){
                        affectedParticleTypeIdPairs.push_back(paramPot2[i]);
                        if(testmode)
                        cout << paramPot2[i] << ", ";
                        localAffectedParticleTypeIds[paramPot2[i]]=1;
                        i++;
                    }
                    if(length==0){
                        for(int j=0; j<nTypes; ++j){
                            localAffectedParticleTypeIds[j]=1;
                        }
                    }
                    if(testmode)
                    cout << endl;
                }
                /* /// you can ad your own parameter here (after defining it in ReaDDy)
                else if(paramPot1[i]==yourUniqueParameterKey){
                    i++;
                    yourParameterVariable= paramPot1[i];
                    if(testmode)
                    cout << "myParameterValue: "<< yourParameterVariable << endl;
                    i++;
                }
                /// you may want to read more than one Value
                else if(paramPot1[i]==yourUniqueParameterKey){
                    i++;
                    int numberOfMyParameterValues = paramPot1[i];
                    i++;
                    for(int j=0; j< numberOfMyParameterValues; j++){
                        myParameterVariablesArray.push_back((int)paramPot1[i]);
                        i++;
                    }
                }
                */
                else if(paramPot2[i]>=1000){
                    /// -> next force definition begins
                    break;
                }
                else{
                    cerr << "confused" << endl;
                    return;
                }
            }

            string forceFormula="";
            stringstream ss;

            /// HARMONIC
            if(type==2000){
                if(testmode)
                    cout << "HARMONIC" << endl;
                /// "attractive"
                if(subtype==1){
                    ss << "O1*O2*0.5*" << K << "*(min(0, (R1+R2)-r)^2)";
                    useO = true;
                    useR = true;

                }
                /// "repulsive"
                if(subtype==2){
                    ss << "O1*O2*0.5*" << K << "*(min(0, r-(R1+R2))^2)";
                    useO = true;
                    useR = true;
                }
                /// "spring"
                if(subtype==3){
                    ss << "O1*O2*0.5*" << K << "*(r-(R1+R2))^2";
                    useO = true;
                    useR = true;
                }
                //cutoff=max();
            }
            /// WEAK_INTERACTION_HARMONIC
            if(type==2001){
                cerr << "WEAK_INTERACTION_HARMONIC: depricated" <<endl;
                return;
            }
            /// WEAK_INTERACTION
            if(type==2002){
                if(testmode)
                    cout << "WEAK_INTERACTION" << endl;
                            /*
                             *("K2N2*K2N1*K2K*step(r-(R2+R1))*step(I2R-r)*(-0.5*I2R*(R1+R2)*r*r+(1/3)*r*r*r*(R2+R1)+(1/3)*I2R*r*r*r-0.25*r*r*r*r)");
                             *("0.5*K2N1*(min(0,R1-r)*min(0,r-I2R))^2");
                             *energy = 1/12 * k * r*r *(3 * r*r + 6 * r0 * iradius - 4 * r * (r0 + iradius));
                             **/
                            //ss << "1/12 * " << K << " * r*r *(3 * r*r + 6 * (R1+R2) * " << radius << " - 4 * r * ( (R1+R2) + " << radius <<" ))";
                            // (K3N2*K3N1*K3K+K2N2*K2N1*K2K)*step(r-(R2+R1))*step(I2R-r)*(-0.5*I2R*(R1+R2)*r*r+(1/3)*r*r*r*(R2+R1)+(1/3)*I2R*r*r*r-0.25*r*r*r*r)")
                cout << "carefull, differences between ReaDDy WEAK_INTERACTION gradient and energy!!"<<endl;
                ss << K << "*O1*O2*(step(r-(R2+R1))*step(" << radius << "-r)*(-0.5*" << radius << "*(R1+R2)*r*r+(1/3)*r*r*r*(R1+R2)+(1/3)*" << radius << "*r*r*r-0.25*r*r*r*r))";
                useO = true;
                useR = true;
            }
            /// WEAK_INTERACTION_PIECEWISE_HARMONIC
            if(type==2003){
                if(testmode)
                    cout << "WEAK_INTERACTION_PIECEWISE_HARMONIC" << endl;
                ss << "O1*O2*( ((1-step(r-(R1+R2)))*(0.5*" << K << "*(r-(R1+R2))^2-" << depth << ") ) + ( (step(r-(R1+R2))*(1-step(r-(R1+R2+0.5*" << length << "))))* (0.5*" << depth << "*(2/" << length << ")^2*(r-(R1+R2))^2-" << depth << ") ) + ( (step(r-(R1+R2+0.5*" << length << "))*(1-step(r-(R1+R2+" << length << "))))*(-0.5*" << depth << "*(2/" << length << ")^2*(r-(R1+R2+" << length << "))^2) ) )";
                useO = true;
                useR = true;
            }
            /* /// add your own potential here
            else if(type==yourPotentialID){
                if(testmode)
                    cout << "myPotential" <<endl;
                /// you can define subtypes
                if(subtype==1){
                    // define here a OpenMM-style algebraic expression for your potential
                    // use youre varables directly or as OpenMMs per-particle-parameter
                    ss << "O*0.5*" << K << "*min(0," << radius <<"-sqrt((x-)^2+(y-)^2+(z-)^2))^2";
                    useO = true;    // you may want to use the predefined per-particle-parameters
                    useR = true;    // the usage of "O" is recommendet for the usage with reaction
                    // or add own per-particle-parameter
                    force->addPerParticleParameter("myParam");
                    for(int type=0; type<nTypes; type++){
                        // add a value for every particle-type
                        localParametersCustomExternalForces[type].push_back(myParameterValue);
                    }
                }
                if(subtype==2){
                    cout << "TODO: a second subtype" <<endl;
                    return;
                }
            }*/

            forceFormula=ss.str();
            if(testmode)
                cout << forceFormula << endl;
            force->setEnergyFunction(forceFormula);
            customNonbondForces.push_back(force);

            if(useO){
                /// "O" artificial parameter, which is used by every force. It is boolean and indicates, whether a particle is currently active or inactive (force actson it or not). This is important for possible reactions, to make particles to dummy particles
                force->addPerParticleParameter("O");
                for(int type=0; type<nTypes; type++){
                    localParametersCustomNonbondForces[type].push_back(1);
                }
            }
            if(useR){
                /// "R" particle Radius variable is also used by every force.
                force->addPerParticleParameter("R");
                for(int type=0; type<nTypes; type++){
                    localParametersCustomNonbondForces[type].push_back(R[type]);
                }
            }
            ParametersCustomNonbondForces.push_back(localParametersCustomNonbondForces);

            /// add all parameters to affected particles
            /// add all particle, use respective parameter or "0"s
            for(int particle=0; particle<NumOfParticles; particle++){
                /// add parameters
                double *para = new double[localParametersCustomNonbondForces[0].size()];
                for(int parameter=0; parameter<localParametersCustomNonbondForces[0].size(); parameter++){
                    /// particle affected?
                    if(localAffectedParticleTypeIds[partType[particle]]==1){
                        para[parameter]=localParametersCustomNonbondForces[partType[particle]][parameter];
                    }
                    else{
                        para[parameter]=0;
                    }
                }
                const std::vector<double> param (para, para + localParametersCustomNonbondForces[0].size());
                force->addParticle(param);
            }

            affectedParticleTypeIdPairsCustomNonbondForces.push_back(affectedParticleTypeIdPairs);

    }


/// Group Forces ///////////////////////////////////////////////////////////////////////////////////////////

    if(testmode)
        cout << endl << "group forces" <<endl;
    /// vector for storing custom group forces
    vector<CustomBondForce*> groupForces;
    /// parameter for the forces ( forces[types[parameter]] )
    vector<vector<vector<double> > > ParametersGroupForces;

    for(; i<paramPot2[1];){

            if(testmode)
                cout << "#####################################################################" << endl<< pairwiseForcesNr+groupForces.size() << endl;

            /// local vectors, to may be added to globals in the end
            CustomBondForce* force = new CustomBondForce("0");
            vector<vector<double> > localParametersGroupsForces;
            localParametersGroupsForces.resize(nTypes);
            /// possible force parameters
            double K;
            int subtype;
            double radius;
            double depth;
            double length;
            vector<int> affectedParticleTypeIds;
            vector<int> affectedParticleIds;
            /// define youre own parameter variables here...
            // double myParameterVariable;
            // vector<int> myParameterVariablesArray;

            /// first value indicates the force type
            double type = paramPot2[i];
            i++;

            /// read in the force parameters
            while(paramPot2[i]<1000 && i<paramPot2[1]){
                /// subtype
                if(paramPot2[i]==4){
                    i++;
                    subtype=paramPot2[i];
                    if(testmode)
                        cout << "subtype: " << subtype << endl;
                    i++;
                }
                /// affectedParticleIds, not supported jet
                else if(paramPot2[i]==5){
                    i++;
                    int tlength=paramPot2[i];
                    if(testmode)
                        cout << "affected particle IDs"<< endl;
                    /// TODO add "affectedParticleIds"
                    i+=tlength+1;
                    if(tlength>0){
                        cout << "not supported jet" << endl;
                        return;
                    }
                }
                /// interaction radius
                else if(paramPot2[i]==13){
                    i++;
                    radius=paramPot2[i];
                    if(testmode)
                        cout << "interactionradius: " << radius << endl;
                    i++;
                }
                /// depth
                else if(paramPot2[i]==14){
                    i++;
                    depth=paramPot2[i];
                    if(testmode)
                        cout << "depth: " << depth << endl;
                    i++;
                }
                /// length
                else if(paramPot2[i]==15){
                    i++;
                    length=paramPot2[i];
                    if(testmode)
                        cout << "length: " << length << endl;
                    i++;
                }
                /// force constant
                else if(paramPot2[i]==7){
                    i++;
                    K=paramPot2[i];
                    if(testmode)
                        cout << "forceconstant: " << K << endl;
                    i++;
                }
                /// affectedParticleTypeIds
                else if(paramPot2[i]==8){
                    i++;
                    cout << paramPot2[i] << endl;
                    int length=paramPot2[i];
                    if(length!=-1){
                        cout << "there shouldn't be affectedParticleTypeIdPairs for groups" << endl;
                        return;
                    }
                    else{
                        i++;
                    }
                }
                /* /// you can ad your own parameter here (after defining it in ReaDDy)
                else if(paramPot1[i]==yourUniqueParameterKey){
                    i++;
                    yourParameterVariable= paramPot1[i];
                    if(testmode)
                    cout << "myParameterValue: "<< yourParameterVariable << endl;
                    i++;
                }
                /// you may want to read more than one Value
                else if(paramPot1[i]==yourUniqueParameterKey){
                    i++;
                    int numberOfMyParameterValues = paramPot1[i];
                    i++;
                    for(int j=0; j< numberOfMyParameterValues; j++){
                        myParameterVariablesArray.push_back((int)paramPot1[i]);
                        i++;
                    }
                }
                */
                else if(paramPot2[i]>=1000){
                    /// -> next force definition begins
                    break;
                }
                else{
                    /// this line should better not be reached. If, then the creation or the interpretation of the force definition array went wrong
                    cout << "confused" << endl;
                    return;
                }
            }

            string forceFormula="";
            stringstream ss;

            /// HARMONIC
            if(type==2000){
                if(testmode)
                cout << "HARMONIC" << endl;
                /// "attractive"
                if(subtype==1){
                    ss << "0.5*" << K << "*(min(0, (R)-r)^2)";

                }
                /// "repulsive"
                if(subtype==2){
                    ss << "0.5*" << K << "*(min(0, r-(R))^2)";
                }
                /// "spring"
                if(subtype==3){
                    ss << "0.5*" << K << "*(r-(R))^2";
                }
                //cutoff=max();
            }
            /// WEAK_INTERACTION_HARMONIC
            if(type==2001){
                cerr << "WEAK_INTERACTION_HARMONIC: depricated" <<endl;
                return;
            }
            /// WEAK_INTERACTION
            if(type==2002){
                if(testmode)
                    cout << "WEAK_INTERACTION" << endl;
                        /*
                         *("K2N2*K2N1*K2K*step(r-(R2+R1))*step(I2R-r)*(-0.5*I2R*(R1+R2)*r*r+(1/3)*r*r*r*(R2+R1)+(1/3)*I2R*r*r*r-0.25*r*r*r*r)");
                         *("0.5*K2N1*(min(0,R1-r)*min(0,r-I2R))^2");
                         *energy = 1/12 * k * r*r *(3 * r*r + 6 * r0 * iradius - 4 * r * (r0 + iradius));
                         **/
                ss << "1/12 * " << K << " * r*r *(3 * r*r + 6 * (R) * " << radius << " - 4 * r * ( (R) + " << radius <<" ))";
            }
            /// WEAK_INTERACTION_PIECEWISE_HARMONIC
            if(type==2003){
                if(testmode)
                    cout << "WEAK_INTERACTION_PIECEWISE_HARMONIC" << endl;
                ss << "( ((1-step(r-(R)))*(0.5*" << K << "*(r-(R))^2-" << depth << ") ) + ( (step(r-(R))*(1-step(r-(R+0.5*" << length << "))))* (0.5*" << depth << "*(2/" << length << ")^2*(r-(R))^2-" << depth << ") ) + ( (step(r-(R+0.5*" << length << "))*(1-step(r-(R+" << length << "))))*(-0.5*" << depth << "*(2/" << length << ")^2*(r-(R+" << length << "))^2) ) )";
            }
            /* /// add your own potential here
            else if(type==yourPotentialID){
                if(testmode)
                    cout << "myPotential" <<endl;
                /// you can define subtypes
                if(subtype==1){
                    // define here a OpenMM-style algebraic expression for your potential
                    // use youre varables directly or as OpenMMs per-particle-parameter
                    ss << "O*0.5*" << K << "*min(0," << radius <<"-sqrt((x-)^2+(y-)^2+(z-)^2))^2";
                    // add per-bond-parameter here
                    force->addPerBondParameter("yourParameter");
                }
                if(subtype==2){
                    cout << "TODO: a second subtype" <<endl;
                    return;
                }
            }*/

            forceFormula=ss.str();
            if(testmode)
                cout << forceFormula << endl;
            force->setEnergyFunction(forceFormula);
            force->addPerBondParameter("R");
            groupForces.push_back(force);
            ParametersGroupForces.push_back(localParametersGroupsForces);

    }
    (env)->ReleasePrimitiveArrayCritical( jparamPot2, paramPot2, 0);

    /// read inividual groups form file
    if(testmode)
        cout << endl << "read groups" << endl;
    int bonds=0;

    ifstream myfile2 (tplgyGrp);
    if(testmode)
        cout << "from file " << "\"" << tplgyGrp << "\"" << endl;
    if (myfile2.is_open())
    {
      while ( myfile2.good() )
        {
        getline (myfile2,line);
        // <g id="0" type="0" internalAndParticleId="[[0,0];[1,1]]"/>
        if(line[1]=='g'){
            string typeS = split(line, "\"")[3];
            string oneS = split(split( split(line, "[")[2], ",")[1], "]")[0];
            string twoS = split(split( split(line, "[")[3], ",")[1], "]")[0];
            int grType = atoi(typeS.c_str());
            int one = atoi(oneS.c_str());
            int two = atoi(twoS.c_str());
            /// parameter: just radii of both particles
            double para[] = {R[partType[one]]+R[partType[two]]};
            const std::vector<double> param (para, para + sizeof(para) / sizeof(double) );
            /// add the group as bond to OpenMM
            groupForces[grType]->addBond(one, two, param);
            bonds++;
        }
      }
      myfile2.close();
    }
    if(testmode)
        cout << bonds << " bonds" << endl;

/// add all forces to OpenMM-System and print all force formulas //////////////////////////////////////////////////////////

    /// add forces to system
    cout << endl<<"External Forces: " <<customExternalForces.size()<<endl;
    for(int i=0; i<customExternalForces.size(); i++){
        system.addForce(customExternalForces[i]);
        cout << customExternalForces[i]->getEnergyFunction() << endl;
        cout << "amount of applied particles: " << customExternalForces[i]->getNumParticles() << endl << endl;
    }

    cout << "Nonbond Forces: " <<customNonbondForces.size()<<endl;
    for(int i=0; i<customNonbondForces.size(); i++){
        system.addForce(customNonbondForces[i]);
        cout << customNonbondForces[i]->getEnergyFunction() << endl << endl;
    }
    cout << "Group Forces: " <<groupForces.size()<<endl;
    for(int i=0; i<groupForces.size(); i++){
        system.addForce(groupForces[i]);
        cout << groupForces[i]->getEnergyFunction() << endl << endl;
    }


    /// set cutoff value (must be same for all forces)
    /// TODO for all nonbond forces (Forces) (max)
    //nonbound->setCutoffDistance(R[0]+R[1]+l);


/// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Forces End ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /// set periodic boundaries, get size of periodic boundary-box from ReaDDy
    jdouble * periodicBoundaries= (jdouble*)(env)->GetPrimitiveArrayCritical( jperiodicBoundaries, NULL);
    system.setDefaultPeriodicBoxVectors(OpenMM::Vec3(periodicBoundaries[0],0,0),OpenMM::Vec3(0,periodicBoundaries[1],0),OpenMM::Vec3(0,0,periodicBoundaries[2]));
    (env)->ReleasePrimitiveArrayCritical( jperiodicBoundaries, periodicBoundaries, 0);


    /// Dummy Particle
    if(testmode)
        cout << "Dummy particle" << endl;

    /// obtain numbers of dummy particles for every type from JNI-array
    jint * numberOfDummyParticles = (jint*)(env)->GetPrimitiveArrayCritical( jNumberOfDummyParticles, NULL);
        vector<int> numberOfFreePositions;
        vector<vector<int> > freePositionsInParticleArray;
        for(int type=0; type<nTypes; ++type){
            numberOfFreePositions.push_back(numberOfDummyParticles[type]);
        }
    (env)->ReleasePrimitiveArrayCritical( jNumberOfDummyParticles, numberOfDummyParticles, 0);

    /// resize termIndices array
    for(int type=0; type<nTypes; ++type){
        for(int force=0; force<customExternalForces.size(); force++){
            termIndices[force].resize(termIndices[force].size()+numberOfFreePositions[type]);
        }
    }

    /// add dummy particles
    int dummyParticleIndex=NumOfParticles; // continue after last particle
    for(int type=0; type<nTypes; type++){
        freePositionsInParticleArray.push_back(*(new vector<int>));
        for(int k=0; k<numberOfFreePositions[type];k++){
            /// new random position
            initPosInNm.push_back(OpenMM::Vec3(rand()%1000+1000,rand()%1000+1000,rand()%1000+1000)); // location, nm
            /// add new particle to system
            system.addParticle(kB*temperature/(friction*D[type]));
            /// type is "dummy" until particle becomes active through reaction
            partType.push_back(-1);
            typeArray[type].insert(dummyParticleIndex);
            jID.push_back(-1);
            freePositionsInParticleArray[type].push_back(dummyParticleIndex);
            /// add dummy particle to respective forces
            /// //////////////////////////////////////////////////////////////////////////////
            for(int force=0; force<customNonbondForces.size(); force++){
                double *para = new double[ParametersCustomNonbondForces[force][0].size()];
                for(int parameter=0; parameter<ParametersCustomNonbondForces[force][0].size(); parameter++){
                    para[parameter]=0;
                }
                const std::vector<double> param (para, para + ParametersCustomNonbondForces[force][0].size() );
                customNonbondForces[force]->addParticle(param);
            }
            for(int force=0; force<customExternalForces.size(); force++){
                if(affectedParticleTypesCustomExternalForces[force][type]==1){
                    double *para = new double[ParametersCustomExternalForces[force][0].size()];
                    for(int parameter=0; parameter<ParametersCustomExternalForces[force][0].size(); parameter++){
                        para[parameter]=0;
                    }
                    const std::vector<double> param (para, para + ParametersCustomExternalForces[force][0].size() );
                    termIndices[force][dummyParticleIndex] = customExternalForces[force]->addParticle(dummyParticleIndex, param);
                }
            }
            /// ////////////////////////////////////////////////////////////////////////////////
            ++dummyParticleIndex;
        }
    }

    /// get and print numer of dummy particles
    int totalNumberOfDummyParticles=0;
    for(int i=0; i<numberOfFreePositions.size(); i++){
        totalNumberOfDummyParticles+=numberOfFreePositions[i];
    }
    if(testmode){
        cout << "Numbers of particles: " << NumOfParticles << endl;
        cout << "Numbers of dummy particles: " << totalNumberOfDummyParticles << endl;
    }

    /// set interaction groups
    if(testmode)
        cout << "set interactiongroups" << endl;
    for(int force=0; force<customNonbondForces.size();force++){
        for(int i=0; i< affectedParticleTypeIdPairsCustomNonbondForces[force].size(); i+=2){
            customNonbondForces[force]->addInteractionGroup(typeArray[affectedParticleTypeIdPairsCustomNonbondForces[force][i]],typeArray[affectedParticleTypeIdPairsCustomNonbondForces[force][i+1]]);
        }
    }

    if(testmode){
        cout << "creating OpenMM-platform" << endl;
    }
    /// choose integrator and timestep -> chose Brownian (OpenMM Users Guide page 189, ReaDDy paper page 3), timestep-> see parameters
    OpenMM::BrownianIntegrator integrator = OpenMM::BrownianIntegrator(temperature, friction, stepSizeInPs);

    /// decide for one platform. we choose CUDA
    OpenMM::Platform& platform = OpenMM::Platform::getPlatformByName("CUDA");
    /// set Nr of used CUDA device
    map<string, string> properties;
    if(testmode)
        cout << cudaDevNr << endl;
    properties["CudaDeviceIndex"] = (int) cudaDevNr;
    OpenMM::Context context(system, integrator, platform, properties);

    /// alternatively:
    /// take OpenCL platform
    /*OpenMM::Platform& platform = OpenMM::Platform::getPlatformByName("OpenCL");
    map<string, string> properties;
    OpenMM::Context context(system, integrator, platform, properties);*/

    /// alternatively:
    /// Let OpenMM Context choose best platform.
    /*OpenMM::Context context(system, integrator);
    OpenMM::Platform& platform = context.getPlatform();*/

    /// print several platform informations
    if(testmode){
        cout << "using OpenMM versions: " << platform.getOpenMMVersion() << endl;
        cout << "using platform: " << platform.getName() << endl;
        const std::vector< std::string > props = platform.getPropertyNames();
        for (int i=0; i<props.size(); i++){
            cout<< props[i]<<": " << platform.getPropertyValue(context, props[i])<<endl;
        }
    }

    /// Set starting positions of the atoms. Leave time and velocity zero.
    context.setPositions(initPosInNm);

    /// obtain callback method form Java
    if(testmode){
        cout << "receive Java callback Method"<< endl;
        cout << "get class" << endl;
    }
    jclass mClass = env->GetObjectClass(obj);
    if(testmode){
        cout << "get method ID" << endl;
    }
    jmethodID mid = env->GetMethodID(mClass, "frameCallback", "(I[F)Z");
    if (mid==0){ cout << "mthod not found" << endl; return;}

    /// clock for seperate time measurements
    float timeGetPos = 0.0;
    float timeCpyPos = 0.0;
    float timeCallback = 0.0;
    float timeIntegrate = 0.0;
    float timeCReactions = 0.0;
    timeval timeX;
    bool fail = false;

    /// Timer
    timeval startSimulationClock;
    gettimeofday(&startSimulationClock, 0);

    int totalSimulationTime=totalNumberOfFrames*stepsPerFrame*stepSizeInPs;

    if(testmode){
        cout << " Alright, ReaDDy for simulation. Lets Go! \n" << endl;
    }
/// Simulate ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    int frameNumber=0;
    for (; frameNumber<=totalNumberOfFrames; frameNumber++) {

        /// integrate ///////////////////////////////////////////////////////////////////////////////////////////////
        if(testmode){
            cout << endl << frameNumber << endl;
            cout << "Integrate " << stepsPerFrame << " steps" << endl;
            gettimeofday(&timeX, 0);
        }
        /// Advance state many steps at a time, for efficient use of OpenMM.
        integrator.step(stepsPerFrame);

        if(testmode){
            timeIntegrate+=(float)getTime(timeX);
            gettimeofday(&timeX, 0);
        }

        /// get positions ///////////////////////////////////////////////////////////////////////////////////////////////

        /// Output current state information.
        OpenMM::State state    = context.getState(OpenMM::State::Positions);
        const double  timeInPs = state.getTime();
        const std::vector<OpenMM::Vec3>& posInNm = state.getPositions();
        if(testmode){
            timeGetPos+=(float)getTime(timeX);
            gettimeofday(&timeX, 0);
        }

        /// return positions to Java/ReaDDy ///////////////////////////////////////////////////////////////////////////////////////////////

        if(testmode){
            printf("return positions\n");
            gettimeofday(&timeX, 0);
        }
        /// open Java-Array for C
        jfloatArray JPos = (env)->NewFloatArray((jsize)NumOfParticles*5+1);
        jfloat *Pos = (jfloat*)(env)->GetPrimitiveArrayCritical( JPos, NULL);
            if(Pos == NULL){
                cout << "cant get array pointer"<<endl;
                return;
            }

            /// first value: number of particles, than add [x,y,z,id(OpenMM), id(ReaDDy)] for each particle
            Pos[0]=NumOfParticles;
            int currentPos=0;
            /// copy positions to Java-Array
            for (int j = 0; j < (int)posInNm.size(); j++){
                if(partType[j]!=-1){
                    for( int i=0; i<3; i++){
                        /// check for errors in particle positions
                        if(testmode && fabs((float)posInNm[j][i])> 100000){
                            cout << "error at pos "<< j <<"["<<i<<"] = "<<posInNm[j][i] << "(Type: "<<partType[j]<< ")" << endl;
                            fail==true;
                            return;
                        }
                        Pos[1+currentPos*5+i]=(float)posInNm[j][i]; /// coordinates
                    }
                    Pos[1+currentPos*5+3]=j;        /// index
                    Pos[1+currentPos*5+4]=jID[j];   /// ReaDDy Particle ID
                    currentPos++;
                }
            }
            if(fail){
                return;
            }

        /// release Java-Array
        (env)->ReleasePrimitiveArrayCritical( JPos, Pos, 0);

        /// callback Java/ReaDDy ///////////////////////////////////////////////////////////////////////////////////////////////

        if(testmode){
            timeCpyPos+=(float)getTime(timeX);
            cout<< "callback" <<endl;
            gettimeofday(&timeX, 0);
        }

        /// callback and check for error
        if(!(env->CallBooleanMethod(obj, mid, frameNumber, JPos))){
            cout << "error in java"<<endl;
            return;
        }
        (env) -> DeleteLocalRef(JPos);

        if(testmode){
            timeCallback+=(float)getTime(timeX);
            printf("back again in native c code\n");
        }

        /// get reactions form ReaDDy ///////////////////////////////////////////////////////////////////////////////////////////////

        if(testmode){
            cout << "Get Reactions" << endl;
            gettimeofday(&timeX, 0);
        }

        /// obtain the reaction array directly from within the java object
        jfieldID fid = env->GetFieldID(mClass, "cReactions", "[F");
        jobject mydata = env->GetObjectField(obj, fid);
        jfloatArray *jReactions = reinterpret_cast<jfloatArray*>(&mydata);
        jfloat *reactions = (jfloat*)(env)->GetPrimitiveArrayCritical( *jReactions, NULL);

        if(testmode){
            cout << reactions[0] << " reactions" << endl;
        }
        /// reaction-code:  if index==-1 -> create new particle
        ///                 if newType==-1 -> delete particle
        ///                 else -> modify particle (first delete, and than create)
        /// array: [0]number reactions, then 6-tupel: [ID(ReaDDy), type, posX, posY, posZ, index(ID OpenMM)]

        /// vector with new positions (copy old positions)
        std::vector<OpenMM::Vec3> posInNmUnlocked(posInNm);
        int numReactions=reactions[0];
        for(int r=0; r<numReactions; r++){
            /// new particle properties:
            int ID = reactions[1+(6*r)];        /// particleID in ReaDDy
            int newType = reactions[1+(6*r)+1]; /// new particle type
            OpenMM::Vec3 newPos = OpenMM::Vec3(reactions[3+(6*r)],reactions[4+(6*r)],reactions[5+(6*r)]);   /// new particle positions
            int index = reactions[1+6*r+5]; /// particleindex in OpenMM

            if(index!=-1){
                /// -> delete particle
                if(testmode)
                    cout <<"deletion: index:" <<index << " type:" << partType[index] << ", ID:" << ID << " pos: " << posInNmUnlocked.at(index)[0] << " " << posInNmUnlocked.at(index)[1] << " " << posInNmUnlocked.at(index)[2] << endl;
                posInNmUnlocked.at(index)=OpenMM::Vec3(rand()%1000+1000,rand()%1000+1000,rand()%1000+1000);
                /// delete from all forces
                /// //////////////////////////////////////////////////////////////////////////////
                for(int force=0; force<customExternalForces.size(); force++){
                    if(affectedParticleTypesCustomExternalForces[force][partType[index]]==1){
                        double *para = new double[ParametersCustomExternalForces[force][0].size()];
                        for(int parameter=0; parameter<ParametersCustomExternalForces[force][0].size(); parameter++){
                            para[parameter]=0;
                        }
                        const std::vector<double> param (para, para + ParametersCustomExternalForces[force][0].size() );
                        customExternalForces[force]->setParticleParameters(termIndices[force][index], index, param);
                    }
                }
                for(int force=0; force<customNonbondForces.size(); force++){
                    double *para = new double[ParametersCustomNonbondForces[force][0].size()];
                    for(int parameter=0; parameter<ParametersCustomNonbondForces[force][0].size(); parameter++){
                        para[parameter]=0;
                    }
                    const std::vector<double> param (para, para + ParametersCustomNonbondForces[force][0].size() );
                    customNonbondForces[force]->setParticleParameters(index,param);
                }

                /// ////////////////////////////////////////////////////////////////////////////////
                /// edit Type, jID, and set respective number of (free) particles
                int oldType = partType[index];
                partType[index]= -1;
                jID[index]=-1;
                NumOfParticles--;
                numberOfFreePositions[oldType]++;
                /// save position(OpenMM ID) from the free slot
                freePositionsInParticleArray[oldType].push_back(index);
            }

            if(newType!=-1){
                /// create a new particle
                NumOfParticles++;
                if(numberOfFreePositions[newType]==0){
                    cerr << "out of dummieparticles! restart with more dummieparticles for type " << newType << endl;
                    return;
                }
                /// use a free slot in dummy particle array
                numberOfFreePositions[newType]--;
                int newIndex=freePositionsInParticleArray[newType].back();
                freePositionsInParticleArray[newType].pop_back();
                index=newIndex;
                if(testmode){
                    cout << "creation: index: " << index << " newType: " << newType << " ID: " << ID <<endl;
                }
                /// Positions
                posInNmUnlocked.at(newIndex)=newPos;
                /// Type
                partType[newIndex]= newType;
                int type = newType;
                jID[newIndex]=ID;
                /// add particle to all necessary forces
                /// //////////////////////////////////////////////////////////////////////////////
                for(int force=0; force<customExternalForces.size(); force++){
                    double *para = new double[ParametersCustomExternalForces[force][0].size()];
                    for(int parameter=0; parameter<ParametersCustomExternalForces[force][0].size(); parameter++){
                        para[parameter]=ParametersCustomExternalForces[force][type][parameter];
                    }
                    if(affectedParticleTypesCustomExternalForces[force][type]==1){
                        const std::vector<double> param (para, para + ParametersCustomExternalForces[force][0].size() );
                        customExternalForces[force]->setParticleParameters(termIndices[force][index], index, param);
                    }
                }
                for(int force=0; force<customNonbondForces.size(); force++){
                    double *para = new double[ParametersCustomNonbondForces[force][0].size()];
                    for(int parameter=0; parameter<ParametersCustomNonbondForces[force][0].size(); parameter++){
                        para[parameter]=ParametersCustomNonbondForces[force][type][parameter];
                    }
                    const std::vector<double> param (para, para + ParametersCustomNonbondForces[force][0].size() );
                    customNonbondForces[force]->setParticleParameters(index, param);
                }

                /// ////////////////////////////////////////////////////////////////////////////////
                if(testmode)
                    cout << "-> " << newIndex << "(type:"<< newType << ", ID:" << ID << "): " << posInNmUnlocked.at(index)[0] << " " << posInNmUnlocked.at(index)[1] << " " << posInNmUnlocked.at(index)[2] <<endl;
            }
        }


        (env)->ReleasePrimitiveArrayCritical( *jReactions, reactions, 0);

        if(testmode)
            cout << "numOfParticles: " << NumOfParticles << endl;

        if(testmode)
            cout << "context.setPositions(posInNm2)" << endl;

        /// set new Positions
        const std::vector<OpenMM::Vec3>& posInNm2 (posInNmUnlocked);
        context.setPositions(posInNm2);

        if(testmode)
            cout << "updateParametersInContext(context)"<< endl;

        for(int force=0; force<customExternalForces.size(); force++){
            customExternalForces[force]->updateParametersInContext(context);
        }
        for(int force=0; force<customNonbondForces.size(); force++){
            customNonbondForces[force]->updateParametersInContext(context);
        }

        if(testmode){
            timeCReactions+=(float)getTime(timeX);
            cout << "runtime: " << getTime(startSimulationClock) << endl;
            cout << frameNumber/totalNumberOfFrames*100 << "%" << " approximate residual runtime: " <<
                    (1.-frameNumber/totalNumberOfFrames)*(getTime(startSimulationClock)/(frameNumber/totalNumberOfFrames))<< endl;
        }

    }

    if(testmode){
        cout << "END, total runtime: "<<getTime(startClock)<<endl;
        cout << "simulated:\t\t"   << totalSimulationTime <<"\t picoseconds" << endl;
        cout << "timesteps:\t\t"   << stepSizeInPs <<"\t picoseconds"<< endl;
        cout << "steps total:\t\t" << totalSimulationTime/stepSizeInPs  << endl;
        cout << "steps per frame:\t"<< stepsPerFrame << endl;
        cout << "frames written: \t" << frameNumber << endl;
        cout << "picosecons per frame:\t" << stepSizeInPs*stepsPerFrame <<endl;
        cout << endl;
        cout << "Times:"<<endl;
        float allTimes=timeGetPos+timeCpyPos+timeCallback+timeIntegrate+timeCReactions;
        cout << "to get Positions:  " << timeGetPos << " \t= " << 100*timeGetPos/(allTimes) << "%" << endl;
        cout << "to copy Positions: " << timeCpyPos << " \t= " << 100*timeCpyPos/(allTimes) << "%" << endl;
        cout << "Callback needs:    " << timeCallback << " \t= " << 100*timeCallback/(allTimes) << "%" << endl;
        cout << "to get Reactions:  " << timeCReactions << " \t= " << 100*timeCReactions/(allTimes) << "%" << endl;
        cout << "to Integrate:      " << timeIntegrate << " \t= " << 100*timeIntegrate/(allTimes) << "%" << endl;
        cout << "total simulation runtime: "<< getTime(startSimulationClock)<<endl;
        cout << "other:             " << getTime(startClock)-(allTimes) << endl;
        /*if(fail){
            cout << "!!! ERRORS occured !!!" << endl;
        }*/
    }

    delete[] D;
    delete[] R;
    return;
}



/// AUXILLARY FUNCTIONS //////////////////////////////////////////////////////

// own string split
string* split(string s, string c){
    int x = 0;
    int count = 0;
    //<p id="0" type="1" c="-31.09250630276887,-42.34489538041901,34.654154873058644"/>
    string* result = new string[7];
    for(int i=0; i<s.length();i++){
        if(s[i]==c[0]){
            result[count]=s.substr(x,i-x);
            count++;
            x=i+c.length();
        }
    }
    result[count]=s.substr(x,s.length()-x);
    return result;
}

// clock function
double getTime(timeval start){
    timeval end;
    gettimeofday(&end, 0);
    double sec =(double)(end.tv_sec-start.tv_sec);
    double usec = (double)(end.tv_usec-start.tv_usec);
    return(sec+(0.000001*usec) );
}

// string to double function
/// http://stackoverflow.com/questions/392981/how-can-i-convert-string-to-double-in-c
double string_to_double( const std::string& s )
{
  std::istringstream i(s);
  double x;
  if (!(i >> x))
    return 0;
  return x;
}


