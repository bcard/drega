package org.bcard.command;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

public class MapSignal implements ICommand {

	private final String newSignal;
	private final String upstreamSignal;
	
	public MapSignal(String newSignal, String upstreamSignal) {
		this.newSignal = newSignal;
		this.upstreamSignal = upstreamSignal;
	}

	@Override
	public void execute(Container container, Vertx vertx) {
		// TODO Auto-generated method stub
		
	}

}
