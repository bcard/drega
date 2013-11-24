package org.bcard;

import java.util.Scanner;

import org.bcard.command.CommandProcessor;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/**
 * Main entry point into the application. This class starts the REPL and command
 * processing verticles.
 * 
 * @author bcard
 * 
 */
public class Main extends Verticle {
	
	InputHandler handler;
	
	@Override
	public void start() {
		container.logger().info("Starting Application...");

		// start command processor
		container.deployVerticle(CommandProcessor.class.getName());

		// command line interface
		handler = new InputHandler(vertx);
		vertx.setTimer(100, handler);
	}
	
	@Override
	public void stop() {
		container.logger().info("Closing Application");
	    handler.close();
	}
	
	/**
	 * This class handles all input from the console and creates and maintains
	 * our interactive REPL. All text is published on the command channel, other
	 * verticles can subscribe to this and react to events as needed. The
	 * {@link CommandProcessor} is currently the only class that monitors this
	 * channel. Note that any verticle that listens on this channel <b>must</b>
	 * reply when they are finished processing the command so the REPL can
	 * continue.
	 * 
	 * @author bcard
	 * 
	 */
	private static class InputHandler implements Handler<Long> {
		
		private final Vertx vertx;
		private final Scanner in;
		
		public InputHandler(Vertx vertx) {
			this.vertx = vertx;
			in = new Scanner(System.in);
		}
		
		@Override
		public void handle(Long event) {
			System.out.print("> ");
			String input = in.nextLine();
			if (!input.isEmpty()) {
				vertx.eventBus().send("command", input, new Handler<Message<Object>>() {

					@Override
					public void handle(Message<Object> event) {
						vertx.setTimer(50, InputHandler.this);
					}

				});
			} else {
				vertx.setTimer(50, InputHandler.this);
			}
		}
		
		private void close() {
			in.close();
		}
	}
}
