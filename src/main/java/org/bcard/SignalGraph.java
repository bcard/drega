package org.bcard;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency graph for observables. This class allows us to find common
 * ancestors which can be used to avoid glitches.
 * 
 * @author bcard
 * 
 */
public class SignalGraph {

	List<SignalGraph> upstreamDependencies = new ArrayList<SignalGraph>();

	private final String id;

	/**
	 * Creates a new {@link SignalGraph}. This graph will use the following
	 * {@code id} as its id and will have the given graphs as upstream
	 * dependencies if applicable (assuming data flows upstream to downstream).
	 * 
	 * @param id
	 *            the id of this graph
	 * @param upstreamDependencies
	 *            this graph's upstream dependencies.
	 */
	public SignalGraph(String id, SignalGraph... upstreamDependencies) {
		this.id = id;
		for (SignalGraph graph : upstreamDependencies) {
			this.upstreamDependencies.add(graph);
		}
	}

	public String getId() {
		return id;
	}

	/**
	 * Return all of the {@link SignalGraph} that this observable is dependent
	 * on. This can also be thought of as all <emphasis>upstream</emphasis>
	 * dependencies, i.e. observables that this observable is receiving values
	 * from.
	 * 
	 * @return all
	 */
	public List<SignalGraph> getDependentSignals() {
		return new ArrayList<SignalGraph>(upstreamDependencies);
	}

	/**
	 * Returns {@code true} if this graph has the given ID or if the ID is
	 * contained somewhere in the dependency graph.
	 * 
	 * @param id2
	 *            the ID to search for
	 * @return {@code true} if the id appears somewhere in this graph,
	 *         {@code false} otherwise
	 */
	public boolean containsId(String id2) {
		if (id2.equals(id)) {
			return true;
		} else {
			for (SignalGraph graph : upstreamDependencies) {
				boolean contains = graph.containsId(id2);
				if (contains) {
					return contains;
				}
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((upstreamDependencies == null) ? 0 : upstreamDependencies
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignalGraph other = (SignalGraph) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (upstreamDependencies == null) {
			if (other.upstreamDependencies != null)
				return false;
		} else if (!upstreamDependencies.equals(other.upstreamDependencies))
			return false;
		return true;
	}

	/**
	 * Converts this graph to a JSON string. Use the {@link #fromJson(String)}
	 * method to trun this string back into a {@link SignalGraph} object.
	 * 
	 * @return JSON representation of this object
	 */
	public String toJson() {
		return "{id:'0'}";
	}
	
	public static SignalGraph fromJson(String json) {
		throw new UnsupportedOperationException();
	}

}
