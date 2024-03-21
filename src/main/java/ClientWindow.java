import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
/**
 * A simple GUI window for the client.
 */
public class ClientWindow extends JFrame {
    /**
     * The file chooser.
     */
    private final JFileChooser fileChooser;
    /**
     * The image label.
     */
    private final JLabel imageLabel;
    /**
     * The remove reds button.
     */
    private final JButton removeRedsButton;
    /**
     * The file name without the extension.
     */
    private String fileNameWithoutExtension;
    /**
     * The file extension.
     */
    private String fileExtension;

    /**
     * Constructs a new client window with the specified image processor, servers, client, and SIMD executor.
     *
     * @param imageProcessor The image processor to use.
     * @param servers        The list of servers to use.
     * @param client         The client to use.
     * @param simdExecutor   The SIMD executor to use.
     */
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

    /**
     * Updates the image in the window.
     *
     * @param image The new image to display.
     */

    public void updateImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon imageIcon = new ImageIcon(image);
            imageLabel.setIcon(imageIcon);
            pack();
        });
    }

    /**
     * Returns the file name without the extension.
     *
     * @return The file name without the extension.
     */
    public String getFileNameWithoutExtension() {
        return fileNameWithoutExtension;
    }

    /**
     * Returns the file extension.
     *
     * @return The file extension.
     */
    public String getFileExtension() {
        return fileExtension;
    }
}