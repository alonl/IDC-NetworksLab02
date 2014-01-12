
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.j256.ormlite.logger.LocalLog;

public class MainRunner {

	private static HelperLogger logger = new HelperLogger(MainRunner.class);

	public static final String CONFIG_FILENAME = "config.ini";

	public static final String PORT_PROPERTY = "port";
	public static final String ROOT_PROPERTY = "root";
	public static final String DEFAULT_PAGE_PROPERTY = "defaultPage";
	public static final String MAX_THREADS_PROPERTY = "maxThreads";

	public static final String SMTP_NAME_PROPERTY = "SMTPName";
	public static final String SMTP_PORT_PROPERTY = "SMTPPort";
	public static final String SERVER_NAME_PROPERTY = "ServerName";
	public static final String SMTP_USERNAME_PROPERTY = "SMTPUsername";
	public static final String SMTP_PASSWORD_PROPERTY = "SMTPPassword";
	public static final String SMTP_IS_AUTH_LOGIN_PROPERTY = "SMTPIsAuthLogin";
	public static final String REMINDER_FILE_PATH_PROPERTY = "reminderFilePath";
	public static final String TASK_FILE_PATH_PROPERTY = "taskFilePath";
	public static final String POLL_FILE_PATH_PROPERTY = "pollFilePath";
	private static final String LOG_LEVEL_PROPERTY = "logLevel";

	public static void main(String[] args) {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "WARNING");
		Properties properties = readServerProperties();
		try {
			SMTPApp smtpApp = new SMTPApp(properties);
			smtpApp.startApp();
			new MainWebServer(properties, smtpApp).startServer();
		} catch (Exception e) {
			logger.error("Error initializing database. Shutting down.", e);
			System.exit(1);
		}
	}

	private static Properties readServerProperties() {
		Properties serverProperties = new Properties();
		try {
			String configFileContents = new String(Files.readAllBytes(Paths.get(CONFIG_FILENAME)));
			serverProperties.load(new StringReader(configFileContents.replace("\\", "\\\\")));
		} catch (IOException e) {
			exit("Couldn't load the configuration file. Shutting down.");
		}
		validateProperties(serverProperties);
		return serverProperties;
	}

	private static void validateProperties(Properties properties) {
		String portProperty = getProperty(properties, PORT_PROPERTY);
		try {
			Integer.parseInt(portProperty);
		} catch (NumberFormatException e) {
			exit("Port is not a number.");
		}

		String rootProperty = getProperty(properties, ROOT_PROPERTY);
		if (!(new File(rootProperty).isDirectory())) {
			exit("Root is not a directory.");
		}

		String maxThreadsProperty = getProperty(properties, MAX_THREADS_PROPERTY);
		try {
			Integer.parseInt(maxThreadsProperty);
		} catch (NumberFormatException e) {
			exit("Max threads is not a number.");
		}

		String smtpName = getProperty(properties, SMTP_NAME_PROPERTY);
		try {
			InetAddress.getByName(smtpName);
		} catch (UnknownHostException e) {
			exit("Couldn't resolve the " + SMTP_NAME_PROPERTY + " IP address.");
		}

		String smtpPort = getProperty(properties, SMTP_PORT_PROPERTY);
		try {
			Integer.parseInt(smtpPort);
		} catch (NumberFormatException e) {
			exit(SMTP_PORT_PROPERTY + " is not a number.");
		}

		String reminderFilePath = getProperty(properties, REMINDER_FILE_PATH_PROPERTY);
		checkCanWrite(reminderFilePath);

		String taskFilePath = getProperty(properties, TASK_FILE_PATH_PROPERTY);
		checkCanWrite(taskFilePath);

		String pollFilePath = getProperty(properties, POLL_FILE_PATH_PROPERTY);
		checkCanWrite(pollFilePath);

		getProperty(properties, DEFAULT_PAGE_PROPERTY);
		getProperty(properties, SERVER_NAME_PROPERTY);
		getProperty(properties, SMTP_USERNAME_PROPERTY);
		getProperty(properties, SMTP_PASSWORD_PROPERTY);
		getProperty(properties, SMTP_IS_AUTH_LOGIN_PROPERTY);

		String logLevel = getOptionalProperty(properties, LOG_LEVEL_PROPERTY);
		if (logLevel != null) {
			HelperLogger.logLevel = HelperLogger.LogLevel.valueOf(logLevel.toUpperCase());
		} else {
			HelperLogger.logLevel = HelperLogger.LogLevel.DEBUG;
		}
	}

	private static void exit(String errorMessage) {
		logger.error(errorMessage);
		System.exit(1);
	}

	private static String getProperty(Properties properties, String propertyName) {
		String property = getOptionalProperty(properties, propertyName);
		if (property == null) {
			logger.error("Error: " + propertyName + " not exists in the configuration file.");
			exit("The configuration file is corrupted. Shutting down.");
		}
		return property;
	}

	private static String getOptionalProperty(Properties properties, String propertyName) {
		return properties.getProperty(propertyName).replaceFirst("#.+", "").trim();
	}

	private static void checkCanWrite(String filePath) {
		File file = new File(filePath);
		try {
			if (!file.canWrite() && !file.getParentFile().canWrite()) {
				exit("Cannot write to file: " + filePath);
			}
			file.getCanonicalPath();
		} catch (NullPointerException | IOException e) {
			exit("Cannot write to file: " + filePath);
		}
	}

}
