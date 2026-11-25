package com.example.login_register;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private Date createdAt;
    private String status;
    private double subtotal;
    private double shippingFee;
    private double discount;
    private String voucherCode;
    private String shippingMethod;
    private String paymentMethod;
    private String address;
    private double total;
    private final List<OrderItem> items;

    public Order(String id, Date createdAt, String status, double subtotal, double shippingFee,
                 double discount, String voucherCode, String shippingMethod, String paymentMethod,
                 String address, double total, List<OrderItem> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.status = status;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.voucherCode = voucherCode;
        this.shippingMethod = shippingMethod;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.total = total;
        this.items = new ArrayList<>(items);
    }

    public String getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public double getDiscount() {
        return discount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getAddress() {
        return address;
    }

    public double getTotal() {
        return total;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    void setId(String id) {
        this.id = id;
    }

    void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    void setStatus(String status) {
        this.status = status;
    }
}
