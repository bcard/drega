package org.bcard.signal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.bcard.signal.Signal.DependencyUpdateHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
	
	JsonObject config = new JsonObject();
	
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
	public void testPublishUpdateOnIncrementCommand() {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".increment"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		
		verify(eventBus).publish(eq("signals."+ID+".value"), eq(createUpdateMsg(1L, ID)));
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
	
	// ------------------ Helper Methods ---------------- //
	
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
	 *            dependencies from bottom to top. e.g. if a sends a message to
	 *            b and the value of b is now 1 then you should invoke this
	 *            methods as: {@code createUpdateMsg(1, "a", "b")}
	 * @return
	 */
	private JsonObject createUpdateMsg(long value, String... ids) {
		JsonObject obj = new JsonObject();
		obj.putNumber("value", value);
		SignalChain chain = new SignalChain(new SignalGraph(ids[0]));
		if (ids.length > 1) {
			for (int i=1; i<ids.length; i++) {
				SignalGraph graph = new SignalGraph(ids[i]);
				chain.chain(graph);
			}
		}
		JsonObject chainObj = new JsonObject(chain.toJson());
		obj.putObject("chain", chainObj);
		
		return obj;
	}
	
}
