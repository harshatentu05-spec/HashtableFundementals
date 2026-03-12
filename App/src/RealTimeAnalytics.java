import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalytics {

    // Total page views per URL
    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();

    // Unique visitors per URL
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source counts
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Scheduled executor for dashboard updates
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RealTimeAnalytics() {
        // Schedule dashboard print every 5 seconds
        scheduler.scheduleAtFixedRate(this::printDashboard, 5, 5, TimeUnit.SECONDS);
    }

    /** Process an incoming page view event */
    public void processEvent(String url, String userId, String source) {
        pageViews.merge(url, 1, Integer::sum);

        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);

        trafficSources.merge(source, 1, Integer::sum);
    }

    /** Print top 10 pages and traffic summary */
    private void printDashboard() {
        System.out.println("\n--- Real-Time Analytics Dashboard ---");

        // Top 10 pages by views
        PriorityQueue<Map.Entry<String, Integer>> topPages =
                new PriorityQueue<>(Map.Entry.comparingByValue());
        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            topPages.offer(entry);
            if (topPages.size() > 10) topPages.poll();
        }

        List<Map.Entry<String, Integer>> topList = new ArrayList<>();
        while (!topPages.isEmpty()) topList.add(topPages.poll());
        Collections.reverse(topList);

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topList) {
            String url = entry.getKey();
            int views = entry.getValue();
            int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)%n", rank++, url, views, uniques);
        }

        // Traffic sources
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            System.out.printf("%s → %d visits%n", entry.getKey(), entry.getValue());
        }

        System.out.println("-----------------------------------");
    }

    /** Demo main */
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalytics analytics = new RealTimeAnalytics();
        Random rand = new Random();
        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/gadgets"};
        String[] sources = {"google", "facebook", "twitter", "direct"};

        // Simulate random page view events
        for (int i = 0; i < 100; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String source = sources[rand.nextInt(sources.length)];
            String userId = "user_" + rand.nextInt(50);
            analytics.processEvent(url, userId, source);
            Thread.sleep(100); // simulate time between events
        }

        // Keep program running to see dashboard updates
        Thread.sleep(20000); // 20 seconds
        analytics.scheduler.shutdown();
    }
}