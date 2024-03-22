import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the load information of servers.
 */

public class LoadInfo {
    /**
     * The filename of the load information.
     */
    private final String filename;

    /**
     * Constructs a new load information with the specified filename.
     *
     * @param filename The filename to use.
     */

    public LoadInfo(String filename) {
        this.filename = filename;
    }

    /**
     * Updates the load of the specified server.
     *
     * @param serverId The server ID to update.
     * @param load     The load to update.
     */

    public synchronized void updateLoad(String serverId, int load) {
        Map<String, Integer> loadMap = readLoadInfo();
        loadMap.put(serverId, load);
        writeLoadInfo(loadMap);
    }

    /**
     * Gets the least loaded server.
     *
     * @return The least loaded server.
     */

    public synchronized String getLeastLoadedServer(){
        Map<String, Integer> loadMap = readLoadInfo();
        return  Collections.min(loadMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Reads the load information from the file.
     *
     * @return The load information read from the file.
     */

    public Map<String, Integer> readLoadInfo() {
        Map<String, Integer> loadMap = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                loadMap.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadMap;
    }

    /**
     * Writes the load information to the file.
     *
     * @param loadMap The load information to write.
     */

    public void writeLoadInfo(Map<String, Integer> loadMap) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Integer> entry : loadMap.entrySet()) {
                writer.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
