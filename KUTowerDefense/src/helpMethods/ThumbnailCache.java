package helpMethods;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * Manages caching of map preview thumbnails both in memory and on disk.
 * Provides cache invalidation when maps are edited or created.
 */
public class ThumbnailCache {
    private static ThumbnailCache instance;

    // In-memory cache: levelName -> BufferedImage
    private final Map<String, BufferedImage> memoryCache = new HashMap<>();

    // Track which levels have been modified in this session
    private final Set<String> modifiedLevels = new HashSet<>();

    // Cache directory path
    private static final String CACHE_DIR_NAME = "ThumbnailCache";

    private ThumbnailCache() {
        // Private constructor for singleton
    }

    public static ThumbnailCache getInstance() {
        if (instance == null) {
            instance = new ThumbnailCache();
        }
        return instance;
    }

    /**
     * Gets the appropriate cache directory path based on project structure
     * Uses the same flexible approach as LoadSave
     */
    private String getCacheDirectoryPath() {
        // Try multiple possible paths in order of preference
        String[] possiblePaths = {
                "src/main/resources/" + CACHE_DIR_NAME,           // Standard Maven structure from project root
                "demo/src/main/resources/" + CACHE_DIR_NAME,     // If running from parent directory
                "main/resources/" + CACHE_DIR_NAME,              // If running from src directory
                "resources/" + CACHE_DIR_NAME,                   // If running from src/main directory
                "KUTowerDefense/resources/" + CACHE_DIR_NAME     // Legacy structure
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                try {
                    return dir.getCanonicalPath();
                } catch (Exception e) {
                    return dir.getAbsolutePath();
                }
            }
        }

        // If none found, default to Maven structure and create it
        String defaultPath = "src/main/resources/" + CACHE_DIR_NAME;
        File defaultDir = new File(defaultPath);
        try {
            defaultDir.mkdirs();
            return defaultDir.getCanonicalPath();
        } catch (Exception e) {
            return defaultDir.getAbsolutePath();
        }
    }

    /**
     * Gets a thumbnail from cache (memory first, then disk)
     * @param levelName The name of the level
     * @param levelDataHash Hash of the level data for validation
     * @return Cached thumbnail or null if not found/invalid
     */
    public BufferedImage getCachedThumbnail(String levelName, int levelDataHash) {
        if (modifiedLevels.contains(levelName)) {
            return null;
        }

        BufferedImage memoryThumbnail = memoryCache.get(levelName);
        if (memoryThumbnail != null) {
            System.out.println("Thumbnail cache HIT (memory): " + levelName);
            return memoryThumbnail;
        }

        BufferedImage diskThumbnail = loadThumbnailFromDisk(levelName, levelDataHash);
        if (diskThumbnail != null) {
            memoryCache.put(levelName, diskThumbnail);
            System.out.println("Thumbnail cache HIT (disk): " + levelName);
            return diskThumbnail;
        }

        System.out.println("Thumbnail cache MISS: " + levelName);
        return null;
    }

    /**
     * Stores a thumbnail in both memory and disk cache
     * @param levelName The name of the level
     * @param thumbnail The thumbnail image
     * @param levelDataHash Hash of the level data for validation
     */
    public void cacheThumbnail(String levelName, BufferedImage thumbnail, int levelDataHash) {
        memoryCache.put(levelName, thumbnail);
        saveThumbnailToDisk(levelName, thumbnail, levelDataHash);
        modifiedLevels.remove(levelName);
        System.out.println("Thumbnail cached: " + levelName);
    }

    /**
     * Invalidates cache for a specific level (when it's been modified)
     * @param levelName The name of the level that was modified
     */
    public void invalidateLevel(String levelName) {
        memoryCache.remove(levelName);
        modifiedLevels.add(levelName);
        deleteThumbnailFromDisk(levelName);
        System.out.println("Thumbnail cache invalidated: " + levelName);
    }

    /**
     * Removes a thumbnail from cache completely (both memory and disk)
     * This is typically used when a level/save is deleted
     * @param levelName The name of the level to remove from cache
     */
    public void removeThumbnail(String levelName) {
        memoryCache.remove(levelName);
        modifiedLevels.remove(levelName);
        deleteThumbnailFromDisk(levelName);
        System.out.println("Thumbnail removed from cache: " + levelName);
    }


    /**
     * Loads a thumbnail from disk cache
     */
    private BufferedImage loadThumbnailFromDisk(String levelName, int expectedHash) {
        String cacheDir = getCacheDirectoryPath();
        String thumbnailPath = cacheDir + "/" + levelName + "_" + expectedHash + ".png";
        File thumbnailFile = new File(thumbnailPath);

        if (!thumbnailFile.exists()) {
            return null;
        }

        try {
            return ImageIO.read(thumbnailFile);
        } catch (IOException e) {
            System.err.println("Error loading thumbnail from disk: " + thumbnailPath + " - " + e.getMessage());
            thumbnailFile.delete();
            return null;
        }
    }

    /**
     * Saves a thumbnail to disk cache
     */
    private void saveThumbnailToDisk(String levelName, BufferedImage thumbnail, int levelDataHash) {
        String cacheDir = getCacheDirectoryPath();
        File cacheDirFile = new File(cacheDir);

        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }

        deleteThumbnailFromDisk(levelName);

        String thumbnailPath = cacheDir + "/" + levelName + "_" + levelDataHash + ".png";
        File thumbnailFile = new File(thumbnailPath);

        try {
            ImageIO.write(thumbnail, "png", thumbnailFile);
        } catch (IOException e) {
            System.err.println("Error saving thumbnail to disk: " + thumbnailPath + " - " + e.getMessage());
        }
    }

    /**
     * Deletes thumbnail files for a specific level from disk
     */
    private void deleteThumbnailFromDisk(String levelName) {
        String cacheDir = getCacheDirectoryPath();
        File cacheDirFile = new File(cacheDir);

        if (!cacheDirFile.exists()) {
            return;
        }

        File[] files = cacheDirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith(levelName + "_") && file.getName().endsWith(".png")) {
                    file.delete();
                }
            }
        }
    }

    /**
     * Gets cache statistics for debugging
     */
    public String getCacheStats() {
        return String.format("Memory cache: %d items, Modified levels: %d",
                memoryCache.size(), modifiedLevels.size());
    }
}