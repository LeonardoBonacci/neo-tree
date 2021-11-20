package fourj;

import static fourj.QueriesHelper.asJsonNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.io.fs.FileUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import fourj.model.Hierarchy;
import fourj.model.Product;

public class QueriesTest {

	private static final Path databaseDirectory = Path.of("target/java-query-db");

	GraphDatabaseService db; 
	DatabaseManagementService managementService;

	@BeforeEach
	public void init() {
		managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
		db = managementService.database(DEFAULT_DATABASE_NAME);
	}
	
	@AfterEach
	public void tini() {
		managementService.shutdown();

		try {
			FileUtils.deleteDirectory(databaseDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@RepeatedTest(5)
	public void one(TestInfo testInfo) {
		insertHData(db);
		var hResult = Queries.query(Queries.hMatch, ImmutableMap.of("id", "n1"), db);
		var expected = "{name:\"my n1 up\",jsonString:\"{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\"}\",id:\"n1\",parentId:\"root\"}";
		assertTrue(hResult.contains(expected));
		
		var pResult = Queries.pquery(Queries.pMatch, ImmutableMap.of("id", "p1"), db);
		expected = "{\"id\":\"p1\",\"name\":\"my p1 up\",\"parentId\":\"n1\",\"parent\":{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\",\"parent\":{\"id\":\"root\",\"name\":\"my root up\"}}}";
		assertEquals(expected, pResult);

		var subtreeResult = Queries.query(Queries.subtreeMatch, ImmutableMap.of("id", "n1"), db);
		System.out.println(subtreeResult);
	}
	
	private void insertHData(final GraphDatabaseService db) {
		var pnodes = Lists.newArrayList(
				asJsonNode("hroot.json"),
				asJsonNode("hn11.json"),
				asJsonNode("hn12.json"),
				asJsonNode("hn2.json"),
				asJsonNode("hn21.json"),
				asJsonNode("hn1.json")
		);		

		Collections.shuffle(pnodes);
		pnodes.stream().forEach(n -> new Hierarchy(db, n));

		////////////////////////////

		var pleaves = Lists.newArrayList(
				asJsonNode("p1.json"),
				asJsonNode("p11.json")
		);		

		Collections.shuffle(pleaves);
		pleaves.stream().forEach(n -> new Product(db, n));

		////////////////////////////

		var upnodes = Lists.newArrayList(
				asJsonNode("hroot-up.json"),
				asJsonNode("hn1-up.json")
//				asJsonNode("hn21-up.json")
		);		

		Collections.shuffle(upnodes);
		upnodes.stream().forEach(n -> new Hierarchy(db, n));

		new Product(db, asJsonNode("p1-up.json")).getUnderlyingNode();
	}
}
