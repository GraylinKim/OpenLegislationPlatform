package org.openleg.platform.parsers.handlers;

import org.openleg.platform.parsers.ParsedDocument;
import org.w3c.dom.Node;

public interface InputProcessor {
	
	public void processNode(Node inputNode, String nodeString, Node outputNode, ParsedDocument doc);
	
}
