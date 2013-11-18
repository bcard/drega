package org.bcard;

import java.util.Scanner;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
	
	Scanner in;
	
	@Override
	public void start() {
		container.logger().info("Starting Application...");
		in = new Scanner(System.in);

		// command line interface
		vertx.setPeriodic(500, new Handler<Long>() {

			@Override
			public void handle(Long event) {
				System.out.print("> ");
				String s = in.nextLine();
				if (!s.isEmpty()) {
					container.logger().info("You typed in: " + s);
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
