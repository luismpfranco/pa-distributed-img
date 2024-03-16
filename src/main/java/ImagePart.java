import java.awt.image.BufferedImage;

public class ImagePart {
    private BufferedImage image;
    private int row;
    private int column;

    public ImagePart(BufferedImage image, int row, int column) {
        this.image = image;
        this.row = row;
        this.column = column;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
