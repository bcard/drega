package org.bcard.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Manages the dependencies for a {@link Signal}. This class maintains the set
 * of dependencies and provides support for responding to dependency updates as
 * well as tracking dependency changes.
 * 
 * @author bcard
 * 
 */
public class DependencyTracker {

	private SignalGraph graph;

	private SignalGraph[] discoveredDependencies;
	
	/**
	 * The ID of the signal being monitored by this tracker.
	 */
	private final String id;
	
	private final List<String> dependencies = new ArrayList<>();
	
	/**
	 * Creates a new {@link DependencyTracker}.
	 * 
	 * @param config
	 *            the config object to extract the dependencies from
	 */
	public DependencyTracker(String id, JsonObject config) {
		final JsonArray configDeps = config.getArray("dependencies");
		if (configDeps != null) {
			for (Object dep : configDeps) {
				this.dependencies.add((String) dep);
			}
		}
		
		discoveredDependencies = new SignalGraph[dependencies.size()];
		this.id = id;
	}
	
	/**
	 * @return the number of dependencies that this class is tracking
	 */
	public int getNumberOfDependencies() {
		return dependencies.size();
	}
	
	/**
	 * Gathers information about the dependency graphs for this signal's
	 * dependencies. The {@code eventBus} is used to communicate with other
	 * signals as needed. The {@code doneHandler} will be called when all
	 * dependency updates have been received.
	 * 
	 * @param eventBus
	 *            the event bus used to communicate with other vertx signals
	 * @param doneHandler
	 *            the handler to be called when all of the dependency
	 *            information has been collected
	 * 
	 */
	public void gatherDependencies(EventBus eventBus, Future<Void> doneHandler) {
		if (dependencies.size() == 0) {
			doneHandler.setResult(null);
			graph = new SignalGraph(id);
		} else {
			for (int i=0; i<dependencies.size(); i++) {
				String signal = (String)dependencies.get(i);
			
				// tell our dependencies to send us their graphs
				eventBus.send("signals."+signal+".sendGraph", "", new GraphReceiver(i, doneHandler));
			}
		}
	}
	
	/**
	 * Returns the {@link SignalGraph} maintained by this class. Note that this
	 * graph will be {@code null} until the
	 * {@link #gatherDependencies(EventBus, Future)} method is call and
	 * completed.
	 * 
	 * @return the {@link SignalGraph} for this class
	 */
	public SignalGraph getGraph() {
		return graph;
	}
	
	/**
	 * Private class that handles receiving the dependency graphs from other
	 * signals. This class saves the graphs into the
	 * {@link Signal#discoveredDependencies} array <i>in the order that they are
	 * declared in the Signal's config file, not the order they arrive</i>. When
	 * all of the dependencies have been received then the {@code finishHandler}
	 * is called.
	 * 
	 * @author bcard
	 * 
	 */
	private class GraphReceiver implements Handler<Message<JsonObject>> {

		/**
		 * The index of this dependency. This is the index to place the
		 * dependency in the {@link Signal#discoveredDependencies} list.
		 */
		private final int index;
		
		/**
		 * Handler to call when all updates have been received.
		 */
		private final Future<Void> finishHandler;
		
		/**
		 * Creates a new {@link GraphReceiver}.
		 * 
		 * @param index
		 *            index in {@link Signal#discoveredDependencies} that this
		 *            dependency corresponds to
		 * @param bus event bus used for listening for events
		 * @param finishHandler
		 *            handler to call when all dependencies have been received
		 */
		public GraphReceiver(int index, Future<Void> finishHandler) {
			this.index = index;
			this.finishHandler = finishHandler;
		}

		@Override
		public void handle(Message<JsonObject> event) {
			SignalGraph found = SignalGraph.fromJson(event
					.body().encodePrettily());
			discoveredDependencies[index] = found;

			int size = 0;
			for (int i=0; i<discoveredDependencies.length; i++) {
				if (discoveredDependencies[i] != null) {
					size++;
				}
			}
			
			if (size >= dependencies.size()) {
				graph = new SignalGraph(id, discoveredDependencies);
				finishHandler.setResult(null);
			}
		}
	}
	
	public List<SignalGraph> getDependencies() {
		return Arrays.asList(discoveredDependencies);
	}
}
