package com.phoenixkahlo.nodenet.proxy;

import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.util.UUID;

/**
 * A representation of a remote object that can be reified and invoked. Is
 * always serializable.
 */
public interface Proxy<E> {

	/**
	 * A reification that will wait for a response upon invocations. In the
	 * event of a disconnection, invocations will throw a
	 * RuntimeDisconnectionException.
	 */
	E blocking();

	/**
	 * A reification that will not wait for a response upon invocations. As
	 * such, any data returned from non-void methods is meaningless.
	 * Disconnections will yield RuntimeDisconnectionExceptions only if enabled
	 * with the disconnectionException argument.
	 */
	E unblocking(boolean disconnectionException);

	/**
	 * A reification that will send invocations to a InvocationBuffer, which the
	 * client can flush as desired. Since the buffer is free is be flushed at
	 * any time (or never), any data returned from non-void methods is
	 * meaningless, and invocations will never throw any exception.
	 */
	E buffered(InvocationBuffer buffer);

	/**
	 * @return the address of the object that this proxy leads to.
	 */
	NodeAddress getSource();

	/**
	 * @return the proxy ID of this proxy.
	 */
	UUID getProxyID();

	/**
	 * Cast this proxy to a certain type parameter, throwing a
	 * ClassCastException if the parameter is invalid.
	 */
	<T> Proxy<T> cast(Class<T> to);

}
