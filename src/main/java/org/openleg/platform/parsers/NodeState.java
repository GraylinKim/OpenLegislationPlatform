package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration.Flag;
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
	public String solrPrefix;
	
	public String schemaString;
	
	public ArrayList<Flag> treeFlags;
	public HashMap<String,Flag> nodeFlags;		
	
	protected ParsedDocument document;
	
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
		solrPrefix = oldState.solrPrefix+"."+oldState.writeName;
		schemaString = oldState.schemaString+"."+node.getNodeName();
		
		generalInit(node);
	}
	
	private void generalInit(Node node) {
		
		//Some general defaults
		writeXml = true;
		writeAsAttribute = false;
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
		treeFlags = document.getTreeFlags(schemaString);
	}
}