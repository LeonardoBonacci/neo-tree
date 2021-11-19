package fourj;

import static fourj.UglyHelper.hlabel;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fourj.Job.RelTypes;

public class Hierarchy {
	static final String NAME = "name";

	private final GraphDatabaseService databaseService;
	private final Node underlyingNode;
	private Hierarchy parent;

	Hierarchy(Node hNode) {
		this.underlyingNode = hNode;
		// Better an ugly face than an ugly mind. 
		this.databaseService = null;
	}

	Hierarchy(GraphDatabaseService db, JsonNode json) {
		this.databaseService = db;

		try (Transaction tx = db.beginTx()) {
			Node hnode = hierarchyNode(tx, json);
			Node parent = parentalNode(tx, json);
			parentalRelationship(hnode, parent);

			tx.commit();
			this.underlyingNode = hnode;
		}	
	}

	private Node hierarchyNode(Transaction tx, JsonNode json) {
		Node n = tx.findNode(hlabel, "id", json.get("id").textValue()); // update
		if (n == null) { // + insert
			n = tx.createNode(hlabel);
		}
		
		// = upsert
		for (String prop : n.getAllProperties().keySet()) {
			n.removeProperty(prop);
		}

		n.setProperty("name", json.get("name").textValue());
		n.setProperty("id", json.get("id").textValue());
		
		// root or no root?
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
		Node parent = tx.findNode(hlabel, "id", parentId);
		
		// if parent does not exist yet create a tmp node
		if (parent == null) {
			parent = tx.createNode(hlabel);
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
		if (pnode.getProperty("parentId") != (String)r.getEndNode().getProperty("id")) {
			r.delete();
			return pnode.createRelationshipTo(parent, RelTypes.PARENT);
		}
		
		// else relationship the same leave alone
		return r;
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
		return o instanceof Hierarchy && underlyingNode.equals(((Hierarchy) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Hierarchy[" + underlyingNode.getAllProperties() + " with |"+ parent +"|]";
	}
	
	public void setParent(Hierarchy p) {
		this.parent = p;
	}
}