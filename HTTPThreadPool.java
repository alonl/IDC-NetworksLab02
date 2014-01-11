


import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;




public class HTTPThreadPool {

	public static int REQUESTS_QUEUE_SIZE_RATIO = 2;
	
	private BlockingQueue<Socket> requestsQueue;
	private List<HTTPProcessor> requestProccessors;
	
	private HelperLogger logger = new HelperLogger(this.getClass());
	
	public HTTPThreadPool(MainWebServer webServer, int size, SMTPApp underlyingApp) {
		this.requestsQueue = new ArrayBlockingQueue<>(size * REQUESTS_QUEUE_SIZE_RATIO);
		this.requestProccessors = new ArrayList<>(size);
		initProccessors(webServer, size, underlyingApp);
	}

	private void initProccessors(MainWebServer webServer, int size, SMTPApp underlyingApp) {
		for (int i = 0; i < size; i++) {
			HTTPProcessor requestProccessor = new HTTPProcessor(webServer, requestsQueue, underlyingApp);
			new Thread(requestProccessor, "RequestProccessor" + i).start();
			requestProccessors.add(requestProccessor);
			logger.debug("RequestProccessor" + i + " is starting to work.");
		}
	}

	public void stop() {
		logger.info("Stopping the Request Proccessors...");
		for (HTTPProcessor proccessor : requestProccessors) {
			proccessor.stop();
		}
	}
	
	public void addRequest(Socket connection) {
		try {
			requestsQueue.put(connection);
			logger.debug("Now proccessing the connection from IP: " + connection.getInetAddress());
		} catch (InterruptedException e) {
			logger.error("Unexpected exception. Cannot enqueue request. Ignoring this request...");
			e.printStackTrace();
		}
	}
	
}
