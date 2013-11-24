package org.bcard.command;

import org.bcard.signal.CombineOperator;
import org.bcard.signal.Signal;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Combine's two symbols together applying an operator that can be used for
 * the combination.
 * 
 * @author bcard
 *
 */
public class CombineSymbols implements ICommand {
	
	/*default for testing*/ final String target;
	/*default for testing*/ final String symbol1;
	/*default for testing*/ final String symbol2;
	/*default for testing*/ final CombineOperator operator;
	
	public CombineSymbols(String target, String symbol1, String symbol2,
			CombineOperator operator) {
		this.target = target;
		this.symbol1 = symbol1;
		this.symbol2 = symbol2;
		this.operator = operator;
	}

	@Override
	public void execute(Container container, Vertx vertx,
			Handler<AsyncResult<String>> done) {
		JsonObject config = new JsonObject();
		config.putString("id", target);
		JsonArray array = new JsonArray();
		array.addString(symbol1);
		array.addString(symbol2);
		config.putArray("dependencies", array);
		config.putString("operator", operator.name());
		container.deployVerticle(Signal.class.getName(), config, done);
	}

}
