package fourj.model;

import static fourj.UglyHelper.hlabel;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.databind.JsonNode;

import fourj.Job.RelTypes;

public abstract class Base {

	protected static final String JSON_STRING = "jsonString";

	protected final Node underlyingNode;
	protected Hierarchy parent;

	Base(Node hNode) {
		this.underlyingNode = hNode;
	}

	Base(GraphDatabaseService db, JsonNode json) {
		try (Transaction tx = db.beginTx()) {
			Node node = thisNode(tx, json);
			Node parent = parentalNode(tx, json);
			parentalRelationship(node, parent);

			tx.commit();
			this.underlyingNode = node;
		}	
	}

	abstract Node thisNode(Transaction tx, JsonNode json);
	
	private Node parentalNode(Transaction tx, JsonNode json) {
		// if root then nothing
		if (json.get("parentId") == null) {
			return null;
		}

		String parentId = json.get("parentId").textValue();
		Node parent = tx.findNode(hlabel, "id", parentId);
		
		// if parent does not exist yet create a tmp node
		if (parent == null) {
			parent = tx.createNode(hlabel);
			parent.setProperty("tmp", true);
		}
		
		return parent;
	}
	
	private Relationship parentalRelationship(Node node, Node parent) {
		// if root then nothing
		if (parent == null) {
			return null;
		}

		// if no relationship then create one
		Relationship r = node.getSingleRelationship(RelTypes.PARENT, Direction.OUTGOING);
		if (r == null) {
			return node.createRelationshipTo(parent, RelTypes.PARENT);
		}
		
		// if parent not the same update (= delete + create)
		if (node.getProperty("parentId") != (String)r.getEndNode().getProperty("id")) {
			r.delete();
			return node.createRelationshipTo(parent, RelTypes.PARENT);
		}
		
		// else relationship the same leave alone
		return r;
	}
	
	public Node getUnderlyingNode() {
		return underlyingNode;
	}

	public String asJsonString() {
		return (String) underlyingNode.getProperty(JSON_STRING);
	}

	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Base && underlyingNode.equals(((Base) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Hierarchy[" + underlyingNode.getAllProperties() + " with |"+ parent +"|]";
	}
	
	public void setParent(Hierarchy p) {
		this.parent = p;
	}
}