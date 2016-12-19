
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
 * <h4>Handshake</h4>
 * <p>
 * The first class of object, Handshake, should be sent immediately whenever a
 * new connection is established. The Handshake contains 3 pieces of data.
 * </p>
 * <p>
 * The first is a chunk of binary data, constant to all programs. If not
 * received correctly - an exception should be thrown on deserialization. This
 * ensures that the other side is a legitimate NodeNetwork implementation, and
 * not some random computer. This constant should be changed between
 * incompatible version of the protocol to prevent bad connections.
 * </p>
 * <p>
 * The second is a list of all node connections known to the sender. All of
 * these should be added to the receiver's set of known connections, so that it
 * can have an accurate model of the graph.
 * </p>
 * <p>
 * The third is the node address of the sender, which is necessary for the
 * receiver to update the network model with the newly formed connection. After
 * a new connection is formed, both sides should virally propagate an
 * UpdateTrigger to inform all nodes of the new connection.
 * </p>
 * <h4>ViralMessage</h4>
 * <p>
 * The second class of object, ViralMessage, carries a payload, and is designed
 * to get that payload to every node in the network, without requiring any node
 * to be aware of the state of the network beyond its immediate neighbors. The
 * usage of ViralMessages is to notify nodes in the network of connections or
 * disconnections.
 * </p>
 * <p>
 * A ViralMessage contains 3 pieces of data: a randomly generated virus ID, a
 * set of node addresses that it has infected, and its payload Object. All nodes
 * must keep track of which virii ID they have already handled, so that they
 * will not infect neighbors with the same virus twice (if they did, sending a
 * ViralMessage would cost O(n!) to the network). If a node receives a virus
 * that it has already handled, it not handle it.
 * </p>
 * <p>
 * To handle of ViralMessage, a node must first add the virus ID to its set of
 * virii it has handled, and add the local node address to the virus' set of
 * nodes it has infected. It must then transmit the ViralMessage to each
 * neighbor that is not in the virus' set of nodes it has already infected. Once
 * this is complete, the node should handle the virus' payload.
 * </p>
 * <p>
 * There are two objects that are valid for a virus' payload - a
 * NeighborSetUpdate and a UpdateTrigger. The NeighborSetUpdate is an update of
 * the set of neighbors that a particular node has. For a node to handle a
 * NeighborSetUpdate, it should remove all connections involving the update's
 * node from its network model, and then add to its network model all the
 * connections described in the update. The UpdateTrigger is a trigger for all
 * nodes to send a NeighborSetUpdate of themselves, which is what a node should
 * do to handle an UpdateTrigger.
 * </p>
 * <h4>AddressedMessage and AddressedMessageResult</h4>
 * <p>
 * The third class of object, AddressedMessage, carries a payload, and is
 * designed to efficiently get that payload to specific node in the network.
 * AddressedMessages are the most complicated type of transmission due to their
 * asynchronous and responsive nature, and rely on an accurate network model to
 * be maintained by the ViralMessage system. AddressedMessages are used to send
 * client objects between nodes.
 * </p>
 * <p>
 * AddressedMessages contain 4 pieces of data. They have a payload object and a
 * destination address, which should not be changed. They have a collection of
 * visited nodes, which should be added to upon a node being received. Finally,
 * they have a random ID. The ID is not meant to represent the message
 * throughout its entire lifespan, but rather to represent the transmission of a
 * message between two nodes. Thus, the AddressedMessage's ID should be
 * randomized between transmissions.
 * </p>
 * <p>
 * For each transmission of an AddressedMessage, a AddressedMessageResult should
 * be transmitted in the opposite direction, as a notification that either the
 * message has reached its destination, or that the receiver of the
 * AddressedMessage was ultimately unable to get the message to its destination.
 * AddressedMessageResults have IDs, which should be equal to the ID of the
 * corresponding AddressedMessage transmission.
 * </p>
 * <p>
 * When a node receives an AddressedMessage, and the AddressedMessage's
 * destination address is the local address, it should simply respond with a
 * success AddressedMessageResult, and then handle the payload. Otherwise, the
 * node's goal is to try to get the AddressedMessage to its destination and then
 * transmit an appropriate AddressedMessageResult. It should send the
 * AddressedMessage to the unvisited neighbor with the shortest distance to the
 * destination, and wait for a response. If the neighbor responds with failure,
 * or doesn't respond within an optimistic amount of time, the node should send
 * the message to its next most direct unvisited neighbor. If it does this
 * because the previous neighbor did not respond in a certain amount of time,
 * the node should still be prepared to receive a response from any such node.
 * If any neighbor responds with a success message, the node should conclude
 * attempts to get the message to its destination, and send a success
 * AddressedMessageResult to the node from which it received the message in the
 * first place. If all unvisited neighbors respond with failure, or a certain
 * large amount of time passes without any success, the node should send a
 * failure AddressedMessageResult.
 * </p>
 * <p>
 * There is only one valid class of payload for an AddressedMessage, which is
 * ClientTransmission. This carries objects sent by client code from one node to
 * another. This payload should be handled by making the ClientTransmission's
 * own payload available to the client.
 * </p>
 */
package com.phoenixkahlo.pnet;