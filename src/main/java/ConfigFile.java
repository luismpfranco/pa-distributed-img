import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * A simple configuration file reader.
 */
public class ConfigFile extends Properties {
    /**
     * The configuration file properties.
     */
    Properties configFileProperties;

    /**
     * Constructs a new configuration file reader with the specified file path.
     *
     * @param configFilePath The path to the configuration file.
     */
    public ConfigFile(String configFilePath) {
        this.configFileProperties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath), StandardCharsets.UTF_8))){
            this.configFileProperties.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
            this.configFileProperties = null;
        }
    }

    /**
     * Gets the string property with the specified key.
     *
     * @param key The key of the property to get.
     * @return The string property with the specified key, or null if the key is not found.
     */

    public int getIntProperty(String key) {
        String property = this.configFileProperties.getProperty(key);
        return Integer.parseInt(property);
    }
}