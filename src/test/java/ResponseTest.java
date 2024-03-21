import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The ResponseTest class implements tests for the Response class.
 */
public class ResponseTest {
    /**
     * The response.
     */
    private Response response;
    /**
     * The image.
     */
    private BufferedImage image;

    /**
     * Setup before each test.
     *
     * @throws IOException if an I/O error occurs
     */
    @BeforeEach
    public void setup() throws IOException {
        image = ImageIO.read(new File("sample.png"));
        response = new Response("TestStatus", "TestMessage", image);
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testGetAndSetStatus() {
        response.setStatus("NewStatus");
        assertEquals("NewStatus", response.getStatus());
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testGetAndSetMessage() {
        response.setMessage("NewMessage");
        assertEquals("NewMessage", response.getMessage());
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testGetAndSetImageSection() throws IOException {
        BufferedImage newImage = ImageIO.read(new File("sample.png"));
        byte[] newImageBytes = ImageTransformer.createBytesFromImage(newImage);
        response.setImageSection(newImageBytes);
        assertArrayEquals(newImageBytes, response.getImageSection());
    }
}