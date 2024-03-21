import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
/**
 * A class that processes images.
 */

public class ImageProcessor {
    /**
     * The blocking queue to store image parts that are waiting to be processed.
     */
    private final BlockingQueue<ImagePart> waitingList = new LinkedBlockingQueue<>();
    /**
     * The semaphore to control the number of servers that can process images concurrently.
     */
    private final Semaphore semaphore;
    /**
     * The client window to use.
     */
    private ClientWindow clientWindow;

    /**
     * Constructs a new image processor with the specified number of servers and client window.
     *
     * @param numServers   The number of servers to use.
     * @param clientWindow The client window to use.
     */

    public ImageProcessor(int numServers, ClientWindow clientWindow) {
        this.semaphore = new Semaphore(numServers);
        this.clientWindow = clientWindow;
    }

    /**
     * Processes an image using the specified servers, client, SIMD executor, file name without extension, and file extension.
     *
     * @param image                   The image to process.
     * @param servers                 The list of servers to use.
     * @param client                  The client to use.
     * @param simdExecutor            The SIMD executor to use.
     * @param fileNameWithoutExtension The file name without extension to use.
     * @param fileExtension            The file extension to use.
     *
     * @return The processed image.
     */

    public BufferedImage processImage(BufferedImage image, List<Server> servers, Client client, SIMDExecutor simdExecutor, String fileNameWithoutExtension, String fileExtension) {
        final int NUM_ROWS = simdExecutor.getnRows();
        final int NUM_COLUMNS = simdExecutor.getnCols();

        BufferedImage[][] subImages = ImageTransformer.splitImage(image, NUM_ROWS, NUM_COLUMNS);

        ImageProducer producer = new ImageProducer(image, NUM_ROWS, NUM_COLUMNS, waitingList, servers);
        new Thread(producer).start();

        ImageConsumerTask consumer = new ImageConsumerTask(waitingList, servers, simdExecutor, client, fileNameWithoutExtension, fileExtension, clientWindow);
        new Thread(consumer).start();

        CountDownLatch latch = new CountDownLatch(NUM_ROWS * NUM_COLUMNS);

        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLUMNS; j++) {
                try {
                    semaphore.acquire();
                    int finalI = i;
                    int finalJ = j;
                    new Thread(() -> {
                        try {
                            processSubImage(subImages, finalI, finalJ, servers, client, simdExecutor);
                            if(!waitingList.isEmpty())
                                processWaitingList(subImages, servers, simdExecutor, client);
                        } finally {
                            semaphore.release();

                            latch.countDown();
                        }
                    }).start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        BufferedImage editedImage = ImageTransformer.joinImages(subImages, image.getWidth(), image.getHeight(), image.getType());
        client.sendImagePart(editedImage, fileNameWithoutExtension + "_edited", fileExtension);
        return editedImage;
    }

    /**
     * Processes a sub-image using the specified sub-images, row, column, servers, client, and SIMD executor.
     *
     * @param subImages     The sub-images to process.
     * @param i             The row of the sub-image.
     * @param j             The column of the sub-image.
     * @param servers       The list of servers to use.
     * @param client        The client to use.
     * @param simdExecutor  The SIMD executor to use.
     */

    void processSubImage(BufferedImage[][] subImages, int i, int j, List<Server> servers, Client client, SIMDExecutor simdExecutor){
        boolean sent = false;
        while (!sent) {
            Server server = servers.stream().min(Comparator.comparingInt(Server::getWorkload)).orElse(null);

            if (server != null) {
                synchronized (server) {
                    if (server.getWorkload() < server.getMaxWorkload()) {
                        try {
                            server.incrementWorkload();
                            processImagePart(subImages, i, j, client, simdExecutor, server);
                            sent = true;
                        } catch (Exception e) {
                            System.err.println("Error processing image part: " + e);
                            System.exit(1);
                        } finally {
                            server.decrementWorkload();
                        }
                    }
                }
            }

            if (!sent) {
                System.out.println("All servers are busy. Adding image part to waiting list.");
                try {
                    waitingList.put(new ImagePart(subImages[i][j], i, j, simdExecutor));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            }
        }
    }

    /**
     * Processes the waiting list using the specified sub-images, servers, SIMD executor, and client.
     *
     * @param subImages     The sub-images to process.
     * @param servers       The list of servers to use.
     * @param simdExecutor  The SIMD executor to use.
     * @param client        The client to use.
     */

    void processWaitingList(BufferedImage[][] subImages, List<Server> servers, SIMDExecutor simdExecutor, Client client) {
        new Thread(() -> {
            while (true) {
                try {
                    ImagePart imagePart = waitingList.take();
                    boolean processed = false;

                    Server server = servers.stream().min(Comparator.comparingInt(Server::getWorkload)).orElse(null);
                    if (server != null) {
                        synchronized (server) {
                            if (server.getWorkload() < server.getMaxWorkload()) {
                                try{
                                    server.incrementWorkload();
                                    processImagePart(subImages, imagePart.getRow(), imagePart.getColumn(), client, simdExecutor, server);
                                    processed = true;
                                }catch(Exception e){
                                    e.printStackTrace();
                                }finally{
                                    server.decrementWorkload();
                                }
                            }
                        }
                    }
                    if (!processed) {
                        waitingList.add(imagePart);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    /**
     * Processes an image part using the specified sub-images, row, column, client, SIMD executor, and server.
     *
     * @param subImages     The sub-images to process.
     * @param i             The row of the sub-image.
     * @param j             The column of the sub-image.
     * @param client        The client to use.
     * @param simdExecutor  The SIMD executor to use.
     * @param server        The server to use.
     */

    void processImagePart(BufferedImage[][] subImages, int i, int j, Client client, SIMDExecutor simdExecutor, Server server) throws InterruptedException {

        simdExecutor.execute(i, j);

        Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
        BufferedImage processedSubImage = ImageTransformer.createImageFromBytes(response.getImageSection());

        subImages[i][j] = processedSubImage;
    }

    /**
     * Sets the client window to use.
     *
     * @param clientWindow The client window to set.
     */
    public void setClientWindow(ClientWindow clientWindow) {
        this.clientWindow = clientWindow;
    }

    /**
     * Gets the waiting list.
     *
     * @return The waiting list.
     */
    public BlockingQueue getWaitingList() {
        return waitingList;
    }
}