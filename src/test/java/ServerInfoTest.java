import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerInfoTest {

    @Test
    public void testServerInfo() {
        String expectedHost = "localhost";
        int expectedPort = 8080;
        ServerInfo serverInfo = new ServerInfo(expectedHost, expectedPort);

        assertEquals(expectedHost, serverInfo.getHost());
        assertEquals(expectedPort, serverInfo.getPort());
    }
}