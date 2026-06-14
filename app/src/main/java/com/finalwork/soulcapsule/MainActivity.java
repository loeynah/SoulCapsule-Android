package com.finalwork.soulcapsule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private static final List<String> FALLBACK_TIPS = Arrays.asList(
            "去吹吹晚风吧",
            "喝一杯热牛奶"
    );

    private TextView tvGreeting;
    private TextView btnRecord;
    private TextView btnStartChat;
    private RecyclerView rvCapsules;
    private CapsuleAdapter capsuleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvGreeting = findViewById(R.id.tv_greeting);
        btnRecord = findViewById(R.id.btn_record);
        btnStartChat = findViewById(R.id.btn_start_chat);
        rvCapsules = findViewById(R.id.rv_capsules);

        updateGreeting();
        setupClickListeners();
        setupBottomNav();
        setupCapsuleList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDecompressionTips();
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (hour >= 5 && hour < 12) {
            timeGreeting = getString(R.string.greeting_morning);
        } else if (hour >= 12 && hour < 18) {
            timeGreeting = getString(R.string.greeting_noon);
        } else {
            timeGreeting = getString(R.string.greeting_evening);
        }

        tvGreeting.setText(timeGreeting + getString(R.string.greeting_suffix));
    }

    private void setupClickListeners() {
        btnRecord.setOnClickListener(v ->
                startActivity(new Intent(this, RecordMoodActivity.class)));

        btnStartChat.setOnClickListener(v ->
                startActivity(new Intent(this, AiChatActivity.class)));
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_footprint).setOnClickListener(v ->
                startActivity(new Intent(this, FootprintActivity.class)));

        findViewById(R.id.nav_mine).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupCapsuleList() {
        rvCapsules.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        capsuleAdapter = new CapsuleAdapter(new ArrayList<>());
        rvCapsules.setAdapter(capsuleAdapter);
    }

    private void loadDecompressionTips() {
        long userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            capsuleAdapter.updateItems(new ArrayList<>(FALLBACK_TIPS));
            return;
        }

        showLoading();
        RetrofitClient.getInstance().getApiService()
                .getDecompressionTips(userId)
                .enqueue(new Callback<ApiResponse<List<String>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<String>>> call,
                                           Response<ApiResponse<List<String>>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200
                                && response.body().getData() != null
                                && !response.body().getData().isEmpty()) {
                            capsuleAdapter.updateItems(response.body().getData());
                        } else {
                            capsuleAdapter.updateItems(new ArrayList<>(FALLBACK_TIPS));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        capsuleAdapter.updateItems(new ArrayList<>(FALLBACK_TIPS));
                        showNetworkError(t);
                    }
                });
    }

    private static class CapsuleAdapter extends RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder> {

        private final List<String> items;

        CapsuleAdapter(List<String> items) {
            this.items = new ArrayList<>(items);
        }

        void updateItems(List<String> newItems) {
            items.clear();
            if (newItems != null) {
                items.addAll(newItems);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CapsuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_capsule, parent, false);
            return new CapsuleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CapsuleViewHolder holder, int position) {
            holder.tvContent.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class CapsuleViewHolder extends RecyclerView.ViewHolder {
            final TextView tvContent;

            CapsuleViewHolder(@NonNull View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tv_capsule_content);
            }
        }
    }
}
