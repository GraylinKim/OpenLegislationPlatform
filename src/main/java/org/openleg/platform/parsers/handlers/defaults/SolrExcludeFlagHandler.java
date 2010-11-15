package org.openleg.platform.parsers.handlers.defaults;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;

public class SolrExcludeFlagHandler implements NodeFlagHandler {

	@Override
	public void processChildState(NodeState state, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processNodeState(NodeState state, String value) {
		if(value.equals("true"))
			state.writeSolr = false;
	}

}
