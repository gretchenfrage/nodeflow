
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
 * them to be buffered on the receiving end until all previously send packages
 * can be received first.
 * </p>
 * <p>
 * DatagramSockets provide and confirmation and retransmission system, which
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
 * tset if it should accept the connection. This predicate is defaulted to
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
 * 
 */
package com.phoenixkahlo.pnet.socket;