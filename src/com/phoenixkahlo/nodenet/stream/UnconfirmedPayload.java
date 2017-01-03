package com.phoenixkahlo.nodenet.stream;

import com.phoenixkahlo.util.UUID;

/**
 * A bean for payloads that have been sent but not confirmed.
 */
public class UnconfirmedPayload {

	private UUID payloadID;
	private byte[] transmission;
	private long lastSentTime;

	public UnconfirmedPayload(UUID payloadID, byte[] transmission, long lastSentTime) {
		this.payloadID = payloadID;
		this.transmission = transmission;
		this.lastSentTime = lastSentTime;
	}

	public UnconfirmedPayload(UUID payloadID, byte[] transmission) {
		this(payloadID, transmission, System.currentTimeMillis());
	}
	
	public UUID getPayloadID() {
		return payloadID;
	}

	public long getLastSentTime() {
		return lastSentTime;
	}
	
	public byte[] getTransmission() {
		return transmission;
	}
	
	public void setLastSendTime(long time) {
		this.lastSentTime = time;
	}

}
