package org.bcard.drega.command;

import org.bcard.drega.signal.Signal;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A map operation allows a single signal to listen to the changes of another signal.
 * 
 * @author bcard
 *
 */
public class MapSignal implements ICommand {

	private final String newSignal;
	private final String upstreamSignal;
	
	public MapSignal(String newSignal, String upstreamSignal) {
		this.newSignal = newSignal;
		this.upstreamSignal = upstreamSignal;
	}

	@Override
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done) {
		JsonObject config = new JsonObject();
		config.putString("id", newSignal);
		JsonArray array = new JsonArray();
		array.addString(upstreamSignal);
		config.putArray("dependencies", array);
		container.deployVerticle(Signal.class.getName(), config, done);
	}

}
