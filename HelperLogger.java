

import java.sql.Timestamp;
import java.util.Date;

public class HelperLogger {

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    public static LogLevel logLevel = LogLevel.DEBUG;

    private String loggerName;

    public HelperLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    public HelperLogger(Class<? extends Object> class1) {
        this.loggerName = class1.getName();
    }

    public void debug(String message) {
        if (logLevel.ordinal() <= LogLevel.DEBUG.ordinal()) outFormat("DEBUG", loggerName, message);
    }

    public void info(String message) {
        if (logLevel.ordinal() <= LogLevel.INFO.ordinal()) outFormat("INFO", loggerName, message);
    }

    public void warn(String message) {
        if (logLevel.ordinal() <= LogLevel.WARN.ordinal()) outFormat("WARN", loggerName, message);
    }

    public void error(String message) {
        if (logLevel.ordinal() <= LogLevel.ERROR.ordinal()) outFormat("ERROR", loggerName, message);
    }

    public void error(String message, Exception e) {
        error(message);
        if (logLevel == LogLevel.DEBUG) {
        	e.printStackTrace();        	
        }
    }

    public void outFormat(String messageType, String className, String message) {
        out(String.format("%-23s %-5s %-18s: %s", new Timestamp(new Date().getTime()).toString(), messageType, className, message));
    }

    private void out(String message) {
        System.out.println(message.trim());
    }

    public static HelperLogger getLogger(Class<? extends Object> aClass) {
        return new HelperLogger(aClass);
    }

}
