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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final int PERIOD_WEEK = 0;
    private static final int PERIOD_MONTH = 1;
    private static final int PERIOD_YEAR = 2;

    private int currentPeriod = PERIOD_WEEK;
    private int timeOffset = 0;

    private TextView tabWeek;
    private TextView tabMonth;
    private TextView tabYear;
    private TextView tvTimeRange;
    private TextView tvCalendarTitle;
    private TextView tvTrendHint;
    private LinearLayout calendarContainer;
    private LinearLayout moodDistContainer;
    private LinearLayout containerHappy;
    private LinearLayout containerSad;
    private View trendOverlay;
    private MoodTrendView moodTrendView;

    private final TextView[] periodTabs = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        refreshAll();
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
        containerHappy = findViewById(R.id.container_happy);
        containerSad = findViewById(R.id.container_sad);
        trendOverlay = findViewById(R.id.trend_overlay);
        moodTrendView = findViewById(R.id.mood_trend_view);

        TextView tvUserId = findViewById(R.id.tv_user_id);
        tvUserId.setText(R.string.profile_user_id);
    }

    private void setupPeriodTabs() {
        tabWeek.setOnClickListener(v -> switchPeriod(PERIOD_WEEK));
        tabMonth.setOnClickListener(v -> switchPeriod(PERIOD_MONTH));
        tabYear.setOnClickListener(v -> switchPeriod(PERIOD_YEAR));

        findViewById(R.id.btn_view_all).setOnClickListener(v ->
                showToast(getString(R.string.profile_toast_view_all)));

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
        String label = period == PERIOD_WEEK
                ? getString(R.string.profile_period_week)
                : period == PERIOD_MONTH
                ? getString(R.string.profile_period_month)
                : getString(R.string.profile_period_year);
        showToast(getString(R.string.profile_toast_switch, label));
        refreshAll();
    }

    private void refreshAll() {
        updatePeriodTabStyle();
        updateTimeRangeText();
        updateCalendarTitle();
        buildCalendar();
        buildMoodDistribution();
        updateTrendSection();
        buildAttributionChips();
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
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, timeOffset);

        if (currentPeriod == PERIOD_WEEK) {
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
        } else if (currentPeriod == PERIOD_MONTH) {
            cal.add(Calendar.MONTH, timeOffset);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            tvTimeRange.setText(String.format(Locale.CHINA, "%d年%d月", year, month));
        } else {
            cal.add(Calendar.YEAR, timeOffset);
            tvTimeRange.setText(String.format(Locale.CHINA, "%d年", cal.get(Calendar.YEAR)));
        }
    }

    private void updateCalendarTitle() {
        int titleRes = currentPeriod == PERIOD_WEEK
                ? R.string.profile_calendar_week
                : currentPeriod == PERIOD_MONTH
                ? R.string.profile_calendar_month
                : R.string.profile_calendar_year;
        tvCalendarTitle.setText(titleRes);
    }

    private void buildCalendar() {
        calendarContainer.removeAllViews();
        calendarContainer.setOrientation(
                currentPeriod == PERIOD_MONTH || currentPeriod == PERIOD_YEAR
                        ? LinearLayout.VERTICAL
                        : LinearLayout.HORIZONTAL
        );

        if (currentPeriod == PERIOD_YEAR) {
            buildYearCalendar();
        } else if (currentPeriod == PERIOD_MONTH) {
            buildMonthCalendar();
        } else {
            buildWeekCalendar();
        }
    }

    private boolean[] getWeekRecordedFlags() {
        return new boolean[]{true, true, false, false, false, false, false};
    }

    private int countWeekRecordedDays() {
        int count = 0;
        for (boolean recorded : getWeekRecordedFlags()) {
            if (recorded) {
                count++;
            }
        }
        return count;
    }

    private void buildWeekCalendar() {
        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        int[] days = {8, 9, 10, 11, 12, 13, 14};
        int todayIndex = 2;

        boolean[] recordedFlags = getWeekRecordedFlags();
        for (int i = 0; i < 7; i++) {
            View dayView = inflateCalendarDay(weekdays[i], String.valueOf(days[i]));
            MoodRingView ring = dayView.findViewById(R.id.mood_ring);
            ring.setStrokeWidthDp(3f);

            if (i == todayIndex) {
                ring.setSegments(
                        new int[]{getColor(R.color.mood_good), getColor(R.color.mood_normal)},
                        new float[]{0.5f, 0.5f}
                );
            } else if (recordedFlags[i]) {
                int color = i % 2 == 0
                        ? getColor(R.color.mood_good)
                        : getColor(R.color.mood_normal);
                ring.setSegments(new int[]{color}, new float[]{1f});
            } else {
                ring.setSegments(null, null);
            }

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

        int[][] monthData = {
                {0, 0, 0, 1, 2, 3, 4},
                {5, 6, 7, 8, 9, 10, 11},
                {12, 13, 14, 15, 16, 17, 18},
                {19, 20, 21, 22, 23, 24, 25},
                {26, 27, 28, 29, 30, 0, 0}
        };
        int today = 10;

        for (int[] week : monthData) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            for (int day : week) {
                if (day == 0) {
                    View spacer = new View(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                    row.addView(spacer);
                } else {
                    View dayView = inflateCalendarDay("", String.valueOf(day));
                    MoodRingView ring = dayView.findViewById(R.id.mood_ring);
                    ring.setStrokeWidthDp(2.5f);
                    TextView tvWeekday = dayView.findViewById(R.id.tv_weekday);
                    tvWeekday.setVisibility(View.GONE);

                    if (day == today) {
                        ring.setSegments(
                                new int[]{getColor(R.color.mood_good), getColor(R.color.mood_normal)},
                                new float[]{0.5f, 0.5f}
                        );
                    } else if (day % 3 == 0) {
                        ring.setSegments(new int[]{getColor(R.color.mood_good)}, new float[]{1f});
                    } else if (day % 3 == 1) {
                        ring.setSegments(new int[]{getColor(R.color.mood_normal)}, new float[]{1f});
                    }
                    row.addView(dayView);
                }
            }
            calendarContainer.addView(row);
        }
    }

    private void buildYearCalendar() {
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"};

        for (int row = 0; row < 2; row++) {
            LinearLayout monthRow = new LinearLayout(this);
            monthRow.setOrientation(LinearLayout.HORIZONTAL);
            monthRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 6; col++) {
                int index = row * 6 + col;
                View dayView = inflateCalendarDay(months[index], String.valueOf(index + 1));
                MoodRingView ring = dayView.findViewById(R.id.mood_ring);
                ring.setStrokeWidthDp(3f);

                if (index == 5) {
                    ring.setSegments(
                            new int[]{
                                    getColor(R.color.mood_normal),
                                    getColor(R.color.mood_good)
                            },
                            new float[]{0.75f, 0.25f}
                    );
                } else {
                    ring.setSegments(null, null);
                }

                monthRow.addView(dayView);
            }
            calendarContainer.addView(monthRow);
        }
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

        String[] labels = {
                getString(R.string.record_mood_very_good),
                getString(R.string.record_mood_good),
                getString(R.string.record_mood_normal),
                getString(R.string.record_mood_bad),
                getString(R.string.record_mood_very_bad)
        };
        int[] colors = {
                R.color.mood_very_good,
                R.color.mood_good,
                R.color.mood_normal,
                R.color.mood_bad,
                R.color.mood_very_bad
        };
        int[] percents = currentPeriod == PERIOD_YEAR
                ? new int[]{15, 30, 28, 17, 10}
                : currentPeriod == PERIOD_MONTH
                ? new int[]{18, 32, 26, 14, 10}
                : new int[]{20, 35, 25, 12, 8};

        for (int i = 0; i < labels.length; i++) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_mood_dist_row, moodDistContainer, false);
            TextView tvLabel = row.findViewById(R.id.tv_mood_label);
            ProgressBar progressBar = row.findViewById(R.id.progress_mood);
            TextView tvPercent = row.findViewById(R.id.tv_mood_percent);

            tvLabel.setText(labels[i]);
            progressBar.setProgress(percents[i]);
            progressBar.setProgressTintList(ColorStateList.valueOf(getColor(colors[i])));
            tvPercent.setText(percents[i] + "%");

            moodDistContainer.addView(row);
        }
    }

    private void updateTrendSection() {
        applyMoodScaleColors();

        if (currentPeriod == PERIOD_WEEK) {
            int recordedDays = countWeekRecordedDays();
            if (recordedDays < 3) {
                trendOverlay.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_go_record).setVisibility(View.VISIBLE);
                tvTrendHint.setText(getString(R.string.profile_trend_hint_week, 3 - recordedDays));
                moodTrendView.setVisibility(View.INVISIBLE);
            } else {
                trendOverlay.setVisibility(View.GONE);
                moodTrendView.setVisibility(View.VISIBLE);
                moodTrendView.setData(
                        new float[]{3.2f, 3.8f, 4.1f, 3.5f, 4.0f, 4.3f, 4.5f},
                        new String[]{"一", "二", "三", "四", "五", "六", "日"}
                );
            }
        } else if (currentPeriod == PERIOD_MONTH) {
            trendOverlay.setVisibility(View.GONE);
            moodTrendView.setVisibility(View.VISIBLE);
            float[] monthPoints = buildMonthTrendPoints(30);
            moodTrendView.setDataWithSparseDays(monthPoints, new int[]{1, 5, 10, 15, 20, 25, 30});
        } else {
            trendOverlay.setVisibility(View.GONE);
            moodTrendView.setVisibility(View.VISIBLE);
            moodTrendView.setData(
                    new float[]{3.5f, 3.8f, 4.0f, 3.6f, 4.2f, 4.5f, 4.1f, 3.9f, 4.3f, 4.6f, 4.4f, 4.7f},
                    new String[]{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"}
            );
        }
    }

    private void applyMoodScaleColors() {
        moodTrendView.setMoodScaleColors(new int[]{
                getColor(R.color.mood_very_good),
                getColor(R.color.mood_good),
                getColor(R.color.mood_normal),
                getColor(R.color.mood_bad),
                getColor(R.color.mood_very_bad)
        });
    }

    private float[] buildMonthTrendPoints(int days) {
        float[] points = new float[days];
        for (int i = 0; i < days; i++) {
            points[i] = 3.0f + (float) (Math.sin(i * 0.35) * 0.8 + Math.cos(i * 0.15) * 0.4);
        }
        return points;
    }

    private void buildAttributionChips() {
        fillChipRow(containerHappy, new String[]{"家人", "天气", "食物", "运动"});
        fillChipRow(containerSad, new String[]{"工作", "学习"});
    }

    private void fillChipRow(LinearLayout container, String[] labels) {
        container.removeAllViews();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(row);

        for (String label : labels) {
            View chip = LayoutInflater.from(this).inflate(R.layout.item_attribution_chip, row, false);
            TextView tvLabel = chip.findViewById(R.id.tv_chip_label);
            tvLabel.setText(label);
            row.addView(chip);
        }
    }

    private void showToast(String message) {
        ToastHelper.show(this, message);
    }
}
