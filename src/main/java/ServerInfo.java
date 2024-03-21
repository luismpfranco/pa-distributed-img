/**
 * ServerInfo
 *
 * This class is used to store the host and port of the server.
 *
 * @version 1.0
 */
public class ServerInfo {
    /**
     * The host of the server.
     */
    private String host;
    /**
     * The port of the server.
     */
    private int port;

    /**
     * Constructs a new ServerInfo with the specified host and port.
     *
     * @param host The host of the server.
     * @param port The port of the server.
     */
    public ServerInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the host of the server.
     *
     * @return The host of the server.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port of the server.
     *
     * @return The port of the server.
     */
    public int getPort() {
        return port;
    }
}
