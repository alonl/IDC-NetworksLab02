
import java.io.File;
import java.io.IOException;

public class ManagerResources {

	public ModelAppResponse handleRequest(HTTPRequest request) {
		try {
			if (validateInServerDir(request)) {
				return staticResponse(request);
			} else {
				return ModelAppResponse.response(HTTPConstants.STATUS_BAD_REQUEST,
						"Don't be evil! Requested page path is outside the server's scope.");
			}
		} catch (IOException e) {
			return ModelAppResponse.responseError(e);
		}
	}

	private ModelAppResponse staticResponse(HTTPRequest request) {
		File requestedFile = request.getRequestedPage();
		if (!request.isFile()) {
			return ModelAppResponse.responseNotFound(request.getRequestedPath());
		} else {
			if (request.isImage()) {
				return ModelAppResponse.responseFile(requestedFile, HTTPConstants.CONTENT_TYPE_IMAGE);
			} else if (request.isIcon()) {
				return ModelAppResponse.responseFile(requestedFile, HTTPConstants.CONTENT_TYPE_ICON);
			} else if (request.isCSS()) {
				return ModelAppResponse.responseFile(requestedFile, HTTPConstants.CONTENT_TYPE_CSS);
			} else if (request.isJS()) {
				return ModelAppResponse.responseFile(requestedFile, HTTPConstants.CONTENT_TYPE_JS);
			} else {
				return ModelAppResponse.responsePage(requestedFile);
			}
		}
	}

	private boolean validateInServerDir(HTTPRequest request) throws IOException {
		if (request.getRequestedPage() != null
				&& !request.getRequestedPage().getCanonicalPath()
						.startsWith(request.getServerRoot().getCanonicalPath())) {
			return false;
		}
		return true;
	}

}
