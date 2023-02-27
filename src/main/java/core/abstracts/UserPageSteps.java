package core.abstracts;

import core.Wait;
import core.logging.LoggingFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

public abstract class UserPageSteps {

    protected static final Logger logger = LoggingFactory.getLogger();
    final private static String PROPERTIES_FILE_PATH = "./constants-test.properties";
    protected WebDriverWait webDriverWait = webDriverWait();
    protected Wait wait = new Wait();
    protected Actions uiActions = new Actions(AbstractApp.getDriver());
    protected WebDriver webDriver = AbstractApp.getDriver();
    public SoftAssert softAssert = new SoftAssert();
    public String osNameProperty = System.getProperty("os.name").toLowerCase();

    public String getStringConstant(final String key) {
        final var constantValue = getPropertyConstant(key, PROPERTIES_FILE_PATH);
        return constantValue;
    }

    private String getPropertyConstant(final String key, final String filePath) {
        String property;
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(filePath)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            property = properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (property == null) {
            throw new RuntimeException(String.format("Property value for key '%s' was not found", key));
        }
        return property;
    }

    public WebDriverWait webDriverWait() {
        WebDriverWait driverWait = new WebDriverWait(AbstractApp.getDriver(), 30);
        driverWait.withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);
        return driverWait;
    }

    public WebDriverWait webDriverLongWait() {
        WebDriverWait driverWait = new WebDriverWait(AbstractApp.getDriver(), 300);
        driverWait.withTimeout(Duration.ofSeconds(300))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);
        return driverWait;
    }

    public Actions uiActions() {
        return new Actions(AbstractApp.getDriver());
    }

    protected void clearTextInputField(
            final WebElement textInputField) {
        textInputField.sendKeys(getCtrlAChord());
        textInputField.sendKeys(Keys.DELETE);
    }

    protected void clearInputField(
            final WebElement textInputField) {
        try {
            var length = textInputField.getAttribute("value").length();
            logger.info("total length is "+length);
            for (int i = 0; i < length; i++) {

                if (osNameProperty.contains("mac")) {
                    uiActions.doubleClick(textInputField)
                            .sendKeys(Keys.DELETE)
                            .build()
                            .perform();
//                    textInputField.click();
//                    textInputField.sendKeys(Keys.DELETE);

                }else  if (osNameProperty.contains("windows")){
                    textInputField.click();
                    textInputField.sendKeys(Keys.BACK_SPACE);
                }

            }
        } catch (Exception e) {
            clearTextInputField(textInputField);
        }
    }

    private String getCtrlAChord() {
        return Keys.chord(Keys.CONTROL, "a");
    }

    protected void waitUntilClickable(WebElement webElement) {
        webDriverWait().until(ExpectedConditions.elementToBeClickable(webElement));
    }

    protected void click(WebElement element) {
        Actions uiActions = uiActions();
        try {
            uiActions
                    .moveToElement(element)
                    .build()
                    .perform();
            uiActions
                    .click(element)
                    .build()
                    .perform();
        } catch (StaleElementReferenceException | NoSuchElementException ex) {
            uiActions
                    .moveToElement(element)
                    .build()
                    .perform();
            uiActions
                    .click(element)
                    .build()
                    .perform();
        }
    }

    protected void click(WebElement element, int pause) {
        Actions uiActions = uiActions();
        try {
            uiActions
                    .click(element)
                    .pause(Duration.ofSeconds(pause))
                    .build()
                    .perform();
        } catch (StaleElementReferenceException | NoSuchElementException ex) {
            uiActions
                    .click(element)
                    .pause(Duration.ofSeconds(pause))
                    .build()
                    .perform();
        }
    }

    public void text(WebElement element, String text) {
        Actions uiActions = uiActions();
      //  osNameProperty = System.getProperty("os.name").toLowerCase();
        if (osNameProperty.contains("mac")) {
            uiActions.click(element)
                    .keyDown(Keys.COMMAND)
                    .sendKeys("a")
                    .keyUp(Keys.COMMAND)
                    .sendKeys(Keys.DELETE)
                    .build()
                    .perform();

        uiActions
                .sendKeys(element, text)
                .build()
                .perform();
        }else if (osNameProperty.contains("windows")) {
            uiActions.click(element)
                    .keyDown(Keys.CONTROL)
                    .sendKeys("a")
                    .keyUp(Keys.CONTROL)
                    .sendKeys(Keys.BACK_SPACE)
                    .build()
                    .perform();
            uiActions
                    .sendKeys(element, text)
                    .build()
                    .perform();

        }

//        Actions uiActions = uiActions();
//        uiActions
//                .sendKeys(element, text)
//                .build()
//                .perform();
    }

    protected void textWithPause(WebElement element, String text, int pause) {
        Actions uiActions = uiActions();
        text.codePoints()
                .mapToObj(e -> (char) e)
                .map(String::valueOf)
                .forEach(
                        idCharacter -> {
                            element.sendKeys(idCharacter);
                            uiActions
                                    .pause(Duration.ofMillis(pause))
                                    .build()
                                    .perform();
                        });
    }

//    protected Boolean waitForElementToDisappear(WebElement element) {
//        boolean checkCondition ;
//
//        try {
//            webDriverWait().until(ExpectedConditions.invisibilityOf(element));
//            checkCondition = true;
//        } catch (Exception e) {
//            logger.info("element is present in the page");
//            checkCondition = false;
//        }
//        return checkCondition;
//    }

    public boolean elementNotPresentCheck(WebElement element) {

        boolean checkCondition ;

        try {
            webDriverWait().until(ExpectedConditions.invisibilityOf(element));
            checkCondition = false;
            Assert.fail("element is present in the group");
        } catch (Exception e) {
            logger.info("element is not present in the page");
            checkCondition = true;
        }
        return checkCondition;

    }


    protected void waitForElementToDisappear(By selector) {
        try {
            webDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(selector));
        } catch (Exception e) {
        }
    }

    public boolean isElementDisplayed(String locator) {
        try {
            return webDriver.findElement(By.cssSelector(locator)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isElementExist(String locator) {
        return webDriver.findElements(By.cssSelector(locator)).size() > 0;
    }

    public void switchFromFrameToDefault() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            try {
                wait.forSeconds(1);
                webDriver
                        .switchTo()
                        .defaultContent();
            } catch (Exception e) {
                logger.error(e.getMessage());
                webDriver
                        .switchTo()
                        .defaultContent();
                wait.forSeconds(1);
                break;
            }
        }
    }

    public void waitForLoaderToDisappear() {
//        WebElement loaderDisappear = webDriver.findElement(By.cssSelector(".loader"));
//        try{
//            new FluentWait<WebDriver>(webDriver)
//                    .withTimeout(30, TimeUnit.SECONDS)
//                    .pollingEvery(1,TimeUnit.SECONDS)
//                    .ignoring(NoSuchElementException.class)
//                    .until((ExpectedCondition<Boolean>) driver -> (!loaderDisappear.isDisplayed())
//                    );
//        }catch(TimeoutException e){
//            fail("Time Out On : "+loaderDisappear);
//        }
        try {
            webDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loader")));
        }catch (Exception e) {
            webDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loader")));
        }
       // wait.forSeconds(1);
    }

    public Set<String> getWindowHandles() {
        return webDriver.getWindowHandles();
    }

    public void switchToWindow(String tabName) {
        webDriver
                .switchTo()
                .window(tabName);
    }

    public Boolean validateDate(String dateFormatCheck){
        Boolean checkDate;

        DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(dateFormatCheck);
            checkDate = true;
        } catch (ParseException e) {
            checkDate = false;
        }
               return  checkDate;
    }

    // Function to validate the time in 12-hour format.
    public static boolean isValidTime(String time)
    {

        // Regex to check valid time in 12-hour format.
        String regexPattern
                = "(1[012]|[1-9]):"
                + "[0-5][0-9](\\s)"
                + "?(?i)(am|pm)";

        // Compile the ReGex
        Pattern compiledPattern
                = Pattern.compile(regexPattern);

        // If the time is empty
        // return false
        if (time == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given time
        // and regular expression.
        Matcher m = compiledPattern.matcher(time);

        // Return if the time
        // matched the ReGex
        return m.matches();
    }


    public void refreshPage() {
        webDriver.navigate().refresh();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void selectByText(WebElement element, String selectByText) {
        webDriverWait.until(elementToBeClickable(element));
        Select printDocSelect = new Select(element);
        printDocSelect.selectByVisibleText(selectByText);
        logger.info("selected the Text successfully");
        wait.forSeconds(1);
        // Assert.assertEquals(printDocSelect.getFirstSelectedOption().getText().trim(),selectByText);

    }

    public String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Step("Subtracting Days")
    public String subtractDaysFromCurrentDate(int days) {
        String somedate_in_the_past = null;
        days = days > 0 ? (days * -1) : days;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            logger.info("Subtracting Days");
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, days);
            somedate_in_the_past = dtf.format(LocalDateTime.from(cal.toZonedDateTime()));
            logger.info("Subtracting " + days + " day(s) from the current day(" + getCurrentDate() + "): "
                    + somedate_in_the_past);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Test Failed due to an exception:::" + e.getMessage());
            Assert.fail(e.getMessage());
        }
        return somedate_in_the_past;
    }


}
