package fourj;

import static fourj.QueriesHelper.asJsonNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import fourj.model.Base;
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
		insertShuffledData(db);
		
		try (Transaction tx = db.beginTx()) {
			assertEquals(6l, tx.getAllNodes().stream().filter(n -> n.getLabels().iterator().next().equals(Base.hlabel)).count());
			assertEquals(3l, tx.getAllNodes().stream().filter(n -> n.getLabels().iterator().next().equals(Base.plabel)).count());
			assertEquals(16l, tx.getAllRelationships().stream().flatMap(r -> Arrays.stream(r.getNodes())).count());
		}
		
		var hResult = Queries.query(Queries.hMatch, ImmutableMap.of("id", "n1"), db);
		var expected = "{name:\"my n1 up\",jsonString:\"{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\"}\",id:\"n1\",parentId:\"root\"}";
		assertTrue(hResult.contains(expected));
		
		var p1Result = Queries.pquery(Queries.pMatch, ImmutableMap.of("id", "p1"), db);
		expected = "{\"id\":\"p1\",\"name\":\"my p1 up\",\"parentId\":\"n1\",\"parent\":{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\",\"parent\":{\"id\":\"root\",\"name\":\"my root up\"}}}";
		assertEquals(expected, p1Result);

		var p11Result = Queries.pquery(Queries.pMatch, ImmutableMap.of("id", "p11"), db);
		expected = "{\"id\":\"p11\",\"name\":\"my p11\",\"parentId\":\"n11\",\"parent\":{\"id\":\"n11\",\"name\":\"my n11\",\"parentId\":\"n1\",\"parent\":{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\",\"parent\":{\"id\":\"root\",\"name\":\"my root up\"}}}}";
		assertEquals(expected, p11Result);
 
		var subtreeResult = Queries.query(Queries.subtreeMatch, ImmutableMap.of("id", "n1"), db);
		var expected1 = "{name:\"my p11\",jsonString:\"{\"id\":\"p11\",\"name\":\"my p11\",\"parentId\":\"n11\"}\",id:\"p11\",parentId:\"n11\"}";
		var expected2 = "{name:\"my p1 up\",jsonString:\"{\"id\":\"p1\",\"name\":\"my p1 up\",\"parentId\":\"n1\"}\",id:\"p1\",parentId:\"n1\"}";
		assertTrue(subtreeResult.contains(expected1));
		assertTrue(subtreeResult.contains(expected2));
		
		var fooResult = Queries.pquery(Queries.pMatch, ImmutableMap.of("id", "foopr"), db);
		expected = "{\"id\":\"foopr\",\"name\":\"foo product\",\"parentId\":\"foo\",\"parent\":{\"id\":\"foo\",\"name\":\"second parent is n11\",\"parentId\":\"n11\",\"parent\":{\"id\":\"n11\",\"name\":\"my n11\",\"parentId\":\"n1\",\"parent\":{\"id\":\"n1\",\"name\":\"my n1 up\",\"parentId\":\"root\",\"parent\":{\"id\":\"root\",\"name\":\"my root up\"}}}}}";
		assertEquals(expected, fooResult);
	}
	
	private void insertShuffledData(final GraphDatabaseService db) {
		var pnodes = Lists.newArrayList(
				asJsonNode("hroot.json"),
				asJsonNode("hn11.json"),
				asJsonNode("hn12.json"),
				asJsonNode("hn2.json"),
				asJsonNode("foo-n2.json"),
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
				asJsonNode("foo-n11.json"),
				asJsonNode("hn1-up.json")
		);		

		Collections.shuffle(upnodes);
		upnodes.stream().forEach(n -> new Hierarchy(db, n));

		var upleaves = Lists.newArrayList(
				asJsonNode("p1-up.json"),
				asJsonNode("foo-pr.json")
		);		

		Collections.shuffle(upleaves);
		upleaves.stream().forEach(n -> new Product(db, n));
	}
}
