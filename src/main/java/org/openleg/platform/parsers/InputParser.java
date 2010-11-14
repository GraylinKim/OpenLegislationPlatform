package org.openleg.platform.parsers;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InputParser extends XmlUtil {

	public ArrayList<HashMap<String,ArrayList<String>>> documents;
	
	public static void main(String[] args) {
		InputParser xml = new InputParser("/home/openleg/OpenLegislationPlatform/src/main/resources/input/S66023-2009.xml");
		System.out.println("Success");
		for(HashMap<String,ArrayList<String>> document : xml.documents) {
			System.out.println("Document Display:");
			for(String key : new TreeSet<String>(document.keySet())) {
				System.out.println("Key Set");
				if(!key.equalsIgnoreCase("defaultSearch")) {
					System.out.println("displaying:");
					for(String value : document.get(key)) {
						System.out.println(key+": "+value);
					}
				}
			}
			System.out.println("\n");
			//System.out.println("\n\n"+document.get("xml")+"\n\n");
		}
		
		//Index that the docs into solr
		
		try {
			//Could use EmbeddedSolrServer(), depends on how we are setting up the environments?
			SolrServer server = (SolrServer)new CommonsHttpSolrServer("http://localhost:8080/solr/");
			server.add(xml.getSolrDocuments());
			server.commit();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<SolrInputDocument> getSolrDocuments() {
		ArrayList<SolrInputDocument> solrDocuments = new ArrayList<SolrInputDocument>();
		for( HashMap<String,ArrayList<String>> doc : documents) {
			SolrInputDocument solrDoc = new SolrInputDocument();
			for( String key : doc.keySet()) {
				for( String value : doc.get(key)) {
					solrDoc.addField(key, value);
				}
			}
			solrDocuments.add(solrDoc);
		}
		return solrDocuments;
	}
	
	public InputParser(String filename) {
		documents = new ArrayList<HashMap<String,ArrayList<String>>>();
		searchNode(getXmlDocument(filename).getDocumentElement());
	}
	
	public void searchNode(Node root) {
		
		if(isFlagSet(root,"document")) {
			startDoc(root);
		
		} else {
			//look for a correctly marked node
			NodeList children = root.getChildNodes();
			for(int n = 0; n < children.getLength(); n++) {
				searchNode(children.item(n));
			}
		}
		
	}
	
	public void startDoc(Node inputRoot) {
		Document xmlDoc = newXmlDocument();
		HashMap<String,ArrayList<String>> solrDoc = new HashMap<String,ArrayList<String>>();
		
		addSolrField(solrDoc,"","otype", inputRoot.getNodeName());
		addSolrField(solrDoc,"","defaultSearch", inputRoot.getTextContent());
		Node outputRoot = xmlDoc.createElement(inputRoot.getNodeName());
		
		//Leaf nodes are handled here without delegating to a builder
		if(isLeafNode(inputRoot)) {
			
			//Don't add any fields to the solrDoc
			//Append a text node to the xmlDoc
			outputRoot.appendChild(xmlDoc.createTextNode(inputRoot.getTextContent()));
			
		//Tree nodes are delegated off to a recursive builder
		} else {
			buildChildren(inputRoot,outputRoot,solrDoc,xmlDoc,"");
		}
		
		//Append this root to the xmlDoc
		xmlDoc.appendChild(outputRoot);
		
		try {
			//Apply a transformer to serialize the xmlDoc and attach it to the solrDoc hashmap
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			//the transformer requires a writer for output
			StringWriter out = new StringWriter(); 
			transformer.transform(new DOMSource(xmlDoc), new StreamResult(out));
			addSolrField(solrDoc,"","xml",out.toString());
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		//Add the solrDoc to the collection
		documents.add(solrDoc);
	}
	
	public void buildChildren(Node inputRoot, Node outputRoot, HashMap<String,ArrayList<String>> solrDoc, Document xmlDoc, String solrPrefix) {
		
		//In order to avoid complications caused by white space text nodes
		//in the buildNode phase we attempt to filter them out here
		NodeList children = inputRoot.getChildNodes();
		for(int n = 0; n < children.getLength(); n++) {
			Node child = children.item(n);
			
			//Ignore text nodes! Only text nodes that are leaf bodies will count.			
			if(child.getNodeType() == Node.TEXT_NODE) {
				//I should probably issue some kind of warning here...
				
			//All other nodes should be a proper elements and can be built
			} else {
				buildNode(child,outputRoot,solrDoc,xmlDoc,solrPrefix);	
			}
			
		}
	}
	
	public void buildNode(Node inputRoot, Node outputRoot, HashMap<String,ArrayList<String>> solrDoc, Document xmlDoc, String solrPrefix) {

		boolean xml_exclude = isFlagSet(inputRoot,"xml_exclude");
		boolean solr_exclude = isFlagSet(inputRoot,"solr_exclude"); 
		
		if(isLeafNode(inputRoot)) {
			
			if(isFlagSet(inputRoot,"document"))
				startDoc(inputRoot);

			if(!solr_exclude)
				indexLeaf(inputRoot,solrDoc,solrPrefix);
			
			if(!xml_exclude)
				writeLeaf(inputRoot,outputRoot,xmlDoc);
			
		} else {
			
			//Completely ignored in all ways...
			if(xml_exclude && solr_exclude) {
				return;
				
			//Only copy this tree to xml
			} else if (xml_exclude) {
				copyTree(inputRoot,outputRoot,xmlDoc);
				
			//Only index this tree in solr
			} else if (solr_exclude) {
				indexTree(inputRoot,solrDoc,solrPrefix);
				
			//Both into solr and copy into xml
			} else {
				
				if(isFlagSet(inputRoot,"document"))
					startDoc(inputRoot);
				
				if(isFlagSet(inputRoot,"solr_value"))
					addSolrField(solrDoc,solrPrefix,inputRoot.getNodeName(),attributeValue(inputRoot,"solr_value"));
				
				if(!isFlagSet(inputRoot,"solr_container"))
					solrPrefix = extendPrefix(solrPrefix,inputRoot);
				
				//Recurse down the rest of the tree
				Node newChild = xmlDoc.createElement(inputRoot.getNodeName());
				buildChildren(inputRoot,newChild,solrDoc,xmlDoc,solrPrefix);
				outputRoot.appendChild(newChild);
			}
		}
		
	}
	
	public void addSolrField(HashMap<String,ArrayList<String>> solrDoc,String prefix, String name, String value) {
		name = prefix+name;
		ArrayList<String> values = solrDoc.get(name);
		if(solrDoc.get(name) == null) {
			values = new ArrayList<String>();
			solrDoc.put(name, values);
		}
		values.add(value);
		solrDoc.put(name, values);
	}
	
	public void copyTree(Node inputRoot, Node outputRoot, Document xmlDoc) {
		
		if(isFlagSet(inputRoot,"document"))
			startDoc(inputRoot);
		
		if(!isFlagSet(inputRoot,"xml_exclude")) {
			
			if(isLeafNode(inputRoot)) {
				writeLeaf(inputRoot,outputRoot,xmlDoc);
				
			} else {
				writeElement(inputRoot,outputRoot,xmlDoc);
				
			}
			
		} else {
			
			//Do nothing
		}
	}
	
	public void indexTree(Node inputRoot, HashMap<String,ArrayList<String>> solrDoc, String solrPrefix) {
		
		if(isFlagSet(inputRoot,"document"))
			startDoc(inputRoot); 
		
		if(!isFlagSet(inputRoot,"solr_exclude")) {
			
			if(isLeafNode(inputRoot)) {
				indexLeaf(inputRoot,solrDoc,solrPrefix);
				
			} else {
				indexElement(inputRoot,solrDoc,solrPrefix);
				
			}
			
		} else {
			
			//Do nothing...
		}
	}
	
	public void indexLeaf(Node inputRoot, HashMap<String,ArrayList<String>> solrDoc, String solrPrefix) {
		
		//Add the leaf as a field in lucene with the correct prefix
		addSolrField(solrDoc,solrPrefix,inputRoot.getNodeName(),inputRoot.getTextContent().trim());
	}
	
	public void indexElement(Node inputRoot, HashMap<String,ArrayList<String>> solrDoc, String solrPrefix) {
		
		//Unless its a transparent container, extend the prefix
		if(!isFlagSet(inputRoot,"solr_container"))
			solrPrefix = extendPrefix(solrPrefix,inputRoot);
		
		//Pass the incremented solrPrefix on
		NodeList children = inputRoot.getChildNodes();
		for(int n = 0; n < children.getLength(); n++) {
			indexTree(children.item(n),solrDoc,solrPrefix);
		}
		
	}
	public void writeLeaf(Node inputRoot, Node outputRoot, Document xmlDoc) {
		
		if(isFlagSet(inputRoot,"xml_attribute")) {
			((Element)outputRoot).setAttribute(inputRoot.getNodeName(), inputRoot.getTextContent().trim());
			
		} else {
			//Create the leaf, append the text node body, attach it to the tree
			Node leaf = xmlDoc.createElement(inputRoot.getNodeName());
			leaf.appendChild(xmlDoc.createTextNode(inputRoot.getTextContent().trim()));
			outputRoot.appendChild(leaf);
		}
	}
	
	public void writeElement(Node inputRoot, Node outputRoot, Document xmlDoc) {
		
		NodeList children = inputRoot.getChildNodes();
		Node newChild = xmlDoc.createElement(inputRoot.getNodeName());
		
		for(int n = 0; n < children.getLength(); n++) {
			Node child = children.item(n);
			copyTree(child,newChild,xmlDoc);
		}
		
		outputRoot.appendChild(newChild);
	}
	
	public String extendPrefix(String prefix, Node node) {
		if(prefix.length()==0)
			return node.getNodeName()+".";
		return prefix+node.getNodeName()+".";
	}
	
}