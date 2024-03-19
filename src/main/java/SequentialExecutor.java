import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class SequentialExecutor {
    private final BufferedImage image;
    private final int nCols;
    private final int nRows;
    private final int width;
    private final int height;
    private final int type;
    private BufferedImage[][] splitImages;

    public SequentialExecutor(BufferedImage image, int nCols, int nRows, int width, int height, int type) {
        this.image = image;
        this.nCols = nCols;
        this.nRows = nRows;
        this.width = width;
        this.height = height;
        this.type = type;
        splitImages = new BufferedImage[nRows][nCols];
    }

    public void execute(Server server, ImagePart imagePart){
        Client client = new Client("Client A", new LoadInfo("load.info"), nRows, nCols);
        Request request = new Request("greeting", "Hello, Server!", imagePart.getImage());
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);

        if(response != null){
            BufferedImage imageWithoutRed = removeRed(ImageTransformer.createImageFromBytes(response.getImageSection()));
            imagePart.setImage(imageWithoutRed);
        }else{
            System.out.println("Error: The server did not respond or the request failde.");
        }
    }

    public BufferedImage removeRed(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                Color newColor = new Color(0, color.getGreen(), color.getBlue());
                image.setRGB(x, y, newColor.getRGB());
            }
        }
        return image;
    }
}
