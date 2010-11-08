package org.openleg.platform.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlParser {
	
	public static void main(String[] args) {
		XmlParser.printDocument(XmlParser.getXmlDocument("/home/graylin/RCOS/input/ADAMS-2009.xml"));
	}
	
	public static Document getXmlDocument(String filename) {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document newXmlDocument() {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void printDocument(Document doc) {
		printNode((Node)doc.getDocumentElement());
	}
	
	public static void printNode(Node root) {
		printNode(root,0);
	}
	
	public static void printNode(Node root,int indent) {
		String prefix = "";
		for(int n = 0; n < indent; n++)
			prefix+="\t";
		
		if(root.getNodeType()==Node.TEXT_NODE ) {
			if(!root.getNodeValue().trim().isEmpty()) {
				System.out.print(prefix+root.getNodeValue());
			}
		} else {
			String tag = root.getNodeName();
			NodeList children = root.getChildNodes();
			
			if(children.getLength()==0) {
				System.out.println(prefix+"<"+tag+"></"+tag+">");
			}
			if(children.getLength()==1)
				System.out.println(prefix+"<"+tag+">"+children.item(0).getNodeValue()+"</"+tag+">");
			else {
				System.out.println(prefix+"<"+root.getNodeName()+">");
				for(int n = 0; n< children.getLength(); n++) {
					printNode(children.item(n),indent+1);
				}
				System.out.println(prefix+"</"+root.getNodeName()+">");
			}

		}
		

	}
	
	
	public Document buildDocument(Document input) {
		try {
			Document output = newXmlDocument();
			Element inputRoot = input.getDocumentElement();
			Element outputRoot = output.createElement(inputRoot.getNodeName());
			buildChildren(output,inputRoot,outputRoot);
			output.appendChild(outputRoot);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void buildChildren(Document doc,Node input,Node output) {
		Node child;
		Node newChild;
		
		if(input.hasChildNodes()) {
			NodeList children = input.getChildNodes();
			for(int n = 0; n < children.getLength(); n++) {
				child = children.item(n);
				if(child.getNodeType() == Node.TEXT_NODE) {
					newChild = doc.createTextNode(child.getNodeValue());
				} else {
					newChild = doc.createElement(child.getNodeName());
					buildChildren(doc,child,newChild);
				}
				output.appendChild(newChild);
			}
		}
	}

}