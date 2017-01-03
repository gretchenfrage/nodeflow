package com.phoenixkahlo.nodenet.stream;

import com.phoenixkahlo.util.UUID;

/**
 * A DatagramStream that belongs to a StreamFamily. Depends on the StreamFamily's
 * helper threads to receive its messages and delegate them to it through the
 * ChildStream interface.
 */
public interface ChildStream extends DatagramStream {

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
	void receivePayloadConfirmation(UUID payloadID);

	/**
	 * @return the time that a heartbeat was last received.
	 */
	long getLastHeartbeat();

	/**
	 * @return the time that this socket was created, so that it can be known
	 *         when a heartbeat is not to be expected.
	 */
	long getTimeOfCreation();

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
	UUID getConnectionID();

	/**
	 * Retransmit any payloads for which confirmation has remained unreceived
	 * for an unacceptable amount of time.
	 */
	void retransmitUnconfirmed();

}
