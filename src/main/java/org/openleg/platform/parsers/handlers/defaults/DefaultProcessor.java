package org.openleg.platform.parsers.handlers.defaults;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.ParsedDocument;
import org.openleg.platform.parsers.XmlUtil;
import org.openleg.platform.parsers.handlers.InputProcessor;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;
import org.openleg.platform.parsers.handlers.TreeFlagHandler;
import org.w3c.dom.Node;

@SuppressWarnings("unused")
class DefaultProcessor implements InputProcessor {
	
	public void processNode(Node inputNode, String nodeString, Node outputNode, ParsedDocument doc) {
		
		String value;
		HashMap<String,String> flags;
		
		String nodeValue;
		String nodeName;
		boolean writeSolr;
		boolean writeXml;
		
		/**
		 * This be Tricky Business in here.
		 * Need to make this part simple and abstracted so others can
		 * implement processors without too much of a hassel
		 */
		//Add our name to the nodeString
		if(!nodeString.isEmpty())
			nodeString += ".";
		nodeString += inputNode.getNodeName();
		
		//Execute Effects from Parent Tree Flags
		flags = doc.getParentTreeFlags(nodeString);
		for(String flag : flags.keySet()) {
			value = flags.get(flag);
			TreeFlagHandler processor = doc.getTreeFlagHandler(flag);
		}
		
		//Execute Effects from Local Tree Flags
		flags = doc.getNodeTreeFlags(nodeString);
		for(String flag : flags.keySet()) {
			value = flags.get(flag);
			TreeFlagHandler processor = doc.getTreeFlagHandler(flag);
			
		}
		
		//Execute Effects from Local Node Flags
		flags = doc.getNodeNodeFlags(nodeString);
		for(String flag : flags.keySet()) {
			value = flags.get(flag);
			NodeFlagHandler processor = doc.getNodeFlagHandler(flag);
		}
		/**
		 * Tricky Business ends around here
		 */
		
		//Handle all the child nodes recursively, look for new processors at every node
		for(Node inputChild : XmlUtil.getChildElements(inputNode)) {
			Node newChild = doc.createElement(inputNode.getNodeName());
			doc.getNodeProcessor(inputChild).processNode(inputChild,nodeString,newChild,doc);
			outputNode.appendChild(newChild);
		}
	}
}
