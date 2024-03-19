import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Main {
    static File selectedFile;
    private static final String IMAGE_NAME = "sample.png";
    private static final BlockingQueue<ImagePart> waitingList = new LinkedBlockingQueue<>();

    private static final ConfigFile CONFIG_FILE = new ConfigFile ( "project.config" );
    private static final int NUM_ROWS = CONFIG_FILE.getIntProperty ( "num_rows" );
    private static final int NUM_COLUMNS = CONFIG_FILE.getIntProperty ( "num_columns" );
    private static final int NUM_SERVERS = CONFIG_FILE.getIntProperty ( "num_servers" );
    private static final int MAX_WORKLOAD = CONFIG_FILE.getIntProperty ( "max_workload" );

    // Semaphore for controlling access to the server
    private static final Semaphore semaphore = new Semaphore(NUM_SERVERS);

    private static int nextServerIndex = 0;
    private static int serverIndex = 0;

    public static void main ( String[] args ) throws IOException {

        LoadInfo loadInfo = new LoadInfo("load.info");
        Client client = new Client("Client A", loadInfo, NUM_ROWS, NUM_COLUMNS);
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
        JButton sequentialButton = new JButton();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        UserInterface userInterface = new UserInterface();
        panel.add(userInterface);

        button.setText("Remove red (SIMD)");
        button.addActionListener(e -> {
            if (selectedFile != null) {
                String selectedFileName = selectedFile.getName();
                try {
                    processImage(ImageIO.read(selectedFile), servers, loadInfo, label, icon, selectedFileName);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else{
                processImage(sampleImage, servers, loadInfo, label, icon, IMAGE_NAME);
            }
        });
        panel.add(button);

        sequentialButton.setText("Remove red (Sequential)");
        sequentialButton.addActionListener(e -> {
            if (selectedFile != null) {
                String selectedFileName = selectedFile.getName();
                try {
                    processImageSequentially(ImageIO.read(selectedFile), servers, label, icon, client, selectedFileName);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else{
                processImageSequentially(sampleImage, servers, label, icon, client, IMAGE_NAME);
            }
        });
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

    private static void processImage(BufferedImage image, List<Server> servers, LoadInfo loadInfo, JLabel label, ImageIcon icon, String name) {
        BufferedImage[][] subImages = ImageTransformer.splitImage(image, NUM_ROWS, NUM_COLUMNS);
        Client client = new Client("Client A", loadInfo, NUM_ROWS, NUM_COLUMNS);
        SIMDExecutor simdExecutor = new SIMDExecutor(image, NUM_COLUMNS, NUM_ROWS);
        String fileNameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
        String fileExtension = name.substring(name.lastIndexOf(".") + 1);

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
                            processSubImage(subImages, finalI, finalJ, servers, client, simdExecutor, fileNameWithoutExtension, fileExtension);
                            if(!waitingList.isEmpty())
                                processWaitingList(subImages, servers, simdExecutor, client, fileNameWithoutExtension, fileExtension);
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

        BufferedImage editedImage = ImageTransformer.joinImages(subImages, image.getWidth(), image.getHeight(), image.getType());
        client.sendImagePart(editedImage, fileNameWithoutExtension + "_edited", fileExtension);
        icon.setImage(editedImage);
        label.repaint();
    }

    private static void processSubImage(BufferedImage[][] subImages, int i, int j, List<Server> servers, Client client, SIMDExecutor simdExecutor, String fileNameWithoutExtension, String fileExtension){
        boolean sent = false;
        while (!sent) {
            // Select a server using round-robin
            Server server = servers.get(nextServerIndex);
            serverIndex = nextServerIndex;
            nextServerIndex = (serverIndex + 1) % NUM_SERVERS;

            if (server.getWorkload() < server.getMaxWorkload()) {
                try {
                    server.incrementWorkload();
                    processImagePart(subImages, i, j, client, simdExecutor, server, fileNameWithoutExtension, fileExtension);
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

    private static void processWaitingList(BufferedImage[][] subImages, List<Server> servers, SIMDExecutor simdExecutor, Client client, String fileNameWithoutExtension, String fileExtension) {
        new Thread(() -> {
            while (true) {
                try {
                    ImagePart imagePart = waitingList.take(); // This will block if the queue is empty
                    boolean processed = false;
                    for (Server server : servers) {
                        synchronized (server) {
                            if (server.getWorkload() < server.getMaxWorkload()) {
                                processImagePart(subImages, imagePart.getRow(), imagePart.getColumn(), client, simdExecutor, server, fileNameWithoutExtension, fileExtension);
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

    private static void processImagePart(BufferedImage[][] subImages, int i, int j, Client client, SIMDExecutor simdExecutor, Server server, String fileNameWithoutExtension, String fileExtension) throws InterruptedException {
        long startTime = System.nanoTime();

        // Pass the correct parameters to the execute method
        simdExecutor.execute(i, j);

        Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
        BufferedImage processedSubImage = ImageTransformer.createImageFromBytes(response.getImageSection());
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime / 1000000 + "ms");

        subImages[i][j] = processedSubImage;
        client.sendImagePart(processedSubImage, fileNameWithoutExtension + "_edited_" + i + "_" + j, fileExtension);
    }

    private static void processImageSequentially(BufferedImage sampleImage, List<Server> servers, JLabel label, ImageIcon icon, Client client, String name) {
        BufferedImage[][] splitImages = ImageTransformer.splitImage(sampleImage, NUM_ROWS, NUM_COLUMNS);
        SequentialExecutor sequentialExecutor = new SequentialExecutor(sampleImage, NUM_ROWS, NUM_COLUMNS, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType());
        String fileNameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
        String fileExtension = name.substring(name.lastIndexOf(".") + 1);

        int serverIndex = 0;
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLUMNS; j++) {
                Server server = servers.get(serverIndex);
                ImagePart imagePart = new ImagePart(splitImages[i][j], i, j);
                sequentialExecutor.execute(server, imagePart);
                splitImages[i][j] = imagePart.getImage();
                client.sendImagePart(splitImages[i][j], fileNameWithoutExtension + "_edited_" + i + "_" + j, fileExtension);
                serverIndex = (serverIndex + 1) % servers.size(); // Move to the next server in a round-robin fashion
            }
        }

        BufferedImage newImage = ImageTransformer.joinImages(splitImages, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType());
        client.sendImagePart(newImage, fileNameWithoutExtension + "_edited", fileExtension);
        icon.setImage(newImage);
        label.repaint();
    }
}