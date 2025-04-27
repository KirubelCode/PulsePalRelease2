package com.movesense.showcaseapp.section_00_mainView;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("signup.php")
    Call<ResponseModel> signup(@Body User user);

    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<ResponseModel> login(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @POST("createSession.php")
    Call<ResponseModel> createSession(@Body CreateSessionRequest request);

    @Headers("Content-Type: application/json")
    @POST("insertDataPoint.php")
    Call<ResponseModel> insertDataPoint(@Body DataPointRequest request);

    @Headers("Content-Type: application/json")
    @POST("finalizeSession.php")
    Call<ResponseModel> finalizeSession(@Body FinalizeSessionRequest request);

    @Headers("Content-Type: application/json")
    @POST("getSessionDataPoints.php")
    Call<SessionDataResponse> getSessionDataPoints(@Body SessionDataRequest request);


    @POST("getSessions.php")
    Call<SessionListResponse> getSessions(@Body UserRequest request);

    @Headers("Content-Type: application/json")
    @POST("update_user.php")
    Call<ResponseModel> updateUser(@Body UpdateUserRequest user);

    @Headers("Content-Type: application/json")
    @POST("createGoal.php")
    Call<ResponseModel> createGoal(@Body CreateGoalRequest req);

    @Headers("Content-Type: application/json")
    @POST("getSessionGoals.php")
    Call<GoalsResponse> getSessionGoals(@Body JsonObject sessionIdJson);


}
