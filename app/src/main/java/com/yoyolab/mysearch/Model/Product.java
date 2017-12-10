package com.yoyolab.mysearch.Model;


public class Product {
    public int id, price;
    public String name;
    public String description;
    public String imageUrl;
    public String inWishList;

    public Product(int id, int price, String name, String description, String imageUrl) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.inWishList = null;
    }

    public Product(int id, int price, String name, String description, String imageUrl, String inWishList) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.inWishList = inWishList;
    }

}