package com.example.android.newsfeed;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsArticle>>, SwipeRefreshLayout.OnRefreshListener {

    private static final int NEWS_LOADER_ID = 1;
    private static final String USGS_REQUEST_URL = "https://content.guardianapis.com";
    private static final String PAGE_SIZE = "30";
    private static final int FIRST_PAGE = 1;
    private NewsFeedAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private ProgressBar mProgressBar;
    private View mFooterView;
    private boolean hasMoreData = true;
    private SwipeRefreshLayout swipe;
    private LoaderManager loaderManager = null;
    private int mPageNumber = 0;
    private String mSearchKeyword = "";
    private boolean isSearchMode = false;
    EndlessScrollListener scrollListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        loaderManager = getLoaderManager();

        // Swipe to refresh the data using setOnRefreshListener
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(this);

        //define listview and asign empty view to show the message when there is error or no resultant data
        final ListView newsListView = (ListView) findViewById(R.id.list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Add footer to ListView to show loading progess of next page before setting adapter
        mFooterView = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar_footer, null, false);
        newsListView.addFooterView(mFooterView);

        // Create a new {@link ArrayAdapter} of NewsArticle and  link to listview
        mAdapter = new NewsFeedAdapter(this, new ArrayList<NewsArticle>());
        newsListView.setAdapter(mAdapter);

        //Set OnItemClickListener for ListView item
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsArticle currentRecord = mAdapter.getItem(position);
                //Location
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(currentRecord.getUrl()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        //Set EndlessSCrollListener for listview
        scrollListener= new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);

                if (!hasMoreData) {
                    return false;
                } else {
                    return true;
                } // ONLY if more data is actually being loaded; false otherwise.
            }
        };
        newsListView.setOnScrollListener(scrollListener);

        //Load first page
        loadNextDataFromApi(FIRST_PAGE);
    }

    private void loadNextDataFromApi(int page) {
        //Check network connection, if not avalaible show messages and return, else process
        if (hasNetworkConnected()) {
            if (page == FIRST_PAGE) {
                mProgressBar = (ProgressBar) findViewById(R.id.loading_spinner);
                mProgressBar.setIndeterminate(true);
                mEmptyStateTextView.setText("");
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            if (page == FIRST_PAGE) {
                mProgressBar = (ProgressBar) findViewById(R.id.loading_spinner);
                mEmptyStateTextView.setText(R.string.no_connection);
                mProgressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Mainly for search
        mPageNumber = page;
        if (page == 1) {
            mAdapter.clear();
        }

        if (loaderManager.getLoader(NEWS_LOADER_ID) == null) {
            loaderManager.initLoader(NEWS_LOADER_ID, null, NewsFeedActivity.this).forceLoad();
        } else {
            loaderManager.restartLoader(NEWS_LOADER_ID, null, NewsFeedActivity.this).forceLoad();
        }

        //In case of no matching data or data not available
        if (page == 1 && !hasMoreData) {
            mEmptyStateTextView.setText(R.string.no_news);
        }
    }

    @Override
    public Loader<List<NewsArticle>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        String countrySelected = sharedPrefs.getString(
                getString(R.string.settings_country_key),
                getString(R.string.settings_country_default));

        //Build URI
        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendEncodedPath(countrySelected);
        if (isSearchMode){
            uriBuilder.appendQueryParameter("q",mSearchKeyword.trim().toLowerCase());
        }
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("show-fields", "byline,trailText,publication,thumbnail");
        uriBuilder.appendQueryParameter("page", mPageNumber + "");
        uriBuilder.appendQueryParameter("page-size", PAGE_SIZE);
        uriBuilder.appendQueryParameter("api-key", getString(R.string.api_key));

        return new NewsFeedLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsArticle>> loader, List<NewsArticle> newsArticleList) {

        if (newsArticleList != null && !newsArticleList.isEmpty()) {
            mAdapter.addAll(newsArticleList);
            hasMoreData = true;
        } else {
            hasMoreData = false;
        }
        mProgressBar.setVisibility(View.GONE);
        swipe.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<NewsArticle>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    mSearchKeyword=query.trim();
                    scrollListener.resetState();
                    mAdapter.clear();
                    //isSearchMode = true;
                    loadNextDataFromApi(FIRST_PAGE);
                    TextView keywordView = (TextView) findViewById(R.id.keyword_display);
                    keywordView.setText(getString(R.string.search_key_prefix)+" "+query.trim());
                    //keywordView.setVisibility(View.GONE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem searchMenuItem =menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchKeyword="";
                scrollListener.resetState();
                mAdapter.clear();
                isSearchMode = true;
                TextView keywordView = (TextView) findViewById(R.id.keyword_display);
                keywordView.setVisibility(View.VISIBLE);
                mEmptyStateTextView.setText(getString(R.string.search_msg));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchKeyword="";
                scrollListener.resetState();
                mAdapter.clear();
                isSearchMode = false;
                TextView keywordView = (TextView) findViewById(R.id.keyword_display);
                keywordView.setVisibility(View.GONE);
                loadNextDataFromApi(FIRST_PAGE);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
    }

    private boolean hasNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
