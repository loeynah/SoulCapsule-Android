package com.finalwork.soulcapsule.network;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.ChatRequest;
import com.finalwork.soulcapsule.dto.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/chat/send")
    Call<ApiResponse<ChatResponse>> sendChatMessage(@Body ChatRequest request);
}
