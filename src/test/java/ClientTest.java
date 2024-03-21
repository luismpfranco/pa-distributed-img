import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
/**
 * The ClientTest class implements tests for the Client class.
 */
public class ClientTest {
    /**
     * The server thread.
     */
    private static Thread serverThread;
    /**
     * The client.
     */
    private static Client client;
    /**
     * The image.
     */
    private static BufferedImage image;
    /**
     * The load information.
     */
    private static LoadInfo loadInfo;
    /**
     * The host.
     */
    private static String host;
    /**
     * The port.
     */
    private static int port = 8888;
    /**
     * The request.
     */
    private static Request request;

    /**
     * Setup before all tests.
     *
     * @throws IOException if an I/O error occurs
     */

    @BeforeAll
    public static void setup() throws IOException {
        // Start the server in a new thread
        serverThread = new Thread(new ServerSocketTest(port));
        serverThread.start();

        // Create a new client
        client = new Client("TestClient", new LoadInfo("load.info"), 1, 1);
        // Load an image
        image = ImageReader.readImage("sample.png");

        loadInfo = new LoadInfo("load.info");

        host = "localhost";

        request = new Request("Test", "This is a test request", image);
    }

    /**
     * Tear down after all tests.
     */
    @Test
    public void testSendRequestAndReceiveResponse() {
        // Send the request and receive the response
        Response response = client.sendRequestAndReceiveResponse(host, port, request);

        // Assert that the response is not null
        assertNotNull(response);

        // Assert that the response status is as expected
        assertEquals("Success", response.getStatus());

        // Assert that the response message is as expected
        assertEquals("Request processed successfully", response.getMessage());
    }

    /**
     * Tear down after all tests.
     */
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

    /**
     * Tear down after all tests.
     */
    @Test
    public void testGetters(){
        assertEquals("TestClient", client.getName());
        assertEquals(1, client.getTotalRows());
        assertEquals(1, client.getTotalColumns());
    }

    /**
     * Tear down after all tests.
     */
    @Test
    void testSendRequestAndReceiveResponseWithInvalidHost() {
        // Test with an invalid host
        Client clientInvalid = new Client("TestClient", new LoadInfo("load.info"), 1, 1);
        Response response = clientInvalid.sendRequestAndReceiveResponse("invalid_host", 8888, request);
        assertNull(response, "Response should be null when an UnknownHostException is thrown");
    }

    /**
     * Tear down after all tests.
     */
    @Test
    void testSendRequestAndReceiveResponseWithInvalidPort() {
        // Test with an invalid port
        Response response = client.sendRequestAndReceiveResponse("localhost",8000, request);
        assertNull(response, "Response should be null when an IOException is thrown");
    }

    /**
     * Tear down after all tests.
     */
    @Test
    void testSendImagePartWithNullImage() {
        // Test with a null image
        assertThrows(RuntimeException.class, () -> client.sendImagePart(null, "sample", "png"), "A RuntimeException should be thrown when an IOException is thrown");
    }
}