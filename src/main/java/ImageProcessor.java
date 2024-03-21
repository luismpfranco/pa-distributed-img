import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class ImageProcessor {
    private final BlockingQueue<ImagePart> waitingList = new LinkedBlockingQueue<>();
    private final Semaphore semaphore;
    private final ClientWindow clientWindow;

    public ImageProcessor(int numServers, ClientWindow clientWindow) {
        this.semaphore = new Semaphore(numServers);
        this.clientWindow = clientWindow;
    }

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

    private void processSubImage(BufferedImage[][] subImages, int i, int j, List<Server> servers, Client client, SIMDExecutor simdExecutor){
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
                    waitingList.put(new ImagePart(subImages[i][j], i, j));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            }
        }
    }

    private void processWaitingList(BufferedImage[][] subImages, List<Server> servers, SIMDExecutor simdExecutor, Client client) {
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

    private void processImagePart(BufferedImage[][] subImages, int i, int j, Client client, SIMDExecutor simdExecutor, Server server) throws InterruptedException {

        simdExecutor.execute(i, j);

        Request request = new Request("greeting", "Hello, Server!", subImages[i][j]);
        Response response = client.sendRequestAndReceiveResponse("localhost", server.getPort(), request);
        BufferedImage processedSubImage = ImageTransformer.createImageFromBytes(response.getImageSection());

        subImages[i][j] = processedSubImage;
    }
}