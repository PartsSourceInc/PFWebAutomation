package core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFactory {

    private static Logger logger;

    private LoggingFactory() {
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger("Automation");
            logger.debug("Initializing Logger.");
        }

        return logger;
    }

    public static Logger getLogger(final String loggerName) {
        logger = LoggerFactory.getLogger(loggerName);
        logger.debug(String.format("Initializing Logger with name '%s'.", loggerName));
        return logger;
    }
}
