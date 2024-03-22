import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * The Main class is the entry point of the application.
 */
public class Main {
    /**
     * The name of the image to process.
     */
    private static final String IMAGE_NAME = "sample.png";
    /**
     * The configuration file to use.
     */
    private static final ConfigFile CONFIG_FILE = new ConfigFile ( "project.config" );
    /**
     * The number of rows to use.
     */
    private static final int NUM_ROWS = CONFIG_FILE.getIntProperty ( "num_rows" );
    /**
     * The number of columns to use.
     */
    private static final int NUM_COLUMNS = CONFIG_FILE.getIntProperty ( "num_columns" );
    /**
     * The number of servers to use.
     */
    private static final int NUM_SERVERS = CONFIG_FILE.getIntProperty ( "num_servers" );
    /**
     * The maximum workload to use.
     */
    private static final int MAX_WORKLOAD = CONFIG_FILE.getIntProperty ( "max_workload" );

    /**
     * The main method of the application.
     *
     * @param args The command line arguments.
     */

    public static void main ( String[] args ) {

        LoadInfo loadInfo = new LoadInfo("load.info");
        Map<String, ServerInfo> serverInfoMap = initializaServerInfoMap();
        List<Server> servers = startServers(loadInfo, serverInfoMap);
        BufferedImage sampleImage = ImageReader.readImage(IMAGE_NAME);
        ImageProcessor imageProcessor = new ImageProcessor(NUM_SERVERS, null);
        Client client = new Client("Client A", loadInfo, NUM_ROWS, NUM_COLUMNS);
        SIMDExecutor simdExecutor = new SIMDExecutor(sampleImage, NUM_COLUMNS, NUM_ROWS);
        BlockingQueue<ImagePart> waitingList = new LinkedBlockingQueue<>();

        JFrame frame = new JFrame("pa-distributed-img");
        frame.setSize(600, 600);
        JPanel panel = new JPanel();
        ImageIcon icon = new ImageIcon(sampleImage);
        JLabel label = new JLabel(icon);
        panel.add(label);
        frame.add(panel);

        JButton button = new JButton();

        button.setText("Remove red (SIMD)");
        button.addActionListener(e -> {

            String fileNameWithoutExtension = IMAGE_NAME.substring(0, IMAGE_NAME.lastIndexOf('.'));
            String fileExtension = IMAGE_NAME.substring(IMAGE_NAME.lastIndexOf(".") + 1);

            BufferedImage editedImage = imageProcessor.processImage(sampleImage, servers, client, simdExecutor, fileNameWithoutExtension, fileExtension);
            icon.setImage(editedImage);
            label.repaint();
        });
        panel.add(button);

        for (int i = 0; i < 3; i++) {
            ClientWindow window = new ClientWindow(servers, client, simdExecutor);
            window.setLocation(300, 200*(i));

            ImageConsumerTask consumer = new ImageConsumerTask(waitingList, servers, simdExecutor, client, window.getFileNameWithoutExtension(), window.getFileExtension(), window);
            new Thread(consumer).start();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Initializes the server info map.
     *
     * @return The server info map.
     */

    private static Map<String, ServerInfo> initializaServerInfoMap() {
        Map<String, ServerInfo> serverInfoMap = new HashMap<>();
        for (int i = 0; i < NUM_SERVERS; i++) {
            String serverId = "Server " + i;
            ServerInfo serverInfo = new ServerInfo("localhost", 8888 + i);
            serverInfoMap.put(serverId, serverInfo);
        }
        return serverInfoMap;
    }

    /**
     * Starts the servers.
     *
     * @param loadInfo     The load info to use.
     * @param serverInfoMap The server info map to use.
     *
     * @return The list of servers.
     */

    private static List<Server> startServers(LoadInfo loadInfo, Map<String, ServerInfo> serverInfoMap) {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < NUM_SERVERS; i++) {
            int port = 8888 + i;
            Server server = new Server(port, loadInfo, MAX_WORKLOAD);
            server.start();
            servers.add(server);

            String serverId = String.valueOf(port);
            ServerInfo serverInfo = new ServerInfo("localhost", port);
            serverInfoMap.put(serverId, serverInfo);
        }
        return servers;
    }
}