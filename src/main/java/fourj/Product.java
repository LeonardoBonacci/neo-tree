package fourj;

import java.util.stream.Stream;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fourj.Job.RelTypes;

public class Product {
	static final String NAME = "name";

	private static final Label plabel = Label.label("Product");

	private final GraphDatabaseService databaseService;
	private final Node underlyingNode;
	private Hierarchy parent;

	Product(Node productNode) {
		this.underlyingNode = productNode;
		// Better an ugly face than an ugly mind. 
		this.databaseService = null;
	}

	Product(GraphDatabaseService db, JsonNode json) {
		this.databaseService = db;

		try (Transaction tx = db.beginTx()) {
			Node pnode = productNode(tx, json);
			Node parent = parentalNode(tx, json);
			parentalRelationship(pnode, parent);

			tx.commit();
			this.underlyingNode = pnode;
		}	
	}

	private Node productNode(Transaction tx, JsonNode json) {
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
	
	private Node parentalNode(Transaction tx, JsonNode json) {
		// if root then nothing
		if (json.get("parentId") == null) {
			return null;
		}

		String parentId = json.get("parentId").textValue();
		Node parent = tx.findNode(Label.label("Hierarchy"), "id", parentId);

		// if parent does not exist yet create a tmp node
		if (parent == null) {
			parent = tx.createNode(Label.label("Hierarchy"));
			parent.setProperty("tmp", true);
		}
		
		return parent;
	}

	private Relationship parentalRelationship(Node pnode, Node parent) {
		// if root then nothing
		if (parent == null) {
			return null;
		}

		// if no relationship then create one
		Relationship r = pnode.getSingleRelationship(RelTypes.PARENT, Direction.OUTGOING);
		if (r == null) {
			return pnode.createRelationshipTo(parent, RelTypes.PARENT);
		}
		
		// if parent not the same update (= delete + create)
		if (parent.getProperty("parentId") != (String)r.getEndNode().getProperty("id")) {
			r.delete();
			return pnode.createRelationshipTo(parent, RelTypes.PARENT);
		}
		
		// else relationship the same leave alone
		return r;
	}

	Product(GraphDatabaseService databaseService, Node productNode) {
		this.databaseService = databaseService;
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