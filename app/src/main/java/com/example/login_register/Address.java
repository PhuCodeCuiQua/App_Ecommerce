package com.example.login_register;

public class Address {
    public long id;
    public String name, phone, detail;
    public boolean isDefault;

    public Address(long id, String name, String phone, String detail, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.detail = detail;
        this.isDefault = isDefault;
    }
}