package fourj;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Product {
	static final String NAME = "name";

	private final GraphDatabaseService databaseService;
	private final Transaction transaction;
	private final Node underlyingNode;
	private Hierarchy parent;

	Product(Node productNode) {
		this.underlyingNode = productNode;
		// Better an ugly face than an ugly mind. 
		this.databaseService = null;
		this.transaction = null;
	}

	Product(GraphDatabaseService db, Transaction tx, JsonNode json) {
		this.databaseService = db;
		this.transaction = tx;
		
		Node n = tx.createNode(Label.label("Product"));
		n.setProperty("name", json.get("name").textValue());
		n.setProperty("id", json.get("id").textValue());
		if (json.get("parentId") != null)
			n.setProperty("parentId", json.get("parentId").textValue());

		try {
			n.setProperty("jsonString", new ObjectMapper().writeValueAsString(json));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		this.underlyingNode = n;
	}

	Product(GraphDatabaseService databaseService, Transaction transaction, Node productNode) {
		this.databaseService = databaseService;
		this.transaction = transaction;
		this.underlyingNode = productNode;
	}

	protected Node getUnderlyingNode() {
		return underlyingNode;
	}

	public String getName() {
		return (String) underlyingNode.getProperty(NAME);
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