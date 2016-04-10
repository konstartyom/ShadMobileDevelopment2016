package com.konstartyom.photos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Tab2 extends TabBase {
    private static class MyLoader extends AsyncTaskLoader<ArrayList<ImageDescriptor>> {
        private static final String TAG = "MyLoader2";

        public MyLoader(Context ctx){
            super(ctx);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public ArrayList<ImageDescriptor> loadInBackground() {
            return loadUrls();
        }

        private ArrayList<ImageDescriptor> loadUrls() {
            ArrayList<ImageDescriptor> list = new ArrayList<>();
            try {
                URL url = new URL("http://api-fotki.yandex.ru/api/podhistory/poddate;2012-04-01T12:00:00Z/");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream stream = conn.getInputStream();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    ImageUrlDescriptor imgDesc = null;
                    while((imgDesc = processEntry(parser)) != null){
                        list.add(imgDesc);
                    }
                }
                else{
                    Log.d(TAG, "Got response code: " + conn.getResponseCode());
                }

            } catch(IOException | XmlPullParserException e){
                Log.d(TAG, "Exception: " + e);
            }
            return list;
        }

        private ImageUrlDescriptor processEntry(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            boolean hasImages = false;
            ImageUrlDescriptor desc = new ImageUrlDescriptor();
            while(parser.next() != XmlPullParser.END_DOCUMENT){
                if(parser.getEventType() != XmlPullParser.START_TAG){
                    continue;
                }
                String name = parser.getName();
                String prefix = parser.getPrefix();
                if("f".equals(prefix) && "img".equals(name)){
                    hasImages = true;

                    for(int i = 0; i < parser.getAttributeCount(); ++i){

                        if("height".equals(parser.getAttributeName(i))){
                            desc.addOption(Integer.parseInt(parser.getAttributeValue(i)),
                                    parser.getAttributeValue(i+1));
                            break;
                        }
                    }
                }
                else if(hasImages){
                    return desc;
                }
            }
            return hasImages ? desc : null;
        }
    }

    @Override
    public Loader<ArrayList<ImageDescriptor>> onCreateLoader(int id, Bundle args) {
        return new MyLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ImageDescriptor>> loader, ArrayList<ImageDescriptor> data) {
        GlobalContext.getImageDescHolder().addUrls(data);
        setData(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ImageDescriptor>> loader) {

    }

    @Override
    protected int getLoaderId() {
        return R.id.internet_loader_id;
    }


}
