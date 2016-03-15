package com.konstartyom.photos;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImageCacher {
    private static LruCache<String, Bitmap> sBitmapCache;

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            sBitmapCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return sBitmapCache.get(key);
    }

    public static void init(){
        if(sBitmapCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 3;
            sBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }


            };
        }
    }
}
