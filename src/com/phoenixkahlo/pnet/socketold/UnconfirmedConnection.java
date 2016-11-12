package com.phoenixkahlo.pnet.socketold;

/**
 * Represents a connection that has been attempted to be formed but has not yet
 * received confirmation.
 */
@Deprecated
public class UnconfirmedConnection {

	private long connectionID;

	public UnconfirmedConnection(long connectionID) {
		this.connectionID = connectionID;
	}

	public long getConnectionID() {
		return connectionID;
	}

}
