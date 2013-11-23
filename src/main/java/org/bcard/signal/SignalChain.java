package org.bcard.signal;

import java.util.ArrayList;
import java.util.List;

/**
 * A chain of different graphs. The chain is updated as events propagate through
 * signals. This class only stores the ID of the signal that it's passed through
 * not the entire graph. This is to make this class more space efficient. The
 * entire chain is not necessary because each signal has the graphs of all of
 * it's dependencies.
 * 
 * @author bcard
 * 
 */
public class SignalChain {

	private SignalGraph graph;

	public SignalChain(SignalGraph head) {
		graph = new SignalGraph(head.getId());
	}

	/**
	 * Returns all of the conflicts that this chain has in common with the
	 * {@code other} chain.
	 * 
	 * @param other
	 * @return
	 */
	public List<String> getConflicts(SignalChain other) {
		List<String> theseValues = toList();
		List<String> otherValues = other.toList();

		theseValues.retainAll(otherValues);
		return theseValues;
	}

	/**
	 * Adds a graph to the head of this chain of graphs.
	 * 
	 * @param link
	 *            the graph to add
	 */
	public void chain(SignalGraph link) {
		graph = new SignalGraph(link.getId(), graph);
	}

	/**
	 * Returns a JSON representation of this object. Use the
	 * {@link #fromJson(String)} method to convert this representation back into
	 * a {@link SignalChain}.
	 * 
	 * @return a JSON representation of this object
	 */
	public String toJson() {
		return graph.toJson();
	}

	/**
	 * Creates a new {@link SignalChain} from a JSON representation. This can be
	 * used in conjunction with the {@link #toJson()} method to
	 * serialize/deserialize this object.
	 * 
	 * @param json
	 *            a serialized {@link SignalChain}
	 * @return the JSON converted to a new {@link SignalChain}
	 */
	public static SignalChain fromJson(String json) {
		SignalGraph graph = SignalGraph.fromJson(json);
		SignalChain chain = new SignalChain(graph);
		chain.graph = graph;
		return chain;
	}

	/**
	 * Converts the {@link #graph} to a list of {@code String} IDs
	 * 
	 * @return a {@link List} of IDs
	 */
	private List<String> toList() {
		List<String> list = new ArrayList<>();
		SignalGraph current = graph;
		while (current != null) {
			list.add(current.getId());
			List<SignalGraph> depSignals = current.getDependentSignals();
			if (!depSignals.isEmpty()) {
				current = depSignals.get(0);
			} else {
				current = null;
			}
		}

		return list;
	}
	
	@Override
	public String toString() {
		return toList().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((graph == null) ? 0 : graph.hashCode());
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
		SignalChain other = (SignalChain) obj;
		if (graph == null) {
			if (other.graph != null)
				return false;
		} else if (!graph.equals(other.graph))
			return false;
		return true;
	}

	
}
