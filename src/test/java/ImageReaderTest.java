import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ImageReaderTest {

    @Test
    public void testReadImage() throws IOException {
        // Create a test image file
        File testImageFile = new File("testImage.png");
        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(testImage, "png", testImageFile);

        // Read the image from the test file
        BufferedImage image = ImageReader.readImage(testImageFile.getPath());

        // Assert that the returned BufferedImage is not null
        assertNotNull(image);

        // Delete the test image file
        Files.delete(testImageFile.toPath());
    }

    @Test
    public void testReadImage_FileNotFoundException() {
        // Try to read a non-existent image file
        BufferedImage image = ImageReader.readImage("nonExistentFile.png");

        // Assert that the returned BufferedImage is null
        assertNull(image);
    }
}