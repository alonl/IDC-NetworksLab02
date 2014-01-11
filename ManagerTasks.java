


import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;



/**
 * Handles Tasks requests.
 */
public class ManagerTasks extends ManagerAbstract<ModelTask> {

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	public ManagerTasks(ServiceAbstractScheduled<ModelTask> service) {
		super(service);
	}

	@Override
	public Class<ModelTask> getModelClass() {
		return ModelTask.class;
	}

	@Override
	public String getServicePath() {
		return HelperConstants.TASKS_PATH;
	}

	@Override
	public ModelTask insert(ModelTask item) throws SQLException, WebServerBadRequestException, WebServerRuntimeException {
		validateToMail(item);
		return super.insert(item);
	}

	@Override
	public boolean update(ModelTask item) throws SQLException, WebServerBadRequestException {
		validateToMail(item);
		return super.update(item);
	}

	@Override
	public boolean remove(Integer id) throws SQLException, WebServerBadRequestException {
		ModelTask task = service.findOne(id);
		if (task != null && task.getStatus() != ModelTask.TaskStatus.IN_PROGRESS) {
			throw new WebServerBadRequestException("Cannot remove a task which is not in progress.");
		}
		return super.remove(id);
	}

	private void validateToMail(ModelTask item) throws WebServerBadRequestException {
		if (item != null && item.getToMail() != null
				&& !item.getToMail().matches("([a-zA-Z0-9_.+-]+(@|(\\%40))[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)")) {
			throw new WebServerBadRequestException("Invalid email: " + item.getToMail());
		}
	}

	@Override
	protected ModelAppResponse handleReply(Map<String, String> urlParameters) throws SQLException {
		Integer id = Integer.parseInt(urlParameters.get("id"));
		if (id != null) {
			boolean res = ((ServiceTasks) service).updateComplete(id);
			if (res) {
				return ModelAppResponse.responseOK("Got it. Your manager thanks you.");
			} else {
				return ModelAppResponse
						.responseError(new WebServerRuntimeException("Couldn't update task."));
			}
		} else {
			return ModelAppResponse.responseError(new WebServerNotFoundException("Missing ID value."));
		}
	}

	@Override
	protected ModelTask parseSubmitParamsInsert(Map<String, String> urlParameters, ModelUser user) throws ParseException {
		return new ModelTask(user.getUserMail().toLowerCase(), urlParameters.get("content"), false,
				format.parse(urlParameters.get("dueDate")), urlParameters.get("phone"), urlParameters.get("title"),
				urlParameters.get("toMail"), ModelTask.TaskStatus.IN_PROGRESS);
	}

	@Override
	protected String getMainPageUrl() {
		return HelperConstants.TASKS_MAIN;
	}

	@Override
	protected ModelTask parseSubmitParamsUpdate(Map<String, String> urlParameters, ModelUser user) throws NumberFormatException,
			ParseException, WebServerBadRequestException {
		throw new WebServerBadRequestException("Missing ID value");
	}

}
