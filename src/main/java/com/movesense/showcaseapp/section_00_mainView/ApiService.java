package com.movesense.showcaseapp.section_00_mainView;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("pulsepal/signup.php")
    Call<ResponseModel> signup(@Body User user);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/login.php")
    Call<ResponseModel> login(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/createSession.php")
    Call<ResponseModel> createSession(@Body CreateSessionRequest request);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/insertDataPoint.php")
    Call<ResponseModel> insertDataPoint(@Body DataPointRequest request);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/finalizeSession.php")
    Call<ResponseModel> finalizeSession(@Body FinalizeSessionRequest request);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/getSessionDataPoints.php")
    Call<SessionDataResponse> getSessionDataPoints(@Body SessionDataRequest request);


    @POST("pulsepal/getSessions.php")
    Call<SessionListResponse> getSessions(@Body UserRequest request);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/update_user.php")
    Call<ResponseModel> updateUser(@Body UpdateUserRequest user);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/createGoal.php")
    Call<ResponseModel> createGoal(@Body CreateGoalRequest req);

    @Headers("Content-Type: application/json")
    @POST("pulsepal/getSessionGoals.php")
    Call<GoalsResponse> getSessionGoals(@Body JsonObject sessionIdJson);


}
