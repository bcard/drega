package org.bcard;

import org.vertx.java.platform.Verticle;

/**
 * A signal is the base construct in our functional reactive
 * system.  The signal is an aggregator of values as well as 
 * producer of values.
 * 
 * @author bcard
 *
 */
public class Signal extends Verticle {

	@Override
	public void start() {
		String id = container.config().getString("id");
		container.logger().info("Starting Signal "+id);
	}
	
	
}
