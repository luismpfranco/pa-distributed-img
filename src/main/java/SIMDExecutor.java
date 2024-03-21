import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
/**
 * A class that executes SIMD operations on an image.
 */
public class SIMDExecutor {
    /**
     * The image to process.
     */
    private final BufferedImage image;
    /**
     * The number of columns to use.
     */
    private final int nCols;
    /**
     * The number of rows to use.
     */
    private final int nRows;
    /**
     * The split images.
     */
    private BufferedImage[][] splitImages;
    /**
     * The semaphore to use.
     */
    private final Semaphore semaphore = new Semaphore(10); // Limit to 10 sub-images being processed at a time

    /**
     * Constructs a new SIMDExecutor with the specified image, number of columns, and number of rows.
     *
     * @param image  The image to process.
     * @param nCols  The number of columns to use.
     * @param nRows  The number of rows to use.
     */
    public SIMDExecutor(BufferedImage image, int nCols, int nRows) {
        this.image = image;
        this.nCols = nCols;
        this.nRows = nRows;
        this.splitImages = ImageTransformer.splitImage(image,nRows,nCols);
    }

    /**
     * Executes the SIMD operation on the specified sub-image.
     *
     * @param i The row index of the sub-image.
     * @param j The column index of the sub-image.
     */

    public void execute(int i, int j){
        int subImageWidth = splitImages[i][j].getWidth();
        int subImageHeight = splitImages[i][j].getHeight();

        Color c;
        for (int x = 0; x < subImageWidth; x++) {
            for (int y = 0; y < subImageHeight; y++) {
                c = new Color(splitImages[i][j].getRGB(x, y));
                int g = c.getGreen();
                int b = c.getBlue();
                splitImages[i][j].setRGB(x, y, new Color(0, g, b).getRGB());
            }
        }
    }

    /**
     * Processes all sub-images.
     */

    public void processAllSubImages() {
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                final int finalI = i;
                final int finalJ = j;
                try {
                    semaphore.acquire();
                    new Thread(() -> {
                        try {
                            execute(finalI, finalJ);
                        } finally {
                            semaphore.release();
                        }
                    }).start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Gets the split images.
     *
     * @param i The row index of the sub-image.
     * @param j The column index of the sub-image.
     * @return The split images.
     */

    public BufferedImage getSplitImages(int i, int j) {
        return splitImages[i][j];
    }

    /**
     * Gets the image.
     *
     * @return The image.
     */

    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets the number of columns.
     *
     * @return The number of columns.
     */

    public int getnCols() {
        return nCols;
    }

    /**
     * Gets the number of rows.
     *
     * @return The number of rows.
     */
    public int getnRows() {
        return nRows;
    }
}

