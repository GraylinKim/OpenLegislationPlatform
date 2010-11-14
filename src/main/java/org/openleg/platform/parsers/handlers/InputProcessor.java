package org.openleg.platform.parsers.handlers;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.ParsedDocument;
import org.w3c.dom.Node;

public interface InputProcessor {
	
	public Node processNode(Node inputNode, NodeState state, ParsedDocument doc);
	
}
