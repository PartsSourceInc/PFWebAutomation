package core.abstracts;

import core.managers.PropertyManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.SlowLoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Clock;
import java.util.Set;

import static core.enums.Property.ENV;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class Page<T extends SlowLoadableComponent<T>> extends SlowLoadableComponent<T> {

    protected WebDriverWait webDriverWait = (WebDriverWait) webDriverWait();

    protected WebDriver webDriver = getDriver();

    protected Page() {
        super(Clock.systemDefaultZone(), 30);
        PageFactory.initElements(new AjaxElementLocatorFactory(AbstractApp.getDriver(), 60), this);
    }

    public WebDriver getDriver() {
        return AbstractApp.getDriver();
    }

    public String getCurrentPageUrl() {
        return getDriver().getCurrentUrl();
    }

    protected abstract String getPageUrl();

    public void refreshPage() {
        getDriver().navigate().refresh();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        final String fullPageUrl = getFullPageUrl();
        webDriver.navigate().to(fullPageUrl);
    }

    @Override
    public void isLoaded() throws Error {
        String actualPageTitle = getDriver().getTitle();
        String expectedPageTitle = getPageTitle();
        try {
            assertThat(actualPageTitle).isEqualToIgnoringCase(expectedPageTitle);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    protected String getPageTitle() {
        return "";
    }

    public String getFullPageUrl() {
        final String fullPageUrl = getBaseUrl() + getPageUrl();
        return fullPageUrl;
    }

    protected String getBaseUrl() {
        return PropertyManager.getProperty(ENV).toLowerCase();
    }

    public void loadPage() {
        load();
    }

    public String getFormattedEnvironmentUrl(String urlTemplate) {
        var formattedUrl = String.format(
                urlTemplate,
                PropertyManager.getProperty(ENV).toLowerCase()
        );
        return formattedUrl;
    }

//    public void refresh() {
//        webDriver.navigate().refresh();
//        waitUntilPageLoaded();
//    }

    public void waitUntilPageLoaded() {
        final String fullPageUrl = getFullPageUrl();
        final String pageTitle = getPageTitle();
        webDriverWait().until(ExpectedConditions.urlToBe(fullPageUrl));
        webDriverWait().until(ExpectedConditions.titleContains(pageTitle));
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isDisplayed(By by) {
        try {
            return getDriver().findElement(by).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void clearTextInputField(
            final WebElement textInputField) {
        textInputField.sendKeys(getCtrlAChord());
        textInputField.sendKeys(Keys.DELETE);
    }

    private String getCtrlAChord() {
        return Keys.chord(Keys.CONTROL, "a");
    }

    public void scrollElementIntoView(final WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public FluentWait<WebDriver> webDriverWait() {
        return new WebDriverWait(getDriver(), 10)
                .ignoring(NoSuchElementException.class).ignoring(TimeoutException.class);
    }

    public void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        getDriver().navigate().refresh();
        waitUntilPageLoaded();
    }


    public  Set<String> getWindowHandles() {
        return webDriver.getWindowHandles();
    }

    public void switchToWindow(String tabName) {
        webDriver
                .switchTo()
                .window(tabName);
    }

}
