package com.finalwork.soulcapsule.network;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.ChatRequest;
import com.finalwork.soulcapsule.dto.ChatResponse;
import com.finalwork.soulcapsule.dto.LoginResponse;
import com.finalwork.soulcapsule.dto.MoodRequest;
import com.finalwork.soulcapsule.dto.MoodResponse;
import com.finalwork.soulcapsule.dto.UserRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @Multipart
    @POST("api/upload")
    Call<ApiResponse<String>> uploadImage(@Part MultipartBody.Part file);

    @POST("api/chat/send")
    Call<ApiResponse<ChatResponse>> sendChatMessage(@Body ChatRequest request);

    @GET("api/ai/tips")
    Call<ApiResponse<List<String>>> getDecompressionTips(@Query("userId") Long userId);

    @POST("api/mood/add")
    Call<ApiResponse<Void>> addMoodRecord(@Body MoodRequest request);

    @POST("api/mood/update")
    Call<ApiResponse<Void>> updateMood(@Body MoodRequest request);

    @DELETE("api/mood/delete/{id}")
    Call<ApiResponse<Void>> deleteMood(@Path("id") Long id);

    @GET("api/mood/list")
    Call<ApiResponse<List<MoodResponse>>> getMoodList(@Query("userId") Long userId,
                                                        @Query("keyword") String keyword);

    @POST("api/user/register")
    Call<ApiResponse<Void>> register(@Body UserRequest request);

    @POST("api/user/login")
    Call<ApiResponse<LoginResponse>> login(@Body UserRequest request);
}
