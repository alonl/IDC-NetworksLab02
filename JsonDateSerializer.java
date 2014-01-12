
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonDateSerializer extends JsonSerializer<Date> {

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		gen.writeString(format.format(value));
	}

}