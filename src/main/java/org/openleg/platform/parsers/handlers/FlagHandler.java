package org.openleg.platform.parsers.handlers;

import org.openleg.platform.parsers.NodeState;

public interface FlagHandler {
	void processNodeState(NodeState state, String value);
	void processChildState(NodeState state, String value);
}
