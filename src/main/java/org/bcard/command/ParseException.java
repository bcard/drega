package org.bcard.command;

/**
 * An {@link Exception} that's thrown when a command cannot be parsed.
 * 
 * @author bcard
 * 
 */
public class ParseException extends RuntimeException {

	/**
	 * Required for serialization.
	 */
	private static final long serialVersionUID = -665202655598056611L;

	/**
	 * Creates a new {@link ParseException}.
	 * 
	 * @param value
	 *            the value that was unable to be parsed.
	 */
	public ParseException(String value) {
		super("Input '" + value + "' is not well formed");
	}
}
