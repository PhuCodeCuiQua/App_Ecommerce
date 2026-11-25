package com.example.login_register;

public class Banner {
    private final String tagline;
    private final String title;
    private final String accentWord;
    private final String subtitle;
    private final String buttonText;
    private final String productId;
    private final int order;
    private final boolean active;

    public Banner(String tagline, String title, String accentWord, String subtitle,
                  String buttonText, String productId, int order, boolean active) {
        this.tagline = tagline;
        this.title = title;
        this.accentWord = accentWord;
        this.subtitle = subtitle;
        this.buttonText = buttonText;
        this.productId = productId;
        this.order = order;
        this.active = active;
    }

    public String getTagline() {
        return tagline;
    }

    public String getTitle() {
        return title;
    }

    public String getAccentWord() {
        return accentWord;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getProductId() {
        return productId;
    }

    public int getOrder() {
        return order;
    }

    public boolean isActive() {
        return active;
    }
}
