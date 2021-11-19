package fourj;

import java.io.InputStream;

import org.neo4j.graphdb.Label;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UglyHelper {

	public static final Label hlabel = Label.label("Hierarchy");
	public static final Label plabel = Label.label("Product");


	public static JsonNode asJsonNode(String filename) {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
			return new ObjectMapper().readValue(inputStream, JsonNode.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
