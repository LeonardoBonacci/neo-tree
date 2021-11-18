package fourj;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;

public class Hierarchy {

	private static final Path databaseDirectory = Path.of("target/java-query-db");
	String resultString;
	String columnsString;
	String nodeResult;
	String rows = "";

	private enum RelTypes implements RelationshipType {
		PARENT
	}

	public static void main(String[] args) {
		Hierarchy javaQuery = new Hierarchy();
		javaQuery.run();
	}

	void run() {
		clearDbPath();

		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
		GraphDatabaseService db = managementService.database(DEFAULT_DATABASE_NAME);

		insertHData(db);
		
		Map<String,Object> params = new HashMap<>();
		params.put( "id", "n1" );

//		String q =
//		    "MATCH (n {id: 'n1'})-[*]-(r:Hierarchy)" + "\n" +
//		    "WHERE r.parentId IS NULL" + "\n" +
//		    "RETURN n, n.name";
		String q =
			    "MATCH (n {id: $id})-[*]-(r:Hierarchy)" + "\n" +
			    "WHERE r.parentId IS NULL" + "\n" +
			    "RETURN n, n.name";

		query(q, params, db);
		
		managementService.shutdown();
	}

	private void query(String q, Map<String,Object> params, GraphDatabaseService db) {
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				for (Entry<String, Object> column : row.entrySet()) {
					rows += column.getKey() + ": " + column.getValue() + "; ";
				}
				rows += "\n";
//				System.out.println(rows);
			}
		}

		System.out.println("------------");
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {

			Iterator<Node> n_column = result.columnAs("n");
			n_column.forEachRemaining(node -> nodeResult = node + ": " + node.getProperty("name"));
//			System.out.println(nodeResult);
			System.out.println("------------");

			List<String> columns = result.columns();

			columnsString = columns.toString();
			resultString = tx.execute(q, params).resultAsString();
			System.out.println(resultString);
		}

	}

	private void insertHData(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			Node root = tx.createNode(Label.label("Hierarchy"));
			root.setProperty("name", "my node");
			root.setProperty("id", "root");

			Node n1 = tx.createNode(Label.label("Hierarchy"));
			n1.setProperty("name", "first layer");
			n1.setProperty("id", "n1");
			n1.setProperty("parentId", "root");

			n1.createRelationshipTo(root, RelTypes.PARENT);

			Node n11 = tx.createNode(Label.label("Hierarchy"));
			n11.setProperty("name", "second layer");
			n11.setProperty("id", "n2");
			n11.setProperty("parentId", "n1");

			n11.createRelationshipTo(n1, RelTypes.PARENT);

			tx.commit();
		}

	}

	private void clearDbPath() {
		try {
			FileUtils.deleteDirectory(databaseDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}