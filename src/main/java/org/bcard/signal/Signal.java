package org.bcard.signal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 * to {@code true} to block this signal, {@code false} to unblock it.
 * <li><b>.glitchAvoidance</b> Boolean message, enables or disables glitch
 * avoidance. Set the message body to {@code true} to enable glitch avoidance.
 * Set to {@code false} to disable glitch avoidance.
 * </ul>
 * 
 * @author bcard
 * 
 */
public class Signal extends Verticle {

	/* protected for testing */long value;

	private String id;

	private DependencyTracker tracker;

	private final Map<SignalGraph, ChainValueMap> lastValues = new HashMap<SignalGraph, ChainValueMap>();

	private CombineOperator operator;

	/**
	 * Whether or not this signal is blocked from sending out value updates.
	 * {@code true} if it is blocked {@code false} if it is not blocked.
	 */
	private boolean blocked = false;
	
	/**
	 * An event counter that's sent out with event update.  This allows
	 * dependency signals to know when they have the latest value (or at
	 * least matching value) for a signal.
	 */
	private int eventCounter = 0;
	
	/**
	 * A flag that's used to enable or disable glitch avoidance.
	 */
	private boolean glitchAvoidanceEnabled = true;
	
	private ResendHandler resendHandler;

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
					// request updates from our dependencies so we can have a good initial
					// value
					vertx.eventBus().send("signals." + dep.getId() + ".get", "");
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
		resendHandler = new ResendHandler("signals."+id+".get");
		GlitchAvoidanceHandler glitchHandler = new GlitchAvoidanceHandler("signals."+id+".glitchAvoidance");

		incrementer.apply(vertx.eventBus());
		printer.apply(vertx.eventBus());
		grapher.apply(vertx.eventBus());
		printGraph.apply(vertx.eventBus());
		blocker.apply(vertx.eventBus());
		resendHandler.apply(vertx.eventBus());
		glitchHandler.apply(vertx.eventBus());
		resendHandler.setLastValue(value, null);
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
			if (!tracker.getGraph().getDependentSignals().isEmpty()) {
				container.logger().info("Cannot increment a signal with dependencies");
			} else {
				updateValue(value + 1, null);
			}
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
	
	private class GlitchAvoidanceHandler extends HandlerApplicator<Boolean> {
		
		public GlitchAvoidanceHandler(String address) {
			super(address);
		}
		
		@Override
		public void handle(Message<Boolean> event) {
			String msg = event.body() ? "enabled" : "disabled";
			container.logger().info("Glitch avoidance "+msg+" on "+id);
			glitchAvoidanceEnabled = event.body();
		}
	}
	
	private class ResendHandler extends HandlerApplicator<String> {

		private Long result;
		private SignalChain chain;
		
		public ResendHandler(String address) {
			super(address);
		}

		@Override
		public void handle(Message<String> event) {
			if (result != null) {
				updateValue(result, chain);
			}
		}
		
		/**
		 * Sets the last value that was sent by this signal. If other signals
		 * ask for a resend then this value and signal chain will be submitted.
		 * 
		 * @param result
		 *            the last result that was sent
		 * @param chain
		 *            the last chain that was sent
		 */
		public void setLastValue(Long result, SignalChain chain) {
			this.result = result;
			this.chain = chain;
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
//			container.logger().info(chain.getLast()+"->"+id+" value:"+newValue+" chain:"+chain);
			ChainValueMap valueMap;
			if (lastValues.containsKey(symbol)) {
				valueMap = lastValues.get(symbol);
			} else {
				valueMap = new ChainValueMap();
				lastValues.put(symbol, valueMap);
			}
			
			valueMap.merge(chain);
			valueMap.value = newValue;

			if (tracker.getNumberOfDependencies() == 1) {
				updateValue(newValue, chain);
				return;
			}

			if (lastValues.size() == tracker.getNumberOfDependencies()) {
				// we've received an update from each dependency so
				// we should be clear to calculate the value if there
				// are no glitches.
				
				if (!glitchAvoidanceEnabled || !checkForGlitches(tracker.getGraph(), lastValues)) {
					List<SignalGraph> graphs = tracker.getDependencies();
					Long[] args = new Long[tracker.getNumberOfDependencies()];
					for (int i = 0; i < tracker.getNumberOfDependencies(); i++) {
						SignalGraph currentGraph = graphs.get(i);
						args[i] = lastValues.get(currentGraph).value;
					}

					// just two values for now
					Long result = operator.call(args[0], args[1]);
					SignalChain allUpdates = new SignalChain();
					for (ChainValueMap map : lastValues.values()) {
						for (Entry<String, Integer> entry : map.counterMap.entrySet()) {
							allUpdates.chain(new SignalGraph(entry.getKey()), entry.getValue());
						}
					}
					
					updateValue(result, allUpdates);
				}
			}
		}
	}
	
	/**
	 * Checks for glitches. Returns {@code true} if a glitch is detected,
	 * {@code false} if there are no glitches and the values are ok to update
	 * 
	 * @param graph
	 *            the dependency graph for this signal
	 * @param lastUpdates
	 *            the recorded values for each dependent signal
	 * @return {@code true} if there are glitches, {@code false} if there are
	 *         not
	 */
	private boolean checkForGlitches(SignalGraph graph, Map<SignalGraph, ChainValueMap> lastUpdates) {
		List<SignalChain> allPaths = graph.allPaths();
		Set<String> collisions = new HashSet<>();
		for (SignalChain chain1 : allPaths) {
			for (SignalChain chain2 : allPaths) {
				if (!chain1.equals(chain2) && !chain1.getConflicts(chain2).isEmpty()) {
					// candidate for a collision, we still need to check to see if the _next_
					// signal in the chain is different
					collisions.addAll(chain1.getConflicts(chain2));
				}
			}
		}
		
		// Need to track counters from each signal.  Once we have that then we should
		// be able to take our conflicts, search through all of our chains that we've 
		// received and make sure that the numbers for the conflicts line up for
		// all events.  If some number doesn't match, then we have an issue and need
		// to hold off until other updates are received.

		boolean returnValue = false;
		Map<String, Integer> counterMap = new HashMap<>();
		for (Entry<SignalGraph, ChainValueMap> entry : lastUpdates.entrySet()) {
			for (String collision : collisions) {
				SignalGraph depGraph = entry.getKey();
				if (depGraph.containsId(collision)) {
					// make sure we have an update for this collision
					// otherwise we can't trust the last update value

					ChainValueMap map = entry.getValue();
					Integer counter = map.get(collision);
					if (counter == null) {
//						container.logger().info("missing update from "+collision+" on signal "+depGraph);
						// missing an update, this is a glitch!
						returnValue = true;
					} else {
						// from here we need to verify that the counter
						// is the same as any other occurrence of that collision
						if (!counterMap.containsKey(collision)) {
							counterMap.put(collision, counter);
						} else {
							// counter must line up
							int existing = counterMap.get(collision);
							returnValue |= existing != counter;
							if (returnValue) {
//								container.logger().info(
//										"counters differ for " + collision + ". counterMap:"
//												+ Integer.valueOf(existing) + ", counter:"
//												+ Integer.valueOf(counter)+", dependency:"+depGraph.getId());
							}
						}
					}
				}
			}
		}
		
		return returnValue;
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
			eventCounter++;
			if (chain == null) {
				chain = new SignalChain(tracker.getGraph(), eventCounter);
			} else {
				chain.chain(tracker.getGraph(), eventCounter);
			}
			JsonObject msg = new JsonObject();
			msg.putNumber("value", value);
			JsonObject chainJson = new JsonObject(chain.toJson());
			msg.putObject("chain", chainJson);
			vertx.eventBus().publish("signals." + id + ".value", msg);
			resendHandler.setLastValue(value, chain);
		}
	}
	
	private class ChainValueMap {
		
		private Map<String, Integer> counterMap = new HashMap<>();
		
		public void merge(SignalChain chain) {
			for (String signal : chain.toList()) {
				Integer counter = chain.getEventCounterFor(new SignalGraph(signal));
				if (!counterMap.containsKey(signal)) {
					counterMap.put(signal, counter);
				} else {
					// TODO this is accounting for out of order signal updates, should be a 
					// unit test for this.
					Integer existing = counterMap.get(signal);
					if (counter.compareTo(existing) > 0) {
						counterMap.put(signal, counter);
					}
				}
			}
		}
		
		public Integer get(String signal) {
			return counterMap.get(signal);
		}
		
		private Long value;
	}

}
