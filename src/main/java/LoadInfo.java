import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LoadInfo {
    private final String filename;
    public LoadInfo(String filename) {
        this.filename = filename;
    }

    public synchronized void updateLoad(String serverId, int load) {
        Map<String, Integer> loadMap = readLoadInfo();
        loadMap.put(serverId, load);
        writeLoadInfo(loadMap);
    }

    public synchronized String getLeastLoadedServer(){
        Map<String, Integer> loadMap = readLoadInfo();
        return  Collections.min(loadMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

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
