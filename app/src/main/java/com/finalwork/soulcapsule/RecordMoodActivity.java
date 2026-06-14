package com.finalwork.soulcapsule;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.MoodRequest;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.ImageLoaderHelper;
import com.finalwork.soulcapsule.util.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordMoodActivity extends BaseActivity {

    private static final int TOTAL_STEPS = 4;

    private int currentStep = 0;
    private int selectedMoodIndex = -1;
    private String selectedMoodText = "";

    private View progressFill;
    private FrameLayout progressContainer;

    private LinearLayout step1Layout;
    private LinearLayout step2Layout;
    private LinearLayout step3Layout;
    private RelativeLayout step4Layout;

    private TextView tvStep1Title;
    private TextView tvStep2Title;
    private TextView tvStep3Title;
    private TextView tvStep4Title;

    private TextView[] moodOptions;
    private RecyclerView rvAttribution;
    private RecyclerView rvEmotions;
    private EditText etSearchEmotion;
    private EditText etDetail;
    private ImageView ivUploadPreview;

    private String pendingImageUrl;
    private boolean isUploadingImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    private AttributionAdapter attributionAdapter;
    private EmotionAdapter emotionAdapter;

    private final Set<Integer> selectedAttributionPositions = new HashSet<>();
    private final Set<Integer> selectedEmotionPositions = new HashSet<>();

    private final String[] moodLabels = new String[5];
    private final List<String> attributionLabels = new ArrayList<>();
    private final List<String> allEmotionLabels = new ArrayList<>();
    private final List<String> filteredEmotionLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setBackgroundDrawableResource(R.color.bg_warm_white);
        setContentView(R.layout.activity_record_mood);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initData();
        bindViews();
        registerPickImageLauncher();
        setupHeader();
        setupStep1();
        setupStep2();
        setupStep3();
        setupStep4();
        setupNavigation();
        applyTitleHighlights();
        showStep(0);
    }

    private void initData() {
        moodLabels[0] = getString(R.string.record_mood_very_good);
        moodLabels[1] = getString(R.string.record_mood_good);
        moodLabels[2] = getString(R.string.record_mood_normal);
        moodLabels[3] = getString(R.string.record_mood_bad);
        moodLabels[4] = getString(R.string.record_mood_very_bad);

        String[] attributions = {
                "工作", "学习", "伴侣", "家人", "朋友", "运动",
                "阅读", "购物", "旅行", "食物", "音乐", "户外",
                getString(R.string.record_custom_tag), "宠物"
        };
        attributionLabels.clear();
        for (String item : attributions) {
            attributionLabels.add(item);
        }

        String[] emotions = {
                "开心", "兴奋", "惊喜", "期待", "自豪", "得意",
                "平静", "温暖", "幸福", "甜蜜", "信任", "满意",
                "有成就感", "充实", "充满感激", "自信", "感动", "焦虑",
                getString(R.string.record_custom_tag)
        };
        allEmotionLabels.clear();
        filteredEmotionLabels.clear();
        for (String item : emotions) {
            allEmotionLabels.add(item);
            filteredEmotionLabels.add(item);
        }
    }

    private void bindViews() {
        progressContainer = findViewById(R.id.progress_container);
        progressFill = findViewById(R.id.progress_fill);

        step1Layout = findViewById(R.id.step1_layout);
        step2Layout = findViewById(R.id.step2_layout);
        step3Layout = findViewById(R.id.step3_layout);
        step4Layout = findViewById(R.id.step4_layout);

        tvStep1Title = findViewById(R.id.tv_step1_title);
        tvStep2Title = findViewById(R.id.tv_step2_title);
        tvStep3Title = findViewById(R.id.tv_step3_title);
        tvStep4Title = findViewById(R.id.tv_step4_title);

        moodOptions = new TextView[]{
                findViewById(R.id.mood_option_0),
                findViewById(R.id.mood_option_1),
                findViewById(R.id.mood_option_2),
                findViewById(R.id.mood_option_3),
                findViewById(R.id.mood_option_4)
        };

        rvAttribution = findViewById(R.id.rv_attribution);
        rvEmotions = findViewById(R.id.rv_emotions);
        etSearchEmotion = findViewById(R.id.et_search_emotion);
        etDetail = findViewById(R.id.et_detail);
        ivUploadPreview = findViewById(R.id.iv_upload_preview);
    }

    private void registerPickImageLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        uploadImageFromUri(uri);
                    }
                });
    }

    private void setupHeader() {
        TextView tvHeaderDate = findViewById(R.id.tv_header_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        Date now = new Date();
        tvHeaderDate.setText(dateFormat.format(now) + " 今天 " + timeFormat.format(now));
    }

    private void setupStep1() {
        for (int i = 0; i < moodOptions.length; i++) {
            final int index = i;
            moodOptions[i].setOnClickListener(v -> selectMood(index));
        }
        findViewById(R.id.btn_step1_next).setOnClickListener(v -> {
            if (selectedMoodIndex < 0) {
                showToast(getString(R.string.record_toast_select_mood));
                return;
            }
            goToNextStep();
        });
    }

    private void setupStep2() {
        attributionAdapter = new AttributionAdapter();
        rvAttribution.setLayoutManager(new GridLayoutManager(this, 3));
        rvAttribution.setAdapter(attributionAdapter);

        findViewById(R.id.btn_step2_next).setOnClickListener(v -> goToNextStep());
    }

    private void setupStep3() {
        emotionAdapter = new EmotionAdapter();
        rvEmotions.setLayoutManager(new GridLayoutManager(this, 3));
        rvEmotions.setAdapter(emotionAdapter);

        etSearchEmotion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmotions(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btn_step3_next).setOnClickListener(v -> goToNextStep());
        findViewById(R.id.btn_step3_skip).setOnClickListener(v -> goToNextStep());
    }

    private void setupStep4() {
        etDetail.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        findViewById(R.id.btn_gallery).setOnClickListener(v -> openImagePicker());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveMoodRecord());
    }

    private void openImagePicker() {
        if (isUploadingImage) {
            showToast(getString(R.string.record_uploading));
            return;
        }
        pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadImageFromUri(Uri uri) {
        isUploadingImage = true;
        pendingImageUrl = null;
        showLoading();
        ivUploadPreview.setVisibility(View.VISIBLE);
        ImageLoaderHelper.loadLocal(this, ivUploadPreview, uri, 8);

        File tempFile = null;
        try {
            tempFile = copyUriToTempFile(uri);
            String mimeType = getContentResolver().getType(uri);
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "image/jpeg";
            }
            RequestBody requestBody = RequestBody.create(tempFile, MediaType.parse(mimeType));
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file", tempFile.getName(), requestBody);
            File finalTempFile = tempFile;
            RetrofitClient.getInstance().getApiService()
                    .uploadImage(part)
                    .enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call,
                                               Response<ApiResponse<String>> response) {
                            isUploadingImage = false;
                            hideLoading();
                            deleteTempFile(finalTempFile);
                            if (!isActivityAlive()) {
                                return;
                            }
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getCode() == 200
                                    && !TextUtils.isEmpty(response.body().getData())) {
                                pendingImageUrl = response.body().getData();
                                ImageLoaderHelper.loadRemote(
                                        RecordMoodActivity.this,
                                        ivUploadPreview,
                                        pendingImageUrl,
                                        8);
                                showToast(getString(R.string.record_upload_success));
                            } else {
                                pendingImageUrl = null;
                                ivUploadPreview.setVisibility(View.GONE);
                                String message = response.body() != null
                                        && response.body().getMessage() != null
                                        ? response.body().getMessage()
                                        : getString(R.string.record_upload_failed);
                                showToast(message);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            isUploadingImage = false;
                            hideLoading();
                            deleteTempFile(finalTempFile);
                            if (!isActivityAlive()) {
                                return;
                            }
                            pendingImageUrl = null;
                            ivUploadPreview.setVisibility(View.GONE);
                            showNetworkError(t);
                        }
                    });
        } catch (IOException e) {
            isUploadingImage = false;
            hideLoading();
            deleteTempFile(tempFile);
            pendingImageUrl = null;
            ivUploadPreview.setVisibility(View.GONE);
            showToast(getString(R.string.record_upload_failed));
        }
    }

    private File copyUriToTempFile(Uri uri) throws IOException {
        String mimeType = getContentResolver().getType(uri);
        String extension = "jpg";
        if (!TextUtils.isEmpty(mimeType) && mimeType.contains("/")) {
            extension = mimeType.substring(mimeType.indexOf('/') + 1);
            if ("jpeg".equals(extension)) {
                extension = "jpg";
            }
        }
        File tempFile = File.createTempFile("mood_upload_", "." + extension, getCacheDir());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream == null) {
                throw new IOException("无法读取图片");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        return tempFile;
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private String buildSelectedTags() {
        List<String> tags = new ArrayList<>();
        for (Integer pos : selectedAttributionPositions) {
            if (pos >= 0 && pos < attributionLabels.size()) {
                tags.add(attributionLabels.get(pos));
            }
        }
        for (Integer pos : selectedEmotionPositions) {
            if (pos >= 0 && pos < filteredEmotionLabels.size()) {
                tags.add(filteredEmotionLabels.get(pos));
            }
        }
        return TextUtils.join(",", tags);
    }

    private void saveMoodRecord() {
        if (isUploadingImage) {
            showToast(getString(R.string.record_uploading));
            return;
        }
        String content = etDetail.getText().toString().trim();
        if (content.isEmpty()) {
            showToast("请先写下你的感受");
            return;
        }
        if (selectedMoodIndex < 0 || selectedMoodText.isEmpty()) {
            showToast(getString(R.string.record_toast_select_mood));
            return;
        }

        long userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            showToast("请先登录后再记录心情");
            return;
        }

        MoodRequest request = new MoodRequest(userId, selectedMoodText, content, pendingImageUrl);
        request.setTags(buildSelectedTags());
        showLoading();
        RetrofitClient.getInstance().getApiService()
                .addMoodRecord(request)
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
                            showToast("心情已珍藏入胶囊！");
                            finish();
                        } else {
                            showToast("网络连接失败，请检查后端状态");
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

    private void setupNavigation() {
        findViewById(R.id.btn_back).setOnClickListener(v -> goToPreviousStep());
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    private void selectMood(int index) {
        selectedMoodIndex = index;
        selectedMoodText = moodLabels[index];
        for (int i = 0; i < moodOptions.length; i++) {
            moodOptions[i].setSelected(i == index);
        }
        updateStep2Title();
    }

    private void updateStep2Title() {
        String mood = selectedMoodText.isEmpty() ? moodLabels[0] : selectedMoodText;
        String fullText = getString(R.string.record_step2_title_prefix)
                + mood
                + getString(R.string.record_step2_title_suffix);
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(mood);
        if (start >= 0) {
            spannable.setSpan(
                    new BackgroundColorSpan(getColor(R.color.capsule_bg)),
                    start,
                    start + mood.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        tvStep2Title.setText(spannable);
    }

    private void applyTitleHighlights() {
        highlightKeyword(tvStep1Title, getString(R.string.record_step1_title), "心情");
        highlightKeyword(tvStep3Title, getString(R.string.record_step3_title), "情绪");
        highlightKeyword(tvStep4Title, getString(R.string.record_step4_title), "感受");
        updateStep2Title();
    }

    private void highlightKeyword(TextView textView, String fullText, String keyword) {
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(keyword);
        if (start >= 0) {
            spannable.setSpan(
                    new BackgroundColorSpan(getColor(R.color.capsule_bg)),
                    start,
                    start + keyword.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        textView.setText(spannable);
    }

    private void showStep(int step) {
        currentStep = step;

        step1Layout.setVisibility(step == 0 ? View.VISIBLE : View.GONE);
        step2Layout.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        step3Layout.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        step4Layout.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        View activeStep;
        switch (step) {
            case 1:
                activeStep = step2Layout;
                break;
            case 2:
                activeStep = step3Layout;
                break;
            case 3:
                activeStep = step4Layout;
                break;
            default:
                activeStep = step1Layout;
                break;
        }
        activeStep.bringToFront();

        updateProgressBar();
    }

    private void updateProgressBar() {
        progressContainer.post(() -> {
            int totalWidth = progressContainer.getWidth();
            float ratio = (currentStep + 1f) / TOTAL_STEPS;
            ViewGroup.LayoutParams params = progressFill.getLayoutParams();
            params.width = (int) (totalWidth * ratio);
            progressFill.setLayoutParams(params);
        });
    }

    private void goToNextStep() {
        if (currentStep < TOTAL_STEPS - 1) {
            showStep(currentStep + 1);
        }
    }

    private void goToPreviousStep() {
        if (currentStep > 0) {
            showStep(currentStep - 1);
        } else {
            finish();
        }
    }

    private void filterEmotions(String keyword) {
        filteredEmotionLabels.clear();
        if (keyword.isEmpty()) {
            filteredEmotionLabels.addAll(allEmotionLabels);
        } else {
            for (String emotion : allEmotionLabels) {
                if (emotion.contains(keyword)) {
                    filteredEmotionLabels.add(emotion);
                }
            }
        }
        selectedEmotionPositions.clear();
        emotionAdapter.notifyDataSetChanged();
    }

    private void showCustomTagDialog(boolean isAttribution) {
        EditText input = new EditText(this);
        input.setHint(R.string.record_dialog_custom_hint);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.record_dialog_custom_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        return;
                    }
                    if (isAttribution) {
                        int insertIndex = attributionLabels.size() - 1;
                        attributionLabels.add(insertIndex, text);
                        attributionAdapter.notifyItemInserted(insertIndex);
                    } else {
                        int insertIndex = allEmotionLabels.size() - 1;
                        allEmotionLabels.add(insertIndex, text);
                        filterEmotions(etSearchEmotion.getText().toString().trim());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }

    private class AttributionAdapter extends RecyclerView.Adapter<AttributionAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attribution_tag, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String label = attributionLabels.get(position);
            holder.tvLabel.setText(label);
            holder.itemView.setSelected(selectedAttributionPositions.contains(position));

            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                if (getString(R.string.record_custom_tag).equals(attributionLabels.get(pos))) {
                    showCustomTagDialog(true);
                    return;
                }
                if (selectedAttributionPositions.contains(pos)) {
                    selectedAttributionPositions.remove(pos);
                    holder.itemView.setSelected(false);
                } else {
                    selectedAttributionPositions.add(pos);
                    holder.itemView.setSelected(true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return attributionLabels.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvLabel;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tv_tag_label);
            }
        }
    }

    private class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emotion_tag, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String label = filteredEmotionLabels.get(position);
            holder.tvLabel.setText(label);
            holder.tvLabel.setSelected(selectedEmotionPositions.contains(position));

            holder.tvLabel.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                if (getString(R.string.record_custom_tag).equals(filteredEmotionLabels.get(pos))) {
                    showCustomTagDialog(false);
                    return;
                }
                if (selectedEmotionPositions.contains(pos)) {
                    selectedEmotionPositions.remove(pos);
                    holder.tvLabel.setSelected(false);
                } else {
                    selectedEmotionPositions.add(pos);
                    holder.tvLabel.setSelected(true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredEmotionLabels.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvLabel;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tv_emotion_label);
            }
        }
    }
}
