package org.openleg.platform.parsers;

import org.w3c.dom.Document;

public class ParserConfigurationParser {

	public ParserConfigurationParser(String filename) {
		Document xml = XmlUtil.getXmlDocument(filename);
		
		xml.getElementsByTagName("processor");
		xml.getElementsByTagName("treeFlagHandler");
		xml.getElementsByTagName("nodeFlagHandler");
	}
}
