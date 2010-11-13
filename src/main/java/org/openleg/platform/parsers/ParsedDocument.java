package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ParsedDocument {
	
	private Document xml;
	private HashMap<String,ArrayList<String>> solr;
	
	private HashMap<String,InputProcessor> inputProcessors;
	private HashMap<String,TreeFlagHandler> treeFlagHandlers;
	private HashMap<String,NodeFlagHandler> nodeFlagHandlers;
	
	private HashMap<String,HashMap<String,String>> schemaTreeFlags;
	private HashMap<String,HashMap<String,String>> schemaNodeFlags;
	private HashMap<String,HashMap<String,String>> localTreeFlags;
	private HashMap<String,HashMap<String,String>> localNodeFlags;
	
	private TreeSet<Node> processedNodes;
	
	public ParsedDocument(Node inputRoot, ParserConfiguration configuration) {
		
		//Create the internal documents
		xml = XmlUtil.newXmlDocument();
		solr = new HashMap<String,ArrayList<String>>();

		//Copy the configuration over
		inputProcessors = configuration.inputProcessors;
		treeFlagHandlers = configuration.treeFlagHandlers;
		nodeFlagHandlers = configuration.nodeFlagHandlers;
		
		//Get/Create the flag maps for this document
		localNodeFlags = new HashMap<String,HashMap<String,String>>();
		localTreeFlags = new HashMap<String,HashMap<String,String>>();
		schemaNodeFlags = configuration.nodeFlags.get(inputRoot.getNodeName());
		schemaTreeFlags = configuration.treeFlags.get(inputRoot.getNodeName());		
		
		//processedNodes keeps track of nodes that have had attributes pulled
		processedNodes = new TreeSet<Node>();
		
		//Process the root element and append the rests to the output tree
		Node outputRoot = xml.createElement(inputRoot.getNodeName());
		getNodeProcessor(inputRoot).processNode(inputRoot,"",outputRoot,this);
		xml.appendChild(outputRoot);
	}
	
	public InputProcessor getNodeProcessor(Node node) {
		
		//Figure out which processor we want to use
		String processorName = XmlUtil.attributeValue(node,"processor");
		if(processorName == null)
			processorName = "default";
		
		//return the indicated processor if it exists
		InputProcessor processor = inputProcessors.get(processorName);
		if(processor == null) {
			System.out.println("FATAL ERROR: No default processor defined");
			return null;
		} else
			return processor;
	}
	
	public Node createElement(String name) { return xml.createElement(name); }
	
	public TreeFlagHandler getTreeFlagHandler(String flag) {	return treeFlagHandlers.get(flag); }
	public NodeFlagHandler getNodeFlagHandler(String flag) {	return nodeFlagHandlers.get(flag); }
	
	public HashMap<String,String> getNodeNodeFlags(Node node, String nodeString) {
		return nodeFlags.get(nodeString);
	}
	
	public HashMap<String,String> getNodeTreeFlags(Node node, String nodeString) {
		NamedNodeMap attributes = node.getAttributes();
		for attributes
		
		return localTreeFlags.get(nodeString).putAll(schemaTreeFlags.get(nodeString));
	}
	
	class NodeState {
		
		public String nodeName;
		public String nodeValue;
		public String solrPrefix;
		
		public boolean writeSolr;
		public boolean writeXml;
		
		public HashMap<String,String> treeFlags;
		public HashMap<String,String> nodeFlags;
		
		public HashMap<String,String> parentTreeFlags;
		public HashMap<String,String> parentNodeFlags;		
		
		public NodeState() {
			treeFlags = new HashMap<String,String>();
			nodeFlags = new HashMap<String,String>();
			parentTreeFlags = new HashMap<String,String>();
			parentNodeFlags = new HashMap<String,String>();
		}
	}
	
	public NodeState getNodeState(Node localNode, String schemaString) {
		NodeState state = new NodeState();
		
		//Set up for parent discovery and aggregation
		String[] nodes = schemaString.split(".");
		
		//If its more than just ourselves
		if(nodes.length >= 2) {
			//Loop over all nodeStrings but our own and build an aggregate HashMaps
			String parentString = "";
			for(int i = 0; i < nodes.length-1; i++) {
				parentString += (i!=0) ? "."+nodes[i] : nodes[i];
				
				//Put schema flags in first so we can over write with local tree flags
				state.parentTreeFlags.putAll(schemaTreeFlags.get(parentString));
				state.parentTreeFlags.putAll(localTreeFlags.get(parentString));
				
				state.parentNodeFlags.putAll(schemaNodeFlags.get(parentString));
				state.parentNodeFlags.putAll(localNodeFlags.get(parentString));
			}
			
		}
		
		if(localNode.hasAttributes()) {
			HashMap<String,String> localTreeFlagMap = new HashMap<String,String>();
			HashMap<String,String> localNodeFlagMap = new HashMap<String,String>();
			
			//Try to math against every attribute
			NamedNodeMap attributes = localNode.getAttributes();
			for(int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				
				//If its a tree Attribute, stick it in the localTreeFlagsMap
				if(treeFlagHandlers.containsKey(attributeName)) {
					localTreeFlagMap.put(attributeName, attribute.getNodeValue());
					
				//if its a node attribute stick it in the localNodeFlagsMap
				} else if (nodeFlagHandlers.containsKey(attributeName)) {
					localNodeFlagMap.put(attributeName, attribute.getNodeValue());
					
				} else {
					//This Attribute hasn't been registered!!!!
				}
			}
			localTreeFlags.put(schemaString,localTreeFlagMap);
			localNodeFlags.put(schemaString,localNodeFlagMap);
		}
		
		//Put schema flags in first so we can over write with local tree flags
		state.treeFlags.putAll(schemaTreeFlags.get(schemaString));
		state.treeFlags.putAll(localTreeFlags.get(schemaString));
		
		state.nodeFlags.putAll(schemaNodeFlags.get(schemaString));
		state.nodeFlags.putAll(localNodeFlags.get(schemaString));
		
		return state;
	}
	
	private void processNodeAttributes(Node node) {
		
	}
	
	public void addSolrField(String prefix, String name, String value) {
		name = prefix+name;
		ArrayList<String> values = solr.get(name);
		if(solr.get(name) == null) {
			values = new ArrayList<String>();
			solr.put(name, values);
		}
		values.add(value);
		solr.put(name, values);
	}
	
	public HashMap<String,String> getParentTreeFlags(String nodeString) {
		
		//Set up for parent discovery and aggregation
		String[] nodes = nodeString.split(".");
		HashMap<String,String> ret = new HashMap<String,String>();
		
		//If we've only got ourself, return an empty map
		if(nodes.length < 2)
			return ret;
		
		//Loop over all nodeStrings but our own and build an aggregate HashMap
		String parentString = "";
		for(int i = 0; i < nodes.length-1; i++) {
			if(i!=0)
				parentString += ".";
			parentString += nodes[i];
			ret.putAll(treeFlags.get(parentString));
		}
		
		return ret;
	}
	

}
