
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonAnswersSerializer extends JsonSerializer<List<String>> {

	@Override
	public void serialize(List<String> value, JsonGenerator gen, SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		StringBuilder output = new StringBuilder();
		for (String answer : value) {
			output.append(answer + "\r\n");
		}
		gen.writeString(output.toString());
	}

}