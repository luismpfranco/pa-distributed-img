import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserInterface extends JPanel {
    private JButton selectImageButton;
    private JTextArea messageArea;
    private JFileChooser fileChooser;

    public UserInterface() {
        this.setLayout(new FlowLayout());

        // Create the select image button
        selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(e -> {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                Main.selectedFile = fileChooser.getSelectedFile();
                messageArea.append("Image selected: " + Main.selectedFile.getName() + "\n");
            }

        });

        messageArea = new JTextArea(5, 20);
        messageArea.setEditable(false);

        // Add components to the panel
        this.add(selectImageButton);
        this.add(new JScrollPane(messageArea));
    }

    public JButton getSelectImageButton() {
        return selectImageButton;
    }

    public JTextArea getMessageArea() {
        return messageArea;
    }
}