package fourj.model;

import static fourj.UglyHelper.hlabel;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Hierarchy extends Base {

	public Hierarchy(Node hNode) {
		super(hNode);
	}

	public Hierarchy(GraphDatabaseService db, JsonNode json) {
		super(db, json);
	}

	@Override
	Node thisNode(Transaction tx, JsonNode json) {
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
	@Override
	public boolean equals(Object o) {
		return o instanceof Hierarchy && underlyingNode.equals(((Hierarchy) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Hierarchy[" + underlyingNode.getAllProperties() + " with |"+ parent +"|]";
	}
}