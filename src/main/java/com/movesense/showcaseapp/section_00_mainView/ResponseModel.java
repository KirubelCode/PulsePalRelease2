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
