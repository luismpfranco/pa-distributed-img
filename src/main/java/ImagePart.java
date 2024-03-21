import java.awt.image.BufferedImage;

/**
 * Represents a part of an image.
 */
public class ImagePart {
    /**
     * The image of this image part.
     */
    private BufferedImage image;
    /**
     * The row of this image part.
     */
    private final int row;
    /**
     * The column of this image part.
     */
    private final int column;

    private SIMDExecutor simdExecutor;

    /**
     * Constructs a new image part with the specified image, row, and column.
     *
     * @param image  The image to use.
     * @param row    The row to use.
     * @param column The column to use.
     */
    public ImagePart(BufferedImage image, int row, int column, SIMDExecutor simdExecutor) {
        this.image = image;
        this.row = row;
        this.column = column;
        this.simdExecutor = simdExecutor;
    }

    /**
     * Gets the image of this image part.
     *
     * @return The image of this image part.
     */

    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets the row of this image part.
     *
     * @return The row of this image part.
     */

    public int getRow() {
        return row;
    }

    /**
     * Gets the column of this image part.
     *
     * @return The column of this image part.
     */

    public int getColumn() {
        return column;
    }

    /**
     * Sets the image of this image part.
     *
     * @param image The image to set.
     */

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public SIMDExecutor getSimdExecutor() {
        return simdExecutor;
    }
}
