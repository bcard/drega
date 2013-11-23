package org.bcard.command;

import java.util.regex.Pattern;

import org.bcard.signal.CombineOperator;

/**
 * Parses command line input and turns it into a concrete command class.
 * 
 * @author bcard
 * 
 */
public class CommandParser {

	public static final String VARIABLE = "[a-zA-Z][a-zA-Z0-9]*";
	
	public static final String WS = "\\s?";
	
	public static final String ASSIGNMENT_WITH_VALUE = VARIABLE+WS+"="+WS+"\\d+";

	public static final String ASSIGNMENT_WITHOUT_VALUE = WS+VARIABLE+"+"+WS;
	
	public static final String EXIT = "exit";
	
	public static final String INCREMENT = ASSIGNMENT_WITHOUT_VALUE+"\\+\\+";
	
	public static final String MAP_SIGNAL = VARIABLE+WS+"="+WS+VARIABLE;
	
	public static final String GRAPH = "graph "+VARIABLE;
	
	public static final String COMBINE_ADDITION = VARIABLE+WS+"="+WS+VARIABLE+WS+"\\+"+WS+VARIABLE;
	
	public static final String COMBINE_SUBTRACTION = VARIABLE+WS+"="+WS+VARIABLE+WS+"\\-"+WS+VARIABLE;
	
	public static final String BLOCK = "block "+VARIABLE;
	
	public static final String UNBLOCK = "unblock "+VARIABLE;
	
	public static ICommand parse(String input) {
		ICommand command = null;
		if (matches(ASSIGNMENT_WITH_VALUE, input)) {
			String[] vals = input.split("=");
			command = new CreateSignal(vals[0].trim(), Long.parseLong(vals[1].trim()));
		} else if (matches(EXIT, input)) {
			command = new Exit();
		} else if (matches(ASSIGNMENT_WITHOUT_VALUE, input)) {
			command = new PrintSignal(input.trim());
		} else if (matches(INCREMENT, input)) {
			String val = input.trim();
			val = val.substring(0, val.length()-2);
			val = val.trim();
			command = new Increment(val);
		} else if (matches(MAP_SIGNAL, input)) {
			String[] vals = input.split("=");
			command = new MapSignal(vals[0].trim(), vals[1].trim());
		} else if (matches(GRAPH, input)) {
			String[] vals = input.split(" ");
			command = new PrintGraph(vals[1]);
		} else if (matches(BLOCK, input)) {
			String[] vals = input.split(" ");
			command = new BlockSignal(vals[1], true);
		} else if (matches(UNBLOCK, input)) {
			String[] vals = input.split(" ");
			command = new BlockSignal(vals[1], false);
		} else if (matches(COMBINE_ADDITION, input)) {
			String[] vals = input.split("[=+]");
			command = new CombineSymbols(vals[0], vals[1], vals[2], CombineOperator.ADD);
		} else if (matches(COMBINE_SUBTRACTION, input)) {
			String[] vals = input.split("[=-]");
			command = new CombineSymbols(vals[0], vals[1], vals[2], CombineOperator.SUBTRACT);
		}
		
		if (command == null) {
			throw new ParseException(input);
		}
		
		return command;
	}
	
	/**
	 * Returns {@code true} if the given regex {@code pattern} matches the given
	 * {@code input}.
	 * 
	 * @param pattern
	 *            a java regular expression
	 * @param input
	 *            a input
	 * @return {@code true} if there's a match {@code false} if there is no
	 *         match
	 */
	private static boolean matches(String pattern, String input) {
		return Pattern.matches(pattern, input);
	}
}
