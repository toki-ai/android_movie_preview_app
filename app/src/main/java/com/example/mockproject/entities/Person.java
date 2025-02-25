package com.example.mockproject.entities;

import com.google.gson.annotations.SerializedName;

public class Person {
    @SerializedName("name")
    private String name;
    @SerializedName("profile_path")
    private String imagePath;

    public String getImageUrl() {
        return "https://image.tmdb.org/t/p/w500" + imagePath;
    }

    public String getName() {
        return name;
    }
}
