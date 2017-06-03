package com.example.android.newsfeed;

/**
 * NewsArticle class to hold the data of a particular news article data
 */

public class NewsArticle {
    private String mTitle;
    private String mSection;
    private String mPublicationDateInUTC;
    private String mAuthor;
    private String mUrl;
    private String mImage;
    private String mTrailText;

    public NewsArticle(String title, String section, String url, String author, String publicationDate, String trailText, String imageLink) {
        this.mTitle = title;
        this.mSection = section;
        this.mPublicationDateInUTC = publicationDate;
        this.mAuthor = author;
        this.mUrl = url;
        this.mTrailText = trailText;
        this.mImage = imageLink;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public String getPublicationDateInUTC() {
        return mPublicationDateInUTC;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTrailText() {
        return mTrailText;
    }

    public String getImage() {
        return mImage;
    }

    public boolean hasImage(){
        if ( mImage == null || mImage.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public boolean hasAuthor(){
        if ( mAuthor == null || mAuthor.isEmpty()){
            return false;
        }else{
            return true;
        }
    }
}
