package org.bcard.command;

import java.util.regex.Pattern;

/**
 * Parses command line input and turns it into a concrete command class.
 * 
 * @author bcard
 * 
 */
public class CommandParser {
	
	public static final String ASSIGNMENT_WITH_VALUE = "[a-zA-Z0-9]+\\s?=\\s?\\d+";

	public static final String ASSIGNMENT_WITHOUT_VALUE = "\\s?[a-zA-Z0-9]+\\s?";
	
	public static final String EXIT = "exit";
	
	public static ICommand parse(String input) {
		ICommand command = null;
		if (matches(ASSIGNMENT_WITH_VALUE, input)) {
			String[] vals = input.split("=");
			command = new CreateSignal(vals[0].trim(), Long.parseLong(vals[1].trim()));
		} else if (matches(EXIT, input)) {
			command = new Exit();
		} else if (matches(ASSIGNMENT_WITHOUT_VALUE, input)) {
			command = new PrintSignal(input.trim());
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
