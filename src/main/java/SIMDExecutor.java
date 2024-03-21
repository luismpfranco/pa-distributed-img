import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class SIMDExecutor {
    private final BufferedImage image;
    private final int nCols;
    private final int nRows;
    private BufferedImage[][] splitImages;
    private final Semaphore semaphore = new Semaphore(10); // Limit to 10 sub-images being processed at a time

    public SIMDExecutor(BufferedImage image, int nCols, int nRows) {
        this.image = image;
        this.nCols = nCols;
        this.nRows = nRows;
        this.splitImages = ImageTransformer.splitImage(image,nRows,nCols);
    }

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

    public BufferedImage getSplitImages(int i, int j) {
        return splitImages[i][j];
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getnCols() {
        return nCols;
    }

    public int getnRows() {
        return nRows;
    }
}

