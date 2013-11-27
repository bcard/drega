package org.bcard.signal;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

/**
 * A helper class to help us apply and encapsulate our message handlers.
 * 
 * @author bcard
 * 
 */
public abstract class HandlerApplicator<T> implements Handler<Message<T>> {

	/**
	 * The address to subscribe to.
	 */
	private String address;

	/**
	 * Creates a new {@link HandlerApplicator}.
	 * 
	 * @param address
	 *            the address used to subscribe to when the
	 *            {@link #apply(EventBus)} method is called.
	 */
	public HandlerApplicator(String address) {
		this.address = address;
	}

	/**
	 * Registers this listener on the event bus.
	 * 
	 * @param bus
	 *            an event bus
	 */
	public void apply(EventBus bus) {
		bus.registerHandler(address, this);
	}

}
