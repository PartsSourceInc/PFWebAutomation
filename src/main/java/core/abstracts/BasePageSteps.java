package core.abstracts;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePageSteps {

    FluentWait<WebDriver> webDriverWait = webDriverWait();

    private FluentWait<WebDriver> webDriverWait() {
        return new WebDriverWait(AbstractApp.getDriver(),30)
                .ignoring(NoSuchElementException.class).ignoring(TimeoutException.class);
    }

    protected void waitUntilClickable(WebElement webElement) {
        webDriverWait.until(ExpectedConditions.elementToBeClickable(webElement));
    }

}
