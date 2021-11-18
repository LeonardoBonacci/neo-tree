package fourj;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UglyHelper {

	public static JsonNode asJsonNode(String filename) {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
			return new ObjectMapper().readValue(inputStream, JsonNode.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
