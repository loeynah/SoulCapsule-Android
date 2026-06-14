package com.finalwork.soulcapsule.util;

import com.finalwork.soulcapsule.dto.MoodResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 心情统计数据处理工具。
 */
public final class MoodStatsHelper {

    public static final int PERIOD_WEEK = 0;
    public static final int PERIOD_MONTH = 1;
    public static final int PERIOD_YEAR = 2;

    private MoodStatsHelper() {
    }

    public static List<MoodResponse> filterByPeriod(List<MoodResponse> source,
                                                    int period,
                                                    int timeOffset) {
        List<MoodResponse> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (MoodResponse mood : source) {
            if (isInPeriod(mood, period, timeOffset)) {
                result.add(mood);
            }
        }
        return result;
    }

    public static boolean isInPeriod(MoodResponse mood, int period, int timeOffset) {
        LocalDate date = MoodDateUtils.toLocalDate(mood);
        java.util.Calendar anchor = java.util.Calendar.getInstance(java.util.Locale.CHINA);
        MoodDateUtils.applyPeriodOffset(anchor, period, timeOffset);

        if (period == PERIOD_WEEK) {
            LocalDate weekStart = MoodDateUtils.getWeekStartLocalDate(anchor);
            LocalDate weekEnd = weekStart.plusDays(6);
            return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
        }
        if (period == PERIOD_MONTH) {
            int year = anchor.get(java.util.Calendar.YEAR);
            int month = anchor.get(java.util.Calendar.MONTH) + 1;
            return date.getYear() == year && date.getMonthValue() == month;
        }
        return date.getYear() == anchor.get(java.util.Calendar.YEAR);
    }

    public static java.util.Calendar getPeriodAnchorCalendar(int period, int timeOffset) {
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.Locale.CHINA);
        MoodDateUtils.applyPeriodOffset(cal, period, timeOffset);
        return cal;
    }

    public static Map<String, List<MoodResponse>> groupByDay(List<MoodResponse> moods) {
        return MoodDateUtils.groupByDay(moods);
    }

    public static Map<String, List<MoodResponse>> groupByMonth(List<MoodResponse> moods) {
        return MoodDateUtils.groupByMonth(moods);
    }

    /**
     * 计算一组心情的 Score → 占比 Map（5→1 顺序插入，占比之和为 1.0）。
     * 无记录时返回空 Map，供 MoodRatioRingView 绘制灰色虚线空圈。
     */
    public static Map<Integer, Float> buildScoreRatioMap(List<MoodResponse> moods) {
        Map<Integer, Float> ratioMap = new LinkedHashMap<>();
        if (moods == null || moods.isEmpty()) {
            return ratioMap;
        }

        int[] counts = new int[6];
        for (MoodResponse mood : moods) {
            int score = MoodColorConstants.resolveScore(mood);
            counts[score]++;
        }

        int total = 0;
        for (int score = MoodColorConstants.SCORE_VERY_BAD;
             score <= MoodColorConstants.SCORE_VERY_GOOD;
             score++) {
            total += counts[score];
        }
        if (total == 0) {
            return ratioMap;
        }

        for (int score = MoodColorConstants.SCORE_VERY_GOOD;
             score >= MoodColorConstants.SCORE_VERY_BAD;
             score--) {
            if (counts[score] > 0) {
                ratioMap.put(score, counts[score] / (float) total);
            }
        }
        return ratioMap;
    }

    /**
     * 统计当前周期内五种心情的百分比，索引 0=很好 … 4=很不好。
     */
    public static int[] calculateDistributionPercents(List<MoodResponse> periodMoods) {
        int[] percents = new int[5];
        if (periodMoods == null || periodMoods.isEmpty()) {
            return percents;
        }

        int[] counts = new int[5];
        for (MoodResponse mood : periodMoods) {
            int score = MoodColorConstants.resolveScore(mood);
            int index = MoodColorConstants.SCORE_VERY_GOOD - score;
            if (index >= 0 && index < counts.length) {
                counts[index]++;
            }
        }

        int total = 0;
        for (int count : counts) {
            total += count;
        }
        if (total == 0) {
            return percents;
        }

        int assigned = 0;
        for (int i = 0; i < counts.length; i++) {
            if (i == counts.length - 1) {
                percents[i] = Math.max(0, 100 - assigned);
            } else {
                percents[i] = Math.round(counts[i] * 100f / total);
                assigned += percents[i];
            }
        }
        return percents;
    }

    public static int countRecordedDaysInWeek(List<MoodResponse> weekMoods) {
        return groupByDay(weekMoods).size();
    }

    public static float[] buildWeekTrendPoints(List<MoodResponse> weekMoods, LocalDate weekStart) {
        float[] points = new float[7];
        Map<String, List<MoodResponse>> dayMap = groupByDay(weekMoods);
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<MoodResponse> dayMoods = dayMap.get(MoodDateUtils.dayKey(day));
            points[i] = averageScore(dayMoods);
        }
        return points;
    }

    public static float[] buildMonthTrendPoints(List<MoodResponse> monthMoods,
                                                int year,
                                                int month,
                                                int daysInMonth) {
        float[] points = new float[daysInMonth];
        Map<String, List<MoodResponse>> dayMap = groupByDay(monthMoods);
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            List<MoodResponse> dayMoods = dayMap.get(MoodDateUtils.dayKey(date));
            points[day - 1] = averageScore(dayMoods);
        }
        return points;
    }

    public static float[] buildYearTrendPoints(List<MoodResponse> yearMoods, int year) {
        float[] points = new float[12];
        Map<String, List<MoodResponse>> monthMap = groupByMonth(yearMoods);
        for (int month = 1; month <= 12; month++) {
            String key = String.format(java.util.Locale.CHINA, "%d-%02d", year, month);
            points[month - 1] = averageScore(monthMap.get(key));
        }
        return points;
    }

    private static float averageScore(List<MoodResponse> moods) {
        if (moods == null || moods.isEmpty()) {
            return 0f;
        }
        float sum = 0f;
        for (MoodResponse mood : moods) {
            sum += MoodColorConstants.resolveScore(mood);
        }
        return sum / moods.size();
    }
}
