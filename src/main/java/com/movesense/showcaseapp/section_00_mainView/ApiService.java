package com.movesense.showcaseapp.section_00_mainView;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json") // Ensure JSON format
    @POST("pulsepal/signup.php")
    Call<ResponseModel> signup(@Body User user);

    @POST("pulsepal/login.php")  // This will call login.php
    Call<ResponseModel> login(@Body LoginRequest loginRequest);
}
