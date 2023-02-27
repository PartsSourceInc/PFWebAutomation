package core.managers;

import core.enums.Property;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public final class PropertyManager {

    private static String config = "config";

    public static void setConfig(String configName) {
        config = configName;
    }

    private static String read(Property property) {
        try (InputStream i = new FileInputStream("src/main/resources/" + config + ".properties")) {
            Properties prop = new Properties();
            prop.load(i);

            return prop.getProperty(property.getValue());
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static String getProperty(Property property) {
        String prop = System.getProperty(property.getValue());
        if (Objects.isNull(prop)) {
            return read(property);
        } else {
            return prop;
        }
    }

}
