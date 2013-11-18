package org.bcard;

import java.util.Scanner;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
	
	Scanner in;
	
	@Override
	public void start() {
		container.logger().info("Starting Application...");

		// start command processor
		container.deployVerticle("org.bcard.command.CommandProcessor");

		// command line interface
		in = new Scanner(System.in);
		vertx.setPeriodic(500, new Handler<Long>() {

			@Override
			public void handle(Long event) {
				System.out.print("> ");
				String input = in.nextLine();
				if (!input.isEmpty()) {
					container.logger().debug("You typed in: " + input);
					vertx.eventBus().publish("command", input);
				}
			}
		});
	}
	
	@Override
	public void stop() {
		container.logger().info("Closing Application");
	    in.close();
	}
}
