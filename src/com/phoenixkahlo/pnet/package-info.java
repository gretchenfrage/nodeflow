
/**
 * <p>
 * A NodeNetwork is a graph-based network, in which each user is considered a
 * node in the graph, and that user's DatagramStream connection to other nodes
 * are considered the edges in the graph. Any group of nodes that are connected
 * to each other, either directly or indirectly, are considered a network.
 * </p>
 * <p>
 * If nodes in two unconnected network form a connection, the networks will be
 * considered one network. If connections within a network are severed such that
 * it is left in two unconnected parts, it will be considered two independent
 * networks. All such changes to the network are considered legal at runtime.
 * </p>
 * <p>
 * While the DatagramStream deals with byte sequences, the NodeNetwork wraps
 * these streams using a serialization service so that nodes can communicate
 * with object messages. This serialization service is configured to allow for
 * the transmission of internal objects, but the client can further configure
 * this service for the transmission of arbitrary types of objects.
 * </p>
 * <p>
 * The primary function of a NodeNetwork is to allow any node to send object to
 * and receive objects from any other node in its network. If these two nodes
 * are not directly connected, they will try to pass the message through nodes
 * in the middle. Transmission should only fail in the event of the network
 * splitting. Upon transmission, the client can configure a listener for if this
 * occurs.
 * </p>
 * <p>
 * Like the underlying DatagramStreams, NodeNetworks are designed to be used
 * asynchronously. All NodeNetwork structures can be invoked without any
 * explicit synchronization unless otherwise documented. Any client code called
 * by a NodeNetwork should be expected to be called asynchronously, and should
 * synchronize interaction with client data structures.
 * </p>
 * <p>
 * The client can configure listeners for nodes being connected to or
 * disconnected (completely) from the network.
 * </p>
 * <p>
 * By default, objects received from other nodes will be added to a queue from
 * which the client can retrieve them. However, if the client would like to
 * handle these messages immediately and asynchronously, it can provide a
 * service to handle these incoming transmissions to replace the enqueueing
 * function.
 * </p>
 * <h3>The NodeNetwork Protocol</h3>
 * <p>
 * All nodes should have a randomly generated NodeAddress. This may be
 * implemented using integers, but should be encapsulated in an object to allow
 * for future changes.
 * </p>
 * <p>
 * There are 4 classes of objects that should be sent through the underlying
 * DatagramStreams:<br>
 * <ul>
 * <li>Handshake</li>
 * <li>ViralMessage</li>
 * <li>AddressedMessage</li>
 * <li>AddressedResult</li>
 * </ul>
 * </p>
 * <p>
 * The first class of object, Handshake, should be sent immediately whenever a
 * new connection is established. The Handshake contains 3 pieces of data:<br>
 * <ul>
 * <li>A chunk of binary data, constant to all programs. If not received
 * correctly - an exception should be thrown on deserialization. This ensures
 * that the other side is a legitimate NodeNetwork implementation, and not some
 * random computer. This constant should be changed between incompatible version
 * of the protocol to prevent bad connections.</li>
 * <li>A list of all node connections known to the sender. All of these should
 * be added to the receiver's set of known connections, so that it can have an
 * accurate model of the graph.</li>
 * <li>The node address of the sender, which is necessary for the receiver to
 * update the network model with the newly formed connection.</li> After a new
 * connection is formed, both sides should virally inform the network of the new
 * connection.
 * </p>
 * <p>
 * ViralMessages carry a payload, and are designed to get that payload to every
 * node in the network, without requiring any node to be aware of the state of
 * the network beyond its immediate neighbors. The purpose of ViralMessages is
 * to notify nodes in the network of connections or disconnections.
 * <h1>TO BE CONTINUED</h1>
 * </p>
 */
package com.phoenixkahlo.pnet;