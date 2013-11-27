package org.bcard.drega.signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bcard.drega.signal.DependencyTracker;
import org.bcard.drega.signal.SignalGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Tests our {@link DependencyTrackerTest} class.
 * 
 * @author bcard
 *
 */
public class DependencyTrackerTest {
	
	private static final String ID = "x";
	
	@Mock
	private Future<Void> doneHandler;
	
	@Mock
	private EventBus eventBus;
	
	@Captor
	private ArgumentCaptor<Handler<Message<JsonObject>>> captor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testBlankConfigHasNoDependencies() {
		DependencyTracker tracker = newTrackerWithNoDependencies();
		assertEquals(0, tracker.getNumberOfDependencies());
	}
	
	@Test
	public void testSingleDependency() {
		DependencyTracker tracker = newTrackerWithTwoDependencies();
		assertEquals(2, tracker.getNumberOfDependencies());
	}
	
	@Test
	public void testGraphIsNullBeforeGather() {
		DependencyTracker tracker = newTrackerWithNoDependencies();
		
		assertNull(tracker.getGraph());
	}
	
	@Test
	public void testCollectNoDependenciesInitializesGraph() {
		DependencyTracker tracker = newTrackerWithNoDependencies();
		tracker.gatherDependencies(eventBus, doneHandler);
		
		SignalGraph actual = tracker.getGraph();
		SignalGraph expected = new SignalGraph("x");
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFinishHandlerCalledImmediatelyWhenThereAreNoDepdencies() {
		DependencyTracker tracker = newTrackerWithNoDependencies();
		tracker.gatherDependencies(eventBus, doneHandler);
	
		verify(doneHandler).setResult(null);
	}
	
	@Test
	public void testRequestDependencyGraph() {
		DependencyTracker tracker = newTrackerWithTwoDependencies();
		tracker.gatherDependencies(eventBus, doneHandler);
		
		verify(eventBus).send(eq("signals.y.sendGraph"), eq(""), Matchers.<Handler<Message<String>>> any());
		verify(eventBus).send(eq("signals.z.sendGraph"), eq(""), Matchers.<Handler<Message<String>>> any());
	}
	
	// test that the updates are ordered correctly
	@Test
	public void testThatUpatesAreOrderedCorrectly() {
		DependencyTracker tracker = newTrackerWithTwoDependencies();
		tracker.gatherDependencies(eventBus, doneHandler);
		
		verify(eventBus).send(eq("signals.y.sendGraph"), eq(""), captor.capture());
		verify(eventBus).send(eq("signals.z.sendGraph"), eq(""), captor.capture());
		
		List<Handler<Message<JsonObject>>> values = captor.getAllValues();
		Handler<Message<JsonObject>> first = values.get(0);
		Handler<Message<JsonObject>> second = values.get(1);
		
		// handle message for z first
		JsonObject zObj = new JsonObject(new SignalGraph("z").toJson());
		JsonObjectMessage zMsg = new JsonObjectMessage(true, "signals.z.sendGraph", zObj);
		second.handle(zMsg);
		
		JsonObject yObj = new JsonObject(new SignalGraph("y").toJson());
		JsonObjectMessage yMsg = new JsonObjectMessage(true, "signals.y.sendGraph", yObj);
		first.handle(yMsg);
		
		SignalGraph graph = tracker.getGraph();
		List<SignalGraph> deps = graph.getDependentSignals();
		
		// even though z was first we still should have the correct order here
		assertEquals("y", deps.get(0).getId());
		assertEquals("z", deps.get(1).getId());
	}
	
	// ----------------- Helper Methods ------------------//
	
	private DependencyTracker newTrackerWithNoDependencies() {
		JsonObject config = new JsonObject();
		DependencyTracker tracker = new DependencyTracker(ID, config);
		return tracker;
	}
	
	private DependencyTracker newTrackerWithTwoDependencies() {
		JsonObject config = new JsonObject();
		putDependencies(config, "y", "z");
		DependencyTracker tracker = new DependencyTracker(ID, config);
		return tracker;
	}
	
	public static void putDependencies(JsonObject config, String... dependencies) {
		JsonArray array = new JsonArray();
		for (int i=0; i<dependencies.length; i++) {
			array.addString(dependencies[i]);
		}
		config.putArray("dependencies", array);
	}
}
