package org.openleg.platform.parsers.handlers.defaults;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.ParsedDocument;
import org.openleg.platform.parsers.XmlUtil;
import org.openleg.platform.parsers.NewInputParser.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("unused")
class DefaultProcessor implements InputProcessor {
	
	public Node processNode(Node inputNode, NodeState state, ParsedDocument doc) {
		
		//Evaluate the treeFlags first, in order
		for(Flag flag : state.treeFlags) {
			state = doc.getTreeFlagHandler(flag.name).process(state);
		}
		
		//Then apply the node effects
		for(Flag flag : state.nodeFlags.values()) {
			state = doc.getTreeFlagHandler(flag.name).process(state);
		}
				
		//Write the node according to the state
		if(state.writeSolr) {
			doc.addSolrField(state.solrPrefix, state.writeName, state.writeValue);
		}
		
		Node outputNode = null;
		
		//Leaf nodes are the end of the line, no more recursion
		if(XmlUtil.isLeafNode(inputNode)) {
			
			if(state.writeXml) {
				if(state.writeAsAttribute)
					outputNode = doc.xml.createAttribute(state.writeName);
				else
					outputNode = doc.xml.createElement(state.writeName);
				
				outputNode.setNodeValue(state.writeValue);
			}
			
		//Inner nodes should recurse through the rest of the document
		} else {

			//Handle all the child nodes recursively, look for new processors at every node
			outputNode = doc.xml.createElement(state.writeName);
			for(Node inputChild : XmlUtil.getChildElements(inputNode)) {
				NodeState childState = new NodeState(inputChild,state);
				Node childNode = doc.getNodeProcessor(state.schemaString).processNode(inputChild,childState,doc);
				
				//If the child wrote to XML
				if( childNode != null) {
					
					//Attach the child node as either an attribute or a child
					if(childNode.getNodeType()==Node.ATTRIBUTE_NODE)
						((Element)outputNode).setAttributeNode((Attr)childNode);
					else
						outputNode.appendChild(childNode);
					
				}
			}
			
		}
		
		//If the XML should be written, return it, else null
		if(state.writeXml)
			return outputNode;
		else
			return null;
	}
}
