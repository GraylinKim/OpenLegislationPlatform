package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.openleg.platform.parsers.ParserConfiguration;
import org.openleg.platform.parsers.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ParsedDocument {
	
	public Document xml;
	protected HashMap<String,ArrayList<String>> solr;
	
	protected HashMap<String,InputProcessor> inputProcessors;
	protected HashMap<String,TreeFlagHandler> treeFlagHandlers;
	protected HashMap<String,NodeFlagHandler> nodeFlagHandlers;
	protected HashMap<String,ArrayList<Flag>> treeFlags;
	protected HashMap<String,HashMap<String,Flag>> nodeFlags;
	
	public ParsedDocument(Node root, ParserConfiguration configuration) {
		
		//Create the internal documents
		xml = XmlUtil.newXmlDocument();
		solr = new HashMap<String,ArrayList<String>>();

		//Copy the configuration over
		inputProcessors = configuration.inputProcessors;
		treeFlagHandlers = configuration.treeFlagHandlers;
		nodeFlagHandlers = configuration.nodeFlagHandlers;
		
		//Get the flag maps for this document
		nodeFlags = configuration.nodeFlags.get(root.getNodeName());
		treeFlags = configuration.treeFlags.get(root.getNodeName());		

		//Process the root element and append the rests to the output tree
		NodeState initialState = new NodeState(root,this);
		InputProcessor processor = initialState.nodeProcessor();
		
		processor.processSolr(root,initialState,solr);
		xml.appendChild(processor.processXml(root, initialState, xml));
		XmlUtil.printNode(xml.getDocumentElement());
		for(String key : new TreeSet<String>(solr.keySet())) {
			for(String value : solr.get(key)) {
				System.out.println(key+": "+value);
			}
		}
	}
	
	public TreeFlagHandler getTreeFlagHandler(String flag) {
		if(treeFlagHandlers!=null)
			return treeFlagHandlers.get(flag);
		return null;
	}
	public NodeFlagHandler getNodeFlagHandler(String flag) {
		if(nodeFlagHandlers!=null)
			return nodeFlagHandlers.get(flag);
		return null;
	}
	
	public ArrayList<Flag> getTreeFlags(String schemaString) {
		if(treeFlags != null)
			return treeFlags.get(schemaString);
		return new ArrayList<Flag>();
	}
	public HashMap<String,Flag> getNodeFlags(String schemaString) {
		if(nodeFlags != null)
			return nodeFlags.get(schemaString);
		return new HashMap<String,Flag>();
	}
	
	/*
	public ParsedDocument(Node inputRoot, ParserConfiguration configuration) {
		
		//Create the internal documents
		xml = XmlUtil.newXmlDocument();
		solr = new HashMap<String,ArrayList<String>>();

		//Copy the configuration over
		inputProcessors = configuration.inputProcessors;
		treeFlagHandlers = configuration.treeFlagHandlers;
		nodeFlagHandlers = configuration.nodeFlagHandlers;
		
		//Get the flag maps for this document
		nodeFlags = configuration.nodeFlags.get(inputRoot.getNodeName());
		treeFlags = configuration.treeFlags.get(inputRoot.getNodeName());		

		//Process the root element and append the rests to the output tree
		String schemaString = inputRoot.getNodeName();
		Node outputRoot = getNodeProcessor(schemaString).processNode(inputRoot,"",this);
		xml.appendChild(outputRoot);
	}
	
	public Node createElement(String name) { return xml.createElement(name); }
	
	
	public TreeFlagHandler getTreeFlagHandler(String flag) { return treeFlagHandlers.get(flag); }
	public NodeFlagHandler getNodeFlagHandler(String flag) { return nodeFlagHandlers.get(flag); }
	
	public ArrayList<Flag> getTreeFlags(String schemaString) { return treeFlags.get(schemaString); }
	public HashMap<String,Flag> getNodeFlags(String schemaString) { return nodeFlags.get(schemaString); }
	
	public NodeState getNodeState(Node localNode, String schemaString) { return new NodeState(localNode,schemaString,this);	}
	
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
	*/
}
