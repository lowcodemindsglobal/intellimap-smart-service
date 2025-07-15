package com.lcm.plugins.intellimapsmartservice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;
import java.time.Duration;

/**
 * Rate limiter for Azure OpenAI API calls
 * Implements sliding window rate limiting
 */
public class RateLimiter {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> windowStart = new ConcurrentHashMap<>();

    private final int maxRequestsPerMinute;
    private final int maxRequestsPerHour;
    private final Duration delayBetweenRequests;

    public RateLimiter() {
        this.maxRequestsPerMinute = IntelliMapConfig.MAX_REQUESTS_PER_MINUTE;
        this.maxRequestsPerHour = IntelliMapConfig.MAX_REQUESTS_PER_HOUR;
        this.delayBetweenRequests = IntelliMapConfig.RATE_LIMIT_DELAY;
    }

    public RateLimiter(int maxRequestsPerMinute, int maxRequestsPerHour, Duration delayBetweenRequests) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.delayBetweenRequests = delayBetweenRequests;
    }

    /**
     * Check if a request can be made and wait if necessary
     */
    public void checkRateLimit(String clientId) throws InterruptedException {
        String minuteKey = "minute:" + clientId + ":" + (Instant.now().getEpochSecond() / 60);
        String hourKey = "hour:" + clientId + ":" + (Instant.now().getEpochSecond() / 3600);

        // Check minute rate limit
        if (!checkLimit(minuteKey, maxRequestsPerMinute)) {
            throw new RuntimeException("Rate limit exceeded for minute");
        }

        // Check hour rate limit
        if (!checkLimit(hourKey, maxRequestsPerHour)) {
            throw new RuntimeException("Rate limit exceeded for hour");
        }

        // Apply delay between requests
        if (delayBetweenRequests.toMillis() > 0) {
            Thread.sleep(delayBetweenRequests.toMillis());
        }
    }

    /**
     * Check if limit is exceeded for a given key
     */
    private boolean checkLimit(String key, int maxRequests) {
        Instant now = Instant.now();

        // Get or create window start time
        windowStart.computeIfAbsent(key, k -> now);

        // Check if window has expired (1 minute for minute key, 1 hour for hour key)
        Duration windowDuration = key.startsWith("minute:") ? Duration.ofMinutes(1) : Duration.ofHours(1);
        if (now.isAfter(windowStart.get(key).plus(windowDuration))) {
            // Reset window
            windowStart.put(key, now);
            requestCounts.put(key, new AtomicInteger(0));
        }

        // Increment and check count
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();

        return currentCount <= maxRequests;
    }

    /**
     * Get current request count for a client
     */
    public int getCurrentRequestCount(String clientId) {
        String minuteKey = "minute:" + clientId + ":" + (Instant.now().getEpochSecond() / 60);
        AtomicInteger count = requestCounts.get(minuteKey);
        return count != null ? count.get() : 0;
    }

    /**
     * Reset rate limit counters for a client
     */
    public void resetCounters(String clientId) {
        String minuteKey = "minute:" + clientId + ":" + (Instant.now().getEpochSecond() / 60);
        String hourKey = "hour:" + clientId + ":" + (Instant.now().getEpochSecond() / 3600);

        requestCounts.remove(minuteKey);
        requestCounts.remove(hourKey);
        windowStart.remove(minuteKey);
        windowStart.remove(hourKey);
    }

    /**
     * Clean up expired entries
     */
    public void cleanup() {
        Instant now = Instant.now();

        // Clean up minute entries older than 2 minutes
        windowStart.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith("minute:")) {
                return now.isAfter(entry.getValue().plus(Duration.ofMinutes(2)));
            }
            return false;
        });

        // Clean up hour entries older than 2 hours
        windowStart.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith("hour:")) {
                return now.isAfter(entry.getValue().plus(Duration.ofHours(2)));
            }
            return false;
        });

        // Clean up corresponding request counts
        requestCounts.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            if (key.startsWith("minute:")) {
                return !windowStart.containsKey(key);
            } else if (key.startsWith("hour:")) {
                return !windowStart.containsKey(key);
            }
            return false;
        });
    }
}