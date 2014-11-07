//
// C++ Implementation: xml_stuff
//
// Description: 
//
//
// Author: Gerd Gruenert <gerdl@ipc758>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "xml_stuff.h"


#include <xercesc/dom/DOM.hpp>
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMDocumentType.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMImplementationLS.hpp>
#include <xercesc/dom/DOMNodeIterator.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMText.hpp>

#include <xercesc/parsers/XercesDOMParser.hpp>
#include <xercesc/util/XMLUni.hpp>



XmlStuff::XmlStuff(string _fname) : fname(_fname)
  {
  xmlInit();
  
  // prepare parser:
  domparser = new XercesDOMParser();
  domparser->setValidationScheme( XercesDOMParser::Val_Never );
  domparser->setDoNamespaces    ( false );
  domparser->setDoSchema        ( false );
  domparser->setLoadExternalDTD ( false );  
  
  try
    {
    domparser->parse( fname.c_str() );
    
    string blub = string("xxx");
    
    // what the heck,...
    xmlDoc = domparser->getDocument();
    if( !xmlDoc ) throw XmlError( string("Seems we couldn't open the gile: ")+fname);
    elementRoot = xmlDoc->getDocumentElement();
    if( !elementRoot ) throw XmlError(string("Empty XML Document: ")+fname);
    
    DOMNodeList* children = elementRoot->getChildNodes();

    actIndex .push( 0 );
    actChilds.push( children );
    actElem  .push( NULL );  
    }
  catch (const DOMException& toCatch) 
    { throw XmlError(string("Error opening XML file: ")+fname+" !"); }
  catch( XMLException& e )
    {
    char* message = XMLString::transcode( e.getMessage() );
    throw XmlError(string("Error opening XML file: ")+fname+" !");
    }  
  }


XmlStuff::~XmlStuff()
  {
  xmlClose();
  }

void XmlStuff::xmlInit( )
  {
  // init xml stuff:
  try
    {
    XMLPlatformUtils::Initialize();  // Initialize Xerces infrastructure
    }
  catch( XMLException& e )
    {
    char* message = XMLString::transcode( e.getMessage() );
    throw XmlError(string("Error initing Xerces-XML-System")+message);
    }  
  }

void XmlStuff::xmlClose( )
  {
  // finalize:  
  try
    {
    delete domparser;
    XMLPlatformUtils::Terminate();  // Terminate Xerces
    }
  catch( xercesc::XMLException& e )
    {
    char* message = xercesc::XMLString::transcode( e.getMessage() );
    throw XmlError(string("Error finalizing Xerces-XML-System")+message);
    }
  }

bool XmlStuff::goToNextTag( string tag )
  {
  int          idx     = actIndex .top();
  DOMNodeList* childs  = actChilds.top();
  //DOMElement * elem    = actElem  .top();  

  try 
    {  
    XMLSize_t nodeCount = childs->getLength();
    // run over all molecules:
    for (XMLSize_t i=idx+1 ; i<nodeCount ; i++ )
        {
        DOMNode* currentNode = childs->item(i);
        if (! currentNode->getNodeType() ||  // true is not NULL
              currentNode->getNodeType() != DOMNode::ELEMENT_NODE ) // is element 
           continue;
      
        DOMElement *elem = dynamic_cast< xercesc::DOMElement* >( currentNode );
      
        char* thisTag = XMLString::transcode( elem->getTagName() );
        bool found = (tag.compare(thisTag) == 0);
        XMLString::release( &thisTag );
      
        if (found)
           {
           //actChilds.stay_the_way_you_are();
           actIndex.pop ();
           actIndex.push( i );
           actElem .pop();
           actElem .push( elem );
           return true;
           }
        }
    }
    
  catch (const DOMException& toCatch) 
    { throw XmlError(string("Error finding next XML Tag: ")+tag+" !"); }
  catch( XMLException& e )
    {
    char* message = XMLString::transcode( e.getMessage() );
    throw XmlError(string("Error finding next XML Tag: ")+tag+" !");
    }  
      
  return false;
  }

void XmlStuff::goIntoTag( )
  {
  //int          idx     = actIndex .top();
  //DOMNodeList* childs  = actChilds.top();
  DOMElement * elem    = actElem  .top();  
  
  try
    {
    DOMNodeList* childs = elem->getChildNodes();
   
    actChilds.push( childs );
    actIndex .push( 0 );
    actElem  .push( NULL );
    }
  catch (const DOMException& toCatch) 
    { throw XmlError(string("Error entering XML tag: ")); }
  catch( XMLException& e )
    {
    char* message = XMLString::transcode( e.getMessage() );
    throw XmlError(string("Error entering XML tag: "));
    }  
  }

void XmlStuff::returnFromTag( )
  {
  actChilds.pop();
  actIndex .pop();
  actElem  .pop();
  
  if (actChilds.empty()) throw XmlError("XmlStuff::returnFromTag: trying to return too far! There's nothing left to return to!");
  }

void XmlStuff::redoActualTag( )
  {
  actIndex.pop ();
  actIndex.push( 0 );
  }

/** throws XmlTagOptionNotFound */
string XmlStuff::retrieveTagOption( string opt )
  {
  //printf (("XmlStuff:: I should look for tag option: "+opt+"\n").c_str());
  DOMElement *elem = actElem.top();  
  
  string ret;
  try
    {
    XMLCh* optionTranscoded = XMLString::transcode( opt.c_str() );
    const XMLCh* value = elem->getAttribute(optionTranscoded);
    char* valTrans = XMLString::transcode(value);
      
    ret = string(valTrans);
    XMLString::release( &optionTranscoded );
    XMLString::release( &valTrans );
    }
  catch (const DOMException& toCatch) 
    { 
    throw XmlTagOptionNotFound(string("Error retrieving tag Option: ")+opt); 
    }
  catch( XMLException& e )
    {
    char* message = XMLString::transcode( e.getMessage() );
    throw XmlTagOptionNotFound(string("Error retrieving tag Option: ")+opt);
    }  
  
  //printf (("  XmlStuff:: done looking for tag option: "+opt+"\n").c_str());
  return ret;
  }








