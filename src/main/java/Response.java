import java.awt.image.BufferedImage;
import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a response received from a server. This class can be extended to include various types of data depending
 * on the application's needs.
 */
public class Response implements Serializable {
    /**
     * The serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L; // Ensure compatibility during serialization
    /**
     * The status of the response.
     */
    private String status;
    /**
     * The message of the response.
     */
    private String message;
    /**
     * The image section.
     */
    private byte[] imageSection;


    /**
     * Constructs a new Response with specified status and message.
     *
     * @param status  The status of the response, e.g., "Success" or "Error".
     * @param message The message or data contained in the response.
     */
    public Response ( String status , String message , BufferedImage imageSection) {
        this.status = status;
        this.message = message;
        this.imageSection = ImageTransformer.createBytesFromImage(imageSection);
    }

    /**
     * Returns the status of the response.
     *
     * @return The status of the response.
     */
    public String getStatus ( ) {
        return status;
    }

    /**
     * Sets the status of the response.
     *
     * @param status The status of the response.
     */
    public void setStatus ( String status ) {
        this.status = status;
    }

    /**
     * Returns the message of the response.
     *
     * @return The message of the response.
     */
    public String getMessage ( ) {
        return message;
    }

    /**
     * Sets the message of the response.
     *
     * @param message The message of the response.
     */
    public void setMessage ( String message ) {
        this.message = message;
    }

    /**
     * Returns the image section.
     *
     * @return The image section.
     */
    public byte[] getImageSection() {
        return imageSection;
    }

    /**
     * Sets the image section.
     *
     * @param imageSection The image section.
     */
    public void setImageSection(byte[] imageSection) {
        this.imageSection = imageSection;
    }

    /**
     * Returns a string representation of the response.
     *
     * @return A string representation of the response.
     */
    @Override
    public String toString ( ) {
        return "Response{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}