import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * The ServerInfoTest class implements tests for the ServerInfo class.
 */
public class ServerInfoTest {
    /**
     * Test the constructor.
     */
    @Test
    public void testServerInfo() {
        String expectedHost = "localhost";
        int expectedPort = 8080;
        ServerInfo serverInfo = new ServerInfo(expectedHost, expectedPort);

        assertEquals(expectedHost, serverInfo.getHost());
        assertEquals(expectedPort, serverInfo.getPort());
    }
}