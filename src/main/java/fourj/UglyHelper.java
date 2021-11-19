package fourj;

import java.io.InputStream;

import org.neo4j.graphdb.Label;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fourj.model.Base;
import fourj.model.Hierarchy;

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

	public static String jsonParentStuff(Base base, Hierarchy parent) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode parentJson = (ObjectNode)mapper
					.readTree((String)parent.getUnderlyingNode().getProperty("jsonString"));

			ObjectNode baseJson = (ObjectNode)mapper
					.readTree((String)base.getUnderlyingNode().getProperty("jsonString"));

			baseJson.set("parent", parentJson); 
			return mapper.writeValueAsString(baseJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

//	public static Hierarchy jsonParentStuff(Hierarchy element, Hierarchy partial) {
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			ObjectNode elementJson = (ObjectNode)mapper
//					.readTree((String)element.getUnderlyingNode().getProperty("jsonString"));
//
//			ObjectNode partialJson = (ObjectNode)mapper
//					.readTree((String)partial.getUnderlyingNode().getProperty("jsonString"));
//
//			partialJson.set("parent", elementJson); 
//			partial.getUnderlyingNode().setProperty("jsonString", mapper.writeValueAsString(partialJson));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		return partial;
//	}
}
