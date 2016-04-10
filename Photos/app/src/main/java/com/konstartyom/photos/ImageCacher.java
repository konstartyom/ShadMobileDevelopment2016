package com.konstartyom.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Base64;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ImageCacher {
    private LruCache<String, Bitmap> sBitmapCache;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private boolean mDiskCacheFailed = false;

    private static final String TAG = "ImageCacher";

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            sBitmapCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return sBitmapCache.get(key);
    }

    private void init(){
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

    private boolean initDiskCache(final File cacheDir, final int cacheSize){
        mDiskCacheStarting = true;
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... unused){
                synchronized (mDiskCacheLock) {
                    try {
                        mDiskLruCache = DiskLruCache.open(cacheDir, 1, 2, cacheSize);
                        mDiskCacheStarting = false; // Finished initialization
                        mDiskCacheLock.notifyAll(); // Wake any waiting threads
                    }
                    catch(IOException _){
                        mDiskCacheFailed = true;
                    }
                }
                return null;
            }
        }.execute();
        return !mDiskCacheStarting;
    }

    public void clearDiskCache(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... unused){
                synchronized (mDiskCacheLock) {
                    try {
                        File myDir = mDiskLruCache.getDirectory();
                        long mySize = mDiskLruCache.getMaxSize();
                        mDiskLruCache.delete();
                        mDiskLruCache = DiskLruCache.open(myDir, 1, 2, mySize);
                    }
                    catch(IOException _){
                        mDiskCacheFailed = true;
                    }
                }
                return null;
            }
        }.execute();
    }

    private String toInternalKey(String key){
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(key.getBytes(), 0, key.length());
            return new BigInteger(1,md.digest()).toString(16);
        }
        catch (NoSuchAlgorithmException _){}
        return null;
    }

    public void addImageToDiskCache(String key, byte image[]) {
        synchronized (mDiskCacheLock) {
            try {
                if (mDiskLruCache != null) {
                    DiskLruCache.Editor e = mDiskLruCache.edit(toInternalKey(key));
                    if(e != null) {
                        e.newOutputStream(0).write(image);
                        e.newOutputStream(1).write(0);
                        e.commit();
                    }
                }
            }
            catch (IOException _){}
        }
    }

    public byte[] getImageFromDiskCache(String key) {
        synchronized (mDiskCacheLock) {
            try {
                // Wait while disk cache is started from background thread
                while (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException _) {}
                }
                if (mDiskLruCache != null && !mDiskCacheFailed) {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(toInternalKey(key));
                    if(snapshot != null){
                        return ImageLoader.getByteArrayFromStream(snapshot.getInputStream(0));
                    }
                }
            } catch (IOException _) {}
        }
        return null;
    }

    private File prepareCacheDir(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ?
                        GlobalContext.getApplicationContext().getExternalCacheDir() :
                        GlobalContext.getApplicationContext().getCacheDir();
    }

    public ImageCacher(){
        Log.d(TAG, "Creating..");
        init();
        initDiskCache(prepareCacheDir(), 100 * 1024 * 1024);
        Log.d(TAG, "Created");
    }
}
