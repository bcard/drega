package org.bcard.command;

import java.util.regex.Pattern;

/**
 * Parses command line input and turns it into a concrete command class.
 * 
 * @author bcard
 * 
 */
public class CommandParser {

	public static final String VARIABLE = "[a-zA-Z0-9]+";
	
	public static final String ASSIGNMENT_WITH_VALUE = VARIABLE+"\\s?=\\s?\\d+";

	public static final String ASSIGNMENT_WITHOUT_VALUE = "\\s?"+VARIABLE+"+\\s?";
	
	public static final String EXIT = "exit";
	
	public static final String INCREMENT = ASSIGNMENT_WITHOUT_VALUE+"\\+\\+";
	
	public static final String MAP_SIGNAL = VARIABLE+"\\s?=\\s?"+VARIABLE;
	
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
