package org.bcard.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
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
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done ) {
		container.exit();
		container.logger().info("goodbye");
	}

}
