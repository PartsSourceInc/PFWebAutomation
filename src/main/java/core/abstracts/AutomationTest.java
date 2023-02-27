package core.abstracts;

import org.openqa.selenium.WebDriver;

/**
 * Base Automation Test interface. Provides access to the {@link WebDriver} for use
 * in Failure screenshots
 */
public interface AutomationTest {


    void executeTestSteps();

    default void executePreconditionSteps() {
    }

}
