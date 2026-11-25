package com.example.login_register;

public class CartItem {
    private final Product product;
    private final String colorName;
    private final int colorImageResId;
    private final int size;
    private int quantity;

    public CartItem(Product product, ProductColor color, int size, int quantity) {
        this(product, color == null ? "" : color.getName(),
                color == null ? R.drawable.ic_shoe_placeholder : color.getImageResId(),
                size, quantity);
    }

    public CartItem(Product product, String colorName, int colorImageResId,
                    int size, int quantity) {
        this.product = product;
        this.colorName = colorName;
        this.colorImageResId = colorImageResId;
        this.size = size;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public String getColorName() {
        return colorName;
    }

    public int getColorImageResId() {
        return colorImageResId;
    }

    public int getSize() {
        return size;
    }

    public String getKey() {
        return product.getId() + "|" + colorName + "|" + size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
