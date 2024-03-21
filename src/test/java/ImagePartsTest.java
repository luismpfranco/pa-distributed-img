import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The ImagePartsTest class implements tests for the ImageParts class.
 */
public class ImagePartsTest {
    /**
     * The image part.
     */
    private ImagePart imagePart;
    /**
     * The image.
     */
    private BufferedImage image;
    private SIMDExecutor simdExecutor;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        // Create a new image
        image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        // Create a new ImagePart object
        imagePart = new ImagePart(image, 0, 0,simdExecutor);
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testConstructor() {
        // Assert that the properties are set correctly
        assertEquals(image, imagePart.getImage());
        assertEquals(0, imagePart.getRow());
        assertEquals(0, imagePart.getColumn());
    }

    /**
     * Test the getImage method.
     */
    @Test
    public void testGetImage() {
        // Assert that the correct image is returned
        assertEquals(image, imagePart.getImage());
    }

    /**
     * Test the getRow method.
     */
    @Test
    public void testGetRow() {
        // Assert that the correct row is returned
        assertEquals(0, imagePart.getRow());
    }

    /**
     * Test the getColumn method.
     */
    @Test
    public void testGetColumn() {
        // Assert that the correct column is returned
        assertEquals(0, imagePart.getColumn());
    }

    /**
     * Test the setRow method.
     */
    @Test
    public void testSetImage() {
        // Create a new image
        BufferedImage newImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

        // Set the new image
        imagePart.setImage(newImage);

        // Assert that the image is updated correctly
        assertEquals(newImage, imagePart.getImage());
    }
}