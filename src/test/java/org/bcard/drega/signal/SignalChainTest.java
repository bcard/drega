package org.bcard.drega.signal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.bcard.drega.signal.SignalChain;
import org.bcard.drega.signal.SignalGraph;
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
	private SignalGraph d;
	
	@Before
	public void setup() {
		b = new SignalGraph("b");
		c = new SignalGraph("c");
		a = new SignalGraph("a", b, c);
		d = new SignalGraph("d");
	}
	
	@Test
	public void testSeparateChainsDoNotDependOnEachOther() {
		SignalChain chainB = new SignalChain(b);
		SignalChain chainC = new SignalChain(c);
		
		assertThat(chainB.getConflicts(chainC)).isEmpty();
	}
	
	@Test
	public void testChainDoesNotDependOnItself() {
		SignalChain chainB = new SignalChain(b);
		
		assertThat(chainB.getConflicts(chainB)).isEmpty();
	}
	
	@Test
	public void testSingleConflict() {
		SignalChain chainB = new SignalChain(a);
		SignalChain chainC = new SignalChain(a);
		
		chainB.chain(b);
		chainC.chain(c);
		
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
		assertEquals(0, chainA.getEventCounterFor(a.getId()));
	}
	
	@Test
	public void testChainWithEventCounter() {
		SignalChain chainA = new SignalChain(a, 0);
		chainA.chain(b, 2);
		
		assertEquals(2, chainA.getEventCounterFor(b.getId()));
	}
	
	@Test
	public void testConflictsWithOneNullAreConflicts() {
		SignalChain chainA = new SignalChain(c);
		chainA.chain(a);
		chainA.chain(b);

		
		SignalChain chainA2 = new SignalChain(c);
		chainA2.chain(a);
		
		/*
		 * We have:
		 * c -> a -> b
		 * c -> a
		 * 
		 * one branches to a null and the other doesn't
		 */
		
		assertThat(chainA.getConflicts(chainA2)).contains("a");
	}
	
	@Test
	public void testConflictsWithNoBranchingAreNotConflicts() {
		SignalChain chainA = new SignalChain(c);
		chainA.chain(a);

		
		SignalChain chainA2 = new SignalChain(c);
		chainA2.chain(a);
		
		/*
		 * We have:
		 * c -> a
		 * c -> a
		 * 
		 * because a doesn't 'branch' to a different value it's not a conflict
		 */
		
		assertThat(chainA.getConflicts(chainA2)).isEmpty();
	}
	
	@Test
	public void testConflictsWithBranchingAreConflicts() {
		SignalChain chainA = new SignalChain(c);
		chainA.chain(b);
		chainA.chain(a);
		
		SignalChain chainA2 = new SignalChain(c);
		chainA2.chain(d);
		chainA2.chain(a);
		
		/*
		 * We have:
		 * c -> b -> a
		 * c -> d -> a
		 * 
		 * because c does 'branch' to a different value it *is*
		 * a conflict, but a is still not a conflict
		 */
		
		assertThat(chainA.getConflicts(chainA2)).contains("c");
	}
	
	@Test
	public void testContainsWithSingleValue() {
		SignalChain chainC = new SignalChain(c);
		
		assertTrue(chainC.contains(c.getId()));
		assertFalse(chainC.contains(a.getId()));
	}
	
	@Test
	public void testContainsWithMultipleValues() {
		SignalChain chainC = new SignalChain(c);
		chainC.chain(a);
		chainC.chain(b);
		
		assertTrue(chainC.contains(c.getId()));
		assertTrue(chainC.contains(a.getId()));
		assertTrue(chainC.contains(b.getId()));
		assertFalse(chainC.contains(d.getId()));
	}

}
