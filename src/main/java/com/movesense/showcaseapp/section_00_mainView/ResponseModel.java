// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Simple model to retrieve a user and the corresponding session id if needed.

package com.movesense.showcaseapp.section_00_mainView;

public class ResponseModel {
    public boolean success;
    public String message;
    private User user;

    private int session_id;

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public User getUser() {
        return user;
    }
    public int getSession_id() {
        return session_id;
    }
}
