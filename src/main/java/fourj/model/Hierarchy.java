package fourj.model;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.databind.JsonNode;

public class Hierarchy extends Base {

	public Hierarchy(Node hNode) {
		super(hNode);
	}

	public Hierarchy(GraphDatabaseService db, JsonNode json) {
		super(db, json);
	}

	@Override
	Label label() {
		return hlabel;
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