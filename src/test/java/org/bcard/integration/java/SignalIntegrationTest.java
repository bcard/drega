package org.bcard.integration.java;

import static org.vertx.testtools.VertxAssert.testComplete;

import org.bcard.command.CombineSymbols;
import org.bcard.command.CommandProcessor;
import org.bcard.command.CreateSignal;
import org.bcard.command.Increment;
import org.bcard.command.MapSignal;
import org.bcard.command.PrintGraph;
import org.bcard.command.PrintSignal;
import org.bcard.signal.CombineOperator;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

/**
 * Integration tests for the signals so we can build more complicated scenerios.
 * 
 * @author bcard
 * 
 */
public class SignalIntegrationTest extends TestVerticle {

	@Test
	public void testIncrement() {
		CreateSignal create = new CreateSignal("x", 0);
		create.execute(container, vertx, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				vertx.eventBus().registerHandler("signals.x.value", new Handler<Message<Long>>() {

					@Override
					public void handle(Message<Long> event) {
						VertxAssert.assertEquals(1L, event.body().longValue());
						testComplete();
					}
				});

				// send the increment command
				Increment increment = new Increment("x");
				increment.execute(container, vertx, new DummyHandler());
			}
		});
	}
	
	@Test
	public void testPrintGraph() {
		CreateSignal create = new CreateSignal("x", 0);
		create.execute(container, vertx, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				PrintGraph print = new PrintGraph("x");
				print.execute(container, vertx, new Handler<AsyncResult<String>>(){

					@Override
					public void handle(AsyncResult<String> event) {
						testComplete();
					}
				});
			}
		});
	}
	
	@Test
	public void testPrint() {
		CreateSignal create = new CreateSignal("x", 0);
		create.execute(container, vertx, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				PrintSignal print = new PrintSignal("x");
				print.execute(container, vertx, new Handler<AsyncResult<String>>(){

					@Override
					public void handle(AsyncResult<String> event) {
						testComplete();
					}
				});
			}
		});
	}
	
	@Test
	public void testMapSingle() {
		CreateSignal create = new CreateSignal("x", 0);
		create.execute(container, vertx, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				
				MapSignal create = new MapSignal("y", "x");
				create.execute(container, vertx, new Handler<AsyncResult<String>>() {

					@Override
					public void handle(AsyncResult<String> event) {
						// check for increase on y
						vertx.eventBus().registerHandler("signals.y.value", new Handler<Message<Long>>() {

							@Override
							public void handle(Message<Long> event) {
								VertxAssert.assertEquals(1L, event.body().longValue());
								testComplete();
							}
						});

						// send the increment command
						Increment increment = new Increment("x");
						increment.execute(container, vertx, new DummyHandler());
					}
				});
			}
		});
	}
	
	@Test
	public void testSimpleCombine() {
		CreateSignal create = new CreateSignal("x", 0);
		create.execute(container, vertx, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				CreateSignal create = new CreateSignal("y", 1);
				create.execute(container, vertx, new Handler<AsyncResult<String>>() {

					@Override
					public void handle(AsyncResult<String> event) {
						CombineSymbols create = new CombineSymbols("z", "x", "y", CombineOperator.ADD);
						create.execute(container, vertx, new Handler<AsyncResult<String>>() {

							@Override
							public void handle(AsyncResult<String> event) {
								Increment increment = new Increment("x");
								increment.execute(container, vertx, new Handler<AsyncResult<String>>() {

									@Override
									public void handle(AsyncResult<String> event) {
										// we should get an update after y is incremented
										vertx.eventBus().registerHandler("signals.z.value", new Handler<Message<Long>>() {

											@Override
											public void handle(Message<Long> event) {
												VertxAssert.assertEquals(3L, event.body().longValue());
												testComplete();
											}
										});
										
										Increment increment = new Increment("y");
										increment.execute(container, vertx, new DummyHandler());
									}
									
								});
								
							}
						});
					}
				});
			}
		});
	}
	
	// --------------- Helper Methods ---------------- //

	@Override
	public void start() {
		// Make sure we call initialize() - this sets up the assert stuff so
		// assert functionality works correctly
		initialize();
		// Deploy the module - the System property `vertx.modulename` will
		// contain the name of the module so you
		// don't have to hardecode it in your tests
		startTests();
	}

	private static final class DummyHandler implements
			Handler<AsyncResult<String>> {

		@Override
		public void handle(AsyncResult<String> event) {
			// do nothing
		}
	}

}
