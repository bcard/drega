package org.bcard.command;

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
	 * Executes the command.  Well behaved commands should typically:
	 * <ul>
	 * <li> Print what they are doing to the logger
	 * </ul>
	 */
	public void execute(Container container, Vertx vertx);
}
