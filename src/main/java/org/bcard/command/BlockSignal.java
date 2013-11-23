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
	 * Whether to block the signal.  Set to {@code false} to unblock.
	 */
	private final boolean block;

	/**
	 * Creates a new {@link BlockSignal}
	 * 
	 * @param id
	 *            the ID of the signal to block
	 * @param block
	 *            whether to block the signal. Set to {@code false} to unblock
	 */
	public BlockSignal(String id, boolean block) {
		this.id = id;
		this.block = block;
	}

	@Override
	public void execute(Container container, Vertx vertx,
			Handler<AsyncResult<String>> done) {
		vertx.eventBus().send("signals."+id+".block", block);
		done.handle(new DefaultFutureResult<String>());
	}

}
