import java.awt.image.BufferedImage;

public class ImagePart {
    private BufferedImage image;
    private final int row;
    private final int column;

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

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
