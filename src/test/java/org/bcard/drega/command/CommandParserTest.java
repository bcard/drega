package org.bcard.drega.command;

import static org.junit.Assert.*;

import org.bcard.drega.command.BlockSignal;
import org.bcard.drega.command.CombineSymbols;
import org.bcard.drega.command.CommandParser;
import org.bcard.drega.command.CreateSignal;
import org.bcard.drega.command.Exit;
import org.bcard.drega.command.GlitchSignal;
import org.bcard.drega.command.ICommand;
import org.bcard.drega.command.Increment;
import org.bcard.drega.command.MapSignal;
import org.bcard.drega.command.ParseException;
import org.bcard.drega.command.PrintGraph;
import org.bcard.drega.command.PrintHelp;
import org.bcard.drega.command.PrintSignal;
import org.bcard.drega.signal.CombineOperator;
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
	
	@Test
	public void testParseIdReturnsPrintSignalCommand() {
		assertThat(parse("x1"), instanceOf(PrintSignal.class));
	}
	
	@Test
	public void testParseIdWithWhitespaceReturnsPrintSignalCommand() {
		assertThat(parse("x1 "), instanceOf(PrintSignal.class));
	}
	
	@Test(expected=ParseException.class)
	public void testNumbersNotMatchedAsVariables() {
		parse("1=1");
	}
	
	@Test
	public void testIncrementOperatorReturnsIncrementCommand() {
		assertThat(parse("x1++"), instanceOf(Increment.class));
	}
	
	@Test
	public void testSingleSymbolAssignment() {
		assertThat(parse("x=y"), instanceOf(MapSignal.class));
	}
	
	@Test
	public void testSingleSymbolAssignmentWithSapce() {
		assertThat(parse("x = y"), instanceOf(MapSignal.class));
	}
	
	@Test
	public void testGraphCommand() {
		assertThat(parse("graph x"), instanceOf(PrintGraph.class));
	}
	
	@Test
	public void testAddingSignals() {
		CombineSymbols command = (CombineSymbols)parse("x=y+z");
		
		assertEquals(CombineOperator.ADD, command.operator);
	}
	
	@Test
	public void testAddingSignalsWithSpaces() {
		CombineSymbols command = (CombineSymbols)parse("x = y + z");
		
		assertEquals(CombineOperator.ADD, command.operator);
	}
	
	@Test
	public void testSubtractingSignals() {
		CombineSymbols command = (CombineSymbols)parse("x=y-z");
		
		assertEquals(CombineOperator.SUBTRACT, command.operator);
	}
	
	@Test
	public void testSubtractingSignalsWithSpaces() {
		CombineSymbols command = (CombineSymbols)parse("x = y - z");
		
		assertEquals(CombineOperator.SUBTRACT, command.operator);
	}
	
	@Test
	public void testSubtractingSignalsWithSpacesTrimsInput() {
		CombineSymbols command = (CombineSymbols)parse("x = y - z");
		assertEquals("x", command.target);
		assertEquals("y", command.symbol1);
		assertEquals("z", command.symbol2);
	}
	
	@Test
	public void testAddingSignalsWithSpacesTrimsInput() {
		CombineSymbols command = (CombineSymbols)parse("x = y + z");
		assertEquals("x", command.target);
		assertEquals("y", command.symbol1);
		assertEquals("z", command.symbol2);
	}
	
	@Test
	public void testBlock() {
		assertThat(parse("block x"), instanceOf(BlockSignal.class));
	}
	
	@Test
	public void testUnBlock() {
		assertThat(parse("unblock x"), instanceOf(BlockSignal.class));
	}
	
	@Test
	public void testGlitch() {
		assertThat(parse("glitch x"), instanceOf(GlitchSignal.class));
	}
	
	@Test
	public void testNoGlitch() {
		assertThat(parse("noglitch x"), instanceOf(GlitchSignal.class));
	}
	
	@Test
	public void testHelpCommand() {
		assertThat(parse("help"), instanceOf(PrintHelp.class));
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
