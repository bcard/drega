package org.bcard;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		
		String json = g0.toJson();
		
		JSONAssert.assertEquals(
				"{ " +
				  "id: '0', " +
				  "dependencies: [" +
				    "{ " +
				      "id: '0'" +
				    "} " +
				  "] " +
				"}", json, false);
	}

	
	private static String id(int value) {
		return Integer.toString(value);
	}
}
