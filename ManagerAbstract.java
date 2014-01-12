
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ManagerAbstract<T extends ModelBaseItem> {

	protected HelperLogger logger = HelperLogger.getLogger(this.getClass());

	private ObjectMapper mapper;

	protected ServiceAbstract<T> service;

	public ManagerAbstract(ServiceAbstract<T> service) {
		this.service = service;
		this.mapper = new ObjectMapper();
	}

	public ModelAppResponse handleRequest(HTTPMethod method, String relativePath, String body, ModelUser user) {
		Object response = null;
		T bodyEntity = null;
		try {
			if (method == HTTPMethod.PUT || method == HTTPMethod.POST) {
				bodyEntity = mapper.readValue(body, getModelClass());
				if (bodyEntity != null) {
					bodyEntity.setUsermail(user.getUserMail());
				}
			}

			switch (method) {
			case HEAD:
			case GET:
				if (relativePath.isEmpty()) {
					response = getAll(user);
				} else if (relativePath.matches("\\/\\d+")) {
					response = getById(Integer.parseInt(relativePath.substring(1)), user);
				}
				break;
			case POST:
				if (relativePath.isEmpty()) {
					bodyEntity.setUsermail(user.getUserMail());
					response = insert(bodyEntity);
				}
				break;
			case PUT:
				if (relativePath.isEmpty()) {
					response = update(bodyEntity);
				}
				break;
			case DELETE:
				if (relativePath.matches("\\/\\d+")) {
					Integer itemId = Integer.parseInt(relativePath.substring(1));
					getById(itemId, user); // authorize
					response = remove(itemId);
				}
				break;
			default:
			}

			if (response != null) {
				return ModelAppResponse.responseJSON(mapper.writeValueAsString(response));
			} else {
				throw new WebServerBadRequestException("Unknown request: " + method + " , " + relativePath);
			}

		} catch (Exception e) {
			logger.error("Error occurred.", e);
			return ModelAppResponse.responseError(e);
		}
	}

	public T getById(Integer id, ModelUser user) throws SQLException, WebServerBadRequestException {
		T item = service.findOne(id);
		authorizeUser(item, user);
		return item;
	}

	private void authorizeUser(T item, ModelUser user) throws WebServerBadRequestException {
		if (!item.getUsermail().equalsIgnoreCase(user.getUserMail())) {
			throw new WebServerBadRequestException("Don't be evil! Cannot create / update an item of another user.");
		}
	}

	public List<T> getAll(ModelUser user) throws SQLException {
		return service.findAll(user);
	}

	public T insert(T item) throws SQLException, WebServerBadRequestException, WebServerRuntimeException {
		validatePhone(item);
		return service.insert(item);
	}

	protected void validatePhone(T item) throws WebServerBadRequestException {
		if (item instanceof ModelScheduledItem) {
			String phone = ((ModelScheduledItem) item).getPhone();
			if (phone == null || phone.isEmpty()) {
				((ModelScheduledItem) item).setPhone(null);
				return;
			} else if (!phone.matches("0\\d\\d\\d\\d\\d\\d\\d\\d\\d")) {
				throw new WebServerBadRequestException("Phone number is not valid: " + phone + " (Format: 0xxxxxxxxx)");
			}
		}
	}

	public boolean update(T item) throws SQLException, WebServerBadRequestException {
		validatePhone(item);
		return service.update(item);
	}

	public boolean remove(Integer id) throws SQLException, WebServerBadRequestException {
		return service.remove(id);
	}

	public abstract Class<T> getModelClass();

	public abstract String getServicePath();

	public ModelAppResponse handleParamRequest(File requestedPage, Map<String, String> urlParameters, ModelUser user) {
		try {
			if (requestedPage.getName().contains("submit")) {
				return handleSubmit(urlParameters, user);
			} else if (requestedPage.getName().contains("reply")) {
				return handleReply(urlParameters);
			}
			return ModelAppResponse
					.responseError(new WebServerNotFoundException("Unknown: " + requestedPage.getName()));
		} catch (Exception e) {
			return ModelAppResponse.responseError(e);
		}
	}

	protected ModelAppResponse handleSubmit(Map<String, String> urlParameters, ModelUser user) throws ParseException {
		try {
			String id = urlParameters.get("id");
			if (id == null || id.isEmpty()) {
				insert(parseSubmitParamsInsert(urlParameters, user));
			} else {
				update(parseSubmitParamsUpdate(urlParameters, user));
			}
			return ModelAppResponse.responseRedirect(getMainPageUrl());
		} catch (Exception e) {
			String linkToMenu = "<a href='/" + getMainPageUrl() + "'>Click here to go back</a>";
			ModelAppResponse appResponse = ModelAppResponse.responseError(e);
			appResponse.setBody(linkToMenu.getBytes());
			return appResponse;
		}
	}

	protected abstract ModelAppResponse handleReply(Map<String, String> urlParameters) throws SQLException,
			WebServerBadRequestException, IOException;

	protected abstract T parseSubmitParamsUpdate(Map<String, String> urlParameters, ModelUser user)
			throws NumberFormatException, ParseException, WebServerBadRequestException;

	protected abstract T parseSubmitParamsInsert(Map<String, String> urlParameters, ModelUser user)
			throws ParseException;

	protected abstract String getMainPageUrl();

}
