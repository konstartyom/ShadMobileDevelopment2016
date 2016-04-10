package com.konstartyom.photos;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

//this class is mainly used to communicate with PrefetchService
public class ImageDescHolder {
    private ConcurrentLinkedQueue<ImageDescriptor> mDescriptors = new ConcurrentLinkedQueue<>();

    public void addUrls(ArrayList<ImageDescriptor> descriptors){
        mDescriptors.addAll(descriptors);
    }

    public boolean isEmpty(){
        return mDescriptors.isEmpty();
    }

    public ImageDescriptor poll(){
        return mDescriptors.poll();
    }
}
