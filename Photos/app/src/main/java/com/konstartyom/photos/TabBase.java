package com.konstartyom.photos;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class TabBase extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<ImageDescriptor>> {

    private static final String TAG = "Tab1Fragment";

    private static final String IMAGE_PATTERN =
            "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp))$)";

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected int mNumCol;
    protected static Pattern mPattern = Pattern.compile(IMAGE_PATTERN);
    protected ArrayList<ImageDescriptor> mData = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Hi!!", TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Create view", TAG);
        View rootView = inflater.inflate(R.layout.tab_fragment_base, container, false);

        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        mNumCol = getRequiredNumCol();

        refreshLayout();

        return rootView;
    }

    protected void setData(ArrayList<ImageDescriptor> newData){
        mData = newData;
        mAdapter.setData(mData);
    }

    protected void refreshLayout() {
        mLayoutManager = new GridLayoutManager(getActivity(), mNumCol);

        mAdapter = new CustomAdapter(mData, getDisplayWidth() / mNumCol, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        Log.d(TAG, "Refreshed");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getRequiredNumCol() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("pref_numbercol", "4"));
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().initLoader(getLoaderId(), null, this);
        int reqNumCol = getRequiredNumCol();
        if (reqNumCol != mNumCol) {
            mNumCol = reqNumCol;
            refreshLayout();
        }
    }

    //protected abstract ArrayList<ImageDescriptor> getData();

    protected abstract int getLoaderId();

    //private void initData(){
     //   mData = getData();
    //}

    private int getDisplayWidth() {
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }
}

