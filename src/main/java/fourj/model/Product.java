package fourj.model;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fourj.QueriesHelper;

public class Product extends Base {

	public Product(Node productNode) {
		super(productNode);
	}

	public Product(GraphDatabaseService db, JsonNode json) {
		super(db, json);
	}

	@Override
	Node thisNode(Transaction tx, JsonNode json) {
		Node n = tx.findNode(plabel, ID, json.get(ID).textValue()); // update
		if (n == null) { // + insert
			n = tx.createNode(plabel);
		}
		
		// = upsert
		for (String prop : n.getAllProperties().keySet()) {
			n.removeProperty(prop);
		}

		n.setProperty(NAME, json.get(NAME).textValue());
		n.setProperty(ID, json.get(ID).textValue());
		
		if (json.get(PARENT_ID) != null) {
			n.setProperty(PARENT_ID, json.get(PARENT_ID).textValue());
		}
		
		try {
			n.setProperty(JSON_STRING, new ObjectMapper().writeValueAsString(json));
			return n;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return n;
		} 
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