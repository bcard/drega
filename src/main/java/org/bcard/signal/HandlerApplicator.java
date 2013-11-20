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
public abstract class HandlerApplicator <T> implements Handler<Message<T>>{
	
	private String address;
	
	public HandlerApplicator(String address) {
		this.address = address;
	}
	
	public void apply(EventBus bus) {
		bus.registerHandler(address, this);
	}

}
