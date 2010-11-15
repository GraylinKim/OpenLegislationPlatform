package org.openleg.platform.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

public class SchemaParser extends XmlUtil {
	
	public static void main(String[] args) {
		SchemaParser sp = new SchemaParser("src/main/resources/input/documents/S66023-2009.xml");
		
		ParserConfiguration parserConfig = sp.parserConfig;
				
		for(String k1:parserConfig.nodeFlags.keySet()) {
			System.out.println("" + k1);
			for(String k2:parserConfig.nodeFlags.get(k1).keySet()) {
				System.out.println("\t" + k2);
				for(String k3:parserConfig.nodeFlags.get(k1).get(k2).keySet()) {
					System.out.println("\t\t" + parserConfig.nodeFlags.get(k1).get(k2).get(k3).name + ": " + 
							parserConfig.nodeFlags.get(k1).get(k2).get(k3).value);
				}
			}
		}
	}
	
	static final String SOLR_EXCLUDE = "solr_exclude";
	static final String XML_EXCLUDE = "xml_exclude";
	static final String SOLR_CONTAINER = "solr_container";
	static final String SOLR_VALUE = "solr_value";
	static final String PROCESSOR = "processor";
	
	ParserConfiguration parserConfig;

	public SchemaParser(String fileName) {
		super();
		
		parserConfig = new ParserConfiguration();
				
		getDocuments(getXmlDocument(fileName).getDocumentElement());
	}
	
	
	
	public void buildSchema(Node root, String path, HashMap<String,HashMap<String,Flag>> flagMap) {
		if(path == null){
			path = root.getNodeName();
		}
		else 
			path += "." + root.getNodeName();
		
		HashMap<String,Flag> flags = new HashMap<String, Flag>();
		
		String value = null;
		
		if((value = attributeValue(root, SOLR_EXCLUDE)) != null)
			flags.put(SOLR_EXCLUDE, new Flag(SOLR_EXCLUDE, value));
		
		if((value = attributeValue(root, XML_EXCLUDE)) != null)
			flags.put(XML_EXCLUDE, new Flag(XML_EXCLUDE, value));
		
		if((value = attributeValue(root, SOLR_CONTAINER)) != null)
			flags.put(SOLR_CONTAINER, new Flag(SOLR_CONTAINER, value));
		
		if((value = attributeValue(root, SOLR_VALUE)) != null)
			flags.put(SOLR_VALUE, new Flag(SOLR_VALUE, value));
		
		if((value = attributeValue(root, PROCESSOR)) != null)
			flags.put(PROCESSOR, new Flag(PROCESSOR, value));
		
		
		if(!flags.isEmpty())
			flagMap.put(path, flags);
		
		//look for a correctly marked node
		for(Node child : XmlUtil.getChildElements(root)) {
			buildSchema(child, path, flagMap);
		}
	}
	
	public void getDocuments(Node root) {
		if(isFlagSet(root, "document")) {
			HashMap<String,HashMap<String,Flag>> flagMap = new HashMap<String,HashMap<String,Flag>>();
			buildSchema(root, null, flagMap);
			
			parserConfig.nodeFlags.put(root.getNodeName(), flagMap);
		}
		
		for(Node child : XmlUtil.getChildElements(root)) {
			getDocuments(child);
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
	
	public class ParserConfiguration {		
		public HashMap<String,HashMap<String,ArrayList<Flag>>> treeFlags;
		public HashMap<String,HashMap<String,HashMap<String,Flag>>> nodeFlags;
		
		public ParserConfiguration() {
			treeFlags = new HashMap<String,HashMap<String,ArrayList<Flag>>>();
			nodeFlags = new HashMap<String,HashMap<String,HashMap<String,Flag>>>();
		}
	}
	
}
