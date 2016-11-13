package com.phoenixkahlo.pnet.socket;

import java.net.SocketAddress;

/**
 * A PNetSocket that belongs to a SocketFamily. Depends on the SocketFamily's
 * helper threads to receive its messages and delegate them to it through the
 * ChildSocket interface.
 */
public interface ChildSocket extends PNetSocket {

	/**
	 * Receive a invocation of disconnect() from the other side.
	 */
	void receiveDisconnect();

	/**
	 * Receive a payload from the other side. If this payload completes a
	 * message, that message should become available to receive().
	 */
	void receivePayload(ReceivedPayload payload);

	/**
	 * Receive confirmation that a particular payload has been received.
	 */
	void receivePayloadConfirmation(int payloadID);

	/**
	 * @return the time that a heartbeat was last received.
	 */
	long getLastHeartbeat();

	/**
	 * Update the result of getLastheartbeat() to the current time.
	 */
	void receiveHeartbeat();

	/**
	 * Transmit a heartbeat to the other side.
	 */
	void sendHeartbeat();

	/**
	 * @return the connection ID within the family.
	 */
	int getConnectionID();

	/**
	 * Retransmit any payloads for which confirmation has remained unreceived
	 * for an unacceptable amount of time.
	 */
	void retransmitUnconfirmed();

	/**
	 * @return the address this socket is transmitting to.
	 */
	SocketAddress getAlienAddress();

}
