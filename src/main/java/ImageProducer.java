import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.BlockingQueue;
/**
 * A class that produces image parts.
 */

public class ImageProducer implements Runnable {
    /**
     * The image to use.
     */
    private final BufferedImage image;
    /**
     * The number of rows to use.
     */
    private final int nRows;
    /**
     * The number of columns to use.
     */
    private final int nCols;
    /**
     * The queue to use.
     */
    private final BlockingQueue<ImagePart> queue;
    /**
     * The servers to use.
     */
    private final List<Server> servers;

    /**
     * Constructs a new image producer with the specified image, number of rows, number of columns, queue, and servers.
     *
     * @param image  The image to use.
     * @param nRows  The number of rows to use.
     * @param nCols  The number of columns to use.
     * @param queue  The queue to use.
     * @param servers The servers to use.
     */

    public ImageProducer(BufferedImage image, int nRows, int nCols, BlockingQueue<ImagePart> queue, List<Server> servers) {
        this.image = image;
        this.nRows = nRows;
        this.nCols = nCols;
        this.queue = queue;
        this.servers = servers;
    }

    /**
     * Produces image parts and puts them in the queue.
     */

    @Override
    public void run() {
        BufferedImage[][] subImages = ImageTransformer.splitImage(image, nRows, nCols);
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                try {
                    if (isServerAvailable()) {
                        queue.put(new ImagePart(subImages[i][j], i, j));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Checks if a server is available.
     *
     * @return True if a server is available, false otherwise.
     */

    private boolean isServerAvailable() {
        for (Server server : servers) {
            if (server.getWorkload() < server.getMaxWorkload() && server.getWorkload() > 0){
                return true;
            }
        }
        return false;
    }
}