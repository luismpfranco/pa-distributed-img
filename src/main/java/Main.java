import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Main {
    public static void main ( String[] args ) {

        LoadInfo loadInfo = new LoadInfo("load.info");

        Map<String,ServerInfo> serverInfoMap = new HashMap<>();

        final ConfigFile configFile = new ConfigFile ( "project.config" );
        int num_rows = configFile.getIntProperty ( "num_rows" );
        int num_columns = configFile.getIntProperty ( "num_columns" );
        int num_servers = configFile.getIntProperty ( "num_servers" );
        int max_workload = configFile.getIntProperty ( "max_workload" );

        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < num_servers; i++) {
            Server server = new Server(8888 + i, loadInfo, max_workload); // incrementing port number for each server
            server.start();
            servers.add(server);

            //Add ServerInfo to the map
            String serverId = "Server " + i;
            ServerInfo serverInfo = new ServerInfo("localhost", 8888 + i);
            serverInfoMap.put(serverId, serverInfo);


        }

        Queue<ImagePart> waitingList = new LinkedList<>();

        BufferedImage sampleImage = ImageReader.readImage("sample.png");
        //Java Swing stuff
        JFrame frame = new JFrame("pa-distributed-img");
        frame.setSize(600, 600);
        JPanel panel = new JPanel();
        ImageIcon icon = new ImageIcon(sampleImage);
        JLabel label = new JLabel(icon);
        panel.add(label);

        JButton button = new JButton();
        button.setText("Remove red");
        panel.add(button);

        button.addActionListener(e -> {
            BufferedImage[][] subImages = ImageTransformer.splitImage(sampleImage, num_rows, num_columns);
            Client client = new Client("Client A",loadInfo,serverInfoMap);
            int serverIndex = 0;
            int nextServerIndex = 0;
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_columns; j++) {

                    boolean sent = false;
                    while (!sent) {
                        Server server = servers.get(nextServerIndex);
                        int port = server.getPort();

                        serverIndex = nextServerIndex;

                        nextServerIndex = (serverIndex + 1) % num_servers;

                        if (server.getWorkload() < server.getMaxWorkload()) {
                            server.incrementWorkload(); // Increment the workload of the server
                            Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
                            Response response = client.sendRequestAndReceiveResponse("localhost", port, request);
                            subImages[i][j] = ImageTransformer.createImageFromBytes(response.getImageSection());
                            server.decrementWorkload(); // Decrement the workload of the server
                            try {
                                Thread.sleep(1000); // Wait for 1 second
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            client.sendImagePart(subImages[i][j]);
                            sent = true;
                        }
                        else
                        {
                            System.out.println("Server " + serverIndex + " is busy. Trying next server.");
                            waitingList.add(new ImagePart(subImages[i][j], i, j));
                            break;
                        }
                    }
                }
            }

            new Thread(() -> {
                while (true) {
                    if (!waitingList.isEmpty()) {
                        for (Server server : servers) {
                            if (server.getWorkload() < server.getMaxWorkload()) {
                                ImagePart imagePart = waitingList.poll();
                                if (imagePart != null) {
                                    int port = server.getPort();
                                    Request request = new Request("greeting", "Hello, Server!", imagePart.getImage());
                                    Response response = client.sendRequestAndReceiveResponse("localhost", port, request);
                                    BufferedImage processedImage = ImageTransformer.createImageFromBytes(response.getImageSection());
                                    server.decrementWorkload(); // Decrement the workload of the server
                                    try {
                                        Thread.sleep(1000); // Wait for 1 second
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    client.sendImagePart(processedImage);

                                    subImages[imagePart.getRow()][imagePart.getColumn()] = processedImage;
                                }
                            }
                        }
                    }
                    else
                    {
                        icon.setImage(ImageTransformer.joinImages(subImages, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType()));
                        panel.repaint();
                        break;
                    }
                }

            }).start();

        });


        frame.add ( panel );
        frame.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        frame.pack ( );
        frame.setVisible ( true );
    }
}