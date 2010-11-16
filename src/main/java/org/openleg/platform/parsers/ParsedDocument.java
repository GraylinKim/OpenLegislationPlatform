package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.solr.common.SolrInputDocument;
import org.openleg.platform.parsers.ParserConfiguration;
import org.openleg.platform.parsers.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ParsedDocument {
	
	public Document xml;
	public HashMap<String,ArrayList<String>> solr;
	
	protected HashMap<String,ArrayList<Flag>> treeFlags;
	protected HashMap<String,HashMap<String,Flag>> nodeFlags;
	protected HashMap<String,InputProcessor> inputProcessors;
	protected HashMap<String,TreeFlagHandler> treeFlagHandlers;
	protected HashMap<String,NodeFlagHandler> nodeFlagHandlers;
	
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

		//Get the initial state and processor
		NodeState initialState = new NodeState(root,this);
		InputProcessor processor = initialState.nodeProcessor();
		
		//Build the XML and SOLR documents
		processor.processSolr(root,initialState,solr);
		xml.appendChild(processor.processXml(root, initialState, xml));
		solr.put("xml", new ArrayList<String>(Arrays.asList(XmlUtil.toString(xml))));
		
		/*
		System.out.println(xml.getDocumentElement().getNodeName()+" Document Fields:");
		System.out.println("========================");
		for(String key : solr.keySet()) {
			if(!key.equals("xml"))
				System.out.println(key+": "+solr.get(key));
		}
		System.out.println("");
		
		System.out.println(xml.getDocumentElement().getNodeName()+" Document XML:");
		System.out.println("========================");
		System.out.println(solr.get("xml").get(0));
		System.out.println("\n\n");
		*/
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
	
	public SolrInputDocument toSolrDocument() {
		SolrInputDocument solrDoc = new SolrInputDocument();
		for( String key : solr.keySet()) {
			for( String value : solr.get(key)) {
				solrDoc.addField(key, value);
			}
		}
		return solrDoc;
	}
}
