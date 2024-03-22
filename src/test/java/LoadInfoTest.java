import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The LoadInfoTest class implements tests for the LoadInfo class.
 */
public class LoadInfoTest {
    /**
     * The load information.
     */
    private LoadInfo loadInfo;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        loadInfo = new LoadInfo("test_load.info");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("test_load.info"))) {
            writer.write("server1 10\n");
            writer.write("server2 20\n");
            writer.write("server3 30\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testUpdateLoad() {
        loadInfo.updateLoad("server1", 15);
        Map<String, Integer> loadMap = loadInfo.readLoadInfo();
        assertEquals(15, loadMap.get("server1"));
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testGetLeastLoadedServer() {
        String leastLoadedServer = loadInfo.getLeastLoadedServer();
        assertEquals("server1", leastLoadedServer);
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testReadLoadInfo() {
        Map<String, Integer> loadMap = loadInfo.readLoadInfo();
        assertEquals(10, loadMap.get("server1"));
        assertEquals(20, loadMap.get("server2"));
        assertEquals(30, loadMap.get("server3"));
    }

    /**
     * Cleanup after each test.
     */
    @Test
    public void testWriteLoadInfo() {
        Map<String, Integer> newLoadMap = Map.of("server1", 15, "server2", 25, "server3", 35);
        loadInfo.writeLoadInfo(newLoadMap);
        Map<String, Integer> loadMap = loadInfo.readLoadInfo();
        assertEquals(15, loadMap.get("server1"));
        assertEquals(25, loadMap.get("server2"));
        assertEquals(35, loadMap.get("server3"));
    }
}