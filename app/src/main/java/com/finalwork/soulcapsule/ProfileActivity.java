package com.finalwork.soulcapsule;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.MoodResponse;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.MoodColorConstants;
import com.finalwork.soulcapsule.util.MoodDateUtils;
import com.finalwork.soulcapsule.util.MoodStatsHelper;
import com.finalwork.soulcapsule.util.SessionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private int currentPeriod = MoodStatsHelper.PERIOD_WEEK;
    private int timeOffset = 0;

    private TextView tabWeek;
    private TextView tabMonth;
    private TextView tabYear;
    private TextView tvTimeRange;
    private TextView tvCalendarTitle;
    private TextView tvTrendHint;
    private LinearLayout calendarContainer;
    private LinearLayout moodDistContainer;
    private View trendOverlay;
    private MoodTrendView moodTrendView;
    private View attributionSection;
    private TextView tvUserId;
    private TextView tvStatsEmpty;

    private final TextView[] periodTabs = new TextView[3];
    private final List<MoodResponse> allMoods = new ArrayList<>();
    private boolean moodDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupPeriodTabs();
        setupTimeNavigator();
        setupBottomNav();
        setupLogout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserInfo();
        loadMoodList();
    }

    private void bindViews() {
        tabWeek = findViewById(R.id.tab_week);
        tabMonth = findViewById(R.id.tab_month);
        tabYear = findViewById(R.id.tab_year);
        periodTabs[0] = tabWeek;
        periodTabs[1] = tabMonth;
        periodTabs[2] = tabYear;

        tvTimeRange = findViewById(R.id.tv_time_range);
        tvCalendarTitle = findViewById(R.id.tv_calendar_title);
        tvTrendHint = findViewById(R.id.tv_trend_hint);
        calendarContainer = findViewById(R.id.calendar_container);
        moodDistContainer = findViewById(R.id.mood_dist_container);
        trendOverlay = findViewById(R.id.trend_overlay);
        moodTrendView = findViewById(R.id.mood_trend_view);
        attributionSection = findViewById(R.id.attribution_section);
        tvUserId = findViewById(R.id.tv_user_id);
        tvStatsEmpty = findViewById(R.id.tv_stats_empty);

        if (attributionSection != null) {
            attributionSection.setVisibility(View.GONE);
        }
    }

    private void refreshUserInfo() {
        String username = SessionManager.getUsername(this);
        if (username != null && !username.isEmpty()) {
            tvUserId.setText(username);
        } else {
            tvUserId.setText(R.string.profile_user_id);
        }
    }

    private void setupLogout() {
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SessionManager.clear(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupPeriodTabs() {
        tabWeek.setOnClickListener(v -> switchPeriod(MoodStatsHelper.PERIOD_WEEK));
        tabMonth.setOnClickListener(v -> switchPeriod(MoodStatsHelper.PERIOD_MONTH));
        tabYear.setOnClickListener(v -> switchPeriod(MoodStatsHelper.PERIOD_YEAR));

        findViewById(R.id.btn_view_all).setOnClickListener(v ->
                startActivity(new Intent(this, FootprintActivity.class)));

        findViewById(R.id.btn_go_record).setOnClickListener(v ->
                startActivity(new Intent(this, RecordMoodActivity.class)));
    }

    private void setupTimeNavigator() {
        findViewById(R.id.btn_time_prev).setOnClickListener(v -> {
            timeOffset--;
            refreshAll();
        });
        findViewById(R.id.btn_time_next).setOnClickListener(v -> {
            timeOffset++;
            refreshAll();
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_today).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_footprint).setOnClickListener(v -> {
            startActivity(new Intent(this, FootprintActivity.class));
            finish();
        });
    }

    private void switchPeriod(int period) {
        currentPeriod = period;
        timeOffset = 0;
        String label = period == MoodStatsHelper.PERIOD_WEEK
                ? getString(R.string.profile_period_week)
                : period == MoodStatsHelper.PERIOD_MONTH
                ? getString(R.string.profile_period_month)
                : getString(R.string.profile_period_year);
        showToast(getString(R.string.profile_toast_switch, label));
        refreshAll();
    }

    private void loadMoodList() {
        long userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            return;
        }

        showLoading();
        RetrofitClient.getInstance().getApiService()
                .getMoodList(userId, null)
                .enqueue(new Callback<ApiResponse<List<MoodResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MoodResponse>>> call,
                                           Response<ApiResponse<List<MoodResponse>>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        moodDataLoaded = true;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200) {
                            allMoods.clear();
                            List<MoodResponse> data = response.body().getData();
                            if (data != null) {
                                allMoods.addAll(data);
                            }
                            refreshAll();
                        } else {
                            ToastHelper.show(ProfileActivity.this, "拉取统计数据失败，请检查网络");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MoodResponse>>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        moodDataLoaded = true;
                        showNetworkError(t);
                    }
                });
    }

    private void refreshAll() {
        updatePeriodTabStyle();
        updateTimeRangeText();
        updateCalendarTitle();
        updateEmptyState();
        buildCalendar();
        buildMoodDistribution();
        updateTrendSection();
    }

    private void updateEmptyState() {
        if (tvStatsEmpty == null) {
            return;
        }
        if (!moodDataLoaded) {
            tvStatsEmpty.setVisibility(View.GONE);
            return;
        }
        List<MoodResponse> periodMoods = getCurrentPeriodMoods();
        tvStatsEmpty.setVisibility(periodMoods.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<MoodResponse> getCurrentPeriodMoods() {
        return MoodStatsHelper.filterByPeriod(allMoods, currentPeriod, timeOffset);
    }

    private void updatePeriodTabStyle() {
        for (int i = 0; i < periodTabs.length; i++) {
            boolean selected = i == currentPeriod;
            periodTabs[i].setBackgroundResource(
                    selected ? R.drawable.shape_period_tab_selected : 0
            );
            periodTabs[i].setTextColor(getColor(
                    selected ? R.color.text_primary : R.color.text_secondary
            ));
        }
    }

    private void updateTimeRangeText() {
        Calendar cal = MoodStatsHelper.getPeriodAnchorCalendar(currentPeriod, timeOffset);

        if (currentPeriod == MoodStatsHelper.PERIOD_WEEK) {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            int startMonth = cal.get(Calendar.MONTH) + 1;
            int startDay = cal.get(Calendar.DAY_OF_MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.add(Calendar.DAY_OF_MONTH, 6);
            int endMonth = cal.get(Calendar.MONTH) + 1;
            int endDay = cal.get(Calendar.DAY_OF_MONTH);
            int endYear = cal.get(Calendar.YEAR);
            tvTimeRange.setText(String.format(Locale.CHINA,
                    "%d.%02d.%02d - %d.%02d.%02d",
                    startYear, startMonth, startDay, endYear, endMonth, endDay));
        } else if (currentPeriod == MoodStatsHelper.PERIOD_MONTH) {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            tvTimeRange.setText(String.format(Locale.CHINA, "%d年%d月", year, month));
        } else {
            tvTimeRange.setText(String.format(Locale.CHINA, "%d年", cal.get(Calendar.YEAR)));
        }
    }

    private void updateCalendarTitle() {
        int titleRes = currentPeriod == MoodStatsHelper.PERIOD_WEEK
                ? R.string.profile_calendar_week
                : currentPeriod == MoodStatsHelper.PERIOD_MONTH
                ? R.string.profile_calendar_month
                : R.string.profile_calendar_year;
        tvCalendarTitle.setText(titleRes);
    }

    private void buildCalendar() {
        calendarContainer.removeAllViews();
        calendarContainer.setOrientation(
                currentPeriod == MoodStatsHelper.PERIOD_MONTH
                        || currentPeriod == MoodStatsHelper.PERIOD_YEAR
                        ? LinearLayout.VERTICAL
                        : LinearLayout.HORIZONTAL
        );

        if (currentPeriod == MoodStatsHelper.PERIOD_YEAR) {
            buildYearCalendar();
        } else if (currentPeriod == MoodStatsHelper.PERIOD_MONTH) {
            buildMonthCalendar();
        } else {
            buildWeekCalendar();
        }
    }

    private void buildWeekCalendar() {
        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        Calendar weekAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                MoodStatsHelper.PERIOD_WEEK, timeOffset);
        LocalDate weekStart = MoodDateUtils.getWeekStartLocalDate(weekAnchor);
        Map<String, List<MoodResponse>> dayMap = MoodDateUtils.groupByDay(getCurrentPeriodMoods());

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            View dayView = inflateCalendarDay(weekdays[i], String.valueOf(day.getDayOfMonth()));
            applyRatioRing(dayView, dayMap.get(MoodDateUtils.dayKey(day)), 3f);
            calendarContainer.addView(dayView);
        }
    }

    private void buildMonthCalendar() {
        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        for (String wd : weekdays) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tv.setGravity(Gravity.CENTER);
            tv.setText(wd);
            tv.setTextColor(getColor(R.color.text_secondary));
            tv.setTextSize(11f);
            header.addView(tv);
        }
        calendarContainer.addView(header);

        Calendar monthAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                MoodStatsHelper.PERIOD_MONTH, timeOffset);
        int year = monthAnchor.get(Calendar.YEAR);
        int month = monthAnchor.get(Calendar.MONTH);
        int daysInMonth = MoodDateUtils.getDaysInMonth(monthAnchor);

        Calendar cursor = (Calendar) monthAnchor.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        int leadingEmpty = MoodDateUtils.getMondayBasedColumnIndex(cursor);

        Map<String, List<MoodResponse>> dayMap = MoodDateUtils.groupByDay(getCurrentPeriodMoods());

        int day = 1;
        while (day <= daysInMonth) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                if ((day == 1 && col < leadingEmpty) || day > daysInMonth) {
                    View spacer = new View(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                    row.addView(spacer);
                } else {
                    LocalDate date = LocalDate.of(year, month + 1, day);
                    View dayView = inflateCalendarDay("", String.valueOf(day));
                    applyRatioRing(dayView, dayMap.get(MoodDateUtils.dayKey(date)), 2.5f);
                    TextView tvWeekday = dayView.findViewById(R.id.tv_weekday);
                    tvWeekday.setVisibility(View.GONE);
                    row.addView(dayView);
                    day++;
                }
            }
            calendarContainer.addView(row);
        }
    }

    private void buildYearCalendar() {
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"};
        Calendar yearAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                MoodStatsHelper.PERIOD_YEAR, timeOffset);
        int year = yearAnchor.get(Calendar.YEAR);
        Map<String, List<MoodResponse>> monthMap = MoodDateUtils.groupByMonth(getCurrentPeriodMoods());

        for (int row = 0; row < 2; row++) {
            LinearLayout monthRow = new LinearLayout(this);
            monthRow.setOrientation(LinearLayout.HORIZONTAL);
            monthRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 6; col++) {
                int index = row * 6 + col;
                String monthKey = String.format(Locale.CHINA, "%d-%02d", year, index + 1);
                View dayView = inflateCalendarDay(months[index], String.valueOf(index + 1));
                applyRatioRing(dayView, monthMap.get(monthKey), 3f);
                monthRow.addView(dayView);
            }
            calendarContainer.addView(monthRow);
        }
    }

    private void applyRatioRing(View dayView, List<MoodResponse> bucketMoods, float strokeDp) {
        MoodRatioRingView ring = dayView.findViewById(R.id.mood_ring);
        ring.setStrokeWidthDp(strokeDp);
        ring.setScoreRatios(MoodStatsHelper.buildScoreRatioMap(bucketMoods));
    }

    private View inflateCalendarDay(String weekday, String day) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_calendar_day, calendarContainer, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        view.setLayoutParams(lp);
        TextView tvWeekday = view.findViewById(R.id.tv_weekday);
        TextView tvDay = view.findViewById(R.id.tv_day);
        tvWeekday.setText(weekday);
        tvDay.setText(day);
        if (weekday.isEmpty()) {
            tvWeekday.setVisibility(View.GONE);
        }
        return view;
    }

    private void buildMoodDistribution() {
        moodDistContainer.removeAllViews();

        String[] labels = MoodColorConstants.getEmotionLabels();
        int[] percents = MoodStatsHelper.calculateDistributionPercents(getCurrentPeriodMoods());

        for (int i = 0; i < labels.length; i++) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_mood_dist_row, moodDistContainer, false);
            TextView tvLabel = row.findViewById(R.id.tv_mood_label);
            ProgressBar progressBar = row.findViewById(R.id.progress_mood);
            TextView tvPercent = row.findViewById(R.id.tv_mood_percent);

            int score = MoodColorConstants.scoreForDistributionIndex(i);
            int color = MoodColorConstants.getColorIntForScore(score);

            tvLabel.setText(labels[i]);
            progressBar.setProgress(percents[i]);
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
            tvPercent.setText(percents[i] + "%");

            moodDistContainer.addView(row);
        }
    }

    private void updateTrendSection() {
        int[] scaleColors = MoodColorConstants.getColorInts();
        moodTrendView.setMoodScaleColors(scaleColors);

        List<MoodResponse> periodMoods = getCurrentPeriodMoods();

        if (currentPeriod == MoodStatsHelper.PERIOD_WEEK) {
            Calendar weekAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                    MoodStatsHelper.PERIOD_WEEK, timeOffset);
            LocalDate weekStart = MoodDateUtils.getWeekStartLocalDate(weekAnchor);
            int recordedDays = MoodStatsHelper.countRecordedDaysInWeek(periodMoods);

            if (recordedDays < 3) {
                trendOverlay.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_go_record).setVisibility(View.VISIBLE);
                tvTrendHint.setText(getString(R.string.profile_trend_hint_week, 3 - recordedDays));
                moodTrendView.setVisibility(View.INVISIBLE);
            } else {
                trendOverlay.setVisibility(View.GONE);
                moodTrendView.setVisibility(View.VISIBLE);
                float[] points = MoodStatsHelper.buildWeekTrendPoints(periodMoods, weekStart);
                moodTrendView.setData(points, new String[]{"一", "二", "三", "四", "五", "六", "日"});
            }
        } else if (currentPeriod == MoodStatsHelper.PERIOD_MONTH) {
            Calendar monthAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                    MoodStatsHelper.PERIOD_MONTH, timeOffset);
            int year = monthAnchor.get(Calendar.YEAR);
            int month = monthAnchor.get(Calendar.MONTH) + 1;
            int daysInMonth = MoodDateUtils.getDaysInMonth(monthAnchor);

            trendOverlay.setVisibility(View.GONE);
            moodTrendView.setVisibility(View.VISIBLE);
            float[] monthPoints = MoodStatsHelper.buildMonthTrendPoints(
                    periodMoods, year, month, daysInMonth);
            moodTrendView.setDataWithSparseDays(monthPoints,
                    new int[]{1, 5, 10, 15, 20, 25, daysInMonth});
        } else {
            Calendar yearAnchor = MoodStatsHelper.getPeriodAnchorCalendar(
                    MoodStatsHelper.PERIOD_YEAR, timeOffset);
            int year = yearAnchor.get(Calendar.YEAR);

            trendOverlay.setVisibility(View.GONE);
            moodTrendView.setVisibility(View.VISIBLE);
            float[] yearPoints = MoodStatsHelper.buildYearTrendPoints(periodMoods, year);
            moodTrendView.setData(yearPoints,
                    new String[]{"1月", "2月", "3月", "4月", "5月", "6月",
                            "7月", "8月", "9月", "10月", "11月", "12月"});
        }
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }
}
