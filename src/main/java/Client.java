import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A simple TCP/IP client that connects to a server, sends an object, and receives a response.
 */
public class Client {

    /**
     * The name of the client.
     */
    private final String name;
    /**
     * The load information.
     */
    private final LoadInfo loadInfo;
    /**
     * The processed image parts.
     */
    private final BufferedImage[][] processedImageParts;
    /**
     * The current row.
     */
    private int currentRow = 0;
    /**
     * The current column.
     */
    private int currentColumn = 0;
    /**
     * The total number of rows.
     */
    private final int totalRows;
    /**
     * The total number of columns.
     */
    private final int totalColumns;


    /**
     * Constructs a new client with the specified name.
     *
     * @param name The name of the client.
     */
    public Client ( String name, LoadInfo loadInfo, int totalRows, int totalColumns ) {
        this.name = name;
        this.loadInfo = loadInfo;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.processedImageParts = new BufferedImage[totalRows][totalColumns];
    }

    /**
     * Sends an object to a specified server and waits for a response.
     *
     * @param host    The hostname or IP address of the server.
     * @param port    The port number of the server.
     * @param request The request object to send to the server.
     *
     * @return The response object from the server, or null in case of an error.
     */
    public Response sendRequestAndReceiveResponse ( String host , int port , Request request ) {
        try ( Socket socket = new Socket ( host , port ) ) {

            // Create and initialize the streams for sending and receiving objects
            ObjectOutputStream out = new ObjectOutputStream ( socket.getOutputStream ( ) );
            ObjectInputStream in = new ObjectInputStream ( socket.getInputStream ( ) );

            // Send the request to the server
            System.out.println ( "Connecting to server at " + host + ":" + port );
            System.out.println ( "Sending: " + request );
            out.writeObject ( request );

            // Wait for and return the response from the server
            Response response = ( Response ) in.readObject ( );
            System.out.println ( "Received: " + response );
            return response;

        } catch ( UnknownHostException e ) {
            System.err.println ( "Server not found: " + e.getMessage ( ) );
        } catch ( IOException e ) {
            System.err.println ( "I/O Error: " + e.getMessage ( ) );
        } catch ( ClassNotFoundException e ) {
            System.err.println ( "Class not found: " + e.getMessage ( ) );
        }
        // Return null or consider a better error handling/return strategy
        return null;
    }

    /**
     * Sends an image part to the server and waits for a response.
     *
     * @param imagePart The image part to send to the server.
     */

    public void sendImagePart(BufferedImage imagePart, String name, String extension){
        String leastLoadedServerHost = loadInfo.getLeastLoadedServer();
        int port = Integer.parseInt(leastLoadedServerHost);
        String host = "localhost"; // Replace with "leastLoadedServerHost" if the server is running on a different machine
        Request request = new Request("imagePart", "Sending image part", imagePart);
        try {
            Response response = sendRequestAndReceiveResponse(host, port, request);
            // Handle the response here
            if (response != null) {
                System.out.println("Response status: " + response.getStatus());
                System.out.println("Response message: " + response.getMessage());
                // If the response contains an image, you can process it here
                if (response.getImageSection() != null) {
                    byte[] receivedImage = response.getImageSection();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(receivedImage));
                    // Process the received image...
                    File outputfile = new File("results/" + name +  "." + extension);
                    ImageIO.write(image, "png", outputfile);
                    System.out.println("Received image saved as " + outputfile.getPath());

                    if (currentRow < totalRows && currentColumn < totalColumns) {
                        processedImageParts[currentRow][currentColumn] = image;

                        // Update currentRow and currentColumn for the next image part
                        currentColumn++;
                        if (currentColumn == totalColumns) {
                            currentColumn = 0;
                            currentRow++;
                        }
                    }

                }
            } else {
                System.out.println("No response received from the server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the processed image parts.
     *
     * @return The processed image parts.
     */
    public BufferedImage[][] getProcessedImageParts() {
        return processedImageParts;
    }
    /**
     * Returns the name of the client.
     *
     * @return The name of the client.
     */

    public String getName() {
        return name;
    }

    /**
     * Returns the total number of rows.
     *
     * @return The total number of rows.
     */

    public int getTotalRows() {
        return totalRows;
    }

    /**
     * Returns the total number of columns.
     *
     * @return The total number of columns.
     */
    public int getTotalColumns() {
        return totalColumns;
    }
}