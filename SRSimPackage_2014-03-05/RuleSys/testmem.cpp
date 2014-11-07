
#include "templ_site.h"
#include "templ_molecule.h"
#include "stdio.h"
#include "stdlib.h"
#include "iostream"



using namespace std;

int main (int argc, char **argv)
  {
  printf ("ich bin doof!\n");
  cout << "ich auch\n";
  
  TemplMolecule *m1 = new TemplMolecule (2,4),
                *m2 = new TemplMolecule (1,3);
  
                
  vector<TemplMolecule*> *v = new vector<TemplMolecule*>;
  //vector<Molecule*> v2;
  
/*  TemplMolecule** x1 = (TemplMolecule**) malloc (sizeof(TemplMolecule*)*20000000L);
  cout << "Writing\n";
  for (long i=0 ; i<20000000 ; i++) x1[i] = new TemplMolecule(i,i+45);
  cout << "Freeing\n";
  for (long i=0 ; i<20000000 ; i++) delete x1[i];
  cout << "Freeing2\n";
  delete []x1;*/
  
  
/*  TemplMolecule** x1 = (TemplMolecule**) malloc (sizeof(TemplMolecule*)*20000000L);
  cout << "Writing\n";
  for (long i=0 ; i<20000000 ; i++) x1[i] = (TemplMolecule*) malloc(sizeof(TemplMolecule));
  //cout << "Freeing\n";
  //for (long i=0 ; i<20000000 ; i++) free (x1[i]);
  cout << "Writing again\n";
  for (long i=0 ; i<20000000 ; i++) x1[i] = (TemplMolecule*) malloc(sizeof(TemplMolecule));
  cout << "Freeing2\n";
  delete []x1;*/
  
/*  TemplMolecule* x = (TemplMolecule*) malloc (sizeof(TemplMolecule) * 20000000);
  TemplMolecule  y(2,3);
  for (long i=0 ; i<20000000L ; i++) x[i] = y;
  free (x);*/
  
  printf ("Filling 20 Mio * %ld \n",sizeof(TemplMolecule));
  for (int i=0 ; i<20000000 ; i++) v->push_back (new TemplMolecule(i,2));
      
  printf ("trying to deletete:\n");
  for (int i=0 ; i<20000000 ; i++) 
      {
      //Molecule *m = v->at(i);
      //free (m);
      Molecule *m = v->at(v->size() - 1);
      //v->pop_back();
      //delete m;
      }
      
  printf ("cap = %ud \n",v->capacity());
  v->clear();
  v->resize(0);
//  delete v;
  cout << "cap = " << v->capacity() << "\n";
  
  printf ("refilling:\n");
  for (int i=0 ; i<20000000 ; i++) v->push_back (new TemplMolecule(i,7));
  
  for (int i=0 ; i<20000000 ; i++) cout << "wääääääiting.\n";
                
  return 0;
  }

