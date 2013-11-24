package org.bcard.signal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for our {@link SignalChain} class.
 * 
 * @author bcard
 * 
 */
public class SignalChainTest {
	
	private SignalGraph a;
	private SignalGraph b;
	private SignalGraph c;
	
	@Before
	public void setup() {
		b = new SignalGraph("b");
		c = new SignalGraph("c");
		a = new SignalGraph("a", b, c);
		
	}
	
	@Test
	public void testSeparateChainsDoNotDependOnEachOther() {
		SignalChain chainB = new SignalChain(b);
		SignalChain chainC = new SignalChain(c);
		
		assertThat(chainB.getConflicts(chainC)).isEmpty();
	}
	
	@Test
	public void testChainDependsOnItself() {
		SignalChain chainB = new SignalChain(b);
		
		assertThat(chainB.getConflicts(chainB)).contains("b");
	}
	
	@Test
	public void testSingleConflict() {
		SignalChain chainB = new SignalChain(b);
		SignalChain chainC = new SignalChain(c);
		
		chainB.chain(a);
		chainC.chain(a);
		
		assertThat(chainB.getConflicts(chainC)).contains("a");
	}
	
	@Test
	public void testToJson() {
		SignalChain chainB = new SignalChain(b);
		chainB.chain(c);
		chainB.chain(a);
		
		String json = chainB.toJson();
		
		SignalChain recreated = SignalChain.fromJson(json);
		assertEquals(chainB, recreated);
	}
	
	@Test
	public void testEventCounter() {
		SignalChain chainA = new SignalChain(a, 0);
		assertEquals(0, chainA.getEventCounterFor(a));
	}
	
	@Test
	public void testChainWithEventCounter() {
		SignalChain chainA = new SignalChain(a, 0);
		chainA.chain(b, 2);
		
		assertEquals(2, chainA.getEventCounterFor(b));
	}

}
