


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


/**
 * Handles Polls requests.
 */
public class ManagerPoll extends ManagerAbstract<ModelPoll> {

    public ManagerPoll(ServiceAbstract<ModelPoll> service) {
        super(service);
    }

    @Override
    public Class<ModelPoll> getModelClass() {
        return ModelPoll.class;
    }

    @Override
    public String getServicePath() {
        return HelperConstants.POLLS_PATH;
    }
    
    @Override
    protected String getMainPageUrl() {
    	return (HelperConstants.POLLS_MAIN);
    }

	@Override
	protected ModelPoll parseSubmitParamsInsert(Map<String, String> urlParameters, ModelUser user) {
		List<String> answers = HelperUtils.parseAnswers(urlParameters.get("answers"));
		Map<String, String> recipients = HelperUtils.parseRecipients(urlParameters.get("recipients"));
		String title = urlParameters.get("title");
		String content = urlParameters.get("content");
		
		ModelPoll poll = new ModelPoll(user.getUserMail(), title, content, recipients, answers, 0);
		return poll;
	}

	@Override
	protected ModelAppResponse handleReply(Map<String, String> urlParameters) throws WebServerBadRequestException,
			SQLException, IOException {
		String recipient = urlParameters.get("recipient").toString();
		int pollId = Integer.parseInt(urlParameters.get("id").toString());
		int answer = Integer.parseInt(urlParameters.get("answer"));
		
		((ServicePolls) service).setRecipientAnswer(pollId, recipient, answer);
		return ModelAppResponse.responseOK("Your vote recieved successfully. Thank you.");
	}
    
    @Override
    public ModelPoll insert(ModelPoll poll) throws SQLException, WebServerBadRequestException, WebServerRuntimeException {
    	validateRecipientsEmails(poll);
    	return super.insert(poll);
    }
    
    @Override
    protected ModelPoll parseSubmitParamsUpdate(Map<String, String> urlParameters, ModelUser user) throws NumberFormatException,
    ParseException, WebServerBadRequestException {
    	throw new WebServerBadRequestException("Missing ID value");
    }

	private void validateRecipientsEmails(ModelPoll poll) {
		try {
			for (String recipient : poll.getRecipients().keySet()) {
				validateEmail(recipient);
			}
		} catch (IOException e) {
			logger.error("Unexpected error.", e);
		}
	}

	private boolean validateEmail(String recipient) {
		return recipient != null && recipient.matches("([a-zA-Z0-9_.+-]+(@|(\\%40))[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
	}

}
