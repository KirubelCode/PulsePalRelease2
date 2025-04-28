// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: An integral part of the app - Layout for User objects.

package com.movesense.showcaseapp.section_00_mainView;

public class User {
    private String fullName;
    private String email;
    private String password;
    private int age;
    private String gender;
    private float height;
    private float weight;

    public User(String fullName, String email, String password, int age, String gender, float height, float weight) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }
}

