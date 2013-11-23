package org.bcard.signal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.Future;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A signal is the base construct in our functional reactive system. The signal
 * is an aggregator of values as well as producer of values. Signals use the
 * event bus as the primary means of communication and will respond on the
 * address {@code signals.[id].[channel]} where [id] is the id of the signal you
 * wish to communicate with and [channel] is the command or channel to send a
 * value to. Currently the following messages are supported:
 * 
 * <ul>
 * <li><b>.print</b> String message, causes the signal to print it's current
 * value to the logger
 * <li><b>.print.graph</b> String message, causes the signal to print it's
 * current dependency graph to the logger
 * <li><b>.increment</b> String message, causes the signal to increment it's
 * current value and send an update to all dependent signals
 * <li><b>.sendGraph</b> String message, causes this signal to reply with the
 * current {@link SignalGraph} in serialized JSON form
 * <li><b>.block</b> Boolean message, blocks the current signal from sending any
 * more value updates. Dependency graph updates are still sent. Set the message
 * to {@code true} to block this signal, {@code false} to unblock it
 * </ul>
 * 
 * @author bcard
 * 
 */
public class Signal extends Verticle {

	/* protected for testing */long value;

	private String id;

	private DependencyTracker tracker;

	private final Map<SignalGraph, Long> lastValues = new HashMap<SignalGraph, Long>();

	private CombineOperator operator;

	/**
	 * Whether or not this signal is blocked from sending out value updates.
	 * {@code true} if it is blocked {@code false} if it is not blocked.
	 */
	private boolean blocked = false;

	@Override
	public void start(final Future<Void> startedResult) {
		JsonObject config = container.config();
		id = config.getString("id");
		container.logger().info("Starting Signal " + id);

		if (config.getField("initialValue") != null) {
			value = config.getLong("initialValue");
		}
		
		if (config.getField("operator") != null) {
			String name = config.getString("operator");
			operator = CombineOperator.valueOf(name);
		}

		tracker = new DependencyTracker(id, config);
		tracker.gatherDependencies(vertx.eventBus(), new DefaultFutureResult<Void>() {
			
			@Override
			public DefaultFutureResult<Void> setResult(Void result) {
				// now that all of our dependencies have been calculated we
				// should be able to subscribe for updates
				
				for (SignalGraph dep : tracker.getDependencies()) {
					DependencyUpdateHandler handler = new DependencyUpdateHandler(
							"signals." + dep.getId() + ".value",
							dep);
					handler.apply(vertx.eventBus());
				}
				startedResult.setResult(result);
				return this;
			}
			
		});
		
		PrintHandler printer = new PrintHandler("signals." + id + ".print");
		PrintGraphHandler printGraph = new PrintGraphHandler("signals." + id + ".print.graph");
		IncrementHandler incrementer = new IncrementHandler("signals." + id + ".increment");
		GraphHandler grapher = new GraphHandler("signals." + id + ".sendGraph");
		BlockHandler blocker = new BlockHandler("signals." + id + ".block");

		incrementer.apply(vertx.eventBus());
		printer.apply(vertx.eventBus());
		grapher.apply(vertx.eventBus());
		printGraph.apply(vertx.eventBus());
		blocker.apply(vertx.eventBus());
	}

	private class PrintHandler extends HandlerApplicator<String> {

		public PrintHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			container.logger().info(id + ": " + value);
		}
	}

	private class PrintGraphHandler extends HandlerApplicator<String> {

		public PrintGraphHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			container.logger().info(
					"Dependency Graph for " + id + ":\n" + tracker.getGraph());
		}
	}

	private class IncrementHandler extends HandlerApplicator<String> {

		public IncrementHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			updateValue(value + 1, null);
		}
	}

	private class GraphHandler extends HandlerApplicator<String> {

		public GraphHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			JsonObject obj = null;
			if (tracker.getGraph() != null) {
				obj = new JsonObject(tracker.getGraph().toJson());
			}
			event.reply(obj);
		}
	}


	private class BlockHandler extends HandlerApplicator<Boolean> {

		public BlockHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<Boolean> event) {
			blocked = event.body();
		}
	}
	
	/**
	 * Tracks update for a single dependency. This class will listen on a
	 * dependency's publish channel for updates and record the values as they
	 * are received.
	 * 
	 * @author bcard
	 * 
	 */
	/*protected for testing*/ class DependencyUpdateHandler extends HandlerApplicator<JsonObject> {

		private final SignalGraph symbol;

		public DependencyUpdateHandler(String address, SignalGraph symbol) {
			super(address);
			this.symbol = symbol;
		}

		@Override
		public void handle(Message<JsonObject> event) {
			JsonObject obj = event.body();
			Long newValue = obj.getLong("value");
			SignalChain chain = SignalChain.fromJson(obj.getObject("chain").toString());
			
			// TODO should not set last value until we know we can trust this update
			lastValues.put(symbol, newValue);

			if (tracker.getNumberOfDependencies() == 1) {
				updateValue(newValue, chain);
				return;
			}

			if (lastValues.size() == tracker.getNumberOfDependencies()) {
				// we've received an update from each dependency so
				// we should be clear to calculate the value.

				List<SignalGraph> graphs = tracker.getDependencies();
				Long[] args = new Long[tracker.getNumberOfDependencies()];
				for (int i = 0; i < tracker.getNumberOfDependencies(); i++) {
					SignalGraph currentGraph = graphs.get(i);
					args[i] = lastValues.get(currentGraph);
				}

				// just two values for now
				Long result = operator.call(args[0], args[1]);
				updateValue(result, chain);
			}
		}
	}

	/**
	 * Updates the valued stored by this signal and broadcasts the new value to
	 * all other signals.
	 * 
	 * @param newValue
	 *            the new value that this signal should represent
	 */
	private void updateValue(long newValue, SignalChain chain) {
		value = newValue;
		new PrintHandler(null).handle(null);
		if (!blocked && tracker.getGraph() != null) {
			if (chain == null) {
				chain = new SignalChain(tracker.getGraph());
			} else {
				chain.chain(tracker.getGraph());
			}
			JsonObject msg = new JsonObject();
			msg.putNumber("value", value);
			JsonObject chainJson = new JsonObject(chain.toJson());
			msg.putObject("chain", chainJson);
			vertx.eventBus().publish("signals." + id + ".value", msg);
		}
	}

}
