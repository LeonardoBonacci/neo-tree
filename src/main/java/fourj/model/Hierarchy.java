package fourj.model;

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
		Node n = tx.findNode(hlabel, ID, json.get(ID).textValue()); // update
		if (n == null) { // + insert
			n = tx.createNode(hlabel);
		}
		
		// = upsert
		for (String prop : n.getAllProperties().keySet()) {
			n.removeProperty(prop);
		}

		n.setProperty(ID, json.get(ID).textValue());
		n.setProperty(NAME, json.get(NAME).textValue());
		
		// root or no root?
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
	public boolean equals(Object o) {
		return o instanceof Hierarchy && underlyingNode.equals(((Hierarchy) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Hierarchy[" + underlyingNode.getAllProperties() + " with |"+ parent +"|]";
	}
}