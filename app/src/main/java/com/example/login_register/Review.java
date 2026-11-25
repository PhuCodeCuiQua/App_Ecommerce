package com.example.login_register;

public class Review {
    private final String author;
    private final int stars;
    private final String content;
    private final String date;

    public Review(String author, int stars, String content, String date) {
        this.author = author;
        this.stars = stars;
        this.content = content;
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public int getStars() {
        return stars;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
