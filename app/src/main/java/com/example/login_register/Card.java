package com.example.login_register;

public class Card {
    public long id;
    public String holder, last4, expiry, brand;

    public Card(long id, String holder, String last4, String expiry, String brand) {
        this.id = id;
        this.holder = holder;
        this.last4 = last4;
        this.expiry = expiry;
        this.brand = brand;
    }
}