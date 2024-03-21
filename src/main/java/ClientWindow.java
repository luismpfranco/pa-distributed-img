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

    private ImageProcessor imageProcessor;
    private ImageIcon icon;

    /**
     * Constructs a new client window with the specified image processor, servers, client, and SIMD executor.
     *
     * @param imageProcessor The image processor to use.
     * @param servers        The list of servers to use.
     * @param client         The client to use.
     * @param simdExecutor   The SIMD executor to use.
     */
    public ClientWindow(ImageProcessor imageProcessor, List<Server> servers, Client client, SIMDExecutor simdExecutor) {

        this.imageProcessor = imageProcessor;
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
                pack();
            }
        });

        removeRedsButton.addActionListener(e->{
            icon = (ImageIcon) imageLabel.getIcon();
            BufferedImage image = (BufferedImage) icon.getImage();

            BufferedImage editedImage = imageProcessor.processImage(image, servers, client, simdExecutor, fileNameWithoutExtension, fileExtension);
            updateImage(editedImage);
            icon.setImage(editedImage);
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
        icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
        pack();
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

    /**
     * Returns the image label.
     *
     * @return The image label.
     */
    public JLabel getImageLabel() {
        return imageLabel;
    }

    public void setImageProcessor(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    public void setFileExtension(String png) {
        this.fileExtension = png;
    }

    public void setFileNameWithoutExtension(String fileNameWithoutExtension) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
    }

    public ImageIcon getImageIcon() {
        return icon;
    }
}