package com.finalwork.soulcapsule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.MoodResponse;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.MoodColorConstants;
import com.finalwork.soulcapsule.util.SessionManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FootprintActivity extends BaseActivity {

    private static final String[] WEEK_DAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final long SEARCH_DEBOUNCE_MS = 400L;

    private FootprintAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView rvFootprints;
    private EditText etSearch;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_footprint);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvFootprints = findViewById(R.id.rv_footprints);
        tvEmpty = findViewById(R.id.tv_empty);
        etSearch = findViewById(R.id.et_search);
        rvFootprints.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FootprintAdapter();
        adapter.setOnItemClickListener(item ->
                startActivity(MoodDetailActivity.createIntent(this, item)));
        rvFootprints.setAdapter(adapter);

        setupSearch();

        ImageView fabNewRecord = findViewById(R.id.fab_new_record);
        fabNewRecord.setOnClickListener(v ->
                startActivity(new Intent(this, RecordMoodActivity.class)));

        findViewById(R.id.nav_today).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_mine).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMoodList(currentKeyword);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void scheduleSearch(String keyword) {
        currentKeyword = keyword;
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        pendingSearchRunnable = () -> loadMoodList(keyword);
        searchHandler.postDelayed(pendingSearchRunnable, SEARCH_DEBOUNCE_MS);
    }

    private void loadMoodList(String keyword) {
        long userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            ToastHelper.show(this, "请先登录");
            updateEmptyState(true);
            return;
        }

        String queryKeyword = TextUtils.isEmpty(keyword) ? null : keyword;

        showLoading();
        RetrofitClient.getInstance().getApiService()
                .getMoodList(userId, queryKeyword)
                .enqueue(new Callback<ApiResponse<List<MoodResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MoodResponse>>> call,
                                           Response<ApiResponse<List<MoodResponse>>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200) {
                            List<MoodResponse> moodList = response.body().getData();
                            List<FootprintItem> items = convertToFootprintItems(moodList);
                            adapter.updateItems(items);
                            updateEmptyState(items.isEmpty());
                        } else {
                            ToastHelper.show(FootprintActivity.this, "拉取足迹失败，请检查网络");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MoodResponse>>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        showNetworkError(t);
                    }
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvFootprints.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private List<FootprintItem> convertToFootprintItems(List<MoodResponse> moodList) {
        List<FootprintItem> items = new ArrayList<>();
        if (moodList == null) {
            return items;
        }
        for (MoodResponse mood : moodList) {
            LocalDateTime dateTime = parseCreateTime(mood.getCreateTime());
            long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            String year = dateTime.getYear() + "年";
            String dateMonthDay = dateTime.getMonthValue() + "."
                    + String.format(Locale.CHINA, "%02d", dateTime.getDayOfMonth());
            String weekDay = WEEK_DAYS[dateTime.getDayOfWeek().getValue() - 1];
            String time = String.format(Locale.CHINA, "%02d:%02d",
                    dateTime.getHour(), dateTime.getMinute());

            String moodStatus = resolveMoodStatus(mood);
            int moodScore = MoodColorConstants.resolveScore(mood);
            boolean goodMood = moodScore >= MoodColorConstants.SCORE_GOOD;
            String tags = mood.getTags() != null ? mood.getTags() : "";
            String feelings = mood.getEmotions() != null ? mood.getEmotions() : "";
            String content = mood.getContent() != null ? mood.getContent() : "";
            String imageUrl = mood.getImageUrl() != null ? mood.getImageUrl() : "";
            String aiFeedback = mood.getAiFeedback() != null ? mood.getAiFeedback() : "";
            long moodId = mood.getId() != null ? mood.getId() : 0L;

            items.add(new FootprintItem(
                    moodId, year, dateMonthDay, weekDay, time,
                    timestamp, moodStatus, moodScore, goodMood, tags,
                    feelings, content, imageUrl, aiFeedback
            ));
        }
        return items;
    }

    private LocalDateTime parseCreateTime(String createTime) {
        if (createTime == null || createTime.isEmpty()) {
            return LocalDateTime.now();
        }
        String normalized = createTime.contains(".")
                ? createTime.substring(0, createTime.indexOf('.'))
                : createTime;
        if (normalized.length() > 19) {
            normalized = normalized.substring(0, 19);
        }
        return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private String resolveMoodStatus(MoodResponse mood) {
        if (mood.getEmotions() != null && !mood.getEmotions().isEmpty()) {
            return "心情" + mood.getEmotions();
        }
        if (mood.getScore() == null) {
            return "心情一般";
        }
        if (mood.getScore() >= 4) {
            return "心情好";
        }
        if (mood.getScore() == 3) {
            return "心情一般";
        }
        return "心情不好";
    }

    @Override
    protected void onDestroy() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        super.onDestroy();
    }
}
