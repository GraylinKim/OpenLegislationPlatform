package org.openleg.platform.parsers;

import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NodeState {
	
	public String writeName;
	public String writeValue;
	public String solrPrefix;
	
	public boolean writeXml;
	public boolean writeSolr;
	
	
	public HashMap<String,String> treeFlags;
	public HashMap<String,String> nodeFlags;
	
	public HashMap<String,String> parentTreeFlags;
	public HashMap<String,String> parentNodeFlags;		
	
	public NodeState(Node node,String schemaString,ParsedDocument doc) {
		
		loadWriteValues(node);
		loadParentFlags(node,schemaString,doc);
		loadLocalFlags(node,schemaString,doc);
		
	}

	private void loadWriteValues(Node node) {
		//Construct Modifiable NodeState from node
		writeXml = true;
		writeName = node.getNodeName();
		if(XmlUtil.isLeafNode(node)) {
			writeValue = node.getTextContent().trim();
			writeSolr = true;
		}
		else {
			writeSolr = false;
			writeValue = null;
		}
	}
	private void loadParentFlags(Node node,String schemaString, ParsedDocument doc) {
		parentTreeFlags = new HashMap<String,String>();
		parentNodeFlags = new HashMap<String,String>();

		
		//Set up for parent discovery and aggregation
		String[] parents = schemaString.split(".");
		
		//If its more than just ourselves
		if(parents.length >= 2) {
			//Loop over all nodeStrings but our own and build an aggregate HashMaps
			String parentString = "";
			for(int i = 0; i < parents.length-1; i++) {
				parentString += (i!=0) ? "."+parents[i] : parents[i];
				
				//Put schema flags in first so we can over write with local tree flags
				parentTreeFlags.putAll(doc.schemaTreeFlags.get(parentString));
				parentTreeFlags.putAll(doc.localTreeFlags.get(parentString));
				
				parentNodeFlags.putAll(doc.schemaNodeFlags.get(parentString));
				parentNodeFlags.putAll(doc.localNodeFlags.get(parentString));
			}
		}
	}
	
	private void loadLocalFlags(Node node,String schemaString,ParsedDocument doc) {
		treeFlags = new HashMap<String,String>();
		nodeFlags = new HashMap<String,String>();
		
		
		if(node.hasAttributes()) {
			HashMap<String,String> localTreeFlagMap = new HashMap<String,String>();
			HashMap<String,String> localNodeFlagMap = new HashMap<String,String>();
			
			//Try to math against every attribute
			NamedNodeMap attributes = node.getAttributes();
			for(int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				
				//If its a tree Attribute, stick it in the localTreeFlagsMap
				if(doc.treeFlagHandlers.containsKey(attributeName)) {
					localTreeFlagMap.put(attributeName, attribute.getNodeValue());
					
				//if its a node attribute stick it in the localNodeFlagsMap
				} else if (doc.nodeFlagHandlers.containsKey(attributeName)) {
					localNodeFlagMap.put(attributeName, attribute.getNodeValue());
					
				} else {
					//This Attribute hasn't been registered!!!!
				}
			}
			doc.localTreeFlags.put(schemaString,localTreeFlagMap);
			doc.localNodeFlags.put(schemaString,localNodeFlagMap);
		}
		
		//Put schema flags in first so we can over write with local tree flags
		treeFlags.putAll(doc.schemaTreeFlags.get(schemaString));
		treeFlags.putAll(doc.localTreeFlags.get(schemaString));
		
		nodeFlags.putAll(doc.schemaNodeFlags.get(schemaString));
		nodeFlags.putAll(doc.localNodeFlags.get(schemaString));
	}
	
}