package com.konstartyom.photos;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUrlDescriptor extends ImageDescriptor{
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        List<String> arr = new ArrayList<>();
        for(SizedDescription desc : mOptions){
            arr.add(desc.toString());
        }
        dest.writeStringList(arr);
    }

    private class SizedDescription{
        public int mHeight;
        public String mUrl;

        public SizedDescription(int height, String url){
            mHeight = height;
            mUrl = url;
        }

        public SizedDescription(String string){
            String[] keys = string.split("\\€");
            mHeight = Integer.parseInt(keys[0]);
            mUrl = keys[1];
        }

        public String toString(){
            return Integer.valueOf(mHeight).toString() + "€" + mUrl;
        }
    }

    private ArrayList<SizedDescription> mOptions;

    public void addOption(int height, String url){
        mOptions.add(new SizedDescription(height, url));
    }

    @Override
    public Bitmap toBitmap(int width, int height) {
        SizedDescription result = mOptions.get(0);
        for(SizedDescription desc : mOptions){
            if(desc.mHeight > result.mHeight){
                result = desc;
            }
        }
        for(SizedDescription desc : mOptions){
            if(desc.mHeight >= height && desc.mHeight < result.mHeight){
                result = desc;
            }
        }
        String key = Integer.valueOf(result.mHeight).toString() + getUniqueRepr();
        byte image[] = GlobalContext.getImageCacher().getImageFromDiskCache(key);
        if(image != null){
            return ImageLoader.decodeBitmapFromByteArray(image, width, height);
        }
        // try to find in disk cache bitmap with larger size
        for(SizedDescription desc : mOptions){
            if(desc.mHeight > result.mHeight){
                String tryKey = Integer.valueOf(desc.mHeight).toString() + getUniqueRepr();
                image = GlobalContext.getImageCacher().getImageFromDiskCache(tryKey);
                if(image != null){
                    return ImageLoader.decodeBitmapFromByteArray(image, width, height);
                }
            }
        }
        try {
            image = ImageLoader.getByteArrayFromUrl(result.mUrl);
            GlobalContext.getImageCacher().addImageToDiskCache(key, image);
            return ImageLoader.decodeBitmapFromByteArray(image, width, height);
        }
        catch (IOException _) {}
        return null;
    }

    @Override
    public String getUniqueRepr() {
        return mOptions.get(0).mUrl;
    }

    public static final Parcelable.Creator<ImageUrlDescriptor> CREATOR
            = new Parcelable.Creator<ImageUrlDescriptor>() {
        public ImageUrlDescriptor createFromParcel(Parcel in) {
            return new ImageUrlDescriptor(in);
        }

        public ImageUrlDescriptor[] newArray(int size) {
            return new ImageUrlDescriptor[size];
        }
    };

    private ImageUrlDescriptor(Parcel in) {
        List<String> arr = new ArrayList<>();
        mOptions = new ArrayList<>();
        in.readStringList(arr);
        for(String item : arr){
            mOptions.add(new SizedDescription(item));
        }
    }

    public ImageUrlDescriptor(){
        mOptions = new ArrayList<>();
    }
}
