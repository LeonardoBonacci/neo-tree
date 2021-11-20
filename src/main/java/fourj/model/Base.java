package fourj.model;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.databind.JsonNode;

import fourj.Queries.RelTypes;

public abstract class Base {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PARENT_ID = "parentId";
	public static final String PARENT = "parent";
	public static final String JSON_STRING = "jsonString";
	public static final String TMP = "tmp";

	public static final Label hlabel = Label.label("Hierarchy");
	public static final Label plabel = Label.label("Product");


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
		if (json.get(PARENT_ID) == null) {
			return null;
		}

		String parentId = json.get(PARENT_ID).textValue();
		Node parent = tx.findNode(hlabel, ID, parentId);
		
		// if parent does not exist yet create a tmp node
		if (parent == null) {
			parent = tx.createNode(hlabel);
			parent.setProperty(ID, parentId);
			parent.setProperty(TMP, true);
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
		if (r.getEndNode().hasProperty(TMP) // what an obscure condition..
			|| node.getProperty(PARENT_ID) != (String)r.getEndNode().getProperty(ID)) {
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