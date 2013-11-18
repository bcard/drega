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
	
	public static ICommand parse(String input) {
		ICommand command = null;
		if (Pattern.matches(ASSIGNMENT_WITH_VALUE, input)) {
			String[] vals = input.split("=");
			command = new CreateSignal(vals[0].trim(), Long.parseLong(vals[1].trim()));
		}
		
		if (command == null) {
			throw new ParseException(input);
		}
		
		return command;
	}
}
