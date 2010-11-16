package org.openleg.platform.parsers;

import org.apache.solr.common.SolrInputDocument;
import org.openleg.platform.parsers.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.openleg.platform.parsers.handlers.defaults.DefaultProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
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
		System.out.println("Successfully loaded configuration");
		InputParser input = new InputParser(new File("src/main/resources/input/documents/senatedocs/"),config);
		
		ArrayList<SolrInputDocument> solrDocs = input.getSolrDocuments();
		System.out.println(solrDocs.size()+" Documents Indexed");
	}
	
	public ArrayList<ParsedDocument> documents;
	
	public InputParser(File file, ParserConfiguration configuration) {
		TreeSet<String> badFiles = new TreeSet<String>(Arrays.asList("A1562.xml,A1267.xml,A1544B.xml,A1137.xml,A10521.xml,A1267A.xml,A1005C.xml,A1520A.xml,A1106.xml,A1697A.xml,A1726B.xml,A1648.xml,A1557A.xml,A1595A.xml".split(",")));
		documents = new ArrayList<ParsedDocument>();
		
		HashMap<String,Long> processingTimes = new HashMap<String,Long>();
		HashMap<String,Exception> BadFiles = new HashMap<String,Exception>();
		
		if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				if(child.isDirectory())
					continue;
				if(badFiles.contains(child.getName()))
					continue;
				
				System.out.println("Processing file: "+child.getName());
				//Get the root of the input file and process it
				try {
					long start = System.nanoTime();
					processFileNode(XmlUtil.getXmlDocument(child).getDocumentElement(),configuration,"");
					long end = System.nanoTime();
					processingTimes.put(child.getName(),end-start);
				} catch (Exception e) {
					BadFiles.put(child.getName(), e);
				}
			}
		} else {
			//Get the root of the input file and process it
			processFileNode(XmlUtil.getXmlDocument(file).getDocumentElement(),configuration,"");
		}
		
		long total = 0;
		for(Long time : processingTimes.values())
			total += time;
		System.out.println("Average File Process time: "+((total/processingTimes.size())/1000000.0f)+" milliseconds");
	}
	
	//Recursively search for document nodes to spawn ParsedDocuments from
	public void processFileNode(Node node, ParserConfiguration configuration, String schemaString) {
		schemaString += ((schemaString.isEmpty()) ? "": "." )+node.getNodeName();
		
		//Start a document if its marked a document on our schema map or if the input has markings
		if(configuration.schemas.contains(schemaString)) {
			documents.add( new ParsedDocument(node,configuration) );
		}
		
		//Recursively search for document roots in the rest of the document
		for(Node child : XmlUtil.getChildElements(node))
			processFileNode(child,configuration,schemaString);
	}

	public ArrayList<SolrInputDocument> getSolrDocuments() {
		ArrayList<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();
		for(ParsedDocument doc : documents) {
			solrDocs.add(doc.toSolrDocument());
		}
		return solrDocs;
	}
}