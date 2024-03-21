import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ImageProducer implements Runnable {
    private final BufferedImage image;
    private final int nRows;
    private final int nCols;
    private final BlockingQueue<ImagePart> queue;
    private final List<Server> servers;

    public ImageProducer(BufferedImage image, int nRows, int nCols, BlockingQueue<ImagePart> queue, List<Server> servers) {
        this.image = image;
        this.nRows = nRows;
        this.nCols = nCols;
        this.queue = queue;
        this.servers = servers;
    }

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

    private boolean isServerAvailable() {
        for (Server server : servers) {
            if (server.getWorkload() < server.getMaxWorkload() && server.getWorkload() > 0){
                return true;
            }
        }
        return false;
    }
}