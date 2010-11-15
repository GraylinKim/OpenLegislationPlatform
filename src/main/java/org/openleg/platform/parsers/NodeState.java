package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Node;

public class NodeState {
	
	//Node State
	public String writeName;
	public String writeValue;
	
	//Xml State
	public boolean writeXml;
	public boolean writeAsAttribute;
	
	//Solr State
	public boolean writeSolr;
	public boolean solrExclude;
	public String solrPrefix;
	
	public String schemaString;
	
	public ArrayList<Flag> treeFlags;
	public HashMap<String,Flag> nodeFlags;		
	
	public ParsedDocument document;
	
	public TreeFlagHandler getTreeFlagHandler(Flag flag) { return document.treeFlagHandlers.get(flag.name); }
	public NodeFlagHandler getNodeFlagHandler(Flag flag) { return document.nodeFlagHandlers.get(flag.name); }
	
	public NodeState(Node node, ParsedDocument doc) {
		
		//Start with fresh values
		solrPrefix = "";
		this.document = doc;
		schemaString = node.getNodeName();
		
		generalInit(node);
	}
	
	public NodeState(Node node, NodeState oldState) {
		
		//Extend the old state
		this.document = oldState.document;
		
		if(oldState.solrPrefix.isEmpty())
			solrPrefix = oldState.writeName;
		else
			solrPrefix = oldState.solrPrefix+"."+oldState.writeName;
		
		schemaString = oldState.schemaString+"."+node.getNodeName();
		
		generalInit(node);
	}
	
	private void generalInit(Node node) {
		
		//Some general defaults
		writeXml = true;
		writeAsAttribute = false;
		solrExclude = false;
		writeName = node.getNodeName();
		
		//Default write flags for inner and leaf nodes
		if(XmlUtil.isLeafNode(node)) {
			
			writeValue = node.getTextContent().trim();
			writeSolr = true;
			
		} else {
			
			writeSolr = false;
			writeValue = null;
		}
		
		//Get the flags from the document schema
		nodeFlags = document.getNodeFlags(schemaString);
		if(nodeFlags == null)
			nodeFlags = new HashMap<String,Flag>();
		
		treeFlags = document.getTreeFlags(schemaString);
		if(treeFlags == null)
			treeFlags = new ArrayList<Flag>();
	}
	
	public InputProcessor nodeProcessor() {
		
		Flag processorFlag = nodeFlags.get("processor");
		
		String processorName;
		if(processorFlag==null)
			processorName="default";
		else
			processorName=processorFlag.value;
		
		//return the indicated processor if it exists
		InputProcessor processor = document.inputProcessors.get(processorName);
		if(processor == null)
			System.out.println("FATAL ERROR: No default processor defined");
		
		return processor;
	}
}