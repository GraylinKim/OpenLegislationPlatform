package org.openleg.platform.parsers.handlers.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.ParsedDocument;
import org.openleg.platform.parsers.XmlUtil;
import org.openleg.platform.parsers.ParserConfiguration.Flag;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("unused")
public class DefaultProcessor implements InputProcessor {
	
	public void applyNodeStateFlags(NodeState nodeState) {
		
		for(Flag flag : nodeState.treeFlags) {
			TreeFlagHandler handler = nodeState.getTreeFlagHandler(flag);
			if(handler!=null)
				handler.processNodeState(nodeState, flag.value);
		}
		
		for(Flag flag : nodeState.nodeFlags.values()) {
			NodeFlagHandler handler = nodeState.getNodeFlagHandler(flag);
			if(handler!=null)
				handler.processNodeState(nodeState, flag.value);
		}
	}
	
	public void applyChildStateFlags(NodeState nodeState, NodeState childState) {
		
		for(Flag flag : nodeState.treeFlags) {
			TreeFlagHandler handler = nodeState.getTreeFlagHandler(flag);
			if(handler!=null)
				handler.processChildState(childState, flag.value);
		}
		
		for(Flag flag : nodeState.nodeFlags.values()) {
			NodeFlagHandler handler = nodeState.getNodeFlagHandler(flag);
			if(handler!=null)
				nodeState.getNodeFlagHandler(flag).processChildState(childState, flag.value);
		}
	}
	
	public Node processXml(Node node, NodeState nodeState, Document xml) {
		
		//Use flags to modify the state
		applyNodeStateFlags(nodeState);
		
		if(node.getNodeName().equals("votes")) {
			String breakPoint = "here";
		}
		Node output = null;
		//System.out.println(nodeState.writeName+" has writeXML as:"+nodeState.writeXml);
		if(nodeState.writeXml) {
			
			if(XmlUtil.isLeafNode(node)) {
				
					if(nodeState.writeAsAttribute) {
						output = xml.createAttribute(nodeState.writeName);
						output.setNodeValue(nodeState.writeValue);
					} else {
						output = xml.createElement(nodeState.writeName);
						output.appendChild(xml.createTextNode(nodeState.writeValue));
					}
				
			} else {
				
				output = xml.createElement(nodeState.writeName);
				System.out.println("Created element: "+nodeState.writeName);
				for( Node child : XmlUtil.getChildElements(node)) {
					NodeState childState = new NodeState(child,nodeState);
					
					//Use flags to modify child state
					applyChildStateFlags(nodeState,childState);
					
					Node outputChild = childState.nodeProcessor().processXml(child, childState, xml);
					
					
					//Use flags to modify the child
					
					if(outputChild != null) {
						if(outputChild.getNodeType()==Node.ATTRIBUTE_NODE)
							((Element)output).setAttributeNode((Attr)outputChild);
						else
							output.appendChild(outputChild);
					} else {
						System.out.println("\tWhich has a null child");
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
		
		if(nodeState.solrExclude == false) {
			
			if(nodeState.writeSolr)
				solr.put(nodeState.solrPrefix+"."+nodeState.writeName, new ArrayList<String>(Arrays.asList(nodeState.writeValue)) );
			
			for(Node child : XmlUtil.getChildElements(node)) {
				NodeState childState = new NodeState(child,nodeState);
				
				//Use flags to modify child state
				applyChildStateFlags(nodeState,childState);
				
				childState.nodeProcessor().processSolr(child, childState, solr);
			}
			
		}
		
		//Use flags to somehow modify the solr document?
	}
	
}
