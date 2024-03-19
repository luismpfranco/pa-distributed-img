import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    private Request request;
    private BufferedImage image;

    @BeforeEach
    public void setup() throws IOException {
        image = ImageIO.read(new File("sample.png"));
        request = new Request("TestType", "TestContent", image);
    }

    @Test
    public void testGetAndSetMessageType() {
        request.setMessageType("NewType");
        assertEquals("NewType", request.getMessageType());
    }

    @Test
    public void testGetAndSetMessageContent() {
        request.setMessageContent("NewContent");
        assertEquals("NewContent", request.getMessageContent());
    }

    @Test
    public void testGetAndSetImageSection() throws IOException {
        BufferedImage newImage = ImageIO.read(new File("sample.png"));
        byte[] newImageBytes = ImageTransformer.createBytesFromImage(newImage);
        request.setImageSection(newImageBytes);
        assertArrayEquals(newImageBytes, request.getImageSection());
    }
}