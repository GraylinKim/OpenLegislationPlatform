package org.openleg.platform.parsers;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.openleg.platform.parsers.handlers.defaults.DefaultProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class NewInputParser {
	
	public static class ParserConfiguration {
		
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
	
	public static void main(String[] args) {
		ParserConfiguration config = new ParserConfiguration();
		config.inputProcessors = new HashMap<String,InputProcessor>();
		config.inputProcessors.put("default",new DefaultProcessor());
		config.treeFlagHandlers = new HashMap<String,TreeFlagHandler>();
		config.nodeFlagHandlers = new HashMap<String,NodeFlagHandler>();
		config.treeFlags = new HashMap<String,HashMap<String,ArrayList<Flag>>>();
		config.nodeFlags = new HashMap<String,HashMap<String,HashMap<String,Flag>>>();
		config.schemas = new TreeSet<String>(Arrays.asList("bill","vote","action"));
		
		System.out.println("Lets get this started!");
		new NewInputParser("/home/openleg/OpenLegislationPlatform/src/main/resources/input/documents/S66023-2009.xml",config);
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