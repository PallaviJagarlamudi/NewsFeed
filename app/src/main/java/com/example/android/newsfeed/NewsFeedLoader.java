package com.example.android.newsfeed;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Pallavi J on 14-05-2017.
 */

public class NewsFeedLoader extends AsyncTaskLoader<List<NewsArticle>> {
    String mUrl;

    public NewsFeedLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<NewsArticle> loadInBackground() {
       /* try {
            Thread.sleep(4000);
        } catch (Exception e) {

        }*/
        if( mUrl == null){
            return null;
        }else{
            List<NewsArticle> newsList = Utils.extractNews(mUrl);
            return newsList;
        }
    }
}

