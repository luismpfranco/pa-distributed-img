import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The ServerSocketTest class implements a server socket for testing purposes.
 */
public class ServerSocketTest implements Runnable {
    /**
     * The server socket.
     */
    private ServerSocket serverSocket;
    /**
     * Constructs a new ServerSocketTest.
     *
     * @param port the port
     * @throws IOException if an I/O error occurs
     */
    public ServerSocketTest(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    /**
     * Runs the server socket.
     */
    @Override
    public void run() {
        while (true) {
            try (Socket socket = serverSocket.accept();
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                // Read the request from the client
                Request request = (Request) in.readObject();

                BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

                // Create a response
                Response response = new Response("Success", "Request processed successfully", image);

                // Send the response to the client
                out.writeObject(response);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}