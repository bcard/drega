package org.bcard.signal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
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
		
		verify(eventBus).publish(eq("signals."+ID+".value"), eq(1L));
	}
	
	// TODO, if we haven't yet received all of our dependencies graphs yet
	// then we shouldn't transmit our own graph
	
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
	public void testPrintGraph() {
		startSignal();
		
		verify(eventBus).registerHandler(eq("signals."+ID+".print.graph"), stringCaptor.capture());
		Message<String> mockMessage = mock(Message.class);
		stringCaptor.getValue().handle(mockMessage);
		
		verify(logger, atLeastOnce()).info(anyString());
	}
	
	private Signal startSignal() {
		Signal signal = new Signal();
		signal.setContainer(container);
		signal.setVertx(vertx);
		
		signal.start(new DefaultFutureResult<Void>());
		return signal;
	}
}
