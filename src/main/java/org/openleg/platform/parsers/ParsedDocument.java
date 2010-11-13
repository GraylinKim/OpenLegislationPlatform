package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ParsedDocument {
	
	protected Document xml;
	protected HashMap<String,ArrayList<String>> solr;
	
	protected HashMap<String,InputProcessor> inputProcessors;
	protected HashMap<String,TreeFlagHandler> treeFlagHandlers;
	protected HashMap<String,NodeFlagHandler> nodeFlagHandlers;
	
	protected HashMap<String,HashMap<String,String>> schemaTreeFlags;
	protected HashMap<String,HashMap<String,String>> schemaNodeFlags;
	protected HashMap<String,HashMap<String,String>> localTreeFlags;
	protected HashMap<String,HashMap<String,String>> localNodeFlags;
	
	protected TreeSet<Node> processedNodes;
	
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
	

	
	public NodeState getNodeState(Node localNode, String schemaString) {
		return new NodeState(localNode,schemaString,this);
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

}
