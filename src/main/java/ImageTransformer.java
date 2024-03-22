import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * The ImageTransformer class implements a set of methods for performing transformations (split, join, and red removal)
 * in a specified image.
 */
public class ImageTransformer {

    /**
     * Splits a given image into a grid of sub-images.
     *
     * @param image     the BufferedImage containing the image
     * @param nRows     the number of rows in the grid
     * @param nColumns  the number of columns in the grid
     *
     * @return a BufferedImage array containing the sub-images
     */
    public static BufferedImage[][] splitImage(BufferedImage image, int nRows, int nColumns) {
        int height = (image.getHeight() / nRows) * nRows;
        int width = (image.getWidth() / nColumns) * nColumns;

        BufferedImage resizedImage = image.getSubimage(0, 0, width, height);

        int subImageWidth = width / nColumns;
        int subImageHeight = height / nRows;
        BufferedImage[][] images = new BufferedImage[nRows][nColumns];

        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                images[i][j] = resizedImage.getSubimage(j * subImageWidth, i * subImageHeight, subImageWidth, subImageHeight);
            }
        }
        return images;
    }

    /**
     * Removes the red component of a given image.
     *
     * @param image the BufferedImage containing the image
     *
     * @return a BufferedImage without the red component
     */
    public static BufferedImage removeReds ( BufferedImage image ) {
        int width = image.getWidth ( );
        int height = image.getHeight ( );
        int type = image.getType ( );
        Color c;
        BufferedImage resultingImage = new BufferedImage ( width , height , type );
        for ( int i = 0 ; i < width ; i++ ) {
            for ( int j = 0 ; j < height ; j++ ) {
                c = new Color ( image.getRGB ( i , j ) );
                int g = c.getGreen ( );
                int b = c.getBlue ( );
                resultingImage.setRGB ( i , j , new Color ( 0 , g , b ).getRGB ( ) );
            }
        }
        return resultingImage;
    }


    /**
     * Joins a given array of BufferedImage in one final image. This method should be called after splitting the images
     * using, for example, the method {@link ImageTransformer#splitImage(BufferedImage , int , int)}.
     *
     * @param splitImages the BufferedImage array containing the sub-images
     * @param width       the width of the final image
     * @param height      the height of the final image
     * @param type        the type of the final image
     *
     * @return a BufferedImage array containing the image joined
     */
    public static BufferedImage joinImages ( BufferedImage[][] splitImages , int width , int height , int type ) {
        BufferedImage resultingImage = new BufferedImage ( width , height , type );
        int nRows = splitImages.length;
        int nColumns = splitImages[ 0 ].length;
        for ( int i = 0 ; i < nRows ; i++ ) {
            for ( int j = 0 ; j < nColumns ; j++ ) {
                BufferedImage split = splitImages[ i ][ j ];
                resultingImage.createGraphics ( ).drawImage ( split , split.getWidth ( ) * j , split.getHeight ( ) * i , null );
            }
        }
        return resultingImage;
    }

    /**
     *  Creates a Buffered image from a byte array
     * @param imageData - Byte array of with the image
     * @return - Image as a BufferedItem object
     */
    public static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes an image as a byte array so it can be sent through a socket. It is important to define the type of image
     * @param image - image to be converted as a byte[]
     * @return - image as an array of bytes
     */
    public static byte[] createBytesFromImage(BufferedImage image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Reconstructs an image from an array of BufferedImages.
     *
     * @param imageParts the array of BufferedImages
     *
     * @return the reconstructed image
     */

    public static BufferedImage reconstructImage(BufferedImage[][] imageParts) {
        int partHeight = imageParts[0][0].getHeight();
        int partWidth = imageParts[0][0].getWidth();
        int totalHeight = partHeight * imageParts.length;
        int totalWidth = partWidth * imageParts[0].length;

        BufferedImage reconstructedImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = reconstructedImage.getGraphics();

        for (int i = 0; i < imageParts.length; i++) {
            for (int j = 0; j < imageParts[i].length; j++) {
                g.drawImage(imageParts[i][j], j * partWidth, i * partHeight, null);
            }
        }

        g.dispose();
        return reconstructedImage;
    }

    /**
     * Saves an edited image to the results folder.
     *
     * @param image the edited image
     * @param originalName the original name of the image
     * @param extension the extension of the image
     */

    public static void saveEditedImage(BufferedImage image, String originalName, String extension) {
        try {
            String editedName = originalName + "_edited." + extension;
            File outputfile = new File("results/" + editedName);
            ImageIO.write(image, extension, outputfile);
            System.out.println("Edited image saved as " + outputfile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}