package com.phoenixkahlo.nodenet.stream;

/**
 * Constants for the protocol of PNetSockets.
 */
public class DatagramStreamConfig {

	private DatagramStreamConfig() {}

	public static final int MAX_PAYLOAD_SIZE = 300;
	public static final int HEARTBEAT_INTERVAL = 1000;
	public static final int RETRANSMISSION_THRESHHOLD = 500;
	
	public static final int TRANSMISSION_TYPE_RANGE = 0xF0000000;
	public static final int CONNECTION_ID_RANGE = ~TRANSMISSION_TYPE_RANGE;

	/**
	 * A part of an unordered message.
	 * - int header
	 * - int payloadID
	 * - int messageID
	 * - byte partNumber
	 * - byte totalParts
	 * - short payloadSize
	 * - byte[] payload
	 */
	public static final int PAYLOAD = 0x00000000;
	/**
	 * A part of an ordered message.
	 * - int header
	 * - int payloadID
	 * - int messageID
	 * - int ordinal
	 * - byte partNumber
	 * - byte totalParts
	 * - short payloadSize
	 * - byte[] payload
	 */
	public static final int ORDERED_PAYLOAD = 0x10000000;
	/**
	 * Header only transmission for trying to start a connection.
	 */
	public static final int CONNECT = 0x20000000;
	/**
	 * Header only transmission for ending a connection;
	 */
	public static final int DISCONNECT = 0x30000000;
	/**
	 * Header only transmission for accepting a connection in response to CONNECT.
	 */
	public static final int ACCEPT = 0x40000000;
	/**
	 * Header only transmission for rejecting a connection in response to CONNECT.
	 */
	public static final int REJECT = 0x50000000;
	/**
	 * Confirmation that a payload has been received.
	 * - int header
	 * - int payloadID
	 */
	public static final int CONFIRM = 0x60000000;
	/**
	 * Header only heartbeat transmission.
	 */
	public static final int HEARTBEAT = 0x70000000;
	
	public static String nameOf(int transmissionType) {
		switch (transmissionType) {
		case PAYLOAD:
			return "payload";
		case ORDERED_PAYLOAD:
			return "ordered payload";
		case CONNECT:
			return "connect";
		case DISCONNECT:
			return "disconnect";
		case ACCEPT:
			return "accept";
		case REJECT:
			return "reject";
		case CONFIRM:
			return "confirm";
		case HEARTBEAT:
			return "heartbeat";
		default:
			return "invalid (" + Integer.toBinaryString(transmissionType >> 28) + ")";
		}
	}

}
