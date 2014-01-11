


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;

public class ModelPoll extends ModelBaseItem {

	private static ObjectMapper mapper = new ObjectMapper();
	private static HelperLogger logger = HelperLogger.getLogger(ModelPoll.class);

	@JsonDeserialize(using = JsonRecipientsDeserializer.class)
    @JsonSerialize(using = JsonRecipientsSerializer.class)
	@JsonProperty(value = "recipients")
	@DatabaseField(canBeNull = false)
	private String recipients;

	@JsonDeserialize(using = JsonAnswersDeserializer.class)
    @JsonSerialize(using = JsonAnswersSerializer.class)
	@JsonProperty(value = "answers")
	@DatabaseField(canBeNull = false)
	private String answers;

	@JsonIgnore
	@DatabaseField(canBeNull = false)
	private int received = 0;

	public ModelPoll() {
	}

	public ModelPoll(int id, Date createdAt, String usermail, String title, String content, Map<String, String> recipients,
			List<String> answers, int received) {
		super(id, createdAt, usermail, title, content);
		try {
			setRecipients(recipients);
			setAnswers(answers);
			this.received = received;
		} catch (IOException e) {
			logger.error("Unexpected error.", e);
		}
	}

	public ModelPoll(String usermail, String title, String content, Map<String, String> recipients, List<String> answers, int received) {
		super(usermail, title, content);
		try {
			setRecipients(recipients);
			setAnswers(answers);
			this.received = received;
		} catch (JsonProcessingException e) {
			logger.error("Unexpected error.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getRecipients() throws IOException {
		return mapper.readValue(recipients, Map.class);
	}

	public void setRecipients(Map<String, String> recipients) throws JsonProcessingException {
		this.recipients = mapper.writeValueAsString(recipients);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAnswers() throws IOException {
		return mapper.readValue(answers, List.class);
	}

	public void setAnswers(List<String> answers) throws JsonProcessingException {
		this.answers = mapper.writeValueAsString(answers);
	}
	
	@JsonIgnore
	public boolean isOpen() throws IOException {
		return getRecipients().size() != received;
	}

	public void setAnswer(String recipient, int answer) throws IOException, WebServerBadRequestException {
		Map<String, String> recipients = this.getRecipients();
		if (!recipients.containsKey(recipient)) {
			throw new WebServerBadRequestException("Poll doesn't contain this recipient: " + recipient);
		}
		if (recipients.get(recipient) == null) {
			received++;
		}
		recipients.put(recipient, answer + "");
		this.setRecipients(recipients);
	}
	
}
