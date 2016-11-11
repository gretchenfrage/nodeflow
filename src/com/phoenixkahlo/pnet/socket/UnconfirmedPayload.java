package com.phoenixkahlo.pnet.socket;

public class UnconfirmedPayload {

	private long payloadID;
	private long sentTime;
	private byte[] transmission;

	public UnconfirmedPayload(long payloadID, long sentTime, byte[] transmission) {
		this.payloadID = payloadID;
		this.sentTime = sentTime;
		this.transmission = transmission;
	}

	public UnconfirmedPayload(long payloadID, byte[] transmission) {
		this(payloadID, System.currentTimeMillis(), transmission);
	}
	
	public long getPayloadID() {
		return payloadID;
	}

	public long getSentTime() {
		return sentTime;
	}
	
	public byte[] getTransmission() {
		return transmission;
	}

}
