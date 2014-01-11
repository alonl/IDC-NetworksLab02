


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * Handles Reminders requests.
 */
public class ManagerReminders extends ManagerAbstract<ModelReminder> {

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	public ManagerReminders(ServiceAbstractScheduled<ModelReminder> service) {
		super(service);
	}

	@Override
	public Class<ModelReminder> getModelClass() {
		return ModelReminder.class;
	}

	@Override
	public String getServicePath() {
		return HelperConstants.REMINDERS_PATH;
	}
	
	@Override
	protected ModelReminder parseSubmitParamsInsert(Map<String, String> urlParameters, ModelUser user) throws ParseException {
		return new ModelReminder(user.getUserMail().toLowerCase(), urlParameters.get("title"),
				urlParameters.get("content"), new Date(), format.parse(urlParameters.get("dueDate")),
				urlParameters.get("phone"));
	}

	@Override
	protected String getMainPageUrl() {
		return HelperConstants.REMINDERS_MAIN;
	}

	@Override
	protected ModelReminder parseSubmitParamsUpdate(Map<String, String> urlParameters, ModelUser user) throws NumberFormatException, ParseException {
		return new ModelReminder(Integer.parseInt(urlParameters.get("id")), urlParameters.get("usermail"),
				urlParameters.get("title"), urlParameters.get("content"), null, format.parse(urlParameters
						.get("dueDate")), urlParameters.get("phone"));
	}

	@Override
	protected ModelAppResponse handleReply(Map<String, String> urlParameters) throws SQLException,
			WebServerBadRequestException, IOException {
		return ModelAppResponse.responseNotFound("");
	}

}
