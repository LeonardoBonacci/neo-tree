package fourj;

import static fourj.UglyHelper.asJsonNode;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.impl.core.NodeEntity;

import com.google.common.collect.ImmutableMap;

import fourj.model.Hierarchy;
import fourj.model.Product;

public class Job {

	private static final Path databaseDirectory = Path.of("target/java-query-db");
	String resultString;

	public enum RelTypes implements RelationshipType {
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

		// @formatter:off
		String hMatch = 
				"MATCH (h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n" + "WHERE r.parentId IS NULL" + "\n"
			  + "RETURN h, h.name";
		// @formatter:on

		query(hMatch, ImmutableMap.of("id", "n1"), db);
		System.out.println("------------");

		// @formatter:off
		String pMatch = 
				"MATCH path = (p:Product {id: $id})-[*]->(r:Hierarchy)" + "\n" + "WHERE r.parentId IS NULL" + "\n" 
			  + "RETURN nodes(path) AS path";
		// @formatter:on

		pquery(pMatch, ImmutableMap.of("id", "p1"), db);
		System.out.println("------------");

		// @formatter:off
		String subtreeMatch = 
				"MATCH (p:Product)-[*]->(h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n"
			  + "WHERE r.parentId IS NULL" + "\n" + "RETURN p, p.name";
		// @formatter:on

		query(subtreeMatch, ImmutableMap.of("id", "n1"), db);

		managementService.shutdown();
	}

	private void query(String q, Map<String, Object> params, GraphDatabaseService db) {
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {
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
			Stream<Hierarchy> h = asList.subList(1, asList.size()).stream().map(n -> new Hierarchy((NodeEntity) n));

			p.addHierarchy(h);
			System.out.println(p);
		}

	}

	private void insertHData(GraphDatabaseService db) {
		new Hierarchy(db, asJsonNode("hroot.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn1.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn11.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn12.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn2.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn21.json")).getUnderlyingNode();

		////////////////////////////

		new Product(db, asJsonNode("p1.json")).getUnderlyingNode();
		new Product(db, asJsonNode("p11.json")).getUnderlyingNode();

		////////////////////////////
		
		new Hierarchy(db, asJsonNode("hroot-up.json")).getUnderlyingNode();
		new Hierarchy(db, asJsonNode("hn1-up.json")).getUnderlyingNode();
		new Product(db, asJsonNode("p1-up.json")).getUnderlyingNode();
	}

	private void clearDbPath() {
		try {
			FileUtils.deleteDirectory(databaseDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}