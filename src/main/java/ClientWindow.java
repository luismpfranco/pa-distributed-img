import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class ClientWindow extends JFrame {
    private final JFileChooser fileChooser;
    private final JLabel imageLabel;
    private final JButton removeRedsButton;
    private String fileNameWithoutExtension;
    private String fileExtension;

    public ClientWindow(ImageProcessor imageProcessor, List<Server> servers, Client client, SIMDExecutor simdExecutor) {

        JButton selectImageButton = new JButton("Select Image");
        fileChooser = new JFileChooser();

        imageLabel = new JLabel();
        add(imageLabel);

        removeRedsButton = new JButton("Remove Reds (SIMD)");
        removeRedsButton.setVisible(false);
        add(removeRedsButton);

        selectImageButton.addActionListener(e -> {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String selectedFileName = selectedFile.getName();
                BufferedImage image = null;
                try {
                    image = ImageIO.read(selectedFile);
                } catch (Exception f) {
                    f.printStackTrace();
                }
                updateImage(image);
                this.fileNameWithoutExtension = selectedFileName.substring(0, selectedFileName.lastIndexOf('.'));
                this.fileExtension = selectedFileName.substring(selectedFileName.lastIndexOf(".") + 1);
                removeRedsButton.setVisible(true);
                selectImageButton.setText("Select Another Image");
            }
        });

        removeRedsButton.addActionListener(e->{
            ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();
            BufferedImage image = (BufferedImage) imageIcon.getImage();

            BufferedImage editedImage = imageProcessor.processImage(image, servers, client, simdExecutor, fileNameWithoutExtension, fileExtension);
            updateImage(editedImage);
            imageIcon.setImage(editedImage);
            imageLabel.repaint();
        });

        setLayout(new FlowLayout());
        add(selectImageButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void updateImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon imageIcon = new ImageIcon(image);
            imageLabel.setIcon(imageIcon);
            pack();
        });
    }
    public String getFileNameWithoutExtension() {
        return fileNameWithoutExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}