package org.openleg.platform.parsers;

import org.openleg.platform.parsers.ParserConfiguration.Flag;
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
public class InputParser {
	
	public static void main(String[] args) {
		ParserConfiguration config = new ParserConfiguration(
				"src/main/resources/input/config.xml",
				"src/main/resources/input/documents/a1800.schm"
			);
		
		System.out.println("Lets get this started!");
		new InputParser("src/main/resources/input/documents/a1800.xml",config);
	}
	
	public ArrayList<ParsedDocument> documents;
	
	public InputParser(String filename, ParserConfiguration configuration) {
		
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