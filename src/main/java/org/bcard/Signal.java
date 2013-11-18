package org.bcard;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/**
 * A signal is the base construct in our functional reactive system. The signal
 * is an aggregator of values as well as producer of values. Signals use the
 * event bus as the primary means of communication and will respond on the
 * address {@code signals.[id]} where [id] is the id of the signal you wish to
 * communicate with. Currently the following messages are supported:
 * 
 * <ul>
 * <li>String message with body 'print', causes the signal to print it's current
 * value to the logger
 * <li>String message with body 'increment' causes the signal to increment it's
 * current value and send an update to all dependent signals
 * </ul>
 * 
 * @author bcard
 * 
 */
public class Signal extends Verticle {

	/*protected for testing*/ long value;

	private String id;

	@Override
	public void start() {
		id = container.config().getString("id");
		value = container.config().getLong("initialValue");
		container.logger().info("Starting Signal " + id);
		
		vertx.eventBus().registerHandler("signals."+id, new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> event) {
				if ("print".equals(event.body())) {
					container.logger().info(id+": "+value);
				} else if ("increment".equals(event.body())) {
					value++;
				}
			}
			
		});
		
	}

}
