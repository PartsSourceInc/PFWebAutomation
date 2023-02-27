package core.abstracts;

import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.Widget;
import org.openqa.selenium.support.PageFactory;

public abstract class Component extends Widget {
    public Component(MobileElement element) {
        super(element);
        PageFactory.initElements(new AppiumFieldDecorator(element), this);
    }

}
