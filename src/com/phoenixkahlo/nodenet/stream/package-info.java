
/**
 * <p>
 * The DatagramStream represents a connection between two programs across a
 * network that can communicate by sending each other arrays of bytes.
 * </p>
 * <p>
 * The DatagramStream is build on top of UDP, allowing for faster transmission
 * of data than TCP.
 * </p>
 * <p>
 * The DatagramSocket will only throw exceptions in the event of a
 * disconnection. Disconnection can be triggered manually from either side, or
 * detected as a result of an underlying network problem. Heartbeats are used to
 * detect disconnections, and will do so even if no data is being transmitted.
 * Disconnections that are not caused intentionally from the local side will
 * always trigger a configurable disconnection handler, so that the client does
 * not have to depend on exceptions when handling disconnections.
 * </p>
 * <p>
 * DatagramStream packets are not guaranteed to arrive in order because of the
 * nature of UDP. However, packets can be sent with ordinals, which will cause
 * them to be buffered on the receiving end until all previously sent packages
 * can be received first.
 * </p>
 * <p>
 * DatagramSockets provide confirmation and retransmission system, which
 * guarantees that all packets will be received, except in the event of a
 * disconnection. In the event of a disconnection, the client can get the
 * collection of packets that have been sent and not confirmed.
 * </p>
 * <p>
 * DatagramStreams are created with a DatagramStreamFamily. The family binds to
 * a particular port, either dynamically or to a port chosen by the client, and
 * can form child connections on that port. A family can reach out to a certain
 * address + port, and attempt to connect to it. If another computer tries to
 * connect to the local family, the family will use a configurable predicate to
 * test if it should accept the connection. This predicate is defaulted to
 * reject all connections. If an incoming connection is accepted, the family
 * will invoke a configurable connection handler.
 * </p>
 * <p>
 * This entire package is designed to be used asynchronously. All methods can be
 * invoked without any explicit synchronization statements, unless documented
 * otherwise. Invocations of client code from this package, such as configurable
 * handlers and predicates, should be expected to be called asynchronously, and
 * thus should make sure to synchronize interactions with client data
 * structures.
 * </p>
 * <h3>The DatagramStream Protocol</h3>
 * <p>
 * DatagramStreams communicate with each other using UDP datagrams.
 * </p>
 * <p>
 * The first 5 bytes in a transmission are a header that is common toe very
 * transmission. The first byte is the message's transmission type, and the next
 * 4 are the connection ID. The transmission type tells the meaning of the rest
 * of the message, and the connection ID is unique to each connection. The
 * connection ID is used to allow for multiple connections on the same port.
 * </p>
 * <p>
 * The first transmission type is PAYLOAD, with an ID of 0x0. It represents a
 * part of an unordered message. Messages can be split into several parts due to
 * the size limit of UDP transmissions. Each <b>payload</b> has a randomly
 * generated payload ID which is used to identify it for the confirmation and
 * retransmission system. Each <b>message</b> also has a randomly generated
 * message ID which is used to combine multiple payloads of the same message.
 * When a payload is received, it should respond with a CONFIRM message. A
 * payload may be sent/received multiple times, as a part of the
 * confirmation/retransmission system. The parts of a payload transmission are:
 * <br>
 * <ol>
 * <li>UUID payloadID</li>
 * <li>UUID messageID</li>
 * <li>int partNumber</li>
 * <li>int totalParts</li>
 * <li>short payloadSize</li>
 * <li>byte[] payload</li>
 * </ol>
 * </p>
 * <p>
 * The next transmission is ORDERED_PAYLOAD, with an ID of 0x1. It is like a
 * normal payload, except it has an ordinal. When it is receives, it will be
 * buffered until messages with all previous ordinals can be received first.
 * Ordered messages sent from a DatagramStream must begin with the ordinal zero,
 * and be incremented by 1 for every message. The parts of an ordered payload
 * transmission are: <br>
 * <ol>
 * <li>UUID payloadID</li>
 * <li>UUID messageID</li>
 * <li>int ordinal</li>
 * <li>int partNumber</li>
 * <li>int totalParts</li>
 * <li>short payloadSize</li>
 * <li>byte[] payload</li>
 * </ol>
 * </p>
 * <p>
 * The next transmission type is CONNECT, with an ID of 0x2. It is a header only
 * transmission for trying to start a connection. The connection ID in this
 * message is the proposed connection ID for the new connection. When received,
 * the other side should respond promptly with either ACCEPT or REJECT.
 * </p>
 * <p>
 * The next transmission type is DISCONNECT, with an ID of 0x3. It is a header
 * only transmission for ending a connection. While a connection could be ended
 * simply by shutting down and allowing the other side to detect the lack of
 * heartbeat, sending a disconnection message allows networks to detect
 * disconnections more quickly and cleanly.
 * </p>
 * <p>
 * The next transmission type is ACCEPT, with an ID of 0x4. It is a header only
 * transmission for accepting a connection in response to CONNECT.
 * </p>
 * <p>
 * The next transmission type is REJECT, with an ID of 0x5. It is a header only
 * transmission for rejecting a connection in response to CONNECT.
 * </p>
 * <p>
 * The next transmission type is CONFIRM, with an ID of 0x6. It is sent in
 * response to any payload to confirm that it has been received. The only
 * contents is the integer payload ID of the payload that was successfully
 * received.
 * </p>
 * <p>
 * The final transmission type if HEARTBEAT, with an ID of 0x7. It is a header
 * only message, sent in each direction every 1000 ms. If a connection goes that
 * interval without receiving a heartbeat, it can consider the connection
 * disconnected. For caution reasons, DatagramStreams are encouraged to send
 * heartbeats at twice the recommended interval, and allow for receiving
 * heartbeats at half the recommended interval.
 * </p>
 */
package com.phoenixkahlo.nodenet.stream;
