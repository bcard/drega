package org.bcard.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * Tests that commands are parsed and executed by the processor.
 * 
 * @author bcard
 * 
 */
public class CommandProcessorTest {

	@Mock
	Vertx vertx;
	
	@Mock
	EventBus eventBus;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(vertx.eventBus()).thenReturn(eventBus);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandChannelRegistered() {
		CommandProcessor processor = new CommandProcessor();
		processor.setVertx(vertx);
		
		processor.start();
		
		verify(eventBus).registerHandler(eq("command"), any(Handler.class));
	}
}
