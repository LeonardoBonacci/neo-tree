package fourj.model;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.databind.JsonNode;

import fourj.QueriesHelper;

public class Product extends Base {

	public Product(Node productNode) {
		super(productNode);
	}

	public Product(GraphDatabaseService db, JsonNode json) {
		super(db, json);
	}

	@Override
	Label label() {
		return plabel;
	}
	
	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Product && underlyingNode.equals(((Product) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Product[" + underlyingNode.getAllProperties() + " with |"+ parent +"|]";
	}
	
	public Product addHierarchyPath(Stream<Hierarchy> h) {	
		parent = h.reduce(null, ((partial, element) -> {
			if (partial == null)
				return element;

			partial.setParent(element);
			partial.getUnderlyingNode()
				.setProperty(JSON_STRING, QueriesHelper.jsonParent(partial, element));
			return partial;
		}));
		
		getUnderlyingNode()
			.setProperty(JSON_STRING, QueriesHelper.jsonParent(this, parent));

		return this;
	}
}