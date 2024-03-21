import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A TCP/IP server that listens for connections on a specified port and handles each client connection in a separate
 * thread.
 */
public class Server extends Thread {
    /**
     * The port number on which the server will listen for incoming connections.
     */
    private final int port;
    /**
     * The load information.
     */
    private LoadInfo loadInfo;
    /**
     * The workload of the server.
     */
    private int workload;
    /**
     * The maximum workload of the server.
     */
    private final int maxWorkload;

    /**
     * Constructs a new server with the specified port number, load information, and maximum workload.
     *
     * @param port       The port number on which the server will listen for incoming connections.
     * @param loadInfo   The load information.
     * @param maxWorkload The maximum workload of the server.
     */
    public Server ( int port, LoadInfo loadInfo, int maxWorkload) {
        this.port = port;
        this.workload = 0;
        this.loadInfo = loadInfo;
        this.maxWorkload = maxWorkload;
    }

    /**
     * Gets the port number on which the server will listen for incoming connections.
     *
     * @return The port number on which the server will listen for incoming connections.
     */
    public int getPort() {
        return port;
    }

    /**
     * The entry point of the server thread. Starts the server to accept and handle client connections.
     */
    @Override
    public void run ( ) {
        try {
            startServer ( );
        } catch ( Exception e ) {
            e.printStackTrace ( );
        }
    }

    /**
     * Initializes the server socket and continuously listens for client connections. Each client connection is handled
     * in a new thread.
     *
     * @throws IOException If an I/O error occurs when opening the socket.
     */
    private void startServer ( ) throws IOException {
        try ( ServerSocket serverSocket = new ServerSocket ( port ) ) {
            System.out.println ( "Server started on port " + port );

            // Create a thread pool with a fixed number of threads.
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            while ( true ) {
                Socket clientSocket = serverSocket.accept ( );

                // Submit a new task to the thread pool for each connected client.
                executorService.submit(new ClientHandler(clientSocket));
            }
        }
    }

    /**
     * Gets the workload of the server.
     *
     * @return The workload of the server.
     */

    public int getWorkload() {
        return workload;
    }

    /**
     * Gets the maximum workload of the server.
     *
     * @return The maximum workload of the server.
     */
    public int getMaxWorkload() {
        return this.maxWorkload;
    }

    /**
     * Increments the workload of the server.
     */
    public void incrementWorkload() {
        this.workload++;
        this.loadInfo.updateLoad(String.valueOf(this.port), this.workload);
        System.out.println("The server " + this.port + " has a workload of " + this.workload);
    }

    /**
     * Decrements the workload of the server.
     */
    public void decrementWorkload() {
        this.workload--;
        this.loadInfo.updateLoad(String.valueOf(this.port), this.workload);
        System.out.println("The server " + this.port + " has a workload of " + this.workload);
    }

    /**
     * Handles client connections. Reads objects from the client, processes them, and sends a response back.
     */
    private static class ClientHandler implements Runnable {
        /**
         * The client socket.
         */
        private final Socket clientSocket;
        /**
         * The output stream to send objects to the client.
         */
        private ObjectOutputStream out;
        /**
         * The input stream to read objects from the client.
         */
        private ObjectInputStream in;

        /**
         * Constructs a new ClientHandler instance.
         *
         * @param socket The client socket.
         */
        public ClientHandler ( Socket socket ) {
            this.clientSocket = socket;
        }

        /**
         * The entry point of the client handler thread. Manages input and output streams for communication with the
         * client.
         */
        @Override
        public void run ( ) {
            try {
                this.out = new ObjectOutputStream ( clientSocket.getOutputStream ( ) ) ;
                this.in = new ObjectInputStream ( clientSocket.getInputStream ( ) );

                Request request;
                // Continuously read objects sent by the client and respond to each.
                while ( ( request = ( Request ) in.readObject ( ) ) != null ) {
                    out.writeObject ( handleRequest ( request ) );
                }

                System.out.println(request.toString());

            } catch ( EOFException e ) {
                System.out.println ( "Client disconnected." );
            } catch ( IOException | ClassNotFoundException e ) {
                System.err.println ( "Error handling client: " + e.getMessage ( ) );
            } finally {
                try {
                    clientSocket.close ( );
                } catch ( IOException e ) {
                    System.err.println ( "Error closing client socket: " + e.getMessage ( ) );
                }
            }
        }



        /**
         * Processes the client's request and generates a response.
         *
         * @param request The object received from the client.
         *
         * @return The response object to be sent back to the client.
         */
        private Response handleRequest(Request request) {
            BufferedImage editedImage = ImageTransformer.removeReds(ImageTransformer.createImageFromBytes(request.getImageSection()));
            return new Response("OK", "Hello, Client!", editedImage);
        }
    }

}