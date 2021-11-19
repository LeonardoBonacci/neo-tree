package fourj.model;

import static fourj.UglyHelper.plabel;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Product extends Base {

	public Product(Node productNode) {
		super(productNode);
	}

	public Product(GraphDatabaseService db, JsonNode json) {
		super(db, json);
	}

	@Override
	Node thisNode(Transaction tx, JsonNode json) {
		Node n = tx.findNode(plabel, "id", json.get("id").textValue()); // update
		if (n == null) { // + insert
			n = tx.createNode(plabel);
		}
		
		// = upsert
		for (String prop : n.getAllProperties().keySet()) {
			n.removeProperty(prop);
		}

		n.setProperty("name", json.get("name").textValue());
		n.setProperty("id", json.get("id").textValue());
		
		if (json.get("parentId") != null) {
			n.setProperty("parentId", json.get("parentId").textValue());
		}
		
		try {
			n.setProperty("jsonString", new ObjectMapper().writeValueAsString(json));
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
	
	public Product addHierarchy(Stream<Hierarchy> h) {	
		parent = h.reduce(null, ((partial, element) -> {
				element.setParent(partial);
				return element;
			}));
		return this;
	}
}