package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration;
import org.openleg.platform.parsers.NewInputParser.ParserConfiguration.Flag;
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
	
	public InputProcessor getNodeProcessor(String schemaString) {
		
		HashMap<String,Flag> flags = nodeFlags.get(schemaString);
		Flag processorFlag = flags.get("processor");
		
		String processorName;
		if(processorFlag==null)
			processorName="default";
		else
			processorName=processorFlag.value;
		
		//return the indicated processor if it exists
		InputProcessor processor = inputProcessors.get(processorName);
		if(processor == null)
			System.out.println("FATAL ERROR: No default processor defined");
		
		return processor;
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
}
