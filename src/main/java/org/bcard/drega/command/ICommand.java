package org.bcard.drega.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Interface fo all of our commands.
 * 
 * @author bcard
 *
 */
public interface ICommand {

	
	/**
	 * Executes the command. Well behaved commands should typically:
	 * <ul>
	 * <li>Print what they are doing to the logger
	 * <li>Call the {@code done} handler when the command has completed
	 * </ul>
	 * If a command spawns a verticle or sends a message over the event bus then
	 * the comment should strive to call the {@code done} handler when the
	 * asynchronous commands complete.
	 * 
	 * @param container
	 *            the vertx container
	 * @param vertx
	 *            the current vertx instance
	 * @param done
	 *            handler to call when the command in finished
	 */
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done);
}
