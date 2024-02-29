package com.zybooks.inventoryapp.inventorymodels;

import android.media.Image;

public class Inventory {
    private long id;
    private String name;
    private String imageUri;
    private long quantity;
    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Inventory(long id, String name, String imageUri, long quantity, String description) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.quantity = quantity;
        this.description = description;
    }
}