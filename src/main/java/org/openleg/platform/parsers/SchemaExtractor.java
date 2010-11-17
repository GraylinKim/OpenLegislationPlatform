package org.openleg.platform.parsers;

import java.io.File;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchemaExtractor {

	public HashMap<String,Document> schemas;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String document = "src/main/resources/input/documents/bills";
		SchemaExtractor schema = new SchemaExtractor(document);
		for(Document doc : schema.schemas.values()) {
			XmlUtil.printDocument(doc);
		}
	}
	
	public SchemaExtractor(String filename) {
		schemas = new HashMap<String,Document>();
		buildFile(new File(filename));
	}
	
	public void buildFile(File file) {
		System.out.println("Processing: "+file.getAbsolutePath());
		if(file.exists()) {
			if(file.canRead()) {
				if(file.isDirectory()) {
					for(File child : file.listFiles())
						buildFile(child);
				
				} else if (file.isFile()) {
					
					try {
						Document input = XmlUtil.getXmlDocument(file);
						Element inputRoot = input.getDocumentElement();
						
						Document schema = schemas.get(inputRoot.getNodeName());
						Element schemaRoot;
						if(schema == null) {
							schema = XmlUtil.newXmlDocument();
							schemas.put(inputRoot.getNodeName(), schema);
							schemaRoot = schema.createElement(inputRoot.getNodeName());
							buildSchema(inputRoot,schema,schemaRoot);
							schema.appendChild(schemaRoot);
						} else {
							schemaRoot = schema.getDocumentElement();
							buildSchema(inputRoot,schema,schemaRoot);
						}
					} catch (Exception e) {
						return;
					}
					
				} else {
					//What the hell is it then?
				}
			} else {
				System.out.println("Permissions Error: can't read the file");
			}
		} else {
			System.out.println("Argument Error: Specified file does not exist");
		}
	}
	
	public void buildSchema(Node inputNode, Document schema, Element schemaNode ) {
		NodeList inputChildren = inputNode.getChildNodes();
		for(int i=0; i<inputChildren.getLength(); i++) {
			Node inputChild = inputChildren.item(i);
			if(inputChild.getNodeType()!=Node.TEXT_NODE) {
				Element schemaChild;
				NodeList schemaChildren = schemaNode.getElementsByTagName(inputChild.getNodeName());
				if(schemaChildren.getLength()==0) {
					schemaChild = schema.createElement(inputChild.getNodeName());
					buildSchema(inputChild,schema,schemaChild);
					schemaNode.appendChild(schemaChild);
				}
				else if(schemaChildren.getLength()==1) {
					schemaChild = (Element)schemaChildren.item(0);
					buildSchema(inputChild,schema,schemaChild);
				}
				else
					System.out.println("Schema should only ever find 1 matching child with a given tag name");
			}
		}
	}
	
}
