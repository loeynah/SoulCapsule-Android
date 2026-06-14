package com.finalwork.soulcapsule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView btnRecord;
    private TextView btnStartChat;
    private RecyclerView rvCapsules;

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
        List<String> capsuleData = Arrays.asList(
                "周末带上相机去街头散步，拍几组充满胶片感的摄影作品。",
                "花一个下午沉浸在手工拼装的世界里。"
        );

        rvCapsules.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCapsules.setAdapter(new CapsuleAdapter(capsuleData));
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }

    private static class CapsuleAdapter extends RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder> {

        private final List<String> items;

        CapsuleAdapter(List<String> items) {
            this.items = items;
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
