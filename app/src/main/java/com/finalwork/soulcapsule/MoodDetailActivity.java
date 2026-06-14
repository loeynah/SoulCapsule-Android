package com.finalwork.soulcapsule;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.ImageLoaderHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoodDetailActivity extends BaseActivity {

    public static final String EXTRA_MOOD_ID = "extra_mood_id";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_MOOD_STATUS = "extra_mood_status";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_FEELINGS = "extra_feelings";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_AI_REPLY = "extra_ai_reply";
    public static final String EXTRA_GOOD_MOOD = "extra_good_mood";
    public static final String EXTRA_SHOW_AI = "extra_show_ai";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    private long moodId;
    private String aiReplyText = "";
    private String moodStatus = "";
    private String reason = "";
    private String content = "";
    private String imageUrl = "";

    private TextView tvHeaderDate;
    private TextView tvTime;
    private TextView tvMoodTitle;
    private TextView tvContent;
    private TextView tvReasonTag;
    private TextView tvFeelingTag;
    private TextView tvAiReply;
    private LinearLayout layoutAiSection;
    private ImageView ivMoodEmoji;
    private ImageView ivDetailImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        bindData();
        setupActions();
    }

    private void bindViews() {
        tvHeaderDate = findViewById(R.id.tv_header_date);
        tvTime = findViewById(R.id.tv_time);
        tvMoodTitle = findViewById(R.id.tv_mood_title);
        tvContent = findViewById(R.id.tv_content);
        tvReasonTag = findViewById(R.id.tv_reason_tag);
        tvFeelingTag = findViewById(R.id.tv_feeling_tag);
        tvAiReply = findViewById(R.id.tv_ai_reply);
        layoutAiSection = findViewById(R.id.layout_ai_section);
        ivMoodEmoji = findViewById(R.id.iv_mood_emoji);
        ivDetailImage = findViewById(R.id.iv_detail_image);
    }

    private void bindData() {
        Intent intent = getIntent();
        moodId = intent.getLongExtra(EXTRA_MOOD_ID, 0L);

        String date = intent.getStringExtra(EXTRA_DATE);
        if (TextUtils.isEmpty(date)) {
            date = getString(R.string.detail_default_date);
        }

        String time = intent.getStringExtra(EXTRA_TIME);
        if (TextUtils.isEmpty(time)) {
            time = getString(R.string.detail_default_time);
        }

        moodStatus = intent.getStringExtra(EXTRA_MOOD_STATUS);
        if (TextUtils.isEmpty(moodStatus)) {
            moodStatus = getString(R.string.detail_default_mood);
        }

        reason = intent.getStringExtra(EXTRA_REASON);
        if (TextUtils.isEmpty(reason)) {
            reason = getString(R.string.detail_default_reason);
        }

        String feelings = intent.getStringExtra(EXTRA_FEELINGS);
        if (TextUtils.isEmpty(feelings)) {
            feelings = getString(R.string.detail_default_feeling);
        }

        content = intent.getStringExtra(EXTRA_CONTENT);
        if (TextUtils.isEmpty(content)) {
            content = getString(R.string.detail_default_content);
        }

        aiReplyText = intent.getStringExtra(EXTRA_AI_REPLY);
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);

        tvHeaderDate.setText(date);
        tvTime.setText(time);
        tvMoodTitle.setText(getString(R.string.detail_mood_title_prefix) + moodStatus);
        tvReasonTag.setText(reason);
        tvFeelingTag.setText(feelings);
        tvContent.setText(content);

        ivMoodEmoji.setImageResource(R.drawable.ic_mood_placeholder);

        if (!TextUtils.isEmpty(imageUrl)) {
            ivDetailImage.setVisibility(View.VISIBLE);
            ImageLoaderHelper.loadRemote(this, ivDetailImage, imageUrl, 12);
        } else {
            ivDetailImage.setVisibility(View.GONE);
            ivDetailImage.setImageDrawable(null);
        }

        if (!TextUtils.isEmpty(aiReplyText)) {
            layoutAiSection.setVisibility(View.VISIBLE);
            tvAiReply.setText(aiReplyText);
        } else {
            layoutAiSection.setVisibility(View.GONE);
        }
    }

    private void setupActions() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_ai).setOnClickListener(v -> {
            if (layoutAiSection.getVisibility() == View.VISIBLE) {
                layoutAiSection.requestFocus();
            }
            showToast(getString(R.string.detail_toast_ai_view));
        });

        findViewById(R.id.btn_more).setOnClickListener(this::showMoreMenu);

        findViewById(R.id.btn_like).setOnClickListener(v ->
                showToast(getString(R.string.detail_toast_liked)));

        findViewById(R.id.btn_dislike).setOnClickListener(v ->
                showToast(getString(R.string.detail_toast_disliked)));

        findViewById(R.id.btn_copy).setOnClickListener(v -> copyAiReply());

        findViewById(R.id.btn_chat_more).setOnClickListener(v ->
                startActivity(new Intent(this, AiChatActivity.class)));
    }

    private void showMoreMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(0, 1, 0, R.string.detail_menu_edit);
        popupMenu.getMenu().add(0, 2, 1, R.string.detail_menu_delete);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                startActivity(EditMoodActivity.createIntent(
                        this, moodId, moodStatus, reason, content, imageUrl));
                return true;
            }
            if (item.getItemId() == 2) {
                confirmDeleteMood();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void confirmDeleteMood() {
        if (moodId <= 0) {
            showToast("无效的心情记录");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.detail_delete_confirm_title)
                .setMessage(R.string.detail_delete_confirm_message)
                .setPositiveButton(R.string.detail_delete_confirm_ok, (dialog, which) -> deleteMoodRecord())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteMoodRecord() {
        showLoading();
        RetrofitClient.getInstance().getApiService()
                .deleteMood(moodId)
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
                            showToast(getString(R.string.detail_toast_delete));
                            finish();
                        } else {
                            String message = response.body() != null
                                    && response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : "删除失败，请稍后重试";
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

    private void copyAiReply() {
        if (TextUtils.isEmpty(aiReplyText)) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("ai_reply", aiReplyText));
            showToast(getString(R.string.detail_toast_copied));
        }
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }

    public static Intent createIntent(Context context, FootprintItem item) {
        Intent intent = new Intent(context, MoodDetailActivity.class);
        intent.putExtra(EXTRA_MOOD_ID, item.getMoodId());
        intent.putExtra(EXTRA_TIME, item.getTime());
        intent.putExtra(EXTRA_MOOD_STATUS, item.getMoodStatus());
        intent.putExtra(EXTRA_REASON, item.getReason());
        intent.putExtra(EXTRA_FEELINGS, item.getFeelings());
        intent.putExtra(EXTRA_CONTENT, item.getContent());
        intent.putExtra(EXTRA_IMAGE_URL, item.getImageUrl());
        intent.putExtra(EXTRA_GOOD_MOOD, item.isGoodMood());
        if (item.hasAiReply()) {
            intent.putExtra(EXTRA_AI_REPLY, item.getAiReply());
        }
        intent.putExtra(EXTRA_DATE, item.getFullDateLabel());
        return intent;
    }
}
