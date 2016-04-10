package com.konstartyom.photos;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;

public class ImageFileDescriptor extends ImageDescriptor{
    private String mFileName;

    public ImageFileDescriptor(File file){
        try {
            mFileName = file.getCanonicalPath();
        }
        catch (IOException _) {}
    }

    public ImageFileDescriptor(String canonicalFilePath){
        mFileName = canonicalFilePath;
    }

    @Override
    public Bitmap toBitmap(int requestedWidth, int requestedHeight) {
        return ImageLoader.decodeBitmapFromName(mFileName, requestedWidth, requestedHeight);
    }

    @Override
    public String getUniqueRepr() {
        return mFileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFileName);
    }

    public static final Parcelable.Creator<ImageFileDescriptor> CREATOR
            = new Parcelable.Creator<ImageFileDescriptor>() {
        public ImageFileDescriptor createFromParcel(Parcel in) {
            return new ImageFileDescriptor(in);
        }

        public ImageFileDescriptor[] newArray(int size) {
            return new ImageFileDescriptor[size];
        }
    };

    private ImageFileDescriptor(Parcel in) {
        mFileName = in.readString();
    }
}
