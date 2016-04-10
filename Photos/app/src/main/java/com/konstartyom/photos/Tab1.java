package com.konstartyom.photos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class Tab1 extends TabBase {
    protected static ArrayList<ImageDescriptor> getData() {
        ArrayList<ImageDescriptor> data = new ArrayList<>();
        File gallery = new File("/storage/emulated/0/DCIM/Camera");
        File[] files = gallery.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return mPattern.matcher(filename).matches();
            }
        });
        for (File file : files) {
            data.add(new ImageFileDescriptor(file));
        }
        return data;
    }

    @Override
    protected int getLoaderId() {
        return R.id.gallery_loader_id;
    }

    private static class MyLoader extends AsyncTaskLoader<ArrayList<ImageDescriptor>>{
        MyLoader(Context ctx){
            super(ctx);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public ArrayList<ImageDescriptor> loadInBackground() {
            return getData();
        }
    }


    @Override
    public Loader<ArrayList<ImageDescriptor>> onCreateLoader(int id, Bundle args) {
        return new MyLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ImageDescriptor>> loader, ArrayList<ImageDescriptor> data) {
        setData(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ImageDescriptor>> loader) {

    }
}
