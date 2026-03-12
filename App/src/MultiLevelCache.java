import java.util.*;

public class MultiLevelCache {

    static class Video {
        String videoId;
        String content; // in-memory content or metadata
        Video(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    // L1: In-memory LRU cache
    private final LinkedHashMap<String, Video> L1;
    private final int L1_CAPACITY;

    // L2: SSD-backed (simulate with HashMap pointing to "file path")
    private final HashMap<String, String> L2;
    private final int L2_CAPACITY;
    private final HashMap<String, Integer> L2AccessCount; // track access frequency

    // L3: Database (simulate with HashMap)
    private final HashMap<String, Video> L3;

    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;

    public MultiLevelCache(int L1Capacity, int L2Capacity) {
        this.L1_CAPACITY = L1Capacity;
        this.L2_CAPACITY = L2Capacity;
        this.L1 = new LinkedHashMap<>(L1Capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Video> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        this.L2 = new HashMap<>();
        this.L2AccessCount = new HashMap<>();
        this.L3 = new HashMap<>();
    }

    /** Load video into L3 (database) */
    public void loadVideoToDB(String videoId, String content) {
        L3.put(videoId, new Video(videoId, content));
        // Simulate SSD path in L2
        if (L2.size() < L2_CAPACITY) {
            L2.put(videoId, "/ssd/path/" + videoId);
            L2AccessCount.put(videoId, 0);
        }
    }

    /** Access a video */
    public Video getVideo(String videoId) {
        // L1 Cache check
        if (L1.containsKey(videoId)) {
            L1Hits++;
            return L1.get(videoId);
        }

        // L2 Cache check
        if (L2.containsKey(videoId)) {
            L2Hits++;
            L2AccessCount.put(videoId, L2AccessCount.get(videoId) + 1);
            // Promote to L1 if access count > threshold
            if (L2AccessCount.get(videoId) >= 3) {
                Video videoFromL3 = L3.get(videoId);
                if (videoFromL3 != null) {
                    L1.put(videoId, videoFromL3);
                    System.out.println("Promoted " + videoId + " to L1");
                }
            }
            return L3.get(videoId); // Simulate fetching content
        }

        // L3 Database
        if (L3.containsKey(videoId)) {
            L3Hits++;
            // Optional: load into L2 if space
            if (L2.size() < L2_CAPACITY) {
                L2.put(videoId, "/ssd/path/" + videoId);
                L2AccessCount.put(videoId, 1);
            }
            return L3.get(videoId);
        }

        return null; // not found
    }

    /** Cache statistics */
    public void printStats() {
        int total = L1Hits + L2Hits + L3Hits;
        System.out.printf("Cache Hits -> L1: %.2f%%, L2: %.2f%%, L3: %.2f%%\n",
                total == 0 ? 0 : 100.0 * L1Hits / total,
                total == 0 ? 0 : 100.0 * L2Hits / total,
                total == 0 ? 0 : 100.0 * L3Hits / total);
    }

    /** Demo main */
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache(3, 5);

        // Load videos
        for (int i = 1; i <= 10; i++) {
            cache.loadVideoToDB("video_" + i, "Content for video " + i);
        }

        // Simulate access
        String[] accesses = {"video_1", "video_2", "video_1", "video_3", "video_1", "video_4", "video_2", "video_5"};
        for (String vid : accesses) {
            System.out.println("Accessing " + vid);
            cache.getVideo(vid);
        }

        cache.printStats();
    }
}