package com.yoyolab.mysearch.Model;

public class Product {
    public int id;
    public String name;
    public String description;
    public String imageUrl;
    public String inWishList;

    public Product(String name, String description, int id, String imageUrl) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.imageUrl = imageUrl;
        this.inWishList = null;
    }

    public Product(String name, String description, int id, String imageUrl, String inWishList) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.imageUrl = imageUrl;
        this.inWishList = inWishList;
    }

}