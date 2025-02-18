package com.movesense.showcaseapp.section_00_mainView;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("signup.php")
    Call<ResponseModel> signup(@Body User user);
}
