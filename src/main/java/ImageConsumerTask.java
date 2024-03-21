import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * A task that consumes image parts from a blocking queue and processes them.
 */

public class ImageConsumerTask implements Runnable {
    /**
     * The blocking queue to consume image parts from.
     */
    private final BlockingQueue<ImagePart> queue;
    /**
     * The list of servers to use.
     */
    private final List<Server> servers;
    /**
     * The SIMD executor to use.
     */
    private final SIMDExecutor simdExecutor;
    /**
     * The client to use.
     */
    private final Client client;
    /**
     * The file name without extension to use.
     */
    private final String fileNameWithoutExtension;
    /**
     * The file extension to use.
     */
    private final String fileExtension;
    /**
     * The client window to use.
     */
    private final ClientWindow clientWindow;

    /**
     * Constructs a new image consumer task with the specified queue, servers, SIMD executor, client, file name without extension, file extension, and client window.
     *
     * @param queue                   The blocking queue to consume image parts from.
     * @param servers                 The list of servers to use.
     * @param simdExecutor            The SIMD executor to use.
     * @param client                  The client to use.
     * @param fileNameWithoutExtension The file name without extension to use.
     * @param fileExtension            The file extension to use.
     * @param clientWindow            The client window to use.
     */

    public ImageConsumerTask(BlockingQueue<ImagePart> queue, List<Server> servers, SIMDExecutor simdExecutor, Client client, String fileNameWithoutExtension, String fileExtension, ClientWindow clientWindow) {
        this.queue = queue;
        this.servers = servers;
        this.simdExecutor = simdExecutor;
        this.client = client;
        this.fileNameWithoutExtension = fileNameWithoutExtension;
        this.fileExtension = fileExtension;
        this.clientWindow = clientWindow;
    }

    /**
     * Consumes image parts from the queue and processes them.
     */

    @Override
    public void run() {
        while (true) {
            try {
                ImagePart imagePart = queue.take();
                boolean processed = false;
                for (Server server : servers) {
                    synchronized (server) {
                        if (server.getWorkload() < server.getMaxWorkload() && server.getWorkload() > 0) {
                            processImagePart(imagePart, client, simdExecutor, server);
                            processed = true;
                            break;
                        }
                    }
                }
                if (!processed) {
                    queue.add(imagePart);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Processes an image part.
     *
     * @param imagePart    The image part to process.
     * @param client       The client to use.
     * @param simdExecutor The SIMD executor to use.
     * @param server       The server to use.
     */

    private void processImagePart(ImagePart imagePart, Client client, SIMDExecutor simdExecutor, Server server) {
        simdExecutor.execute(imagePart.getRow(), imagePart.getColumn());
        Request request = new Request("greeting", "Hello, Server!", imagePart.getImage());
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
        BufferedImage processedSubImage = ImageTransformer.createImageFromBytes(response.getImageSection());
        imagePart.setImage(processedSubImage);
        clientWindow.updateImage(processedSubImage);
        client.sendImagePart(processedSubImage, fileNameWithoutExtension + "_edited_" + imagePart.getRow() + "_" + imagePart.getColumn(), fileExtension);
    }
}