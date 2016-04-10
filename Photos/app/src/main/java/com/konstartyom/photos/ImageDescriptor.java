package com.konstartyom.photos;

import android.graphics.Bitmap;
import android.os.Parcelable;

public abstract class ImageDescriptor implements Parcelable{
    public abstract Bitmap toBitmap(int requestedWidth, int requestedHeight);

    public abstract String getUniqueRepr();
}
