package com.example.login_register;

public class ViewedProduct {
    public String productId, name;
    public double price;
    public long viewedAt;
    public Product product;

    public ViewedProduct(String productId, String name, double price,
                         long viewedAt, Product product) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.viewedAt = viewedAt;
        this.product = product;
    }
}