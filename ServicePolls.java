


import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;


public class ServicePolls extends ServiceAbstract<ModelPoll> {

	public ServicePolls(SMTPThreadPool smtpThreadPool, File dbFilePath, String serverName) throws IOException,
			SQLException {
		super(smtpThreadPool, dbFilePath, serverName);
	}

	@Override
	protected void init(File dbFilePath) throws IOException, SQLException {
		repo = new RepositoryAbstract<ModelPoll>(dbFilePath) {
			@Override
			protected Class<ModelPoll> getModelClass() {
				return ModelPoll.class;
			}
		};
	}

	@Override
	public ModelPoll insert(ModelPoll poll) throws SQLException, WebServerRuntimeException {
		ModelPoll inserted =  super.insert(poll);
		sendMessagesToRecipients(inserted);
		return inserted;
	}

	public void setRecipientAnswer(int pollId, String recipient, int answer) throws WebServerBadRequestException,
			SQLException, IOException {
		ModelPoll poll = findOne(pollId);
		if (poll == null) {
			throw new WebServerBadRequestException("No poll with ID: " + pollId);
		}
		if (!poll.isOpen()) {
			throw new WebServerBadRequestException("The poll with ID: '" + pollId + "' is already completed.");
		}
		if (answer + 1 > poll.getAnswers().size()) {
			throw new WebServerBadRequestException("There are less than " + answer + " answers.");
		}

		poll.setAnswer(recipient, answer);
		update(poll);

		sendMessage(poll, new ModelMailMessage(poll.getUsermail(), poll.getUsermail(), generateSubjectUpdate(poll),
				generateMessageUpdate(poll, recipient)));

		if (!poll.isOpen()) {
			sendMessage(poll, new ModelMailMessage(poll.getUsermail(), poll.getUsermail(), generateSubjectDone(poll),
					generateMessageDone(poll)));
		}
	}

	private String generateMessageDone(ModelPoll poll) throws IOException {
		return "The all recipients have voted. Poll is completed! Here are the results: \r\n\r\n"
				+ generatePollResults(poll);
	}

	private String generatePollResults(ModelPoll poll) throws IOException {
		StringBuilder results = new StringBuilder();
		List<String> answers = poll.getAnswers();
		for (Entry<String, String> recipientAnswer : poll.getRecipients().entrySet()) {
			if (recipientAnswer.getValue() == null) {
				results.append(String.format("%20s voted: Hasn't voted yet.", recipientAnswer.getKey()) + "\r\n");
			} else {
				Integer answer = Integer.parseInt(recipientAnswer.getValue());
				results.append(String.format("%20s voted: %s - %s", recipientAnswer.getKey(), answer, answers.get(answer)) + "\r\n");
			}
		}
		return results.toString();
	}

	private String generateSubjectDone(ModelPoll poll) {
		return "Poll: Poll is now completed! View the results. (ID: " + poll.getId() + ")";
	}

	private String generateMessageUpdate(ModelPoll poll, String recipient) throws IOException {
		return recipient + " has now voted! Here are the updated results:\r\n\r\n" + generatePollResults(poll);
	}

	private String generateSubjectUpdate(ModelPoll poll) {
		return "Poll: A new vote for your poll has been received.";
	}

	private void sendMessagesToRecipients(ModelPoll poll) throws WebServerRuntimeException {
		try {
			for (String recipient : poll.getRecipients().keySet()) {
				sendMessage(poll, new ModelMailMessage(poll.getUsermail(), recipient, generateSubject(poll),
						generateMessage(poll, recipient)));
			}
		} catch (IOException e) {
			logger.error("Unexpected error while parsing the json.", e);
			throw new WebServerRuntimeException("Unexpected error. Please try again.");
		}
	}

	private String generateMessage(ModelPoll poll, String recipient) throws WebServerRuntimeException, IOException {
		String answersLinks = generateAnswersLinks(poll, recipient);
		return "Hi, A new poll invitation. Please submit your vote by clicking one of the above links." + "\r\n"
				+ "Title: " + poll.getTitle() + "\r\n" + "Content: " + poll.getContent() + "\r\n" + "From: "
				+ poll.getUsermail() + "\r\n" + "Created at: " + poll.getCreatedAt() + "\r\n" + "\r\n" + "Answers: "
				+ "\r\n" + answersLinks;
	}

	private String generateSubject(ModelPoll poll) {
		return "Poll: A new poll! Please send your vote!";
	}

	private String generateAnswersLinks(ModelPoll poll, String recipient) throws WebServerRuntimeException, IOException {
		StringBuilder answersLinks = new StringBuilder();
		String voteFor = "Vote for ";
		String answerLink = "http://" + serverName + "/poll_reply.html?recipient="
				+ URLEncoder.encode(recipient, "UTF-8") + "&id=" + poll.getId();
		int index = 0;
		for (String answer : poll.getAnswers()) {
			answersLinks.append(voteFor + index + " - " + answer + " - " + answerLink + "&answer=" + index + "\r\n");
			index++;
		}
		return answersLinks.toString();
	}

	private void sendMessage(ModelPoll poll, ModelMailMessage mailMessage) {
		logger.info("Sending a mail message for the poll: '" + poll.getTitle() + "', to: " + mailMessage.getTo());
		smtpThreadPool.sendMessage(mailMessage);
	}

}
