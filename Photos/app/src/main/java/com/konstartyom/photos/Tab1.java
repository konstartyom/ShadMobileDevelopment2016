package com.konstartyom.photos;

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

import java.io.File;
import java.io.FilenameFilter;
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

        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        mNumCol = getRequiredNumCol();

        refreshLayout();

        return rootView;
    }

    private void refreshLayout() {
        mLayoutManager = new GridLayoutManager(getActivity(), mNumCol);

        mAdapter = new CustomAdapter(mData, getDisplayWidth() / mNumCol, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
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
        int reqNumCol = getRequiredNumCol();
        if (reqNumCol != mNumCol) {
            mNumCol = reqNumCol;
            refreshLayout();
        }
    }

    private void initData() {
        mData = new ArrayList<>();
        File gallery = new File("/storage/emulated/0/DCIM/Camera");
        File[] files = gallery.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return mPattern.matcher(filename).matches();
            }
        });
        for (File file : files) {
            mData.add(file);
        }
    }

    private int getDisplayWidth() {
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }
}
