package org.bcard.signal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bcard.signal.Signal.DependencyUpdateHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * Tests the all important {@link Signal} class. This class tries to handle
 * cases that are difficult to reproduce in the integration tests.
 * 
 * @author bcard
 * 
 */
public class SignalTest {

	@Mock
	Vertx vertx;
	
	@Mock
	EventBus eventBus;
	
	@Mock 
	Container container;
	
	@Mock
	Logger logger;
	
	@Captor
	ArgumentCaptor<Handler<Message<String>>> stringCaptor;
	
	@Captor
	ArgumentCaptor<Handler<Message<Long>>> longCaptor;
	
	@Captor
	private ArgumentCaptor<Handler<Message<JsonObject>>> handlerCaptor;
	
	Map<String, Handler<Message<JsonObject>>> depUpdateHandlers = new HashMap<String, Handler<Message<JsonObject>>>();
	
	JsonObject config = new JsonObject();
	
	private int numEvents = 0;
	
	private static final String ID = "x";
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(vertx.eventBus()).thenReturn(eventBus);
		when(container.logger()).thenReturn(logger);

		config.putNumber("initialValue", 0);
		config.putString("id", ID);
		when(container.config()).thenReturn(config);
	}
	
	@Test
	public void testLogValueOnPrintCommand() {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".print"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		verify(logger, atLeastOnce()).info(anyString());
	}
	
	@Test
	public void testUpdateValueOnIncrementCommand() {
		Signal signal = startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".increment"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		assertEquals(1, signal.value);
	}
	
	@Test
	public void testDoNotUpdateValueOnIncrementCommandWhenSignalHasDependencies() {
		DependencyTrackerTest.putDependencies(config, "2", "3");
		Signal signal = startSignal();
		
		setGraphForSignal("2", new SignalGraph("2"), 0);
		setGraphForSignal("3", new SignalGraph("3"), 1);
		
		verify(eventBus).registerHandler(eq("signals."+ID+".increment"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		assertEquals(0, signal.value);
	}
	
	@Test
	public void testPublishUpdateOnIncrementCommand() {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".increment"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		
		verify(eventBus).publish(eq("signals."+ID+".value"), eq(createUpdateMsg(1L, sc(ID, 1))));
	}
	
	@Test
	public void testReplyWithSimpleGraph() throws Exception {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".sendGraph"), stringCaptor.capture());
		
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		// signal should reply with the current graph when we ask it
		ArgumentCaptor<JsonObject> graphCaptor = ArgumentCaptor.forClass(JsonObject.class);
		verify(mockMessage).reply(graphCaptor.capture());
		
		SignalGraph expected = new SignalGraph(ID);
		String recievedJson = graphCaptor.getValue().encodePrettily();
		String expectedJson = expected.toJson();
		
		JSONAssert.assertEquals(expectedJson, recievedJson, false);
	}
	
	@Test
	public void testSignalUpdate() {
		DependencyTrackerTest.putDependencies(config, "a");
		
		Signal signal = startSignal();
		
		// we are listing 'a' as a dependency, this will setup the message to return
		// 'a's graph
		verify(eventBus).send(eq("signals.a.sendGraph"), eq(""), handlerCaptor.capture());
		Handler<Message<JsonObject>> first = handlerCaptor.getValue();
		JsonObject zObj = new JsonObject(new SignalGraph("a").toJson());
		JsonObjectMessage zMsg = new JsonObjectMessage(true, "signals.a.sendGraph", zObj);
		first.handle(zMsg);
		
		SignalGraph graph = new SignalGraph("x");
		DependencyUpdateHandler handler = signal.new DependencyUpdateHandler("address", graph);
		JsonObject obj = new JsonObject();
		obj.putNumber("value", 1);
		SignalChain chain = new SignalChain(new SignalGraph("a"));
		JsonObject chainObj = new JsonObject(chain.toJson());
		obj.putObject("chain", chainObj);
		
		JsonObjectMessage msg = new JsonObjectMessage(true, "address", obj);
		handler.handle(msg);
		
		assertEquals(1, signal.value);
	}
	
	@Test
	public void testUpdateNotSentWhenGraphNotYetPopulated() {
		DependencyTrackerTest.putDependencies(config, "a");
		Signal signal = startSignal();
		SignalGraph graph = new SignalGraph("x");
		DependencyUpdateHandler handler = signal.new DependencyUpdateHandler("address", graph);
		JsonObject obj = new JsonObject();
		obj.putNumber("value", 1);
		SignalChain chain = new SignalChain(new SignalGraph("a"));
		JsonObject chainObj = new JsonObject(chain.toJson());
		obj.putObject("chain", chainObj);
		
		JsonObjectMessage msg = new JsonObjectMessage(true, "address", obj);
		handler.handle(msg);
		
		verify(eventBus, times(0)).publish(eq("signals.x.value"), any());
	}
	
	@Test
	public void testPrintGraph() {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".print.graph"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		verify(logger, atLeastOnce()).info(anyString());
	}
	
	// --------------- Glitch avoidance tests ----------- //
	/*
	 * For all of these tests we will use one big complicated graph:
	 * 
	 *              6
	 *             / \
	 *      7     1   5
	 *       \  /  \ /  
	 *        2     3
	 *         \   /
	 *          \ /
	 *           4
	 *  
	 *  Dependencies flow from top to bottom so 4 depends on everything,
	 *  6 and 7 depend on nothing.  Our tests are written from the perspective
	 *  of 4.
	 *  
	 */
	
	@Test
	public void testSimpleGlitchAvoidance() {
		// we'll start out with an easy test
		/*
		 * x1 = 0
		 * x2 = x1
		 * x3 = x1 + x1
		 */
		
		setupSimpleSignal();
		sendEvent(sc("x1", 1));
		assertNumberOfSentValues(0);
		sendEvent(sc("x1", 1), sc("x2", 1));
		assertNumberOfSentValues(1);
		sendEvent(sc("x1", 2));
		// glitch, must wait for x2 update
		assertNumberOfSentValues(1);
	}
	
	@Test
	public void testPartialInitialize() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("5", 1), sc("3", 1));
		
		// need to wait for events from 1
		assertNumberOfSentValues(0);
	}
	
	@Test
	public void testPartialInitialize2() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("3", 1));
		
		// need to wait for events from 2
		assertNumberOfSentValues(0);
	}
	
	@Test
	public void testInitialize() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("3", 1));
		assertNumberOfSentValues(0);
		sendEvent(sc("6", 1), sc("5", 1), sc("3", 1));
		assertNumberOfSentValues(0);
		sendEvent(sc("6", 1), sc("1", 1), sc("2", 1));
		
		// our first update
		assertNumberOfSentValues(1);
	}
	
	
	@Test
	public void testAfterInitializeUpdateFrom7ok() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("5", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("2", 1));
		
		sendEvent(sc("7", 2), sc("2", 2));
		sendEvent(sc("7", 3), sc("2", 3));
		
		// updates from 7 should go through fine
		assertNumberOfSentValues(3);
	}
	
	
	@Test
	public void testAfterInitializeUpdateFrom6NeedBlock() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("5", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("2", 1));
		
		sendEvent(sc("6", 2), sc("1", 2), sc("2", 2));
		assertNumberOfSentValues(1);
		sendEvent(sc("6", 2), sc("1", 2), sc("3", 2));
		// have to assume 3 is also avoiding glitches since
		// we aren't tracking an update from 5
		assertNumberOfSentValues(2);
	}
	
	@Test
	public void testAfterInitializeNeedToKeepOrderStraight() {
		setupComplicatedSignal();
		sendEvent(sc("7", 1), sc("2", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("5", 1), sc("3", 1));
		sendEvent(sc("6", 1), sc("1", 1), sc("2", 1));
		
		sendEvent(sc("6", 2), sc("1", 2), sc("3", 2));
		assertNumberOfSentValues(1);
		sendEvent(sc("7", 2), sc("2", 2));
		// even though on different paths
		// we know the value of 2 is bad so we can't
		// apply the update from 7
		assertNumberOfSentValues(1);
		
		sendEvent(sc("6", 2), sc("5", 2), sc("3", 2));
		sendEvent(sc("6", 2), sc("1", 2), sc("2", 3));
		// ok to take event now, 2 should be at counter 3
		assertNumberOfSentValues(2);
	}
	
	
	
	// ------------------ Helper Methods ---------------- //
	
	private void setupSimpleSignal() {
		config.putString("id", "x3");
		config.putString("operator", "ADD");
		SignalGraph x1Graph = new SignalGraph("x1");
		SignalGraph x2Graph = new SignalGraph("x2", x1Graph);
		
		DependencyTrackerTest.putDependencies(config, "x2", "x1");
		
		captureHandlersAndEvents();
		
		startSignal();
		
		setGraphForSignal("x1", x1Graph, 0);
		setGraphForSignal("x2", x2Graph, 1);
	}
	
	private void setupComplicatedSignal() {
		config.putString("id", "4");
		config.putString("operator", "ADD");
		SignalGraph graph7 = new SignalGraph("7");
		SignalGraph graph6 = new SignalGraph("6");
		SignalGraph graph1 = new SignalGraph("1", graph6);
		SignalGraph graph5 = new SignalGraph("5", graph6);
		SignalGraph graph2 = new SignalGraph("2", graph7, graph1);
		SignalGraph graph3 = new SignalGraph("3", graph1, graph5);
		
		DependencyTrackerTest.putDependencies(config, "2", "3");
		
		captureHandlersAndEvents();
		
		startSignal();
		
		setGraphForSignal("2", graph2, 0);
		setGraphForSignal("3", graph3, 1);
	}
	
	private void captureHandlersAndEvents() {
		when(eventBus.registerHandler(contains(".value"), (Handler<? extends Message>) any())).then(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String address = (String)invocation.getArguments()[0];
				Handler<Message<JsonObject>> handler = (Handler<Message<JsonObject>>)invocation.getArguments()[1];
				depUpdateHandlers.put(address, handler);
				return null;
			}
			
		});
		
		when(eventBus.publish(contains(".value"), any(JsonObject.class))).then(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				numEvents++;
				return null;
			}
			
		});
	}
	
	private void setGraphForSignal(String id, SignalGraph graph, int invocationCount) {
		verify(eventBus).send(eq("signals."+id+".sendGraph"), eq(""), handlerCaptor.capture());
		List<Handler<Message<JsonObject>>> values = handlerCaptor.getAllValues();
		Handler<Message<JsonObject>> theOne = values.get(invocationCount);
		JsonObject zObj = new JsonObject(graph.toJson());
		JsonObjectMessage zMsg = new JsonObjectMessage(true, "signals."+id+".sendGraph", zObj);
		theOne.handle(zMsg);
	}
	
	private void assertNumberOfSentValues(int number) {
		assertEquals(number, numEvents);
	}
	
	private static SignalCounterPair sc(String signal, int counter) {
		SignalCounterPair pair = new SignalCounterPair();
		pair.signal = signal;
		pair.counter = counter;
		return pair;
	}
	
	private static class SignalCounterPair {
		private String signal;
		private int counter;
	}
	
	/**
	 * Sends an update event to the given signal. The path is the path from
	 * start to finish that the event has traveled through.
	 * 
	 * @param path the path to follow.
	 */
	private void sendEvent(SignalCounterPair... path) {
		JsonObject obj = createUpdateMsg(1, path);
		String last = path[path.length-1].signal;
		Handler<Message<JsonObject>> handler = depUpdateHandlers.get("signals."+last+".value");
		JsonObjectMessage msg = new JsonObjectMessage(true, "", obj);
		handler.handle(msg);
	}
	
	private Signal startSignal() {
		Signal signal = new Signal();
		signal.setContainer(container);
		signal.setVertx(vertx);
		
		signal.start(new DefaultFutureResult<Void>());
		return signal;
	}
	
	/**
	 * 
	 * @param value
	 * @param ids
	 *            dependencies from bottom to top. e.g. if 'a' sends a message to
	 *            'b' and the value of 'b' is now 1 then you should invoke this
	 *            methods as: {@code createUpdateMsg(1, "a", "b")}
	 * @return
	 */
	private JsonObject createUpdateMsg(long value, SignalCounterPair... ids) {
		JsonObject obj = new JsonObject();
		obj.putNumber("value", value);
		SignalChain chain = new SignalChain(new SignalGraph(ids[0].signal), ids[0].counter);
		if (ids.length > 1) {
			for (int i=1; i<ids.length; i++) {
				SignalGraph graph = new SignalGraph(ids[i].signal);
				chain.chain(graph, ids[i].counter);
			}
		}
		JsonObject chainObj = new JsonObject(chain.toJson());
		obj.putObject("chain", chainObj);
		
		return obj;
	}
	
}
