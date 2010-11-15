package org.openleg.platform.parsers.handlers.defaults;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;

public class SolrValueFlagHandler implements NodeFlagHandler {

	@Override
	public void processChildState(NodeState state, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processNodeState(NodeState state, String value) {
		
		state.writeSolr = true;
		state.writeValue = value;

	}

}
