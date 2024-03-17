import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Main {
    private static final String IMAGE_NAME = "sample.png";
    private static final String IMAGE_NAME_WITHOUT_EXTENSION = IMAGE_NAME.substring(0, IMAGE_NAME.lastIndexOf('.'));
    private static final String IMAGE_EXTENSION = IMAGE_NAME.substring(IMAGE_NAME.lastIndexOf(".") + 1);
    private static final BlockingQueue<ImagePart> waitingList = new LinkedBlockingQueue<>();

    private static final ConfigFile CONFIG_FILE = new ConfigFile ( "project.config" );
    private static final int NUM_ROWS = CONFIG_FILE.getIntProperty ( "num_rows" );
    private static final int NUM_COLUMNS = CONFIG_FILE.getIntProperty ( "num_columns" );
    private static final int NUM_SERVERS = CONFIG_FILE.getIntProperty ( "num_servers" );
    private static final int MAX_WORKLOAD = CONFIG_FILE.getIntProperty ( "max_workload" );

    // Semaphore for controlling access to the server
    private static final Semaphore semaphore = new Semaphore(NUM_SERVERS);

    private static int nextServerIndex = 0;

    public static void main ( String[] args ) throws IOException {

        LoadInfo loadInfo = new LoadInfo("load.info");
        Map<String, ServerInfo> serverInfoMap = initializaServerInfoMap();
        List<Server> servers = startServers(loadInfo, serverInfoMap);
        BufferedImage sampleImage = ImageReader.readImage(IMAGE_NAME);

        JFrame frame = new JFrame("pa-distributed-img");
        frame.setSize(600, 600);
        JPanel panel = new JPanel();
        ImageIcon icon = new ImageIcon(sampleImage);
        JLabel label = new JLabel(icon);
        panel.add(label);
        frame.add(panel);

        JButton button = new JButton();
        button.setText("Remove red (SIMD)");
        button.addActionListener(e -> processImage(sampleImage, servers, loadInfo, label, icon));
        panel.add(button);

        JButton sequentialButton = new JButton();
        sequentialButton.setText("Remove red (Sequential)");
        sequentialButton.addActionListener(e -> processImageSequentially(sampleImage, servers, label, icon));
        panel.add(sequentialButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static Map<String, ServerInfo> initializaServerInfoMap() {
        Map<String, ServerInfo> serverInfoMap = new HashMap<>();
        for (int i = 0; i < NUM_SERVERS; i++) {
            String serverId = "Server " + i;
            ServerInfo serverInfo = new ServerInfo("localhost", 8888 + i);
            serverInfoMap.put(serverId, serverInfo);
        }
        return serverInfoMap;
    }

    private static List<Server> startServers(LoadInfo loadInfo, Map<String, ServerInfo> serverInfoMap) {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < NUM_SERVERS; i++) {
            int port = 8888 + i;
            Server server = new Server(port, loadInfo, MAX_WORKLOAD); // incrementing port number for each server
            server.start();
            servers.add(server);

            //Add ServerInfo to the map
            String serverId = String.valueOf(port);
            ServerInfo serverInfo = new ServerInfo("localhost", port);
            serverInfoMap.put(serverId, serverInfo);
        }
        return servers;
    }

    private static void processImage(BufferedImage sampleImage, List<Server> servers, LoadInfo loadInfo, JLabel label, ImageIcon icon) {
        BufferedImage[][] subImages = ImageTransformer.splitImage(sampleImage, NUM_ROWS, NUM_COLUMNS);
        Client client = new Client("Client A", loadInfo, NUM_ROWS, NUM_COLUMNS);
        SIMDExecutor simdExecutor = new SIMDExecutor(sampleImage, NUM_COLUMNS, NUM_ROWS);

        // Initialize the CountDownLatch with the total number of image processing tasks
        CountDownLatch latch = new CountDownLatch(NUM_ROWS * NUM_COLUMNS);

        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLUMNS; j++) {
                try {
                    semaphore.acquire();
                    int finalI = i;
                    int finalJ = j;
                    new Thread(() -> {
                        try {
                            processSubImage(subImages, finalI, finalJ, servers, client, simdExecutor);
                            processWaitingList(servers, sampleImage, loadInfo, simdExecutor);
                        } finally {
                            semaphore.release();

                            // Call countDown() on the latch when the task completes
                            latch.countDown();
                        }
                    }).start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Wait for all image processing tasks to complete before performing the repaint operation
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        BufferedImage newImage = ImageTransformer.joinImages(subImages, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType());
        icon.setImage(newImage);
        label.repaint();
    }

    private static void processSubImage(BufferedImage[][] subImages, int i, int j, List<Server> servers, Client client, SIMDExecutor simdExecutor){
        boolean sent = false;
        while (!sent) {
            // Select a server using round-robin
            Server server = servers.get(nextServerIndex);
            nextServerIndex = (nextServerIndex + 1) % NUM_SERVERS;

            if (server.getWorkload() < server.getMaxWorkload()) {
                try {
                    server.incrementWorkload();
                    processImagePart(subImages, i, j, client, simdExecutor, server);
                    sent = true;
                } catch (Exception e) {
                    System.err.println("Error processing image part: " + e);
                    System.exit(1);
                } finally {
                    server.decrementWorkload();
                }
            }

            if (!sent) {
                System.out.println("Server " + server.getPort() + " is busy. Trying next server.");
                try {
                    waitingList.put(new ImagePart(subImages[i][j], i, j));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            }
        }
    }

    private static void processWaitingList(List<Server> servers, BufferedImage sampleImage, LoadInfo loadInfo, SIMDExecutor simdExecutor) {
        new Thread(() -> {
            BufferedImage[][] subImages = ImageTransformer.splitImage(sampleImage, NUM_ROWS, NUM_COLUMNS);
            Client client = new Client("Client A", loadInfo, NUM_ROWS, NUM_COLUMNS);
            while (true) {
                try {
                    ImagePart imagePart = waitingList.take(); // This will block if the queue is empty
                    boolean processed = false;
                    for (Server server : servers) {
                        synchronized (server) {
                            if (server.getWorkload() < server.getMaxWorkload()) {
                                processImagePart(subImages, imagePart.getRow(), imagePart.getColumn(), client, simdExecutor, server);
                                processed = true;
                                break;
                            }
                        }
                    }
                    if (!processed) {
                        waitingList.add(imagePart);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private static void processImagePart(BufferedImage[][] subImages, int i, int j, Client client, SIMDExecutor simdExecutor, Server server) {
        long startTime = System.nanoTime();

        // Pass the correct parameters to the execute method
        simdExecutor.execute(i, j);

        // Remove or replace the getProcessedSubImage method call
        // subImages[i][j] = simdExecutor.getProcessedSubImage;

        Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
        subImages[i][j] = ImageTransformer.createImageFromBytes(response.getImageSection());
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime / 1000000 + "ms");

        client.sendImagePart(subImages[i][j], IMAGE_NAME_WITHOUT_EXTENSION, IMAGE_EXTENSION);
    }

    private static void processImageSequentially(BufferedImage sampleImage, List<Server> servers, JLabel label, ImageIcon icon) {
        BufferedImage[][] splitImages = ImageTransformer.splitImage(sampleImage, NUM_ROWS, NUM_COLUMNS);
        SequentialExecutor sequentialExecutor = new SequentialExecutor(sampleImage, NUM_ROWS, NUM_COLUMNS, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType());

        int serverIndex = 0;
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLUMNS; j++) {
                Server server = servers.get(serverIndex);
                ImagePart imagePart = new ImagePart(splitImages[i][j], i, j);
                sequentialExecutor.execute(server, imagePart);
                splitImages[i][j] = imagePart.getImage();
                serverIndex = (serverIndex + 1) % servers.size(); // Move to the next server in a round-robin fashion
            }
        }

        BufferedImage newImage = ImageTransformer.joinImages(splitImages, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType());
        icon.setImage(newImage);
        label.repaint();
    }
}