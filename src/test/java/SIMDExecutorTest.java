import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SIMDExecutorTest {

    private static BufferedImage image;

    static {
        try {
            image = ImageIO.read(new File("sample.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructor() throws IOException {
        int nRows = 2;
        int nCols = 2;
        SIMDExecutor simdExecutor = new SIMDExecutor(image, nCols, nRows);

        assertEquals(image, simdExecutor.getImage());
        assertEquals(nRows, simdExecutor.getnRows());
        assertEquals(nCols, simdExecutor.getnCols());
    }

    @Test
    public void testExecuteMethod() throws IOException {
        int nRows = 2;
        int nCols = 2;
        SIMDExecutor simdExecutor = new SIMDExecutor(image, nCols, nRows);

        Color c;
        Color color_original;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                BufferedImage originalSubImage = simdExecutor.getSplitImages(i, j);
                simdExecutor.execute(i, j);
                BufferedImage subImage = simdExecutor.getSplitImages(i, j);

                assertNotNull(subImage);

                c = new Color(subImage.getRGB(0, 0));
                color_original = new Color(originalSubImage.getRGB(0, 0));

                assertEquals(0, c.getRed());
                assertEquals(color_original.getGreen(), c.getGreen());
                assertEquals(color_original.getBlue(), c.getBlue());
            }
        }
    }

    @Test
    public void testGetSplitImagesMethod() throws IOException {
        int nRows = 2;
        int nCols = 2;
        SIMDExecutor simdExecutor = new SIMDExecutor(image, nCols, nRows);

        simdExecutor.execute(0, 0);
        BufferedImage subImage = simdExecutor.getSplitImages(0, 0);

        assertNotNull(subImage);
    }
}