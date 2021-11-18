package fourj;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.exceptions.KernelException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.kernel.api.Procedures;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.impl.core.NodeEntity;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import com.google.common.collect.ImmutableMap;

public class Job {

	private static final Path databaseDirectory = Path.of("target/java-query-db");
	String resultString;
	String columnsString;
	String nodeResult;
	String rows = "";

	private enum RelTypes implements RelationshipType {
		PARENT
	}

	public static void main(String[] args) {
		Job javaQuery = new Job();
		javaQuery.run();
	}

	void run() {
		clearDbPath();

		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
		GraphDatabaseService db = managementService.database(DEFAULT_DATABASE_NAME);

		insertHData(db);

		String hMatch = "MATCH (h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n" + "WHERE r.parentId IS NULL" + "\n"
				+ "RETURN h, h.name";

		query(hMatch, ImmutableMap.of("id", "n1"), db);
		System.out.println("------------");

		String pMatch = "MATCH path = (p:Product {id: $id})-[*]->(r:Hierarchy)" + "\n" + "WHERE r.parentId IS NULL"
				+ "\n" + "RETURN nodes(path) AS path";

		pquery(pMatch, ImmutableMap.of("id", "p1"), db);
		System.out.println("------------");

		String subtreeMatch = "MATCH (p:Product)-[*]->(h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n"
				+ "WHERE r.parentId IS NULL" + "\n" + "RETURN p, p.name";

		query(subtreeMatch, ImmutableMap.of("id", "n1"), db);

		managementService.shutdown();
	}

	private void query(String q, Map<String, Object> params, GraphDatabaseService db) {
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

		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {

//			Iterator<Node> n_column = result.columnAs("n");
//			n_column.forEachRemaining(node -> nodeResult = node + ": " + node.getProperty("name"));
//			System.out.println(nodeResult);
//			System.out.println("------------");

			List<String> columns = result.columns();

			columnsString = columns.toString();
			resultString = tx.execute(q, params).resultAsString();
			System.out.println(resultString);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void pquery(String q, Map<String, Object> params, GraphDatabaseService db) {
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {
			Map<String, Object> row = result.next();
			ArrayList asList = (ArrayList) row.get("path");

			Product p = new Product((NodeEntity) asList.get(0));
			Stream<Hierarchy> h = asList.subList(1, asList.size()).stream()
						.map(n -> new Hierarchy((NodeEntity)n));

			p.addHierarchy(h);
			System.out.println(p);
		}

	}

	private void insertHData(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			Node root = new Hierarchy(db, tx, UglyHelper.asJsonNode("hroot.json")).getUnderlyingNode();
			Node n1 = new Hierarchy(db, tx, UglyHelper.asJsonNode("hn1.json")).getUnderlyingNode();
			n1.createRelationshipTo(root, RelTypes.PARENT);

			Node n11 = new Hierarchy(db, tx, UglyHelper.asJsonNode("hn11.json")).getUnderlyingNode();
			n11.createRelationshipTo(n1, RelTypes.PARENT);

			Node n12 = new Hierarchy(db, tx, UglyHelper.asJsonNode("hn12.json")).getUnderlyingNode();
			n12.createRelationshipTo(n1, RelTypes.PARENT);

			Node n2 = new Hierarchy(db, tx, UglyHelper.asJsonNode("hn2.json")).getUnderlyingNode();
			n2.createRelationshipTo(root, RelTypes.PARENT);

			Node n21 = new Hierarchy(db, tx, UglyHelper.asJsonNode("hn21.json")).getUnderlyingNode();
			n21.createRelationshipTo(n2, RelTypes.PARENT);

			////////////////////////////

			Node p1 = new Product(db, tx, UglyHelper.asJsonNode("p1.json")).getUnderlyingNode();
			p1.createRelationshipTo(n1, RelTypes.PARENT);

			Node p11 = new Product(db, tx, UglyHelper.asJsonNode("p11.json")).getUnderlyingNode();
			p11.createRelationshipTo(n11, RelTypes.PARENT);

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

	public static void registerProcedure(GraphDatabaseService db, Class<?>... procedures) throws KernelException {
		Procedures proceduresService = ((GraphDatabaseAPI) db).getDependencyResolver()
				.resolveDependency(Procedures.class);
//		for (Class<?> procedure : procedures) {
//			proceduresService.registerProcedure(procedure, true);
//			proceduresService.registerFunction(procedure, true);
//			proceduresService.registerAggregationFunction(procedure, true);
//		}
	}
}