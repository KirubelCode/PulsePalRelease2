// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: When updating a user in settings this is the object layout for a user.

package com.movesense.showcaseapp.section_00_mainView;

public class UpdateUserRequest {
    public String fullName, email, password, gender;
    public int age;
    public float height, weight;
    public UpdateUserRequest(String f, String e, String p, int a, String g, float h, float w){
        fullName=f; email=e; password=p; age=a; gender=g; height=h; weight=w;
    }
}
