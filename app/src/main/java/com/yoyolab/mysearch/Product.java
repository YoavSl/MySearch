package com.yoyolab.mysearch;

public class Product {
    public int id;
    public String name;
    public String description;
    public String imageUrl;

    Product(String name, String description, int id, String imageUrl) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.imageUrl = imageUrl;
    }

}