package com.example.mypc.ble_test;

/**
 * Created by MyPC on 2016/1/9.
 */
public class Fruit {
    private String name;
    private String address;
    private int imageId;
    public Fruit(String name, String address,int imageId) {
        this.name = name;
        this.imageId = imageId;
        this.address = address;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public int getImageId() {
        return imageId;
    }
}
