package org.openleg.platform.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import org.openleg.platform.parsers.NewInputParser.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.openleg.platform.parsers.handlers.defaults.DefaultProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserConfiguration {
	
	//Configuration Data Structures
	public TreeSet<String> schemas;
	public HashMap<String,InputProcessor> inputProcessors;
	public HashMap<String,TreeFlagHandler> treeFlagHandlers;
	public HashMap<String,NodeFlagHandler> nodeFlagHandlers;
	public HashMap<String,HashMap<String,ArrayList<Flag>>> treeFlags;
	public HashMap<String,HashMap<String,HashMap<String,Flag>>> nodeFlags;
	
	// String constants
	static final String SOLR_EXCLUDE = "solr_exclude";
	static final String XML_EXCLUDE = "xml_exclude";
	static final String SOLR_CONTAINER = "solr_container";
	static final String SOLR_VALUE = "solr_value";
	static final String PROCESSOR = "processor";
	
	public ParserConfiguration(String schemaFileName, String configFileName) {
		this.inputProcessors = new HashMap<String,InputProcessor>();
		this.treeFlagHandlers = new HashMap<String,TreeFlagHandler>();
		this.nodeFlagHandlers = new HashMap<String,NodeFlagHandler>();
		this.treeFlags = new HashMap<String,HashMap<String,ArrayList<Flag>>>();
		this.nodeFlags = new HashMap<String,HashMap<String,HashMap<String,Flag>>>();
		
		processConfiguration(new File(configFileName));
		processSchemas(new File(schemaFileName));
	}
	
	public void processConfiguration(File configFile) {

		//Handle files directly in this directory if it is a directory
		if(configFile.isDirectory()) {
			for( File file : configFile.listFiles()) {
				if(!file.isDirectory())
					buildConfiguration(XmlUtil.getXmlDocument(configFile).getDocumentElement());
			}
			
		//Just handle the file if its not a directory
		} else {
			buildConfiguration(XmlUtil.getXmlDocument(configFile).getDocumentElement());
		}
	}
	
	public void buildConfiguration(Element root) {
		
		InputProcessor inputProcessor;
		NodeList processors = root.getElementsByTagName("processor");
		for(int i=0; i < processors.getLength(); i++ ) {
			Element processor = (Element)processors.item(i); 
			
			try {
				Class<?> processorClass = Class.forName(XmlUtil.getChildValue(processor,"class"));
				inputProcessor = InputProcessor.class.cast(processorClass.newInstance());
				this.inputProcessors.put(XmlUtil.getChildValue(processor,"name"),inputProcessor);
			} catch (ClassNotFoundException e) {
				//Class name was invalid
			} catch (IllegalAccessException e) {
				//The constructor was not public...
			} catch (InstantiationException e) {
				//The newInstance has failed!
			} catch (ClassCastException e) {
				//Invalid Class type for processor
			}
			
		}
		
		TreeFlagHandler treeFlagHandler;
		NodeList treeFlagHandlers = root.getElementsByTagName("treeFlagHandler");
		for(int i=0; i < treeFlagHandlers.getLength(); i++ ) {
			Element flagHandler = (Element)processors.item(i); 
			
			try {
				Class<?> flagHandlerClass = Class.forName(XmlUtil.getChildValue(flagHandler,"class"));
				treeFlagHandler = TreeFlagHandler.class.cast(flagHandlerClass.newInstance());
				this.treeFlagHandlers.put(XmlUtil.getChildValue(flagHandler,"name"),treeFlagHandler);
			} catch (ClassNotFoundException e) {
				//Class name was invalid
			} catch (IllegalAccessException e) {
				//The constructor was not public...
			} catch (InstantiationException e) {
				//The newInstance has failed!
			} catch (ClassCastException e) {
				//Invalid Class type for processor
			}
						
		}
		
		NodeFlagHandler nodeFlagHandler;
		NodeList nodeFlagHandlers = root.getElementsByTagName("nodeFlagHandler");
		for(int i=0; i < nodeFlagHandlers.getLength(); i++ ) {
			Element flagHandler = (Element)processors.item(i); 
			
			try {
				Class<?> flagHandlerClass = Class.forName(XmlUtil.getChildValue(flagHandler,"class"));
				nodeFlagHandler = NodeFlagHandler.class.cast(flagHandlerClass.newInstance());
				this.nodeFlagHandlers.put(XmlUtil.getChildValue(flagHandler,"name"),nodeFlagHandler);
			} catch (ClassNotFoundException e) {
				//Class name was invalid
			} catch (IllegalAccessException e) {
				//The constructor was not public...
			} catch (InstantiationException e) {
				//The newInstance has failed!
			} catch (ClassCastException e) {
				//Invalid Class type for processor
			}
				
		}
	}
	
	public void processSchemas(File schemaFile) {
		
		//Handle files directly in this directory if it is a directory
		if(schemaFile.isDirectory()) {
			for( File file : schemaFile.listFiles()) {
				if(!file.isDirectory())
					buildSchemas(XmlUtil.getXmlDocument(file).getDocumentElement());
			}
			
		//Just handle the file if its not a directory
		} else {
			buildSchemas(XmlUtil.getXmlDocument(schemaFile).getDocumentElement());
		}
	}
	
	public void buildSchemas(Node root) {
		if(XmlUtil.isFlagSet(root, "document")) {
			HashMap<String,HashMap<String,Flag>> flagMap = new HashMap<String,HashMap<String,Flag>>();
			buildSchema(root, null, flagMap);
			
			this.nodeFlags.put(root.getNodeName(), flagMap);
		}
		
		for(Node child : XmlUtil.getChildElements(root)) {
			buildSchemas(child);
		}
	}

	public void buildSchema(Node root, String path, HashMap<String,HashMap<String,Flag>> flagMap) {
		
		//Advance the path name
		if(path == null)
			path = root.getNodeName();
		else 
			path += "." + root.getNodeName();
		
		
		String value;
		HashMap<String,Flag> flags = new HashMap<String, Flag>();
		
		if((value = XmlUtil.attributeValue(root, SOLR_EXCLUDE)) != null)
			flags.put(SOLR_EXCLUDE, new Flag(SOLR_EXCLUDE, value));
		
		if((value = XmlUtil.attributeValue(root, XML_EXCLUDE)) != null)
			flags.put(XML_EXCLUDE, new Flag(XML_EXCLUDE, value));
		
		if((value = XmlUtil.attributeValue(root, SOLR_CONTAINER)) != null)
			flags.put(SOLR_CONTAINER, new Flag(SOLR_CONTAINER, value));
		
		if((value = XmlUtil.attributeValue(root, SOLR_VALUE)) != null)
			flags.put(SOLR_VALUE, new Flag(SOLR_VALUE, value));
		
		if((value = XmlUtil.attributeValue(root, PROCESSOR)) != null)
			flags.put(PROCESSOR, new Flag(PROCESSOR, value));
		
		flagMap.put(path, flags);
		
		//look for a correctly marked node
		for(Node child : XmlUtil.getChildElements(root)) {
			buildSchema(child, path, flagMap);
		}
	}

	public class Flag {
		
		public String name;
		public String value;
		
		public Flag(String name,String value) {
			this.name = name;
			this.value = value;
		}
	}
}
