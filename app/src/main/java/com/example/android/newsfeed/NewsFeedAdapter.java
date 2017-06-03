package com.example.android.newsfeed;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Pallavi J on 15-05-2017.
 */

public class NewsFeedAdapter extends ArrayAdapter<NewsArticle> {
    Context mContext;

    public NewsFeedAdapter(Context context, List<NewsArticle> newsArticleList) {
        super(context, 0, newsArticleList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        //If ItemView is already created reuse it, else create
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_feed_item, parent, false);
        }

        //Get the current article to display
        NewsArticle currentNewsArticle = getItem(position);

        // Displays the title along with section name at end
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        SpannableStringBuilder spannableTitle = new SpannableStringBuilder("");
        spannableTitle.append(currentNewsArticle.getSection() + " --- " );
        spannableTitle.append(currentNewsArticle.getTitle());
        int endPos = currentNewsArticle.getSection().length();
        spannableTitle.setSpan(new StyleSpan(Typeface.NORMAL),0,endPos,0);
        spannableTitle.setSpan(new ForegroundColorSpan(Color.RED),0,endPos,0);
        titleTextView.setText(spannableTitle);

        // Display the Short description of the news article
        TextView trailTextView = (TextView) listItemView.findViewById(R.id.shortDesc);
        Spanned sp = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            sp = Html.fromHtml(position+"-->"+currentNewsArticle.getTrailText(), Html.FROM_HTML_MODE_COMPACT);
        }else{
            sp = Html.fromHtml(position+"-->"+currentNewsArticle.getTrailText());
        }
        trailTextView.setText(sp);

        // Displays image if available
        ImageView coverImageView = (ImageView) listItemView.findViewById(R.id.coverImageView);
        if (currentNewsArticle.hasImage()) {
            // loading album cover using Glide library
            Glide.with(mContext).load(currentNewsArticle.getImage()).centerCrop().into(coverImageView);
        } else {
            coverImageView.setVisibility(View.GONE);
        }

        // Displays how much time has elapsed from news publication
        TextView timeTextView = (TextView) listItemView.findViewById(R.id.timeElapsed);
        timeTextView.setText(getTimeElapsed(formatDate(currentNewsArticle.getPublicationDateInUTC())));

        // Displays author details
        TextView authorTextView = (TextView) listItemView.findViewById(R.id.author);
        if ( currentNewsArticle.hasAuthor() ){
            authorTextView.setText("- " + currentNewsArticle.getAuthor());
        }else{
            authorTextView.setVisibility(View.GONE);
        }

        return listItemView;
    }


    /**
     * Return the formatted date string  from a Date string.
     */
    private Date formatDate(String webDateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try{
            Date givenDate =  dateFormat.parse(webDateString);
            return givenDate;
        }catch (Exception e){
            return null;
        }
    }

    private String getTimeElapsed(Date date){
        StringBuilder timeElapsed = new StringBuilder("");
        if (date != null){
            timeElapsed.append(DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));
        }else{
            timeElapsed.append("Unknown");
        }
        return timeElapsed.toString();
    }
}
