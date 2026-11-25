package com.example.login_register;

public final class Voucher {
    public static final Voucher NONE = new Voucher("NONE", "Không dùng voucher",
            0, 0, 0, false);

    private final String id;
    private final String name;
    private final double discountRate;
    private final double maximumDiscount;
    private final double fixedDiscount;
    private final boolean freeShipping;

    public Voucher(String id, String name, double discountRate, double maximumDiscount,
                   double fixedDiscount, boolean freeShipping) {
        this.id = id;
        this.name = name;
        this.discountRate = discountRate;
        this.maximumDiscount = maximumDiscount;
        this.fixedDiscount = fixedDiscount;
        this.freeShipping = freeShipping;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public double calculateDiscount(double subtotal) {
        if (discountRate > 0) {
            return Math.min(subtotal * discountRate, maximumDiscount);
        }
        return Math.min(subtotal, fixedDiscount);
    }
}
