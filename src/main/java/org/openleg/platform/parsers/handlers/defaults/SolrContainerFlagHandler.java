package org.openleg.platform.parsers.handlers.defaults;

import org.openleg.platform.parsers.NodeState;
import org.openleg.platform.parsers.handlers.NodeFlagHandler;

public class SolrContainerFlagHandler implements NodeFlagHandler {

	@Override
	public void processChildState(NodeState state, String value) {
		//Pops the parent piece of the prefix off
		System.out.println("Write name: '"+state.writeName+"'");
		System.out.println(state.schemaString);
		System.out.println(state.solrPrefix);
		if(value.equals("true"))
			state.addPrefix = false;
			//state.solrPrefix.substring(0, state.solrPrefix.lastIndexOf('.'));
	}

	@Override
	public void processNodeState(NodeState state, String value) {
		// TODO Auto-generated method stub

	}

}
