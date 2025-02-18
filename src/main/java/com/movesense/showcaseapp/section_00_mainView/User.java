package com.movesense.showcaseapp.section_00_mainView;

public class User {
    private String fullName;
    private String email;
    private String password;
    private int age;  // Changed from String to int
    private String gender;
    private float height;  // Changed from String to float
    private float weight;  // Changed from String to float

    public User(String fullName, String email, String password, int age, String gender, float height, float weight) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }
}

