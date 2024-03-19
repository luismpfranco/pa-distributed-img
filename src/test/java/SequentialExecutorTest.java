import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;

public class SequentialExecutorTest {

    @Test
    public void testExecute() throws IOException {
        Server server = new Server(8888, new LoadInfo("load.info"), 2);

        BufferedImage image = ImageIO.read(new File("sample.png"));
        ImagePart imagePart = new ImagePart(image, 0, 0);

        SequentialExecutor sequentialExecutor = new SequentialExecutor(image, 2, 2, image.getWidth(), image.getHeight(), image.getType());

        sequentialExecutor.execute(server, imagePart);

        assertNotNull(imagePart.getImage());
    }

    @Test
    public void testRemoveRed() throws IOException {
        BufferedImage image = ImageIO.read(new File("sample.png"));

        SequentialExecutor sequentialExecutor = new SequentialExecutor(image, 2, 2, image.getWidth(), image.getHeight(), image.getType());

        BufferedImage processedImage = sequentialExecutor.removeRed(image);

        for (int x = 0; x < processedImage.getWidth(); x++) {
            for (int y = 0; y < processedImage.getHeight(); y++) {
                int rgb = processedImage.getRGB(x, y);
                assertEquals(0, (rgb >> 16) & 0xFF);
            }
        }
    }
}