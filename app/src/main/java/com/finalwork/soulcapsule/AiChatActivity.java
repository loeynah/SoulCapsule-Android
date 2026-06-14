package com.finalwork.soulcapsule;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.ChatRequest;
import com.finalwork.soulcapsule.dto.ChatResponse;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatActivity extends AppCompatActivity {

    private static final String SENDER_ID = "0";
    private static final String AI_ID = "1";

    private static final Author USER = new Author(SENDER_ID, "我", null);
    private static final Author AI = new Author(AI_ID, "小旅", null);

    private MessagesListAdapter<Message> messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MessagesList messagesList = findViewById(R.id.messages_list);
        MessageInput messageInput = findViewById(R.id.message_input);

        ImageLoader imageLoader = (imageView, url, payload) ->
                imageView.setImageResource(R.drawable.ic_ai_bot);

        messagesAdapter = new MessagesListAdapter<>(SENDER_ID, imageLoader);
        messagesList.setAdapter(messagesAdapter);

        addWelcomeMessage();

        messageInput.setInputListener(input -> {
            String text = input.toString().trim();
            if (text.isEmpty()) {
                return false;
            }

            Message userMessage = new Message(
                    generateMessageId(),
                    text,
                    USER,
                    new Date()
            );
            messagesAdapter.addToStart(userMessage, true);

            sendMessageToBackend(text);
            return true;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void sendMessageToBackend(String text) {
        ChatRequest request = new ChatRequest(text);
        RetrofitClient.getInstance().getApiService()
                .sendChatMessage(request)
                .enqueue(new Callback<ApiResponse<ChatResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatResponse>> call,
                                           Response<ApiResponse<ChatResponse>> response) {
                        if (isFinishing()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ChatResponse> apiResponse = response.body();
                            if (apiResponse.getCode() == 200
                                    && apiResponse.getData() != null
                                    && apiResponse.getData().getReply() != null) {
                                Message aiMessage = new Message(
                                        generateMessageId(),
                                        apiResponse.getData().getReply(),
                                        AI,
                                        new Date()
                                );
                                messagesAdapter.addToStart(aiMessage, true);
                            } else {
                                Toast.makeText(AiChatActivity.this,
                                        apiResponse.getMessage() != null
                                                ? apiResponse.getMessage()
                                                : "AI 回复失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AiChatActivity.this,
                                    "网络连接失败，请检查后端状态",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ChatResponse>> call, Throwable t) {
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(AiChatActivity.this,
                                "网络连接失败，请检查后端状态",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addWelcomeMessage() {
        Message welcome = new Message(
                generateMessageId(),
                getString(R.string.ai_chat_welcome),
                AI,
                new Date()
        );
        messagesAdapter.addToStart(welcome, false);
    }

    private String generateMessageId() {
        return String.valueOf(System.currentTimeMillis()) + "_" + System.nanoTime();
    }
}
