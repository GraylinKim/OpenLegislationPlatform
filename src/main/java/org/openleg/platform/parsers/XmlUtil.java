package org.openleg.platform.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtil {
	
	public static void main(String[] args) {
		XmlUtil.printDocument(XmlUtil.getXmlDocument("/home/graylin/RCOS/input/ADAMS-2009.xml"));
	}
	
	public static Document getXmlDocument(String filename) {
		return getXmlDocument(new File(filename));
	}
	
	public static Document getXmlDocument(File file) {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
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
	
	public static Document newXmlDocument() {
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
			if(children.getLength()==1 && children.item(0).getNodeType()==Node.TEXT_NODE)
				System.out.println(prefix+"<"+tag+">"+children.item(0).getNodeValue().trim()+"</"+tag+">");
			else {
				System.out.println(prefix+"<"+root.getNodeName()+">");
				for(int n = 0; n< children.getLength(); n++) {
					printNode(children.item(n),indent+1);
				}
				System.out.println(prefix+"</"+root.getNodeName()+">");
			}

		}
		

	}
	
	public static Document buildDocument(Document input) {
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
	
	public static void buildChildren(Document doc,Node input,Node output) {
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

	public static boolean isFlagSet(Node node, String attribute) {
		String value = attributeValue(node,attribute);
		if( value != null )
			return value.equalsIgnoreCase("true");
		return false;
	}
	
	public static String attributeValue(Node node, String attribute) {
		
		NamedNodeMap attrs = node.getAttributes();
		if(attrs == null)
			return null;
		
		Node attr = attrs.getNamedItem(attribute);
		if(attr == null)
			return null;
		
		return attr.getNodeValue();
	}
	
	public static ArrayList<Node> getChildElements(Node node) {
		NodeList children = node.getChildNodes();
		
		//This gets all NON TEXT_NODE children
		ArrayList<Node> ret = new ArrayList<Node>();
		for(int n = 0; n < children.getLength(); n++) {
			Node child = children.item(n);
			if(child.getNodeType() != Node.TEXT_NODE)
				ret.add(child);
		}
		
		return ret;
	}
	
	public static boolean isLeafNode(Node leaf) {
		NodeList children = leaf.getChildNodes();
		if(children.getLength() == 0)
			return true;
		else if(children.getLength() == 1)
			return (children.item(0).getNodeType() == Node.TEXT_NODE);
		else
			return false;
	}
	
	public static String getChildValue(Node node,String childName) {
		NodeList children = ((Element)node).getElementsByTagName(childName);
		
		if(children.getLength()==1) {
			Node child = children.item(0);
			if(XmlUtil.isLeafNode(child)) {
				return child.getFirstChild().getNodeValue().trim();
			} else {
				//Error!!! Should be a leaf node
				System.out.println("Child: "+child.getNodeName()+" must be a leaf node");
			}
		} else {
			//Error!!! Should only have 1 match
			System.out.println("There should only 1 one child matching: "+childName);
		}
		return null;
	}
}