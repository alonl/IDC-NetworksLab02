


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ModelAppResponse {

    private boolean hasResponse;
    private String statusCode;
    private String contentType;
    private byte[] body;
    private Map<String, String> headers;
    private Exception error;

    public ModelAppResponse() {
        this.headers = new HashMap<>();
    }

    public boolean hasResponse() {
        return hasResponse;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody() {
        return body;
    }
    
    public void setBody(byte[] body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headers.get(header.toUpperCase());
    }

    public void setHeader(String header, String value) {
        headers.put(header.toUpperCase(), value);
    }

    public Exception getError() {
        return error;
    }

    public static ModelAppResponse responseRedirect(String location) {
        ModelAppResponse response = new ModelAppResponse();
        response.hasResponse = true;
        response.statusCode = HTTPConstants.STATUS_REDIRECT;
        response.headers.put(HTTPConstants.HEADER_LOCATION, location);
        response.contentType = null;
        response.body = null;
        return response;
    }

    public static ModelAppResponse responseFile(File requestedPage, String contentType) {
        ModelAppResponse response = new ModelAppResponse();
        response.contentType = contentType;
        response.hasResponse = true;
        response.statusCode = HTTPConstants.STATUS_OK;;
        try {
            response.body = HelperUtils.readFile(requestedPage);
        } catch (IOException e) {
            response.error = e;
        }
        return response;
    }

    public static ModelAppResponse responsePage(File requestedPage) {
        return responseFile(requestedPage, HTTPConstants.CONTENT_TYPE_TEXT);
    }

    public static ModelAppResponse responseError(Exception e) {
        ModelAppResponse appResponse = new ModelAppResponse();
        appResponse.hasResponse = true;
        appResponse.error = e;
        return appResponse;
    }

    public static ModelAppResponse response(String statusCode, String message) {
        ModelAppResponse appResponse = new ModelAppResponse();
        appResponse.hasResponse = true;
        appResponse.statusCode = statusCode;
        appResponse.contentType = HTTPConstants.CONTENT_TYPE_TEXT;
        appResponse.body = message.getBytes();
        return appResponse;
    }

    public static ModelAppResponse responseOK(String body) {
        return response(HTTPConstants.STATUS_OK, body);
    }
    
    public static ModelAppResponse responseJSON(String body) {
    	ModelAppResponse response = responseOK(body);
    	response.contentType = HTTPConstants.CONTENT_TYPE_JSON;
    	return response;
    }

    public static ModelAppResponse responseOK(byte[] body) {
        ModelAppResponse appResponse = response(HTTPConstants.STATUS_OK, "");
        appResponse.body = body;
        return appResponse;
    }

    public static ModelAppResponse responseNotFound(String requestedPath) {
        return responseError(new WebServerNotFoundException("Sorry, the requested page was not found: " + requestedPath));
    }

}
