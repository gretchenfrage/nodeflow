package com.phoenixkahlo.pnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import com.phoenixkahlo.pnet.serialization.AutoSerializer;
import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.util.UnorderedTuple;

/**
 * Whenever a new connection is made between two nodes, they shake each other's
 * hands.
 */
public class Handshake implements AutoSerializer {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(Handshake.class, subSerializer, Handshake::new);
	}
	
	private static long binSeed = 6549849846L;
	private static byte[] bin;
	static {
		bin = new byte[20];
		new Random(binSeed).nextBytes(bin);
	}
	
	private NodeAddress senderAddress;
	private Set<UnorderedTuple<NodeAddress>> knownConnections;
	
	private Handshake() {
	}
	
	public Handshake(NodeAddress senderAddress, Set<UnorderedTuple<NodeAddress>> knownConnections) {
		this.senderAddress = senderAddress;
		this.knownConnections = knownConnections;
	}
	
	public NodeAddress getSenderAddress() {
		return senderAddress;
	}
	
	public Set<UnorderedTuple<NodeAddress>> getKnownConnections() {
		return knownConnections;
	}
	
	@Override
	public void autoSerialize(OutputStream out) throws IOException {
		out.write(bin);
	}
	
	@Override
	public void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException {
		byte[] received = new byte[bin.length];
		in.read(received);
		if (!Arrays.equals(received, bin))
			throw new ProtocolViolationException("Handshake bin failure");
	}

}
