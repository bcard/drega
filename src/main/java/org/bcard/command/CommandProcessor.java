package org.bcard.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/**
 * The command processor parses and executes commands written to the
 * {@code command} address. See {@link CommandParser} for a description of the
 * command syntax.
 * 
 * @author bcard
 * 
 */
public class CommandProcessor extends Verticle {

	@Override
	public void start() {
		container.logger().info("Starting Command Processor...");
		vertx.eventBus().registerLocalHandler("command", new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> event) {
				String text = event.body();
				try {
					ICommand command = CommandParser.parse(text);
					
					AsynchResultHandler handler = new AsynchResultHandler(event);
					command.execute(container, vertx, handler);
				}
				catch (ParseException e) {
					container.logger().error("Invalid Command:"+text);
					event.reply();
				}
			}
		});
		
	}
	
	private static class AsynchResultHandler implements Handler<AsyncResult<String>> {
		
		private final Message<String> event;
		
		public AsynchResultHandler(Message<String> event) {
			this.event = event;
		}

		@Override
		public void handle(AsyncResult<String> result) {
			// must be called for the REPL to know that we are finished
			event.reply();
		}
		
	}
}
