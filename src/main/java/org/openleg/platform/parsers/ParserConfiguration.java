package org.openleg.platform.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserConfiguration {
	
	//A simple pair data structure for flags
	public class Flag {
		
		public String name;
		public String value;
		
		public Flag(String name,String value) {
			this.name = name;
			this.value = value;
		}
	}
	
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
	
	public static void main(String[] args) {
		ParserConfiguration config = new ParserConfiguration(
				"src/main/resources/input/config.xml",
				"src/main/resources/input/documents/S66023-2009.xml"
			);
		
		System.out.println("Processors:");
		for(String key : config.inputProcessors.keySet()){
			System.out.println("\t"+key+":"+config.inputProcessors.get(key).getClass().getName());
		}
		System.out.println("TreeFlagHandlers:");
		for(String key : config.treeFlagHandlers.keySet()){
			System.out.println("\t"+key+":"+config.treeFlagHandlers.get(key).getClass().getName());
		}
		System.out.println("NodeFlagHandlers:");
		for(String key : config.nodeFlagHandlers.keySet()){
			System.out.println("\t"+key+":"+config.nodeFlagHandlers.get(key).getClass().getName());
		}
		
		for(String k1:config.nodeFlags.keySet()) {
			System.out.println(k1+" schema");
			for(String k2:config.nodeFlags.get(k1).keySet()) {
				System.out.println("\t" + k2);
				for(String k3:config.nodeFlags.get(k1).get(k2).keySet()) {
					System.out.println("\t\t" + config.nodeFlags.get(k1).get(k2).get(k3).name + ": " + 
							config.nodeFlags.get(k1).get(k2).get(k3).value);
				}
			}
		}
	}
	public ParserConfiguration(String configFileName, String schemaFileName) {
		this.schemas = new TreeSet<String>();
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
				String className = XmlUtil.getChildValue(processor,"class");
				System.out.println("Loading: "+className);
				Class<?> processorClass = Class.forName(className);
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
			Element flagHandler = (Element)treeFlagHandlers.item(i); 
			
			try {
				String className = XmlUtil.getChildValue(flagHandler,"class");
				System.out.println("Loading: "+className);
				Class<?> flagHandlerClass = Class.forName(className);
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
			Element flagHandler = (Element)nodeFlagHandlers.item(i); 
			
			try {
				String className = XmlUtil.getChildValue(flagHandler,"class");
				System.out.println("Loading: "+className);
				Class<?> flagHandlerClass = Class.forName(className);
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
			buildSchema(root, "", flagMap);
			this.nodeFlags.put(root.getNodeName(), flagMap);
			this.schemas.add(root.getNodeName());
		}
		
		//recurse through the subtree
		for(Node child : XmlUtil.getChildElements(root)) {
			buildSchemas(child);
		}
	}

	public void buildSchema(Node root, String path, HashMap<String,HashMap<String,Flag>> flagMap) {
		
		//Advance the path name
		path += ((path.isEmpty())  ? "" : "." )+root.getNodeName();
		
		
		//The current node's tree map
		HashMap<String,Flag> flags = new HashMap<String, Flag>();
		
		//Look for flags with registered handlers!
		String value;
		for(String treeFlagName : this.treeFlagHandlers.keySet()) {
			if((value = XmlUtil.attributeValue(root, treeFlagName)) != null)
				flags.put(treeFlagName, new Flag(treeFlagName, value));	
		}
		for(String nodeFlagName : this.nodeFlagHandlers.keySet()) {
			if((value = XmlUtil.attributeValue(root, nodeFlagName)) != null)
				flags.put(nodeFlagName, new Flag(nodeFlagName, value));
		}

		//store our results
		flagMap.put(path, flags);
		
		//recurse through the subtree
		for(Node child : XmlUtil.getChildElements(root)) {
			buildSchema(child, path, flagMap);
		}
	}

}
