
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class MainWebServer {

	private int port;
	private File root;
	private String defaultPage;
	private int maxThreads;
	private SMTPApp underlyingApp;

	private static HelperLogger logger = new HelperLogger(MainWebServer.class);

	public MainWebServer(Properties serverProperties, SMTPApp smtpApp) {
		port = Integer.parseInt(serverProperties.getProperty(MainRunner.PORT_PROPERTY));
		root = new File(serverProperties.getProperty(MainRunner.ROOT_PROPERTY));
		defaultPage = serverProperties.getProperty(MainRunner.DEFAULT_PAGE_PROPERTY);
		maxThreads = Integer.parseInt(serverProperties.getProperty(MainRunner.MAX_THREADS_PROPERTY));
		underlyingApp = smtpApp;
	}

	public File getRoot() {
		return root;
	}

	public String getDefaultPage() {
		return defaultPage;
	}

	public void startServer() {
		logger.info("Creating a thread pool with size " + maxThreads);
		HTTPThreadPool threadPool = new HTTPThreadPool(this, maxThreads, underlyingApp);

		// Establish the listen socket.
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
			logger.info("Server is listening on port: " + port);

			// Process HTTP service requests in an infinite loop.
			while (true) {

				Socket connection = null;
				try {

					// Listen for a TCP connection request.
					connection = socket.accept();
					logger.debug("Got new connection from IP: " + connection.getInetAddress());

					// Construct an object to process the HTTP request message.
					// Create a new thread to process the request.
					threadPool.addRequest(connection);

				} catch (IOException e) {
					logger.error("Error occurred while waiting for a connection. Ignoring...");
					e.printStackTrace();
				}

			}

		} catch (IOException e) {
			logger.error("Couldn't start the server. Shutting down.");
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
