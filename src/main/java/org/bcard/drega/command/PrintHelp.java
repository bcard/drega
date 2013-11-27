package org.bcard.drega.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * Prints the help documentation.
 * 
 * @author bcard
 *
 */
public class PrintHelp implements ICommand {

	@Override
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done) {
		container.logger().info(" Create a signal by assigning an integer value to a variable:");
		container.logger().info("    x=1");
		container.logger().info(" Create a dependent signal by assigning a signal to a variable");
		container.logger().info("    y=x");
		container.logger().info(" Or by combining two signals using + or -");
		container.logger().info("    z=x+y");
		container.logger().info("");
		container.logger().info(" Other commands:");
		container.logger().info(" x           print the value of x");
		container.logger().info(" x++         increment x by one");
		container.logger().info(" graph x     print the dependency graph of x");
		container.logger().info(" block x     prevent x from sending any updates to other signals");
		container.logger().info(" unblock x   allow x to send value to other signals");
		container.logger().info(" glitch x    disable glitch avoidance");
		container.logger().info(" noglitch x  enable glitch avoidance");
		container.logger().info(" exit        exit the application- use ^C in cluster mode");
		container.logger().info(" help        print this help");
		done.handle(new DefaultFutureResult<String>());
	}

}
