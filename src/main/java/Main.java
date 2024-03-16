import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main ( String[] args ) {

        final ConfigFile configFile = new ConfigFile ( "project.config" );
        int num_rows = configFile.getIntProperty ( "num_rows" );
        int num_columns = configFile.getIntProperty ( "num_columns" );
        int num_servers = configFile.getIntProperty ( "num_servers" );

        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < num_servers; i++) {
            Server server = new Server(8888 + i); // incrementing port number for each server
            server.start();
            servers.add(server);
        }

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
            Client client = new Client("Client A");
            Server server = servers.get(0); // Get the single server
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_columns; j++) {
                    Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
                    Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
                    subImages[i][j] = ImageTransformer.createImageFromBytes(response.getImageSection());
                }
            }
            icon.setImage(ImageTransformer.joinImages(subImages, sampleImage.getWidth(), sampleImage.getHeight(), sampleImage.getType()));
            panel.repaint();
        });

        frame.add ( panel );
        frame.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        frame.pack ( );
        frame.setVisible ( true );
    }

}