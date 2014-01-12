
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class HTTPProcessor implements Runnable {

	private MainWebServer webServer;
	private BlockingQueue<Socket> requestsQueue;
	private final SMTPApp underlyingApp;
	private boolean isRunning;

	private HelperLogger logger = new HelperLogger(this.getClass());

	public HTTPProcessor(MainWebServer webServer, BlockingQueue<Socket> requestsQueue, SMTPApp underlyingApp) {
		this.webServer = webServer;
		this.requestsQueue = requestsQueue;
		this.underlyingApp = underlyingApp;
		isRunning = true;
	}

	public void stop() {
		isRunning = false;
	}

	@Override
	public void run() {
		while (isRunning) {
			Socket socket = null;
			try {
				socket = requestsQueue.take();
				processRequest(webServer, socket);

			} catch (InterruptedException e) {
				logger.error("Unexpected exception. Ignoring...");
				e.printStackTrace();

			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						logger.error("Unexpected exception.");
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void processRequest(MainWebServer webServer, Socket socket) {
		logger.debug("Opening the streams and reading the request headers...");
		DataOutputStream os = null;
		BufferedReader is = null;
		String requestHeaders = null;
		try {
			os = new DataOutputStream(socket.getOutputStream());
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			requestHeaders = HelperUtils.readRequestHeaders(is);
			logger.info("The request headers: " + requestHeaders.replaceAll("[\r\n]", ", "));

		} catch (Exception e) {
			logger.error("Exception occurred, ignoring the request.", e);
			closeStreams(os, is);
			return;
		}

		logger.debug("Parsing the request...");
		HTTPRequest httpRequest = null;
		try {
			httpRequest = new HTTPRequest(webServer.getRoot(), webServer.getDefaultPage(), requestHeaders);
			if (httpRequest.isEmpty()) {
				closeStreams(os, is);
				return;
			}
		} catch (Exception e) {
			logger.error("IOException occurred while parsing the request. Skipping this request...", e);
			closeStreams(os, is);
			return;
		}

		// lab 2 addition
		logger.debug("Processing the request...");
		ModelAppResponse appResponse = underlyingApp.handleHttpRequest(httpRequest, is);

		logger.debug("Generating the response...");
		HTTPResponse httpResponse = null;
		try {
			httpResponse = new HTTPResponse(httpRequest, appResponse);
			httpResponse.writeTo(os);
		} catch (Exception e) {
			logger.error("Exception occured while writing the response. Skipping this session...", e);
		} finally {
			closeStreams(os, is);
		}
	}

	private void closeStreams(DataOutputStream os, BufferedReader is) {
		logger.debug("Closing streams...");
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				logger.error("Unexpected exception.", e);
			}
		}
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				logger.error("Unexpected exception.", e);
			}
		}
	}
}
