package org.bcard.drega.signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.fest.assertions.api.Assertions.*;

import java.util.List;

import org.bcard.drega.signal.SignalChain;
import org.bcard.drega.signal.SignalGraph;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Some tests for the {@link SignalGraph} class.
 * 
 * @author bcard
 *
 */
public class SignalGraphTest {
	
	@Test
	public void testCreateSingleGraph() {
		SignalGraph graph = new SignalGraph(id(1));
		assertEquals(id(1), graph.getId());
		assertEquals(0, graph.getDependentSignals().size());
	}
	
	@Test
	public void testAddDependencies() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		
		assertTrue(g1.getDependentSignals().contains(g0));
	}
	
	@Test
	public void testGraphContainsIdOfItself() {
		SignalGraph g0 = new SignalGraph(id(0));
		
		assertTrue(g0.containsId(id(0)));
	}
	
	@Test
	public void testGraphContainsDependentId() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		
		assertTrue(g1.containsId(id(0)));
	}
	
	@Test
	public void testGraphContainsDeepDependentId() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1));
		SignalGraph g2 = new SignalGraph(id(2), g0, g1);
		
		SignalGraph g3 = new SignalGraph(id(3), g1);
		SignalGraph g4 = new SignalGraph(id(4), g3);
		
		/*
		 * Graph looks like:
		 * 
		 * g0   g1
		 *  \  / \
		 *   g2   g3
		 *         \
		 *          g4
		 */
		
		assertTrue(g4.containsId(id(1)));
		assertTrue(g2.containsId(id(1)));
		assertTrue(g2.containsId(id(0)));
	}
	
	@Test
	public void testEquals() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1));
		SignalGraph g2 = new SignalGraph(id(2), g0, g1);
		
		SignalGraph g3 = new SignalGraph(id(0));
		SignalGraph g4 = new SignalGraph(id(1));
		SignalGraph g5 = new SignalGraph(id(2), g3, g4);
		
		assertEquals(g2, g5);
	}
	
	@Test
	public void testHashcode() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1));
		SignalGraph g2 = new SignalGraph(id(2), g0, g1);
		
		SignalGraph g3 = new SignalGraph(id(0));
		SignalGraph g4 = new SignalGraph(id(1));
		SignalGraph g5 = new SignalGraph(id(2), g3, g4);
		
		assertEquals(g2.hashCode(), g5.hashCode());
	}
	
	@Test
	public void testJsonNoDependencies() throws JSONException {
		SignalGraph g0 = new SignalGraph(id(0));
		String json = g0.toJson();
		
		JSONAssert.assertEquals("{ id: '0' }", json, false);
	}
	
	@Test
	public void testJsonOneDependency() throws Exception {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		
		String json = g1.toJson();
		
		JSONAssert.assertEquals(
				"{ " +
				  "id: '1', " +
				  "dependencies: [" +
				    "{ " +
				      "id: '0'" +
				    "} " +
				  "] " +
				"}", json, false);
	}
	
	@Test
	public void testJsonNestedDependencies() throws Exception {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		SignalGraph g2 = new SignalGraph(id(2), g1);
		
		String json = g2.toJson();
		
		JSONAssert.assertEquals(
				"{ " +
				  "id: '2', " +
				  "dependencies: [" +
				    "{ " +
				      "id: '1'," +
				      "dependencies: [" +
					    "{ " +
					      "id: '0'" +
					    "} " +
					  "] " +
				    "} " +
				  "] " +
				"}", json, false);
	}
	
	@Test
	public void testJsonSimpleDeserialize() {
		SignalGraph g0 = new SignalGraph(id(0));
		String json = g0.toJson();
		
		SignalGraph copy = SignalGraph.fromJson(json);
		
		assertEquals(g0, copy);
	}
	
	@Test
	public void testJsonDeserializeWithDependencies() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1));
		SignalGraph g2 = new SignalGraph(id(2), g0, g1);
		String json = g2.toJson();
		
		SignalGraph copy = SignalGraph.fromJson(json);
		
		assertEquals(g2, copy);
	}
	
	@Test
	public void testJsonDeserializeWithMultiLevel() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1));
		SignalGraph g2 = new SignalGraph(id(2));
		SignalGraph g3 = new SignalGraph(id(3), g2);
		
		SignalGraph g4 = new SignalGraph(id(4), g0, g1, g3);
		String json = g4.toJson();
		SignalGraph copy = SignalGraph.fromJson(json);
		
		assertEquals(g4, copy);
	}
	
	@Test
	public void testAllPathsWithSingleDepJustReturnsThatDep() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		
		List<SignalChain> allPaths = g1.allPaths();
		assertThat(allPaths).hasSize(1);
	}
	
	@Test
	public void testAllPathsStick() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		SignalGraph g2 = new SignalGraph(id(2), g1);
		SignalGraph g3 = new SignalGraph(id(3), g2);
		
		List<SignalChain> allPaths = g3.allPaths();
		assertThat(allPaths).hasSize(1);
	}
	
	
	@Test
	public void testAllPathsBinaryTree() {
		/*
		 *       0
		 *      / \
		 *     /   \
		 *    1     4
		 *   / \   / \
		 *  2   3 5   6
		 */

		SignalGraph g2 = new SignalGraph(id(2));
		SignalGraph g3 = new SignalGraph(id(3));
		SignalGraph g5 = new SignalGraph(id(5));
		SignalGraph g6 = new SignalGraph(id(6));
		
		SignalGraph g1 = new SignalGraph(id(1), g2, g3);
		SignalGraph g4 = new SignalGraph(id(4), g5, g6);
		
		SignalGraph g0 = new SignalGraph(id(0), g1, g4);
		
		List<SignalChain> allPaths = g0.allPaths();
		assertThat(allPaths).hasSize(4);
	}
	
	@Test
	public void testAllPathsRoots() {
		/*
		 *       0
		 *       |
		 *       1
		 *      / \
		 *     2   3
		 */

		SignalGraph g2 = new SignalGraph(id(2));
		SignalGraph g3 = new SignalGraph(id(3));
		
		SignalGraph g1 = new SignalGraph(id(1), g2, g3);
		
		SignalGraph g0 = new SignalGraph(id(0), g1);
		
		List<SignalChain> allPaths = g0.allPaths();
		assertThat(allPaths).hasSize(2);
	}
	
	@Test
	public void testOrderOfPaths() {
		SignalGraph g0 = new SignalGraph(id(0));
		SignalGraph g1 = new SignalGraph(id(1), g0);
		SignalGraph g2 = new SignalGraph(id(2), g1);
		
		List<SignalChain> allPaths = g2.allPaths();
		SignalChain expected = new SignalChain(g0);
		expected.chain(g1);
		
		assertEquals(expected, allPaths.get(0));
	}
	
	private static String id(int value) {
		return Integer.toString(value);
	}
}
