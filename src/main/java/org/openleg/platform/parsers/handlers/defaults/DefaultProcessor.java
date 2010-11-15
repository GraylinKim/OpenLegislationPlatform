package org.openleg.platform.parsers.handlers.defaults;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public void applyNodeStateFlags(NodeState nodeState) {
		
		for(Flag flag : nodeState.treeFlags)
			nodeState.getTreeFlagHandler(flag).processNodeState(nodeState);
		
		for(Flag flag : nodeState.nodeFlags.values())
			nodeState.getNodeFlagHandler(flag).processNodeState(nodeState);
	}
	
	public void applyChildStateFlags(NodeState childState) {
		
		for(Flag flag : childState.treeFlags)
			childState.getTreeFlagHandler(flag).processChildState(childState);
		
		for(Flag flag : childState.nodeFlags.values())
			childState.getNodeFlagHandler(flag).processChildState(childState);
	}
	
	public Node processXml(Node node, NodeState nodeState, Document xml) {
		
		//Use flags to modify the state
		applyNodeStateFlags(nodeState);
		
		Node output = null;
		if(nodeState.writeXml) {
			
			if(XmlUtil.isLeafNode(node)) {
				
					if(nodeState.writeAsAttribute) {
						output = xml.createAttribute(nodeState.writeName);
					} else {
						output = xml.createElement(nodeState.writeName);
					}
					
					output.setNodeValue(nodeState.writeValue);
				
			} else {
				
				output = xml.createElement(nodeState.writeName);
				for( Node child : XmlUtil.getChildElements(node)) {
					NodeState childState = new NodeState(child,nodeState);
					
					//Use flags to modify child state
					applyChildStateFlags(childState);
					
					Node outputChild = childState.nodeProcessor().processXml(child, childState, xml);
					
					//Use flags to modify the child
					
					if(outputChild != null) {
						if(outputChild.getNodeType()==Node.ATTRIBUTE_NODE)
							((Element)output).setAttributeNode((Attr)outputChild);
						else
							output.appendChild(outputChild);
					}
				}
			}
			
			//Use flags to modify the tree
		}
		
		return output;
	}
	
	public void processSolr(Node node, NodeState nodeState, HashMap<String,ArrayList<String>> solr) {
		
		//Use flags to modify the state
		applyNodeStateFlags(nodeState);
		
		if(nodeState.writeSolr) {
			
			solr.put(nodeState.solrPrefix+nodeState.writeName, (ArrayList<String>)Arrays.asList(nodeState.writeValue) );
			
			for(Node child : XmlUtil.getChildElements(node)) {
				NodeState childState = new NodeState(child,nodeState);
				
				//Use flags to modify child state
				applyChildStateFlags(childState);
				
				childState.nodeProcessor().processSolr(child, childState, solr);
			}
			
		}
		
		//Use flags to somehow modify the solr document?
	}
	
}
