


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;




public class HTTPRequest {

    public static final String[] IMAGE_TYPES_ARR = {"BMP", "GIF", "PNG", "JPG"};
    private static final HashSet<String> IMAGE_TYPES = new HashSet<>(Arrays.asList(IMAGE_TYPES_ARR));

    private File serverRoot;
    private String defaultPage;

    private Exception error = null;
    private boolean isEmpty = false;
    private boolean isFile = false;
    private String httpVersion;
    private HTTPMethod method;
    private File requestedPage = null;
    private String requestedPath;
    private final String headers;
    private Map<String, String> headersMap = new HashMap<>();
    private Map<String, String> urlParameters = new HashMap<>();

    private HelperLogger logger = new HelperLogger(this.getClass());

    public HTTPRequest(File serverRoot, String defaultPage, String headers) throws IOException {
        this.serverRoot = serverRoot;
        this.defaultPage = defaultPage;
        this.headers = headers;
        try {
            parseRequest(headers);
        } catch (WebServerBadRequestException | WebServerRuntimeException e) {
            this.error = e;
        }
    }

    public Integer getContentLength() throws WebServerBadRequestException {
        String contentLength = getHeader(HTTPConstants.HEADER_CONTENT_LENGTH);
        try {
            return contentLength == null ? null : Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new WebServerBadRequestException("Error: Content-Length is not a number: '" + contentLength + "'");
        }
    }

    public Exception getError() {
        return error;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headersMap.get(header.toUpperCase());
    }

    public void setHeader(String header, String value) {
        headersMap.put(header.toUpperCase(), value);
    }

    public boolean containsHeader(String header) {
        return headersMap.containsKey(header.toUpperCase());
    }

    public HTTPMethod getMethod() {
        return this.method;
    }

    public String getRequestedPath() {
        return this.requestedPath;
    }

    public String getReferrer() {
        return getHeader(HTTPConstants.HEADER_REFERER);
    }

    public File getRequestedPage() {
        return requestedPage;
    }

    public File getServerRoot() {
        return serverRoot;
    }

    public Map<String, String> getUrlParameters() {
        return urlParameters;
    }

    public String getUserAgent() {
        return getHeader(HTTPConstants.HEADER_USER_AGENT);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean isFile() {
        return isFile;
    }

    private String getFileExtension() {
        if (requestedPage != null && requestedPage.canRead()) {
            int lastIndexOfDot = requestedPage.getName().lastIndexOf(".");
            if (lastIndexOfDot > 0) {
                return requestedPage.getName().substring(lastIndexOfDot + 1).toUpperCase();

            }
        }
        return null;
    }

    public boolean isResource() {
        if (isIcon() || isImage()) {
            return true;
        }
        String fileExtension = getFileExtension();
        return fileExtension != null && (fileExtension.equals("JS") || fileExtension.equals("CSS"));
    }

    public boolean isImage() {
        String fileExtension = getFileExtension();
        if (fileExtension != null && IMAGE_TYPES.contains(fileExtension)) {
            return true;
        }
        return false;
    }

    public boolean isIcon() {
        String fileExtension = getFileExtension();
        if (fileExtension != null && fileExtension.equals("ICO")) {
            return true;
        }
        return false;
    }

    private void parseFirstLine(String request) throws WebServerRuntimeException, IOException,
            WebServerBadRequestException {
        try (Scanner scanner = new Scanner(request)) {
            scanner.useDelimiter(" ");

            if (!scanner.hasNext()) {
                throw new WebServerBadRequestException("Error: Request method not exists.");
            }
            parseMethod(scanner.next());

            if (!scanner.hasNext()) {
                throw new WebServerBadRequestException("Error: Request requested page not exists.");
            }
            parseRequestedPageAndParams(scanner.next());

            if (!scanner.hasNext()) {
                throw new WebServerBadRequestException("Error: Request HTTP version not exists.");
            }

            if (!validateHttpVersion(scanner.next())) {
                throw new WebServerBadRequestException("Error: Invalid HTTP version.");
            }
        }
    }

    private void parseHeader(String header) {
        try (Scanner scanner = new Scanner(header)) {
            scanner.useDelimiter(": ");
            String key = scanner.next();
            String value = scanner.next();
            setHeader(key, value);
        } catch (NoSuchElementException e) {
            logger.error("Invalid header: '" + header + "'. Skipping this header...");
        }
    }

    private void parseMethod(String method) {
        try {
            this.method = HTTPMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Not supported HTTP request method: " + method);
            this.method = null;
        }
    }

    private void parseRequest(String headers) throws WebServerBadRequestException, WebServerRuntimeException,
            IOException {
        try (Scanner scanner = new Scanner(headers);) {
            if (!scanner.hasNext()) {
                logger.debug("Empty request.");
                this.isEmpty = true;
                return;
            }
            parseFirstLine(scanner.nextLine());

            while (scanner.hasNextLine()) {
                parseHeader(scanner.nextLine());
            }
        }
    }

    private void parseRequestedPage(String requestedPage) throws WebServerRuntimeException, IOException {
        this.requestedPath = requestedPage;

        if (requestedPage.length() == 0) {
            return;
        }

        if (requestedPage.equals("/")) {
            requestedPage += defaultPage;
        }
        requestedPage = requestedPage.replaceAll("/", Matcher.quoteReplacement(File.separator));

        this.requestedPage = new File(serverRoot + requestedPage);
        if (this.requestedPage.isFile()) {
            isFile = true;
        } else {
            isFile = false;
        }
    }

    private void parseRequestedPageAndParams(String requestedPage) throws WebServerRuntimeException, IOException {
        int indexOfParams = requestedPage.indexOf('?');
        if (indexOfParams >= 0) {
            parseRequestedPage(requestedPage.substring(0, indexOfParams));
            HelperUtils.parseParameters(this.urlParameters, requestedPage.substring(indexOfParams + 1));
        } else {
            parseRequestedPage(requestedPage);
        }
    }

    private boolean validateHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return (httpVersion.toUpperCase().equals(HTTPConstants.HTTP_VERSION_1_0.toUpperCase()) || httpVersion
                .toUpperCase().equals(HTTPConstants.HTTP_VERSION_1_1.toUpperCase()));

    }

	public boolean isCSS() {
		return isResource() && getFileExtension().equals("CSS"); 
	}

	public boolean isJS() {
		return isResource() && getFileExtension().equals("JS");
	}

}