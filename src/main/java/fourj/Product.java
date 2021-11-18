package fourj;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class Product {
	static final String NAME = "name";

	private GraphDatabaseService databaseService;
	private Transaction transaction;
	private final Node underlyingNode;
	private Hierarchy parent;

	Product(Node productNode) {
		this.underlyingNode = productNode;
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