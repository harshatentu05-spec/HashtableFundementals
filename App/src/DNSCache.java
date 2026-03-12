import java.util.*;
import java.util.concurrent.*;

public class DNSCache {

    private final int capacity;
    private final long defaultTTLMillis;
    private final Map<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;

    // Entry class
    private static class DNSEntry {
        String ipAddress;
        long expiryTime;

        DNSEntry(String ipAddress, long ttlMillis) {
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public DNSCache(int capacity, long defaultTTLSeconds) {
        this.capacity = capacity;
        this.defaultTTLMillis = defaultTTLSeconds * 1000;
        // LinkedHashMap with accessOrder = true for LRU eviction
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        // Background thread to remove expired entries
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::removeExpiredEntries, defaultTTLSeconds, defaultTTLSeconds, TimeUnit.SECONDS);
    }

    /** Resolve a domain to IP */
    public synchronized String resolve(String domain) {
        DNSEntry entry = cache.get(domain);
        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        } else {
            misses++;
            String ip = queryUpstreamDNS(domain);
            cache.put(domain, new DNSEntry(ip, defaultTTLMillis));
            return "Cache MISS → Query upstream → " + ip + " (TTL: " + (defaultTTLMillis / 1000) + "s)";
        }
    }

    /** Simulate upstream DNS query */
    private String queryUpstreamDNS(String domain) {
        // Fake IP generation for demo
        return "192.168." + (new Random().nextInt(255)) + "." + (new Random().nextInt(255));
    }

    /** Remove expired entries */
    private synchronized void removeExpiredEntries() {
        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isExpired()) it.remove();
        }
    }

    /** Cache statistics */
    public synchronized String stats() {
        int total = hits + misses;
        double hitRatio = total == 0 ? 0 : (hits * 100.0 / total);
        double missRatio = total == 0 ? 0 : (misses * 100.0 / total);
        return String.format("Cache Hits: %d (%.2f%%), Misses: %d (%.2f%%)", hits, hitRatio, misses, missRatio);
    }

    /** Demo main */
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5, 10); // capacity 5, TTL 10 seconds
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nEnter domain to resolve (or 'exit'):");
            String domain = scanner.nextLine();
            if (domain.equalsIgnoreCase("exit")) break;

            System.out.println(dnsCache.resolve(domain));
            System.out.println("Stats: " + dnsCache.stats());
        }

        scanner.close();
    }
}