
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class JsonAnswersDeserializer extends JsonDeserializer<List<String>> {
	@Override
	public List<String> deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext)
			throws IOException, JsonProcessingException {

		String answers = jsonparser.getText();
		return HelperUtils.parseAnswers(answers);
	}

}