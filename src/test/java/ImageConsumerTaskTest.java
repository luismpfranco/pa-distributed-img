import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageConsumerTaskTest {
    private ImageConsumerTask consumerTask;
    private ImagePart imagePart;
    private Client client;
    private SIMDExecutor simdExecutor;
    private List<Server> servers;
    private ClientWindow clientWindow;
    private String fileNameWithoutExtension;
    private String fileExtension;
    private BlockingQueue<ImagePart> queue;

    @BeforeEach
    void setUp() {
        // Initialize your dependencies here
        // This is just an example, replace with your actual initialization code
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        simdExecutor = new SIMDExecutor(image, 2, 2);
        client = new Client("test", new LoadInfo("load.info"), 2, 2);
        servers = new ArrayList<>();
        clientWindow = new ClientWindow(new ImageProcessor(1, clientWindow), servers, client, simdExecutor);
        fileNameWithoutExtension = "test";
        fileExtension = "png";
        queue = new LinkedBlockingDeque<>();
        consumerTask = new ImageConsumerTask(queue, new ArrayList<>(), simdExecutor, client, fileNameWithoutExtension, fileExtension, clientWindow);
        imagePart = new ImagePart(image, 2, 2, simdExecutor);
    }

    @Test
    public void testProcessImagePart() throws InterruptedException {
        queue.add(imagePart);
        ImagePart imagePart = queue.take();
        assertEquals(2, imagePart.getRow());
        assertEquals(2, imagePart.getColumn());
        assertEquals(simdExecutor, imagePart.getSimdExecutor());
        //assertEquals(client, imagePart.getClient());
        //assertEquals(clientWindow, imagePart.getClientWindow());
        //assertEquals(fileExtension, imagePart.getFileExtension());
        //assertEquals(fileNameWithoutExtension + "_edited_10_10", imagePart.getFileName());
    }
}