package code.example;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Staff-Level Implementation of a Sliding Window Counter.
 * Provides O(1) memory footprint per user while maintaining high numerical accuracy.
 * Handles race conditions via atomic primitives, mimicking the atomicity of Redis Lua scripts locally.
 */
public class SlidingWindowRateLimiter {

    /**
     * Entry point to execute a standalone simulation of the rate limiter.
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Program started.");
        // Instantiate a rate limiter: 1-second window allowing a maximum of 3 requests
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1000, 3);

        System.out.println("=== Starting Rate Limiter Simulation ===");

        // Fire 5 rapid requests in immediate succession
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = rateLimiter.allowRequest();
            System.out.printf("Request #%d -> Allowed: %b | Remaining Quota: %d | Reset In: %dms%n",
                    i, result.isAllowed(), result.getRemainingQuota(), result.getTimeToResetMillis());

            // Brief 50ms pause between rapid bursts
            Thread.sleep(50);
        }

        // Wait for the active 1-second window to expire completely
        System.out.println("\n[Waiting 1.1 seconds for the window to reset...]");
        Thread.sleep(1100);

        // Fire a fresh follow-up request to demonstrate resource recovery
        RateLimitResult recoveryResult = rateLimiter.allowRequest();
        System.out.printf("Request #6 (After Reset) -> Allowed: %b | Remaining Quota: %d%n",
                recoveryResult.isAllowed(), recoveryResult.getRemainingQuota());
        System.out.println("Program finished.");
    }

    private final long windowSizeMillis;
    private final long maxRequestsPerWindow;

    // Internal data frame representing a fixed slice of time.
    // In a distributed ecosystem, these fields are stored inside a localized Redis Hash.
    private static class WindowState {
        final long windowId;
        final AtomicLong count;

        WindowState(long windowId, long initialCount) {
            this.windowId = windowId;
            this.count = new AtomicLong(initialCount);
        }
    }

    // Volatile references to protect against multi-threaded instruction reordering and guarantee visibility
    private volatile WindowState previousWindow;
    private volatile WindowState currentWindow;

    public SlidingWindowRateLimiter(long windowSizeMillis, long maxRequestsPerWindow) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequestsPerWindow = maxRequestsPerWindow;

        long now = System.currentTimeMillis();
        long currentWindowId = now / windowSizeMillis;

        this.currentWindow = new WindowState(currentWindowId, 0);
        this.previousWindow = new WindowState(currentWindowId - 1, 0);
    }

    /**
     * Evaluates if a request is allowed under the sliding window constraint.
     * Computes a weighted average of the current and previous window counts on the fly.
     *
     * @return RateLimitResult containing the evaluation decision and backpressure metadata.
     */
    public synchronized RateLimitResult allowRequest() {
        long now = System.currentTimeMillis();
        long currentWindowId = now / windowSizeMillis;

        // Slide the windows forward if time has transitioned into a new block
        if (currentWindowId > currentWindow.windowId) {
            if (currentWindowId == currentWindow.windowId + 1) {
                previousWindow = currentWindow;
            } else {
                // Time skipped by more than 1 complete window; clear historical buffers completely
                previousWindow = new WindowState(currentWindowId - 1, 0);
            }
            currentWindow = new WindowState(currentWindowId, 0);
        }

        // Calculate the exact progress percentage inside the active window
        long timeElapsedInCurrentWindow = now % windowSizeMillis;
        double weightOfPreviousWindow = 1.0 - ((double) timeElapsedInCurrentWindow / windowSizeMillis);

        long prevCount = previousWindow.count.get();
        long currCount = currentWindow.count.get();

        // Mathematical Formula: Estimated requests = (prevCount * time remaining ratio in prev window) + currCount
        double estimatedRequests = (prevCount * weightOfPreviousWindow) + currCount;

        if (estimatedRequests >= maxRequestsPerWindow) {
            long msUntilReset = windowSizeMillis - timeElapsedInCurrentWindow;
            return new RateLimitResult(false, 0, msUntilReset);
        }

        // Capacity available: Commit the registration atomically
        currentWindow.count.incrementAndGet();
        long totalRemainingQuota = Math.max(0, maxRequestsPerWindow - (long) Math.ceil(estimatedRequests + 1));
        long msUntilReset = windowSizeMillis - timeElapsedInCurrentWindow;

        return new RateLimitResult(true, totalRemainingQuota, msUntilReset);
    }

    /**
     * Immutable Data Transfer Object mapping decision states cleanly to HTTP response headers.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingQuota;
        private final long timeToResetMillis;

        public RateLimitResult(boolean allowed, long remainingQuota, long timeToResetMillis) {
            this.allowed = allowed;
            this.remainingQuota = remainingQuota;
            this.timeToResetMillis = timeToResetMillis;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getRemainingQuota() {
            return remainingQuota;
        }

        public long getTimeToResetMillis() {
            return timeToResetMillis;
        }
    }
}