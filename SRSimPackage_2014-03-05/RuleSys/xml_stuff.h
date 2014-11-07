//
// C++ Interface: xml_stuff
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef XML_STUFF_H
#define XML_STUFF_H

#include <stdio.h>

#include <string>
#include <iostream>
#include <stack>


#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/parsers/XercesDOMParser.hpp>



using namespace std;
using namespace xercesc;


class XmlError 
    {
    public:
     string err;
     XmlError (string _err) : err(_err) {}
     XmlError (string _err, string _err2) {err = _err+_err2; }
     void print () { cerr << "XmlStuffError: "<< err; }
    };
    
class XmlTagOptionNotFound : public XmlError
    {
    public:
     XmlTagOptionNotFound(string _err)              : XmlError(_err)       {}
     XmlTagOptionNotFound(string _err, string _err2): XmlError(_err,_err2) {}
    };

/**
@author Gerd Gruenert
*/
class XmlStuff
    {
    public:
     XmlStuff(string _fname);
    ~XmlStuff();
    
     bool   goToNextTag       (string tag);
     void   goIntoTag         ();
     void   returnFromTag     ();
     void   redoActualTag     ();           // of the actual tag-layer...
     string retrieveTagOption (string opt);
    
    private:
     string           fname;
     XercesDOMParser *domparser;
     DOMDocument     *xmlDoc;
     DOMElement      *elementRoot;
     
     stack<int>                actIndex;
     stack<class DOMNodeList*> actChilds;
     stack<class DOMElement *> actElem;
     
     void xmlInit ();
     void xmlClose();
     };

#endif
