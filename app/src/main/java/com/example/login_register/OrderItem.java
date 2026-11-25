package com.example.login_register;

public class OrderItem {
    private final String productId;
    private final String name;
    private final String colorName;
    private final int size;
    private final int quantity;
    private final double unitPrice;
    private final String imageName;

    public OrderItem(String productId, String name, String colorName, int size, int quantity,
                     double unitPrice, String imageName) {
        this.productId = productId;
        this.name = name;
        this.colorName = colorName;
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.imageName = imageName;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getColorName() {
        return colorName;
    }

    public int getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getImageName() {
        return imageName;
    }
}
