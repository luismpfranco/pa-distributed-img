import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {

    private Response response;
    private BufferedImage image;

    @BeforeEach
    public void setup() throws IOException {
        image = ImageIO.read(new File("sample.png"));
        response = new Response("TestStatus", "TestMessage", image);
    }

    @Test
    public void testGetAndSetStatus() {
        response.setStatus("NewStatus");
        assertEquals("NewStatus", response.getStatus());
    }

    @Test
    public void testGetAndSetMessage() {
        response.setMessage("NewMessage");
        assertEquals("NewMessage", response.getMessage());
    }

    @Test
    public void testGetAndSetImageSection() throws IOException {
        BufferedImage newImage = ImageIO.read(new File("sample.png"));
        byte[] newImageBytes = ImageTransformer.createBytesFromImage(newImage);
        response.setImageSection(newImageBytes);
        assertArrayEquals(newImageBytes, response.getImageSection());
    }
}