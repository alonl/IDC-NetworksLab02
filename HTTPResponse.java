
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HTTPResponse {

	public static final String CRLF = "\r\n";
	private static final int CHUNK_SIZE = 200;

	private HTTPRequest httpRequest;
	private ModelAppResponse appResponse;

	private String statusCode;
	private Map<String, String> headersMap = new HashMap<>();
	private byte[] entityBody;
	private HTTPMethod method;

	private HelperLogger logger = new HelperLogger(this.getClass());

	public HTTPResponse(HTTPRequest httpRequest, ModelAppResponse appResponse) throws IOException {
		this.httpRequest = httpRequest;
		this.appResponse = appResponse;
		initResponse();
	}

	private void initResponse() throws IOException {
		Exception error;
		if ((error = httpRequest.getError()) != null
				|| (appResponse != null && appResponse.hasResponse() && (error = appResponse.getError()) != null)) {
			generateResponseError(error);
			return;
		}

		this.method = httpRequest.getMethod();
		if (method != null) {
			switch (method) {

			case OPTIONS:
				generateOptionsResponse();
				break;

			case TRACE:
				generateTraceResponse();
				break;

			case POST:
				generateAppResponse();
				break;

			case GET:
				generateAppResponse();
				break;

			case PUT:
				generateAppResponse();
				break;

			case DELETE:
				generateAppResponse();
				break;

			case HEAD:
				generateAppResponse();
				entityBody = null;
				break;

			default: // NOT_SUPPORTED
				generateResponse(HTTPConstants.STATUS_NOT_IMPLEMENTED, null, null);
				break;
			}

		} else {
			generateResponse(HTTPConstants.STATUS_NOT_IMPLEMENTED, null, null);
		}

	}

	private void generateResponseError(Exception error) {
		String body = "";
		if (this.appResponse.getBody() != null) {
			body = new String(this.appResponse.getBody());
		}
		if (error instanceof WebServerBadRequestException) {
			generateResponseString(HTTPConstants.STATUS_BAD_REQUEST, HTTPConstants.CONTENT_TYPE_TEXT,
					HTTPConstants.STATUS_BAD_REQUEST + ":" + CRLF + error.getMessage() + CRLF + body);
		} else if (error instanceof WebServerNotFoundException) {
			generateResponseString(HTTPConstants.STATUS_NOT_FOUND, HTTPConstants.CONTENT_TYPE_TEXT,
					HTTPConstants.STATUS_NOT_FOUND + ":" + CRLF + error.getMessage() + CRLF + body);
		} else {
			generateResponseString(HTTPConstants.STATUS_INTERNAL_ERROR, HTTPConstants.CONTENT_TYPE_TEXT,
					HTTPConstants.STATUS_INTERNAL_ERROR + ":" + CRLF + error.getMessage() + CRLF + body);
		}
		logger.error("Error occurred: " + error.getMessage(), error);
	}

	private void generateTraceResponse() {
		String response = httpRequest.getHeaders();
		response = response.substring(response.indexOf("\n") + 1);
		generateResponse(HTTPConstants.STATUS_OK, HTTPConstants.CONTENT_TYPE_HTTP, response.getBytes());
	}

	private void generateOptionsResponse() {
		addHeader(HTTPConstants.HEADER_ALLOW, allMethodsToOneLine());
		generateResponse(HTTPConstants.STATUS_OK, null, null);
	}

	private String allMethodsToOneLine() {
		StringBuilder output = new StringBuilder();
		for (HTTPMethod method : HTTPMethod.values()) {
			output.append(method + ", ");
		}
		return output.substring(0, output.length() - 2);
	}

	private void generateAppResponse() {
		if (appResponse == null || !appResponse.hasResponse()) {
			generateResponseError(new WebServerRuntimeException("Unexpected error. Please try again."));
		} else {
			for (Entry<String, String> entry : this.appResponse.getHeaders().entrySet()) {
				addHeader(entry.getKey(), entry.getValue());
			}
			generateResponse(appResponse.getStatusCode(), appResponse.getContentType(), appResponse.getBody());
		}
	}

	private void generateResponse(String statusCode, String contentType, byte[] body) {
		this.statusCode = statusCode;
		if (contentType != null) {
			addHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
		}
		if (body != null) {
			setEntityBody(body);
		}
	}

	private void generateResponseString(String statusCode, String contentType, String body) {
		generateResponse(statusCode, contentType, body.getBytes());
	}

	private void setEntityBody(byte[] bytes) {
		this.entityBody = bytes;
		if (httpRequest.containsHeader(HTTPConstants.HEADER_CHUNKED)
				&& httpRequest.getHeader(HTTPConstants.HEADER_CHUNKED).equalsIgnoreCase("yes")) {
			addHeader(HTTPConstants.HEADER_TRANSFER_ENCODING, "chunked");
		} else {
			addHeader(HTTPConstants.HEADER_CONTENT_LENGTH, bytes.length + "");
		}
	}

	private void addHeader(String headerKey, String headerValue) {
		headersMap.put(headerKey.toUpperCase(), headerValue);
	}

	public void writeTo(DataOutputStream os) throws IOException {
		if (!statusCode.contains("200")) {
			headersMap.remove(HTTPConstants.HEADER_CHUNKED);
		}

		os.writeBytes(httpRequest.getHttpVersion() + " " + statusCode + CRLF);

		StringBuilder outputHeaders = new StringBuilder();
		for (Entry<String, String> header : headersMap.entrySet()) {
			outputHeaders.append(header.getKey() + ": " + header.getValue() + CRLF);
		}
		os.writeBytes(outputHeaders.toString());
		logger.info("The response headers: " + outputHeaders.toString().replaceAll("[\r\n]", ", "));

		os.writeBytes(CRLF);

		if (entityBody != null) {
			if (headersMap.containsKey(HTTPConstants.HEADER_CHUNKED.toUpperCase())) {
				writeChunked(os, entityBody);
			} else {
				os.write(entityBody);
			}
		}
	}

	private void writeChunked(DataOutputStream os, byte[] entityBody) throws IOException {
		int offset = 0;
		int len = CHUNK_SIZE;
		while (offset < entityBody.length) {
			if (offset + len > entityBody.length) {
				len = entityBody.length - offset;
			}

			os.writeBytes(Integer.toHexString(len) + CRLF);
			os.write(entityBody, offset, len);
			os.writeBytes(CRLF);

			offset += len;
		}

		os.writeBytes("0" + CRLF + CRLF);
	}

}
