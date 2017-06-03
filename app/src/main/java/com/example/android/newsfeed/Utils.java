package com.example.android.newsfeed;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with Helper methods related to requesting and receiving news article data from Gaurdian.
 */

public class Utils {

    /** Tag for the log messages */
    public static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     * Return a list of {@link NewsArticle} objects that has been built up from
     * parsing a JSON response recieved from Guardian API.
     */
    public static List<NewsArticle> extractNews(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        List<NewsArticle> newsList = extractResultFromJson(jsonResponse);

        // Return the {@link Event}
        return newsList;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }



    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the newsfeed JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link NewsArticle} objects by parsing out information
     * of the news from the input newsJSON string.
     */
    private static List<NewsArticle> extractResultFromJson(String newsJSON) {

        ArrayList<NewsArticle> newsList = new ArrayList<>();
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        try {
            JSONObject baseJsonResponse = new JSONObject(newsJSON);
            JSONObject jsonResponse = baseJsonResponse.getJSONObject("response");
            JSONArray featureArray = jsonResponse.getJSONArray("results");

            // If there are results in the features array
            for ( int i=0; i< featureArray.length(); i++){
                JSONObject articleInfo = featureArray.getJSONObject(i);

                String articleTitle = articleInfo.getString("webTitle");
                String articleSection = articleInfo.getString("sectionName");
                String articleDate = articleInfo.getString("webPublicationDate");
                String articleWeblink = articleInfo.getString("webUrl");
                JSONObject articleExtraInfo = articleInfo.getJSONObject("fields");

                String articleAuthor = "";
                if (!articleExtraInfo.isNull("byline")) {
                    articleAuthor = articleExtraInfo.getString("byline");
                };

                String articleTrailText = articleExtraInfo.getString("trailText");

                String articleImageLink = "";
                if (!articleExtraInfo.isNull("thumbnail")) {
                    articleImageLink = articleExtraInfo.getString("thumbnail");
                }

                NewsArticle newsArticle = new NewsArticle(articleTitle, articleSection, articleWeblink,
                        articleAuthor, articleDate, articleTrailText, articleImageLink );
                newsList.add(newsArticle);
            }
            return newsList;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the News JSON results", e);
        }
        return null;
    }

}
