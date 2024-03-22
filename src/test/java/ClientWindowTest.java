import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ClientWindowTest {
    /**
     * The client window.
     */
    private ClientWindow clientWindow;
    /**
     * The image processor.
     */
    private ImageProcessor imageProcessor;
    /**
     * The image.
     */
    private BufferedImage image;
    /**
     * The client.
     */
    private int numServers = 3;
    /**
     * The client.
     */
    private Client client;
    /**
     * The SIMD executor.
     */
    private SIMDExecutor simdExecutor;
    /**
     * The servers.
     */
    private List<Server> servers = new LinkedList<>();
    private static final int nCols = 2;
    private static final int nRows = 2;
    private static final String name = "Luis";
    private LoadInfo loadInfo = new LoadInfo("test_load.info");
    /**
     * The icon.
     */
    private ImageIcon icon;

    @BeforeAll
    static void setUpClass() {
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setUp() {
        clientWindow = new ClientWindow(null, servers, client, simdExecutor);
        client = new Client(name,loadInfo,nRows,nCols);
        image = ImageReader.readImage("sample.png");
        simdExecutor = new SIMDExecutor(image,nCols,nRows);
        imageProcessor = new ImageProcessor(numServers, clientWindow);
        clientWindow.setImageProcessor(imageProcessor);
        JLabel imageLabel = new JLabel();
        clientWindow.add(imageLabel);
        clientWindow.setFileExtension("png");
        clientWindow.setFileNameWithoutExtension("sample");
        //clientWindow.updateImage(image);
    }

    /**
     * Tests the constructor of the ClientWindow class.
     */
    @Test
    void testUpdateImage() {
        clientWindow.updateImage(image);
        icon = clientWindow.getImageIcon();
        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    /**
     * Tests the getFileNameWithoutExtension method of the ClientWindow class.
     */
    @Test
    void testGetFileNameWithoutExtension() {
        String expectedFileNameWithoutExtension = "sample";
        String actualFileNameWithoutExtension = clientWindow.getFileNameWithoutExtension();
        assertEquals(expectedFileNameWithoutExtension, actualFileNameWithoutExtension);
    }

    /**
     * Tests the getFileExtension method of the ClientWindow class.
     */
    @Test
    void testGetFileExtension() {
        String expectedFileExtension = "png";
        String actualFileExtension = clientWindow.getFileExtension();
        assertEquals(expectedFileExtension, actualFileExtension);
    }
}