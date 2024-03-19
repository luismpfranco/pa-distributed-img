import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTransformerTest {

    @Test
    public void testSplitImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        BufferedImage[][] splitImages = ImageTransformer.splitImage(image, 2, 2);

        assertEquals(2, splitImages.length);
        assertEquals(2, splitImages[0].length);
    }

    @Test
    public void testRemoveReds() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        BufferedImage noRedsImage = ImageTransformer.removeReds(image);

        assertNotNull(noRedsImage);
    }

    @Test
    public void testJoinImages() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        BufferedImage[][] splitImages = ImageTransformer.splitImage(image, 2, 2);
        BufferedImage joinedImage = ImageTransformer.joinImages(splitImages, image.getWidth(), image.getHeight(), image.getType());

        assertNotNull(joinedImage);
    }

    @Test
    public void testCreateImageFromBytes() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();

        BufferedImage imageFromBytes = ImageTransformer.createImageFromBytes(bytes);

        assertNotNull(imageFromBytes);
    }

    @Test
    public void testCreateBytesFromImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        byte[] bytes = ImageTransformer.createBytesFromImage(image);

        assertNotNull(bytes);
    }

    @Test
    public void testReconstructImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        BufferedImage[][] splitImages = ImageTransformer.splitImage(image, 2, 2);
        BufferedImage reconstructedImage = ImageTransformer.reconstructImage(splitImages);

        assertNotNull(reconstructedImage);
    }

    @Test
    public void testSaveEditedImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));
        ImageTransformer.saveEditedImage(image, "test", "png");

        File file = new File("results/test_edited.png");
        assertTrue(file.exists());
    }
}