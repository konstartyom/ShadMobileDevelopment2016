package com.konstartyom.photos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Tab1 extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private static final String IMAGE_PATTERN =
            "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp))$)";

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<File> mData;
    protected int mNumCol;
    protected Pattern mPattern = Pattern.compile(IMAGE_PATTERN);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Hi!!", TAG);
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Create view", TAG);
        View rootView = inflater.inflate(R.layout.tab_fragment_base, container, false);

        //.findViewById(R.id.tabs).getHeight();
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        mNumCol = getRequiredNumCol();

        /*mLayoutManager = new GridLayoutManager(getActivity(), mNumCol);

        mAdapter = new CustomAdapter(mData, getDisplayWidth() / mNumCol, getActivity());
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);*/

        refreshLayout();

        return rootView;
    }

    private void refreshLayout(){
        mLayoutManager = new GridLayoutManager(getActivity(), mNumCol);

        mAdapter = new CustomAdapter(mData, getDisplayWidth() / mNumCol,
                getDisplayWidth() / 3, getActivity());
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getRequiredNumCol(){
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("pref_numbercol", "4"));
    }

    @Override
    public void onResume(){
        super.onResume();
        int reqNumCol = getRequiredNumCol();
        if(reqNumCol != mNumCol) {
            mNumCol = reqNumCol;
            refreshLayout();
        }
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initData() {
        mData = new ArrayList<>();
        File gallery = new File("/storage/emulated/0/DCIM/Camera");
        File[] files = gallery.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return mPattern.matcher(filename).matches();
                //return filename.matches("jpg$");
                //return true;
            }
        });
        for(File file : files){
            mData.add(file);
            /*if(mData.size() >= 10){
                break;
            }*/
        }
    }

    private int getDisplayWidth(){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }
}
