package com.konstartyom.photos;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoader {
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeBitmapFromName(String fname, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fname, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fname, options);
    }

    public static byte[] getByteArrayFromStream(InputStream is) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException _) {
            return new byte[0];
        }
    }

    public static byte[] getByteArrayFromUrl(String url) throws IOException {
        return getByteArrayFromStream((InputStream) new URL(url).getContent());
    }

    public static Bitmap decodeBitmapFromByteArray(byte image[], int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeByteArray(image, 0, image.length, options);
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(image, 0, image.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(image, 0, image.length, options);
    }

    public static Bitmap decodeBitmapFromUrl(String url, int reqWidth, int reqHeight) {
        try {
            return decodeBitmapFromByteArray(getByteArrayFromUrl(url), reqWidth, reqHeight);
        } catch (IOException _) {
        }
        return null;
    }

    public static Bitmap decodeBitmap(File file, int reqWidth, int reqHeight) {
        try {
            return decodeBitmapFromName(file.getCanonicalPath(), reqWidth, reqHeight);
        } catch (IOException _) {
            return null;
        }
    }
}
