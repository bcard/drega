package org.bcard;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
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
 * <li>String message with the body 'sendGraph' causes this signal to reply with
 * the current {@link SignalGraph} in serialized JSON form
 * </ul>
 * 
 * @author bcard
 * 
 */
public class Signal extends Verticle {

	/*protected for testing*/ long value;

	private String id;
	
	private SignalGraph graph;
	
	List<SignalGraph> discoveredDependencies = new ArrayList<SignalGraph>();

	@Override
	public void start() {
		JsonObject config = container.config();
		id = config.getString("id");
		container.logger().info("Starting Signal " + id);

		if (config.getField("initialValue") != null) {
			value = config.getLong("initialValue");
		}
		
		JsonArray dependencies = config.getArray("dependencies");
		final int waitSize = dependencies == null ? 0 : dependencies.size();
		if (waitSize == 0) {
			graph = new SignalGraph(id);
		} else {
			for (Object dep : dependencies) {
				String signal = (String) dep;
				// first, let watch for any updates for this signal
				vertx.eventBus().registerHandler("signals."+signal+".value", new Handler<Message<Long>>() {

					@Override
					public void handle(Message<Long> event) {
						value = event.body();
					}
					
				});
				
				vertx.eventBus().send("signals." + signal, "sendGraph",
						new Handler<Message<JsonObject>>() {

					@Override
					public void handle(Message<JsonObject> event) {
						SignalGraph found = SignalGraph.fromJson(event.body().encodePrettily());
						discoveredDependencies.add(found);
						if (discoveredDependencies.size() >= waitSize) {
							SignalGraph[] graphs = discoveredDependencies.toArray(new SignalGraph[0]);
							graph = new SignalGraph(id, graphs);
							container.logger().info("Finished recieving graph updates, graph is now:\n"+graph);
						}
					}
				});
			}
		}
		
		vertx.eventBus().registerHandler("signals."+id, new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> event) {
				if ("print".equals(event.body())) {
					container.logger().info(id+": "+value);
				} else if ("increment".equals(event.body())) {
					value++;
					vertx.eventBus().publish("signals."+id+".value", value);
				} else if ("sendGraph".equals(event.body())) {
					JsonObject obj = null;
					if (graph != null) {
					  obj = new JsonObject(graph.toJson());
					}
					event.reply(obj);
				}
			}
		});

	}

}
