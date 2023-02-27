package core.managers;

import core.enums.Property;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.openqa.selenium.firefox.FirefoxDriver.MARIONETTE;

@Log4j2
public class DriverManager {

    private WebDriver driver;

    public synchronized WebDriver setUp() {
        var browser = PropertyManager.getProperty(Property.BROWSER);

        if (Objects.nonNull(browser) && browser.equalsIgnoreCase("firefox")) {
            return setUpFirefox();
        } else {
            return setUpChrome();
        }
    }

    public synchronized WebDriver setUpChrome() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions chromeOptions = initializeChromeOptions();

        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

        return driver;
    }

    public synchronized WebDriver setUpFirefox() {
        WebDriverManager.firefoxdriver().forceDownload().setup();

        FirefoxOptions firefoxOptions = initializeFirefoxOptions();
        driver = new FirefoxDriver(firefoxOptions);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

        return driver;
    }

    private ChromeOptions initializeChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("--enable-automation");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--enable-precise-memory-info");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--no-sandbox");
        options.addArguments("--dns-prefetch-disable");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("test-type=browser");
        options.addArguments("--error-console");
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        final URL url = getClass().getResource("/" + "ranorex_extension_1_4_3_0.crx");
        final File file = new File(url.getPath());
        options.addExtensions(file);
//      options.addArguments("--auto-open-devtools-for-tabs");
        options.addArguments("--disable-extensions");

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        DesiredCapabilities capabilities = new DesiredCapabilities();
//        capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
//        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        log.debug(capabilities.toString());

        return options;
    }

    private FirefoxOptions initializeFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions()
//                .setLegacy(false)
                .addArguments("--marionette-port")
                .addArguments("2828")
//       .addArguments("--headless")
                .addPreference("devtools.selfxss.count", 100) // this helps to use console and evaluate xpath, e.g. $x(".//xpath")
                .setPageLoadStrategy(PageLoadStrategy.EAGER)
                .addPreference(MARIONETTE, true)
                .setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE)
                .setAcceptInsecureCerts(true);

        DesiredCapabilities capabilities = new DesiredCapabilities();
//        capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);

        log.debug(capabilities.toString());

        return options;
    }

    private String getPathTo(String nativeApp) {
        String path = URLDecoder.decode(this.getClass().getClassLoader().getResource("app" + File.separator + nativeApp).getPath(), StandardCharsets.UTF_8);
        return new File(path).getAbsolutePath();
    }

    public WebDriver getDriver() {
        return Objects.isNull(driver) ? setUp() : this.driver;
    }

    public WebDriver getDriverForChrome() {
        return Objects.isNull(driver) ? setUpChrome() : this.driver;
    }

    public void close() {
        if (Objects.nonNull(driver)) {
            driver.quit();
            driver = null;
        }
    }

}
