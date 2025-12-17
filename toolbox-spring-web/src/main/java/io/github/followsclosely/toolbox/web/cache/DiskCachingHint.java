package io.github.followsclosely.toolbox.web.cache;

import java.util.Arrays;

/**
 * Stores a per-thread hint for disk caching, allowing human-readable cache file names.
 */
public class DiskCachingHint {

    private static final ThreadLocal<String> hint = new ThreadLocal<>();

    /**
     * Sets the current thread's cache hint.
     */
    public static void set(String value) {
        hint.set(value);
    }
    public static void set(String... value) {
        String[] cleaned = Arrays.stream(value)
                .map(token -> token.replaceAll("[^a-zA-Z0-9._-]", "_"))
                .toArray(String[]::new);

        hint.set(String.join("/", cleaned));
    }

    /**
     * Gets the current thread's cache hint, or null if not set.
     */
    public static String get() {
        return hint.get();
    }

    /**
     * Clears the current thread's cache hint.
     */
    public static void clear() {
        hint.remove();
    }
}

