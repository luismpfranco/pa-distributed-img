import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    private static Thread serverThread;
    private static Client client;
    private static BufferedImage image;
    private static LoadInfo loadInfo;

    private static String host;

    @BeforeAll
    public static void setup() throws IOException {
        // Start the server in a new thread
        serverThread = new Thread(new ServerTest(8888));
        serverThread.start();

        // Create a new client
        client = new Client("TestClient", new LoadInfo("load.info"), 1, 1);
        // Load an image
        image = ImageReader.readImage("sample.png");

        loadInfo = new LoadInfo("load.info");

        host = "localhost";
    }

    @Test
    public void testSendRequestAndReceiveResponse() {

        // Define the host and port
        String host = "localhost";
        int port = 8888;

        // Create a request
        Request request = new Request("Test", "This is a test request", image);

        // Send the request and receive the response
        Response response = client.sendRequestAndReceiveResponse(host, port, request);

        // Assert that the response is not null
        assertNotNull(response);

        // Assert that the response status is as expected
        assertEquals("Success", response.getStatus());

        // Assert that the response message is as expected
        assertEquals("Request processed successfully", response.getMessage());
    }

    @Test
    public void testSendImagePart() {
        // Send the image part
        client.sendImagePart(image, "sample", "png");

        // Assert that the processed image part is not null
        assertNotNull(client.getProcessedImageParts()[0][0]);

        // Process the image by removing the reds
        BufferedImage processedImage = ImageTransformer.removeReds(image);

        // Join the processed image parts back into a single image
        BufferedImage joinedImage = ImageTransformer.joinImages(client.getProcessedImageParts(), image.getWidth(), image.getHeight(), image.getType());

        // Assert that the processed image part is not null
        assertNotNull(joinedImage);

        ColorModel clientResultColor = joinedImage.getColorModel();

        // Assert that the processed image part is as expected
        assertEquals(processedImage.getColorModel(), clientResultColor);
    }

    @Test
    public void testGetters(){
        assertEquals("TestClient", client.getName());
        assertEquals(1, client.getTotalRows());
        assertEquals(1, client.getTotalColumns());
    }

    @Test
    void testSendRequestAndReceiveResponseWithInvalidHost() {
        Client client = new Client("TestClient", new LoadInfo("load.info"), 1, 1);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Request request = new Request("Test", "This is a test request", image);

        // Test with an invalid host
        Response response = client.sendRequestAndReceiveResponse("invalid_host", 8888, request);
        assertNull(response, "Response should be null when an UnknownHostException is thrown");
    }

    @Test
    void testSendRequestAndReceiveResponseWithInvalidPort() {
        Client client = new Client("TestClient", new LoadInfo("load.info"), 1, 1);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Request request = new Request("Test", "This is a test request", image);

        // Test with an invalid port
        Response response = client.sendRequestAndReceiveResponse("localhost",8888 , request);
        assertNull(null, "Response should be null when an IOException is thrown");
    }

    @Test
    void testSendImagePartWithNullImage() {
        Client client = new Client("TestClient", new LoadInfo("load.info"), 1, 1);

        // Test with a null image
        assertThrows(RuntimeException.class, () -> client.sendImagePart(null, "sample", "png"), "A RuntimeException should be thrown when an IOException is thrown");
    }
}