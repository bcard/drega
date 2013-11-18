package org.bcard.command;

import static org.junit.Assert.*;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import static org.hamcrest.core.IsInstanceOf.*;

/**
 * Tests our command parsing input for the repl.
 * 
 * @author bcard
 * 
 */
public class CommandParserTest {

	@Test
	public void testDefineSignal() {
		assertCreateParsesToIdAndValue("x1=1", "x1", 1);
	}

	@Test
	public void testDefineSignalWithSpaces() {
		assertCreateParsesToIdAndValue("x1 = 1", "x1", 1);
	}
	
	@Test
	public void testDefineSignalJustLetters() {
		assertCreateParsesToIdAndValue("xxsd=1", "xxsd", 1);
	}

	@Test(expected=ParseException.class)
	public void testHangingEqualsNotAllowed() {
		assertCreateParsesToIdAndValue("x1 =", "x1", 1);
	}
	
	@Test(expected=ParseException.class)
	public void testLeadingEqualsNotAllowed() {
		assertCreateParsesToIdAndValue("= 0", null, 0);
	}

	@Test(expected=ParseException.class)
	public void testJustEqualsNotAllowed() {
		assertCreateParsesToIdAndValue("=", null, 0);
	}
	
	@Test
	public void testParsesToExitCommand() {
		assertThat(parse("exit"), instanceOf(Exit.class));
	}
	
	/// ------------- Helper methods --------------- /// 
	
	private ICommand parse(String input) {
		return CommandParser.parse(input);
	}

	private void assertCreateParsesToIdAndValue(String input, String id,
			long value) {
		CreateSignal cmd = (CreateSignal) parse(input);
		assertEquals(id, cmd.getId());
		assertEquals(value, cmd.getInitialValue());
	}

}
