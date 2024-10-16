package com.example.appchatt;
import com.example.appchatt.Models.Chat;
import com.example.appchatt.Models.LoginResponse;
import com.example.appchatt.Models.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    Call<ResponseBody> registerUser(@Body User user);

    Call<LoginResponse> loginUser(@Body User user);

    @GET("chats/{username}")
    Call<List<Chat>> getChatHistory(@Path("username") String username);
}
