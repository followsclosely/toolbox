package io.github.followsclosely.toolbox.web.cache;

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
        hint.set(String.join("-", value));
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

