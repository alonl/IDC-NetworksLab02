

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonRecipientsSerializer extends JsonSerializer<Map<String, String>> {

    @Override
    public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider arg2) throws IOException, JsonProcessingException {
    	StringBuilder output = new StringBuilder();
    	for (Entry<String, String> entry : value.entrySet()) {
    		if (entry.getValue() == null) {
    			output.append(entry.getKey() + ":null\r\n");
    		} else {
    			output.append(entry.getKey() + ":" + entry.getValue().toString() + "\r\n");
    		}
    	}
    	gen.writeString(output.toString());
    }
    
}