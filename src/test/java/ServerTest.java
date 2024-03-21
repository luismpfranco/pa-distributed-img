import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The ServerTest class implements tests for the Server class.
 */
public class ServerTest {
    /**
     * The server.
     */
    private Server server;
    /**
     * The load info.
     */
    private LoadInfo loadInfo;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        loadInfo = new LoadInfo("test_load.info");
        server = new Server(8080, loadInfo, 100);
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testGetPort() {
        assertEquals(8080, server.getPort());
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testWorkloadManagement() {
        assertEquals(0, server.getWorkload());
        assertEquals(100, server.getMaxWorkload());

        server.incrementWorkload();
        assertEquals(1, server.getWorkload());

        server.decrementWorkload();
        assertEquals(0, server.getWorkload());
    }
}