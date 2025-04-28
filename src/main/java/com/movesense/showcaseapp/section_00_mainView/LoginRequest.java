// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Layout for login.


package com.movesense.showcaseapp.section_00_mainView;

public class LoginRequest {
    private boolean success;
    private String message;
    private User user; // <-- nested user object

    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
