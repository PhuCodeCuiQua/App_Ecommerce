package com.example.login_register;

import java.io.Serializable;

public class ProductColor implements Serializable {
    private final String name;
    private final String imageName;
    private final int imageResId;

    public ProductColor(String name, String imageName, int imageResId) {
        this.name = name;
        this.imageName = imageName;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public int getImageResId() {
        return imageResId;
    }
}
