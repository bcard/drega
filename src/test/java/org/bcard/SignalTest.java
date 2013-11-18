package org.bcard;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * Tests the all important {@link Signal} class.
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
	ArgumentCaptor<Handler<Message<String>>> captor;
	
	JsonObject config = new JsonObject();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(vertx.eventBus()).thenReturn(eventBus);
		when(container.logger()).thenReturn(logger);

		config.putNumber("initialValue", 0);
		config.putString("id", "x");
		when(container.config()).thenReturn(config);
	}
	
	@Test
	public void testRegisterOnIdChannel() {
		config.putString("id", "x1");
		
		Signal signal = new Signal();
		signal.setContainer(container);
		signal.setVertx(vertx);
		
		signal.start();
		
		verify(eventBus).registerHandler(eq("signals.x1"), any(Handler.class));
	}
	
	@Test
	public void testLogValueOnPrintCommand() {
		Signal signal = new Signal();
		signal.setContainer(container);
		signal.setVertx(vertx);
		
		signal.start();
		
		verify(eventBus).registerHandler(anyString(), captor.capture());
		
		Message<String> mockMessage = mock(Message.class);
		when(mockMessage.body()).thenReturn("print");
		
		captor.getValue().handle(mockMessage);
		
		verify(logger, atLeastOnce()).info(anyString());
	}
}
