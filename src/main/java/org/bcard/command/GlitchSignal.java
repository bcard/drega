package org.bcard.command;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * A command that can be used to turn on or off glitch avoidance.
 * 
 * @author bcard
 * 
 */
public class GlitchSignal implements ICommand {
	
	private final String signal;
	private final boolean glitchAvoidanceEnabled;
	
	public GlitchSignal(String signal, boolean glitchAvoidanceEnabled) {
		this.signal = signal;
		this.glitchAvoidanceEnabled = glitchAvoidanceEnabled;
	}

	@Override
	public void execute(Container container, Vertx vertx, Handler<AsyncResult<String>> done) {
		vertx.eventBus().send("signals."+signal+".glitchAvoidance", glitchAvoidanceEnabled);
		done.handle(new DefaultFutureResult<String>());
	}

}
