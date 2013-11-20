package org.bcard.signal;

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
 * address {@code signals.[id].[channel]} where [id] is the id of the signal you
 * wish to communicate with and [channel] is the command or channel to send
 * a value to. Currently the following messages are supported:
 * 
 * <ul>
 * <li><b>.print</b> String message, causes the signal to print it's current
 * value to the logger
 * <li><b>.increment</b> String message, causes the signal to increment it's
 * current value and send an update to all dependent signals
 * <li><b>.sendGraph</b> String message, causes this signal to reply with
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
						updateValue(event.body());
					}
					
				});
				
				// next tell our dependencies to send us their graphs
				vertx.eventBus().send("signals." + signal+".sendGraph", "",
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
		
		PrintHandler printer = new PrintHandler("signals."+id+".print");
		PrintGraphHandler printGraph = new PrintGraphHandler("signals."+id+".print.graph");
		IncrementHandler incrementer = new IncrementHandler("signals."+id+".increment");
		GraphHandler grapher = new GraphHandler("signals."+id+".sendGraph");
		
		incrementer.apply(vertx.eventBus());
		printer.apply(vertx.eventBus());
		grapher.apply(vertx.eventBus());
		printGraph.apply(vertx.eventBus());
	}
	
	private class PrintHandler extends HandlerApplicator<String> {
		
		public PrintHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			container.logger().info(id+": "+value);
		}
	}
	
	private class PrintGraphHandler extends HandlerApplicator<String> {
		
		public PrintGraphHandler(String address) {
			super(address);
		}
		
		@Override
		public void handle(Message<String> event) {
			container.logger().info("Dependency Graph for "+id+":\n"+graph);
		}
	}
	
	private class IncrementHandler extends HandlerApplicator<String> {
		
		public IncrementHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			updateValue(value + 1);
		}
	}
	
	private class GraphHandler extends HandlerApplicator<String> {
		
		public GraphHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			JsonObject obj = null;
			if (graph != null) {
			  obj = new JsonObject(graph.toJson());
			}
			event.reply(obj);
		}
	}
	
	/**
	 * Updates the valued stored by this signal and broadcasts the new value to
	 * all other signals.
	 * 
	 * @param newValue
	 *            the new value that this signal should represent
	 */
	private void updateValue(long newValue) {
		value = newValue;
		vertx.eventBus().publish("signals."+id+".value", value);
	}

}
