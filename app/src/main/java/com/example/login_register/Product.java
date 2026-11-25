package com.example.login_register;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    private final String id;
    private final String name;
    private final String type;
    private final String gender;
    private final String collection;
    private final double originalPrice;
    private final int discountPercent;
    private final String description;
    private final String material;
    private final String designer;
    private final String features;
    private final List<Integer> sizes;
    private final List<ProductColor> colors;
    private final double ratingAverage;
    private final int ratingCount;
    private final int soldCount;

    public Product(String id, String name, String type, String gender, String collection,
                   double originalPrice, int discountPercent, String description,
                   String material, String designer, String features, List<Integer> sizes,
                   List<ProductColor> colors, double ratingAverage, int ratingCount,
                   int soldCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.gender = gender;
        this.collection = collection;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.description = description;
        this.material = material;
        this.designer = designer;
        this.features = features;
        this.sizes = sizes == null ? new ArrayList<>() : new ArrayList<>(sizes);
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
        this.soldCount = soldCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getGender() {
        return gender;
    }

    public String getCollection() {
        return collection;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public double getSalePrice() {
        return originalPrice * (100 - discountPercent) / 100.0;
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }

    public String getDescription() {
        return description;
    }

    public String getMaterial() {
        return material;
    }

    public String getDesigner() {
        return designer;
    }

    public String getFeatures() {
        return features;
    }

    public List<Integer> getSizes() {
        return new ArrayList<>(sizes);
    }

    public List<ProductColor> getColors() {
        return new ArrayList<>(colors);
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public int getImageResId() {
        return colors.isEmpty() ? R.drawable.ic_shoe_placeholder
                : colors.get(0).getImageResId();
    }
}
