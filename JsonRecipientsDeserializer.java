
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class JsonRecipientsDeserializer extends JsonDeserializer<Map<String, String>> {
	@Override
	public Map<String, String> deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext)
			throws IOException, JsonProcessingException {

		String recipients = jsonparser.getText();
		return HelperUtils.parseRecipients(recipients);
	}

}