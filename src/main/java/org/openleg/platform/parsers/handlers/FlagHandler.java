package org.openleg.platform.parsers.handlers;

import org.openleg.platform.parsers.NodeState;

public interface FlagHandler {
	void processNodeState(NodeState state);
	void processChildState(NodeState state);
}
