package org.bcard.command;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Exits the Application and the JVM.
 * 
 * @author bcard
 *
 */
public class Exit implements ICommand {

	@Override
	public void execute(Container container, Vertx vertx) {
		container.exit();
	}

}
