
/**
 * <p>
 * Proxies are a class of object that represent other objects in a remote
 * nodenet client, which allows the local client to remotely invoke methods of
 * the represented object.
 * </p>
 * <p>
 * The central class of this package is Proxy<E>, which represents a remote
 * object of interface E, and can be remotely invoked through the interface E.
 * To create a proxy out of a local object, the client must invoke the LocalNode
 * with that object and the an supertype interface E, which will return a
 * Proxy<E>. That Proxy is serializable, and can be sent to other nodenet
 * clients. A Proxy can provide 3 <i>invokables</i> of type E. When the methods
 * of the invokable are invoked, the effect will depend on what type of
 * invokable they are.
 * </p>
 * <p>
 * The first type of invokable is the blocking type. When it is invoked, the
 * calling thread will sleep, and a transmission will be sent to the proxy
 * source, containium the given arguments. The source of the proxy will be
 * invoked with those arguments, and the returned object (or returned void, or
 * thrown exception) will be transmitted back to the calling node, and the
 * calling thread will awaken and return the object (or void, or throw the
 * exception). If the node is disconnected before this process is completed, the
 * method will yield a RuntimeDisconnectionException.This invokable type is most
 * simillar to invoking a local object.
 * </p>
 * <p>
 * The second type of invokable is the unblocking type. When it is invoked, a
 * transmission containium the arguments will be sent to the source node and the
 * source object will be invoked, as with the blocking type. However, instead of
 * the calling thread sleeping, it will return immediately. If the attempt to
 * transmit the message yields a DisconnectionException, the method will yield a
 * RuntimeDisconnectionException. Any values returned from non-void methods will
 * be gibberish. Sequences of invocations will not be guarenteed to arrive in
 * order.
 * </p>
 * <p>
 * The third type of invokable is buffered. To create a buffered invokable from
 * a proxy, you must pass it an InvocationBuffer. When it is invoked, the
 * transmission representing that invocation will simply be added to the buffer.
 * The method will return immediately, with gibberish returned data, and never
 * throw an exception. At any time, the buffer can be flushed, which will cause
 * all the invocations to be transmitted to the destination as one message. When
 * the buffer is flushed, the flushing thread can choose either to resume
 * immediately (throwing a DisconnectionException if the node is disconnected)
 * or to wait until confirmation is received that all the methods have been
 * invoked (throwing a DisconnectionException if this fails).
 * </p>
 * <p>
 * If a node is waiting on the result of a proxy invocation, and the source node
 * receives the transmission but fails to invoke the proxy for whatever reason,
 * it will send a transmission representing its failure, and the invoking thread
 * will yield a ProxyException or RuntimeProxyException.
 * </p>
 */
package com.phoenixkahlo.nodenet.proxy;