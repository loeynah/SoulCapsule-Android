package com.finalwork.soulcapsule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.MoodRequest;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.MoodColorConstants;
import com.finalwork.soulcapsule.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditMoodActivity extends BaseActivity {

    public static final String EXTRA_MOOD_ID = "extra_mood_id";
    public static final String EXTRA_MOOD_STATUS = "extra_mood_status";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    private long moodId;
    private String imageUrl;

    private RadioGroup rgMood;
    private EditText etTags;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_mood);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rgMood = findViewById(R.id.rg_mood);
        etTags = findViewById(R.id.et_tags);
        etContent = findViewById(R.id.et_content);
        etContent.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        bindInitialData();

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_done).setOnClickListener(v -> saveMoodChanges());
    }

    private void bindInitialData() {
        Intent intent = getIntent();
        moodId = intent.getLongExtra(EXTRA_MOOD_ID, 0L);
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);

        String moodStatus = intent.getStringExtra(EXTRA_MOOD_STATUS);
        selectMoodByStatus(moodStatus);

        String reason = intent.getStringExtra(EXTRA_REASON);
        if (!TextUtils.isEmpty(reason)) {
            etTags.setText(reason);
        }

        String content = intent.getStringExtra(EXTRA_CONTENT);
        if (!TextUtils.isEmpty(content)) {
            etContent.setText(content);
        }
    }

    private void saveMoodChanges() {
        if (moodId <= 0) {
            showToast("无效的心情记录");
            return;
        }

        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            showToast("请先填写心情内容");
            return;
        }

        String moodType = getSelectedMoodType();
        String tags = etTags.getText().toString().trim();

        MoodRequest request = new MoodRequest();
        request.setId(moodId);
        request.setUserId(SessionManager.getUserId(this));
        request.setMoodType(moodType);
        request.setContent(content);
        request.setTags(tags);
        request.setImageUrl(imageUrl);

        showLoading();
        RetrofitClient.getInstance().getApiService()
                .updateMood(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200) {
                            showToast(getString(R.string.edit_mood_toast_success));
                            finish();
                        } else {
                            String message = response.body() != null
                                    && response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : "修改失败，请稍后重试";
                            showToast(message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        showNetworkError(t);
                    }
                });
    }

    private String getSelectedMoodType() {
        int checkedId = rgMood.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_mood_very_good) {
            return MoodColorConstants.EMOTION_VERY_GOOD;
        }
        if (checkedId == R.id.rb_mood_good) {
            return MoodColorConstants.EMOTION_GOOD;
        }
        if (checkedId == R.id.rb_mood_bad) {
            return MoodColorConstants.EMOTION_BAD;
        }
        if (checkedId == R.id.rb_mood_very_bad) {
            return MoodColorConstants.EMOTION_VERY_BAD;
        }
        return MoodColorConstants.EMOTION_NORMAL;
    }

    private void selectMoodByStatus(String moodStatus) {
        int radioId = R.id.rb_mood_normal;
        if (!TextUtils.isEmpty(moodStatus)) {
            if (moodStatus.contains("很不好")) {
                radioId = R.id.rb_mood_very_bad;
            } else if (moodStatus.contains("不好")) {
                radioId = R.id.rb_mood_bad;
            } else if (moodStatus.contains("很好")) {
                radioId = R.id.rb_mood_very_good;
            } else if (moodStatus.contains("好")) {
                radioId = R.id.rb_mood_good;
            }
        }
        rgMood.check(radioId);
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }

    public static Intent createIntent(Context context, long moodId, String moodStatus,
                                      String tags, String content, String imageUrl) {
        Intent intent = new Intent(context, EditMoodActivity.class);
        intent.putExtra(EXTRA_MOOD_ID, moodId);
        intent.putExtra(EXTRA_MOOD_STATUS, moodStatus);
        intent.putExtra(EXTRA_REASON, tags);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        return intent;
    }

    public static Intent createIntent(Context context, FootprintItem item) {
        return createIntent(
                context,
                item.getMoodId(),
                item.getMoodStatus(),
                item.getReason(),
                item.getContent(),
                item.getImageUrl()
        );
    }
}
