package org.openleg.platform.parsers;

import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class NewInputParser {
	
	public class ParserConfiguration {
		
		public class Flag {
			
			public String name;
			public String value;
			
			public Flag(String name,String value) {
				this.name = name;
				this.value = value;
			}
		}
		
		public TreeSet<String> schemas;
		public HashMap<String,InputProcessor> inputProcessors;
		public HashMap<String,TreeFlagHandler> treeFlagHandlers;
		public HashMap<String,NodeFlagHandler> nodeFlagHandlers;
		public HashMap<String,HashMap<String,ArrayList<Flag>>> treeFlags;
		public HashMap<String,HashMap<String,HashMap<String,Flag>>> nodeFlags;
	}
	
	public ArrayList<ParsedDocument> documents;
	
	public NewInputParser(String filename, ParserConfiguration configuration) {
		
		documents = new ArrayList<ParsedDocument>();
		
		//Get the root of the input file and process it
		processFileNode(XmlUtil.getXmlDocument(filename).getDocumentElement(),configuration);
	}
	
	//Recursively search for document nodes to spawn ParsedDocuments from
	public void processFileNode(Node node, ParserConfiguration configuration) {
		
		//Start a document if its marked a document on our schema map or if the input has markings
		if(configuration.schemas.contains(node.getNodeName())) {
			documents.add( new ParsedDocument(node,configuration) );
		}
		
		//Recursively search for document roots in the rest of the document
		for(Node child : XmlUtil.getChildElements(node))
			processFileNode(child,configuration);
	}

}