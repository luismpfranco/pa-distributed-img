import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class ImagePartsTest {

    private ImagePart imagePart;
    private BufferedImage image;

    @BeforeEach
    public void setup() {
        // Create a new image
        image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        // Create a new ImagePart object
        imagePart = new ImagePart(image, 0, 0);
    }

    @Test
    public void testConstructor() {
        // Assert that the properties are set correctly
        assertEquals(image, imagePart.getImage());
        assertEquals(0, imagePart.getRow());
        assertEquals(0, imagePart.getColumn());
    }

    @Test
    public void testGetImage() {
        // Assert that the correct image is returned
        assertEquals(image, imagePart.getImage());
    }

    @Test
    public void testGetRow() {
        // Assert that the correct row is returned
        assertEquals(0, imagePart.getRow());
    }

    @Test
    public void testGetColumn() {
        // Assert that the correct column is returned
        assertEquals(0, imagePart.getColumn());
    }

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