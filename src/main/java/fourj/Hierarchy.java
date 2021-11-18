package fourj;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class Hierarchy {
	static final String NAME = "name";

	private GraphDatabaseService databaseService;
	private Transaction transaction;
	private final Node underlyingNode;
	private Hierarchy parent;

	Hierarchy(Object hNode) {
		this.underlyingNode = (Node)hNode;
	}

	Hierarchy(Node hNode) {
		this.underlyingNode = hNode;
	}

	Hierarchy(GraphDatabaseService databaseService, Transaction transaction, Node hNode) {
		this.databaseService = databaseService;
		this.transaction = transaction;
		this.underlyingNode = hNode;
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