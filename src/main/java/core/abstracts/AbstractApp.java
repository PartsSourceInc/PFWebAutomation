package core.abstracts;

import core.managers.DriverManager;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;

@Log4j2
public abstract class AbstractApp {

    protected static ThreadLocal<DriverManager> driverManager = ThreadLocal.withInitial(DriverManager::new);

    public static WebDriver getDriver() {
        return driverManager.get().getDriver();
    }

    public static WebDriver getDriverForChrome() {
        return driverManager.get().getDriverForChrome();
    }

    public static void closeApp() {
        driverManager.get().close();
    }

}
