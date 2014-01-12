
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SMTPApp {

	public static final int SMTP_THREAD_POOL_SIZE = 5;

	private MainRouter mainRouter;
	private Map<String, ServiceAbstract<? extends ModelBaseItem>> services;

	private String smtpName;
	private int smtpPort;
	private String serverName;
	private String smtpUsername;
	private String smtpPassword;
	private boolean smtpIsAuthLogin;
	private File reminderFilePath;
	private File taskFilePath;
	private File pollFilePath;

	public SMTPApp(Properties properties) throws IOException, SQLException {
		setProperties(properties);
		if (!smtpIsAuthLogin) {
			smtpUsername = null;
			smtpPassword = null;
		}
		SMTPThreadPool smtpThreadPool = new SMTPThreadPool(SMTP_THREAD_POOL_SIZE, smtpName, smtpPort, smtpUsername,
				smtpPassword);
		services = new HashMap<>();
		services.put(ModelReminder.class.getName(), new ServiceReminder(smtpThreadPool, reminderFilePath, serverName));
		services.put(ModelTask.class.getName(), new ServiceTasks(smtpThreadPool, taskFilePath, serverName));
		services.put(ModelPoll.class.getName(), new ServicePolls(smtpThreadPool, pollFilePath, serverName));
		this.mainRouter = new MainRouter(services);
	}

	public void startApp() throws SQLException {
		for (ServiceAbstract<?> service : services.values()) {
			service.start();
		}
	}

	public ModelAppResponse handleHttpRequest(HTTPRequest httpRequest, BufferedReader is) {
		return mainRouter.handleRequest(httpRequest, is);
	}

	private void setProperties(Properties appProperties) {
		this.smtpName = appProperties.getProperty(MainRunner.SMTP_NAME_PROPERTY);
		this.smtpPort = Integer.parseInt(appProperties.getProperty(MainRunner.SMTP_PORT_PROPERTY));
		this.serverName = appProperties.getProperty(MainRunner.SERVER_NAME_PROPERTY) + ":"
				+ appProperties.getProperty(MainRunner.PORT_PROPERTY);
		this.smtpUsername = appProperties.getProperty(MainRunner.SMTP_USERNAME_PROPERTY);
		this.smtpPassword = appProperties.getProperty(MainRunner.SMTP_PASSWORD_PROPERTY);
		this.smtpIsAuthLogin = Boolean.parseBoolean(appProperties.getProperty(MainRunner.SMTP_IS_AUTH_LOGIN_PROPERTY));
		this.reminderFilePath = new File(appProperties.getProperty(MainRunner.REMINDER_FILE_PATH_PROPERTY));
		this.taskFilePath = new File(appProperties.getProperty(MainRunner.TASK_FILE_PATH_PROPERTY));
		this.pollFilePath = new File(appProperties.getProperty(MainRunner.POLL_FILE_PATH_PROPERTY));
	}

}
