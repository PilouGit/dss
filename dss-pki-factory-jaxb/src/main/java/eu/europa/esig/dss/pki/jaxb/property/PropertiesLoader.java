package eu.europa.esig.dss.pki.jaxb.property;


import eu.europa.esig.dss.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A utility class to load properties from a properties file named "pki.properties".
 * This class provides a method to retrieve property values based on given keys.
 */
public final class PropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    /**
     * The name of the properties file to be loaded.
     */
    public static final String APPLICATION_PROPERTIES = "pki.properties";

    /** Singleton */
    private static Properties prop;

    /**
     * Loads properties from the "pki.properties" file located in the classpath.
     *
     * @return {@link Properties} object containing the loaded properties.
     */
    private static Properties getProperties() {
        if (prop == null) {
            prop = new Properties();
            try (InputStream inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOG.error("Could not load properties file : {}", e.getMessage(), e);
            }
        }
        return prop;
    }

    /**
     * Retrieves the value of a property with the given key from the properties file.
     *
     * @param propertyKey  The key of the property to retrieve.
     * @return The value of the property if found, NULL otherwise.
     */
    public static String getProperty(String propertyKey) {
        return getProperty(propertyKey, null);
    }

    /**
     * Retrieves the value of a property with the given key from the properties file,
     * with the possibility to define a default property.
     *
     * @param propertyKey  The key of the property to retrieve.
     * @param defaultValue A default value to be returned if the property is not found or empty.
     * @return The value of the property if found, or the defaultValue if the property is not found or NULL.
     */
    public static String getProperty(String propertyKey, String defaultValue) {
        String value = getProperties().getProperty(propertyKey);
        if (Utils.isStringNotBlank(value)) {
            return value;
        } else if (defaultValue != null) {
            return defaultValue;
        }
        return null;
    }

}
