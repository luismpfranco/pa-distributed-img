import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigFile extends Properties {
    Properties configFileProperties;

    public ConfigFile(String configFilePath) {
        this.configFileProperties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath), StandardCharsets.UTF_8))){
            this.configFileProperties.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
            this.configFileProperties = null;
        }
    }

    public int getIntProperty(String key) {
        String property = this.configFileProperties.getProperty(key);
        return Integer.parseInt(property);
    }
}