import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

    private static class TokenBucket {
        final int maxTokens;
        final long refillIntervalMillis;
        AtomicInteger tokens;
        long lastRefillTime;

        TokenBucket(int maxTokens, long refillIntervalMillis) {
            this.maxTokens = maxTokens;
            this.refillIntervalMillis = refillIntervalMillis;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            } else {
                return false;
            }
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime >= refillIntervalMillis) {
                tokens.set(maxTokens);
                lastRefillTime = now;
            }
        }

        int getRemainingTokens() {
            refill();
            return tokens.get();
        }
    }

    private final Map<String, TokenBucket> clients = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long refillIntervalMillis;

    public RateLimiter(int maxRequestsPerHour) {
        this.maxRequests = maxRequestsPerHour;
        this.refillIntervalMillis = 60 * 60 * 1000L; // 1 hour
    }

    /** Check if a client is allowed to make a request */
    public String checkRateLimit(String clientId) {
        clients.putIfAbsent(clientId, new TokenBucket(maxRequests, refillIntervalMillis));
        TokenBucket bucket = clients.get(clientId);

        boolean allowed = bucket.tryConsume();
        if (allowed) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retryAfter = (bucket.lastRefillTime + refillIntervalMillis - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    /** Get current rate limit status */
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);
        if (bucket == null) return "Client not found";

        long resetTime = bucket.lastRefillTime + refillIntervalMillis;
        int used = maxRequests - bucket.getRemainingTokens();
        return String.format("{used: %d, limit: %d, reset: %d}", used, maxRequests, resetTime / 1000);
    }

    /** Demo main */
    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter(10); // 10 requests per hour for demo
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter clientId to test (or 'exit'):");
        while (true) {
            String clientId = scanner.nextLine();
            if (clientId.equalsIgnoreCase("exit")) break;

            System.out.println(limiter.checkRateLimit(clientId));
            System.out.println(limiter.getRateLimitStatus(clientId));
        }

        scanner.close();
    }
}