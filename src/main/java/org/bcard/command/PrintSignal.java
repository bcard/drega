package org.bcard.command;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

public class PrintSignal implements ICommand {
	
	private String id;
	
	public PrintSignal(String id) {
		this.id = id;
	}

	@Override
	public void execute(Container container, Vertx vertx) {
		vertx.eventBus().publish("signals."+id+".print", "");
	}

}
