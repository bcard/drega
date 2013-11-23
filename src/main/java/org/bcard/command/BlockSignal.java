package org.bcard.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * Blocks a signal from sending events.
 * 
 * |@author bcard
 *
 */
public class BlockSignal implements ICommand {

	/**
	 * The id of the signal to block.
	 */
	private final String id;

	/**
	 * Creates a new {@link BlockSignal}
	 * 
	 * @param id
	 *            the ID of the signal to block
	 */
	public BlockSignal(String id) {
		this.id = id;
	}

	@Override
	public void execute(Container container, Vertx vertx,
			Handler<AsyncResult<String>> done) {
		vertx.eventBus().send("signals."+id+".block", true);
		done.handle(new DefaultFutureResult<String>());
	}

}
