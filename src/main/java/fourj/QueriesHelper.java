package fourj;

import static fourj.model.Base.JSON_STRING;
import static fourj.model.Base.PARENT;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fourj.model.Base;
import fourj.model.Hierarchy;

public class QueriesHelper {

	public static JsonNode asJsonNode(String filename) {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
			return new ObjectMapper().readValue(inputStream, JsonNode.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String jsonParent(Base base, Hierarchy parent) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode parentJson = mapper
					.readTree((String)parent.getUnderlyingNode().getProperty(JSON_STRING));

			ObjectNode baseJson = (ObjectNode)mapper
					.readTree((String)base.getUnderlyingNode().getProperty(JSON_STRING));

			baseJson.set(PARENT, parentJson); 
			return mapper.writeValueAsString(baseJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
