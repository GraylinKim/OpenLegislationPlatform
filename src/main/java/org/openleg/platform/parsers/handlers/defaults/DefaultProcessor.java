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
import org.w3c.dom.Node;

@SuppressWarnings("unused")
class DefaultProcessor implements InputProcessor {
	
	public void processNode(Node inputNode, String schemaString, Node outputNode, ParsedDocument doc) {
		/**
		 * This be Tricky Business in here.
		 * Need to make this part simple and abstracted so others can
		 * implement processors without too much of a hassle
		 */
		//Add our name to the nodeString
		schemaString += ((!schemaString.isEmpty()) ? "." : "") +inputNode.getNodeName();
		
		NodeState state = new NodeState(inputNode,schemaString,doc);
		
		ArrayList<Flag> treeFlags = doc.getTreeFlags(schemaString);
		HashMap<String,Flag> nodeFlags = doc.getNodeFlags(schemaString);
		
		//Evaluate the treeFlags first, in order
		for(Flag flag : treeFlags) {
			
		}
		
		//Then apply the node effects
		for(Flag flag : nodeFlags.values()) {
			
		}
		
		//Handle all the child nodes recursively, look for new processors at every node
		for(Node inputChild : XmlUtil.getChildElements(inputNode)) {
			Node newChild = doc.createElement(inputNode.getNodeName());
			doc.getNodeProcessor(schemaString).processNode(inputChild,schemaString,newChild,doc);
			outputNode.appendChild(newChild);
		}
	}
}
