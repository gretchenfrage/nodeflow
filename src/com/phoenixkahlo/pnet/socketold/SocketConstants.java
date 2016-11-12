package com.phoenixkahlo.pnet.socketold;
@Deprecated
public class SocketConstants {

	private SocketConstants() {
	}

	public static final int MAX_PAYLOAD_SIZE = 300;
	public static final int HEARTBEAT_INTERVAL = 1000;
	public static final int RETRANSMISSION_INTERVAL = 100;
	
	public static final long TRANSMISSION_TYPE_MASK_RANGE = 0xF000000000000000L;
	public static final long CONNECTION_ID_RANGE = ~TRANSMISSION_TYPE_MASK_RANGE;

	/**
	 * A part of an unordered message. First 8 bytes are header, next 8 bytes
	 * are payload ID, next 8 bytes are message ID, next byte is message part
	 * number, next byte is total number of payloads in message, next 2 bytes is
	 * size of payload, all following bytes are payload.
	 */
	public static final long PAYLOAD_MASK = 0x0000000000000000L;
	/**
	 * A part of an ordered message. First 8 bytes are header, next 8 bytes are
	 * payload ID, next 8 bytes are message ID, next 4 bytes are message
	 * ordinal, next byte is message part number, next byte is total number of
	 * payloads in message, next 2 bytes are size of payload, all following
	 * bytes are payload.
	 */
	public static final long ORDERED_PAYLOAD_MASK = 0x1000000000000000L;
	/**
	 * A header-only message for reaching out to start a connection.
	 */
	public static final long START_CONNECTION_MASK = 0x2000000000000000L;
	/**
	 * A header-only message for ending a connection.
	 */
	public static final long DISCONNECT_CONNECTION_MASK = 0x3000000000000000L;
	/**
	 * A header-only message for accepting a proposed connection.
	 */
	public static final long ACCEPT_CONNECTION_MASK = 0x4000000000000000L;
	/**
	 * A header-only message for rejecting a proposed connection.
	 */
	public static final long REJECT_CONNECTION_MASK = 0x5000000000000000L;
	/**
	 * A confirmation that a payload has been received. First 8 bytes are
	 * header, next 8 bytes are payload ID.
	 */
	public static final long CONFIRM_PAYLOAD_MASK = 0x6000000000000000L;
	/**
	 * A heartbeat that must be sent every HEARTBEAT_INTERVAL.
	 */
	public static final long HEARTBEAT_MASK = 0x7000000000000000L;

}
