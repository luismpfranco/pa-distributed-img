import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ImageConsumerTask implements Runnable {
    private final BlockingQueue<ImagePart> queue;
    private final List<Server> servers;
    private final SIMDExecutor simdExecutor;
    private final Client client;
    private final String fileNameWithoutExtension;
    private final String fileExtension;
    private final ClientWindow clientWindow;

    public ImageConsumerTask(BlockingQueue<ImagePart> queue, List<Server> servers, SIMDExecutor simdExecutor, Client client, String fileNameWithoutExtension, String fileExtension, ClientWindow clientWindow) {
        this.queue = queue;
        this.servers = servers;
        this.simdExecutor = simdExecutor;
        this.client = client;
        this.fileNameWithoutExtension = fileNameWithoutExtension;
        this.fileExtension = fileExtension;
        this.clientWindow = clientWindow;
    }

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