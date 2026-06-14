package com.finalwork.soulcapsule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditMoodActivity extends AppCompatActivity {

    public static final String EXTRA_MOOD_STATUS = "extra_mood_status";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_CONTENT = "extra_content";

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

        bindInitialData();

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_done).setOnClickListener(v -> {
            showToast(getString(R.string.edit_mood_toast_success));
            finish();
        });
    }

    private void bindInitialData() {
        Intent intent = getIntent();

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

    public static Intent createIntent(Context context, String moodStatus, String reason, String content) {
        Intent intent = new Intent(context, EditMoodActivity.class);
        intent.putExtra(EXTRA_MOOD_STATUS, moodStatus);
        intent.putExtra(EXTRA_REASON, reason);
        intent.putExtra(EXTRA_CONTENT, content);
        return intent;
    }

    public static Intent createIntent(Context context, FootprintItem item) {
        return createIntent(context, item.getMoodStatus(), item.getReason(), item.getContent());
    }
}
