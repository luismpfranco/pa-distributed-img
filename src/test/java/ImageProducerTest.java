import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The ImageProducerTest class implements tests for the ImageProducer class.
 */
public class ImageProducerTest {
    /**
     * The image producer.
     */
    private ImageProducer imageProducer;
    /**
     * The image.
     */
    private BufferedImage image;
    /**
     * The number of rows.
     */
    private int nRows;
    /**
     * The number of columns.
     */
    private int nCols;
    /**
     * The queue.
     */
    private BlockingQueue<ImagePart> queue;
    /**
     * The servers.
     */
    private List<Server> servers;
    /**
     * The server.
     */
    private Server server;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setUp() {
        // Initialize your ImageProducer here
        image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        nRows = 2;
        nCols = 2;
        queue = new LinkedBlockingQueue<>();
        servers = new ArrayList<>();
        imageProducer = new ImageProducer(image, nRows, nCols, queue, servers);
        server = new Server(8888, new LoadInfo("load.info"), 5);
    }

    /**
     * Test the run method.
     */
    @Test
    void testRun() {
        assertTrue(queue.isEmpty());
        // Act
        imageProducer.run();
        // Assert
        // Check that the queue is not empty after running the ImageProducer
        assertTrue(queue.isEmpty());
    }

    /**
     * Test the isServerAvailable method when the server is available.
     */
    @Test
    void testIsServerAvailableWhenServerIsAvailable() {
        assertFalse(imageProducer.isServerAvailable());
        servers.add(server);
        server.setWorkload(1);
        assertTrue(imageProducer.isServerAvailable());
    }
    /**
     * Test the isServerAvailable method when the server is not available.
     */
    @Test
    void testIsServerAvailableWhenServerIsNotAvailable() {
        servers.add(new Server(8888, new LoadInfo("load.info"), 0));

        assertFalse(imageProducer.isServerAvailable());
    }
    /**
     * Test the run method when the server is available.
     */
    @Test
    void testRunWhenServerIsAvailable() {
        // Arrange
        servers.add(server);
        servers.get(0).setWorkload(1); // Make the server available

        // Act
        imageProducer.run();

        // Assert
        assertFalse(queue.isEmpty());
    }
}