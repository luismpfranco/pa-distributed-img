import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The ConfigFileTest class implements tests for the ConfigFile class.
 */
public class ConfigFileTest {
    /**
     * The configuration file.
     */
    private ConfigFile configFile;
    /**
     * The test file.
     */
    private File testFile;

    /**
     * Setup before each test.
     *
     * @throws IOException if an I/O error occurs
     */

    @BeforeEach
    public void setup() throws IOException {
        // Create a test configuration file
        testFile = new File("configFileTest.config");
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        writer.write("testIntProperty=123\n");
        writer.close();

        // Load the test configuration file
        configFile = new ConfigFile(testFile.getPath());
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testGetIntProperty() {
        // Read an integer property
        int value = configFile.getIntProperty("testIntProperty");

        // Assert that the value is as expected
        assertEquals(123, value);
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testConstructor() {
        // Assert that the properties are loaded correctly
        assertNotNull(configFile.configFileProperties);
        assertEquals("123", configFile.configFileProperties.getProperty("testIntProperty"));
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testConstructor_FileNotFoundException() {
        // Try to load a non-existent configuration file
        ConfigFile configFile = new ConfigFile("nonExistentFile.properties");

        // Assert that the properties are null
        assertNull(configFile.configFileProperties);
    }
}