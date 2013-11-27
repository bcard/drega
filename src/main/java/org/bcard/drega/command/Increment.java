package org.bcard.drega.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * A command that increments the value of a signal.
 * 
 * @author bcard
 *
 */
public class Increment implements ICommand {

	private final String id;
	
	public Increment(String id) {
		this.id = id;
	}
	
	@Override
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done) {
		vertx.eventBus().publish("signals."+id+".increment", "");
		done.handle(new DefaultFutureResult<String>());
	}

}
