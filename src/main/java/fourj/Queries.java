package fourj;

import java.util.ArrayList;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.core.NodeEntity;

import fourj.model.Hierarchy;
import fourj.model.Product;

public class Queries {

	public enum RelTypes implements RelationshipType {
		PARENT
	}

	// @formatter:off
	public final static String hMatch = 
			"MATCH (h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n" 
		  + "WHERE r.parentId IS NULL AND r.tmp IS NULL" + "\n" 
		  + "RETURN h, h.name";

	public final static String pMatch = 
			"MATCH path = (p:Product {id: $id})-[*]->(r:Hierarchy)" + "\n" 
		  + "WHERE r.parentId IS NULL AND r.tmp IS NULL" + "\n" 
		  + "RETURN nodes(path) AS path";

	public final static String subtreeMatch = 
			"MATCH (p:Product)-[*]->(h:Hierarchy {id: $id})-[*]->(r:Hierarchy)" + "\n"
		  + "WHERE r.parentId IS NULL AND r.tmp IS NULL" + "\n" 
		  + "RETURN p, p.name";
	// @formatter:on

	public static String query(String q, Map<String, Object> params, GraphDatabaseService db) {
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {
			return tx.execute(q, params).resultAsString();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String pquery(String q, Map<String, Object> params, GraphDatabaseService db) {
		try (Transaction tx = db.beginTx(); Result result = tx.execute(q, params)) {
			var row = result.next();
			var asList = (ArrayList) row.get("path");

			var p = new Product((NodeEntity) asList.get(0));
			var hPath = asList.subList(1, asList.size()).stream().map(n -> new Hierarchy((NodeEntity) n));

			return p.addHierarchyPath(hPath).asJsonString();
		}
	}
}