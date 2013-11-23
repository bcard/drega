package org.bcard.signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
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

	// TODO this logic is in the signal class now
//	@Test
//	public void testRegisterForDependencyUpdates() {
//		DependencyTracker tracker = newTrackerWithTwoDependencies();
//		tracker.gatherDependencies(eventBus, doneHandler);
//		
//		verify(eventBus).registerHandler(eq("signals.y.value"), Matchers.<Handler<Message<Long>>> any());
//		verify(eventBus).registerHandler(eq("signals.z.value"), Matchers.<Handler<Message<Long>>> any());
//	}
	
	// ----------------- Helper Methods ------------------//
	
	private DependencyTracker newTrackerWithNoDependencies() {
		JsonObject config = new JsonObject();
		DependencyTracker tracker = new DependencyTracker(ID, config);
		return tracker;
	}
	
	private DependencyTracker newTrackerWithTwoDependencies() {
		JsonObject config = new JsonObject();
		JsonArray array = new JsonArray();
		array.addString("y");
		array.addString("z");
		config.putArray("dependencies", array);
		DependencyTracker tracker = new DependencyTracker(ID, config);
		return tracker;
	}
}
