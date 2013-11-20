package org.bcard.command;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Command to print out the dependency graph for a signal.
 * 
 * @author bcard
 *
 */
public class PrintGraph implements ICommand {

	private final String id;
	
	public PrintGraph(String id){
		this.id = id;
	}
	
	@Override
	public void execute(Container container, Vertx vertx) {
		vertx.eventBus().send("signals."+id+".print.graph", "");
	}

}
