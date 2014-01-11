


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Multi-threaded (thread pool) SMTP client.
 * <p/>
 * Gets sender's and recipient's details and the message and sends.
 */
public class SMTPThreadPool {

    private HelperLogger logger = new HelperLogger(this.getClass());

    private final String smtpName;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    private ExecutorService executor;

    public SMTPThreadPool(int threadPoolSize, String smtpName, int smtpPort, String smtpUsername, String smtpPassword) {
        this.smtpName = smtpName;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;

        logger.info("Creating SMTP clients thread pool...");
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void stop() {
        logger.info("Stopping the SMTP Executors...");
        executor.shutdown();
    }

    public Future<Boolean> sendMessage(final ModelMailMessage mailMessage) {
        return executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return new SMTPClient(smtpName, smtpPort, smtpUsername, smtpPassword).sendMessage(mailMessage);
            }
        });
    }

    public Future<Boolean> sendSMS(final String to, String message) {
    	if (message.length() > 150) {
        	message = message.substring(0, 150);
        }
    	final String finalMessage = message;
    	return executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return new SMTPClient(smtpName, smtpPort, smtpUsername, smtpPassword).sendSMS(to, finalMessage);
            }
		});
	}

}
