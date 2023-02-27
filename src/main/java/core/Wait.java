package core;

import com.google.common.base.Preconditions;
import core.logging.LoggingFactory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Waiting utility. It is currently necessary, because the load state of some UI elements
 * cannot be detected. Should be removed when no longer necessary.
 */
public class Wait {

    private final Logger logger = LoggingFactory.getLogger();

    /**
     * Wait for a specific number of seconds.
     *
     * @param secondsToWait A number between 1 and 60.
     * @throws RuntimeException When interrupted.
     */
    public void forSeconds(int secondsToWait) {
        Preconditions.checkArgument(secondsToWait > 0 && secondsToWait < 60);
        try {
            logger.debug(String.format("Waiting for %d seconds.", secondsToWait));
            TimeUnit.SECONDS.sleep(secondsToWait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for a specific number of seconds.
     *
     * @param secondsToWait A number between 1 and 60.
     * @param reason        reason for the wait
     * @throws RuntimeException When interrupted.
     */
    public void forSeconds(int secondsToWait, String reason) {
        Preconditions.checkArgument(secondsToWait > 0 && secondsToWait < 60);
        try {
            logger.debug(String.format("Waiting '%d' sec with reason: '%s'", secondsToWait, reason));
            TimeUnit.SECONDS.sleep(secondsToWait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for a specific number of milliseconds.
     *
     * @param millisToWait A number between 1 and 1000.
     * @throws RuntimeException When interrupted.
     */
    public void forMilliSeconds(int millisToWait) {
        Preconditions.checkArgument(millisToWait > 0 && millisToWait < 1000);
        try {
            logger.debug(String.format("Waiting for %dms.", millisToWait));
            TimeUnit.MILLISECONDS.sleep(millisToWait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
