import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ClientWindowTest {
    private ClientWindow clientWindow;
    private ImageProcessor imageProcessor;
    private BufferedImage image;
    private int numServers = 3;
    private Client client;
    private SIMDExecutor simdExecutor;
    private List<Server> servers = new LinkedList<>();
    private static final int nCols = 2;
    private static final int nRows = 2;
    private static final String name = "Luis";
    private LoadInfo loadInfo = new LoadInfo("test_load.info");
    private ImageIcon icon;
    @BeforeEach
    void setUp() {
        imageProcessor = new ImageProcessor(numServers, null);
        client = new Client(name,loadInfo,nRows,nCols);
        image = ImageReader.readImage("sample.png");
        simdExecutor = new SIMDExecutor(image,nCols,nRows);
        clientWindow = new ClientWindow(imageProcessor, servers, client, simdExecutor);
        clientWindow.setImageProcessor(imageProcessor);
        JLabel imageLabel = new JLabel();
        clientWindow.add(imageLabel);
        clientWindow.setFileExtension("png");
        clientWindow.setFileNameWithoutExtension("sample");
        //clientWindow.updateImage(image);
    }

    @Test
    void testUpdateImage() {
        clientWindow.updateImage(image);
        icon = clientWindow.getImageIcon();
        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }
    // Add more @Test methods here for the other methods in ClientWindow

    @Test
    void testGetFileNameWithoutExtension() {
        String expectedFileNameWithoutExtension = "sample";
        String actualFileNameWithoutExtension = clientWindow.getFileNameWithoutExtension();
        assertEquals(expectedFileNameWithoutExtension, actualFileNameWithoutExtension);
    }

    @Test
    void testGetFileExtension() {
        String expectedFileExtension = "png";
        String actualFileExtension = clientWindow.getFileExtension();
        assertEquals(expectedFileExtension, actualFileExtension);
    }
}