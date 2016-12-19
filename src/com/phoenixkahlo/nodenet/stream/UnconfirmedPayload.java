package com.phoenixkahlo.nodenet.stream;

/**
 * A bean for payloads that have been sent but not confirmed.
 */
public class UnconfirmedPayload {

	private int payloadID;
	private byte[] transmission;
	private long lastSentTime;

	public UnconfirmedPayload(int payloadID, byte[] transmission, long lastSentTime) {
		this.payloadID = payloadID;
		this.transmission = transmission;
		this.lastSentTime = lastSentTime;
	}

	public UnconfirmedPayload(int payloadID, byte[] transmission) {
		this(payloadID, transmission, System.currentTimeMillis());
	}
	
	public int getPayloadID() {
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
