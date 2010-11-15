package org.openleg.platform.parsers.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import org.openleg.platform.parsers.NodeState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface InputProcessor {
	
	public void processSolr(Node source, NodeState state, HashMap<String,ArrayList<String>> solr);
	public Node processXml(Node inputNode, NodeState state, Document xml);
	
}
